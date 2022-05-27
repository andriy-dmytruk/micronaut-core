package io.micronaut.validation

import io.micronaut.annotation.processing.test.AbstractTypeElementSpec
import io.micronaut.aop.Around
import io.micronaut.inject.ProxyBeanDefinition
import io.micronaut.inject.writer.BeanDefinitionVisitor
import io.micronaut.inject.writer.BeanDefinitionWriter

import javax.validation.Constraint
import javax.validation.Valid
import java.time.LocalDate

class ValidatedParseSpec extends AbstractTypeElementSpec {
    void "test constraints on beans make them @Validated"() {
        given:
        def definition = buildBeanDefinition('test.$Test' + BeanDefinitionWriter.CLASS_SUFFIX + BeanDefinitionVisitor.PROXY_SUFFIX,'''
package test;

@jakarta.inject.Singleton
class Test {

    @io.micronaut.context.annotation.Executable
    public void setName(@javax.validation.constraints.NotBlank String name) {
    
    }
    
    @io.micronaut.context.annotation.Executable
    public void setName2(@javax.validation.Valid String name) {
    
    }
}
''')

        expect:
        definition instanceof ProxyBeanDefinition
        definition.findMethod("setName", String).get().hasStereotype(Validated)
        definition.findMethod("setName2", String).get().getAnnotationTypesByStereotype(Around).contains(Validated)
    }

    void "test constraints on a declarative client makes it @Validated"() {
        given:
        def definition = buildBeanDefinition('test.ExchangeRates' + BeanDefinitionVisitor.PROXY_SUFFIX,'''
package test;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Client("https://exchangeratesapi.io")
interface ExchangeRates {

    @Get("{date}")
    String rate(@PastOrPresent LocalDate date);
}
''')

        expect:
        definition.findMethod("rate", LocalDate).get().hasStereotype(Validated)
    }

    void "test constraints on list generic parameters make method parameters @Validated"() {
        given:
        def definition = buildBeanDefinition('test.Test','''
package test;

import java.util.List;
import javax.validation.constraints.NotBlank;

@jakarta.inject.Singleton
class Test {
    @io.micronaut.context.annotation.Executable
    public void setList(List<@NotBlank String> list) {
    
    }
}
''')
        when:
        def method = definition.findMethod("setList", List<String>);

        then:
        method.isPresent()
        method.get().hasStereotype(Validated.class)
        method.get().getArguments().size() == 1

        when:
        def argument = method.get().getArguments()[0]
        def argumentMetadata = argument.getAnnotationMetadata()

        then:
        argumentMetadata.hasStereotype(Valid.class)
    }

    void "test constraints on list generic parameters make method value @Validated"() {
        given:
        def definition = buildBeanDefinition('test.Test','''
package test;

import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import io.micronaut.context.annotation.Executable;

@jakarta.inject.Singleton
class Test {

    @Executable
    public @Min(value=10) Integer getValue() {
        return 1;
    }
    
    @Executable
    public List<@NotNull String> getStrings() {
        return null;
    }
}
''')
        var method = definition.findMethod("getValue")

        expect:
        method.isPresent()
        //method.get().hasStereotype(Validated.class)
        method.get().hasStereotype(Constraint.class)

        when:
        var method2 = definition.findMethod("getStrings")

        then:
        method2.isPresent()
        //method.get().hasStereotype(Validated.class)
        method2.get().hasStereotype(Valid.class)
    }
}
