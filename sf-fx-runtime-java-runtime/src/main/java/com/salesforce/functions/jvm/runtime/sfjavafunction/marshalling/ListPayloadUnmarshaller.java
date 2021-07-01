/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.PayloadUnmarshallingException;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ListPayloadUnmarshaller implements PayloadUnmarshaller {
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

    return new Gson().fromJson(new String(data.toBytes(), StandardCharsets.UTF_8), List.class);
  }
}
