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
     * @param paramValues an array of "param=value" pairs. The param and value
     *        and separated by the first "="
     * @param cols
     * @return
     * @throws WsfServiceException
     */
    public String[][] invoke(String pluginClassName, String[] paramValues,
            String[] cols) throws WsfServiceException {
        logger.debug(pluginClassName);

        Map<String, String> params = convertParams(paramValues);
        try {
            // use reflection to load the plugin object
            logger.info("Loading object " + pluginClassName);
            Class pluginClass = Class.forName(pluginClassName);
            IWsfPlugin plugin = (IWsfPlugin) pluginClass.newInstance();
            plugin.setLogger(Logger.getLogger(pluginClass));

            // invoke the plugin
            logger.info("Invoking Plugin " + pluginClassName);
            return plugin.invoke(params, cols);
        } catch (WsfServiceException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw new WsfServiceException(ex);
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
