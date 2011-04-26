/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.system.configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.wrapper.AbstractServiceWrapper;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.model.GbeanType;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */

@GBean
public class WrapperAttributeManager extends AbstractServiceWrapper<PluginAttributeStore> implements PluginAttributeStore, PersistentConfigurationList  {


    public WrapperAttributeManager(@ParamSpecial(type = SpecialAttributeType.bundle)final Bundle bundle) {
        super(bundle, PluginAttributeStore.class);
    }
    
    private PersistentConfigurationList getPersistentConfigurationList() {
        return (PersistentConfigurationList)get();
    }

    public void addConfigSubstitutions(Map<String, String> properties) {
        get().addConfigSubstitutions(properties);
    }

    public boolean isModuleInstalled(Artifact artifact) {
        return get().isModuleInstalled(artifact);
    }

    public void setModuleGBeans(Artifact moduleName, List<GbeanType> gbeans, boolean load, String condition) throws InvalidGBeanException {
        get().setModuleGBeans(moduleName, gbeans, load, condition);
    }

    public String substitute(String in) {
        return get().substitute(in);
    }

    @Override
    public String getServerName() {
        return get().getServerName();
    }

    public void addGBean(Artifact configurationName, GBeanData gbeanData, Bundle bundle) {
        get().addGBean(configurationName, gbeanData, bundle);
    }

    public Collection<GBeanData> applyOverrides(Artifact configurationName, Collection<GBeanData> datas, Bundle bundle) throws InvalidConfigException {
        return get().applyOverrides(configurationName, datas, bundle);
    }

    @Override
    public void save() throws IOException {
        get().save();
    }

    public void setKernelFullyStarted(boolean kernelFullyStarted) {
        getPersistentConfigurationList().setKernelFullyStarted(kernelFullyStarted);
    }

    public void startConfiguration(Artifact configName) {
        getPersistentConfigurationList().startConfiguration(configName);
    }

    public void stopConfiguration(Artifact configName) {
        getPersistentConfigurationList().stopConfiguration(configName);
    }

    public void setReferencePatterns(Artifact configurationName, AbstractName gbean, GReferenceInfo reference, ReferencePatterns patterns) {
        get().setReferencePatterns(configurationName, gbean, reference, patterns);
    }

    public void setShouldLoad(Artifact configurationName, AbstractName gbean, boolean load) {
        get().setShouldLoad(configurationName, gbean, load);
    }

    public void setValue(Artifact configurationName, AbstractName gbeanName, GAttributeInfo attribute, Object value, Bundle bundle) {
        get().setValue(configurationName, gbeanName, attribute, value, bundle);
    }

    public void addConfiguration(Artifact configName) {
        getPersistentConfigurationList().addConfiguration(configName);
    }

    public Artifact[] getListedConfigurations(Artifact query) {
        return getPersistentConfigurationList().getListedConfigurations(query);
    }

    public boolean hasGBeanAttributes(Artifact configName) {
        return getPersistentConfigurationList().hasGBeanAttributes(configName);
    }

    public boolean isKernelFullyStarted() {
        return getPersistentConfigurationList().isKernelFullyStarted();
    }

    public void migrateConfiguration(Artifact oldName, Artifact newName, Configuration configuration) {
        getPersistentConfigurationList().migrateConfiguration(oldName, newName, configuration);
    }

    public void removeConfiguration(Artifact configName) {
        getPersistentConfigurationList().removeConfiguration(configName);
    }

    public List<Artifact> restore() throws IOException {
        return getPersistentConfigurationList().restore();
    }
}
