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


package org.apache.geronimo.jetty7.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.Context;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.resource.ResourceException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.util.component.LifeCycle;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.SharedConnectorInstanceContext;

/**
 * @version $Rev$ $Date$
 */
public class TwistyWebAppContext extends WebAppContext {

    private Handler handler;
    protected final IntegrationContext integrationContext;


    public TwistyWebAppContext(SecurityHandler securityHandler, SessionHandler sessionHandler, ServletHandler servletHandler, ErrorHandler errorHandler, IntegrationContext integrationContext, ClassLoader classLoader) {
        super(sessionHandler, securityHandler, servletHandler, errorHandler);
        this.integrationContext = integrationContext;
        setClassLoader(classLoader);
    }

    public void setTwistyHandler(Handler handler) {
        this.handler = handler;
    }

    public Handler newTwistyHandler() {
        return new TwistyHandler();
    }

    @Override
    protected void doStart() throws Exception {
        javax.naming.Context context = integrationContext.setContext();
        boolean txActive = integrationContext.isTxActive();
        SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(null);
        ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(null, newContext);
        try {
            try {
                super.doStart();
            } finally {
                integrationContext.restoreConnectorContext(connectorContext, null, newContext);
            }
        } finally {
            integrationContext.restoreContext(context);
            integrationContext.completeTx(txActive, null);
        }
    }

    @Override
    protected void doStop() throws Exception {
        javax.naming.Context context = integrationContext.setContext();
        boolean txActive = integrationContext.isTxActive();
        SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(null);
        ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(null, newContext);
        try {
            try {
                super.doStop();
            } finally {
                integrationContext.restoreConnectorContext(connectorContext, null, newContext);
            }
        } finally {
            integrationContext.restoreContext(context);
            integrationContext.completeTx(txActive, null);
        }
    }

    @Override
    public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        javax.naming.Context context = integrationContext.setContext();
        boolean txActive = integrationContext.isTxActive();
        SharedConnectorInstanceContext newContext = integrationContext.newConnectorInstanceContext(baseRequest);
        ConnectorInstanceContext connectorContext = integrationContext.setConnectorInstance(baseRequest, newContext);
        try {
            try {
                super.doScope(target, baseRequest, request, response);
            } finally {
                integrationContext.restoreConnectorContext(connectorContext, baseRequest, newContext);
            }
        } finally {
            integrationContext.restoreContext(context);
            integrationContext.completeTx(txActive, baseRequest);
        }
    }


//    @Override
//    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//        handler.handle(target, baseRequest, request, response);
//    }

    private class TwistyHandler implements Handler {

        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            TwistyWebAppContext.super.doHandle(target, baseRequest, request, response);
        }

        public void setServer(Server server) {
             TwistyWebAppContext.super.setServer(server);
        }

        public Server getServer() {
            return TwistyWebAppContext.super.getServer();
        }

        public void destroy() {
            TwistyWebAppContext.super.destroy();
        }

        public void start() throws Exception {
            TwistyWebAppContext.super.start();
        }

        public void stop() throws Exception {
            TwistyWebAppContext.super.stop();
        }

        public boolean isRunning() {
            return TwistyWebAppContext.super.isRunning();
        }

        public boolean isStarted() {
            return TwistyWebAppContext.super.isStarted();
        }

        public boolean isStarting() {
            return TwistyWebAppContext.super.isStarting();
        }

        public boolean isStopping() {
            return TwistyWebAppContext.super.isStopping();
        }

        public boolean isStopped() {
            return TwistyWebAppContext.super.isStopped();
        }

        public boolean isFailed() {
            return TwistyWebAppContext.super.isFailed();
        }

        public void addLifeCycleListener(Listener listener) {
        }

        public void removeLifeCycleListener(Listener listener) {
        }
    }

}
