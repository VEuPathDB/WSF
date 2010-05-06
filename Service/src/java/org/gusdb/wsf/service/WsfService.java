/**
 * 
 */
package org.gusdb.wsf.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.xml.rpc.ServiceException;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.gusdb.wsf.plugin.Plugin;
import org.gusdb.wsf.plugin.WsfRequest;
import org.gusdb.wsf.plugin.WsfResponse;
import org.gusdb.wsf.plugin.WsfServiceException;
import org.json.JSONArray;

/**
 * The WSF Web service entry point.
 * 
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WsfService {

    private static double PACKET_SIZE = 1000000;

    private static Logger logger = Logger.getLogger(WsfService.class);

    private static Map<String, Plugin> plugins = new LinkedHashMap<String, Plugin>();

    private File tempDir;

    public WsfService() {
        // initialize temp Dir
        String temp = System.getProperty("java.io.tmpdir", "/tmp");
        tempDir = new File(temp);
        if (!tempDir.exists() || !tempDir.isDirectory()) {
            tempDir.mkdirs();
        }
    }

    public WsfResponse invoke(String pluginClassName, WsfRequest request)
            throws ServiceException {
        long start = System.currentTimeMillis();
        logger.info("Invoking: " + pluginClassName + ", projectId: "
                + request.getProjectId());

        try {

            // use reflection to load the plugin object
            logger.debug("Loading object " + pluginClassName);

            // check if the plugin has been cached
            Plugin plugin;
            if (plugins.containsKey(pluginClassName)) {
                plugin = plugins.get(pluginClassName);
            } else {
                logger.info("Creating plugin " + pluginClassName);
                Class<?> pluginClass = Class.forName(pluginClassName);
                plugin = (Plugin) pluginClass.newInstance();

                // get context
                Map<String, Object> context = new HashMap<String, Object>();

                MessageContext msgContext = MessageContext.getCurrentContext();
                Servlet servlet = (Servlet) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLET);
                ServletContext scontext = servlet.getServletConfig().getServletContext();
                for (String key : plugin.getContextKeys()) {
                    Object value = scontext.getAttribute(key);
                    context.put(key, value);
                }

                plugin.setContext(context);
                plugins.put(pluginClassName, plugin);
            }

            // invoke the plugin
            logger.debug("Invoking Plugin " + pluginClassName);
            WsfResponse result = invokePlugin(plugin, request);
            logger.info("Result Message: '" + result.getMessage() + "'");
            prepareResult(result);

            long end = System.currentTimeMillis();
            logger.info("WSF finshed in: " + ((end - start) / 1000.0)
                    + " seconds with " + result.getResult().length
                    + " results.");

            return result;
        } catch (Exception ex) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            logger.error(ex);
            logger.error(writer.toString());
            throw new ServiceException(ex);
        }
    }

    public String requestResult(String requestId, int packetId)
            throws ServiceException {
        try {
            File file = new File(tempDir, requestId);
            logger.debug("Get WSF message: " + requestId + ", packet = "
                    + packetId + ", at " + file.getAbsolutePath());
            if (!file.exists())
                throw new WsfServiceException(
                        "The requestId doesn't match any "
                                + "previous request.");
            double packets = Math.ceil(file.length() / PACKET_SIZE);
            if (packetId < 0 || packets < packetId)
                throw new WsfServiceException(
                        "The packet id is beyond the scope: " + packets);
            RandomAccessFile reader = new RandomAccessFile(file, "r");
            long pos = packetId * (long) PACKET_SIZE;
            reader.seek(pos);
            int size = (int) Math.min(PACKET_SIZE, file.length() - pos);
            byte[] buffer = new byte[size];
            reader.read(buffer);
            reader.close();

            // check if the packet is the last piece, if so, remove the cache
            if (packetId + 1 == packets) file.delete();

            return new String(buffer);
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    private void prepareResult(WsfResponse result) throws IOException {
        String requestId = getNextId();
        result.setRequestId(requestId);
        String content = convertResult(result.getResult());
        int packets = (int) Math.ceil(content.length() / PACKET_SIZE);
        result.setTotalPackets(packets);
        result.setCurrentPacket(1);

        File file = new File(tempDir, requestId);
        if (packets > 1) {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.flush();
            writer.close();
            String part = content.substring(0, (int) PACKET_SIZE);
            result.setResult(new String[][] { { part } });
        } else { // no cache needed, delete the file handle
            if (file.exists()) file.delete();
        }
    }

    private String getNextId() throws IOException {
        File file = File.createTempFile("wsf-", ".cache", tempDir);
        return file.getName();
    }

    private String convertResult(String[][] array) {
        JSONArray jsResult = new JSONArray();
        for (int row = 0; row < array.length; row++) {
            JSONArray jsRow = new JSONArray();
            for (int col = 0; col < array[row].length; col++) {
                jsRow.put(array[row][col]);
            }
            jsResult.put(jsRow);
        }
        return jsResult.toString();
    }

    private WsfResponse invokePlugin(Plugin plugin, WsfRequest request)
            throws WsfServiceException {
        // validate required parameters
        validateRequiredParameters(plugin, request);

        // validate columns
        validateColumns(plugin, request);

        // validate parameters
        plugin.validateParameters(request);

        // execute the main function, and obtain result
        WsfResponse result = plugin.execute(request);

        return result;
    }

    private void validateRequiredParameters(Plugin plugin, WsfRequest request)
            throws WsfServiceException {
        String[] reqParams = plugin.getRequiredParameterNames();

        // validate parameters
        Map<String, String> params = request.getParams();
        for (String param : reqParams) {
            if (!params.containsKey(param)) {
                throw new WsfServiceException(
                        "The required parameter is missing: " + param);
            }
        }
    }

    private void validateColumns(Plugin plugin, WsfRequest request)
            throws WsfServiceException {
        String[] reqColumns = plugin.getColumns();

        Set<String> colSet = new HashSet<String>();
        String[] orderedColumns = request.getOrderedColumns();
        for (String col : orderedColumns) {
            colSet.add(col);
        }
        for (String col : reqColumns) {
            if (!colSet.contains(col)) {
                throw new WsfServiceException(
                        "The required column is missing: " + col);
            }
        }
        // cross check
        colSet.clear();
        colSet = new HashSet<String>(reqColumns.length);
        for (String col : reqColumns) {
            colSet.add(col);
        }
        for (String col : orderedColumns) {
            if (!colSet.contains(col)) {
                throw new WsfServiceException("Unknown column: " + col);
            }
        }
    }

}
