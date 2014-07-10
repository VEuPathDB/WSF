package org.gusdb.wsf.client;

import org.gusdb.wsf.plugin.WsfException;
import org.gusdb.wsf.service.WsfRequest;

public interface WsfClient {
  
  void setResponseListener(WsfResponseListener listener);

  int invoke(WsfRequest request) throws WsfException;

}
