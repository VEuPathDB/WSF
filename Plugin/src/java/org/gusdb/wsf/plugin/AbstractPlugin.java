/**
 * 
 */
package org.gusdb.wsf.plugin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * The WsfPlugin provides the common routines a plugin needs to simplify the
 * development of new WSF plugins.
 * 
 * @author Jerric
 * @created Feb 9, 2006
 */
public abstract class AbstractPlugin implements Plugin {

    protected static final String newline = System.getProperty("line.separator");

    private static final String CTX_CONFIG_PATH = "wsfConfigDir_param";

    protected abstract String[] defineContextKeys();

    /**
     * The logger for this plugin. It is a recommended way to record standard
     * output and error messages.
     */
    protected Logger logger;

    protected Map<String, Object> context;

    /**
     * It stores the properties defined in the configuration file. If the plugin
     * doesn't use a configuration file, this map is empty.
     */
    private Properties properties;

    /**
     * Initialize a plugin with empty properties
     */
    public AbstractPlugin() {
        this.logger = Logger.getLogger(AbstractPlugin.class); // use default
        properties = new Properties();
    }

    /**
     * Initialize a plugin and assign a property file to it
     * 
     * @param propertyFile
     *            the name of the property file. The base class will resolve the
     *            path to this file, which should be under the WEB-INF of axis'
     *            webapps.
     * @throws WsfServiceException
     */
    public AbstractPlugin(String propertyFile) throws WsfServiceException {
        this();
        // load the properties
        try {
            loadConfiguration(propertyFile);
        } catch (IOException ex) {
            logger.error(ex);
            throw new WsfServiceException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.plugin.Plugin#getContextKeys()
     */
    public String[] getContextKeys() {
        String[] keys = defineContextKeys();

        if (keys == null) {
            return new String[] { CTX_CONFIG_PATH };
        } else {
            String[] newKeys = new String[keys.length + 1];
            newKeys[0] = CTX_CONFIG_PATH;
            System.arraycopy(keys, 0, newKeys, 1, keys.length);
            return newKeys;
        }
    }

    public void setContext(Map<String, Object> context) {
        this.context = new HashMap<String, Object>(context);
    }

    private void loadConfiguration(String configFile)
            throws InvalidPropertiesFormatException, IOException {
        String configDir = (String) context.get(CTX_CONFIG_PATH);
        String filePath;
        if (configDir == null) {
            URL url = this.getClass().getResource("/" + configFile);
            filePath = url.toString();
        } else {
            if (!configDir.endsWith("/")) configDir += "/";
            filePath = configDir + configFile;
        }
        logger.debug("WSF Plugin prop file: " + filePath);

        InputStream in = new FileInputStream(filePath);
        properties.loadFromXML(in);
        in.close();
    }

    protected String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    /**
     * @param command
     *            the command array. If you have param values with spaces in it,
     *            put the value into one cell to avoid the value to be splitted.
     * @param timeout
     *            the maximum allowed time for the command to run, in seconds
     * @param result
     *            Contains raw output of the command.
     * @return the exit code of the invoked command
     * @throws IOException
     */
    protected int invokeCommand(String[] command, StringBuffer result,
            long timeout) throws IOException {
        logger.debug("WsfPlugin.invokeCommand()");
        // invoke the command
        Process process = Runtime.getRuntime().exec(command);

        StringBuffer sbErr = new StringBuffer();
        StringBuffer sbOut = new StringBuffer();

        // any error message?
        StreamGobbler errorGobbler = new StreamGobbler(
                process.getErrorStream(), "ERROR", sbErr);
        // any output?
        StreamGobbler outputGobbler = new StreamGobbler(
                process.getInputStream(), "OUTPUT", sbOut);
        logger.info("kicking off the stderr and stdout stream gobbling threads...");
        errorGobbler.start();
        outputGobbler.start();

        long start = System.currentTimeMillis();
        long limit = timeout * 1000;
        // check the exit value of the process; if the process is not
        // finished yet, an IllegalThreadStateException is thrown out
        int signal = -1;
        while (true) {
            try {
                logger.debug("waiting for 1 second ...");
                Thread.sleep(1000);

                signal = process.exitValue();
                result.append((signal == 0) ? sbOut : sbErr);
                break;
            } catch (IllegalThreadStateException ex) {
                // if the timeout is set to <= 0, keep waiting till the process
                // is finished
                if (timeout <= 0) continue;

                // otherwise, check if time's up
                long time = System.currentTimeMillis() - start;
                if (time > limit) {
                    logger.warn("Time out, the command is cancelled: "
                            + command);
                    outputGobbler.close();
                    errorGobbler.close();
                    process.destroy();
                    result.append("Time out, the command is cancelled.");
                    break;
                }
            } catch (InterruptedException ex) {
                // do nothing, keep looping
                continue;
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

        public void run() {
            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));
                String line = null;
                while ((line = br.readLine()) != null) {
                    // sb.append(type + ">" + line);
                    sb.append(line + newline);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void close() {
            try {
                is.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
