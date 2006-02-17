package org.gusdb.wsf;

/**
 * 
 */

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WsfServiceException extends Exception {

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
        super(cause);
        // TODO Auto-generated constructor stub
    }
}
