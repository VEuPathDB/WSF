package org.gusdb.wsf.client;


public interface WsfClient {
  
  void setResponseListener(WsfResponseListener listener);

  int invoke(ClientRequest request) throws ClientModelException, ClientUserException;

}
