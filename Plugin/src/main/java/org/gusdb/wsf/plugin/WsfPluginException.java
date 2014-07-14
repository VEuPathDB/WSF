package org.gusdb.wsf.plugin;


/**
 * 
 */

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WsfPluginException extends WsfException {

    /**
   * 
   */
  private static final long serialVersionUID = 4927047061317403654L;

    /**
     * 
     */
    public WsfPluginException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public WsfPluginException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public WsfPluginException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public WsfPluginException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public WsfPluginException(String message, Throwable cause,
        boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
    }

}
