package org.gusdb.wsf.plugin.mock;

import java.util.Map;
import java.util.Random;

import org.gusdb.wsf.plugin.AbstractPlugin;
import org.gusdb.wsf.plugin.PluginModelException;
import org.gusdb.wsf.plugin.PluginRequest;
import org.gusdb.wsf.plugin.PluginResponse;
import org.gusdb.wsf.plugin.PluginUserException;

public class MockPlugin extends AbstractPlugin {

  public static final String PARAM_ROW_SIZE = "row.size";
  public static final String PARAM_ATTACHMENT_SIZE = "attachment.size";

  public static final String[] COLUMNS = { "col1", "col2", "col3" };
  public static final String[] REQUIRED_PARAMS = { PARAM_ROW_SIZE, PARAM_ATTACHMENT_SIZE };

  public static final int SIGNAL = 10;
  public static final String MESSAGE_PREFIX = "mock-message-";
  public static final String ATTACHMENT_KEY_PREFIX = "mock-att-key-";
  public static final String ATTACHMENT_VALUE_PREFIX = "mock-att-val-";

  private final Random random = new Random();

  public MockPlugin() {}

  public MockPlugin(String propertyFile) {
    super(propertyFile);
  }

  @Override
  public String[] getRequiredParameterNames() {
    return REQUIRED_PARAMS;
  }

  @Override
  public String[] getColumns() {
    return COLUMNS;
  }

  @Override
  public void validateParameters(PluginRequest request) throws PluginUserException {
    Map<String, String> params = request.getParams();

    // make sure the row size is greater than 0
    long rowSize = Long.valueOf(params.get(PARAM_ROW_SIZE));
    if (rowSize <= 0)
      throw new PluginUserException("The " + PARAM_ROW_SIZE + " param must be a number greater than 0");

    // make sure the attachment size is greater than, or equal to, 0;
    int attachmentSize = Integer.valueOf(params.get(PARAM_ATTACHMENT_SIZE));
    if (attachmentSize < 0)
      throw new PluginUserException("The " + PARAM_ATTACHMENT_SIZE +
          " param must be a number greater than, or equal to, 0");
  }

  @Override
  protected int execute(PluginRequest request, PluginResponse response) throws PluginModelException,
      PluginUserException {
    // get params
    Map<String, String> params = request.getParams();
    long rowSize = Long.valueOf(params.get(PARAM_ROW_SIZE));
    int attachmentSize = Integer.valueOf(params.get(PARAM_ATTACHMENT_SIZE));

    // get ordered columns
    String[] columns = request.getOrderedColumns();
    // generate rows
    for (int i = 0; i < rowSize; i++) {
      String[] row = new String[columns.length];
      for (int c = 0; c < columns.length; c++) {
        row[c] = columns[c] + "-" + random.nextInt();
      }
      response.addRow(row);
    }

    // generate attachments
    for (int i = 0; i < attachmentSize; i++) {
      String key = ATTACHMENT_KEY_PREFIX + random.nextInt();
      String value = ATTACHMENT_VALUE_PREFIX + random.nextInt();
      response.addAttachment(key, value);
    }

    // generate message
    response.setMessage(MESSAGE_PREFIX + random.nextInt());

    return SIGNAL;
  }

}
