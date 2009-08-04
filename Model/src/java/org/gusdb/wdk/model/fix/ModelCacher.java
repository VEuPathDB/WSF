/**
 * 
 */
package org.gusdb.wdk.model.fix;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wsf.util.BaseCLI;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class ModelCacher extends BaseCLI {

    private static final Logger logger = Logger.getLogger(ModelCacher.class);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String cmdName = System.getProperty("cmdName");
        ModelCacher cacher = new ModelCacher(cmdName);
        try {
            cacher.invoke(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            logger.info("model cacher done.");
            System.exit(0);
        }
    }

    /**
     * @param command
     * @param description
     */
    public ModelCacher(String command) {
        super((command == null) ? command : "wdkCacheModel",
                "store model information into database");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#declareOptions()
     */
    @Override
    protected void declareOptions() {
        addSingleValueOption(ARG_PROJECT_ID, true, null, "A comma-separated"
                + " list of ProjectIds, which should match the directory name"
                + " under $GUS_HOME, where model-config.xml is stored.");
        addNonValueOption("create", false, "create the cache tables to store "
                + "model definition. It affects all projects.");
        addNonValueOption("drop", false, "drop the cache table for storing "
                + "model definition. It affects all projects.");
        addNonValueOption("expand", false, "load the model definition into "
                + "the cache tables.");
        addGroup(true, "create", "drop", "expand");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#execute()
     */
    @Override
    protected void execute() throws Exception {
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        String strProject = (String) getOptionValue(ARG_PROJECT_ID);
        String[] projects = strProject.split(",");

        boolean create = (Boolean) getOptionValue("create");
        boolean drop = (Boolean) getOptionValue("drop");
        boolean expand = (Boolean) getOptionValue("expand");

        if (create) {
            String projectId = projects[0];
            WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
            createTables(wdkModel);
            logger.info("created model cache tables");
        } else if (drop) {
            String projectId = projects[0];
            WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
            dropTables(wdkModel);
            logger.info("dropped model cache tables");
        } else if (expand) {
            for (String projectId : projects) {
                logger.info("Expanding model for project " + projectId);
                WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
                expand(wdkModel);
            }
        } else {
            logger.error("No valid operation specified");
            throw new WdkModelException("No valid operation specified");
        }
    }

    public void expand(WdkModel wdkModel) throws SQLException,
            NoSuchAlgorithmException, JSONException, WdkModelException,
            WdkUserException {
        // need to reset the cache first
        wdkModel.getResultFactory().getCacheFactory().resetCache();
        
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        String projectId = wdkModel.getProjectId();
        deleteCache(dataSource, projectId);

        PreparedStatement psQuestion = null, psParam = null, psEnum = null;
        try {
            String sql = "INSERT INTO wdk_questions "
                    + "(question_id, question_name, project_id, "
                    + " question_checksum, query_checksum, record_class) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            psQuestion = SqlUtils.getPreparedStatement(dataSource, sql);

            sql = "INSERT INTO wdk_params (param_id, question_id, param_name, "
                    + "param_type) VALUES (?, ?, ?, ?)";
            psParam = SqlUtils.getPreparedStatement(dataSource, sql);

            sql = "INSERT INTO wdk_enum_params (param_id, param_value) "
                    + "VALUES (?, ?)";
            psEnum = SqlUtils.getPreparedStatement(dataSource, sql);

            for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
                for (Question question : questionSet.getQuestions()) {
                    saveQuestion(question, psQuestion, psParam, psEnum);
                }
            }
        } finally {
            SqlUtils.closeStatement(psQuestion);
            SqlUtils.closeStatement(psParam);
            SqlUtils.closeStatement(psEnum);
        }
    }

    public void dropTables(WdkModel wdkModel) {
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        String[] sequences = new String[] { "wdk_questions_pkseq",
                "wdk_params_pkseq" };
        for (String sequence : sequences) {
            try {
                SqlUtils.executeUpdate(dataSource, "DROP SEQUENCE " + sequence);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        String[] tables = new String[] { "wdk_enum_params", "wdk_params",
                "wdk_questions" };
        for (String table : tables) {
            try {
                SqlUtils.executeUpdate(dataSource, "DROP TABLE " + table);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void createTables(WdkModel wdkModel) throws SQLException,
            WdkModelException {
        DBPlatform platform = wdkModel.getUserPlatform();
        DataSource dataSource = platform.getDataSource();

        if (!platform.checkTableExists(null, "wdk_questions"))
            createQuestionTable(dataSource);
        if (!platform.checkTableExists(null, "wdk_params"))
            createParamTable(dataSource);
        if (!platform.checkTableExists(null, "wdk_enum_params"))
            createEnumParamTable(dataSource);
    }

    private void createQuestionTable(DataSource dataSource) throws SQLException {
        // create sequence
        String sql = "CREATE SEQUENCE wdk_questions_pkseq "
                + "INCREMENT BY 1 START WITH 1";
        SqlUtils.executeUpdate(dataSource, sql);

        // create table
        sql = "CREATE TABLE wdk_questions (question_id NUMBER(12) NOT NULL, "
                + "question_name VARCHAR(200) NOT NULL, "
                + "project_id VARCHAR(50) NOT NULL, "
                + "question_checksum  VARCHAR(40) NOT NULL, "
                + "query_checksum  VARCHAR(40) NOT NULL, "
                + "record_class  VARCHAR(200) NOT NULL, "
                + "CONSTRAINT wdk_questions_pk PRIMARY KEY (question_id), "
                + "CONSTRAINT wdk_questions_uq1 "
                + "  UNIQUE (project_id, question_name) )";
        SqlUtils.executeUpdate(dataSource, sql);

        // create index
        sql = "CREATE INDEX wdk_questions_idx01 "
                + "ON wdk_questions (question_checksum)";
        SqlUtils.executeUpdate(dataSource, sql);
    }

    private void createParamTable(DataSource dataSource) throws SQLException {
        // create sequence
        String sql = "CREATE SEQUENCE wdk_params_pkseq "
                + "INCREMENT BY 1 START WITH 1";
        SqlUtils.executeUpdate(dataSource, sql);

        // create table
        sql = "CREATE TABLE wdk_params ( "
                + "param_id NUMBER(12) NOT NULL, "
                + "question_id NUMBER(12) NOT NULL, "
                + "param_name VARCHAR(200) NOT NULL, "
                + "param_type  VARCHAR(200) NOT NULL, "
                + "CONSTRAINT wdk_params_pk PRIMARY KEY (param_id), "
                + "CONSTRAINT wdk_params_question_id_fk FOREIGN KEY (question_id) "
                + "  REFERENCES wdk_questions (question_id), "
                + "CONSTRAINT wdk_params_uq1 UNIQUE (question_id, param_name) )";
        SqlUtils.executeUpdate(dataSource, sql);

        // create index
        sql = "CREATE INDEX wdk_params_idx01 ON wdk_params (param_type)";
        SqlUtils.executeUpdate(dataSource, sql);
    }

    private void createEnumParamTable(DataSource dataSource)
            throws SQLException {
        // create table
        String sql = "CREATE TABLE wdk_enum_params ( "
                + "param_id NUMBER(12) NOT NULL, "
                + "param_value  VARCHAR(1000) NOT NULL, "
                + "CONSTRAINT wdk_enum_params_fk01 FOREIGN KEY (param_id) "
                + "   REFERENCES wdk_params (param_id) )";
        SqlUtils.executeUpdate(dataSource, sql);

        // create index
        sql = "CREATE INDEX wdk_enum_params_idx01 "
                + "ON wdk_enum_params (param_id, param_value)";
        SqlUtils.executeUpdate(dataSource, sql);
    }

    private void deleteCache(DataSource dataSource, String projectId)
            throws SQLException {
        // delete enum_param values
        PreparedStatement psEnums = null, psParams = null, psQuestions = null;
        try {
            // delete enum params
            String sql = "DELETE FROM wdk_enum_params WHERE param_id IN "
                    + "(SELECT param_id FROM wdk_params p, wdk_questions q "
                    + " WHERE p.question_id = q.question_id AND project_id = ?)";
            psEnums = SqlUtils.getPreparedStatement(dataSource, sql);
            psEnums.setString(1, projectId);
            psEnums.executeUpdate();

            // delete params
            sql = "DELETE FROM wdk_params WHERE question_id IN "
                    + "(SELECT question_id FROM wdk_questions WHERE project_id = ?)";
            psParams = SqlUtils.getPreparedStatement(dataSource, sql);
            psParams.setString(1, projectId);
            psParams.executeUpdate();

            // delete questions
            sql = "DELETE FROM wdk_questions WHERE project_id = ?";
            psQuestions = SqlUtils.getPreparedStatement(dataSource, sql);
            psQuestions.setString(1, projectId);
            psQuestions.executeUpdate();
        } finally {
            SqlUtils.closeStatement(psEnums);
            SqlUtils.closeStatement(psParams);
            SqlUtils.closeStatement(psQuestions);
        }
    }

    private void saveQuestion(Question question, PreparedStatement psQuestion,
            PreparedStatement psParam, PreparedStatement psEnum)
            throws NoSuchAlgorithmException, JSONException, WdkModelException,
            SQLException, WdkUserException {
        WdkModel wdkModel = question.getWdkModel();
        DBPlatform platform = wdkModel.getUserPlatform();

        int questionId = platform.getNextId(null, "wdk_questions");
        psQuestion.setInt(1, questionId);
        psQuestion.setString(2, question.getFullName());
        psQuestion.setString(3, wdkModel.getProjectId());
        psQuestion.setString(4, question.getQuery().getChecksum(false));
        psQuestion.setString(5, question.getQuery().getChecksum(true));
        psQuestion.setString(6, question.getRecordClass().getFullName());
        psQuestion.executeUpdate();

        // save the params
        for (Param param : question.getParams()) {
            saveParam(wdkModel, param, questionId, psParam, psEnum);
        }
    }

    private void saveParam(WdkModel wdkModel, Param param, int questionId,
            PreparedStatement psParam, PreparedStatement psEnum)
            throws SQLException, WdkModelException, NoSuchAlgorithmException,
            JSONException, WdkUserException {
        DBPlatform platform = wdkModel.getUserPlatform();

        int paramId = platform.getNextId(null, "wdk_params");
        psParam.setInt(1, paramId);
        psParam.setInt(2, questionId);
        psParam.setString(3, param.getName());
        psParam.setString(4, param.getClass().getSimpleName());
        psParam.executeUpdate();

        if (param instanceof AbstractEnumParam)
            saveEnums((AbstractEnumParam) param, paramId, psEnum);
    }

    private void saveEnums(AbstractEnumParam param, int paramId,
            PreparedStatement psEnum) throws NoSuchAlgorithmException,
            WdkModelException, SQLException, JSONException, WdkUserException {
        for (String term : param.getVocab()) {
            psEnum.setInt(1, paramId);
            psEnum.setString(2, term);
            psEnum.addBatch();
        }
        psEnum.executeBatch();
    }
}