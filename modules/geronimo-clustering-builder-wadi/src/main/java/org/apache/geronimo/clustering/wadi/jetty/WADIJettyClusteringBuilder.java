/**
 *
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
package org.apache.geronimo.clustering.wadi.jetty;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.geronimo.clustering.wadi.BasicWADISessionManager;
import org.apache.geronimo.clustering.wadi.WADISessionManagerConfigInfo;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.jetty.JettyWebAppContext;
import org.apache.geronimo.jetty.cluster.ClusteredWebApplicationHandlerFactory;
import org.apache.geronimo.jetty.cluster.wadi.WADIClusteredHandleInterceptor;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.GerClusteringWadiDocument;
import org.apache.geronimo.xbeans.geronimo.GerClusteringWadiType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;

/**
 *
 * @version $Rev$ $Date$
 */
public class WADIJettyClusteringBuilder implements NamespaceDrivenBuilder {
    private static final QName CLUSTERING_WADI_QNAME = GerClusteringWadiDocument.type.getDocumentElementName();
    private static final QNameSet CLUSTERING_WADI_QNAME_SET = QNameSet.singleton(CLUSTERING_WADI_QNAME);

    private final int defaultSweepInterval;
    private final int defaultNumPartitions;
    private final AbstractNameQuery defaultRepManagerFactoryName;
    private final AbstractNameQuery defaultRepStorageFactoryName;
    private final AbstractNameQuery defaultBackingStrategyFactoryName;
    private final AbstractNameQuery defaultDispatcherHolderName;
    private final Environment defaultEnvironment;

    public WADIJettyClusteringBuilder(int defaultSweepInterval,
            int defaultNumPartitions,
            AbstractNameQuery defaultRepManagerFactoryName,
            AbstractNameQuery defaultRepStorageFactoryName,
            AbstractNameQuery defaultBackingStrategyFactoryName,
            AbstractNameQuery defaultDispatcherHolderName,
            Environment defaultEnvironment) {
        this.defaultSweepInterval = defaultSweepInterval;
        this.defaultNumPartitions = defaultNumPartitions;
        this.defaultRepManagerFactoryName = defaultRepManagerFactoryName;
        this.defaultRepStorageFactoryName = defaultRepStorageFactoryName;
        this.defaultBackingStrategyFactoryName = defaultBackingStrategyFactoryName;
        this.defaultDispatcherHolderName = defaultDispatcherHolderName;
        this.defaultEnvironment = defaultEnvironment;
        SchemaConversionUtils.registerNamespaceConversions(Collections.singletonMap(CLUSTERING_WADI_QNAME.getLocalPart(), new NamespaceElementConverter(CLUSTERING_WADI_QNAME.getNamespaceURI())));
    }

    public void buildEnvironment(XmlObject container, Environment environment) throws DeploymentException {
        if (getWadiClusterConfig(container) != null) {
            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
        }
    }

    public void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext) throws DeploymentException {
        GerClusteringWadiType clusteringWadiType = getWadiClusterConfig(container);
        if (clusteringWadiType != null) {
            GBeanData webModuleData = extractWebModule(moduleContext);
            try {
                AbstractName sessionManagerName = addSessionManager(clusteringWadiType, webModuleData, moduleContext);
                addHandlerFactory(moduleContext, webModuleData, sessionManagerName);
                addInterceptor(moduleContext, webModuleData, sessionManagerName);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Duplicate GBean", e);
            }
        }
    }

    private GBeanData extractWebModule(DeploymentContext moduleContext) throws DeploymentException {
        Configuration configuration = moduleContext.getConfiguration();
        AbstractNameQuery webModuleQuery = new AbstractNameQuery(configuration.getId(), Collections.EMPTY_MAP, Collections.singleton(JettyWebAppContext.class.getName()));
        try {
            return configuration.findGBeanData(webModuleQuery);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not locate web module gbean in web app configuration", e);
        }
    }

    private GerClusteringWadiType getWadiClusterConfig(XmlObject container) throws DeploymentException {
        XmlObject[] items = container.selectChildren(CLUSTERING_WADI_QNAME_SET);
        if (items.length > 1) {
            throw new DeploymentException("Unexpected count of clustering elements in geronimo plan " + items.length + " qnameset: " + CLUSTERING_WADI_QNAME_SET);
        }
        if (items.length == 1) {
            try {
                return (GerClusteringWadiType) XmlBeansUtil.typedCopy(items[0], GerClusteringWadiType.type);
            } catch (XmlException e) {
                throw new DeploymentException("Could not validate jetty wadi clustering config", e);
            }
        }
        return null;
    }

    private AbstractName addSessionManager(GerClusteringWadiType clustering, GBeanData webModuleData,
            DeploymentContext moduleContext) throws GBeanAlreadyExistsException {
        AbstractName name = moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
                "WADISessionManager", NameFactory.GERONIMO_SERVICE);

        GBeanData beanData = new GBeanData(name, BasicWADISessionManager.GBEAN_INFO);

        setConfigInfo(clustering, webModuleData, beanData);
        setReplicationManagerFactory(clustering, beanData);
        setReplicaStorageFactory(clustering, beanData);
        setBackingStrategyFactory(clustering, beanData);
        setDispatcher(clustering, beanData);

        moduleContext.addGBean(beanData);

        return name;
    }

    private void setConfigInfo(GerClusteringWadiType clustering, GBeanData webModuleData, GBeanData beanData) {
        int sweepInterval = defaultSweepInterval;
        if (clustering.isSetSweepInterval()) {
            sweepInterval = clustering.getSweepInterval().intValue();
        }
        int numPartitions = defaultNumPartitions;
        if (clustering.isSetNumPartitions()) {
            numPartitions = clustering.getNumPartitions().intValue();
        }
        Integer sessionTimeout = (Integer) webModuleData.getAttribute(JettyWebAppContext.GBEAN_ATTR_SESSION_TIMEOUT);
        if (null == sessionTimeout) {
            throw new AssertionError();
        }

        WADISessionManagerConfigInfo configInfo = new WADISessionManagerConfigInfo(
                beanData.getAbstractName().toURI(),
                sweepInterval,
                numPartitions,
                sessionTimeout.intValue());
        beanData.setAttribute(BasicWADISessionManager.GBEAN_ATTR_WADI_CONFIG_INFO, configInfo);
    }

    private void setDispatcher(GerClusteringWadiType clustering, GBeanData beanData) {
        Set patterns = new HashSet();
        if (clustering.isSetDispatcher()) {
            addAbstractNameQueries(patterns, clustering.getDispatcher().getPatternArray());
        } else {
            patterns.add(defaultDispatcherHolderName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_DISPATCHER_HOLDER, patterns);
    }

    private void setBackingStrategyFactory(GerClusteringWadiType clustering, GBeanData beanData) {
        Set patterns = new HashSet();
        if (clustering.isSetBackingStrategyFactory()) {
            addAbstractNameQueries(patterns, clustering.getBackingStrategyFactory().getPatternArray());
        } else {
            patterns.add(defaultBackingStrategyFactoryName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_BACKING_STRATEGY_FACTORY, patterns);
    }

    private void setReplicaStorageFactory(GerClusteringWadiType clustering, GBeanData beanData) {
        Set patterns = new HashSet();
        if (clustering.isSetReplicaStorageFactory()) {
            addAbstractNameQueries(patterns, clustering.getReplicaStorageFactory().getPatternArray());
        } else {
            patterns.add(defaultRepStorageFactoryName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_REPLICA_STORAGE_FACTORY, patterns);
    }

    private void setReplicationManagerFactory(GerClusteringWadiType clustering, GBeanData beanData) {
        Set patterns = new HashSet();
        if (clustering.isSetReplicationManagerFactory()) {
            addAbstractNameQueries(patterns, clustering.getReplicationManagerFactory().getPatternArray());
        } else {
            patterns.add(defaultRepManagerFactoryName);
        }
        beanData.setReferencePatterns(BasicWADISessionManager.GBEAN_REF_REPLICATION_MANAGER_FACTORY, patterns);
    }

    private AbstractName addHandlerFactory(DeploymentContext moduleContext,
            GBeanData webModuleData, AbstractName sessionManagerName) throws GBeanAlreadyExistsException {
        AbstractName name = moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
                "ClusteredWebApplicationHandlerFactory", NameFactory.GERONIMO_SERVICE);

        GBeanData beanData = new GBeanData(name, ClusteredWebApplicationHandlerFactory.GBEAN_INFO);
        beanData.setReferencePattern(ClusteredWebApplicationHandlerFactory.GBEAN_REF_SESSION_MANAGER, sessionManagerName);

        webModuleData.setReferencePattern(JettyWebAppContext.GBEAN_REF_WEB_APPLICATION_HANDLER_FACTORY, name);

        moduleContext.addGBean(beanData);

        return name;
    }

    private AbstractName addInterceptor(DeploymentContext moduleContext,
            GBeanData webModuleData, AbstractName sessionManagerName) throws GBeanAlreadyExistsException {
        AbstractName name = moduleContext.getNaming().createChildName(moduleContext.getModuleName(),
                "WADIClusteredHandleInterceptor", NameFactory.GERONIMO_SERVICE);

        GBeanData beanData = new GBeanData(name, WADIClusteredHandleInterceptor.GBEAN_INFO);
        beanData.setReferencePattern(WADIClusteredHandleInterceptor.GBEAN_REF_WADI_SESSION_MANAGER, sessionManagerName);

        webModuleData.setReferencePattern(JettyWebAppContext.GBEAN_REF_HANDLE_INTERCEPTOR, name);

        moduleContext.addGBean(beanData);

        return name;
    }

    private void addAbstractNameQueries(Set patterns, GerPatternType[] patternTypes) {
        for (int i = 0; i < patternTypes.length; i++) {
            AbstractNameQuery query = ENCConfigBuilder.buildAbstractNameQuery(patternTypes[i], null, null, null);
            patterns.add(query);
        }
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return CLUSTERING_WADI_QNAME_SET;
    }

    public static final GBeanInfo GBEAN_INFO;

    public static final String GBEAN_ATTR_DFT_SWEEP_INTERVAL = "defaultSweepInterval";
    public static final String GBEAN_ATTR_DFT_NUM_PARTITIONS = "defaultNumPartitions";
    public static final String GBEAN_ATTR_DFT_REP_MANAGER_FACTORY_NAME = "defaultReplicationManagerFactoryName";
    public static final String GBEAN_ATTR_DFT_REP_STORAGE_FACTORY_NAME = "defaultReplicaStorageFactoryName";
    public static final String GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME = "defaultBackingStrategyFactoryName";
    public static final String GBEAN_ATTR_DFT_DISPATCHER_HOLDER_NAME = "defaultDispatcherHolderName";
    public static final String GBEAN_ATTR_DFT_ENVIRONMENT = "defaultEnvironment";

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic("WADI Session Manager",
                WADIJettyClusteringBuilder.class,
                NameFactory.MODULE_BUILDER);

        infoBuilder.addAttribute(GBEAN_ATTR_DFT_SWEEP_INTERVAL, int.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_NUM_PARTITIONS, int.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_REP_MANAGER_FACTORY_NAME, AbstractNameQuery.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_REP_STORAGE_FACTORY_NAME, AbstractNameQuery.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME, AbstractNameQuery.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_DISPATCHER_HOLDER_NAME, AbstractNameQuery.class, true);
        infoBuilder.addAttribute(GBEAN_ATTR_DFT_ENVIRONMENT, Environment.class, true);

        infoBuilder.setConstructor(new String[]{GBEAN_ATTR_DFT_SWEEP_INTERVAL,
                GBEAN_ATTR_DFT_NUM_PARTITIONS,
                GBEAN_ATTR_DFT_REP_MANAGER_FACTORY_NAME,
                GBEAN_ATTR_DFT_REP_STORAGE_FACTORY_NAME,
                GBEAN_ATTR_DFT_BACKING_STRATEGY_FACTORY_NAME,
                GBEAN_ATTR_DFT_DISPATCHER_HOLDER_NAME,
                GBEAN_ATTR_DFT_ENVIRONMENT});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
