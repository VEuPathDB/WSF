/**
 * 
 */
package org.gusdb.wsf.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.log4j.Logger;

/**
 * The WsfPlugin provides the common routines a plugin needs to simplify the
 * development of new WSF plugins.
 * 
 * @author Jerric
 * @created Feb 9, 2006
 */
public abstract class WsfPlugin implements IWsfPlugin {

    protected static final String newline = System.getProperty("line.separator");

    /**
     * The Plugin needs to provide a list of required parameter names; the base
     * class will use this template method in the input validation process.
     * 
     * @return returns an array the names of the required parameters
     */
    protected abstract String[] getRequiredParameterNames();

    /**
     * The Plugin needs to provides a list of the columns expected in the
     * result; the base class will use this template method in the input
     * validation process.
     * 
     * @return returns an array the columns expected in the result
     */
    protected abstract String[] getColumns();

    /**
     * The plugin should implement this method to do the real job, for example,
     * to invoke an application, and prepare the results into tabular format,
     * and then return the results.
     * 
     * @param queryName
     *            the name of the query that invokes this plugin
     * @param params
     *            The <name, value> Map of parameters given by the client
     * @param orderedColumns
     *            The ordered columns assigned by the client. each of the
     *            columns must match with one column sepecified in getColumns()
     *            by the plugin. The The plugin is responsible to re-format the
     *            result following the order of the columns defined here.
     * @return returns the result in 2-dimensional array of strings format.
     * @throws WsfServiceException
     */
    protected abstract WsfResult execute(String projectId,
            String userSignature, Map<String, String> params,
            String[] orderedColumns) throws WsfServiceException;

    /**
     * The logger for this plugin. It is a recommended way to record standard
     * output and error messages.
     */
    protected Logger logger;

    protected ServletContext servletContext;

    /**
     * It stores the properties defined in the configuration file. If the plugin
     * doesn't use a configuration file, this map is empty.
     */
    private Properties properties;

    /**
     * Initialize a plugin with empty properties
     */
    public WsfPlugin() {
        this.logger = Logger.getLogger(WsfPlugin.class); // use default
        properties = new Properties();

        MessageContext msgContext = MessageContext.getCurrentContext();
        Servlet servlet = (Servlet) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLET);
        servletContext = servlet.getServletConfig().getServletContext();
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
    public WsfPlugin(String propertyFile) throws WsfServiceException {
        this();
        // load the properties
        try {
            loadPropertyFile(propertyFile);
        } catch (IOException ex) {
            logger.error(ex);
            throw new WsfServiceException(ex);
        }
    }

    /**
     * @param logger
     *            set a different logger to the plugin. The WsfService will
     *            assign a specific logger to each plugin.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * The service will call the plugin through this interface. Plugin cannot
     * override this method from the base class, instead it should implement
     * execute() method.
     * 
     * @see org.gusdb.wsf.IWsfPlugin#invoke(java.util.Map, java.lang.String[])
     */
    public final WsfResult invoke(String projectId, String userSignature,
            Map<String, String> params, String[] orderedColumns)
            throws WsfServiceException {
        // validate the input
        logger.debug("WsfPlugin.validateInput()");
        validateInput(projectId, params, orderedColumns);

        // execute the main function, and obtain result
        logger.debug("WsfPlugin.execute()");
        WsfResult result = execute(projectId, userSignature, params,
                orderedColumns);
        // TEST
        logger.info("Result Message: '" + result.getMessage() + "'");

        return result;
    }

    private void validateInput(String projectId, Map<String, String> params,
            String[] orderedColumns) throws WsfServiceException {
        // validate required parameters
        validateRequiredParameters(params);

        // validate parameters
        validateParameters(params);

        // validate columns
        validateColumns(orderedColumns);
    }

    protected abstract void validateParameters(Map<String, String> params)
            throws WsfServiceException;

    private void validateRequiredParameters(Map<String, String> params)
            throws WsfServiceException {
        String[] reqParams = getRequiredParameterNames();

        // validate parameters
        for (String param : reqParams) {
            if (!params.containsKey(param)) {
                throw new WsfServiceException(
                        "The required parameter is missing: " + param);
            }
        }
    }

    protected void validateColumns(String[] orderedColumns)
            throws WsfServiceException {
        String[] reqColumns = getColumns();

        Set<String> colSet = new HashSet<String>(orderedColumns.length);
        for (String col : orderedColumns) {
            colSet.add(col);
        }
        for (String col : reqColumns) {
            if (!colSet.contains(col)) {
                throw new WsfServiceException(
                        "The required column is missing: " + col);
            }
        }
        // cross check
        colSet.clear();
        colSet = new HashSet<String>(reqColumns.length);
        for (String col : reqColumns) {
            colSet.add(col);
        }
        for (String col : orderedColumns) {
            if (!colSet.contains(col)) {
                throw new WsfServiceException("Unknown column: " + col);
            }
        }
    }

    private void loadPropertyFile(String propertyFile)
            throws InvalidPropertiesFormatException, IOException {

        String wsfConfigDir = servletContext.getInitParameter("wsfConfigDir_param");
        if (wsfConfigDir == null) {
            wsfConfigDir = "WEB-INF/wsf-config/";
        }

        String root = servletContext.getRealPath("/");
        logger.info(root);

        // String root = System.getProperty("webservice.home");
        File rootDir;
        // if (root == null) {
        // if the webservice.home is not specified, by default, we assume
        // the Axis is installed under ${tomcat_home}/webapps
        // root = System.getProperty("catalina.home");
        // root = System.getProperty("catalina.base");
        // rootDir = new File(root, "webapps/axis");
        // } else
        rootDir = new File(root);
        File configFile = new File(rootDir, wsfConfigDir + propertyFile);
        InputStream in = new FileInputStream(configFile);
        properties.loadFromXML(in);
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
