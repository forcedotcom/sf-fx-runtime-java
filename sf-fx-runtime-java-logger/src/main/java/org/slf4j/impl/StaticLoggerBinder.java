/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package org.slf4j.impl;

import com.salesforce.functions.jvm.runtime.logger.FunctionsLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class StaticLoggerBinder implements LoggerFactoryBinder {
    private final ILoggerFactory loggerFactory;

    public StaticLoggerBinder() {
        this.loggerFactory = new FunctionsLoggerFactory();
    }

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return FunctionsLoggerFactory.class.getName();
    }

    // To avoid constant folding by the compiler, this field must *not* be final!
    public static String REQUESTED_API_VERSION = "1.6.99";

    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
}
