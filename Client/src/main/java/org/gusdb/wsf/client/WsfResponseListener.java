package org.gusdb.wsf.client;

import org.gusdb.wsf.plugin.WsfException;


public interface WsfResponseListener {

  void onRowReceived(String[] row) throws WsfException;
  
  void onAttachmentReceived(String key, String content) throws WsfException;
  
  void onMessageReceived(String message) throws WsfException;
}
