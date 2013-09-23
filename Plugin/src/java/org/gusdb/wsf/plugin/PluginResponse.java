/**
 * 
 */
package org.gusdb.wsf.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author jerric
 * 
 */
/**
 * @author jerric
 * 
 */
public class PluginResponse {

  private static final String FILE_PREFIX = "cache.";
  private static final long PAGE_SIZE = 5 * 1024 * 1024;

  /**
   * it contains the exit value of the invoked application. If the last
   * invocation is successfully finished, this value is 0; if the plugin hasn't
   * invoked any application, this value is -1; if the last invocation is
   * failed, this value can be any number other than 0. However, this is not the
   * recommended way to check if an invocation is succeeded or not since it
   * relies on the behavior of the external application.
   */
  private int signal;

  /**
   * The message which the plugin wants to return to the invoking client
   */
  private String message;

  /**
   * Additional information that the plugin wants to return to the client.
   */
  private Map<String, String> attachments;

  private final File storageDir;
  private final int invokeId;
  private final List<String[]> rows;

  private long size = 0;
  private int pageIndex = 0;

  /**
   * 
   */
  public PluginResponse(File storageDir, int invokeId) {
    this.storageDir = storageDir;
    this.invokeId = invokeId;
    this.rows = new ArrayList<>();
  }

  /**
   * read the data of the given page from storage, and the data will also be
   * removed from the storage. Therefore, this method should only be called once
   * for a give page in an invocation.
   * 
   * @param pageId
   *          0 based page id, the maximum page id is pageCount - 1.
   * @return returns data of the current page.
   * @throws WsfServiceException
   */
  public String[][] getPage(int pageId) throws WsfServiceException {
    File dir = new File(storageDir, Integer.toString(invokeId));
    File file = new File(dir, FILE_PREFIX + pageIndex);
    if (!file.exists()) return null;
    try {
      // read the content of the file into byte array.
      InputStream input = new FileInputStream(file);
      byte[] content = new byte[(int) file.length()];
      input.read(content);
      input.close();

      // delete the file since it's been accessed.
      file.delete();
      // also check if the folder is empty, if so, delete it too.
      String[] children = dir.list();
      boolean empty = true;
      for (String child : children) {
        if (!child.equals(".") && !child.equals("..")) {
          empty = false;
          break;
        }
      }
      if (empty) dir.delete();

      // convert JSON representation back into 2D String array.
      JSONArray jsRows = new JSONArray(new String(content, "utf-8"));
      String[][] rows = new String[jsRows.length()][];
      for (int i = 0; i < jsRows.length(); i++) {
        JSONArray jsRow = jsRows.getJSONArray(i);
        String[] row = new String[jsRow.length()];
        for (int j = 0; j < jsRow.length(); j++) {
          row[j] = jsRow.getString(j);
        }
        rows[i] = row;
      }
      return rows;
    } catch (IOException | JSONException ex) {
      throw new WsfServiceException(ex);
    }
  }
  
  public void cleanup() {
    // delete all the resources storage by the response
    File dir = new File(storageDir, Integer.toString(invokeId));
    deleteFolder(dir);
  }
  
  /**
   * Delete a folder and all the sub-folders and files under it recursively.
   * @param dir
   */
  private void deleteFolder(File dir) {
    if (!dir.exists()) return;
    // delete
  }

  /**
   * Add a new row of result into the storage. when the data reaches the max
   * size of a page, the page is saved into storage automatically, and a new
   * page will be created.
   * 
   * @param row
   * @throws WsfServiceException
   */
  public synchronized void addRow(String[] row) throws WsfServiceException {
    // check if we need to start a new page
    if (size >= PAGE_SIZE) flush();

    // add in new rows;
    rows.add(row);

    // compute the current page size, and flush if it reaches max page size.
    for (String value : row) {
      size += value.length();
    }
    if (size > PAGE_SIZE) flush();
  }

  /**
   * Save the remaining data into storage. if there is no more remaining data,
   * calling flush will do nothing. If a flush does happen, the page count will
   * be increased by one.
   * 
   * This method should always be called at last when adding to storage is
   * finished.
   * 
   * @throws WsfServiceException
   */
  public void flush() throws WsfServiceException {
    // if there's no data, do nothing.
    if (rows.size() == 0) return;

    try {
      saveCurrentPage();
    } catch (IOException ex) {
      throw new WsfServiceException(ex);
    }

    // reset page
    pageIndex++;
    rows.clear();
    size = 0;
  }

  /**
   * get the total number of pages. The page index is 0 based, and the maximum
   * index is pageCount - 1;
   * 
   * @return
   */
  public int getPageCount() {
    return pageIndex;
  }

  /**
   * Get the message which the plugin wants to return to the invoking client.
   * 
   * @return
   */
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Get additional information that the plugin wants to return to the client.
   * 
   * @return
   */
  public Map<String, String> getAttachments() {
    return attachments;
  }

  public void setAttachments(Map<String, String> attachments) {
    this.attachments = attachments;
  }
  
  public void addAttachments(Map<String, String> attachments) {
    this.attachments.putAll(attachments);
  }

  /**
   * The invokeId represent a unique invocation of the plugin.
   * 
   * @return
   */
  public int getInvokeId() {
    return invokeId;
  }

  /**
   * it contains the exit value of the invoked application. If the last
   * invocation is successfully finished, this value is 0; if the plugin hasn't
   * invoked any application, this value is -1; if the last invocation is
   * failed, this value can be any number other than 0. However, this is not the
   * recommended way to check if an invocation is succeeded or not since it
   * relies on the behavior of the external application.
   * 
   * @return
   */
  public int getSignal() {
    return signal;
  }

  public void setSignal(int signal) {
    this.signal = signal;
  }

  private void saveCurrentPage() throws IOException {
    JSONArray jsRows = new JSONArray();
    for (String[] row : rows) {
      JSONArray jsRow = new JSONArray();
      for (String value : row) {
        jsRow.put(value);
      }
      jsRows.put(jsRow);
    }
    File dir = new File(storageDir, Integer.toString(invokeId));
    if (dir.mkdir()) {
      File file = new File(dir, FILE_PREFIX + pageIndex);
      OutputStream output = new FileOutputStream(file, false);
      output.write(jsRows.toString().getBytes("utf-8"));
      output.flush();
      output.close();
    } else {
      throw new IOException("cannot create directory: " + dir.getAbsolutePath());
    }
  }
}
