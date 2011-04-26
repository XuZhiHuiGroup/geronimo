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

package org.apache.geronimo.kernel.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The standard non-editable ConfigurationManager implementation.  That is,
 * you can save a lost configurations and stuff, but not change the set of
 * GBeans included in a configuration.
 *
 * @version $Rev:386276 $ $Date$
 */

@GBean(j2eeType = "ConfigurationManager")
@OsgiService
public class KernelConfigurationManager extends SimpleConfigurationManager implements GBeanLifecycle {

    protected final Kernel kernel;
    protected final ManageableAttributeStore attributeStore;
    protected final PersistentConfigurationList configurationList;
    private final ArtifactManager artifactManager;
    private final ShutdownHook shutdownHook;
    private boolean online = true;

    public KernelConfigurationManager(@ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                                      @ParamReference(name = "Stores", namingType = "ConfigurationStore") Collection<ConfigurationStore> stores,
                                      @ParamReference(name = "AttributeStore", namingType = "AttributeStore") ManageableAttributeStore attributeStore,
                                      @ParamReference(name = "PersistentConfigurationList") PersistentConfigurationList configurationList,
                                      @ParamReference(name = "ArtifactManager", namingType = "ArtifactManager") ArtifactManager artifactManager,
                                      @ParamReference(name = "ArtifactResolver", namingType = "ArtifactResolver") ArtifactResolver artifactResolver,
                                      @ParamReference(name = "Repositories", namingType = "Repository") Collection<ListableRepository> repositories,
                                      @ParamReference(name = "Watchers") Collection<DeploymentWatcher> watchers,
                                      @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {

        super(stores,
                createArtifactResolver(artifactResolver, artifactManager, repositories),
                repositories, watchers, bundleContext);

        this.kernel = kernel;
        this.attributeStore = attributeStore;
        this.configurationList = configurationList;
        this.artifactManager = artifactManager;

        shutdownHook = new ShutdownHook(kernel, configurationModel);
    }

    private static ArtifactResolver createArtifactResolver(ArtifactResolver artifactResolver, ArtifactManager artifactManager, Collection<ListableRepository> repositories) {
        if (artifactResolver != null) {
            return artifactResolver;
        }
        //TODO no reference to this may cause problems
        return new DefaultArtifactResolver(artifactManager, repositories, null, Collections.<ConfigurationManager>emptyList());
    }

    @Override
    public synchronized LifecycleResults loadConfiguration(Artifact configurationId) throws NoSuchConfigException, LifecycleException {
        // todo hack for bootstrap deploy
        AbstractName abstractName = null;
        try {
            abstractName = Configuration.getConfigurationAbstractName(configurationId);
        } catch (InvalidConfigException e) {
            throw new RuntimeException(e);
        }
        if (getConfiguration(configurationId) == null && kernel.isLoaded(abstractName)) {
            try {
                Configuration configuration = (Configuration) kernel.getGBean(abstractName);
                //TODO someone loads the configuration by kernel directly ???
                if (!loadedConfigurationData.containsKey(configurationId)) {
                    loadedConfigurationData.put(configurationId, configuration.getConfigurationData());
                    addConfigurationModel(configurationId);
                }
                configurations.put(configurationId, configuration);
                configurationModel.load(configurationId);
                configurationModel.start(configurationId);
                return new LifecycleResults();
            } catch (GBeanNotFoundException e) {
                // configuration was unloaded, just continue as normal
            } catch (MissingDependencyException e) {
            }
        }

        return super.loadConfiguration(configurationId);
    }

    @Override
    protected void loadConfigurationModel(Artifact configurationId) throws NoSuchConfigException {
        super.loadConfigurationModel(configurationId);
        if (configurationList != null) {
            configurationList.addConfiguration(configurationId);
        }
    }

    @Override
    protected void migrateConfiguration(Artifact oldName, Artifact newName, Configuration configuration, boolean running) throws NoSuchConfigException {
        super.migrateConfiguration(oldName, newName, configuration, running);
        if (configurationList != null) {
            configurationList.migrateConfiguration(oldName, newName, configuration);
            if (running) {
                configurationList.startConfiguration(newName);
            }
        }
    }

    @Override
    protected Configuration start(ConfigurationData configurationData, Set<Artifact> resolvedParentIds, Map<Artifact, Configuration> loadedConfigurations) throws InvalidConfigException {
        Artifact configurationId = configurationData.getId();
        AbstractName configurationName = Configuration.getConfigurationAbstractName(configurationId);
        GBeanData gbeanData = new GBeanData(configurationName, Configuration.class);
        gbeanData.setAttribute("configurationData", configurationData);
        DependencyNode dependencyNode = null;
        ConfigurationResolver configurationResolver = new ConfigurationResolver(configurationData, repositories, getArtifactResolver());
        gbeanData.setAttribute("configurationResolver", configurationResolver);
        try {
            dependencyNode = buildDependencyNode(configurationData);
            gbeanData.setAttribute("dependencyNode", dependencyNode);
            gbeanData.setAttribute("allServiceParents", buildAllServiceParents(loadedConfigurations, dependencyNode));
        } catch (MissingDependencyException e) {
            throw new InvalidConfigException(e);
        }
        gbeanData.setAttribute("configurationManager", this);
        //TODO is this dangerous?  should really add dependency on attribute store name
        gbeanData.setAttribute("attributeStore", attributeStore);

        // add parents to the parents reference collection
        //TODO Only add those service parents as dependencies, all the class parents should be only required resolved
        LinkedHashSet<AbstractName> parentNames = new LinkedHashSet<AbstractName>();
        for (Artifact resolvedParentId : dependencyNode.getServiceParents()) {
            if (isConfiguration(resolvedParentId)) {
                AbstractName parentName = Configuration.getConfigurationAbstractName(resolvedParentId);
                parentNames.add(parentName);
            }
        }
        gbeanData.addDependencies(parentNames);

        // load the configuration
        try {
            //TODO OSGI more likely use the configuration bundle??
            kernel.loadGBean(gbeanData, bundleContext.getBundle());
        } catch (GBeanAlreadyExistsException e) {
            throw new InvalidConfigException("Unable to load configuration gbean " + configurationId, e);
        }

        // start the configuration and assure it started
        Configuration configuration;
        try {
            kernel.startGBean(configurationName);
            if (State.RUNNING_INDEX != kernel.getGBeanState(configurationName)) {
                String stateReason = kernel.getStateReason(configurationName);
                throw new InvalidConfigurationException("Configuration gbean failed to start " + configurationId + "\nreason: " + stateReason);
            }

            // get the configuration
            configuration = (Configuration) kernel.getGBean(configurationName);

            // declare the dependencies as loaded
            if (artifactManager != null) {
                artifactManager.loadArtifacts(configurationId, new LinkedHashSet<Artifact>());
            }
            Map<Artifact, Configuration> moreLoadedConfigurations = new LinkedHashMap<Artifact, Configuration>(loadedConfigurations);
            moreLoadedConfigurations.put(dependencyNode.getId(), configuration);
            for (Map.Entry<String, ConfigurationData> childEntry : configurationData.getChildConfigurations().entrySet()) {
                ConfigurationResolver childResolver = configurationResolver.createChildResolver(childEntry.getKey());
                Configuration child = doStart(childEntry.getValue(), resolvedParentIds, moreLoadedConfigurations, childResolver);
                configuration.addChild(child);
            }

            log.debug("Loaded Configuration {}", configurationName);
        } catch (Exception e) {
            unloadInternal(configurationId);
            if (e instanceof InvalidConfigException) {
                throw (InvalidConfigException) e;
            }
            throw new InvalidConfigException("Error starting configuration gbean " + configurationId, e);
        }
        return configuration;
    }

    @Override
    protected void startInternal(Configuration configuration) throws InvalidConfigException {
        if (online) {
            ConfigurationUtil.startConfigurationGBeans(configuration, kernel);
        }

        if (configurationList != null && configuration.getConfigurationData().isAutoStart()) {
            configurationList.startConfiguration(configuration.getId());
        }
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    protected void stopInternal(Configuration configuration) {
        stopRecursive(configuration);
        if (configurationList != null) {
            configurationList.stopConfiguration(configuration.getId());
        }
    }

    private void stopRecursive(Configuration configuration) {
        // stop all of the child configurations first
        for (Configuration childConfiguration :  configuration.getChildren()) {
            stopRecursive(childConfiguration);
        }

        // stop the gbeans
        for (Map.Entry<AbstractName, GBeanData> entry : configuration.getGBeans().entrySet()) {
            AbstractName gbeanName = entry.getValue().getAbstractName();
            try {
                kernel.stopGBean(gbeanName);
            } catch (GBeanNotFoundException ignored) {
            } catch (IllegalStateException ignored) {
            } catch (InternalKernelException kernelException) {
                log.debug("Error cleaning up after failed start of configuration " + configuration.getId() + " gbean " + gbeanName, kernelException);
            }
        }

        // unload the gbeans
        for (Map.Entry<AbstractName, GBeanData> entry : configuration.getGBeans().entrySet()) {
            AbstractName gbeanName = entry.getValue().getAbstractName();
            try {
                kernel.unloadGBean(gbeanName);
            } catch (GBeanNotFoundException ignored) {
            } catch (IllegalStateException ignored) {
            } catch (InternalKernelException kernelException) {
                log.debug("Error cleaning up after failed start of configuration " + configuration.getId() + " gbean " + gbeanName, kernelException);
            }
        }

        AbstractName configurationName = configuration.getAbstractName();

        if (artifactManager != null) {
            artifactManager.unloadAllArtifacts(configuration.getId());
        }

        // unload this configuration
        try {
            kernel.stopGBean(configurationName);
        } catch (GBeanNotFoundException ignored) {
            // Good
        } catch (Exception stopException) {
            log.warn("Unable to stop failed configuration: " + configuration.getId(), stopException);
        }

        try {
            kernel.unloadGBean(configurationName);
        } catch (GBeanNotFoundException ignored) {
            // Good
        } catch (Exception unloadException) {
            log.warn("Unable to unload failed configuration: " + configuration.getId(), unloadException);
        }
    }

    @Override
    protected void uninstallInternal(Artifact configurationId) {
        if (configurationList != null) {
            configurationList.removeConfiguration(configurationId);
        }
    }

    @Override
    public void doStart() {
        kernel.registerShutdownHook(shutdownHook);
    }

    @Override
    public void doStop() {
        kernel.unregisterShutdownHook(shutdownHook);
    }

    @Override
    public void doFail() {
        log.error("Cofiguration manager failed");
    }

    private static class ShutdownHook implements Runnable {
        private final Kernel kernel;
        private final ConfigurationModel configurationModel;
        private final Logger log = LoggerFactory.getLogger(ShutdownHook.class);

        public ShutdownHook(Kernel kernel, ConfigurationModel configurationModel) {
            this.kernel = kernel;
            this.configurationModel = configurationModel;
        }

        @Override
        public void run() {
            while (true) {
                Set configs = kernel.listGBeans(new AbstractNameQuery(Configuration.class.getName()));
                if (configs.isEmpty()) {
                    return;
                }
                LinkedHashSet orderedConfigs = new LinkedHashSet();
                for (Iterator i = configs.iterator(); i.hasNext();) {
                    AbstractName configName = (AbstractName) i.next();
                    if (kernel.isLoaded(configName) && !orderedConfigs.contains(configName)) {
                        LinkedHashSet startedChildren = configurationModel.getStartedChildren(configName.getArtifact());
                        for (Iterator iterator = startedChildren.iterator(); iterator.hasNext();) {
                            Artifact configurationId = (Artifact) iterator.next();
                            Set childConfig = kernel.listGBeans(new AbstractNameQuery(configurationId, Collections.emptyMap(), Configuration.class.getName()));
                            if (!childConfig.isEmpty()) {
                                AbstractName childConfigName = (AbstractName) childConfig.iterator().next();
                                if (!orderedConfigs.contains(childConfigName))
                                    orderedConfigs.add(childConfigName);
                            }
                        }
                        orderedConfigs.add(configName);
                    }
                }

                for (Iterator i = orderedConfigs.iterator(); i.hasNext();) {
                    AbstractName configName = (AbstractName) i.next();
                    try {
                        kernel.stopGBean(configName);
                    } catch (GBeanNotFoundException e) {
                        // ignore
                    } catch (InternalKernelException e) {
                        log.warn("Could not stop configuration: " + configName, e);
                    }
                    try {
                        kernel.unloadGBean(configName);
                    } catch (GBeanNotFoundException e) {
                        // ignore
                    }
                }
            }
        }
    }

}
