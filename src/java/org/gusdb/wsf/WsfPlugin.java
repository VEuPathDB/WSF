/**
 * 
 */
package org.gusdb.wsf;

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

import org.apache.log4j.Logger;

/**
 * @author Jerric
 * @created Feb 9, 2006
 */
public abstract class WsfPlugin implements IWsfPlugin {

    private Logger logger;
    private Properties properties;

    protected int exitValue;

    protected abstract String[] getRequiredParameters();

    protected abstract String[] getColumns();

    protected abstract String[][] execute(Map<String, String> params,
            String[] cols);

    /**
     * With-property-file case
     * 
     * @param logger
     * @param propertyFile
     * @throws InvalidPropertiesFormatException
     * @throws IOException
     */
    public WsfPlugin(Logger logger, String propertyFile)
            throws InvalidPropertiesFormatException, IOException {
        this(logger);
        exitValue = 0;
        // load the properties
        loadPropertyFile(propertyFile);
    }

    /**
     * Initialize an empty property file
     * 
     * @param logger
     */
    public WsfPlugin(Logger logger) {
        this.logger = logger;
        properties = new Properties();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.IWsfPlugin#invoke(java.util.Map, java.lang.String[])
     */
    public final String[][] invoke(Map<String, String> params, String[] cols)
            throws WsfServiceException {
        // validate the input
        if (!validateInput(params, cols))
            throw new WsfServiceException("Input validation failed.");

        // execute the main function, and obtain result
        return execute(params, cols);
    }

    protected boolean validateInput(Map<String, String> params, String[] cols) {
        logger.info("WsfPlugin.validateInput()");
        String[] reqParams = getRequiredParameters();
        String[] reqColumns = getColumns();

        // validate parameters
        for (String param : reqParams) {
            if (!params.containsKey(param)) {
                logger.error("The required parameter is missing: " + param);
                return false;
            }
        }

        // validate columns
        Set<String> colSet = new HashSet<String>(cols.length);
        for (String col : cols) {
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
        for (String col : cols) {
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
        File configFile = new File(rootDir, "WEB-INF/" + propertyFile);
        InputStream in = new FileInputStream(configFile);
        properties.loadFromXML(in);
    }

    protected String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    protected String invokeCommand(String command, long timeout)
            throws IOException {
        // invoke the command
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                process.getInputStream()));

        long start = System.currentTimeMillis();
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
                String newline = System.getProperty("line.separator");
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    sb.append(newline);
                }
                return sb.toString();
            } catch (IllegalThreadStateException ex) {
                // if the timeout is set to <= 0, keep waiting till the process
                // is finished
                if (timeout <= 0) continue;

                // otherwise, check if time's up
                long time = System.currentTimeMillis() - start;
                if (time > timeout) {
                    logger.warn("Time out, the command is cancelled: "
                            + command);
                    process.destroy();
                    exitValue = -1;
                    return "";
                }
            } catch (InterruptedException ex) {
                // do nothing, keep looping
                continue;
            }
        }
    }
    
    protected String printArray(String[] array) {
        StringBuffer sb = new StringBuffer();
        sb.append("{'");
        for (String s : array) {
            sb.append(s);
            sb.append("', '");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("'}");
        return sb.toString();
    }

}
