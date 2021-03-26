/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.logger;


import java.util.regex.Pattern;

import static com.salesforce.functions.jvm.runtime.logger.Constants.LOGGER_NAME_SEGMENT_DELIMITER;

public class Utils {

    public static String shortenLoggerName(String loggerName, int maxLength) {
        String[] loggerNameSegments = loggerName.split(Pattern.quote(LOGGER_NAME_SEGMENT_DELIMITER));

        String result = "";
        for (int i = 0; i < loggerNameSegments.length; i++) {
            result = String.join(LOGGER_NAME_SEGMENT_DELIMITER, loggerNameSegments);

            if (result.length() <= maxLength) {
                break;
            }

            if (loggerNameSegments[i].length() > 1) {
                loggerNameSegments[i] = loggerNameSegments[i].substring(0, 1);
            }
        }

        return result;
    }
}
