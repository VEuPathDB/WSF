/**
 * 
 */
package org.gusdb.wsf.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.gusdb.wsf.plugin.IWsfPlugin;
import org.gusdb.wsf.plugin.WsfServiceException;

/**
 * The WSF Web service entry point.
 * 
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WsfService {

    private static Logger logger = Logger.getLogger(WsfService.class);

    private static Map<String, IWsfPlugin> plugins = new LinkedHashMap<String, IWsfPlugin>();

    /**
     * Client requests to run a plugin by providing the complete class name of
     * the plugin, and the service will invoke the plugin and return the result
     * to the client in tabular format.
     * 
     * @param pluginClassName
     * @param queryName
     *          the name of the query that invokes the plugin
     * @param paramValues
     *            an array of "param=value" pairs. The param and value and
     *            separated by the first "="
     * @param cols
     * @return
     * @throws WsfServiceException
     */
    public WsfResponse invoke(String pluginClassName, String queryName,
            String[] paramValues, String[] columns) throws ServiceException {
        int resultSize = 0;
        long start = System.currentTimeMillis();
        logger.info("Invoking: " + pluginClassName + ", queryName: "
                + queryName);

        Map<String, String> params = convertParams(paramValues);
        try {
            // use reflection to load the plugin object
            logger.debug("Loading object " + pluginClassName);

            // check if the plugin has been cached
            IWsfPlugin plugin;
            if (plugins.containsKey(pluginClassName)) {
                plugin = plugins.get(pluginClassName);
            } else {
                logger.info("Creating plugin " + pluginClassName);
                Class pluginClass = Class.forName(pluginClassName);
                plugin = (IWsfPlugin) pluginClass.newInstance();
                plugin.setLogger(Logger.getLogger(pluginClass));
                plugins.put(pluginClassName, plugin);
            }

            // invoke the plugin
            logger.debug("Invoking Plugin " + pluginClassName);
            String[][] result = plugin.invoke(queryName, params, columns);
            resultSize = result.length;
            String message = plugin.getMessage();

            // prepare the response message
            WsfResponse response = new WsfResponse();
            response.setMessage(message);
            response.setResults(result);

            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex);
            throw new ServiceException(ex);
        } finally {
            long end = System.currentTimeMillis();
            logger.info("WSF finshed in: " + ((end - start) / 1000.0)
                    + " seconds with " + resultSize + " results.");
        }
    }

    private Map<String, String> convertParams(String[] paramValues) {
        Map<String, String> params = new HashMap<String, String>(
                paramValues.length);
        for (String paramValue : paramValues) {
            int pos = paramValue.indexOf('=');
            if (pos < 0) params.put(paramValue.trim(), "");
            else {
                String param = paramValue.substring(0, pos).trim();
                String value = paramValue.substring(pos + 1).trim();
                params.put(param, value);
            }
        }
        return params;
    }
}
