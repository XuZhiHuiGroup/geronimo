/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.deployment.plugin.jmx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.security.auth.login.FailedLoginException;

import org.apache.geronimo.deployment.plugin.GeronimoDeploymentManager;
import org.apache.geronimo.deployment.spi.ModuleConfigurer;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.NoSuchStoreException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.system.plugin.DownloadPoller;
import org.apache.geronimo.system.plugin.DownloadResults;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginRepositoryList;
import org.apache.geronimo.system.plugin.ServerArchiver;
import org.apache.geronimo.system.plugin.model.AttributesType;
import org.apache.geronimo.system.plugin.model.PluginListType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects to a Kernel in a remote VM (may or many not be on the same machine).
 *
 * @version $Rev$ $Date$
 */
public abstract class ExtendedDeploymentManager extends JMXDeploymentManager implements GeronimoDeploymentManager {
    
    private static final Logger log = LoggerFactory.getLogger(ExtendedDeploymentManager.class);

    public ExtendedDeploymentManager(Collection<ModuleConfigurer> moduleConfigurers) {
        super(moduleConfigurers);
    }

    public <T> T getImplementation(Class<T> clazz) {
        try {
            return kernel.getGBean(clazz);
        } catch (GBeanNotFoundException e) {
            throw new IllegalStateException("No implementation for " + clazz.getName(), e);
        }
    }
    
    protected <T> T getImplementation(AbstractName name, Class<T> clazz) {
        try {
            return clazz.cast(kernel.getGBean(name));
        } catch (GBeanNotFoundException e) {
            throw new IllegalStateException("No implementation for " + clazz.getName(), e);
        }
    }
    
    public PluginListType listPlugins(URL mavenRepository) throws FailedLoginException, IOException {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.listPlugins(mavenRepository);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }
    
    public boolean validatePlugin(PluginType plugin) throws MissingDependencyException {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.validatePlugin(plugin);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public Dependency[] checkPrerequisites(PluginType plugin) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.checkPrerequisites(plugin);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public DownloadResults install(PluginListType configsToInstall, String defaultRepository, boolean restrictToDefaultRepository, String username, String password) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.install(configsToInstall, defaultRepository, restrictToDefaultRepository, username, password);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public void install(PluginListType configsToInstall, String defaultRepository, boolean restrictToDefaultRepository, String username, String password, DownloadPoller poller) {
        PluginInstaller installer = getPluginInstaller();
        try {
            installer.install(configsToInstall, defaultRepository, restrictToDefaultRepository, username, password, poller);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public Object startInstall(PluginListType configsToInstall, String defaultRepository, boolean restrictToDefaultRepository, String username, String password) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.startInstall(configsToInstall, defaultRepository, restrictToDefaultRepository, username, password);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public Object startInstall(File carFile, String defaultRepository, boolean restrictToDefaultRepository, String username, String password) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.startInstall(carFile, defaultRepository, restrictToDefaultRepository, username, password);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public DownloadResults checkOnInstall(Object key) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.checkOnInstall(key);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public DownloadResults checkOnInstall(Object key, boolean remove) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.checkOnInstall(key, remove);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    private PluginInstaller getPluginInstaller() {
        return getImplementation(PluginInstaller.class);
    }
    
    private ServerArchiver getServerArchiver() {
        return getImplementation(ServerArchiver.class);
    }

    public PluginListType createPluginListForRepositories(String repo) throws NoSuchStoreException {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.createPluginListForRepositories(repo);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public Map<String, Artifact> getInstalledPlugins() {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.getInstalledPlugins();
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public PluginType getPluginMetadata(Artifact configId) {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.getPluginMetadata(configId);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public void updatePluginMetadata(PluginType metadata) {
        PluginInstaller installer = getPluginInstaller();
        try {
            installer.updatePluginMetadata(metadata);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public URL[] getRepositories() {
        List<URL> list = new ArrayList<URL>();
        Set<AbstractName> set = kernel.listGBeans(new AbstractNameQuery(PluginRepositoryList.class.getName()));
        for (AbstractName name : set) {
            PluginRepositoryList repo = getImplementation(name, PluginRepositoryList.class);
            try {
                list.addAll(repo.getRepositories());
            } finally {
                kernel.getProxyManager().destroyProxy(repo);
            }
        }
        return list.toArray(new URL[list.size()]);
    }

    public Artifact installLibrary(File libFile, String groupId) throws IOException {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.installLibrary(libFile, groupId);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public DownloadResults installPluginList(String targetRepositoryPath, String relativeTargetServerPath, PluginListType pluginList) throws Exception {
        PluginInstaller installer = getPluginInstaller();
        try {
            return installer.installPluginList(targetRepositoryPath, relativeTargetServerPath, pluginList);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public void mergeOverrides(String server, AttributesType overrides) throws InvalidGBeanException, IOException {
        PluginInstaller installer = getPluginInstaller();
        try {
            installer.mergeOverrides(server, overrides);
        } finally {
            kernel.getProxyManager().destroyProxy(installer);
        }
    }

    public File archive(String sourcePath, String destPath, Artifact artifact) throws IOException {
        ServerArchiver archiver = getServerArchiver();
        try {
            return archiver.archive(sourcePath, destPath, artifact);
        } finally {
            kernel.getProxyManager().destroyProxy(archiver);
        }
    }
    
    public Artifact[] getEBAConfigurationIds(){
        List<AbstractName> stores = configurationManager.listStores();
        if (stores.isEmpty()) {
            return null;
        }
        
        ArrayList<Artifact> result = new ArrayList<Artifact>();
        for (AbstractName store : stores) {
            List infos;
            try {
                infos = configurationManager.listConfigurations(store);
                for (Object info : infos) {
                    ConfigurationInfo configInfo = (ConfigurationInfo) info;
                    
                    if (ConfigurationModuleType.EBA.equals(configInfo.getType())) {
                        result.add(configInfo.getConfigID());
                    }
                }
            } catch (NoSuchStoreException e) {
                e.printStackTrace();
            }
        }

        return result.size() == 0 ? null : result.toArray(new Artifact[result.size()]);
    }
    
    public long[] getEBAContentBundleIds(AbstractName applicationGBeanName) throws GBeanNotFoundException, NoSuchOperationException, Exception{
        long[] ids = (long[])kernel.getAttribute(applicationGBeanName, "applicationContentBundleIds");
        
        return ids;
    }
    
    public String getEBAContentBundleSymbolicName(AbstractName applicationGBeanName, long bundleId) throws GBeanNotFoundException, NoSuchOperationException, Exception{
        Object name = kernel.invoke(applicationGBeanName, "getApplicationContentBundleSymbolicName", new Object[]{bundleId}, new String[]{long.class.getName()});
        if (name!=null) return (String)name;
        
        return null;
    }
    
    /**
     * Only support local bundle update
     */
    public void updateEBAContent(AbstractName applicationGBeanName, long bundleId, File newfile) throws GBeanNotFoundException, NoSuchOperationException, Exception{
        kernel.invoke(applicationGBeanName, "updateApplicationContent", new Object[]{bundleId, newfile.toURI()}, new String[]{long.class.getName(), newfile.toURI().getClass().getName()});
    }
    
    public AbstractName getApplicationGBeanName(Artifact configurationId) {
        Set<AbstractName> applicationGBeanNames = kernel.listGBeans(new AbstractNameQuery(configurationId, Collections.EMPTY_MAP, "org.apache.geronimo.aries.builder.ApplicationGBean"));
        if (applicationGBeanNames.size()!=1){
            throw new IllegalStateException("An EBA should have one and only one ApplicationGean object");
        }
        return applicationGBeanNames.iterator().next();
    }
    
}
