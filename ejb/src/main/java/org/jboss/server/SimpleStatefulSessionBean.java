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
package org.jboss.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.StatefulTimeout;
import javax.interceptor.Interceptors;

import org.jboss.ejb3.annotation.Cache;
import org.jboss.logging.Logger;
import org.jboss.server.SkipPassivationInterceptor.IsSerializable;

@Stateful
@Cache("passivating")
@Remote(SessionBeanRemote.class)
@StatefulTimeout(value=5, unit=TimeUnit.SECONDS)
@Interceptors({ MetricInterceptor.class, SkipPassivationInterceptor.class })
public class SimpleStatefulSessionBean implements SessionBeanRemote, IsSerializable {

    private static final Logger logger = Logger.getLogger(SimpleStatefulSessionBean.class);
    
    private static final int MAX = 20;
    private int lastResult = 0;
    private List<UnserializableData> grow = new ArrayList<UnserializableData>(MAX);

    public void businessMethod(String calledFrom) {

        doBusiness(calledFrom);
    }

    @Remove
    public void businessMethodDone() {
        //logger.infof("removing %s", this);
    }
    

    @Override
    public byte[] sizedBusinessMethod(String calledFrom, int responseSize) {

        doBusiness(calledFrom);
        
        return ByteArrayUtilities.getInstance().getByteArray(responseSize);
    }
    
    @Override
    public boolean isSerializable() {
        return false;
    }
    
    private void doBusiness(String calledFrom) {
//
//        for (int i = 0; i < MAX; i++) {
//            int result = new Random().nextInt(100) * new Random().nextInt(100);
//            grow.add(new UnserializableData("Thread-" + Thread.currentThread().getId(),  "sfdfs", result));
//        }
        
        //int sum = grow.stream().mapToInt(myData -> myData.getResult()).sum();
        
        //logger.infof("#%d MyData entries with a total result of %d, previous result was %d", grow.size(), sum, lastResult);
        
        lastResult = 67;
    }
}
