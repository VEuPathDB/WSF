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
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;
import org.gusdb.wsf.common.ResponseAttachment;
import org.gusdb.wsf.common.ResponseMessage;
import org.gusdb.wsf.common.ResponseRow;
import org.gusdb.wsf.common.ResponseStatus;
import org.gusdb.wsf.common.WsfRequest;
import org.gusdb.wsf.plugin.PluginUserException;

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
  public int invoke(ClientRequest request) throws ClientModelException, ClientUserException {
    Client client = ClientBuilder.newClient();
    int checksum = request.getChecksum();
    LOG.debug("WSF Remote: checksum=" + checksum + ", url=" + serviceURI + "\n" + request);

    // prepare the form
    Form form = new Form();
    form.param(WsfRequest.PARAM_REQUEST, request.toString());

    // invoke service
    Response response = client.target(serviceURI).property(ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE).request(
        MediaType.APPLICATION_OCTET_STREAM_TYPE).post(
        Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
    int status = response.getStatus();
    if (status >= 400)
      throw new ClientModelException("Request failed with status code: " + status);

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
      new ClientModelException(ex);
    }
    finally {
      if (inStream != null) {
        try {
          inStream.close();
        }
        catch (IOException ex) {
          throw new ClientModelException(ex);
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

  private int readStream(InputStream inStream, Map<String, Integer> stats) throws ClientUserException,
      ClientModelException, IOException, ClassNotFoundException {
    ObjectInputStream objectStream = new ObjectInputStream(inStream);
    while (true) {
      Object object = objectStream.readObject();
      if (object instanceof ResponseStatus) {
        // received a status object, which should be the last object to receive
        ResponseStatus status = (ResponseStatus) object;

        // check if an exception is present
        Exception exception = status.getException();
        if (exception != null) {
          if (exception instanceof PluginUserException) {
            throw new ClientUserException(exception);
          }
          else {
            throw new ClientModelException(exception);
          }
        }
        ;

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
        throw new ClientModelException("Unknown object type received: [" + object.getClass().getName() +
            "] - " + object);
      }
    }
  }
}
