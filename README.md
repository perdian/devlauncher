# DevLauncher

DevLauncher is a simple Java framework allowing you to easily configure an
embedded [Tomcat](http://tomcat.apache.org/) servlet container to startup
your web application.

Although Apache Maven provides a basic embedded webserver integration, the
DevLauncher gives you much more flexibility during the configuration and allows
a finer interaction with the launching process. It also doesn't depend on
Maven as build environment and can be used in standalone applications.

## Download

The official releases are available from Maven Central using the following
dependency:

      <dependency>
          <groupId>de.perdian.apps.devlauncher</groupId>
          <artifactId>devlauncher</artifactId>
          <version>4.0.0</version>
      </dependency>

## Usage

Using the DevLauncher is very easy (as it should be, since you want a framework
to make your life easier, not the other way around).

### Hello World!

A "Hello World" example, in which a simple web application without any special
configuration is started looks like this:

      public class SimpleExample {

          public static void main(String[] args) throws Exception {

              DevLauncher devLauncher = new DevLauncher();
              devLauncher.addListener(new ExplodedWebappListenerBuilder().contextName("simple").webappDirectory(Path.get("src/example/webapp/simple/")).createListener());
              devLauncher.launch();

          }

      }

This is the simplest example thinkable. The HTTP port under which the embedded
server will be started is 8080 and the context path will be the root context, so
after executing the main method of the DevLauncher, the example application will
be available at: `http://localhost:8080/simple/`.

The DevLauncher doesn't do very much here in terms of configuration etc. So
let's introduce the first real feature which is already there even if you don't
see it: Automatic server shutdown.

While developing a web application you often need to restart the server in order
for the new changes to be available. This also means, that you will have to stop
the old server before starting a new instance. As for me: This is something I
usually forget followed by the realization "Oh yes, that's why the changes
aren't visible yet - the old instance is still running!".

When executing the `launch` Method, the DevLauncher will automatically stop any
previously running Server instance (if it has been started by DevLauncher)
before starting up the new server. So all you have to do once you want to start
or restart your server is to execute the `launch` method and DevLauncher will
take care of checking whether or not there is any active instance.

If you do not want this feature to be available, you can disable it by simply
setting the shutdownListenerPort to a value of 0 or less.

## Listeners

Implementations of the `DevLauncherListener` interface can be added to the
`DevLauncher` to enable fine tuning of the embedded webserver in a powerful
way. A series of provided listeners can be utilized for common tasks. Following
a list of the listeners provided by the devlauncher out of the box.

Every listener can either be created manually or through a corresponding Builder
that allows an easy fluent API to configure the target listener

### de.perdian.apps.devlauncher.impl.ConnectorListener

Adds a connector to the embedded webserver. A connector represetns an endpoint
that listens on a specified port for incoming connections and handles these
connections. Additionally to the port defined within the `DevLauncher` itself,
the server will also listen for incoming requests on the port defined in the
connector.

For example, if you want the server to not only listen on port 8080 but also on
port 9090, the following code can be used:

      DevLauncher devLauncher = DevLauncher.createLauncher();
      devLauncher.addListener(new ConnectorListenerBuilder().port(9090).createListener());
      devLauncher.addListener(new ExplodedWebappListener().contextName("simple").webappDirectory(Path.get("src/example/webapp/simple/")).createListener());
      devLauncher.launch();

You can also customize the protocol to be used for the connector. For example to
add a listener on port 8009 listening for AJP requests (and using a redirect
port of 8443), the initialization looks like this:

      DevLauncher devLauncher = DevLauncher.createLauncher();
      devLauncher.addListener(new ConnectorListenerBuilder().port(9090));
      devLauncher.addListener(new ConnectorListenerBuilder().port(8009).ajp().redirectPort(8443).createListener());
      devLauncher.addListener(new ExplodedWebappListenerBuilder().contextName("simple").webappDirectory(Path.get("src/example/webapp/simple/")).createListener());
      devLauncher.launch();

When setting the `secure` property of the `ConnectorListener` to true, the
connector will use the TLS (HTTPS) protocol for handling incoming requests.
The listener will make sure, that a self signed certificate is available that
is used during the transfer. The certificate itself will then be stored in the
working directory of the launcher for further use, so if it has been created
once (and most likely has been added to the exception list of your browser) it
will be reused the next time you use the DevLauncher.

      DevLauncher devLauncher = DevLauncher.createLauncher();
      devLauncher.addListener(new ConnectorListenerBuilder().port(443).secure(true).createListener());
      devLauncher.addListener(new ExplodedWebappListenerBuilder().contextName("simple").webappDirectory(Path.get("src/example/webapp/simple/")).createListener());
      devLauncher.launch();

### de.perdian.apps.devlauncher.impl.ExplodedWebappListener

As seen in the first example, the `ExplodedWebappListener` makes the content of 
a directory available within a web application. Since the directory itself might
not just be a predefined directory, it provides several options to configure the
directory according to the way you configure the listener before adding it to
the `DevLauncher`.

The easiest way has been demonstrated before - you just create the listener
providing the name of the context under which it should be made available and
pass the directory from which the content should be served. However, if you do
not want to give the directory directory, there are a series of resoling steps:
Let's walk through the steps in detail.

First of all, we need to determine the **workspaceRootDirectory**. If you're
developing within a standard IDE you might have multiple directories containing
multiple projects. The root directory in which all these multiple directories
are contains is called the **workspaceRootDirectory**. You can defined this
directory by setting the system property (or configuration property as explained
above) named `devlauncher.workspaceDirectory`. If there is no configuration
property defined then the parent directory of the directory from which the
launcher was started will be selected. You can overwrite this schema by directly
setting the `workspaceDirectory` property of the listener.

When we know the workspace directory, we need to determine the
**projectDirectory**. This is the directory in which the web application project
is located (that is: the directory from which - if you're using Maven - the
WAR file is being generated). If a `projectDirectoryName` is set, then the
project directory will be the combination of `workspaceDirectory` and
`projectDirectoryName`. If no `projectDirectoryName` is set, then the
`contextName` will be used as project directory name. You can overwrite this 
schema directly by setting the `projectDirectory` property of the listener.

Now that we know the project directory, we need to determine the actual
directory in which the web application is located. By default (following the
Maven directory naming convention) this directory is `src/main/webapp`. You can
however overwrite this by setting the `webappDirectoryName` property of the
listener. This can also directly be overwritten by setting the `webappDirectory`
property of the listener.

You can also set a `contextConfigurationFile` that contains additional
configuration entries to be loaded once the embedded Tomcat server initializes
the web application. If you just specify a `contextConfigurationFileName` then
the configuration file will be resolved under the `projectDirectory` (which is
described above).

## Version history

### Version 4.0.0

[UPDATE] Increased required Java version to 1.8
[UPDATE] Adjusted dependency versions

### Version 3.2.0

[UPDATE] Replaced `DevLauncherBuilder` with static factory methods in
         `DevLauncher`

### Version 3.1.0

[UPDATE] Made `CopyResourcesListener` more customizable

### Version 3.0.1

[UPDATE] Made `CopyResourcesListener` fit into fluent API concept

### Version 3.0.0

[UPDATE] Eliminated the need for different connector and webapp listeners.
         Consolidated the existing logics into new listeners with fluent API for
         easier configuration and consistent resolving logics.

### Version 2.1.0

[NEW]    Added CopyResourcesListener