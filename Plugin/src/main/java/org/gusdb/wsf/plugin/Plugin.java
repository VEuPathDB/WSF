package org.gusdb.wsf.plugin;

/**
 * @author Jerric
 * @created Feb 10, 2006
 */
public interface Plugin {

  /**
   * Invoke a plugin, using the parameters in the request, and save the result into response.
   * 
   * @param request
   * @param response
   * @return the signal set by the plugin
   * @throws PluginModelException
   * @throws PluginUserException
   */
  public int invoke(PluginRequest request, PluginResponse response) throws PluginModelException,
      PluginUserException;

  /**
   * The Plugin needs to provide a list of required parameter names; the base class will use this template
   * method in the input validation process.
   * 
   * @return returns an array the names of the required parameters
   */
  public String[] getRequiredParameterNames();

  /**
   * The Plugin needs to provides a list of the columns expected in the result; the base class will use this
   * template method in the input validation process.
   * 
   * @return returns an array the columns expected in the result
   */
  public String[] getColumns();

  /**
   * Initialize the plugin singleton.
   * 
   * @param context
   *          a map of the objects fetched from servlet context, using the context keys.
   * @throws PluginModelException
   */
  public void initialize() throws PluginModelException;

  /**
   * Validate the parameters passed by the service. This validation confirms that the service (the wdk model)
   * has parameter options that agree with this plugin's API.
   */
  public void validateParameters(PluginRequest request) throws PluginModelException, PluginUserException;

}
