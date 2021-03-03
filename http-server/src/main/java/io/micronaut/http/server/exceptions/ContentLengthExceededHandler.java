/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.http.server.exceptions;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.exceptions.ContentLengthExceededException;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.server.exceptions.format.DefaultJsonErrorContext;
import io.micronaut.http.server.exceptions.format.JsonErrorContext;
import io.micronaut.http.server.exceptions.format.JsonErrorResponseFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Default handle for {@link ContentLengthExceededException} errors.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Singleton
@Produces
public class ContentLengthExceededHandler implements ExceptionHandler<ContentLengthExceededException, HttpResponse> {

    private final JsonErrorResponseFactory<?> responseFactory;

    @Deprecated
    public ContentLengthExceededHandler() {
        this.responseFactory = null;
    }

    @Inject
    public ContentLengthExceededHandler(JsonErrorResponseFactory<?> responseFactory) {
        this.responseFactory = responseFactory;
    }

    @Override
    public HttpResponse handle(HttpRequest request, ContentLengthExceededException exception) {
        Object error;
        HttpStatus status = HttpStatus.REQUEST_ENTITY_TOO_LARGE;
        if (responseFactory != null) {
            error = responseFactory.createResponse(JsonErrorContext.builder(request, status)
                    .cause(exception)
                    .errorMessage(exception.getMessage())
                    .build());
        } else {
            error = new JsonError(exception.getMessage())
                    .link(Link.SELF, Link.of(request.getUri()));
        }

        return HttpResponse
            .status(status)
            .body(error);
    }
}

