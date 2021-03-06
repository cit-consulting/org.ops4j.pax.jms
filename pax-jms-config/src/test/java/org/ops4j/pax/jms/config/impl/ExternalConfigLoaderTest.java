/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.jms.config.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.jms.config.ConfigLoader;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.newCapture;
import static org.junit.Assert.assertEquals;

public class ExternalConfigLoaderTest {

    private IMocksControl c;
    private BundleContext context;

    @Before
    public void setup() throws Exception {
        c = createControl();
        context = c.createMock(BundleContext.class);
        Capture<String> capture = newCapture();
        expect(context.createFilter(capture(capture))).andStubAnswer(() -> FrameworkUtil.createFilter(capture.getValue()));
        context.addServiceListener(anyObject(ServiceListener.class), anyString());
        ServiceReference ref1 = c.createMock(ServiceReference.class);
        ServiceReference ref2 = c.createMock(ServiceReference.class);
        ServiceReference[] refs = new ServiceReference[]{ref1, ref2};
        String filter = "(" + Constants.OBJECTCLASS + "=" + ConfigLoader.class.getName() + ")";
        expect(context.getServiceReferences((String) null, filter)).andReturn(refs);
        expect(context.getService(ref1)).andReturn(new FileConfigLoader());
        expect(context.getService(ref2)).andReturn(new CustomConfigLoader());
        c.replay();
    }

    @Test
    public void testNoExternalConfig() {
        final Map<String, Object> expectedProps = new Hashtable<>();
        expectedProps.put("name", "testCF");
        expectedProps.put("timeout", 2000);

        Dictionary<String, Object> cfProps = new Hashtable<String, Object>(expectedProps);

        final ExternalConfigLoader externalConfigLoader = new ExternalConfigLoader(context);
        externalConfigLoader.resolve(cfProps);

        for (Enumeration<String> e = cfProps.keys(); e.hasMoreElements();) {
            String key = e.nextElement();
            String expectedValue = String.valueOf(expectedProps.get(key));
            String actualValue = String.valueOf(cfProps.get(key));
            assertEquals(expectedValue, actualValue);
        }
    }

    @Test
    public void testExternalConfig() {
        final String myExternalPassword = createExternalSecret("password");

        Dictionary<String, Object> cfProps = new Hashtable<>();
        cfProps.put("name", "testCF");
        cfProps.put("password", "FILE(" + myExternalPassword + ")");
        cfProps.put("timeout", 2000);

        final ExternalConfigLoader externalConfigLoader = new ExternalConfigLoader(context);
        Dictionary<String, Object> loaded = externalConfigLoader.resolve(cfProps);

        assertEquals("testCF", loaded.get("name"));
        assertEquals("password", loaded.get("password"));
        assertEquals(2000, loaded.get("timeout"));
    }

    @Test
    public void testCustomExternalConfig() {
        Dictionary<String, Object> cfProps = new Hashtable<>();
        cfProps.put("name", "testCF");
        cfProps.put("password", "CUSTOM(password)");
        cfProps.put("timeout", 2000);

        final ExternalConfigLoader externalConfigLoader = new ExternalConfigLoader(context);
        Dictionary<String, Object> loaded = externalConfigLoader.resolve(cfProps);

        assertEquals("testCF", loaded.get("name"));
        assertEquals("password", loaded.get("password"));
        assertEquals(2000, loaded.get("timeout"));
    }

    public static String createExternalSecret(final String value) {
        try {
            final File file = File.createTempFile("externalPaxJmsConfig-", ".secret");
            file.deleteOnExit();

            System.out.println("CREATED SECRET: " + file.getAbsolutePath());

            Files.write(Paths.get(file.toURI()), value.getBytes());

            return file.getAbsolutePath();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create temporary secret file", ex);
        }
    }

    public static class CustomConfigLoader implements ConfigLoader {

        @Override
        public String getName() {
            return "CUSTOM";
        }

        @Override
        public String resolve(String key) {
            return key;
        }
    }
}
