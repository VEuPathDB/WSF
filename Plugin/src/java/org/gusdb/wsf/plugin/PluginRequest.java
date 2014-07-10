package org.gusdb.wsf.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginRequest {

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
    this.projectId = request.projectId;
    this.params = new HashMap<>(request.params);
    this.orderedColumns = new ArrayList<>(request.orderedColumns);
    this.context = new HashMap<>(request.context);
  }

  /**
   * @return the projectId
   */
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
  public String[] getOrderedColumns() {
    String[] array = new String[orderedColumns.size()];
    orderedColumns.toArray(array);
    return array;
  }

  /**
   * @return a map of ordered columns, where the key is the column name, and the value is the zero-based order
   *         of that column.
   */
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
