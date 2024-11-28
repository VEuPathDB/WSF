package org.gusdb.wsf.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jakarta.ws.rs.core.Response;

import org.gusdb.wsf.common.WsfRequest;
import org.gusdb.wsf.plugin.DelayedResultException;
import org.gusdb.wsf.plugin.Plugin;
import org.gusdb.wsf.plugin.PluginModelException;
import org.gusdb.wsf.plugin.PluginUserException;
import org.gusdb.wsf.plugin.mock.MockPlugin;
import org.gusdb.wsf.plugin.mock.MockPluginResponse;
import org.junit.Assert;
import org.junit.Test;

public class MockServiceTest {
  
  private static final String MOCK_PROJECT = "MockProject";

  public static ServiceRequest createRequest(int rowCount, int attachmentCount) {
    Map<String, String> params = new HashMap<>();
    params.put(MockPlugin.PARAM_ROW_SIZE, Integer.toString(rowCount));
    params.put(MockPlugin.PARAM_ATTACHMENT_SIZE, Integer.toString(attachmentCount));

    ServiceRequest request = new ServiceRequest();
    request.setProjectId(MOCK_PROJECT);
    request.setPluginClass(MockPlugin.class.getName());
    request.setParams(params);
    request.setOrderedColumns(MockPlugin.COLUMNS);
    request.setContext(new HashMap<String, String>());

    return request;
  }

  private final Random random = new Random();

  @Test
  public void testPlugin() throws PluginModelException, PluginUserException, DelayedResultException {
    int rowCount = random.nextInt(1000) + 10;
    int attachmentCount = random.nextInt(1000);
    ServiceRequest request = createRequest(rowCount, attachmentCount);
    MockPluginResponse response = new MockPluginResponse();
    Plugin plugin = new MockPlugin();
    int signal = plugin.invoke(request, response);
    Assert.assertEquals(MockPlugin.SIGNAL, signal);
    Assert.assertEquals(rowCount, response.getRowCount());
    Assert.assertEquals(attachmentCount, response.getAttachmentCount());
  }

  @Test
  public void testService() {
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
    // Object object = objectStream.readUnshared();
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
}
