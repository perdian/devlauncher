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

public class ConnectorListenerBuilder {

    private Integer port = null;
    private Integer redirectPort = null;
    private String protocol = null;
    private String uriEncoding = "UTF-8";
    private boolean secure = false;

    public ConnectorListener createListener() {
        ConnectorListener listener = new ConnectorListener();
        listener.setPort(this.getPort());
        listener.setRedirectPort(this.getRedirectPort());
        listener.setProtocol(this.getProtocol());
        listener.setUriEncoding(this.getUriEncoding());
        listener.setSecure(this.isSecure());
        return listener;
    }

    // -------------------------------------------------------------------------
    // --- Property access methods ---------------------------------------------
    // -------------------------------------------------------------------------

    public ConnectorListenerBuilder port(int port) {
        this.setPort(Integer.valueOf(port));
        return this;
    }
    public Integer getPort() {
        return this.port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }

    public ConnectorListenerBuilder redirectPort(int redirectPort) {
        this.setRedirectPort(Integer.valueOf(redirectPort));
        return this;
    }
    public Integer getRedirectPort() {
        return this.redirectPort;
    }
    public void setRedirectPort(Integer redirectPort) {
        this.redirectPort = redirectPort;
    }

    public ConnectorListenerBuilder ajp() {
        return this.protocol(ConnectorListener.PROTOCOL_AJP);
    }
    public ConnectorListenerBuilder protocol(String protocol) {
        this.setProtocol(protocol);
        return this;
    }
    public String getProtocol() {
        return this.protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public ConnectorListenerBuilder uriEncoding(String uriEncoding) {
        this.setUriEncoding(uriEncoding);
        return this;
    }
    public String getUriEncoding() {
        return this.uriEncoding;
    }
    public void setUriEncoding(String uriEncoding) {
        this.uriEncoding = uriEncoding;
    }

    public ConnectorListenerBuilder secure(boolean secure) {
        this.setSecure(secure);
        return this;
    }
    public boolean isSecure() {
        return this.secure;
    }
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

}