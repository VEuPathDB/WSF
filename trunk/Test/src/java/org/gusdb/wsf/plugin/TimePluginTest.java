/**
 * 
 */
package org.gusdb.wsf.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class TimePluginTest {

    private Plugin plugin;
    private WsfRequest request;
    private Map<String, String> params;

    public TimePluginTest() {
        plugin = new TimePlugin();
        request = new WsfRequest();
        request.setProjectId("TestDB");

        params = new HashMap<String, String>();
        params.put(TimePlugin.REQUIRED_PARAMS[0], "true");
        params.put(TimePlugin.REQUIRED_PARAMS[1], "true");
        request.setParams(params);

        request.setOrderedColumns(TimePlugin.COLUMNS);
    }

    @Test
    public void testGetDate() throws WsfServiceException {
        // prepare params
        params.put(TimePlugin.REQUIRED_PARAMS[1], "false");
        request.setParams(params);

        WsfResponse result = plugin.execute(request);
        Map<String, String> resultMap = buildResultMap(result.getResult(), 0);
        String message = result.getMessage();
        assertEquals("result size", 3, resultMap.size());
        assertTrue("message", message.trim().length() > 0);
        assertEquals("signal", 0, result.getSignal());

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

        String[][] result = plugin.execute(request).getResult();
        Map<String, String> resultMap = buildResultMap(result, 0);
        assertEquals("result size", 4, result.length);

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
        // prepare params

        String[][] result = plugin.execute(request).getResult();
        assertEquals("result size", 1, result.length);
        assertEquals("weekday field", TimePlugin.WEEK_DAY, result[0][1]);

        Calendar now = Calendar.getInstance();
        int weekDay = Integer.parseInt(result[0][0]);
        assertEquals(TimePlugin.WEEK_DAY, now.get(Calendar.DAY_OF_WEEK) - 1,
                weekDay);
    }

    @Test
    public void testGetAll() throws WsfServiceException {
        params.put(TimePlugin.OPTIONAL_PARAMS[0], "true");
        params.put("-u", null);
        request.setParams(params);
        String[][] result = plugin.execute(request).getResult();
        assertEquals("result size", 8, result.length);

    }

    @Test(expected = WsfServiceException.class)
    public void testMissingParam() throws WsfServiceException {
        params.remove(TimePlugin.REQUIRED_PARAMS[0]);
        request.setParams(params);
        plugin.execute(request);
    }

    @Test(expected = WsfServiceException.class)
    public void testInvalidParam() throws WsfServiceException {
        params.put(TimePlugin.REQUIRED_PARAMS[1], "bad");
        request.setParams(params);
        plugin.execute(request);
    }

    @Test(expected = WsfServiceException.class)
    public void testInvalidColumn() throws WsfServiceException {
        request.setOrderedColumns(new String[] { TimePlugin.COLUMNS[0],
                TimePlugin.COLUMNS[1], "Bad" });
        plugin.execute(request);
    }

    @Test(expected = WsfServiceException.class)
    public void testMissingColumn() throws WsfServiceException {
        request.setOrderedColumns(new String[] { TimePlugin.COLUMNS[0] });
        plugin.execute(request);
    }

    private Map<String, String> buildResultMap(String[][] result, int fieldIndex) {
        Map<String, String> resultMap = new HashMap<String, String>();
        for (int i = 0; i < result.length; i++) {
            String field = result[i][fieldIndex];
            String value = result[i][1 - fieldIndex];
            resultMap.put(field, value);
        }
        return resultMap;
    }
}
