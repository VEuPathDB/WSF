/**
 * 
 */
package org.gusdb.wsf.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wsf.plugin.PluginRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jerric
 * 
 */
public class WsfRequest extends PluginRequest {

  private String pluginClass;

  public WsfRequest() {}

  public WsfRequest(PluginRequest pluginRequest) {
    super(pluginRequest);
  }

  public WsfRequest(String jsonString) throws WsfServiceException {
    try {
      parseJSON(jsonString);
    }
    catch (JSONException ex) {
      throw new WsfServiceException(ex);
    }
  }

  /**
   * the full class name of the WSF plugin. The service will instantiate a plugin instance from this class
   * name, and invoke it.
   * 
   * @return the pluginClass
   */
  public String getPluginClass() {
    return pluginClass;
  }

  /**
   * @param pluginClass
   *          the pluginClass to set
   */
  public void setPluginClass(String pluginClass) {
    this.pluginClass = pluginClass;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    JSONObject jsRequest = new JSONObject();
    try {
      jsRequest.put("project", getProjectId());
      jsRequest.put("plugin", pluginClass);

      // output columns
      JSONArray jsColumns = new JSONArray();
      for (String column : getOrderedColumns()) {
        jsColumns.put(column);
      }
      jsRequest.put("ordered-columns", jsColumns);

      // output params
      JSONObject jsParams = new JSONObject();
      Map<String, String> params = getParams();
      for (String paramName : params.keySet()) {
        jsParams.put(paramName, params.get(paramName));
      }
      jsRequest.put("parameters", jsParams);

      // output request context
      JSONObject jsContext = new JSONObject();
      Map<String, String> context = getContext();
      for (String contextKey : context.keySet()) {
        jsContext.put(contextKey, context.get(contextKey));
      }
      jsRequest.put("context", jsContext);
    }
    catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
    return jsRequest.toString();
  }

  private void parseJSON(String jsonString) throws JSONException {
    JSONObject jsRequest = new JSONObject(jsonString);
    setProjectId(jsRequest.getString("project"));
    this.pluginClass = jsRequest.getString("plugin");

    JSONArray jsColumns = jsRequest.getJSONArray("ordered-columns");
    List<String> columns = new ArrayList<>();
    for (int i = 0; i < jsColumns.length(); i++) {
      columns.add(jsColumns.getString(i));
    }
    setOrderedColumns(columns.toArray(new String[0]));

    Map<String, String> params = new LinkedHashMap<>();
    addToMap(params, jsRequest.getJSONObject("parameters"));
    setParams(params);

    Map<String, String> context = new LinkedHashMap<>();
    addToMap(context, jsRequest.getJSONObject("context"));
    setContext(context);
  }

  private static void addToMap(Map<String, String> map, JSONObject newValues) throws JSONException {
    @SuppressWarnings("unchecked")
    Iterator<String> keys = newValues.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      map.put(key, newValues.getString(key));
    }
  }

}
