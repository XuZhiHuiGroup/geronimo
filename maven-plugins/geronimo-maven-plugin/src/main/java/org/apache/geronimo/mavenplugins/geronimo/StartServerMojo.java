/*
 *  Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.mavenplugins.geronimo;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Java;

import org.codehaus.plexus.util.FileUtils;

/**
 * Start the Geronimo server.
 *
 * @goal start
 *
 * @version $Rev$ $Date$
 */
public class StartServerMojo
    extends ServerMojoSupport
{
    /**
     * Flag to control if we background the server or block Maven execution.
     *
     * @parameter expression="${background}" default-value="false"
     */
    private boolean background = false;

    /**
     * Set the maximum memory for the forked JVM.
     *
     * @parameter expression="${maximumMemory}"
     */
    private String maximumMemory = null;

    /**
     * Enable quiet mode.
     *
     * @parameter expression="${quiet}" default-value="false"
     */
    private boolean quiet = false;

    /**
     * Enable verbose mode.
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose = false;

    /**
     * Enable veryverbose mode.
     *
     * @parameter expression="${veryverbose}" default-value="false"
     */
    private boolean veryverbose = false;

    /**
     * Enable forced install refresh.
     *
     * @parameter expression="${refresh}" default-value="false"
     */
    private boolean refresh = false;

    /**
     * Time in seconds to wait before terminating the forked JVM.
     *
     * @parameter expression="${timeout}" default-value="-1"
     */
    private int timeout = -1;

    /**
     * Time in seconds to wait while verifing that the server has started.
     *
     * @parameter expression="${verifyTimeout}" default-value="-1"
     */
    private int verifyTimeout = -1;

    private Timer timer = new Timer(true);

    protected void doExecute() throws Exception {
        log.info("Starting Geronimo server...");

        // Check if there is a newer archive or missing marker to trigger assembly install
        File installMarker = new File(installDir, ".installed");
        boolean refresh = this.refresh; // don't override config state with local state

        if (!refresh) {
            if (!installMarker.exists()) {
                refresh = true;
            }
            else if (installArchive.lastModified() > installMarker.lastModified()) {
                log.debug("Detected new assembly archive");
                refresh = true;
            }
        }
        else {
            log.debug("User requested installation refresh");
        }

        if (refresh) {
            if (installDir.exists()) {
                log.debug("Removing: " + installDir);
                FileUtils.forceDelete(installDir);
            }
        }

        // Install the assembly
        if (!installMarker.exists()) {
            log.info("Installing assembly...");

            Expand unzip = (Expand)createTask("unzip");
            unzip.setSrc(installArchive);
            unzip.setDest(outputDirectory);
            unzip.execute();

            installMarker.createNewFile();
        }
        else {
            log.debug("Assembly already installed... reusing");
        }

        // Setup the JVM to start the server with
        final Java java = (Java)createTask("java");
        java.setJar(new File(installDir, "bin/server.jar"));
        java.setDir(installDir);
        java.setFailonerror(true);
        java.setFork(true);
        java.setLogError(true);

        if (timeout > 0) {
            java.setTimeout(new Long(timeout * 1000));
        }

        //
        // TODO: Capture output/error to files
        //

        if (maximumMemory != null) {
            java.setMaxmemory(maximumMemory);
        }

        if (quiet) {
            java.createArg().setValue("--quiet");
        }
        else {
            java.createArg().setValue("--long");
        }

        if (verbose) {
            java.createArg().setValue("--verbose");
        }

        if (veryverbose) {
            java.createArg().setValue("--veryverbose");
        }

        //
        // TODO: Support --override
        //

        //
        // TODO: Support JVM args for debug mode, add debug flag to enable or disable
        //

        // Holds any exception that was thrown during startup (as the cause)
        final Throwable errorHolder = new Throwable();

        // Start the server int a seperate thread
        Thread t = new Thread("Geronimo Server Runner") {
            public void run() {
                try {
                    java.execute();
                }
                catch (Exception e) {
                    errorHolder.initCause(e);

                    //
                    // NOTE: Don't log here, as when the JVM exists an exception will get thrown by Ant
                    //       but that should be fine.
                    //
                }
            }
        };
        t.start();

        log.info("Waiting for Geronimo server...");

        // Setup a callback to time out verification
        final ObjectHolder verifyTimedOut = new ObjectHolder();

        log.debug("Starting verify timeout task; triggers in: " + verifyTimeout + "s");

        TimerTask timeoutTask = new TimerTask() {
            public void run() {
                verifyTimedOut.set(Boolean.TRUE);
            }
        };

        if (verifyTimeout > 0) {
            timer.schedule(timeoutTask, verifyTimeout * 1000);
        }

        //
        // TODO: Check the status via JMX:
        //
        //       "service:jmx:rmi://localhost/jndi/rmi://localhost:" + port + "/JMXConnector"
        //

        // Verify server started
        URL url = new URL("http://localhost:8080");
        boolean started = false;
        while (!started) {
            if (verifyTimedOut.isSet()) {
                throw new MojoExecutionException("Unable to verify if the server was started in the given time");
            }

            if (errorHolder.getCause() != null) {
                throw new MojoExecutionException("Failed to start Geronimo server", errorHolder.getCause());
            }

            log.debug("Trying connection to: " + url);

            try {
                url.openConnection().getContent();
                started = true;
            }
            catch (Exception e) {
                // ignore
            }

            Thread.sleep(1000);
        }

        // Stop the timer, server should be up now
        timeoutTask.cancel();

        //
        // HACK: Give it a few seconds... our detection method here is lossy
        //

        Thread.sleep(10000);

        log.info("Geronimo server started");

        if (!background) {
            log.info("Waiting for Geronimo server to shutdown...");

            t.join();
        }
    }
}
