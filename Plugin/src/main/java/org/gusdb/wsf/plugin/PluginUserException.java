/**
 * 
 */
package org.gusdb.wsf.plugin;




/**
 * @author Jerric
 *
 */
public class PluginUserException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 2;

  /**
   * 
   */
  public PluginUserException() {
  }

  /**
   * @param message
   */
  public PluginUserException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public PluginUserException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public PluginUserException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public PluginUserException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
