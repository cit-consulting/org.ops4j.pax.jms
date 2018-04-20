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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.junit.Test;

public class ExternalConfigLoaderTest {

    @Test
    public void testNoExternalConfig() {
        final Map<String, Object> expectedProps = new Hashtable<>();
        expectedProps.put("name", "testCF");
        expectedProps.put("timeout", 2000);
        
        Dictionary<String, Object> cfProps = new Hashtable<String, Object>(expectedProps);

        final ExternalConfigLoader externalConfigLoader = new ExternalConfigLoader();
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

        final ExternalConfigLoader externalConfigLoader = new ExternalConfigLoader();
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
}
