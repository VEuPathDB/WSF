package org.gusdb.wsf.plugin;

import org.gusdb.wsf.common.WsfException;

/**
 * @author Jerric
 * @created Nov 2, 2005
 */
public class WsfPluginException extends WsfException {

  private static final long serialVersionUID = 4927047061317403654L;

  public WsfPluginException() {
    super();
  }

  public WsfPluginException(String message) {
    super(message);
  }

  public WsfPluginException(String message, Throwable cause) {
    super(message, cause);
  }

  public WsfPluginException(Throwable cause) {
    super(cause.getMessage(), cause);
  }

  public WsfPluginException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
