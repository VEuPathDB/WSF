/**
 * 
 */
package org.gusdb.wsf.client;


/**
 * @author Jerric
 *
 */
public class ClientUserException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 2L;

  /**
   * 
   */
  public ClientUserException() {
    super();
  }

  /**
   * @param message
   */
  public ClientUserException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public ClientUserException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public ClientUserException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public ClientUserException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
