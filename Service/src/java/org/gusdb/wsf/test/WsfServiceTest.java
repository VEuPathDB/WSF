/**
 * 
 */
package org.gusdb.wsf.test;

import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import org.gusdb.wsf.service.WsfResponse;
import org.gusdb.wsf.service.WsfService;

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WsfServiceTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * Test method for 'org.gusdb.wdk.service.ProcessService.invoke(String[],
     * String[], String[])'
     */
    public void testInvoke() {
        // get plugin name; required
        String pluginClassName = System.getProperty("plugin.class");
        assertNotNull(pluginClassName);

        // get plugin name; optional
        String invokeKey = System.getProperty("invoke.key");
        if (invokeKey == null) invokeKey = "";

        // get columns for the result; required
        String columnTemp = System.getProperty("columns");
        assertNotNull(columnTemp);
        String[] columns = columnTemp.split(",");
        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].trim();
        }

        // get parameters for the plugin; optional
        String paramTemp = System.getProperty("parameters");
        String[] params = (paramTemp != null) ? paramTemp.split(",")
                : new String[0];

        WsfService service = new WsfService();
        try {
            WsfResponse response = service.invoke(pluginClassName, invokeKey,
                    params, columns);
            String[][] result = response.getResults();
            String message = response.getMessage();

            // print out the message returned by the plugin
            System.out.println("Plugin returns: " + message);

            // create column map for printing purpose
            Map<String, Integer> map = new HashMap<String, Integer>();
            for (int i = 0; i < columns.length; i++) {
                map.put(columns[i], i);
            }

            // print out the result
            System.out.println("");
            for (int i = 0; i < result.length; i++) {
                System.out.println("================ " + result[i][0]
                        + " ================");
                for (String col : columns) {
                    System.out.println("------------ " + col + " ------------");
                    System.out.println(result[i][map.get(col)]);
                }
                System.out.println();
            }
        } catch (ServiceException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            // System.err.println(ex);
            assertTrue(false);
        }
    }

}
