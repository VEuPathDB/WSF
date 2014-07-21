package org.gusdb.wsf.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wsf.common.WsfRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jerric
 * 
 */
public class PluginRequest implements WsfRequest {

  private String projectId;
  private Map<String, String> params;
  private List<String> orderedColumns;
  private Map<String, String> context = new HashMap<String, String>();

  public PluginRequest() {
    this.params = new HashMap<String, String>();
    this.orderedColumns = new ArrayList<String>();
    this.context = new HashMap<String, String>();
  }

  public PluginRequest(PluginRequest request) {
    this.projectId = request.getProjectId();
    this.params = new HashMap<>(request.getParams());
    this.orderedColumns = new ArrayList<>(Arrays.asList(request.getOrderedColumns()));
    this.context = new HashMap<>(request.getContext());
  }

  public PluginRequest(String jsonString) throws PluginModelException {
    try {
      parseJSON(new JSONObject(jsonString));
    }
    catch (JSONException ex) {
      throw new PluginModelException(ex);
    }
  }

  public int getChecksum() {
    String content = toString();
    int checksum = 0;
    for (int i = 0; i < content.length(); i++) {
      checksum ^= content.charAt(i);
    }
    return checksum;
  }

  protected JSONObject getJSON() throws JSONException {
    JSONObject jsRequest = new JSONObject();
    jsRequest.put("project", getProjectId());

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
    return jsRequest;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    try {
      return getJSON().toString();
    }
    catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }

  protected void parseJSON(JSONObject jsRequest) throws JSONException {
    if (jsRequest.has("project"))
      setProjectId(jsRequest.getString("project"));

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

  /**
   * @return the projectId
   */
  @Override
  public String getProjectId() {
    return projectId;
  }

  /**
   * @param projectId
   *          the projectId to set
   */
  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  /**
   * @return the params
   */
  @Override
  public Map<String, String> getParams() {
    return new HashMap<String, String>(params);
  }

  /**
   * @param params
   *          the params to set
   */
  public void setParams(Map<String, String> params) {
    this.params = new HashMap<String, String>(params);
  }

  public void putParam(String name, String value) {
    this.params.put(name, value);
  }

  /**
   * @return the orderedColumns
   */
  @Override
  public String[] getOrderedColumns() {
    String[] array = new String[orderedColumns.size()];
    orderedColumns.toArray(array);
    return array;
  }

  /**
   * @return a map of ordered columns, where the key is the column name, and the value is the zero-based order
   *         of that column.
   */
  @Override
  public Map<String, Integer> getColumnMap() {
    Map<String, Integer> map = new HashMap<>();
    for (int i = 0; i < orderedColumns.size(); i++) {
      map.put(orderedColumns.get(i), i);
    }
    return map;
  }

  /**
   * @param orderedColumns
   *          the orderedColumns to set
   */
  public void setOrderedColumns(String[] orderedColumns) {
    this.orderedColumns = new ArrayList<String>(orderedColumns.length);
    for (String column : orderedColumns) {
      this.orderedColumns.add(column);
    }
  }

  /**
   * The context can be used to hold additional information, such as user id, calling query name, etc, which
   * can be used by plugins.
   * 
   * @return the context
   */
  @Override
  public Map<String, String> getContext() {
    return new HashMap<String, String>(context);
  }

  /**
   * @param context
   *          the context to set
   */
  public void setContext(Map<String, String> context) {
    this.context = new HashMap<String, String>(context);
  }

}