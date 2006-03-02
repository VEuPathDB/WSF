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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * The logger for this plugin. It is a recommended way to record standard
     * output and error messages.
     */
    protected Logger logger;

    /**
     * It stores the properties defined in the configuration file. If the plugin
     * doesn't use a configuration file, this map is empty.
     */
    private Properties properties;

    /**
     * it contains the exit value of the invoked appliaction. If the last
     * invocation is successfully finished, this value is 0; if the plugin
     * hasn't invoked any application, this value is -1; if the last invocation
     * is failed, this value can be any number other than 0. However, this is
     * not the recommended way to check if an invocation is succeeded or not
     * since it relies on the behavior of the external application.
     */
    protected int exitValue;

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
     * This is an optional method for the plugin to validate its parameter by
     * itself. The plugin can validate the conceptual type of certain parameter
     * values, as well as their value ranges. <br>
     * Plugin can ignore this by leave an empty implementation of this method.
     * <br>
     * If the validation fails, Plugin is expected to throw out a
     * WsfServiceException, describing the reson.
     * 
     * @param params
     * @throws WsfServiceException
     */
    protected abstract void validateParameters(Map<String, String> params)
            throws WsfServiceException;

    /**
     * The plugin should implement this method to do the real job, for example,
     * to invoke an application, and prepare the results into tabular format,
     * and then return the results.
     * 
     * @param params The <name, value> Map of parameters given by the client
     * @param orderedColumns The ordered columns assigned by the client. each of
     *        the columns must match with one column sepecified in getColumns()
     *        by the plugin. The The plugin is responsible to re-format the
     *        result following the order of the columns defined here.
     * @return returns the result in 2-dimensional array of strings format.
     * @throws WsfServiceException
     */
    protected abstract String[][] execute(Map<String, String> params,
            String[] orderedColumns) throws WsfServiceException;

    /**
     * Initialize a plugin with empty properties
     */
    public WsfPlugin() {
        this.logger = Logger.getLogger(WsfPlugin.class); // use default
        exitValue = -1;
        // logger
        properties = new Properties();
    }

    /**
     * Initialize a plugin and assign a property file to it
     * 
     * @param propertyFile the name of the property file. The base class will
     *        resolve the path to this file, which should be under the WEB-INF
     *        of axis' webapps.
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
     * @param logger set a different logger to the plugin. The WsfService will
     *        assign a specific logger to each plugin.
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
    public final String[][] invoke(Map<String, String> params,
            String[] orderedColumns) throws WsfServiceException {
        // validate the input
        logger.debug("WsfPlugin.validateInput()");
        if (!validateInput(params, orderedColumns))
            throw new WsfServiceException("Input validation failed.");

        // validate the type and content of parameters
        logger.debug("WsfPlugin.validateParameters()");
        validateParameters(params);

        // execute the main function, and obtain result
        logger.debug("WsfPlugin.execute()");
        return execute(params, orderedColumns);
    }

    private boolean validateInput(Map<String, String> params,
            String[] orderedColumns) {
        String[] reqParams = getRequiredParameterNames();
        String[] reqColumns = getColumns();

        // validate parameters
        for (String param : reqParams) {
            if (!params.containsKey(param)) {
                logger.error("The required parameter is missing: " + param);
                return false;
            }
        }

        // validate columns
        Set<String> colSet = new HashSet<String>(orderedColumns.length);
        for (String col : orderedColumns) {
            colSet.add(col);
        }
        for (String col : reqColumns) {
            if (!colSet.contains(col)) {
                logger.error("The expected column is missing: " + col);
                return false;
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
                logger.error("Unknown column: " + col);
                return false;
            }
        }
        return true;
    }

    private void loadPropertyFile(String propertyFile)
            throws InvalidPropertiesFormatException, IOException {
        String root = System.getProperty("webservice.home");
        File rootDir;
        if (root == null) {
            // if the webservice.home is not specified, by default, we assume
            // the Axis is installed under ${tomcat_home}/webapps
            root = System.getProperty("catalina.home");
            rootDir = new File(root, "webapps/axis");
        } else rootDir = new File(root);
        File configFile = new File(rootDir, "WEB-INF/wsf-config/" + propertyFile);
        InputStream in = new FileInputStream(configFile);
        properties.loadFromXML(in);
    }

    protected String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    protected String invokeCommand(String command, long timeout)
            throws IOException {
        logger.debug("WsfPlugin.invokeCommand()");
        // invoke the command
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        BufferedReader err = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));

        long start = System.currentTimeMillis();
        long limit = timeout * 1000;
        // check the exit value of the process; if the process is not
        // finished yet, an IllegalThreadStateException is thrown out
        while (true) {
            try {
                Thread.sleep(1000);

                exitValue = process.exitValue();

                // an exception will be thrown before reaching here if the
                // process is still running

                // obtain the standard output
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    sb.append(newline);
                }
                
                // obtain the error output, if have
                StringBuffer sberr = new StringBuffer();
                while ((line = err.readLine()) != null) {
                    sberr.append(line);
                    sberr.append(newline);
                }
                return (exitValue == 0)?sb.toString(): sberr.toString();
            } catch (IllegalThreadStateException ex) {
                // if the timeout is set to <= 0, keep waiting till the process
                // is finished
                if (timeout <= 0) continue;

                // otherwise, check if time's up
                long time = System.currentTimeMillis() - start;
                if (time > limit) {
                    logger.warn("Time out, the command is cancelled: "
                            + command);
                    process.destroy();
                    exitValue = -1;
                    return "Time out, the command is cancelled.";
                }
            } catch (InterruptedException ex) {
                // do nothing, keep looping
                continue;
            }
        }
    }

    public static String printArray(String[] array) {
        StringBuffer sb = new StringBuffer();
        sb.append("{\"");
        for (String s : array) {
            sb.append(s);
            sb.append("\", \"");
        }
        sb.delete(sb.length() - 3, sb.length());
        sb.append("}");
        return sb.toString();
    }

    public static String printArray(String[][] array) {
        StringBuffer sb = new StringBuffer();
        for (String[] parts : array) {
            sb.append(printArray(parts));
            sb.append(newline);
        }
        return sb.toString();
    }
    

    public static String[] tokenize(String line) {
        Pattern pattern = Pattern.compile("\\b[\\w\\.]+\\b");
        Matcher match = pattern.matcher(line);
        List<String> tokens = new ArrayList<String>();
        while (match.find()) {
            String token = line.substring(match.start(), match.end());
            tokens.add(token);
        }
        String[] sArray = new String[tokens.size()];
        tokens.toArray(sArray);
        return sArray;
    }

}
