package org.gusdb.wsf;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.gusdb.wsf.client.MockResponseListener;
import org.gusdb.wsf.client.WsfClient;
import org.gusdb.wsf.client.WsfClientBuilder;
import org.gusdb.wsf.plugin.MockPlugin;
import org.gusdb.wsf.plugin.MockPluginResponse;
import org.gusdb.wsf.plugin.Plugin;
import org.gusdb.wsf.plugin.PluginRequest;
import org.gusdb.wsf.plugin.WsfException;
import org.gusdb.wsf.plugin.WsfPluginException;
import org.gusdb.wsf.plugin.WsfUserException;
import org.gusdb.wsf.service.WsfRequest;
import org.gusdb.wsf.service.WsfService;
import org.junit.Assert;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

public class MockPluginTest {
  
  private static final String MOCK_PROJECT = "MockProject";
  private static final String TEST_SERVER = "http://localhost/";
  private static final int TEST_PORT = 9998;

  public static WsfRequest createRequest(int rowCount, int attachmentCount) {
    Map<String, String> params = new HashMap<>();
    params.put(MockPlugin.PARAM_ROW_SIZE, Integer.toString(rowCount));
    params.put(MockPlugin.PARAM_ATTACHMENT_SIZE, Integer.toString(attachmentCount));

    WsfRequest request = new WsfRequest();
    request.setProjectId(MOCK_PROJECT);
    request.setPluginClass(MockPlugin.class.getName());
    request.setParams(params);
    request.setOrderedColumns(MockPlugin.COLUMNS);
    request.setContext(new HashMap<String, String>());

    return request;
  }

  private final Random random = new Random();

  @Test
  public void testPlugin() throws WsfException {
    int rowCount = random.nextInt(1000) + 10;
    int attachmentCount = random.nextInt(1000);
    PluginRequest request = createRequest(rowCount, attachmentCount);
    MockPluginResponse response = new MockPluginResponse();
    Plugin plugin = new MockPlugin();
    int signal = plugin.invoke(request, response);
    Assert.assertEquals(MockPlugin.SIGNAL, signal);
    Assert.assertEquals(rowCount, response.getRowCount());
    Assert.assertEquals(attachmentCount, response.getAttachmentCount());
  }

  @Test
  public void testService() throws IOException, ClassNotFoundException, WsfPluginException, WsfUserException {
    int rowCount = random.nextInt(1000) + 10;
    int attachmentCount = random.nextInt(1000);
    WsfRequest request = createRequest(rowCount, attachmentCount);
    WsfService service = new WsfService();
    Response response = service.invoke(request.toString());
    Assert.assertNotNull(response);

    // read from stream
    // StreamingOutput output = (StreamingOutput)response.getEntity();
    // OutputStream outStream = output.;
    // ObjectInputStream objectStream = null;
    // // use mock response to validate the result.
    // MockPluginResponse mockResponse = new MockPluginResponse();
    // try {
    // objectStream = new ObjectInputStream(inStream);
    // while (true) {
    // Object object = objectStream.readObject();
    // if (object instanceof ResponseStatus) {
    // ResponseStatus status = (ResponseStatus) object;
    // Assert.assertEquals(MockPlugin.SIGNAL, status.getSignal());
    // break;
    // }
    // else if (object instanceof ResponseRow) {
    // ResponseRow row = (ResponseRow) object;
    // mockResponse.addRow(row.getRow());
    // }
    // else if (object instanceof ResponseAttachment) {
    // ResponseAttachment attachment = (ResponseAttachment) object;
    // mockResponse.addAttachment(attachment.getKey(), attachment.getContent());
    // }
    // else if (object instanceof ResponseMessage) {
    // ResponseMessage message = (ResponseMessage) object;
    // mockResponse.setMessage(message.getMessage());
    // }
    // else {
    // Assert.assertTrue("unknown object type: " + object.getClass().getName(), false);
    // }
    // }
    // Assert.assertEquals(rowCount, mockResponse.getRowCount());
    // Assert.assertEquals(attachmentCount, mockResponse.getAttachmentCount());
    // }
    // finally {
    // if (objectStream != null)
    // objectStream.close();
    // response.close();
    // }
  }

  @Test
  public void testClientLocal() throws WsfException {
    int rowCount = random.nextInt(1000) + 10;
    int attachmentCount = random.nextInt(1000);
    WsfRequest request = createRequest(rowCount, attachmentCount);

    MockResponseListener listener = new MockResponseListener();
    WsfClient client = WsfClientBuilder.newClient(listener);
    int signal = client.invoke(request);

    Assert.assertEquals(MockPlugin.SIGNAL, signal);
    Assert.assertEquals(rowCount, listener.getRowCount());
    Assert.assertEquals(attachmentCount, listener.getAttachmentCount());
  }

  @Test
  public void testClientRemote() throws WsfException {
    // start up a http server
    URI url = UriBuilder.fromUri(TEST_SERVER).port(TEST_PORT).build();
    ResourceConfig config = new ResourceConfig(WsfService.class);
    HttpServer server = JdkHttpServerFactory.createHttpServer(url, config);
    
    int rowCount = random.nextInt(1000) + 10;
    int attachmentCount = random.nextInt(1000);
    WsfRequest request = createRequest(rowCount, attachmentCount);

    MockResponseListener listener = new MockResponseListener();
    WsfClient client = WsfClientBuilder.newClient(listener, url);
    int signal = client.invoke(request);

    Assert.assertEquals(MockPlugin.SIGNAL, signal);
    Assert.assertEquals(rowCount, listener.getRowCount());
    Assert.assertEquals(attachmentCount, listener.getAttachmentCount());
    
    server.stop(0);
  }
}
