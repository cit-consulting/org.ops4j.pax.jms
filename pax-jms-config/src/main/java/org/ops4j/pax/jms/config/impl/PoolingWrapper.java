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

import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.jms.JMSRuntimeException;
import javax.jms.XAConnectionFactory;

import org.ops4j.pax.jms.service.ConnectionFactoryFactory;
import org.ops4j.pax.jms.service.PooledConnectionFactoryFactory;

public class PoolingWrapper implements ConnectionFactoryFactory {
    
    private PooledConnectionFactoryFactory pdsf;
    private ConnectionFactoryFactory dsf;

    public PoolingWrapper(PooledConnectionFactoryFactory pdsf, ConnectionFactoryFactory dsf) {
        this.pdsf = pdsf;
        this.dsf = dsf;
    }

    @Override
    public ConnectionFactory createConnectionFactory(Map<String, Object> props) throws JMSRuntimeException {
        return pdsf.create(dsf, props);
    }

    @Override
    public XAConnectionFactory createXAConnectionFactory(Map<String, Object> props) throws JMSRuntimeException {
        return dsf.createXAConnectionFactory(props);
    }

}
