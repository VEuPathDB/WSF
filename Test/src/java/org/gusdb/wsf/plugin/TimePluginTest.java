/**
 * 
 */
package org.gusdb.wsf.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class TimePluginTest {

  private final Random random;
  private final Plugin plugin;
  private final PluginRequest request;
  private final Map<String, String> params;
  private final File storageDir;

  public TimePluginTest() {
    random = new Random();
    plugin = new TimePlugin();

    params = new HashMap<String, String>();
    params.put(TimePlugin.REQUIRED_PARAMS[0], "true");
    params.put(TimePlugin.REQUIRED_PARAMS[1], "true");

    request = new PluginRequest();
    request.setProjectId("TestDB");
    request.setParams(params);
    request.setOrderedColumns(TimePlugin.COLUMNS);

    String temp = System.getProperty("java.io.tmpdir", "/tmp");
    storageDir = new File(temp + "wsf-test/");
    if (!storageDir.exists() || !storageDir.isDirectory()) storageDir.mkdirs();
  }

  @Test
  public void testGetDate() throws WsfServiceException {
    // prepare params
    params.put(TimePlugin.REQUIRED_PARAMS[1], "false");
    request.setParams(params);

    int invokeId = random.nextInt();
    PluginResponse response = new PluginResponse(storageDir, invokeId);
    plugin.invoke(request, response);

    Map<String, String> resultMap = assertResponse(response, invokeId, 3);

    Calendar now = Calendar.getInstance();
    int year = Integer.parseInt(resultMap.get(TimePlugin.YEAR));
    assertEquals(TimePlugin.YEAR, now.get(Calendar.YEAR), year);
    int month = Integer.parseInt(resultMap.get(TimePlugin.MONTH));
    assertEquals(TimePlugin.MONTH, now.get(Calendar.MONTH), month - 1);
    int day = Integer.parseInt(resultMap.get(TimePlugin.DAY));
    assertEquals(TimePlugin.DAY, now.get(Calendar.DAY_OF_MONTH), day);
  }

  @Test
  public void testGetTime() throws WsfServiceException {
    // prepare params
    params.put(TimePlugin.REQUIRED_PARAMS[0], "false");
    request.setParams(params);

    int invokeId = random.nextInt();
    PluginResponse response = new PluginResponse(storageDir, invokeId);
    plugin.invoke(request, response);

    Map<String, String> resultMap = assertResponse(response, invokeId, 4);

    String timeZone = resultMap.get(TimePlugin.TIME_ZONE);
    Calendar now = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
    int hour = Integer.parseInt(resultMap.get(TimePlugin.HOUR));
    int diff = now.get(Calendar.HOUR_OF_DAY) - hour;
    assertTrue(TimePlugin.HOUR + ": " + hour, diff == 0 || diff == 1);
    // difficult to verify minutes and second, since they may have changed
    assertEquals(TimePlugin.TIME_ZONE, now.getTimeZone().getID(), timeZone);
  }

  @Test
  public void testGetWeekDay() throws WsfServiceException {
    params.put(TimePlugin.REQUIRED_PARAMS[0], "false");
    params.put(TimePlugin.REQUIRED_PARAMS[1], "false");
    params.put(TimePlugin.OPTIONAL_PARAMS[0], "true");
    request.setParams(params);

    int invokeId = random.nextInt();
    PluginResponse response = new PluginResponse(storageDir, invokeId);
    plugin.invoke(request, response);

    Map<String, String> resultMap = assertResponse(response, invokeId, 1);

    assertTrue("has weekday field", resultMap.containsKey(TimePlugin.WEEK_DAY));

    Calendar now = Calendar.getInstance();
    int weekDay = Integer.parseInt(resultMap.get(TimePlugin.WEEK_DAY));
    assertEquals(TimePlugin.WEEK_DAY, now.get(Calendar.DAY_OF_WEEK) - 1,
        weekDay);
  }

  @Test
  public void testGetAll() throws WsfServiceException {
    params.put(TimePlugin.OPTIONAL_PARAMS[0], "true");
    params.put("-u", null);
    request.setParams(params);

    int invokeId = random.nextInt();
    PluginResponse response = new PluginResponse(storageDir, invokeId);
    plugin.invoke(request, response);

    assertResponse(response, invokeId, 8);
  }

  @Test(expected = WsfServiceException.class)
  public void testMissingParam() throws WsfServiceException {
    params.remove(TimePlugin.REQUIRED_PARAMS[0]);
    request.setParams(params);

    int invokeId = random.nextInt();
    PluginResponse response = new PluginResponse(storageDir, invokeId);
    plugin.invoke(request, response);

    assertResponse(response, invokeId, 8);
  }

  @Test(expected = WsfServiceException.class)
  public void testInvalidParam() throws WsfServiceException {
    params.put(TimePlugin.REQUIRED_PARAMS[1], "bad");
    request.setParams(params);

    int invokeId = random.nextInt();
    PluginResponse response = new PluginResponse(storageDir, invokeId);
    plugin.invoke(request, response);

    assertResponse(response, invokeId, 8);
  }

  @Test(expected = WsfServiceException.class)
  public void testInvalidColumn() throws WsfServiceException {
    request.setOrderedColumns(new String[] { TimePlugin.COLUMNS[0],
        TimePlugin.COLUMNS[1], "Bad" });

    int invokeId = random.nextInt();
    PluginResponse response = new PluginResponse(storageDir, invokeId);
    plugin.invoke(request, response);

    assertResponse(response, invokeId, 8);
  }

  @Test(expected = WsfServiceException.class)
  public void testMissingColumn() throws WsfServiceException {
    request.setOrderedColumns(new String[] { TimePlugin.COLUMNS[0] });

    int invokeId = random.nextInt();
    PluginResponse response = new PluginResponse(storageDir, invokeId);
    plugin.invoke(request, response);

    assertResponse(response, invokeId, 8);
  }

  private Map<String, String> assertResponse(PluginResponse response,
      int invokeId, int resultSize) throws WsfServiceException {
    Map<String, String> resultMap = buildResultMap(response, 0);
    String message = response.getMessage();
    int signal = response.getSignal();
    assertEquals("result size", 3, resultMap.size());
    assertTrue("message", message.trim().length() > 0);
    assertEquals("signal", 0, signal);
    assertEquals("invoke id", invokeId, response.getInvokeId());
    assertEquals("page count", TimePlugin.PAGE_COUNT, response.getPageCount());

    Map<String, String> attachments = response.getAttachments();
    assertEquals("signal", 2, attachments.size());
    assertEquals(TimePlugin.ATTACHMENT_DATE, message,
        attachments.get(TimePlugin.ATTACHMENT_DATE));
    assertEquals(TimePlugin.ATTACHMENT_SIGNAL, signal,
        Integer.parseInt(attachments.get(TimePlugin.ATTACHMENT_SIGNAL)));

    return resultMap;
  }

  private Map<String, String> buildResultMap(PluginResponse response,
      int fieldIndex) throws WsfServiceException {
    String[][] result = response.getPage(0);
    Map<String, String> resultMap = new HashMap<String, String>();
    for (int i = 0; i < result.length; i++) {
      String field = result[i][fieldIndex];
      String value = result[i][1 - fieldIndex];
      resultMap.put(field, value);
    }
    return resultMap;
  }
}
