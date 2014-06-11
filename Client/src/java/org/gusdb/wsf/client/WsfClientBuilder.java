package org.gusdb.wsf.client;

import java.net.URI;

public final class WsfClientBuilder {

  public static WsfClient newClient(WsfResponseListener listener) {
    WsfClient client = new WsfLocalClient();
    client.setResponseListener(listener);
    return client;
  }

  public static WsfClient newClient(WsfResponseListener listener, URI serviceURI) {
    WsfClient client = new WsfRemoteClient(serviceURI);
    client.setResponseListener(listener);
    return client;
  }
  
  
}
