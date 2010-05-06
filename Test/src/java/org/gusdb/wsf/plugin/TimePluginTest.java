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

    public TimePluginTest() {
        plugin = new TimePlugin();
        request = new WsfRequest();
        request.setProjectId("TestDB");
        request.setParam(TimePlugin.REQUIRED_PARAMS[0], "true");
        request.setParam(TimePlugin.REQUIRED_PARAMS[1], "true");
        for (String column : TimePlugin.COLUMNS) {
            request.addOrderedColumn(column);
        }
    }

    @Test
    public void testGetDate() throws WsfServiceException {
        // prepare params
        request.setParam(TimePlugin.REQUIRED_PARAMS[1], "false");

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
        request.setParam(TimePlugin.REQUIRED_PARAMS[0], "false");

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
        // prepare params
        request.setParam(TimePlugin.REQUIRED_PARAMS[0], "false");
        request.setParam(TimePlugin.REQUIRED_PARAMS[1], "false");
        request.setParam(TimePlugin.OPTIONAL_PARAMS[0], "true");

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
        request.setParam(TimePlugin.OPTIONAL_PARAMS[0], "true");
        request.setParam("-u", null);
        String[][] result = plugin.execute(request).getResult();
        assertEquals("result size", 8, result.length);

    }

    @Test(expected = WsfServiceException.class)
    public void testMissingParam() throws WsfServiceException {
        request.removeParam(TimePlugin.REQUIRED_PARAMS[0]);
        plugin.execute(request);
    }

    @Test(expected = WsfServiceException.class)
    public void testInvalidParam() throws WsfServiceException {
        request.setParam(TimePlugin.REQUIRED_PARAMS[1], "bad");
        plugin.execute(request);
    }

    @Test(expected = WsfServiceException.class)
    public void testInvalidColumn() throws WsfServiceException {
        request.clearOrderedColumns();
        request.addOrderedColumn(TimePlugin.COLUMNS[0]);
        request.addOrderedColumn(TimePlugin.COLUMNS[1]);
        request.addOrderedColumn("Bad");
        plugin.execute(request);
    }

    @Test(expected = WsfServiceException.class)
    public void testMissingColumn() throws WsfServiceException {
        request.clearOrderedColumns();
        request.addOrderedColumn(TimePlugin.COLUMNS[0]);
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
