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
package io.micronaut.inject.constructor.factoryinjection;

import io.micronaut.context.annotation.Factory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Factory
public class AProvider implements Provider<A> {
    final C c;
    @Inject
    C another;

    @Inject
    protected D d;

    @Inject
    public AProvider(C c) {
        this.c = c;
    }

    @Override
    @Singleton
    public A get() {
        final AImpl a = new AImpl(c, another);
        a.d = d;
        return a;
    }
}
