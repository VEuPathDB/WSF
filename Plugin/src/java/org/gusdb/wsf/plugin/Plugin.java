package org.gusdb.wsf.plugin;

import java.util.Map;

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
   * @throws WsfPluginException
   */
  public void invoke(PluginRequest request, PluginResponse response)
      throws WsfPluginException;

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
   * @return An array of keys the plugin expects to be provided by the context (eg, the servlet).  Each key will provide an associated object.  An example is a WDK_MODEL_KEY that provides a handle on the wdk model.
   */
  public String[] getContextKeys();

  /**
   * Initialize the plugin singleton.
   * @param context
   *          a map of the objects fetched from servlet context, using the
   *          context keys.
   * @throws WsfPluginException
   */
  public void initialize(Map<String, Object> context)
      throws WsfPluginException;

  /**
   * Validate the parameters passed by the service.  This validation confirms that the service (the wdk model)
   * has parameter options that agree with this plugin's API.
   */
  public void validateParameters(PluginRequest request) throws WsfPluginException;

}
