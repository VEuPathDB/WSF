package org.gusdb.wsf.plugin;

import java.util.Map;

/**
 * 
 */

/**
 * @author Jerric
 * @created Feb 10, 2006
 */
public interface Plugin {

  public static final String CTX_CONFIG_PATH = "wsfConfigDir_param";

  /**
   * Invoke a plugin, using the parameters in the request, and save the result
   * into response.
   * 
   * @param request
   * @param response
   * @throws WsfServiceException
   */
  public void invoke(PluginRequest request, PluginResponse response)
      throws WsfServiceException;

  /**
   * The Plugin needs to provide a list of required parameter names; the base
   * class will use this template method in the input validation process.
   * 
   * @return returns an array the names of the required parameters
   */
  public String[] getRequiredParameterNames();

  /**
   * The Plugin needs to provides a list of the columns expected in the result;
   * the base class will use this template method in the input validation
   * process.
   * 
   * @return returns an array the columns expected in the result
   */
  public String[] getColumns();

  /**
   * @return an array of keys that a used in the servlet context.
   */
  public String[] getContextKeys();

  /**
   * the service will get the objects from the serlvet context using the the
   * context keys, and send the fetched objects as parameters.
   * 
   * @param context
   *          a map of the objects fetched from servlet context, using the
   *          context keys.
   * @throws WsfServiceException
   */
  public void initialize(Map<String, Object> context)
      throws WsfServiceException;

  public void validateParameters(PluginRequest request) throws WsfServiceException;

}
