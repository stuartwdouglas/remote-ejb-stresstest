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

import java.io.Serializable;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

public class MetricInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = Logger.getLogger(MetricInterceptor.class);
    
    @AroundInvoke
    public Object arroundInvoke(InvocationContext context) throws Exception {

        long t0 = System.currentTimeMillis();
        try {
            return context.proceed();
        } finally {
            long t1 = System.currentTimeMillis();
            //logger.infof("execution of %s took %dms", context.getTarget(), t1 - t0);
        }
    }
}
