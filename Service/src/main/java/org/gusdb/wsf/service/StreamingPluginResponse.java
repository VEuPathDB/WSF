package org.gusdb.wsf.service;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonGenerator;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wsf.common.ResponseAttachment;
import org.gusdb.wsf.common.ResponseStatus;
import org.gusdb.wsf.plugin.PluginModelException;
import org.gusdb.wsf.plugin.PluginResponse;
import org.gusdb.wsf.plugin.PluginSupport;

public class StreamingPluginResponse implements PluginResponse, AutoCloseable {

  private final JsonGenerator jsonStream;

  private int rowCount;
  private int attachmentCount;

  public StreamingPluginResponse(OutputStream outStream) throws PluginModelException {
    try {
      this.jsonStream = JsonUtil.Jackson.createGenerator(outStream);
    }
    catch (IOException e) {
      throw new PluginModelException(e);
    }
  }

  public int getRowCount() {
    return rowCount;
  }

  public int getAttachmentCount() {
    return attachmentCount;
  }

  @Override
  public void addRow(String[] row) throws PluginModelException {
    try {
      jsonStream.writeArray(row, 0, row.length);
      rowCount++;
    }
    catch (IOException ex) {
      throw new PluginModelException(ex);
    }
  }

  @Override
  public void addAttachment(String key, String content) throws PluginModelException {
    try {
      jsonStream.writeStartObject();
      jsonStream.writeStringField(ResponseAttachment.JSON_KEY_KEY, key);
      jsonStream.writeStringField(ResponseAttachment.JSON_KEY_CONTENT, content);
      jsonStream.writeEndObject();
      attachmentCount++;
    }
    catch (IOException ex) {
      throw new PluginModelException(ex);
    }
  }

  @Override
  public void setMessage(String message) throws PluginModelException {
    try {
      jsonStream.writeString(message);
    }
    catch (IOException ex) {
      throw new PluginModelException(ex);
    }
  }

  public void writeStatus(ResponseStatus status) throws PluginModelException {
    try {
      jsonStream.writeStartObject();
      jsonStream.writeNumberField(ResponseStatus.JSON_KEY_SIGNAL, status.getSignal());
      if (status.getException() != null) {
        jsonStream.writeFieldName(ResponseStatus.JSON_KEY_EXCEPTION);
        PluginSupport.writeException(jsonStream, status.getException());
      }
      jsonStream.writeEndObject();
    } catch (IOException e) {
      throw new PluginModelException(e);
    }
  }

  @Override
  public void close() throws Exception {
    jsonStream.close();
  }
}
