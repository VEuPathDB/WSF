package org.gusdb.wsf.plugin.mock;

import org.gusdb.wsf.plugin.PluginResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
  public void addRow(String[] row) {
    validateRow(row);
  }

  @Override
  public void addAttachment(String key, String content) {
    validateAttachment(key, content);
  }

  @Override
  public void setMessage(String message) {
    validateMessage(message);
  }


  protected void validateRow(String[] row) {
    for (int i = 0; i < row.length; i++) {
      var column = MockPlugin.COLUMNS[i];
      assertTrue(row[i].startsWith(column + "-"));
    }
    rowCount++;
  }

  protected void validateAttachment(String key, String content) {
    assertTrue(key.startsWith(MockPlugin.ATTACHMENT_KEY_PREFIX));
    assertTrue(content.startsWith(MockPlugin.ATTACHMENT_VALUE_PREFIX));
    attachmentCount++;
  }

  protected void validateMessage(String message) {
    assertTrue(message.startsWith(MockPlugin.MESSAGE_PREFIX));
  }

}
