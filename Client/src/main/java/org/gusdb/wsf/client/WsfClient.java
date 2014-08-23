package org.gusdb.wsf.client;

import org.gusdb.wsf.common.WsfException;
import org.gusdb.wsf.common.WsfRequest;

public interface WsfClient {
  
  void setResponseListener(WsfResponseListener listener);

  int invoke(WsfRequest request) throws WsfException;

}
