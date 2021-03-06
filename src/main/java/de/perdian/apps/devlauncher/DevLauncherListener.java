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
package de.perdian.apps.devlauncher;

import java.io.IOException;

import org.apache.catalina.startup.Tomcat;

/**
 * Receive notifications about lifecycle events during the launch process
 *
 * @author Christian Robert
 */

@FunctionalInterface
public interface DevLauncherListener {

    /**
     * Customizes the Tomcat instance before it is about to be started
     *
     * @param tomcat
     *      the Tomcat instance to be customized
     * @param devLauncher
     *      the launcher instance in which the listener is to be executed
     * @throws IOException
     *      thrown if the server cannot be started correctly
     */
    void customizeServer(Tomcat tomcat, DevLauncher devLauncher) throws IOException;

}