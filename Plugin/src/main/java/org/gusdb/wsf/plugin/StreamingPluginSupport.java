package org.gusdb.wsf.plugin;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.NullNode;

import javax.annotation.Nullable;

import static org.gusdb.fgputil.json.JsonUtil.Jackson;

/**
 * Helpers for working with JSON stream based plugins.
 */
public final class StreamingPluginSupport {
  private StreamingPluginSupport() {}

  /**
   * JSON reader pre-configured to quickly deserialize Java Exception types.
   */
  public static final ObjectReader EXCEPTION_READER;

  /**
   * JSON writer pre-configured to quickly serialize Java Exception types.
   */
  public static final ObjectWriter EXCEPTION_WRITER;

  static {
    // Create a mapper that serializes exception type info
    var mapper = Jackson.copy()
      .addMixIn(Exception.class, ExceptionMixin.class);

    EXCEPTION_READER = mapper.readerFor(Exception.class)
      .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    EXCEPTION_WRITER = mapper.writerFor(Exception.class);
  }

  /**
   * Tests if the given {@link JsonNode} is either a {@code null} value,
   * representing the absence of a value, or a {@link NullNode} instance,
   * representing an explicitly set {@code null} literal.
   *
   * @param node JSON node value to test.
   *
   * @return {@code true} if the given node is {@code null} or represents a
   * literal JSON {@code null}, otherwise {@code false}.
   */
  public static boolean isNull(@Nullable JsonNode node) {
    return node == null || node instanceof NullNode;
  }

  /**
   * Mixin type to tell Jackson how to (de)serialize exceptions polymorphically.
   */
  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
  private static class ExceptionMixin {}
}
