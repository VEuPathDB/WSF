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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class TimePluginTest {

    private IWsfPlugin plugin;
    private final String projectId = "TestDB";
    private Map<String, String> params;

    public TimePluginTest() {
        plugin = new TimePlugin();
        params = new HashMap<String, String>();
    }

    @Before
    public void createParams() {
        params.put(TimePlugin.REQUIRED_PARAMS[0], "true");
        params.put(TimePlugin.REQUIRED_PARAMS[1], "true");
    }

    @After
    public void clearParams() {
        params.clear();
    }

    @Test
    public void testGetDate() throws WsfServiceException {
        // prepare params
        params.put(TimePlugin.REQUIRED_PARAMS[1], "false");

        WsfResult result = plugin.invoke(projectId, null, params,
                TimePlugin.COLUMNS);
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

        String[][] result = plugin.invoke(projectId, null, params,
                TimePlugin.COLUMNS).getResult();
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
        params.put(TimePlugin.REQUIRED_PARAMS[0], "false");
        params.put(TimePlugin.REQUIRED_PARAMS[1], "false");
        params.put(TimePlugin.OPTIONAL_PARAMS[0], "true");

        String[] columns = { TimePlugin.COLUMNS[1], TimePlugin.COLUMNS[0] };

        String[][] result = plugin.invoke(projectId, null, params, columns).getResult();
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
        String[][] result = plugin.invoke(projectId, null, params,
                TimePlugin.COLUMNS).getResult();
        assertEquals("result size", 8, result.length);

    }

    @Test(expected = WsfServiceException.class)
    public void testMissingParam() throws WsfServiceException {
        params.remove(TimePlugin.REQUIRED_PARAMS[0]);
        plugin.invoke(projectId, null, params, TimePlugin.COLUMNS);
    }

    @Test(expected = WsfServiceException.class)
    public void testInvalidParam() throws WsfServiceException {
        params.put(TimePlugin.REQUIRED_PARAMS[1], "bad");
        plugin.invoke(projectId, null, params, TimePlugin.COLUMNS);
    }

    @Test(expected = WsfServiceException.class)
    public void testInvalidColumn() throws WsfServiceException {
        String[] columns = { TimePlugin.COLUMNS[0], TimePlugin.COLUMNS[1],
                "Bad" };
        plugin.invoke(projectId, null, params, columns);
    }

    @Test(expected = WsfServiceException.class)
    public void testMissingColumn() throws WsfServiceException {
        String[] columns = { TimePlugin.COLUMNS[0] };
        plugin.invoke(projectId, null, params, columns);
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
