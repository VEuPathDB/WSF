/**
 * 
 */
package org.gusdb.wsf.client;

import org.gusdb.wsf.plugin.mock.MockPluginResponse;

/**
 * @author Jerric
 *
 */
public class MockResponseListener extends MockPluginResponse implements WsfResponseListener {

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wsf.client.WsfResponseListener#onRowReceived(java.lang.String[])
   */
  @Override
  public void onRowReceived(String[] row) {
    validateRow(row);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wsf.client.WsfResponseListener#onAttachmentReceived(java.lang.String, java.lang.String)
   */
  @Override
  public void onAttachmentReceived(String key, String content) {
    validateAttachment(key, content);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wsf.client.WsfResponseListener#onMessageReceived(java.lang.String)
   */
  @Override
  public void onMessageReceived(String message) {
    validateMessage(message);
  }

}
