# DevLauncher

DevLauncher is a simple Java framework allowing you to easily configure an
embedded [Tomcat](http://tomcat.apache.org/) servlet container to startup
your web application.

Although Apache Maven provides a basic embedded webserver integration, the
DevLauncher gives you much more flexibility during the configuration and allows
a finer interaction with the launching process. It also doesn't depend on
Maven as build environment and can be used in standalone applications.

## Download

The latest version of the DevLauncher can be downloaded from:
**[http://dev.perdian.de/devlauncher/releases/latest/](http://dev.perdian.de/devlauncher/releases/latest/)**

The releases are also available from Maven Central using the following dependency:

      <dependency>
        <groupId>de.perdian.apps.devlauncher</groupId>
        <artifactId>devlauncher</artifactId>
        <version>2.0.3</version>
      </dependency>

## Usage

Using the DevLauncher is very easy (as it should be, since you want a framework
to make your life easier, not the other way around).

### Hello World!

A "Hello World" example, in which a simple web application without any special
configuration is started looks like this:

      public class SimpleExample {

        public static void main(String[] args) throws Exception {

          DevLauncherBuilder devLauncherBuilder = new DevLauncherBuilder();
          DevLauncher devLauncher = new DevLauncher();
          devLauncher.addListener(new SimpleWebappListener("simple", "src/example/webapp/simple/"));
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

## Adding additional configuration options

The DevLauncher class can be configured with a series of additional options to
aid in your development cycle.

### Properties initialization

Additional properties will be read from the system properties - if they are
set. Additionally, you can define an external properties file. All the
properties in this file are added to the system properties before any
configuration is done. This allows you to externalize your properties in one
location.

If there is a system property already existing when the external configuration
is loaded, then the existing value will remain unchanged, the new value from the
properties file will be ignored.

Let's take a look at another example:

      public class SystemPropertyTest {

        public static void main(String[] args) {

          System.setProperty("devlauncher.configurationFile", "/home/foo/file.properties");

          DevLauncherBuilder devLauncherBuilder = new DevLauncherBuilder();
          DevLauncher devLauncher = new DevLauncher();
          devLauncher.addListener(new SimpleWebappListener("simple", "src/example/webapp/simple/"));
          devLauncher.launch();

        }

      }

Here, the properties from the configuration file at `/home/foo/file.properties`
will be made available in the system properties. If you do not specify a value
for `devlauncher.configurationFile` the file will be expected in the current
directory from which the Java application was started. If it can't be found
there, then no additional properties will be set.

### Properties

Either through setting them directly as system property or through
initialization via the configuraion file (as shown above) the following
properties will be evaluated by the launcher:

#### devlauncher.defaultPort (int)

> The port on which the server will listen for incoming requests.
>
> Default value: `8080`.

#### devlauncher.shutdownPort (int)

> The port on which the server will open a shutdown connection, where a new
> instance of the application will first contact any running old instance on
> that port to make sure the old instance is shutdown first, before the new
> instance initializes the embedded webserver.
>
> Default value: `8081`

### devlauncher.workingDirectory (String)

> A directory on the local file system, where the launcher will store it's
> temporary data (like the workfiles from the webserver)
>
> Default value: `[User_Home]/.devlauncher`

### devlauncher.workingDirectoryName (String)

> The `.devlauncher` part of the default directory (see above) can be customized
> separately. It can also be customized by passing at as constructor argument
> to the constructor of the `DevLauncherBuilder`.
>
> Default value: `.devlauncher` (see above)

## Listeners

Implementations of the `DevLauncherListener` interface can be added to the
`DevLauncher` to enable fine tuning of the embedded webserver in a powerful
way. A series of provided listeners can be utilized for common tasks. Following
a list of the listeners provided by the devlauncher out of the box:

### de.perdian.apps.devlauncher.impl.connectors.SimpleConnectorListener

Adds a simple HTTP connector to the embedded webserver. Additionally to the
port defined within the `DevLauncher` itself, the server will also listen for
incoming requests on the port defined in the connector.

For example, if you want the server to not only listen on port 8080 but also on
port 9090, the following code can be used:

      DevLauncherBuilder devLauncherBuilder = new DevLauncherBuilder();
      DevLauncher devLauncher = new DevLauncher();
      devLauncher.addListener(new SimpleConnectorListener(9090));
      devLauncher.addListener(new SimpleWebappListener("simple", "src/example/webapp/simple/"));
      devLauncher.launch();

### de.perdian.apps.devlauncher.impl.connectors.TlsConnectorListener

Adds a TLS connector, which enables requests made using the HTTPS protocol.
The listener will make sure, that a self signed certificate is available that
is used during the transfer. The certificate itself will then be stored in the
working directory, that is passed as constructor argument:

      File workingDirectory = new File("/home/foo/directory");
      DevLauncherBuilder devLauncherBuilder = new DevLauncherBuilder();
      DevLauncher devLauncher = new DevLauncher();
      devLauncher.addListener(new TlsConnectorListener(workingDirectory, 443));
      devLauncher.addListener(new SimpleWebappListener("simple", "src/example/webapp/simple/"));
      devLauncher.launch();

### de.perdian.apps.devlauncher.impl.webapps.SimpleWebappListener

As seen in the first example, the ´SimpleWebappListener` makes the content of
a directory available within a web application.

### de.perdian.apps.devlauncher.impl.webapps.ExtendedWebappListener

Adds a webapp to the embedded webserver instance. The only constructor argument
is the name of the webapp under which it will be made available in the server.
The webapp itself is automatically lookup up.

Instead of writing a detailed documentation, take a look at the following
implementation code which will give a very good introduction of how the
initialization is done:

      String webappName = ... // The name passed to the constructor
      String projectRootDirectoryValue = System.getProperty("devlauncher.projectRootDirectory", null);
      File projectRootDirectory = projectDirectoryValue == null ? new File(".").getParentFile() : new File(projectDirectoryValue);
      String projectDirectoryValue = System.getProperty("devlauncher.project." + webappName, null);
      File projectDirectory = projectDirectoryValue == null ? new File(projectRootDirectory, webappName) : new File(projectDirectoryValue);
      File webappDirectory = new File(projectDirectory, "src/main/webapp");

The computed `webappDirectory` will then be used as source for the web
application that is added to the embedded webserver.
