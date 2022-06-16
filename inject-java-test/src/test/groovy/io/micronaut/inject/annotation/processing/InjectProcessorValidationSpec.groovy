package io.micronaut.inject.annotation.processing

import io.micronaut.annotation.processing.test.AbstractTypeElementSpec
import io.micronaut.core.beans.BeanIntrospection
import spock.lang.Ignore

/**
 * This is a spec to test the functionality implemented in
 * {@link io.micronaut.annotation.processing.BeanDefinitionInjectProcessor}
 * related to validation.
 *
 * In particular, the inject processor annotates elements with {@link javax.validation.Valid} if their generic
 * type parameters require validation (have constraints or are annotated with {@code @Valid}).
 * It also annotated methods with arguments that require validation as {@code io.micronaut.validation.Valid}
 */
class InjectProcessorValidationSpec extends AbstractTypeElementSpec {
    void "test annotate method with constrained parameters as validated"() {
        given:
        var definition = buildBeanDefinition('test.A', '''
package test;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;
import javax.validation.Valid;

@Introspected
@Singleton
public class A {
    @Executable
    public void giveString(@NotBlank String string) {}
    
    @Executable
    public void giveObject(@Valid B object) {}
}

@Introspected
class B {}
        ''')

        expect:
        definition != null

        when:
        var method1 = definition.findMethod("giveString", String)
        var method2 = definition.findPossibleMethods("giveObject").findFirst()

        then:
        method1.isPresent()
        method1.get().hasStereotype("io.micronaut.validation.Validated")

        method2.isPresent()
        method2.get().hasStereotype("io.micronaut.validation.Validated")
    }

    void "test do not annotate method without constrained parameters as validated"() {
        given:
        var definition = buildBeanDefinition('test.A', '''
package test;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.inject.Singleton;

@Introspected
@Singleton
public class A {
    @Executable
    public void giveString(String string) {}
    
    @Executable
    public void giveObject(B object) {}
}

@Introspected
class B {}
        ''')

        expect:
        definition != null

        when:
        var method1 = definition.findMethod("giveString", String)
        var method2 = definition.findPossibleMethods("giveObject").findFirst()

        then:
        method1.isPresent()
        !method1.get().hasStereotype("io.micronaut.validation.Validated")

        method2.isPresent()
        !method2.get().hasStereotype("io.micronaut.validation.Validated")
    }

    void "test annotate return type with constrained generic parameters as valid"() {
        given:
        var definition = buildBeanDefinition('test.A', '''
package test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.inject.Singleton;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Introspected
@Singleton
public class A {
    @Executable
    public List<@NotBlank String> getStringsList() { return null; }
    
    @Executable
    public Map<@NotBlank String, String> getStringMap() { return null; }
    
    @Executable
    public Map<String, @Min(1) Integer> getIntegerMap() { return null; }
    
    @Executable
    public Optional<@NotBlank String> getOptionalString() { return Optional.empty(); }
}
        ''')
        var method = definition.findMethod("getStringsList")

        expect:
        definition != null
        method.isPresent()
        method.get().getReturnType().annotationMetadata.hasStereotype("javax.validation.Valid")

        when:
        var method2 = definition.findMethod("getStringMap")
        var method3 = definition.findMethod("getIntegerMap")
        var method4 = definition.findMethod("getOptionalString")

        then:
        method2.isPresent()
        method2.get().getReturnType().annotationMetadata.hasStereotype("javax.validation.Valid")
        method3.isPresent()
        method3.get().getReturnType().annotationMetadata.hasStereotype("javax.validation.Valid")
        method4.isPresent()
        method4.get().getReturnType().annotationMetadata.hasStereotype("javax.validation.Valid")
    }

    void "test annotate return type with valid generic parameters as valid"() {
        given:
        var definition = buildBeanDefinition('test.A', '''
package test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;
import javax.validation.Valid;

@Introspected
@Singleton
public class A {
    @Executable
    public List<@Valid B> getList() { return null; }
    
    @Executable
    public Map<Integer, @Valid B> getMap() { return null; }
    
    @Executable
    public Optional<@Valid B> getOptional() { return Optional.empty(); }
}

@Introspected
class B {
    @NotBlank
    private String b;
    
    B(@NotBlank String b) { this.b = b; }
    @NotBlank String getB() { return b; }
}
        ''')
        expect:
        definition != null

        when:
        var method1 = definition.findMethod("getList")
        var method2 = definition.findMethod("getMap")
        var method3 = definition.findMethod("getOptional")

        then:
        method1.isPresent()
        method1.get().getReturnType().annotationMetadata.hasStereotype("javax.validation.Valid")
        method2.isPresent()
        method2.get().getReturnType().annotationMetadata.hasStereotype("javax.validation.Valid")
        method3.isPresent()
        method3.get().getReturnType().annotationMetadata.hasStereotype("javax.validation.Valid")
    }

    void "test annotate argument with constrained generic parameters as valid"() {
        given:
        var definition = buildBeanDefinition('test.A', '''
package test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.inject.Singleton;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.Valid;

@Introspected
@Singleton
public class A {
    @Executable
    public void putList(List<@NotBlank String> list) {}
    
    @Executable
    public void putMaps(Map<@NotBlank String, String> map, Map<Integer, @Max(10) Integer> map2) {}
    
    @Executable
    public void putObjectList(List<@Valid B> objectsList) {}
}

@Introspected
class B {}
        ''')

        expect:
        definition != null

        when:
        var method1 = definition.findMethod("putList", List<String>)
        var method2 = definition.findMethod("putMaps", Map<String, String>, Map<Integer, Integer>)
        var method3 = definition.findPossibleMethods("putObjectList").findFirst()

        then:
        method1.isPresent()
        method2.isPresent()
        method3.isPresent()

        when:
        var method1Arg1 = method1.get().getArguments()[0]
        var method2Arg1 = method2.get().getArguments()[0]
        var method2Arg2 = method2.get().getArguments()[1]
        var method3Arg1 = method3.get().getArguments()[0]

        then:
        method1.get().hasStereotype("io.micronaut.validation.Validated")
        method1Arg1.annotationMetadata.hasStereotype("javax.validation.Valid")
        !method1Arg1.annotationMetadata.hasStereotype("javax.validation.Constraint")
        method1Arg1.typeParameters[0].annotationMetadata.hasStereotype("javax.validation.Constraint")
        !method1Arg1.typeParameters[0].annotationMetadata.hasStereotype("javax.validation.Valid")

        method2.get().hasStereotype("io.micronaut.validation.Validated")
        method2Arg1.annotationMetadata.hasStereotype("javax.validation.Valid")
        method2Arg2.annotationMetadata.hasStereotype("javax.validation.Valid")

        method3.get().hasStereotype("io.micronaut.validation.Validated")
        method3Arg1.annotationMetadata.hasStereotype("javax.validation.Valid")
        method3Arg1.getTypeParameters()[0].annotationMetadata.hasStereotype("javax.validation.Valid")
    }

//    @Ignore("TODO")
    void "test annotate argument with nested constrained generic parameters as valid"() {
        given:
        var definition = buildBeanDefinition('test.A', '''
package test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.inject.Singleton;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.Valid;

@Introspected
@Singleton
public class A {
    @Executable
    public void putMap(Map<String, List<@Min(10) Float>> map) {}
    
    @Executable
    public void putDoubleMap(Map<String, Map<String, Optional<@Min(0) Integer>>> map) {}
    
    @Executable
    public List<List<@Valid B>> getDoubleList() { return null; }
}

@Introspected
class B {}
        ''')

        expect:
        definition != null

        when:
        var method1 = definition.findPossibleMethods("putMap").findFirst()
        var method2 = definition.findPossibleMethods("putDoubleMap").findFirst()
        var method3 = definition.findPossibleMethods("getDoubleList").findFirst()

        then:
        method1.isPresent()
        method2.isPresent()
        method3.isPresent()

        when:
        var arg1 = method1.get().getArguments()[0]
        var arg2 = method2.get().getArguments()[0]
        var returnType = method3.get().getReturnType()

        then:
        method1.get().hasStereotype("io.micronaut.validation.Validated")
        arg1.annotationMetadata.hasStereotype("javax.validation.Valid")
        !arg1.getTypeParameters()[0].annotationMetadata.hasStereotype("javax.validation.Valid")
        arg1.getTypeParameters()[1].annotationMetadata.hasStereotype("javax.validation.Valid")
        arg1.typeParameters[1].typeParameters[0].annotationMetadata.hasStereotype("javax.validation.Constraint")

        method2.get().hasStereotype("io.micronaut.validation.Validated")
        arg2.annotationMetadata.hasStereotype("javax.validation.Valid")
        !arg2.typeParameters[0].annotationMetadata.hasStereotype("javax.validation.Valid")
        arg2.typeParameters[1].annotationMetadata.hasStereotype("javax.validation.Valid")
        !arg2.typeParameters[1].typeParameters[0].annotationMetadata.hasStereotype("javax.validation.Valid")
        arg2.typeParameters[1].typeParameters[1].annotationMetadata.hasStereotype("javax.validation.Valid")
        arg2.typeParameters[1].typeParameters[1].typeParameters[0]
                .annotationMetadata.hasStereotype("javax.validation.Constraint")

        !method3.get().hasStereotype("io.micronaut.validation.Validated")
        returnType.annotationMetadata.hasStereotype("javax.validation.Valid")
        returnType.typeParameters[0].annotationMetadata.hasStereotype("javax.validation.Valid")
        returnType.typeParameters[0].typeParameters[0].annotationMetadata.hasStereotype("javax.validation.Valid")
    }

    @Ignore("GenericPlaceholderElement is missing annotations")
    void "test validation in cyclic generics"() {
        given:
        var definition = buildBeanDefinition('test.A','''
package test;

import java.util.List;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.validation.Valid;
import javax.inject.Singleton;

@Introspected
@Singleton
@SuppressWarnings("rawtypes")
class A<T extends B> {
    @Valid
    private T child;
    
    @Executable
    public @Valid T getChild() { return child; } 
    
    @Executable
    public void setChildren(List<@Valid T> children) {}
}

@SuppressWarnings("rawtypes")
class B<T extends A> {}
        ''')

        expect:
        definition != null

        when:
        var method1 = definition.findMethod("getChild")
        var method2 = definition.findPossibleMethods("setChildren").findFirst()

        then:
        method1.isPresent()
        method1.get().returnType.annotationMetadata.hasStereotype("javax.validation.Valid")

        method2.isPresent()
        method2.get().hasStereotype("io.micronaut.validation.Validated")
        method2.get().getArguments()[0].annotationMetadata.hasStereotype("javax.validation.Valid")
    }

    void "test cyclic generics do not cause infinite loop"() {
        given:
        var introspection = buildBeanIntrospection('test.A','''
package test;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.inject.Singleton;

@Introspected
@Singleton
@SuppressWarnings("rawtypes")
class A<T extends B> {
    private T child;
    
    @Executable
    public T getChild() { return child; } 
}

@SuppressWarnings("rawtypes")
class B<T extends A> {}
        ''')

        expect:
        introspection != null
    }

    void "test cyclic generics do not cause infinite loop 2"() {
        given:
        BeanIntrospection introspection = buildBeanIntrospection('test.A','''
package test;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.inject.Singleton;
import javax.validation.Valid;

@Introspected
@Singleton
class A<T extends A<T, P>, P extends B<T, P>> {
    @Valid
    private P child;
    
    @Executable
    public P getChild() {
        return child;
    } 
}

class B<T extends A<T, P>, P extends B<T, P>> {
    @Valid
    private T parent;
    
    @Executable
    public T getParent() {
        return parent;
    }
}
        ''')

        expect:
        introspection != null
    }

    @Ignore("TODO")
    void "test error for non-introspected types"() {
        when:
        BeanIntrospection introspection = buildBeanIntrospection('test.A','''
package test;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.inject.Singleton;
import javax.validation.Valid;

@Introspected
@Singleton
class A {
    @Executable
    public @Valid B getChild() {
        return child;
    } 
}

class B {}
        ''')

        then:
        thrown(Exception)
    }
}
