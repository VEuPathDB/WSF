package org.gusdb.wdk.model.jspwrap;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.TableField;

/**
 * A wrapper on a {@link RecordClass} that provides simplified access for 
 * consumption by a view
 */ 
public class RecordClassBean {

    RecordClass recordClass;

    public RecordClassBean(RecordClass recordClass) {
	this.recordClass = recordClass;
    }

    public String getFullName() {
	return recordClass.getFullName();
    }

    public String getType() {
	return recordClass.getType();
    }

    /**
     * @return Map of fieldName --> {@link org.gusdb.wdk.model.FieldI}
     */
    public Map<String, AttributeFieldBean> getAttributeFields() {
        AttributeField[] fields = recordClass.getAttributeFields();
        Map<String, AttributeFieldBean> fieldBeans = 
            new HashMap<String, AttributeFieldBean>(fields.length);
        for (AttributeField field : fields) {
            fieldBeans.put(field.getName(), new AttributeFieldBean(field));
        }
        return fieldBeans;
    }

    /**
     * @return Map of fieldName --> {@link org.gusdb.wdk.model.FieldI}
     */
    public Map<String, TableFieldBean> getTableFields() {
        TableField[] fields = recordClass.getTableFields();
        Map<String, TableFieldBean> fieldBeans = 
            new HashMap<String, TableFieldBean>(fields.length);
        for (TableField field : fields) {
            fieldBeans.put(field.getName(), new TableFieldBean(field));
        }
        return fieldBeans;
    }

    /**
     * used by the controller
     */
    public RecordBean makeRecord () {
	return new RecordBean(recordClass.makeRecordInstance());
    }

    public QuestionBean[] getQuestions(){

	Question questions[] = recordClass.getQuestions();
	QuestionBean[] questionBeans = new QuestionBean[questions.length];
	for (int i = 0; i < questions.length; i++){
	    questionBeans[i] = new QuestionBean(questions[i]);
	}
	return questionBeans;
    }
}