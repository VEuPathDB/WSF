package org.gusdb.wsf.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WsfRequest {

    private String projectId;
    private Map<String, String> params = new HashMap<String, String>();
    private List<String> orderedColumns = new ArrayList<String>();
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
    public String getParam(String key) {
        return params.get(key);
    }

    public String[] getParamKeys() {
        String[] keys = new String[params.size()];
        params.keySet().toArray(keys);
        return keys;
    }

    /**
     * @param params
     *            the params to set
     */
    public void setParam(String key, String value) {
        this.params.put(key, value);
    }
    
    public void removeParam(String key) {
        this.params.remove(key);
    }

    /**
     * @return the orderedColumns
     */
    public String[] getOrderedColumns() {
        String[] columns = new String[orderedColumns.size()];
        orderedColumns.toArray(columns);
        return columns;
    }

    /**
     * @param orderedColumns
     *            the orderedColumns to set
     */
    public void addOrderedColumn(String column) {
        this.orderedColumns.add(column);
    }
    
    public void clearOrderedColumns() {
        this.orderedColumns.clear();
    }

    /**
     * @return the context
     */
    public String getContext(String key) {
        return context.get(key);
    }

    public String[] getContextKeys() {
        String[] keys = new String[context.size()];
        context.keySet().toArray(keys);
        return keys;
    }

    /**
     * @param context
     *            the context to set
     */
    public void setContext(String key, String context) {
        this.context.put(key, context);
    }

}
