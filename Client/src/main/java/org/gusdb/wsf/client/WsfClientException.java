/**
 * 
 */
package org.gusdb.wsf.client;

import org.gusdb.wsf.common.WsfException;

/**
 * @author Jerric
 *
 */
public class WsfClientException extends WsfException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public WsfClientException() {
    super();
  }

  /**
   * @param message
   */
  public WsfClientException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public WsfClientException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public WsfClientException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public WsfClientException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
