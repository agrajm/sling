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
package org.apache.sling.replication.packaging.impl.importer;

import java.io.InputStream;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.replication.agent.ReplicationComponentFactory;
import org.apache.sling.replication.agent.ReplicationComponentProvider;
import org.apache.sling.replication.packaging.ReplicationPackage;
import org.apache.sling.replication.packaging.ReplicationPackageImporter;
import org.apache.sling.replication.serialization.ReplicationPackageReadingException;
import org.apache.sling.replication.transport.authentication.TransportAuthenticationProvider;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote implementation of {@link org.apache.sling.replication.packaging.ReplicationPackageImporter}
 */
@Component(label = "Sling Replication - Remote Package Importer Factory",
        metatype = true,
        configurationFactory = true,
        specVersion = "1.1",
        policy = ConfigurationPolicy.REQUIRE)
@Service(value = ReplicationPackageImporter.class)
public class RemoteReplicationPackageImporterFactory implements ReplicationPackageImporter, ReplicationComponentProvider {
    private static final String TRANSPORT_AUTHENTICATION_PROVIDER_TARGET = ReplicationComponentFactory.COMPONENT_TRANSPORT_AUTHENTICATION_PROVIDER + ".target";


    private final Logger log = LoggerFactory.getLogger(getClass());

    @Property(value = ReplicationComponentFactory.PACKAGE_IMPORTER_REMOTE, propertyPrivate = true)
    private static final String TYPE = ReplicationComponentFactory.COMPONENT_TYPE;

    @Property
    private static final String NAME = ReplicationComponentFactory.COMPONENT_NAME;

    @Property(name = TRANSPORT_AUTHENTICATION_PROVIDER_TARGET)
    @Reference(name = "TransportAuthenticationProvider", policy = ReferencePolicy.STATIC)
    private volatile TransportAuthenticationProvider transportAuthenticationProvider;

    @Property(cardinality = 100)
    public static final String ENDPOINTS = ReplicationComponentFactory.PACKAGE_IMPORTER_REMOTE_PROPERTY_ENDPOINTS;

    @Property(options = {
            @PropertyOption(name = "All",
                    value = "all endpoints"
            ),
            @PropertyOption(name = "One",
                    value = "one endpoint"
            )},
            value = "One"
    )
    private static final String ENDPOINT_STRATEGY = ReplicationComponentFactory.PACKAGE_IMPORTER_REMOTE_PROPERTY_ENDPOINTS_STRATEGY;

    private ReplicationPackageImporter importer;

    @Reference
    ReplicationComponentFactory replicationComponentFactory;

    @Activate
    protected void activate(BundleContext context, Map<String, Object> config) throws Exception {

        importer = replicationComponentFactory.createComponent(ReplicationPackageImporter.class, config, this);

    }

    public boolean importPackage(ResourceResolver resourceResolver, ReplicationPackage replicationPackage) throws ReplicationPackageReadingException {
        return importer.importPackage(resourceResolver, replicationPackage);
    }

    public ReplicationPackage readPackage(ResourceResolver resourceResolver, InputStream stream) throws ReplicationPackageReadingException {
        return importer.readPackage(resourceResolver, stream);
    }

    public <ComponentType> ComponentType getComponent(Class<ComponentType> type, String componentName) {
        if (type.isAssignableFrom(TransportAuthenticationProvider.class)) {
            return (ComponentType) transportAuthenticationProvider;
        }

        return null;
    }
}