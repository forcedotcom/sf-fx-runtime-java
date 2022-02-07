/*
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.functions.jvm.runtime.sfjavafunction.marshalling;

import com.google.common.net.MediaType;
import com.salesforce.functions.jvm.runtime.sfjavafunction.exception.PayloadUnmarshallingException;
import io.cloudevents.CloudEvent;
import java.lang.reflect.Type;

public interface PayloadUnmarshaller {
  MediaType getHandledMediaType();

  Type getTargetType();

  Object unmarshall(CloudEvent cloudEvent) throws PayloadUnmarshallingException;
}
