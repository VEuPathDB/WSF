package org.gusdb.wsf.common;

import java.util.Map;

public interface WsfRequest {

  public static final String PARAM_REQUEST = "request";

  /**
   * @return the projectId
   */
  public String getProjectId();

  /**
   * @return the params
   */
  public Map<String, String> getParams();

  /**
   * @return the orderedColumns
   */
  public String[] getOrderedColumns();

  /**
   * @return a map of ordered columns, where the key is the column name, and the value is the zero-based order
   *         of that column.
   */
  public Map<String, Integer> getColumnMap();

  /**
   * The context can be used to hold additional information, such as user id, calling query name, etc, which
   * can be used by plugins.
   * 
   * @return the context
   */
  public Map<String, String> getContext();

}
