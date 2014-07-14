package org.gusdb.wsf.plugin;

import org.gusdb.wsf.common.WsfException;

public interface PluginResponse {

  void addRow(String[] row) throws WsfException;

  void addAttachment(String key, String content) throws WsfException;

  void setMessage(String message) throws WsfException;
}
