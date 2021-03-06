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
package org.apache.sling.nosql.generic.resource.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Test monting NoSqlResourceProvider as root resource provider.
 */
public abstract class AbstractNoSqlResourceProviderRootTest {
    
    @Rule
    public SlingContext context = new SlingContext(ResourceResolverType.NONE);
    
    protected abstract void registerResourceProviderFactoryAsRoot();

    @Before
    public void setUp() throws Exception {
        registerResourceProviderFactoryAsRoot();
    }
    
    @After
    public void tearDown() {
        context.resourceResolver().revert();
    }
    
    @Test
    public void testRoot() {
        Resource root = context.resourceResolver().getResource("/");
        assertNotNull(root);
        assertTrue(root instanceof NoSqlResource);
    }

    @Test
    public void testCreatePath() throws PersistenceException {
        ResourceUtil.getOrCreateResource(context.resourceResolver(), "/test/test1",
                ImmutableMap.<String, Object>of(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED),
                JcrConstants.NT_UNSTRUCTURED, true);
        
        Resource test = context.resourceResolver().getResource("/test");
        assertNotNull(test);
        
        Resource test1 = context.resourceResolver().getResource("/test/test1");
        assertNotNull(test1);
        
        context.resourceResolver().delete(test);
    }
    
    @Test(expected = PersistenceException.class)
    public void testDeleteRootPath() throws PersistenceException {
        Resource root = context.resourceResolver().getResource("/");
        context.resourceResolver().delete(root);
    }

    @Test(expected = Throwable.class)
    public void testUpdateRootPath() throws PersistenceException {
        Resource root = context.resourceResolver().getResource("/");
        ModifiableValueMap props = root.adaptTo(ModifiableValueMap.class);
        props.put("prop1", "value1");
        context.resourceResolver().commit();
    }

}
