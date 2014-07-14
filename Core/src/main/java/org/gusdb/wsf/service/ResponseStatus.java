package org.gusdb.wsf.service;

import java.io.Serializable;

import org.gusdb.wsf.common.WsfException;

public class ResponseStatus implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private int signal;

  private WsfException exception;

  public void setSignal(int signal) {
    this.signal = signal;
  }

  /**
   * @return the signal
   */
  public int getSignal() {
    return signal;
  }

  /**
   * @return the exception
   */
  public WsfException getException() {
    return exception;
  }

  /**
   * @param exception
   *          the exception to set
   */
  public void setException(WsfException exception) {
    this.exception = exception;
  }

  @Override
  public String toString() {
    return "signal=" + signal + ", exception=" + exception;
  }
}
