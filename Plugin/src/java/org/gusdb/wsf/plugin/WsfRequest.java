package org.gusdb.wsf.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public String[] getParamKeys() {
        String[] keys = new String[params.size()];
        params.keySet().toArray(keys);
        return keys;
    }

    public String getParamsJSON() throws WsfServiceException {
        JSONArray array = new JSONArray();
        try {
            for (String name : params.keySet()) {
                JSONObject pair = new JSONObject();
                pair.put("name", name);
                pair.put("value", params.get(name));
                array.put(pair);
            }
            return array.toString();
        } catch (JSONException ex) {
            throw new WsfServiceException(ex);
        }
    }

    public void setParamsJSON(String json) throws WsfServiceException {
        try {
            JSONArray array = new JSONArray(json);
            params.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject pair = array.getJSONObject(i);
                String name = pair.getString("name");
                String value = pair.getString("value");
                params.put(name, value);
            }
        } catch (JSONException ex) {
            throw new WsfServiceException(ex);
        }
    }

    /**
     * @return the orderedColumns
     */
    public String[] getOrderedColumns() {
        String[] columns = new String[orderedColumns.size()];
        orderedColumns.toArray(columns);
        return columns;
    }

    public void setOrderedColumns(String[] columns) {
        this.orderedColumns.clear();
        for (String column : columns) {
            orderedColumns.add(column);
        }
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

    public String getContextsJSON() throws WsfServiceException {
        JSONArray array = new JSONArray();
        try {
            for (String name : context.keySet()) {
                JSONObject pair = new JSONObject();
                pair.put("name", name);
                pair.put("value", context.get(name));
                array.put(pair);
            }
            return array.toString();
        } catch (JSONException ex) {
            throw new WsfServiceException(ex);
        }
    }

    public void setContextsJSON(String json) throws WsfServiceException {
        try {
            JSONArray array = new JSONArray(json);
            context.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject pair = array.getJSONObject(i);
                String name = pair.getString("name");
                String value = pair.getString("value");
                context.put(name, value);
            }
        } catch (JSONException ex) {
            throw new WsfServiceException(ex);
        }
    }

}
