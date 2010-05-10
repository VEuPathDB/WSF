/**
 * 
 */
package org.gusdb.wsf.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.gusdb.wsf.plugin.TimePlugin;
import org.gusdb.wsf.plugin.WsfRequest;
import org.gusdb.wsf.plugin.WsfResponse;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class TimeServiceTest {

    private WsfRequest request;
    private WsfService service;
    private Map<String, String> params;

    public TimeServiceTest() {
        service = new WsfService();
        request = new WsfRequest();
        request.setProjectId("TestDB");

        params = new HashMap<String, String>();
        params.put(TimePlugin.REQUIRED_PARAMS[0], "true");
        params.put(TimePlugin.REQUIRED_PARAMS[1], "true");
        request.setParams(params);
        request.setOrderedColumns(TimePlugin.COLUMNS);
    }

    @Test
    public void testTimePlugin() throws ServiceException {
        String plugin = "org.gusdb.wsf.plugin.TimePlugin";

        WsfRequest request = new WsfRequest();
        request.setProjectId("TestDB");
        params.put(TimePlugin.REQUIRED_PARAMS[0], "true");
        params.put(TimePlugin.REQUIRED_PARAMS[1], "true");
        request.setParams(params);

        WsfResponse result = service.invoke(plugin, request);

        assertEquals("signal", 0, result.getSignal());

        String message = result.getMessage();
        assertTrue("Message: " + message, message.trim().length() > 0);

        String[][] array = result.getResult();
        Map<String, String> resultMap = new HashMap<String, String>();
        for (int i = 0; i < array.length; i++) {
            resultMap.put(array[i][0], array[i][1]);
        }
        assertEquals("result size", 7, array.length);

        Calendar now = Calendar.getInstance();
        int year = Integer.parseInt(resultMap.get(TimePlugin.YEAR));
        assertEquals(TimePlugin.YEAR, now.get(Calendar.YEAR), year);
        int month = Integer.parseInt(resultMap.get(TimePlugin.MONTH));
        assertEquals(TimePlugin.MONTH, now.get(Calendar.MONTH), month - 1);
        int day = Integer.parseInt(resultMap.get(TimePlugin.DAY));
        assertEquals(TimePlugin.DAY, now.get(Calendar.DAY_OF_MONTH), day);
    }

    @Test(expected = ServiceException.class)
    public void testInvalidPlugin() throws ServiceException {
        service.invoke("Invalid.plugin", request);
    }
}