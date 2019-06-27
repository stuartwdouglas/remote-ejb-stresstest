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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.jboss.server.UnserializableData;

public class SimpleInvocationContext implements InvocationContext {

    UnserializableData data;
    
    public SimpleInvocationContext(UnserializableData data) {
        this.data = data;
    }
    
    @Override
    public Object getTarget() {
        return this.data;
    }

    @Override
    public Method getMethod() {
        throw new UnsupportedOperationException("method implementation not available");
    }

    @Override
    public Constructor<?> getConstructor() {
        throw new UnsupportedOperationException("method implementation not available");
    }

    @Override
    public Object[] getParameters() throws IllegalStateException {
        throw new UnsupportedOperationException("method implementation not available");
    }

    @Override
    public void setParameters(Object[] params) throws IllegalStateException, IllegalArgumentException {
        throw new UnsupportedOperationException("method implementation not available");
    }

    @Override
    public Map<String, Object> getContextData() {
        throw new UnsupportedOperationException("method implementation not available");
    }

    @Override
    public Object getTimer() {
        throw new UnsupportedOperationException("method implementation not available");
    }

    @Override
    public Object proceed() throws Exception {
        throw new UnsupportedOperationException("method implementation not available");
    }
}
