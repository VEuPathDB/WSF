/**
 * 
 */
package org.gusdb.wsf.client;

import org.gusdb.wsf.common.WsfException;
import org.gusdb.wsf.common.WsfRequest;
import org.gusdb.wsf.plugin.PluginExecutor;
import org.gusdb.wsf.plugin.PluginResponse;

/**
 * @author Jerric
 *
 */
public class WsfLocalClient implements WsfClient, PluginResponse {

  private WsfResponseListener listener;

  protected WsfLocalClient() {}

  @Override
  public void addRow(String[] row) throws WsfException {
    listener.onRowReceived(row);
  }

  @Override
  public void addAttachment(String key, String attachment) throws WsfException {
    listener.onAttachmentReceived(key, attachment);
  }

  @Override
  public void setMessage(String message) throws WsfException {
    listener.onMessageReceived(message);
  }

  @Override
  public void setResponseListener(WsfResponseListener listener) {
    this.listener = listener;
  }

  @Override
  public int invoke(WsfRequest request) throws WsfException {
    PluginExecutor executor = new PluginExecutor();
    String pluginClassName = request.getPluginClass();
    return executor.execute(pluginClassName, request, this);
  }
}
