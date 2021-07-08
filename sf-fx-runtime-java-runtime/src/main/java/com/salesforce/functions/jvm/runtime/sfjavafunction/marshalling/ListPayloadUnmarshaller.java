/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.json.JsonLibrary;
import com.salesforce.functions.jvm.runtime.json.JsonLibraryDetector;
import com.salesforce.functions.jvm.runtime.json.ListParameterizedType;
import com.salesforce.functions.jvm.runtime.json.exception.AmbiguousJsonLibraryException;
import com.salesforce.functions.jvm.runtime.json.exception.JsonDeserializationException;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.PayloadUnmarshallingException;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ListPayloadUnmarshaller<A> implements PayloadUnmarshaller {
  private final Class<A> clazz;
  private final JsonLibrary jsonLibrary;

  public ListPayloadUnmarshaller(Class<A> clazz) throws AmbiguousJsonLibraryException {
    this.clazz = clazz;
    this.jsonLibrary = JsonLibraryDetector.detect(clazz);
  }

  @Override
  public MediaType getHandledMediaType() {
    return MediaType.JSON_UTF_8;
  }

  @Override
  public Class<?> getTargetClass() {
    return List.class;
  }

  @Override
  public Object unmarshall(CloudEvent cloudEvent) throws PayloadUnmarshallingException {

    CloudEventData data = cloudEvent.getData();
    if (data == null) {
      throw new PayloadUnmarshallingException("No data given");
    }
    Type type = new ListParameterizedType(clazz);
    try {

      return jsonLibrary.deserializeListAt(
          new String(data.toBytes(), StandardCharsets.UTF_8), clazz);
    } catch (JsonDeserializationException e) {
      throw new PayloadUnmarshallingException("Could not unmarshall payload!", e);
    }
  }
}
