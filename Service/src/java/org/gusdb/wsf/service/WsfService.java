/**
 * 
 */
package org.gusdb.wsf.service;

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
     * @param params
     * @param cols
     * @return
     * @throws WsfServiceException
     */
    public String[][] invoke(String pluginClassName,
            Map<String, String> params, String[] cols)
            throws WsfServiceException {
        logger.debug(pluginClassName);

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
}
