/**
 *
 * Copyright 2005 The Apache Software Foundation
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

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;

import javax.naming.Reference;
import java.net.URI;

/**
 * @version $Rev$ $Date$
 */
public class UnavailableEJBReferenceBuilder implements EJBReferenceBuilder {

    public Reference createCORBAReference(Configuration configuration, AbstractNameQuery containerNameQuery, URI nsCorbaloc, String objectName, String home) throws DeploymentException {
        throw new DeploymentException("EJB references are unavailable in this configuration");
    }

    public Object createHandleDelegateReference() throws DeploymentException {
        throw new DeploymentException("EJB references are unavailable in this configuration");
    }

    public Reference createEJBRemoteRef(String requiredModule, String optionalModule, String name, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String home, String remote, Configuration configuration) throws DeploymentException {
        throw new DeploymentException("EJB references are unavailable in this configuration");
    }

    public Reference createEJBLocalRef(String requiredModule, String optionalModule, String name, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String localHome, String local, Configuration configuration) throws DeploymentException {
        throw new DeploymentException("EJB references are unavailable in this configuration");
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(UnavailableEJBReferenceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(EJBReferenceBuilder.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
