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
package org.jboss.server.rest;

import java.util.Random;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.jboss.server.SkipPassivationInterceptor;
import org.jboss.server.UnserializableData;

public class SimulatePassivationImpl implements SimulatePassivation {

    private static final Logger logger = Logger.getLogger(SimulatePassivationImpl.class);
    private static String RETURN_TEMPLATE = "Passivation triggered: %s";

    @Override
    public String trigger() {

        try {

            int result = new Random().nextInt(100) * new Random().nextInt(100);

            UnserializableData data = new UnserializableData("Thread-" + Thread.currentThread().getId(), UUID.randomUUID().toString(), result);
            
            logger.infof("trigger passivation on %s", data);
            
            SkipPassivationInterceptor interceptor = new SkipPassivationInterceptor();
            interceptor.prePassivate(new SimpleInvocationContext(data));
            
            return String.format(RETURN_TEMPLATE, "true");
        } catch (Exception e) {
            logger.error("Error triggering passivation", e);
            return String.format(RETURN_TEMPLATE, "false");
        }
    }
}
