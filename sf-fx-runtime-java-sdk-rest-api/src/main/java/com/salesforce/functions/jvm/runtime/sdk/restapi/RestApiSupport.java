/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sdk.restapi;

import com.google.gson.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

public class RestApiSupport {
  public static boolean isOK(HttpResponse response) {
    int statusCode = response.getStatusLine().getStatusCode();
    return statusCode >= 200 && statusCode < 300;
  }

  public static JsonElement parseJson(HttpResponse response)
      throws IOException, RestApiException {
    return parseJson(response, JsonElement.class);
  }

  public static <T> T parseJson(HttpResponse response, Class<T> bindTo)
      throws IOException, RestApiException {
    return parseJson(response, bindTo, new Gson());
  }

  public static <T> T parseJson(
      HttpResponse response, Class<T> bindTo, Function<GsonBuilder, GsonBuilder> customizer)
      throws IOException, RestApiException {
    return parseJson(response, bindTo, customizer.apply(new GsonBuilder()).create());
  }

  public static <T> T parseJson(HttpResponse response, Class<T> bindTo, Gson gson)
      throws RestApiException, IOException {
    String body = getJsonContent(response);
    try {
      return gson.fromJson(body, bindTo);
    } catch (JsonSyntaxException e) {
      throw new RestApiException("Could not parse API response as JSON!\n" + body, e);
    }
  }

  public static RestApiErrorsException parseJsonErrors(HttpResponse response)
      throws RestApiException {
    String body = null;
    try {
      body = getJsonContent(response);
      JsonElement json = new Gson().fromJson(body, JsonElement.class);
      List<RestApiError> apiErrors = new ArrayList<>();

      for (JsonElement apiErrorElement : json.getAsJsonArray()) {
        JsonObject object = apiErrorElement.getAsJsonObject();
        String message = object.get("message").getAsString();
        String errorCode = object.get("errorCode").getAsString();

        List<String> fields = new ArrayList<>();
        JsonElement fieldsJsonElement = object.get("fields");
        if (fieldsJsonElement != null) {
          for (JsonElement field : fieldsJsonElement.getAsJsonArray()) {
            fields.add(field.getAsString());
          }
        }

        apiErrors.add(new RestApiError(message, errorCode, fields));
      }

      return new RestApiErrorsException(apiErrors);
    } catch (Exception e) {
      String errorMessage = "Could not parse API response as JSON!";
      if (body != null && !body.isEmpty()) {
        errorMessage += "\n" + body;
      }
      throw new RestApiException(errorMessage, e);
    }
  }

  private static String getJsonContent(HttpResponse response) throws IOException {
    if (response.getEntity() == null) {
      throw new IOException("Response has no JSON body to parse");
    }

    ContentType contentType = ContentType.get(response.getEntity());
    if (contentType == null) {
      throw new IOException("Response has no Content-Type");
    }

    return EntityUtils.toString(response.getEntity(), contentType.getCharset());
  }

  private static final CSVFormat csvFormat =
      CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).build();

  public static <T> Iterable<T> parseCsv(
      HttpResponse response, Function<CSVRecord, T> rowTransformer) throws IOException {
    if (response.getStatusLine().getStatusCode() == 204) {
      return Collections.emptyList();
    }

    CSVParser parser = csvFormat.parse(new InputStreamReader(response.getEntity().getContent()));
    return () ->
        new Iterator<T>() {
          final Iterator<CSVRecord> iterator = parser.iterator();

          @Override
          public boolean hasNext() {
            return iterator.hasNext();
          }

          @Override
          public T next() {
            return rowTransformer.apply(iterator.next());
          }
        };
  }
}
