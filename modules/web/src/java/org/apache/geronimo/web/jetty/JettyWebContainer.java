/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.web.jetty;

import java.util.Arrays;
import java.util.Collection;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.management.StateManageable;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.web.AbstractWebContainer;
import org.apache.geronimo.web.WebApplication;
import org.apache.geronimo.web.WebAccessLog;
import org.apache.geronimo.web.WebConnector;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GEndpointInfo;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.w3c.dom.Document;

/**
 * Base class for jetty web containers.
 *
 *
 * @version $Revision: 1.11 $ $Date: 2004/01/16 02:19:23 $
 */
public class JettyWebContainer extends AbstractWebContainer {
    private final static GBeanInfo GBEAN_INFO;

    private final Log log = LogFactory.getLog(JettyWebContainer.class);

    private final Server jettyServer;

    /**
     *  @deprecated, remove when GBean -only
     */
    public JettyWebContainer() throws Exception {
        jettyServer = new Server();
        jettyServer.start();
    }

    public JettyWebContainer(URI defaultWebXmlURI, Document defaultWebXmlDoc, Collection webApplications, Collection webConnectors, Collection webAccessLogs) throws Exception {
        super(defaultWebXmlURI, defaultWebXmlDoc);
        jettyServer = new Server();
        jettyServer.start();
        //Does order matter here?
        setWebApplications(webApplications);
        setWebAccessLogs(webAccessLogs);
        setWebConnectors(webConnectors);
    }

    /**
     * Get the Jetty server delegate
     *
     * @return the Jetty server
     */
    Server getJettyServer ()
    {
        return jettyServer;
    }



    /**
     * Handle addition of a web connector.
     *
     * @param connector a <code>WebConnector</code> value
     */
    protected void webConnectorAdded(WebConnector connector)
    {
        JettyWebConnector jettyWebConnector = (JettyWebConnector)connector;
        jettyServer.addListener(jettyWebConnector.getListener());
        log.debug ("Web connector="+connector+" added");
    }




    /**
     * Handle removal of a web connector
     *
     * @param connector a <code>WebConnector</code> value
     */
    protected void webConnectorRemoved (WebConnector connector)
    {
        try
        {
            //stop the connector
            if (connector instanceof StateManageable)
                ((StateManageable)connector).stop();
        }
        catch (Exception e)
        {
            log.warn("Ignoring exception on stopping connector", e);
        }

        //remove the listener
        jettyServer.removeListener (((JettyWebConnector)connector).getListener());
    }


    /* -------------------------------------------------------------------------------------- */
    /* Add a webapp to the underlying Jetty delegate.
     * Called when a webapp's war or dir is deployed.
     * @param webapp
     * @see org.apache.geronimo.web.AbstractWebContainer#webApplicationAdded(org.apache.geronimo.web.WebApplication)
     */
    protected void webApplicationAdded (WebApplication webapp)
    {
        log.debug ("Web application="+webapp+" added to Jetty");
        JettyWebApplication jettyWebApplication = (JettyWebApplication)webapp;
        WebApplicationContext webApplicationContext = jettyWebApplication.getJettyContext();
        jettyServer.addContext(webApplicationContext);
        //TODO Why isn't this done in JettyWebApplication or JettyWebApplicationContext?
        webApplicationContext.setExtractWAR(true);

        webApplicationContext.setDefaultsDescriptor(getDefaultWebXmlURI() == null? null: getDefaultWebXmlURI().toString());
        try {
            webApplicationContext.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not start jetty context", e);
        }
        log.debug("web application " + webapp + " file classpath:" + webApplicationContext.getFileClassPath());
    }


    /* -------------------------------------------------------------------------------------- */
    /**Remove a web app from the underlying Jetty delegate.
     * Called when the web app's war or dir is undeployed.
     * @param webapp
     */
    protected void webApplicationRemoval (WebApplication webapp)
    {
        log.debug ("Web application="+webapp+" removed from Jetty");
        JettyWebApplication jettyWebApplication = (JettyWebApplication)webapp;
        WebApplicationContext webApplicationContext = jettyWebApplication.getJettyContext();
        try {
            webApplicationContext.stop();
        } catch (InterruptedException e) {
            throw new RuntimeException("could not stop jetty context", e);
        }
        jettyServer.removeContext(webApplicationContext);
    }

    protected void webAccessLogAdded(WebAccessLog webAccessLog) {
        try {
            ((JettyWebAccessLog)webAccessLog).registerLog(jettyServer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void WebAccessLogRemoval(WebAccessLog webAccessLog) {
        try {
            ((JettyWebAccessLog)webAccessLog).unregisterLog(jettyServer);
        } catch (InterruptedException e) {
            log.info(e);
            //???ignore???
        }
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Jetty Web Container", "Geronimo integrated Jetty Server", JettyWebContainer.class.getName(), AbstractWebContainer.getGBeanInfo());
        infoFactory.addEndpoint(new GEndpointInfo("WebApplications", JettyWebApplication.class.getName()));
        infoFactory.addEndpoint(new GEndpointInfo("WebConnectors", JettyWebConnector.class.getName()));
        infoFactory.addEndpoint(new GEndpointInfo("WebAccessLogs", JettyWebAccessLog.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(
                Arrays.asList(new Object[] {"DefaultWebXmlURI", "DefaultWebXmlDoc", "WebApplications", "WebConnectors", "WebAccessLogs", }),
                Arrays.asList(new Object[] {URI.class, Document.class, Collection.class, Collection.class, Collection.class })));
         GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGbeanInfo() {
        return GBEAN_INFO;
    }

    /**
     *  @deprecated, remove when GBean -only
     */
    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        return AbstractWebContainer.getGeronimoMBeanInfo(JettyWebContainer.class, "Jetty", JettyWebApplication.class, JettyWebConnector.class, JettyWebAccessLog.class);
    }
}
