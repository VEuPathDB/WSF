package org.gusdb.wsf.plugin;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import static org.gusdb.fgputil.json.JsonUtil.Jackson;

public final class PluginSupport {
  private PluginSupport() {}

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
   * Mixin type to tell Jackson how to (de)serialize exceptions polymorphically.
   */
  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
  private static class ExceptionMixin {}
}
