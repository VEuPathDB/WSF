/**
 * 
 */
package org.gusdb.wsf.plugin;

import java.io.Serializable;

/**
 * @author xingao
 * 
 */
public class WsfResult implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 9126955986713669742L;

    private String[][] result;
    /**
     * it contains the exit value of the invoked application. If the last
     * invocation is successfully finished, this value is 0; if the plugin
     * hasn't invoked any application, this value is -1; if the last invocation
     * is failed, this value can be any number other than 0. However, this is
     * not the recommended way to check if an invocation is succeeded or not
     * since it relies on the behavior of the external application.
     */
    protected int signal;

    /**
     * The message which the plugin wants to return to the invoking client
     */
    protected String message;

    private String requestId;
    private int totalPackets = 1;
    private int currentPacket = 0;

    public WsfResult() {}

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
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the exit code returned by the invoked command, or any user
     *         defined code if the plugin doesn't invoke any external command
     */
    public int getSignal() {
        return this.signal;
    }

    /**
     * @param signal
     *            the signal to set
     */
    public void setSignal(int signal) {
        this.signal = signal;
    }

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @param requestId
     *            the requestId to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * @return the totalPackets
     */
    public int getTotalPackets() {
        return totalPackets;
    }

    /**
     * @param totalPackets
     *            the totalPackets to set
     */
    public void setTotalPackets(int totalPackets) {
        this.totalPackets = totalPackets;
    }

    /**
     * @return the currentPacket
     */
    public int getCurrentPacket() {
        return currentPacket;
    }

    /**
     * @param currentPacket
     *            the currentPacket to set
     */
    public void setCurrentPacket(int currentPacket) {
        this.currentPacket = currentPacket;
    }
}
