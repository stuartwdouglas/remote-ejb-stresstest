/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.client;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.server.SessionBeanRemote;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ParallelClient {

    static final String LOGGER_CATEGORY = "org.jboss.client.ParallelClient";
    
    private static Logger log = Logger.getLogger(LOGGER_CATEGORY);
    private static volatile Context ctx = null;

    public static void main(String[] args) throws Exception {

        String host = System.getProperty("host", "localhost");
        host = (host.equals("${host}")) ? "localhost" : host;

        String type = System.getProperty("type", "slsb");
        type = (type.equals("${type}")) ? "slsb" : type;

        int port = Integer.getInteger("port", 8080);
        long runtime = Long.getLong("runtime", 300l) * 1000;
        long delay = Long.getLong("delay", 0l);
        int poolSize = Integer.getInteger("poolSize", 20);
        int iterations = Integer.getInteger("iterations", 0);
        int calls = Integer.getInteger("calls", 20);

        final String jndiName;

        switch (type) {
            case "slsb":
                jndiName = "ejb:remote-ejb-stresstest-ear/remote-ejb-stresstest-ejb/SimpleStatelessSessionBean!org.jboss.server.SessionBeanRemote";
                break;
            case "sfsb":
                jndiName = "ejb:remote-ejb-stresstest-ear/remote-ejb-stresstest-ejb/SimpleStatefulSessionBean!org.jboss.server.SessionBeanRemote?stateful";
                break;
            default:
                throw new IllegalArgumentException("SystemProperty 'type' must be one of [slsb|sfsb], it's ["+type+"]");
        }

        log.infof("Using type '%s' with '%s' on '%s:%d', executing %d method calls", type, jndiName, host, port, calls);

        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(poolSize);
        taskExecutor.setMaxPoolSize(poolSize);
        taskExecutor.setQueueCapacity(poolSize);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(10 * 60);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setThreadNamePrefix("t-");
        taskExecutor.initialize();

        long now = System.currentTimeMillis();
        
        long start = System.currentTimeMillis();
        long end = now + runtime;

        Context ctx = getInitialContext(host, port);

        Statistics.Builder statisticsBuilder = new Statistics.Builder();
        
        if (iterations == 0) {
            while (now < end) {
                iterations++;

                Future<InvocationResult> future = taskExecutor.submit(new ThreadProcessor(ctx, iterations, jndiName, calls));
                InvocationResult ir = future.get();
                statisticsBuilder.addInvocationResult(ir);
                
                log.debugf("TotalDuration: %d, AverageDuration: %d, Total calls: %d", ir.getTotalDuration(), ir.getAvgDuration(), ir.getCallCount());
                
                Thread.sleep(delay);
                now = System.currentTimeMillis();
            }
        } else {
            for (int i = 1; i <= iterations; i++) {
                Future<InvocationResult> future = taskExecutor.submit(new ThreadProcessor(ctx, iterations, jndiName, calls));
                InvocationResult ir = future.get();
                statisticsBuilder.addInvocationResult(ir);

                log.debugf("TotalDuration: %d, AverageDuration: %d, Total calls: %d", ir.getTotalDuration(), ir.getAvgDuration(), ir.getCallCount());

                Thread.sleep(delay);
            }
        }

        taskExecutor.shutdown();
        long stop = System.currentTimeMillis();
        
        Statistics stats = statisticsBuilder.build();
        stats.dump();
        
        long totalDuration = stop - start;
        
        log.infof("#%d request in total á #%d method calls, took %dms in total, which is an overal client runtime average of %dms", iterations, calls, totalDuration, Long.valueOf(totalDuration/iterations));
    }

    public static Context getInitialContext(String host1, Integer port1) throws NamingException {
        if (ctx == null) {
            synchronized (ParallelClient.class) {
                if (ctx == null) {
                    Properties contextProperties = new Properties();
                    contextProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
                    ctx = new InitialContext(contextProperties);
                }
            }
        }
        return ctx;
    }
}

class Statistics {
    
    private static Logger logger = Logger.getLogger(ParallelClient.LOGGER_CATEGORY);
    
    TreeMap<String, Set<InvocationResult>> invocationResultsByThreadIds = new TreeMap<>(new Comparator<String>() {
        public int compare(String o1, String o2) {
            return extractInt(o1) - extractInt(o2);
        }

        int extractInt(String s) {
            String num = s.replaceAll("\\D", "");
            // return 0 if no digits found
            return num.isEmpty() ? 0 : Integer.parseInt(num);
        }
    });
    
    private Statistics(Builder builder) {
        for(InvocationResult ir : builder.invocationResults) {
            String threadId = ir.getThreadId();
            
            Set<InvocationResult> invocationResults = invocationResultsByThreadIds.get(threadId); 
            
            if(invocationResults == null) {
                invocationResults = new HashSet<>();
                invocationResultsByThreadIds.put(threadId, invocationResults);
            }
            invocationResults.add(ir);
        }
    }
    
    public void dump() {
        
        logger.info(" Thread | executions |   min |   avg |   max");
        logger.info("--------+------------+-------+-------+-------");
        
        for(Map.Entry<String, Set<InvocationResult>> entry : invocationResultsByThreadIds.entrySet()) {
            
            Set<InvocationResult> ir = entry.getValue();
            
            long min = ir.stream().min(Comparator.comparingLong(InvocationResult::getTotalDuration)).get().getTotalDuration();
            long avg = Long.valueOf( ir.stream().collect(Collectors.summingLong(InvocationResult::getTotalDuration)).longValue() / ir.size());
            long max = ir.stream().max(Comparator.comparingLong(InvocationResult::getTotalDuration)).get().getTotalDuration();
            
            logger.infof(" %-6s | %10d | %5d | %5d | %5d", entry.getKey(), ir.size(), min, avg, max);
        }
        
        logger.info("--------+------------+-------+-------+-------");
    }

    public static class Builder {
    
        Set<InvocationResult> invocationResults = new HashSet<>();
        
        public Builder addInvocationResult(InvocationResult ir) {
            invocationResults.add(ir);
            return this;
        }
        
        public Statistics build() {
            return new Statistics(this);
        }
    }
}

class InvocationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    String threadId;
    int iteration;
    long totalDuration;
    HashMap<Integer, Long> callDurations = new HashMap<>();

    private InvocationResult(Builder builder) {
        this.threadId = builder.threadId;
        this.iteration = builder.iteration;
        this.totalDuration = builder.totalDuration;
        this.callDurations = builder.callDurations;
    }
    
    public String getThreadId() {
        return this.threadId;
    }
    
    public int getCallCount() {
        return callDurations.size();
    }

    public long getFirstDuration() {
        return TimeUnit.MILLISECONDS.convert(callDurations.get(0), TimeUnit.NANOSECONDS);
    }
    
    public long getMinDuration() {
        return TimeUnit.MILLISECONDS.convert(callDurations.values().stream().mapToLong(l -> l).min().getAsLong(), TimeUnit.NANOSECONDS);
    }
    
    public long getAvgDuration() {
        return TimeUnit.MILLISECONDS.convert(Double.valueOf(callDurations.values().stream().mapToLong(l -> l).average().getAsDouble()).longValue(), TimeUnit.NANOSECONDS);
    }

    public long getMaxDuration() {
        return TimeUnit.MILLISECONDS.convert(callDurations.values().stream().mapToLong(l -> l).max().getAsLong(), TimeUnit.NANOSECONDS);
    }

    public long getTotalDuration() {
        return TimeUnit.MILLISECONDS.convert(this.totalDuration, TimeUnit.NANOSECONDS);
    }

    public static class Builder {

        String threadId;
        int iteration;
        long totalDuration;
        HashMap<Integer, Long> callDurations = new HashMap<>();
        
        public Builder onIteration(int iteration) {
            this.iteration = iteration;
            return this;
        }
        
        public Builder withThreadId(String threadId) {
            this.threadId = threadId;
            return this;
        }

        public Builder withSingleCallDuration(int callNumber, long singleDuration) {
            this.callDurations.put(callNumber, singleDuration);
            return this;
        }
        
        public Builder withTotalDuration(long totalDuration) {
            this.totalDuration = totalDuration;
            return this;
        }
        
        public InvocationResult build() {
            return new InvocationResult(this);
        }
    }
}

class ThreadProcessor implements Callable<InvocationResult> {

    private static Logger logger = Logger.getLogger(ParallelClient.LOGGER_CATEGORY);
    private String jndiName;
    private int iteration = 0;
    private int calls;
    private Context ctx;

    public ThreadProcessor(Context ctx, int iteration, String jndiName, int calls) {
        this.calls = calls;
        this.ctx = ctx;
        this.jndiName = jndiName;
        this.iteration = iteration;
    }

    @Override
    public InvocationResult call() throws Exception {

        try {
            SessionBeanRemote ejb = (SessionBeanRemote) ctx.lookup(jndiName);

            String threadId = String.valueOf(Thread.currentThread().getId());
            threadId = Thread.currentThread().getName();

            long t0 = System.nanoTime();

            InvocationResult.Builder builder = new InvocationResult.Builder().withThreadId(threadId);
            
            for (int i = 1; i <= calls; i++) {
                long t1 = System.nanoTime();
                ejb.businessMethod(threadId);
                long singleDuration = (System.nanoTime() - t1);
                logger.debugf("method call #%d duration %dns (%dms)", i, singleDuration, TimeUnit.MILLISECONDS.convert(singleDuration, TimeUnit.NANOSECONDS));
                builder.withSingleCallDuration(i, singleDuration);
            }

            ejb.businessMethodDone();
            
            long duration = (System.nanoTime() - t0);

            logger.debugf("#%05d - processed %d calls on (%s) in %dns (%dms)", iteration, calls, threadId, duration, TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS));

            return builder.withTotalDuration(duration).build();
            
        } catch (NamingException e) {
            logger.debug(e);
            logger.errorf("NamingError: %s", e.getMessage());
            return null;
        }
    }
}
