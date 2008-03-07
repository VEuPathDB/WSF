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
import org.gusdb.wsf.plugin.WsfResult;
import org.junit.Test;

/**
 * @author xingao
 *
 */
public class TimeServiceTest {

    private WsfService service;

    public TimeServiceTest() {
        service = new WsfService();
    }

    @Test
    public void testTimePlugin() throws ServiceException {
        String plugin = "org.gusdb.wsf.plugin.TimePlugin";
        String projectId = "TestDB";
        String[] params = { TimePlugin.REQUIRED_PARAMS[0] + "=true",
                TimePlugin.REQUIRED_PARAMS[1] + "=true" };
        WsfResult result = service.invoke(plugin, projectId, params,
                TimePlugin.COLUMNS);

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
        String[] params = { "param=value" };
        String[] columns = { "column1", "column2" };
        service.invoke("Invalid.plugin", "TestDB", params, columns);
    }
}
