/**
 * 
 */
package org.gusdb.wsf.service;

import java.io.Serializable;
import java.util.Map;

/**
 * @author xingao
 * 
 */
public class WsfResponse implements Serializable {

  /**
     * 
     */
  private static final long serialVersionUID = 9126955986713669742L;

  private String[][] result = new String[0][0];

  /**
   * it contains the exit value of the invoked application. If the last
   * invocation is successfully finished, this value is 0; if the plugin hasn't
   * invoked any application, this value is -1; if the last invocation is
   * failed, this value can be any number other than 0. However, this is not the
   * recommended way to check if an invocation is succeeded or not since it
   * relies on the behavior of the external application.
   */
  protected int signal;

  /**
   * The message which the plugin wants to return to the invoking client
   */
  protected String message;

  private int invokeId;
  private int pageCount = 1;
  private int currentPage = 0;

  private Map<String, String> attachments;

  public WsfResponse() {}

  /**
   * If the response has more than 1 packet, this method will return [1][1]
   * array, in which the string representation of a packet is stored. The client
   * is responsible for getting all the packets, and concatenate them to a JSON
   * representation, then convert JSON back to String[][] array. If the response
   * has only one packet, the original result is returned.
   * 
   * @return
   */
  public String[][] getResult() {
    return this.result;
  }

  public void setResult(String[][] result) {
    this.result = result;
  }

  /**
   * @return get the result message
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * @param message
   *          the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * @return the exit code returned by the invoked command, or any user defined
   *         code if the plugin doesn't invoke any external command
   */
  public int getSignal() {
    return this.signal;
  }

  /**
   * @param signal
   *          the signal to set
   */
  public void setSignal(int signal) {
    this.signal = signal;
  }

  public int getInvokeId() {
    return invokeId;
  }

  public void setInvokeId(int invokeId) {
    this.invokeId = invokeId;
  }

  public int getPageCount() {
    return pageCount;
  }

  public void setPageCount(int pageCount) {
    this.pageCount = pageCount;
  }

  public int getCurrentPage() {
    return currentPage;
  }

  public void setCurrentPage(int currentPage) {
    this.currentPage = currentPage;
  }

  public Map<String, String> getAttachments() {
    return attachments;
  }

  public void setAttachments(Map<String, String> attachments) {
    this.attachments = attachments;
  }
}
