package org.gusdb.wsf.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WsfRequest {

    private String projectId;
    private Map<String, String> params;
    private List<String> orderedColumns;
    private Map<String, String> context = new HashMap<String, String>();

    /**
     * @return the projectId
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * @param projectId
     *            the projectId to set
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
     *            the params to set
     */
    public void setParams(Map<String, String> params) {
        this.params = new HashMap<String, String>(params);
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
     * @param orderedColumns
     *            the orderedColumns to set
     */
    public void setOrderedColumns(String[] orderedColumns) {
        this.orderedColumns = new ArrayList<String>(orderedColumns.length);
        for (String column : orderedColumns) {
            this.orderedColumns.add(column);
        }
    }

    /**
     * @return the context
     */
    public Map<String, String> getContext() {
        return new HashMap<String, String>(context);
    }

    /**
     * @param context
     *            the context to set
     */
    public void setContext(Map<String, String> context) {
        this.context = new HashMap<String, String>(context);
    }
}
