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
package org.apache.geronimo.j2ee.deployment;

import java.util.Map;
import java.util.jar.JarFile;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class WebModule extends Module {
    private final String contextRoot;

    public WebModule(boolean standAlone, AbstractName moduleName, Environment environment, JarFile moduleFile, String targetPath, XmlObject specDD, XmlObject vendorDD, String originalSpecDD, String contextRoot, Map sharedContext, String namespace) {
        super(standAlone, moduleName, environment, moduleFile, targetPath, specDD, vendorDD, originalSpecDD, namespace, sharedContext);
        this.contextRoot = contextRoot;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public ConfigurationModuleType getType() {
        return ConfigurationModuleType.WAR;
    }

}

