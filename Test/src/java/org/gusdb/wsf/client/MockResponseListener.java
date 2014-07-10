/**
 * 
 */
package org.gusdb.wsf.client;

import org.gusdb.wsf.MockResponseHandler;
import org.gusdb.wsf.plugin.WsfPluginException;
import org.gusdb.wsf.plugin.WsfUserException;


/**
 * @author Jerric
 *
 */
public class MockResponseListener extends MockResponseHandler implements WsfResponseListener {

  /* (non-Javadoc)
   * @see org.gusdb.wsf.client.WsfResponseListener#onRowReceived(java.lang.String[])
   */
  @Override
  public void onRowReceived(String[] row) {
    try {
      validateRow(row);
    }
    catch (WsfPluginException | WsfUserException ex) {
      throw new RuntimeException(ex);
    }
  }

  /* (non-Javadoc)
   * @see org.gusdb.wsf.client.WsfResponseListener#onAttachmentReceived(java.lang.String, java.lang.String)
   */
  @Override
  public void onAttachmentReceived(String key, String content) {
    try {
    validateAttachment(key, content);
  }
  catch (WsfPluginException | WsfUserException ex) {
    throw new RuntimeException(ex);
  }
 }

  /* (non-Javadoc)
   * @see org.gusdb.wsf.client.WsfResponseListener#onMessageReceived(java.lang.String)
   */
  @Override
  public void onMessageReceived(String message) {
    try {
      validateMessage(message);
    }
    catch (WsfPluginException | WsfUserException ex) {
      throw new RuntimeException(ex);
    }
  }

}
