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

package org.apache.geronimo.security.realm.providers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.util.SimpleEncryption;
import org.apache.geronimo.util.EncryptionManager;
import org.apache.geronimo.util.encoders.Base64;
import org.apache.geronimo.util.encoders.HexTranslator;


/**
 * A LoginModule that reads a list of credentials and group from files on disk.  The
 * files should be formatted using standard Java properties syntax.  Expects
 * to be run by a GenericSecurityRealm (doesn't work on its own).
 * <p/>
 * This login module checks security credentials so the lifecycle methods must return true to indicate success
 * or throw LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
 */
public class PropertiesFileLoginModule implements LoginModule {
    public final static String USERS_URI = "usersURI";
    public final static String GROUPS_URI = "groupsURI";
    public final static String DIGEST = "digest";
    public final static String ENCODING = "encoding";

    private static Log log = LogFactory.getLog(PropertiesFileLoginModule.class);
    final Properties users = new Properties();
    final Map<String, Set<String>> groups = new HashMap<String, Set<String>>();
    private String digest;
    private String encoding;

    private Subject subject;
    private CallbackHandler handler;
    private String username;
    private String password;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.handler = callbackHandler;
        try {
            ServerInfo serverInfo = (ServerInfo) options.get(JaasLoginModuleUse.SERVERINFO_LM_OPTION);
            final String users = (String) options.get(USERS_URI);
            final String groups = (String) options.get(GROUPS_URI);
            digest = (String) options.get(DIGEST);
            encoding = (String) options.get(ENCODING);

            if (digest != null && !digest.equals("")) {
                // Check if the digest algorithm is available
                try {
                    MessageDigest.getInstance(digest);
                } catch (NoSuchAlgorithmException e) {
                    log.error("Initialization failed. Digest algorithm " + digest + " is not available.", e);
                    throw new IllegalArgumentException(
                            "Unable to configure properties file login module: " + e.getMessage(), e);
                }
                if (encoding != null && !"hex".equalsIgnoreCase(encoding) && !"base64".equalsIgnoreCase(encoding)) {
                    log.error("Initialization failed. Digest Encoding " + encoding + " is not supported.");
                    throw new IllegalArgumentException(
                            "Unable to configure properties file login module. Digest Encoding " + encoding + " not supported.");
                }
            }
            if (users == null || groups == null) {
                throw new IllegalArgumentException("Both " + USERS_URI + " and " + GROUPS_URI + " must be provided!");
            }
            URI usersURI = new URI(users);
            URI groupsURI = new URI(groups);
            loadProperties(serverInfo, usersURI, groupsURI);
        } catch (Exception e) {
            log.error("Initialization failed", e);
            throw new IllegalArgumentException("Unable to configure properties file login module: " + e.getMessage(),
                    e);
        }
    }

    public void loadProperties(ServerInfo serverInfo, URI userURI, URI groupURI) throws GeronimoSecurityException {
        try {
            URI userFile = serverInfo.resolveServer(userURI);
            URI groupFile = serverInfo.resolveServer(groupURI);
            InputStream stream = userFile.toURL().openStream();
            users.clear();
            users.load(stream);
            stream.close();

            Properties temp = new Properties();
            stream = groupFile.toURL().openStream();
            temp.load(stream);
            stream.close();

            Enumeration e = temp.keys();
            while (e.hasMoreElements()) {
                String groupName = (String) e.nextElement();
                String[] userList = ((String) temp.get(groupName)).split(",");

                Set<String> userset = groups.get(groupName);
                if (userset == null) {
                    userset = new HashSet<String>();
                    groups.put(groupName, userset);
                }
                for (String user : userList) {
                    userset.add(user);
                }
            }

        } catch (Exception e) {
            log.error("Properties File Login Module - data load failed", e);
            throw new GeronimoSecurityException(e);
        }
    }


    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("User name");
        callbacks[1] = new PasswordCallback("Password", false);
        try {
            handler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        assert callbacks.length == 2;
        username = ((NameCallback) callbacks[0]).getName();
        if (username == null || username.equals("")) {
            throw new FailedLoginException();
        }
        String realPassword = users.getProperty(username);
        // Decrypt the password if needed, so we can compare it with the supplied one
        if (realPassword != null) {
            realPassword = (String) EncryptionManager.decrypt(realPassword);
        }
        char[] entered = ((PasswordCallback) callbacks[1]).getPassword();
        password = entered == null ? null : new String(entered);
        if (!checkPassword(realPassword, password)) {
            throw new FailedLoginException();
        }
        return true;
    }

    public boolean commit() throws LoginException {
        Set<Principal> principals = subject.getPrincipals();

        principals.add(new GeronimoUserPrincipal(username));

        for (Map.Entry<String, Set<String>> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            Set<String> users = entry.getValue();
            for (String user : users) {
                if (username.equals(user)) {
                    principals.add(new GeronimoGroupPrincipal(groupName));
                    break;
                }
            }
        }

        return true;
    }

    public boolean abort() throws LoginException {
        username = null;
        password = null;

        return true;
    }

    public boolean logout() throws LoginException {
        username = null;
        password = null;
        //todo: should remove principals added by commit
        return true;
    }

    /**
     * This method checks if the provided password is correct.  The original password may have been digested.
     *
     * @param real     Original password in digested form if applicable
     * @param provided User provided password in clear text
     * @return true     If the password is correct
     */
    private boolean checkPassword(String real, String provided) {
        if (real == null && provided == null) {
            return true;
        }
        if (real == null || provided == null) {
            return false;
        }

        //both non-null
        if (digest == null || digest.equals("")) {
            // No digest algorithm is used
            return real.equals(provided);
        }
        try {
            // Digest the user provided password
            MessageDigest md = MessageDigest.getInstance(digest);
            byte[] data = md.digest(provided.getBytes());
            if (encoding == null || "hex".equalsIgnoreCase(encoding)) {
                // Convert bytes to hex digits
                byte[] hexData = new byte[data.length * 2];
                HexTranslator ht = new HexTranslator();
                ht.encode(data, 0, data.length, hexData, 0);
                // Compare the digested provided password with the actual one
                return real.equalsIgnoreCase(new String(hexData));
            } else if ("base64".equalsIgnoreCase(encoding)) {
                return real.equals(new String(Base64.encode(data)));
            }
        } catch (NoSuchAlgorithmException e) {
            // Should not occur.  Availability of algorithm has been checked at initialization
            log.error("Should not occur.  Availability of algorithm has been checked at initialization.", e);
        }
        return false;
    }
}