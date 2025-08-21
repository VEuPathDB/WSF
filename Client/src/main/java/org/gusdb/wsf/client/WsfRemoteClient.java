package org.gusdb.wsf.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;
import org.gusdb.fgputil.functional.Either;
import org.gusdb.wsf.common.ResponseAttachment;
import org.gusdb.wsf.common.ResponseStatus;
import org.gusdb.wsf.plugin.DelayedResultException;
import org.gusdb.wsf.plugin.StreamingPluginSupport;
import org.gusdb.wsf.plugin.PluginUserException;

import static org.gusdb.fgputil.json.JsonUtil.Jackson;

public class WsfRemoteClient implements WsfClient {

  private static final Logger LOG = Logger.getLogger(WsfRemoteClient.class);

  private final URI serviceURI;

  private WsfResponseListener listener;

  /**
   * Buffer list to be reused for constructing individual row string arrays to
   * reduce the overhead of instantiation and backing array resizing.
   */
  private ArrayList<String> arrayBuffer;

  protected WsfRemoteClient(URI serviceURI) {
    this.serviceURI = serviceURI;
  }

  @Override
  public void setResponseListener(WsfResponseListener listener) {
    this.arrayBuffer = new ArrayList<>(64);
    this.listener = listener;
  }

  @Override
  public int invoke(ClientRequest request) throws ClientModelException, ClientUserException, DelayedResultException {
    Client client = ClientBuilder.newClient();
    int checksum = request.getChecksum();
    LOG.debug("WSF Remote: checksum=" + checksum + ", url=" + serviceURI + "\n" + request);

    // invoke service
    Response response;
    try {
      final Optional<Duration> timeout = request.getRemoteExecuteTimeout();
      final Future<Response> responseFuture = client.target(serviceURI)
          .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE)
          .request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
          .async()
          .post(Entity.entity(request.toString(), MediaType.APPLICATION_JSON));
      if (timeout.isPresent()) {
        response = responseFuture.get(timeout.get().toMillis(), TimeUnit.MILLISECONDS);
      } else {
        response = responseFuture.get();
      }
    } catch (InterruptedException ex) {
      LOG.warn(String.format("Interrupted while invoking service at %s.", serviceURI.toString()), ex);
      Thread.currentThread().interrupt();
      throw new ClientModelException(ex);
    } catch (ExecutionException | TimeoutException ex) {
      LOG.warn(String.format("Exception while invoking service at %s.", serviceURI.toString()), ex);
      throw new ClientModelException(ex);
    }
    int status = response.getStatus();
    if (status >= 400)
      throw new ClientModelException("Request failed with status code: " + status);

    InputStream inStream = null;
    int signal;
    var stats = new Stats();
    try {
      inStream = response.readEntity(InputStream.class);
      signal = readStream(inStream, stats);
    }
    catch (ClassNotFoundException | IOException ex) {
      throw new ClientModelException(ex);
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
          stats.rows + ", #attch=" + stats.attachments + ", url=" + serviceURI);
    }
    return signal;
  }

  private int readStream(InputStream inStream, Stats stats) throws ClientUserException,
      ClientModelException, IOException, ClassNotFoundException, DelayedResultException {

    try (var parser = Jackson.getFactory().createParser(inStream)) {
      JsonToken jsonToken;

      while ((jsonToken = parser.nextToken()) != null) {
        switch (jsonToken) {
          // row == array of strings
          case START_ARRAY:
            listener.onRowReceived(readStreamArray(parser));
            stats.rows++;
            break;

          // message == string
          case VALUE_STRING:
            listener.onMessageReceived(parser.getText());
            break;

          // (status || attachment) == object
          case START_OBJECT:
            var multiResponse = readStreamObject(parser);

            if (multiResponse.isRight())
              return processResponseStatus(multiResponse.getRight());

            var attachment = multiResponse.getLeft();
            listener.onAttachmentReceived(attachment.getKey(), attachment.getContent());
            stats.attachments++;
            break;

          default:
            throw new ClientModelException("malformed result stream, expected START_ARRAY, VALUE_STRING, "
              + "or START_OBJECT.  Instead got: " + jsonToken);
        }
      }
    }

    throw new ClientModelException("result stream ended with no status object");
  }

  /**
   * Parses an object from the json stream into one of the 2 possible valid
   * types: A {@link ResponseAttachment} record, or the stream end indicator
   * {@link ResponseStatus}.
   *
   * @param parser JSON stream parser.
   *
   * @return An {@link Either} instance containing one of the two valid stream
   * object types.
   *
   * @throws ClientModelException If the stream object does not conform to the
   * shape of one of the expected object types.
   */
  private Either<ResponseAttachment, ResponseStatus> readStreamObject(JsonParser parser)
  throws ClientModelException, IOException {
    var node = parser.<ObjectNode>readValueAsTree();

    // If the signal key exists, it is a ResponseStatus instance.
    if (node.has(ResponseStatus.JSON_KEY_SIGNAL)) {
      var out = new ResponseStatus();

      out.setSignal(node.get(ResponseStatus.JSON_KEY_SIGNAL).intValue());

      // get a possible exception value as a raw json node (or null if absent)
      var exception = node.get(ResponseStatus.JSON_KEY_EXCEPTION);

      // if there was an exception value in the status json, then add it to the
      // output response status object.
      if (!StreamingPluginSupport.isNull(exception)) {
        out.setException(StreamingPluginSupport.EXCEPTION_READER.readValue(exception));
      }

      return Either.right(out);
    }

    // If the content key exists, it is a ResponseAttachment instance.
    if (node.has(ResponseAttachment.JSON_KEY_CONTENT)) {
      return Either.left(new ResponseAttachment(
        node.get(ResponseAttachment.JSON_KEY_KEY).textValue(),
        node.get(ResponseAttachment.JSON_KEY_CONTENT).textValue()
      ));
    }

    throw new ClientModelException("unrecognized object in result stream: " + node.toPrettyString());
  }

  /**
   * Checks the response status for any exceptions and returns the signal code
   * if there are none.
   *
   * @param status Plugin response status info.
   *
   * @return The status signal code if no exceptions were included in the status
   * object.
   *
   * @throws ClientUserException If the response status contains a
   * {@link PluginUserException} instance.
   *
   * @throws DelayedResultException If the response status contains a
   * {@link DelayedResultException} instance.
   *
   * @throws ClientModelException If the response status contains any other
   * non-{@link RuntimeException} exception instance.
   */
  private static int processResponseStatus(ResponseStatus status)
  throws ClientUserException, DelayedResultException, ClientModelException {
    var exception = status.getException();

    if (exception == null)
      return status.getSignal();

    if (exception instanceof PluginUserException) {
      throw new ClientUserException(exception);
    }

    if (exception instanceof DelayedResultException) {
      throw (DelayedResultException) exception;
    }

    if (exception instanceof RuntimeException) {
      throw (RuntimeException) exception;
    }

    throw new ClientModelException(exception);
  }

  /**
   * Parses a string array from the json stream.
   *
   * @param parser JSON stream parser.
   *
   * @return A new string array containing the values read from the stream.
   *
   * @throws ClientModelException If an unexpected value is found in the stream
   * array.
   */
  private String[] readStreamArray(JsonParser parser) throws ClientModelException, IOException {
    while (true) {
      var jsonToken = parser.nextToken();

      if (jsonToken == JsonToken.END_ARRAY)
        break;

      if (jsonToken != JsonToken.VALUE_STRING)
        throw new ClientModelException("malformed result stream, expected VALUE_STRING, got: " + jsonToken);

      arrayBuffer.add(parser.getText());
    }

    // creates a new array using System.arrayCopy under the hood.
    var out = arrayBuffer.toArray(String[]::new);

    // clear out the buffer for the next row.
    arrayBuffer.clear();

    return out;
  }

  /**
   * Debug stats tracking.
   */
  private static class Stats {
    int rows = 0;
    int attachments = 0;
  }

}
