/**
 * 
 */
package org.gusdb.wsf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WsfPortalService {

    private static final boolean DEBUG = false;

    private static Map<String, String> plugins = new HashMap<String, String>();

    static {
        // load configurations
        Properties prop = new Properties();
        String root = System.getProperty("webservice.home");
        File rootDir;
        if (root == null) {
            root = System.getProperty("catalina.home");
            rootDir = new File(root, "webapps/axis");
        } else rootDir = new File(root);
        File configFile = new File(rootDir, "WEB-INF/wsfService-config.xml");
        try {
            // TEST
            if (DEBUG)
                System.out.println("WSF Service config file: "
                        + configFile.getAbsolutePath());

            prop.loadFromXML(new FileInputStream(configFile));

            for (Object key : prop.keySet()) {
                String propName = (String) key;
                // if (propName.endsWith("Plugin")) {
                // load all the entries
                String propValue = prop.getProperty(propName);
                plugins.put(propName, propValue);
                // }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 
     */
    public WsfPortalService() {}

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.service.IProcessor#invoke(java.lang.String[],
     *      java.lang.String[], java.lang.String[])
     */
    public String[][] invoke(String pluginName, String[] params,
            String[] values, String[] cols) throws WsfServiceException {
        // use reflection to load the parser
        if (DEBUG)
            System.out.println("WSFPortalService.invoke(" + pluginName + ")");

        // check if the processor valid
        if (!plugins.containsKey(pluginName)) {
            if (DEBUG) System.err.println("Unknown plugin: " + pluginName);
            throw new WsfServiceException("Unknown plugin: " + pluginName);
        }

        try {
            String processClass = plugins.get(pluginName);
            IWsfPlugin processor = loadPlugin(processClass);
            // invoke process and obtain result
            return processor.invoke(params, values, cols);
        } catch (ClassNotFoundException ex) {
            if (DEBUG) ex.printStackTrace();
            // if (DEBUG) System.out.println(ex);
            throw new WsfServiceException(ex);
        } catch (InstantiationException ex) {
            if (DEBUG) ex.printStackTrace();
            // if (DEBUG) System.out.println(ex);
            throw new WsfServiceException(ex);
        } catch (IllegalAccessException ex) {
            if (DEBUG) ex.printStackTrace();
            // if (DEBUG) System.out.println(ex);
            throw new WsfServiceException(ex);
        }
    }

    private IWsfPlugin loadPlugin(String className)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        Class pluginClass = Class.forName(className);
        IWsfPlugin plugin = (IWsfPlugin) pluginClass.newInstance();
        return plugin;
    }
}
