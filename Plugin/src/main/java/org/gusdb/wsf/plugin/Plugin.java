package org.gusdb.wsf.plugin;

import javax.ws.rs.core.HttpHeaders;

import org.gusdb.fgputil.Tuples.TwoTuple;

/**
 * @author Jerric
 * @since Feb 10, 2006
 */
public interface Plugin {

  /**
   * Invoke a plugin, using the parameters in the request, and save the result
   * into response.
   *
   * @return the signal set by the plugin
   */
  int invoke(PluginRequest request, PluginResponse response)
  throws PluginModelException, PluginUserException, DelayedResultException;

  /**
   * The Plugin needs to provide a list of required parameter names; the base
   * class will use this template method in the input validation process.
   *
   * @return returns an array the names of the required parameters
   */
  String[] getRequiredParameterNames();

  /**
   * The Plugin needs to provides a list of the columns expected in the result;
   * the base class will use this template method in the input validation
   * process.
   * @param request 
   *
   * @return returns an array the columns expected in the result
   * @throws PluginModelException 
   */
  String[] getColumns(PluginRequest request) throws PluginModelException;

  /**
   * Initialize the plugin instance.
   *
   * @param request that spurred creation of this plugin
   */
  void initialize(PluginRequest request) throws PluginModelException;

  /**
   * Validate the parameters passed by the service. This validation confirms
   * that the service (the wdk model) has parameter options that agree with this
   * plugin's API.
   */
  void validateParameters(PluginRequest request)
  throws PluginModelException, PluginUserException;

  /**
   * Returns a bearer token authorization header given a bearer token value
   *
   * @param bearerTokenValue value of the bearer token
   * @return header pair for use with authenticated HTTP services
   */
  static TwoTuple<String, String> getServiceAuthorizationHeader(String bearerTokenValue) {
    return new TwoTuple<>(HttpHeaders.AUTHORIZATION, "Bearer " + bearerTokenValue);
  }
}
