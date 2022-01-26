/*
 * Copyright (c) 2021 - Yupiik SAS - https://www.yupiik.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.yupiik.uship.tracing.servlet;

import io.yupiik.uship.tracing.span.Span;
import jakarta.servlet.http.HttpServletRequest;

import java.util.function.Supplier;

public class ServletContextAttributeEvaluator implements Supplier<Span> {
    private final HttpServletRequest request;

    public ServletContextAttributeEvaluator(final HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Span get() {
        return Span.class.cast(request.getAttribute(Span.class.getName()));
    }
}