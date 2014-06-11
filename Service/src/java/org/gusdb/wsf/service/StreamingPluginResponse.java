package org.gusdb.wsf.service;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.gusdb.wsf.plugin.PluginResponse;

public class StreamingPluginResponse implements PluginResponse {

  private final ObjectOutputStream outStream;

  public StreamingPluginResponse(ObjectOutputStream outStream) {
      this.outStream = outStream;
  }

  @Override
  public void addRow(String[] row) throws WsfServiceException {
    ResponseRow responseRow = new ResponseRow(row);
    try {
      outStream.writeObject(responseRow);
    }
    catch (IOException ex) {
      throw new WsfServiceException(ex);
    }
  }

  @Override
  public void addAttachment(String key, String content) throws WsfServiceException {
    ResponseAttachment attachment = new ResponseAttachment(key, content);
    try {
      outStream.writeObject(attachment);
    }
    catch (IOException ex) {
      throw new WsfServiceException(ex);
    }
  }

  @Override
  public void setMessage(String message) throws WsfServiceException {
    ResponseMessage responseMessage = new ResponseMessage(message);
    try {
      outStream.writeObject(responseMessage);
    }
    catch (IOException ex) {
      throw new WsfServiceException(ex);
    }
  }

}
