package org.gusdb.wsf.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WsfRequest {

    private String pluginClass;
    private String projectId;
    private Map<String, String> params = new HashMap<String, String>();
    private List<String> orderedColumns = new ArrayList<String>();
    private Map<String, String> context = new HashMap<String, String>();

    public WsfRequest() {}

    public WsfRequest(String jsonString) throws JSONException {
        parseJSON(jsonString);
    }

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
     * @return the pluginClass
     */
    public String getPluginClass() {
        return pluginClass;
    }

    /**
     * @param pluginClass
     *            the pluginClass to set
     */
    public void setPluginClass(String pluginClass) {
        this.pluginClass = pluginClass;
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        JSONObject jsRequest = new JSONObject();
        try {
            jsRequest.put("project", projectId);
            jsRequest.put("plugin", pluginClass);

            // output columns
            JSONArray jsColumns = new JSONArray();
            for (String column : orderedColumns) {
                jsColumns.put(column);
            }
            jsRequest.put("ordered-columns", jsColumns);

            // output params
            JSONObject jsParams = new JSONObject();
            for (String paramName : params.keySet()) {
                jsParams.put(paramName, params.get(paramName));
            }
            jsRequest.put("parameters", jsParams);

            // output request context
            JSONObject jsContext = new JSONObject();
            for (String contextKey : context.keySet()) {
                jsContext.put(contextKey, context.get(contextKey));
            }
            jsRequest.put("context", jsContext);
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
        return jsRequest.toString();
    }

    private void parseJSON(String jsonString) throws JSONException {
        JSONObject jsRequest = new JSONObject(jsonString);
        this.projectId = jsRequest.getString("project");
        this.pluginClass = jsRequest.getString("plugin");

        JSONArray jsColumns = jsRequest.getJSONArray("ordered-columns");
        for (int i = 0; i < jsColumns.length(); i++) {
            orderedColumns.add(jsColumns.getString(i));
        }

        JSONObject jsParams = jsRequest.getJSONObject("parameters");
        Iterator<String> keys = jsParams.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            params.put(key, jsParams.getString(key));
        }

        JSONObject jsContext = jsRequest.getJSONObject("context");
        keys = jsContext.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            context.put(key, jsContext.getString(key));
        }
    }
}
