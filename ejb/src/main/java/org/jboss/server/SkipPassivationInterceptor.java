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

import java.io.IOException;
import java.io.Serializable;

import javax.ejb.PrePassivate;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;
import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.SerializabilityChecker;
import org.jboss.stdio.NullOutputStream;

public class SkipPassivationInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    public static interface IsSerializable {
        boolean isSerializable();
    }

    private static Logger log = Logger.getLogger(SkipPassivationInterceptor.class.getSimpleName());

    private void checkJBossMarshalling(InvocationContext context) throws IOException {
        final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("river");
        final MarshallingConfiguration configuration = new MarshallingConfiguration();

        configuration.setSerializabilityChecker(new SerializabilityChecker() {
            @Override
            public boolean isSerializable(Class<?> targetClass) {
                return (targetClass != Object.class) || SerializabilityChecker.DEFAULT.isSerializable(targetClass);
            }
        });

        log.infof("now trying to marshall %s", context.getTarget());
        
        try (Marshaller marshaller = marshallerFactory.createMarshaller(configuration)) {
            marshaller.start(Marshalling.createByteOutput(NullOutputStream.getInstance()));
            marshaller.writeObject(context.getTarget());
            marshaller.finish();
            log.infof("marshalling on %s done", context.getTarget());
        } catch (Exception e) {
            log.error("Exception while Marshalling!", e);
            throw new IOException("SFSB is not Serializable, so preventing passivation");
        }
    }

    private boolean checkIsSerializable(InvocationContext context) throws IOException {
        if (context.getTarget() instanceof IsSerializable) {
            if (!((IsSerializable) context.getTarget()).isSerializable()) {
                throw new IOException("SFSB is not Serializable, so preventing passivation");
            }
            return true;
        }
        return false;
    }

    @PrePassivate
    public void prePassivate(InvocationContext context) throws Exception {
        // check the SFSB to see if it is passivatable or not
        log.info("Checking if the SFSB is Serializable: " + context.getTarget());

        // If the SFSB instance has a way to check if it is serializable, then you could
        // have it have a method to indicate this
        // and then if it is not serializable just throw
        if (checkIsSerializable(context))
            return;

        // if you do not, you can call checkJBossMarshalling
        // which will throw if it is not serializable
        checkJBossMarshalling(context);
    }

}
