/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.jmsmanager.renderers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.jmsmanager.AbstractJMSManager;
import org.apache.geronimo.console.jmsmanager.DestinationInfo;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

public class ViewDestinationsRenderer extends AbstractJMSManager implements
        PortletRenderer {

    protected static Log log = LogFactory
            .getLog(ViewDestinationsRenderer.class);

    public String render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        List destinationList = getDestinationList(request, response);

        request.setAttribute(DESTINATION_LIST, destinationList);

        return "/WEB-INF/view/jmsmanager/view.jsp";
    }

    public List getDestinationList(RenderRequest request,
            RenderResponse response) {
        Kernel kernel = KernelRegistry.getSingleKernel();

        Set destinations = kernel.listGBeans(DESTINATION_QUERY);
        List destinationInfos = new ArrayList(destinations.size());
        DependencyManager dm = kernel.getDependencyManager();
        for (Iterator iterator = destinations.iterator(); iterator.hasNext();) {
            ObjectName destinationName = (ObjectName) iterator.next();

            try {
                Class type;
                try {
                    type = Class.forName((String) kernel.getAttribute(
                            destinationName, "adminObjectInterface"));
                } catch (ClassCastException cce) {
                    type = (Class) kernel.getAttribute(destinationName,
                            "adminObjectInterface");
                }
                Set parents = dm.getParents(destinationName);
                Iterator i = parents.iterator();
                // If no parents this is a configuration we don't need those
                // here.
                if (!i.hasNext()) {
                    continue;
                }
                ObjectName parent = (ObjectName) i.next();
                String adminObjectName = destinationName
                        .getKeyProperty(NameFactory.J2EE_NAME);
                if (adminObjectName.equals("MDBTransferBeanOutQueue")
                        || adminObjectName.equals("SendReceiveQueue")) {
                    continue;
                }
                String configURI = parent.getKeyProperty("name");
                if (configURI.startsWith("\"")) {
                    configURI = configURI.substring(1);
                }
                if (configURI.endsWith("\"")) {
                    configURI = configURI.substring(0, configURI.length() - 1);
                }

                DestinationInfo info = new DestinationInfo(adminObjectName,
                        (String) kernel.getAttribute(destinationName,
                                "PhysicalName"), type, destinationName
                                .getKeyProperty(NameFactory.J2EE_APPLICATION),
                        destinationName
                                .getKeyProperty(NameFactory.JCA_RESOURCE),
                        configURI);
                destinationInfos.add(info);
            } catch (Exception e) {
                log.error(e);
            }
        }
        Collections.sort(destinationInfos);
        return destinationInfos;
    }

}
