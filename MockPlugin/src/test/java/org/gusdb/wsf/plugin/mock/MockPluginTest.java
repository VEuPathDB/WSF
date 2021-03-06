package org.gusdb.wsf.plugin.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.gusdb.wsf.plugin.DelayedResultException;
import org.gusdb.wsf.plugin.Plugin;
import org.gusdb.wsf.plugin.PluginModelException;
import org.gusdb.wsf.plugin.PluginRequest;
import org.gusdb.wsf.plugin.PluginUserException;
import org.junit.Assert;
import org.junit.Test;

public class MockPluginTest {

  private static final String MOCK_PROJECT = "MockProject";

  public static PluginRequest createRequest(int rowCount, int attachmentCount) {
    Map<String, String> params = new HashMap<>();
    params.put(MockPlugin.PARAM_ROW_SIZE, Integer.toString(rowCount));
    params.put(MockPlugin.PARAM_ATTACHMENT_SIZE, Integer.toString(attachmentCount));

    PluginRequest request = new PluginRequest();
    request.setProjectId(MOCK_PROJECT);
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
    PluginRequest request = createRequest(rowCount, attachmentCount);
    MockPluginResponse response = new MockPluginResponse();
    Plugin plugin = new MockPlugin();
    int signal = plugin.invoke(request, response);
    Assert.assertEquals(MockPlugin.SIGNAL, signal);
    Assert.assertEquals(rowCount, response.getRowCount());
    Assert.assertEquals(attachmentCount, response.getAttachmentCount());
  }
}
