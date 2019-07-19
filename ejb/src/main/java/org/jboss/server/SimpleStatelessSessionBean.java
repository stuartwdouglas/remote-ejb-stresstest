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

import java.util.Random;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.jboss.logging.Logger;

@Interceptors({ MetricInterceptor.class })
@Stateless
@Remote(SessionBeanRemote.class)
public class SimpleStatelessSessionBean implements SessionBeanRemote {

    private static final Logger logger = Logger.getLogger(SimpleStatelessSessionBean.class);
    
    public void businessMethod(String calledFrom) {
        //logger.infof("Client Thread (%s) - Result: %d", calledFrom, new Random().nextInt(100) * new Random().nextInt(100));
    }


    @Override
    public byte[] sizedBusinessMethod(String calledFrom, int responseSize) {
        logger.infof("Client Thread (%s) - Result: %d", calledFrom, new Random().nextInt(100) * new Random().nextInt(100));
        return ByteArrayUtilities.getInstance().getByteArray(responseSize);
    }
    
    @Override
    public void businessMethodDone() {
        // do nothing in a SLSB
    }
}
