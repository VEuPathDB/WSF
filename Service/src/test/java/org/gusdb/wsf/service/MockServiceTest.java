package org.gusdb.wsf.service;

import org.gusdb.wsf.plugin.PluginModelException;
import org.gusdb.wsf.plugin.PluginUserException;
import org.gusdb.wsf.plugin.mock.MockPlugin;
import org.gusdb.wsf.plugin.mock.MockPluginResponse;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MockServiceTest {

  private static final String MOCK_PROJECT = "MockProject";

  public static ServiceRequest createRequest(int rowCount, int attachmentCount) {
    var params = new HashMap<String, String>();
    params.put(MockPlugin.PARAM_ROW_SIZE, Integer.toString(rowCount));
    params.put(MockPlugin.PARAM_ATTACHMENT_SIZE, Integer.toString(attachmentCount));

    var request = new ServiceRequest();
    request.setProjectId(MOCK_PROJECT);
    request.setPluginClass(MockPlugin.class.getName());
    request.setParams(params);
    request.setOrderedColumns(MockPlugin.COLUMNS);
    request.setContext(new HashMap<>());

    return request;
  }

  private final Random random = new Random();

  @Test
  public void testPlugin() throws PluginModelException, PluginUserException {
    int rowCount = random.nextInt(1000) + 10;
    int attachmentCount = random.nextInt(1000);
    var request = createRequest(rowCount, attachmentCount);
    var response = new MockPluginResponse();
    var plugin = new MockPlugin();
    int signal = plugin.invoke(request, response);
    assertEquals(MockPlugin.SIGNAL, signal);
    assertEquals(rowCount, response.getRowCount());
    assertEquals(attachmentCount, response.getAttachmentCount());
  }

  @Test
  public void testService() {
    int rowCount = random.nextInt(1000) + 10;
    int attachmentCount = random.nextInt(1000);
    var request = createRequest(rowCount, attachmentCount);
    var service = new WsfService();
    var response = service.invoke(request.toString());
    assertNotNull(response);
  }
}
