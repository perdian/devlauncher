/*
 * DevLauncher
 * Copyright 2013 Christian Robert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.perdian.apps.devlauncher.impl.connectors;

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
 * Create the TLS connector, which is the connector that will listen to
 * requests made via HTTPS. A self signed certificate will automatically be
 * created (if it hasn't been created already) to make sure that the browser
 * can successfully establish an SSL connection directly to the Tomcat
 * webserver without the need for an additional Apache HTTPD server in
 * front.
 *
 * @author Christian Robert
 */

@SuppressWarnings("deprecation")
public class TlsConnectorListener implements DevLauncherListener {

    private static final Logger log = LoggerFactory.getLogger(TlsConnectorListener.class);

    private static final String KEYSTORE_PASSWORD = "tlsKeystorePassword";
    private static final String TLS_KEY_NAME = "tlsKeyName";
    private static final String TLS_KEY_PASSWORD = "tlsKeyPassword";

    private File myWorkingDirectory = null;
    private int myPort = -1;

    public TlsConnectorListener(File workingDirectory, int port) {
        this.setWorkingDirectory(workingDirectory);
        this.setPort(port);
    }

    @Override
    public void customizeServer(Tomcat tomcat, DevLauncher launcher) throws Exception {

        // First we need to make sure, that we have a valid KeyStore in our
        // configuration, that is used to keep track of the TLS certificate
        File keystoreFile = new File(this.getWorkingDirectory(), "config/keystore");
        KeyStore keyStore = this.ensureKeyStore(keystoreFile);
        this.ensureKeyInStore(keystoreFile, keyStore);

        Connector tlsConnector = new Connector();
        tlsConnector.setPort(this.getPort());
        tlsConnector.setSecure(true);
        tlsConnector.setScheme("https");
        tlsConnector.setAttribute("keyAlias", TLS_KEY_NAME);
        tlsConnector.setAttribute("keyPass", TLS_KEY_PASSWORD);
        tlsConnector.setAttribute("keystoreFile", keystoreFile.getCanonicalPath());
        tlsConnector.setAttribute("keystorePass", KEYSTORE_PASSWORD);
        tlsConnector.setAttribute("clientAuth", "false");
        tlsConnector.setAttribute("sslProtocol", "TLS");
        tlsConnector.setAttribute("SSLEnabled", true);

        log.debug("Adding new TLS context listening on port: " + this.getPort());
        tomcat.getConnector().setRedirectPort(this.getPort());
        tomcat.getService().addConnector(tlsConnector);

    }

    private Key ensureKeyInStore(File keystoreFile, KeyStore keyStore) throws GeneralSecurityException, IOException {
        Key key = this.lookupKeyFromStore(keyStore);
        if(key == null) {

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
            v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)));
            v3CertGen.setSubjectDN(new X509Principal("CN=" + "localhost" + ", OU=None, O=None L=None, C=None"));
            v3CertGen.setPublicKey(keyPair.getPublic());
            v3CertGen.setSignatureAlgorithm("MD5WithRSAEncryption");
            X509Certificate certificate = v3CertGen.generateX509Certificate(keyPair.getPrivate());

            // Store the key (including the certificate) into the keystore
            keyStore.setKeyEntry(TLS_KEY_NAME, keyPair.getPrivate(), TLS_KEY_PASSWORD.toCharArray(), new java.security.cert.Certificate[] { certificate });

            // Write the keystore into the target file
            log.debug("Updating KeyStore at: " + keystoreFile.getAbsolutePath());
            if(!keystoreFile.getParentFile().exists()) {
                keystoreFile.getParentFile().mkdirs();
            }
            OutputStream keyStoreStream = new BufferedOutputStream(new FileOutputStream(keystoreFile));
            try {
                keyStore.store(keyStoreStream, KEYSTORE_PASSWORD.toCharArray());
                keyStoreStream.flush();
            } finally {
                keyStoreStream.close();
            }

        }
        return key;
    }

    private Key lookupKeyFromStore(KeyStore keyStore) {
        try {
            Key key = keyStore.getKey(TLS_KEY_NAME, TLS_KEY_PASSWORD.toCharArray());
            if(key != null) {
                log.debug("Found key '" + TLS_KEY_NAME + "' in KeyStore with format: " + key.getFormat());
            }
            return key;
        } catch(GeneralSecurityException e) {
            log.debug("Cannot retrieve key from KeyStore", e);
            return null;
        }
    }

    private KeyStore ensureKeyStore(File keystoreFile) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        if(keystoreFile.exists()) {
            try {
                InputStream keystoreFileStream = new BufferedInputStream(new FileInputStream(keystoreFile));
                try {
                    keyStore.load(keystoreFileStream, KEYSTORE_PASSWORD.toCharArray());
                } finally {
                    keystoreFileStream.close();
                }
            } catch(Exception e) {
                log.warn("Cannot load KeyStore from file at: " + keystoreFile.getAbsolutePath());
            }
        }
        return keyStore;
    }

    // -------------------------------------------------------------------------
    // ---  Property access methods  -------------------------------------------
    // -------------------------------------------------------------------------

    private File getWorkingDirectory() {
        return this.myWorkingDirectory;
    }
    private void setWorkingDirectory(File workingDirectory) {
        this.myWorkingDirectory = workingDirectory;
    }

    private int getPort() {
        return this.myPort;
    }
    private void setPort(int port) {
        this.myPort = port;
    }

}