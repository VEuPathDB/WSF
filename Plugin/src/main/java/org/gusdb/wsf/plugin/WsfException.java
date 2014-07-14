/**
 * 
 */
package org.gusdb.wsf.plugin;

/**
 * @author Jerric
 *
 */
public abstract class WsfException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -8937414006144596084L;

  /**
   * 
   */
  public WsfException() {
  }

  /**
   * @param message
   */
  public WsfException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public WsfException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public WsfException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public WsfException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
