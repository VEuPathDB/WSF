package org.gusdb.wsf.client;

import java.net.URI;

public interface WsfClientFactory {

  public WsfClient newClient(WsfResponseListener listener);

  public WsfClient newClient(WsfResponseListener listener, URI serviceURI);
}
