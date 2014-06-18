/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.ide.test.impl;

import static org.apache.sling.ide.test.impl.helpers.EclipseResourceMatchers.hasFile;
import static org.apache.sling.ide.test.impl.helpers.EclipseResourceMatchers.hasFolder;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;

import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.apache.sling.ide.eclipse.ui.internal.ImportRepositoryContentAction;
import org.apache.sling.ide.test.impl.helpers.DisableDebugStatusHandlers;
import org.apache.sling.ide.test.impl.helpers.ExternalSlingLaunchpad;
import org.apache.sling.ide.test.impl.helpers.LaunchpadConfig;
import org.apache.sling.ide.test.impl.helpers.ProjectAdapter;
import org.apache.sling.ide.test.impl.helpers.RepositoryAccessor;
import org.apache.sling.ide.test.impl.helpers.ServerAdapter;
import org.apache.sling.ide.test.impl.helpers.SlingWstServer;
import org.apache.sling.ide.test.impl.helpers.TemporaryProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;


public class ContentImportTest {

    private final LaunchpadConfig config = LaunchpadConfig.getInstance();

    private final SlingWstServer wstServer = new SlingWstServer(config);

    @Rule
    public TestRule chain = RuleChain.outerRule(new ExternalSlingLaunchpad(config)).around(wstServer);

    @Rule
    public TemporaryProject projectRule = new TemporaryProject();

    @Rule
    public DisableDebugStatusHandlers disableDebugHandlers = new DisableDebugStatusHandlers();

    @Test
    public void importFilesAndFolders() throws Exception {

        // create faceted project
        IProject contentProject = projectRule.getProject();

        ProjectAdapter project = new ProjectAdapter(contentProject);
        project.addNatures(JavaCore.NATURE_ID, "org.eclipse.wst.common.project.facet.core.nature");

        // install bundle facet
        project.installFacet("sling.content", "1.0");

        wstServer.waitForServerToStart();

        ServerAdapter server = new ServerAdapter(wstServer.getServer());
        server.installModule(contentProject);

        project.createVltFilterWithRoots("/content/test-root/en");
        project.createOrUpdateFile(Path.fromPortableString("jcr_root/content/test-root/en/hello.txt"),
                new ByteArrayInputStream("hello, world".getBytes()));

        // create server-side content
        RepositoryAccessor repo = new RepositoryAccessor(config);
        repo.createNode("/content/test-root/en", "nt:folder");
        repo.createNode("/content/test-root/en/files", "nt:folder");
        repo.createFile("/content/test-root/en/files/first.txt", "first file".getBytes());

        ImportRepositoryContentAction action = new ImportRepositoryContentAction(wstServer.getServer(),
                Path.fromPortableString("/content/test-root"), contentProject, Activator.getDefault()
                        .getSerializationManager());
        action.run(new NullProgressMonitor());

        assertThat(contentProject, hasFolder("jcr_root/content/test-root/en/files"));
        assertThat(contentProject, hasFile("jcr_root/content/test-root/en/files/first.txt", "first file".getBytes()));
    }

    @Test
    public void importFilesAndFoldersRespectsVltFilters() throws Exception {

        // create faceted project
        IProject contentProject = projectRule.getProject();

        ProjectAdapter project = new ProjectAdapter(contentProject);
        project.addNatures(JavaCore.NATURE_ID, "org.eclipse.wst.common.project.facet.core.nature");

        // install bundle facet
        project.installFacet("sling.content", "1.0");

        wstServer.waitForServerToStart();

        ServerAdapter server = new ServerAdapter(wstServer.getServer());
        server.installModule(contentProject);

        project.createVltFilterWithRoots("/content/test-root/en");
        project.createOrUpdateFile(Path.fromPortableString("jcr_root/content/test-root/en/hello.txt"),
                new ByteArrayInputStream("hello, world".getBytes()));

        // create server-side content
        RepositoryAccessor repo = new RepositoryAccessor(config);
        repo.createNode("/content/test-root/de", "nt:folder");
        repo.createNode("/content/test-root/de/files", "nt:folder");
        repo.createFile("/content/test-root/de/files/first.txt", "first file".getBytes());

        ImportRepositoryContentAction action = new ImportRepositoryContentAction(wstServer.getServer(),
                Path.fromPortableString("/content/test-root"), contentProject, Activator.getDefault()
                        .getSerializationManager());
        action.run(new NullProgressMonitor());

        assertThat(contentProject.findMember("jcr_root/content/test-root/de"), nullValue());
    }

    @Test
    public void importFilesAndFoldersRespectsVltIgnore() throws Exception {

        // create faceted project
        IProject contentProject = projectRule.getProject();

        ProjectAdapter project = new ProjectAdapter(contentProject);
        project.addNatures(JavaCore.NATURE_ID, "org.eclipse.wst.common.project.facet.core.nature");

        // install bundle facet
        project.installFacet("sling.content", "1.0");

        wstServer.waitForServerToStart();

        ServerAdapter server = new ServerAdapter(wstServer.getServer());
        server.installModule(contentProject);

        project.createVltFilterWithRoots("/content/test-root/en");
        project.createOrUpdateFile(Path.fromPortableString("jcr_root/content/test-root/.vltignore"),
                new ByteArrayInputStream("en\n".getBytes()));

        // create server-side content
        RepositoryAccessor repo = new RepositoryAccessor(config);
        repo.createNode("/content/test-root/en", "nt:folder");
        repo.createNode("/content/test-root/en/files", "nt:folder");
        repo.createFile("/content/test-root/en/files/first.txt", "first file".getBytes());

        ImportRepositoryContentAction action = new ImportRepositoryContentAction(wstServer.getServer(),
                Path.fromPortableString("/content/test-root"), contentProject, Activator.getDefault()
                        .getSerializationManager());
        action.run(new NullProgressMonitor());

        assertThat(contentProject.findMember("jcr_root/content/test-root/en"), nullValue());
    }

    @Test
    public void importFilesAndFoldersRespectsVltIgnoreNotUnderImportRoot() throws Exception {

        // create faceted project
        IProject contentProject = projectRule.getProject();

        ProjectAdapter project = new ProjectAdapter(contentProject);
        project.addNatures(JavaCore.NATURE_ID, "org.eclipse.wst.common.project.facet.core.nature");

        // install bundle facet
        project.installFacet("sling.content", "1.0");

        wstServer.waitForServerToStart();

        ServerAdapter server = new ServerAdapter(wstServer.getServer());
        server.installModule(contentProject);

        project.createVltFilterWithRoots("/content/test-root/en");
        project.createOrUpdateFile(Path.fromPortableString("jcr_root/content/hello.txt"), new ByteArrayInputStream(
                "hello, world".getBytes()));

        project.createOrUpdateFile(Path.fromPortableString("jcr_root/.vltignore"), new ByteArrayInputStream(
                "content/test-root/en\n".getBytes()));

        // create server-side content
        RepositoryAccessor repo = new RepositoryAccessor(config);
        repo.createNode("/content/test-root/en", "nt:folder");
        repo.createNode("/content/test-root/en/files", "nt:folder");
        repo.createFile("/content/test-root/en/files/first.txt", "first file".getBytes());

        ImportRepositoryContentAction action = new ImportRepositoryContentAction(wstServer.getServer(),
                Path.fromPortableString("/content/test-root"), contentProject, Activator.getDefault()
                        .getSerializationManager());
        action.run(new NullProgressMonitor());

        assertThat(contentProject.findMember("jcr_root/content/test-root/en"), nullValue());
    }

    @Before
    public void setUp() throws Exception {
        RepositoryAccessor repo = new RepositoryAccessor(config);
        repo.createNode("/content", "nt:folder");
        repo.createNode("/content/test-root", "nt:folder");
    }

    @After
    public void cleanUp() throws Exception {
        new RepositoryAccessor(config).tryDeleteResource("/content/test-root");
    }
}