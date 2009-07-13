/**
 * 
 */
package org.gusdb.wsf.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.gusdb.wsf.plugin.IWsfPlugin;
import org.gusdb.wsf.plugin.WsfResult;
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

    private static Map<String, IWsfPlugin> plugins = new LinkedHashMap<String, IWsfPlugin>();

    private File tempDir;

    public WsfService() {
        // initialize temp Dir
        String temp = System.getProperty("java.io.tmpdir", "/tmp");
        tempDir = new File(temp + "/wsf-service/cache/");
        if (!tempDir.exists() || !tempDir.isDirectory()) tempDir.mkdirs();

    }

    /**
     * This method is left for backward compatibility purpose
     * 
     * @param pluginClassName
     * @param projectId
     * @param paramValues
     * @param columns
     * @return
     * @throws ServiceException
     */
    public WsfResponse invoke(String pluginClassName, String projectId,
            String[] paramValues, String[] columns) throws ServiceException {
        WsfResult result = invokeEx(pluginClassName, projectId, paramValues,
                columns);
        WsfResponse response = new WsfResponse();
        response.setMessage(result.getMessage());
        response.setResults(result.getResult());
        return response;
    }

    /**
     * Client requests to run a plugin by providing the complete class name of
     * the plugin, and the service will invoke the plugin and return the result
     * to the client in tabular format.
     * 
     * @param pluginClassName
     * @param projectId
     *            The id of the project that invokes the service
     * @param paramValues
     *            an array of "param=value" pairs. The param and value and
     *            separated by the first "="
     * @param cols
     * @return
     * @throws WsfServiceException
     */
    public WsfResult invokeEx(String pluginClassName, String projectId,
            String[] paramValues, String[] columns) throws ServiceException {
        int resultSize = 0;
        long start = System.currentTimeMillis();
        logger.info("Invoking: " + pluginClassName + ", projectId: "
                + projectId);

        Map<String, String> params = convertParams(paramValues);
        try {
            // use reflection to load the plugin object
            logger.debug("Loading object " + pluginClassName);

            // check if the plugin has been cached
            IWsfPlugin plugin;
            if (plugins.containsKey(pluginClassName)) {
                plugin = plugins.get(pluginClassName);
            } else {
                logger.info("Creating plugin " + pluginClassName);
                Class<?> pluginClass = Class.forName(pluginClassName);
                plugin = (IWsfPlugin) pluginClass.newInstance();
                plugin.setLogger(Logger.getLogger(pluginClass));
                plugins.put(pluginClassName, plugin);
            }

            // invoke the plugin
            logger.debug("Invoking Plugin " + pluginClassName);
            WsfResult result = plugin.invoke(projectId, params, columns);
            resultSize = result.getResult().length;

            prepareResult(result);

            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex);
            throw new ServiceException(ex);
        } finally {
            long end = System.currentTimeMillis();
            logger.info("WSF finshed in: " + ((end - start) / 1000.0)
                    + " seconds with " + resultSize + " results.");
        }
    }

    public String requestResult(String requestId, int packetId)
            throws WsfServiceException, IOException {
        File file = new File(tempDir, requestId + ".out");
        if (!file.exists())
            throw new WsfServiceException("The requestId doesn't match any "
                    + "previous request.");
        double packets = Math.ceil(file.length() / PACKET_SIZE);
        if (packetId < 0 || packets < packetId)
            throw new WsfServiceException("The packet id is beyond the scope: "
                    + packets);
        RandomAccessFile reader = new RandomAccessFile(file, "r");
        long pos = packetId * (long) PACKET_SIZE;
        reader.seek(pos);
        int size = (int)Math.min(PACKET_SIZE, file.length() - pos);
        byte[] buffer = new byte[size];
        reader.read(buffer);
        return new String(buffer);
    }

    private Map<String, String> convertParams(String[] paramValues) {
        Map<String, String> params = new HashMap<String, String>(
                paramValues.length);
        for (String paramValue : paramValues) {
            int pos = paramValue.indexOf('=');
            if (pos < 0) params.put(paramValue.trim(), "");
            else {
                String param = paramValue.substring(0, pos).trim();
                String value = paramValue.substring(pos + 1).trim();
                params.put(param, value);
            }
        }
        return params;
    }

    private void prepareResult(WsfResult result) throws IOException {
        String requestId = getNextId();
        result.setRequestId(requestId);
        String content = convertResult(result.getResult());
        int packets = (int) Math.ceil(content.length() / PACKET_SIZE);
        result.setTotalPackets(packets);
        result.setCurrentPacket(1);

        if (packets > 1) {
            String fileName = tempDir.getAbsolutePath() + requestId + ".out";
            FileWriter writer = new FileWriter(fileName);
            writer.write(content);
            writer.flush();
            writer.close();
            String part = content.substring(0, (int) PACKET_SIZE);
            result.setResult(new String[][] { { part } });
        }
    }

    private String getNextId() throws IOException {
        String requestId = null;
        while (true) {
            requestId = UUID.randomUUID().toString();
            File file = new File(tempDir, requestId + ".out");
            // atomic method, return uuid if file created successfully,
            // otherwise, the file already exists, and try a next uuid.
            if (file.createNewFile()) break;
        }
        return requestId;
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
}
