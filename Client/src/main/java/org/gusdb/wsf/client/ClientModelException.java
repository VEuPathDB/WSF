/**
 * 
 */
package org.gusdb.wsf.client;


/**
 * @author Jerric
 *
 */
public class ClientModelException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 2L;

  /**
   * 
   */
  public ClientModelException() {
    super();
  }

  /**
   * @param message
   */
  public ClientModelException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public ClientModelException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public ClientModelException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public ClientModelException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
