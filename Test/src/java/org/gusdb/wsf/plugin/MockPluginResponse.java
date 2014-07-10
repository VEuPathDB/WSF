package org.gusdb.wsf.plugin;

import org.gusdb.wsf.MockResponseHandler;

public class MockPluginResponse extends MockResponseHandler implements PluginResponse {

  @Override
  public void addRow(String[] row) throws WsfPluginException, WsfUserException {
    validateRow(row);
  }

  @Override
  public void addAttachment(String key, String content) throws WsfPluginException, WsfUserException {
    validateAttachment(key, content);
  }

  @Override
  public void setMessage(String message) throws WsfPluginException, WsfUserException {
    validateMessage(message);
  }

}
