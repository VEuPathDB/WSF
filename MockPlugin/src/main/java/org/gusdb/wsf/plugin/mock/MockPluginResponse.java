package org.gusdb.wsf.plugin.mock;

import org.gusdb.wsf.plugin.PluginModelException;
import org.gusdb.wsf.plugin.PluginResponse;
import org.gusdb.wsf.plugin.PluginUserException;
import org.junit.Assert;


public class MockPluginResponse implements PluginResponse {

  private int rowCount = 0;
  private int attachmentCount = 0;

  /**
   * @return the rowCount
   */
  public int getRowCount() {
    return rowCount;
  }

  /**
   * @return the attachmentCount
   */
  public int getAttachmentCount() {
    return attachmentCount;
  }

  @Override
  public void addRow(String[] row) throws PluginModelException, PluginUserException {
    validateRow(row);
  }

  @Override
  public void addAttachment(String key, String content) throws PluginModelException, PluginUserException {
    validateAttachment(key, content);
  }

  @Override
  public void setMessage(String message) throws PluginModelException, PluginUserException {
    validateMessage(message);
  }


  protected void validateRow(String[] row) {
    for (int i = 0; i < row.length; i++) {
      String column = MockPlugin.COLUMNS[i];
      Assert.assertTrue(row[i].startsWith(column + "-"));
    }
    rowCount++;
  }

  protected void validateAttachment(String key, String content) {
    Assert.assertTrue(key.startsWith(MockPlugin.ATTACHMENT_KEY_PREFIX));
    Assert.assertTrue(content.startsWith(MockPlugin.ATTACHMENT_VALUE_PREFIX));
    attachmentCount++;
  }

  protected void validateMessage(String message) {
    Assert.assertTrue(message.startsWith(MockPlugin.MESSAGE_PREFIX));
  }

}
