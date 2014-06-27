package org.gusdb.wsf.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.wsf.plugin.WsfException;
import org.gusdb.wsf.service.ResponseAttachment;
import org.gusdb.wsf.service.ResponseMessage;
import org.gusdb.wsf.service.ResponseRow;
import org.gusdb.wsf.service.ResponseStatus;
import org.gusdb.wsf.service.WsfRequest;
import org.gusdb.wsf.service.WsfService;

public class WsfRemoteClient implements WsfClient {

  private static final Logger LOG = Logger.getLogger(WsfRemoteClient.class);

  private final URI serviceURI;

  private WsfResponseListener listener;

  protected WsfRemoteClient(URI serviceURI) {
    this.serviceURI = serviceURI;
  }

  @Override
  public void setResponseListener(WsfResponseListener listener) {
    this.listener = listener;
  }

  @Override
  public int invoke(WsfRequest request) throws WsfException {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(serviceURI);
    int checksum = request.getChecksum();
    LOG.debug("WSF Remote: checksum=" + checksum + ", url=" + serviceURI + "\n" + request);

    // prepare the form
    Form form = new Form();
    form.param(WsfService.PARAM_REQUEST, request.toString());

    // invoke service
    Response response = target.request(MediaType.APPLICATION_OCTET_STREAM_TYPE).post(
        Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
    int status = response.getStatus();
    if (status != 200)
      throw new WsfClientException("Request failed with status code: " + status);

    InputStream inStream = null;
    int signal = 0;
    Map<String, Integer> stats = new HashMap<String, Integer>();
    stats.put("rows", 0);
    stats.put("attachments", 0);
    try {
      inStream = response.readEntity(InputStream.class);
      signal = readStream(inStream, stats);
    }
    catch (ClassNotFoundException | IOException ex) {
      new WsfClientException(ex);
    }
    finally {
      if (inStream != null) {
        try {
          inStream.close();
        }
        catch (IOException ex) {
          throw new WsfClientException(ex);
        }
        finally {
          response.close();
        }
      }
      LOG.debug("WSF Remote finished: checksum=" + checksum + ", status " + status + ", #rows=" +
          stats.get("rows") + ", #attch=" + stats.get("attachments") + ", url=" + serviceURI);
    }
    return signal;
  }

  private int readStream(InputStream inStream, Map<String, Integer> stats) throws IOException,
      ClassNotFoundException, WsfException {
    ObjectInputStream objectStream = new ObjectInputStream(inStream);
    while (true) {
      Object object = objectStream.readObject();
      if (object instanceof ResponseStatus) {
        // received a status object, which should be the last object to receive
        ResponseStatus status = (ResponseStatus) object;

        // check if an exception is present
        WsfException exception = status.getException();
        if (exception != null)
          throw exception;

        // return the signal
        return status.getSignal();
      }
      else if (object instanceof ResponseRow) { // received a new row from the service
        ResponseRow row = (ResponseRow) object;
        listener.onRowReceived(row.getRow());
        stats.put("rows", stats.get("rows") + 1);
      }
      else if (object instanceof ResponseAttachment) { // received a new attachment
        ResponseAttachment attachment = (ResponseAttachment) object;
        listener.onAttachmentReceived(attachment.getKey(), attachment.getContent());
        stats.put("rows", stats.get("attachments") + 1);
      }
      else if (object instanceof ResponseMessage) { // received message
        ResponseMessage message = (ResponseMessage) object;
        listener.onMessageReceived(message.getMessage());
      }
      else {
        throw new WsfClientException("Unknown object type received: [" + object.getClass().getName() +
            "] - " + object);
      }
    }
  }
}
