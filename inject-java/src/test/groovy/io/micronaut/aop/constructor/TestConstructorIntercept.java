package io.micronaut.aop.constructor;

import io.micronaut.context.env.Environment;

import javax.inject.Singleton;

@Singleton
@TestConstructorAnn
public class TestConstructorIntercept {
    private final Environment environment;

    public TestConstructorIntercept(Environment environment) {
        this.environment = environment;
    }
}
