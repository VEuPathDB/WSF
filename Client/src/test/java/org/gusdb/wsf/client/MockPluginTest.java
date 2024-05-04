package org.gusdb.wsf.client;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jakarta.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.gusdb.wsf.plugin.DelayedResultException;
import org.gusdb.wsf.plugin.mock.MockPlugin;
import org.gusdb.wsf.service.WsfService;
import org.junit.Assert;
import org.junit.Test;

public class MockPluginTest {

  private static final String MOCK_PROJECT = "MockProject";
  private static final String TEST_SERVER = "http://localhost/";
  private static final int TEST_PORT = 9998;

  public static ClientRequest createRequest(int rowCount, int attachmentCount) {
    Map<String, String> params = new HashMap<>();
    params.put(MockPlugin.PARAM_ROW_SIZE, Integer.toString(rowCount));
    params.put(MockPlugin.PARAM_ATTACHMENT_SIZE, Integer.toString(attachmentCount));

    ClientRequest request = new ClientRequest();
    request.setProjectId(MOCK_PROJECT);
    request.setPluginClass(MockPlugin.class.getName());
    request.setParams(params);
    request.setOrderedColumns(MockPlugin.COLUMNS);
    request.setContext(new HashMap<String, String>());

    return request;
  }

  private final Random random = new Random();

  @Test
  public void testClientLocal() throws ClientModelException, ClientUserException, DelayedResultException {
    int rowCount = random.nextInt(1000) + 10;
    int attachmentCount = random.nextInt(1000);
    ClientRequest request = createRequest(rowCount, attachmentCount);

    MockResponseListener listener = new MockResponseListener();
    WsfClient client = new WsfClientFactoryImpl().newClient(listener);
    int signal = client.invoke(request);

    Assert.assertEquals(MockPlugin.SIGNAL, signal);
    Assert.assertEquals(rowCount, listener.getRowCount());
    Assert.assertEquals(attachmentCount, listener.getAttachmentCount());
  }

  @Test
  public void testClientRemote() throws Exception {
    // start up a http server
    URI url = UriBuilder.fromUri(TEST_SERVER).port(TEST_PORT).build();
    ResourceConfig config = new ResourceConfig(WsfService.class);
    Server server = JettyHttpContainerFactory.createServer(url, config);

    int rowCount = random.nextInt(1000) + 10;
    int attachmentCount = random.nextInt(1000);
    ClientRequest request = createRequest(rowCount, attachmentCount);

    MockResponseListener listener = new MockResponseListener();
    WsfClient client = new WsfClientFactoryImpl().newClient(listener, url);
    int signal = client.invoke(request);

    Assert.assertEquals(MockPlugin.SIGNAL, signal);
    Assert.assertEquals(rowCount, listener.getRowCount());
    Assert.assertEquals(attachmentCount, listener.getAttachmentCount());

    server.stop();
  }
}
