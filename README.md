# DevLauncher

Web application launcher to easy up developing web applications.

The devlauncher is a simple Java framework allowing you to easily configure an
embedded [Jetty](http://www.eclipse.org/jetty/) servlet container to startup
your web application already configured using a Spring ApplicationContext.

Although Apache Maven provides a basic embedded webserver integration, the
DevLauncher gives you much more flexibility to configuration and interact with
the launching process and is also available outside a maven environment.

## Download

The DevLauncher can be downloaded from: **[http://dev.perdian.de/devlauncher/releases/latest/](http://dev.perdian.de/devlauncher/releases/latest/)**

## Usage

Using the DevLauncher is very easy (as it should be, since you want a framework
to make your life easier, not the other way around).

### Hello World!

A "Hello World" example, in
which a simple web application without any special configuration is started
looks like this:

      package de.perdian.apps.devlauncher.example;

      import de.perdian.apps.devlauncher.DevLauncher;

      public class SimpleExample {

        public static void main(String[] args) {

          DevLauncher devLauncher = new DevLauncher();
          devLauncher.setWebappDirectory("src/example/webapp/simple/");
          devLauncher.launch();

        }

      }

This is the simplest example thinkable. The HTTP port under which the embedded
server will be started is 8080 and the context path will be the root context, so
after executing the main method of the DevLauncher, the example application will
be available at: `http://localhost:8080/`.

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

### Adding additional configuration options

The DevLauncher class can be configured with a series of additional options to
aid in your development cycle.

#### initProperties

Additional properties can be added that will be passed to the web application
and will be available from within the `ServletContext`. For example:

      package de.perdian.apps.devlauncher.example;

      import java.util.Properties;

      import de.perdian.apps.devlauncher.DevLauncher;

      public class PropertiesPassingExample {

        public static void main(String[] args) {

          Properties initParameters = new Properties();
          initParameters.setProperty("foo", "bar");

          DevLauncher devLauncher = new DevLauncher();
          devLauncher.setWebappDirectory("src/example/webapp/propertiesPassing/");
          devLauncher.setWebappInitParameters(initParameters);
          devLauncher.launch();

        }

      }

Which will result in the following output from the servlet:

      package de.perdian.apps.devlauncher.example;

      import java.io.IOException;

      import javax.servlet.ServletException;
      import javax.servlet.http.HttpServlet;
      import javax.servlet.http.HttpServletRequest;
      import javax.servlet.http.HttpServletResponse;

      public class PropertiesPassingServlet extends HttpServlet {

        static final long serialVersionUID = 1L;

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

          resp.getWriter().println("Value for foo: " + this.getServletContext().getInitParameter("foo"));

          // The result should be:
          // Value for foo: bar

        }

      }