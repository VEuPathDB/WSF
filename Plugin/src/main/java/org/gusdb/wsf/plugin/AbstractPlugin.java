package org.gusdb.wsf.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wsf.common.PluginRequest;
import org.gusdb.wsf.common.WsfException;

/**
 * 
 * An abstract super class for all WSF plugins. This class is a SINGLETON. Instance variables apply to all
 * calls of the plugin.
 * 
 * @author Jerric
 * @created Feb 9, 2006
 */
public abstract class AbstractPlugin implements Plugin {

  public static final String newline = System.getProperty("line.separator");

  protected abstract int execute(PluginRequest request, PluginResponse response) throws WsfException;

  /**
   * The logger for this plugin. It is a recommended way to record standard output and error messages.
   */
  private static final Logger LOG = Logger.getLogger(AbstractPlugin.class);

  /**
   * It stores the properties defined in the configuration file. If the plugin doesn't use a configuration
   * file, this map is empty.
   */
  protected Properties properties;

  private String propertyFile;

  /**
   * Initialize a plugin with empty properties
   */
  public AbstractPlugin() {
    properties = new Properties();
  }

  /**
   * Initialize a plugin and assign a property file to it
   * 
   * @param propertyFile
   *          the name of the property file. The base class will resolve the path to this file, which should
   *          be under the WEB-INF of axis' webapps.
   */
  public AbstractPlugin(String propertyFile) {
    this();
    this.propertyFile = propertyFile;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wsf.plugin.Plugin#initialize(java.util.Map)
   */
  @Override
  public void initialize() throws WsfPluginException {
    // load the properties
    if (propertyFile != null) {
      try {
        loadConfiguration();
      }
      catch (IOException ex) {
        LOG.error(ex);
        throw new WsfPluginException(ex);
      }
    }
  }

  @Override
  public int invoke(PluginRequest request, PluginResponse response) throws WsfException {
    try {
      return execute(request, response);
    }
    catch (WsfPluginException ex) {
      throw ex;
    }
  }

  private void loadConfiguration() throws InvalidPropertiesFormatException, IOException, WsfPluginException {
    String configDir = null;
    String filePath = null;

    String gusHome = GusHome.getGusHome();
    if (gusHome != null)
      configDir = gusHome + "/config/";

    // if config is null, try loading the resource from class path root.
    if (configDir == null) {
      URL url = this.getClass().getResource("/" + propertyFile);
      if (url == null)
        throw new WsfPluginException("property file cannot be found " + "in the class path: " + propertyFile);

      filePath = url.toString();
    }
    else {
      if (!configDir.endsWith("/"))
        configDir += "/";
      String path = configDir + propertyFile;
      File file = new File(path);
      if (!file.exists() || !file.isFile())
        throw new WsfPluginException("property file cannot be found " + " in the configuration path: " + path);

      filePath = path;
    }
    LOG.debug("WSF Plugin prop file: " + filePath);

    InputStream in = new FileInputStream(filePath);
    properties.loadFromXML(in);
    in.close();
  }

  protected String getProperty(String propertyName) {
    return properties.getProperty(propertyName);
  }

  protected boolean hasProperty(String propertyName) {
    return properties.containsKey(propertyName);
  }

  protected int invokeCommand(String[] command, StringBuffer result, long timeout) throws IOException,
      WsfPluginException {
    return invokeCommand(command, result, timeout, null);
  }

  /**
   * @param command
   *          the command array. If you have param values with spaces in it, put the value into one cell to
   *          avoid the value to be splitted.
   * @param timeout
   *          the maximum allowed time for the command to run, in seconds
   * @param result
   *          Contains raw output of the command.
   * @param env
   *          a string including env variables, as expected by exec. Useful to pass in a PATH
   * @return the exit code of the invoked command
   * @throws IOException
   * @throws WsfPluginException
   */
  protected int invokeCommand(String[] command, StringBuffer result, long timeout, String[] env)
      throws IOException, WsfPluginException {
    LOG.info("WsfPlugin.invokeCommand: " + FormatUtil .printArray(command));
    // invoke the command
    Process process = Runtime.getRuntime().exec(command, env);

    StringBuffer sbErr = new StringBuffer();
    StringBuffer sbOut = new StringBuffer();

    // any error message?
    StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR", sbErr);
    // any output?
    StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT", sbOut);
    LOG.info("kicking off the stderr and stdout stream gobbling threads...");
    errorGobbler.start();
    outputGobbler.start();

    long start = System.currentTimeMillis();
    long limit = timeout * 1000;
    // check the exit value of the process; if the process is not
    // finished yet, an IllegalThreadStateException is thrown out
    int signal = -1;
    while (true) {
      LOG.debug("waiting for 1 second ...");
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException ex) {
        // do nothing, keep looping
      }

      try {
        signal = process.exitValue(); // throws IllegalThreadStateException if the process is still running.

        // process is stopped.
        result.append((signal == 0) ? sbOut : sbErr);
        break;
      }
      catch (IllegalThreadStateException ex) {
        // if the timeout is set to <= 0, keep waiting till the process
        // is finished
        if (timeout <= 0)
          continue;

        // otherwise, check if time's up
        long time = System.currentTimeMillis() - start;
        if (time > limit) {
          // convert string array to string
          StringBuilder buffer = new StringBuilder();
          for (String piece : command) {
            if (buffer.length() > 0)
              buffer.append(" ");
            buffer.append(piece);
          }
          LOG.warn("Time out, the command is cancelled: " + buffer);
          outputGobbler.close();
          errorGobbler.close();
          process.destroy();
          throw new WsfPluginException("Time out, the command is cancelled.");
        }
      }
    }
    return signal;
  }

  class StreamGobbler extends Thread {

    InputStream is;
    String type;
    StringBuffer sb;

    StreamGobbler(InputStream is, String type, StringBuffer sb) {
      this.is = is;
      this.type = type;
      this.sb = sb;
    }

    @Override
    public void run() {
      try {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = br.readLine()) != null) {
          // sb.append(type + ">" + line);
          sb.append(line + newline);
        }
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
      finally {
        try {
          is.close();
        }
        catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    }

    public void close() {
      try {
        is.close();
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}