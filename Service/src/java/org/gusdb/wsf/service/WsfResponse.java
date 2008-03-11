/**
 * 
 */
package org.gusdb.wsf.service;

/**
 * @author xingao
 * 
 */
public class WsfResponse {

    private String message;
    private String[][] results;

    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return message;
        
    }

    /**
     * @param message
     *            The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return Returns the results.
     */
    public String[][] getResults() {
        return results;
    }

    /**
     * @param results
     *            The results to set.
     */
    public void setResults(String[][] results) {
        this.results = results;
    }

}
