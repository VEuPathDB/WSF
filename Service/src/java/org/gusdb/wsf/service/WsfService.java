/**
 * 
 */
package org.gusdb.wsf.service;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Client requests to run a plugin by providing the complete class name of
     * the plugin, and the service will invoke the plugin and return the result
     * to the client in tabular format.
     * 
     * @param pluginClassName
     * @param paramValues
     *            an array of "param=value" pairs. The param and value and
     *            separated by the first "="
     * @param cols
     * @return
     * @throws WsfServiceException
     */
    public WsfResponse invoke(String pluginClassName, String invokeKey, String[] paramValues,
            String[] columns) throws WsfServiceException {
        int resultSize = 0;
        long start = System.currentTimeMillis();
        logger.info("Invoking: " + pluginClassName + ", invokeKey: " + invokeKey);

        Map<String, String> params = convertParams(paramValues);
        try {
            // use reflection to load the plugin object
            logger.debug("Loading object " + pluginClassName);
            Class pluginClass = Class.forName(pluginClassName);
            IWsfPlugin plugin = (IWsfPlugin) pluginClass.newInstance();
            plugin.setLogger(Logger.getLogger(pluginClass));

            // invoke the plugin
            logger.debug("Invoking Plugin " + pluginClassName);
            String[][] result = plugin.invoke(invokeKey, params, columns);
            resultSize = result.length;
            String message = plugin.getMessage();

            // prepare the response message
            WsfResponse response = new WsfResponse();
            response.setMessage(message);
            response.setResults(result);

            return response;
        } catch (WsfServiceException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex);
            throw new WsfServiceException(ex);
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
