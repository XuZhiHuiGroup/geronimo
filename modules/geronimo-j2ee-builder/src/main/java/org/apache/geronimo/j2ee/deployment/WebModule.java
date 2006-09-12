/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.j2ee.deployment;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeployableModule;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class WebModule extends Module {
    private final String contextRoot;
    private final Map portMap;

    public WebModule(boolean standAlone, AbstractName moduleName, Environment environment, DeployableModule deployableModule, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD, String contextRoot, Map portMap, String namespace) {
        super(standAlone, moduleName, environment, deployableModule, targetPath, specDD, vendorDD, originalSpecDD, namespace);
        this.contextRoot = contextRoot;
        this.portMap = portMap;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public ConfigurationModuleType getType() {
        return ConfigurationModuleType.WAR;
    }

    public Map getPortMap() {
        return portMap;
    }

}

