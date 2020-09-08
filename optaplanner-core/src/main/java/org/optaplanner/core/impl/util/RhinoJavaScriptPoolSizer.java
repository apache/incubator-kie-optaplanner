/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.util;

import java.util.function.IntSupplier;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.optaplanner.core.config.util.ConfigUtils;

/**
 * This class exists separately, as Rhino is an optional dependency and the imports would have immediately triggered
 * {@link ClassNotFoundException}s if present on {@link ConfigUtils} directly.
 */
public final class RhinoJavaScriptPoolSizer implements IntSupplier {

    private final String propertyName;
    private final String script;

    public RhinoJavaScriptPoolSizer(String propertyName, String script) {
        this.propertyName = propertyName;
        this.script = script;
    }

    @Override
    public int getAsInt() {
        String actualScript = "var availableProcessorCount = " + Runtime.getRuntime().availableProcessors() + ";\n"
                + script;
        Context cx = ContextFactory.getGlobal().enterContext();
        try {
            Scriptable scope = cx.initStandardObjects();
            Object result = cx.evaluateString(scope, actualScript, "source", 1, null);
            return ((Double) result).intValue();
        } catch (RhinoException e) {
            throw new IllegalArgumentException("The " + propertyName + " (" + script
                    + ") cannot be parsed in JavaScript with the variables ([" + ConfigUtils.AVAILABLE_PROCESSOR_COUNT + "]).",
                    e);
        } finally {
            Context.exit();
        }
    }
}
