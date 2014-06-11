/**
 * 
 */
package org.gusdb.wsf.plugin;

/**
 * @author Jerric
 *
 */
public class WsfUserException extends WsfException {

  /**
   * 
   */
  private static final long serialVersionUID = -1678301995177067480L;

  /**
   * 
   */
  public WsfUserException() {
  }

  /**
   * @param message
   */
  public WsfUserException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public WsfUserException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public WsfUserException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public WsfUserException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
