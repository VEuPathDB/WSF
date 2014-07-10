package org.gusdb.wsf;

import org.gusdb.wsf.plugin.MockPlugin;
import org.gusdb.wsf.plugin.WsfPluginException;
import org.gusdb.wsf.plugin.WsfUserException;
import org.junit.Assert;

public class MockResponseHandler {

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

  protected void validateRow(String[] row) throws WsfPluginException, WsfUserException {
    for (int i = 0; i < row.length; i++) {
      String column = MockPlugin.COLUMNS[i];
      Assert.assertTrue(row[i].startsWith(column + "-"));
    }
    rowCount++;
  }

  protected void validateAttachment(String key, String content) throws WsfPluginException, WsfUserException {
    Assert.assertTrue(key.startsWith(MockPlugin.ATTACHMENT_KEY_PREFIX));
    Assert.assertTrue(content.startsWith(MockPlugin.ATTACHMENT_VALUE_PREFIX));
    attachmentCount++;
  }

  protected void validateMessage(String message) throws WsfPluginException, WsfUserException {
    Assert.assertTrue(message.startsWith(MockPlugin.MESSAGE_PREFIX));
  }

}
