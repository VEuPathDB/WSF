/**
 * 
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class AnswerFilterInstance extends WdkModelBase {

    private String name;
    private String displayName;
    private boolean isDefault;
    private boolean isBooleanExpansion;

    private List<WdkModelText> descriptionList = new ArrayList<WdkModelText>();
    private String description;

    private List<WdkModelText> paramValueList = new ArrayList<WdkModelText>();
    private Map<String, String> paramValueMap = new LinkedHashMap<String, String>();

    private RecordClass recordClass;
    private SqlQuery filterQuery;
    private AnswerParam answerParam;

    private WdkModel wdkModel;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the isDefault
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * @param isDefault
     *            the isDefault to set
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * @return the isBooleanExpansion
     */
    public boolean isBooleanExpansion() {
        return isBooleanExpansion;
    }

    /**
     * @param isBooleanExpansion
     *            the isBooleanExpansion to set
     */
    public void setBooleanExpansion(boolean isBooleanExpansion) {
        this.isBooleanExpansion = isBooleanExpansion;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    public void addDescription(WdkModelText description) {
        this.descriptionList.add(description);
    }

    public void addParamValue(WdkModelText param) {
        this.paramValueList.add(param);
    }

    /**
     * @return the recordClass
     */
    public RecordClass getRecordClass() {
        return recordClass;
    }

    /**
     * @param recordClass
     *            the recordClass to set
     */
    void setRecordClass(RecordClass recordClass) {
        this.recordClass = recordClass;
    }

    /**
     * @return the filterQuery
     */
    public SqlQuery getFilterQuery() {
        return filterQuery;
    }

    /**
     * @param filterQuery
     *            the filterQuery to set
     */
    void setFilterQuery(SqlQuery filterQuery) {
        this.filterQuery = filterQuery;
    }

    /**
     * @return the answerParam
     */
    public AnswerParam getAnswerParam() {
        return answerParam;
    }

    /**
     * @param answerParam
     *            the answerParam to set
     */
    void setAnswerParam(AnswerParam answerParam) {
        this.answerParam = answerParam;
    }

    public Map<String, Object> getParamValueMap() {
        return new LinkedHashMap<String, Object>(paramValueMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude the descriptions
        for (WdkModelText text : descriptionList) {
            if (text.include(projectId)) {
                text.excludeResources(projectId);
                if (description != null)
                    throw new WdkModelException("Description of "
                            + "answerFilterInstance '" + name + "' in "
                            + recordClass.getFullName()
                            + " is included more than once.");
                this.description = text.getText();
            }
        }
        descriptionList = null;

        // exclude the param values
        for (WdkModelText param : paramValueList) {
            if (param.include(projectId)) {
                param.excludeResources(projectId);
                String paramName = param.getName();
                String paramValue = param.getText().trim();

                if (paramValueMap.containsKey(paramName))
                    throw new WdkModelException("The param [" + paramName
                            + "] for answerFilterInstance [" + name
                            + "] of type " + recordClass.getFullName()
                            + "  is included more than once.");
                paramValueMap.put(paramName, paramValue);
            }
        }
        paramValueList = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
     * .WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        if (resolved) return;

        this.wdkModel = wdkModel;

        // make sure the params provides match with those in the filter query
        Map<String, Param> params = filterQuery.getParamMap();
        for (String paramName : paramValueMap.keySet()) {
            if (!params.containsKey(paramName))
                throw new WdkModelException("The param [" + paramName
                        + "] declared in answerFilterInstance [" + name
                        + "] of type " + recordClass.getFullName()
                        + " does not exist in the" + " filter query ["
                        + filterQuery.getFullName() + "]");
        }
        // make sure the required param is defined
        for (String paramName : params.keySet()) {
            if (answerParam.getName().equals(paramName)) continue;
            if (!paramValueMap.containsKey(paramName))
                throw new WdkModelException("The required param value of ["
                        + paramName + "] is not assigned to filter ["
                        + getName() + "]");

            // validate the paramValue
            Param param = params.get(paramName);
            String paramValue = paramValueMap.get(paramName);
            param.validateValue(paramValue);
        }

        resolved = true;
    }

    public ResultList getResults(AnswerValue answerValue) throws SQLException,
            NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException {
        // use only the id query sql as input
        String sql = answerValue.getIdsQueryInstance().getSql();
        sql = applyFilter(sql);
        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql);
        try {
            return new SqlResultList(resultSet);
        } catch (SQLException ex) {
            resultSet.close();
            throw ex;
        }
    }

    public String applyFilter(String sql) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        Map<String, Param> params = filterQuery.getParamMap();

        String filterSql = filterQuery.getSql();
        // replace the answer param
        String answerName = answerParam.getName();
        filterSql = filterSql.replaceAll("\\$\\$" + answerName + "\\$\\$", "("
                + sql + ")");

        // replace the rest of the params; the answer param has been replaced
        // and will be ignored here.
        for (Param param : params.values()) {
            if (param.getFullName().equals(answerParam.getFullName()))
                continue;

            String external = paramValueMap.get(param.getName());
            filterSql = param.replaceSql(filterSql, external);
        }
        return filterSql;
    }
}