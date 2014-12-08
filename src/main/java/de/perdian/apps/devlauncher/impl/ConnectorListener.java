/*
 * DevLauncher
 * Copyright 2013 Christian Robert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.perdian.apps.devlauncher.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.perdian.apps.devlauncher.DevLauncher;
import de.perdian.apps.devlauncher.DevLauncherListener;

/**
 * Builder that collects information about the listener that is to be added to
 * an embedded Tomcat server instance
 *
 * @author Christian Robert
 */

@SuppressWarnings("deprecation")
public class ConnectorListener implements DevLauncherListener {

    private static final Logger log = LoggerFactory.getLogger(ConnectorListener.class);
    private static final String KEYSTORE_PASSWORD = "tlsKeystorePassword";
    private static final String TLS_KEY_NAME = "tlsKeyName";
    private static final String TLS_KEY_PASSWORD = "tlsKeyPassword";

    public static final String PROTOCOL_AJP = "AJP/1.3";

    private int myPort = -1;
    private int myRedirectPort = -1;
    private String myProtocol = null;
    private String myUriEncoding = "UTF-8";
    private boolean stateSecure = false;

    /**
     * Creates a new listener instance
     *
     * @param port
     *     the port on which the listener will listen for incoming requests
     */
    public ConnectorListener(int port) {
        if (port < 0) {
            throw new IllegalArgumentException("Parameter 'port' must not be negative! [Was: " + port + "]");
        } else {
            this.setPort(port);
        }
    }

    @Override
    public void customizeServer(Tomcat tomcat, DevLauncher launcher) {

        Connector connector = this.createConnector(launcher);
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Adding").append(this.isSecure() ? " secure" : "").append(" connector");
        if (this.getProtocol() != null) {
            logMessage.append(" for protocol '").append(this.getProtocol());
        }
        logMessage.append(" listening on port ").append(this.getPort());
        if (this.getRedirectPort() > 0) {
            logMessage.append(" and redirectPort ").append(this.getRedirectPort());
        }
        logMessage.append(" [").append(connector).append("]");
        log.debug(logMessage.toString());

        // Special handling for TLS connectors
        if (this.isSecure()) {
            try {

                File keystoreFile = new File(launcher.getWorkingDirectory(), "config/keystore");
                KeyStore keyStore = this.ensureKeyStore(keystoreFile);
                this.ensureKeyInStore(keystoreFile, keyStore);

                connector.setSecure(true);
                connector.setScheme("https");
                connector.setAttribute("keyAlias", TLS_KEY_NAME);
                connector.setAttribute("keyPass", TLS_KEY_PASSWORD);
                connector.setAttribute("keystoreFile", keystoreFile.getCanonicalPath());
                connector.setAttribute("keystorePass", KEYSTORE_PASSWORD);
                connector.setAttribute("clientAuth", "false");
                connector.setAttribute("sslProtocol", "TLS");
                connector.setAttribute("SSLEnabled", true);
                tomcat.getConnector().setRedirectPort(connector.getPort());

            } catch (Exception e) {
                throw new RuntimeException("Cannot prepare SSL keystore configuration", e);
            }
        }
        tomcat.getService().addConnector(connector);

    }

    protected Connector createConnector(DevLauncher launcher) {
        Connector connector = new Connector(this.getProtocol());
        connector.setPort(this.getPort());
        if (this.getRedirectPort() > 0) {
            connector.setRedirectPort(this.getRedirectPort());
        }
        if (this.getUriEncoding() != null) {
            connector.setURIEncoding(this.getUriEncoding());
        }
        connector.setXpoweredBy(false);
        return connector;
    }

    // -------------------------------------------------------------------------
    // --- TLS keystore handling -----------------------------------------------
    // -------------------------------------------------------------------------

    private Key ensureKeyInStore(File keystoreFile, KeyStore keyStore) throws GeneralSecurityException, IOException {
        Key key = this.lookupKeyFromStore(keyStore);
        if (key == null) {

            log.info("Creating new TLS key to enable HTTPS access");

            // No key available, so we have to create the key from scratch and
            // make it available in the store
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
            v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
            v3CertGen.setIssuerDN(new X509Principal("CN=" + "localhost" + ", OU=None, O=None L=None, C=None"));
            v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
            v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * 10)));
            v3CertGen.setSubjectDN(new X509Principal("CN=" + "localhost" + ", OU=None, O=None L=None, C=None"));
            v3CertGen.setPublicKey(keyPair.getPublic());
            v3CertGen.setSignatureAlgorithm("MD5WithRSAEncryption");
            X509Certificate certificate = v3CertGen.generateX509Certificate(keyPair.getPrivate());

            // Store the key (including the certificate) into the keystore
            keyStore.setKeyEntry(TLS_KEY_NAME, keyPair.getPrivate(), TLS_KEY_PASSWORD.toCharArray(), new java.security.cert.Certificate[] { certificate });

            // Write the keystore into the target file
            log.debug("Updating KeyStore at: " + keystoreFile.getAbsolutePath());
            if (!keystoreFile.getParentFile().exists()) {
                keystoreFile.getParentFile().mkdirs();
            }
            try (OutputStream keyStoreStream = new BufferedOutputStream(new FileOutputStream(keystoreFile))) {
                keyStore.store(keyStoreStream, KEYSTORE_PASSWORD.toCharArray());
                keyStoreStream.flush();
            }

        }
        return key;
    }

    private Key lookupKeyFromStore(KeyStore keyStore) {
        try {
            Key key = keyStore.getKey(TLS_KEY_NAME, TLS_KEY_PASSWORD.toCharArray());
            if (key != null) {
                log.trace("Found key '" + TLS_KEY_NAME + "' in KeyStore with format: " + key.getFormat());
            }
            return key;
        } catch (GeneralSecurityException e) {
            log.debug("Cannot retrieve key from KeyStore", e);
            return null;
        }
    }

    private KeyStore ensureKeyStore(File keystoreFile) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        if (keystoreFile.exists()) {
            try {
                try (InputStream keystoreFileStream = new BufferedInputStream(new FileInputStream(keystoreFile))) {
                    keyStore.load(keystoreFileStream, KEYSTORE_PASSWORD.toCharArray());
                }
            } catch (Exception e) {
                log.warn("Cannot load KeyStore from file at: " + keystoreFile.getAbsolutePath());
            }
        }
        return keyStore;
    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public int getPort() {
        return this.myPort;
    }
    private void setPort(int port) {
        this.myPort = port;
    }

    public ConnectorListener redirectPort(int port) {
        this.setRedirectPort(port);
        return this;
    }
    public int getRedirectPort() {
        return this.myRedirectPort;
    }
    private void setRedirectPort(int redirectPort) {
        this.myRedirectPort = redirectPort;
    }

    public ConnectorListener protocol(String protocol) {
        this.setProtocol(protocol);
        return this;
    }
    public String getProtocol() {
        return this.myProtocol;
    }
    private void setProtocol(String protocol) {
        this.myProtocol = protocol;
    }

    public ConnectorListener secure(boolean secure) {
        this.setSecure(secure);
        return this;
    }
    public boolean isSecure() {
        return this.stateSecure;
    }
    private void setSecure(boolean secure) {
        this.stateSecure = secure;
    }

    public ConnectorListener uriEncoding(String encoding) {
        this.setUriEncoding(encoding);
        return this;
    }
    public String getUriEncoding() {
        return this.myUriEncoding;
    }
    private void setUriEncoding(String uRIEncoding) {
        this.myUriEncoding = uRIEncoding;
    }

}