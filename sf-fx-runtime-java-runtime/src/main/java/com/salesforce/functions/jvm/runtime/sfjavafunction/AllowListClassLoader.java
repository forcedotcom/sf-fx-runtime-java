/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.functions.jvm.runtime.sfjavafunction;

import java.util.Arrays;
import java.util.List;

public class AllowListClassLoader extends ClassLoader {
    private final ClassLoader exposedClassLoader;
    private final List<String> allowList;

    public AllowListClassLoader(ClassLoader exposedClassLoader, ClassLoader parent, String... allowList) {
        super(parent);
        this.exposedClassLoader = exposedClassLoader;
        this.allowList = Arrays.asList(allowList.clone());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (String allowListItem : allowList) {
            if (name.startsWith(allowListItem)) {
                return exposedClassLoader.loadClass(name);
            }
        }

        return super.loadClass(name, resolve);
    }
}
