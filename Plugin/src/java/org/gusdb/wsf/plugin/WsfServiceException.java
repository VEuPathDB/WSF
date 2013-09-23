package org.gusdb.wsf.plugin;

import java.io.Serializable;

/**
 * 
 */

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WsfServiceException extends Exception implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6228934705413257448L;

    /**
     * 
     */
    public WsfServiceException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public WsfServiceException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public WsfServiceException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public WsfServiceException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public WsfServiceException(String message, Throwable cause,
        boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
    }

}
