package io.micronaut.inject.visitor

import io.micronaut.annotation.processing.test.AbstractTypeElementSpec
import io.micronaut.core.beans.BeanIntrospection
import spock.lang.Ignore

class ClassElementAnnotationValidationSpec extends AbstractTypeElementSpec {
    void "test annotations on type arguments are preserved"() {
        given:
        var classElement = buildClassElement('''
package test;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.inject.Singleton;
import java.util.List;

@Introspected
@Singleton
class Test {
    public List<String> list1;
    public List<String> list2;

    @Executable
    public List<String> getList1() { return null; }

    @Executable
    public List<String> getList2() { return null; }
}
        ''')

        var list1 = classElement.getBeanProperties().get(0)
        var list2 = classElement.getBeanProperties().get(1)
        var myAnnotation = "test.MyAnnotation"

        expect:
        list1.getName() == "list1"
        list2.getName() == "list2"
        !list1.hasStereotype(myAnnotation)
        !list2.hasStereotype(myAnnotation)

        when:
        var list1arg = list1.getType().getTypeArguments().get("E")
        var list2arg = list2.getType().getTypeArguments().get("E")
        list1arg.annotate(myAnnotation)

        then:
        list1.getType().getTypeArguments().get("E").hasStereotype(myAnnotation)
        list1arg.hasStereotype(myAnnotation)
        !list2arg.hasStereotype(myAnnotation)
    }

    void "test annotations on type arguments are preserved 2"() {
        given:
        var classElement = buildClassElement('''
package test;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.context.annotation.Executable;
import javax.inject.Singleton;
import java.util.List;

@Introspected
@Singleton
class Test {
    public List<String> list1;
    public List<String> list2;

    @Executable
    public List<String> getList1() { return null; }

    @Executable
    public List<String> getList2() { return null; }
}
        ''')

        var list1 = classElement.getBeanProperties().get(0)
        var list2 = classElement.getBeanProperties().get(1)
        var myAnnotation = "test.MyAnnotation"

        expect:
        list1.getName() == "list1"
        list2.getName() == "list2"
        !list1.hasStereotype(myAnnotation)
        !list2.hasStereotype(myAnnotation)

        when:
        var list1arg = list1.getType().getTypeArguments().get("E")
        list1arg.annotate(myAnnotation)

        then:
        list1.getType().getTypeArguments().get("E").hasStereotype(myAnnotation)
        list1arg.hasStereotype(myAnnotation)

        when:
        var list2arg = list2.getType().getTypeArguments().get("E")

        then:
        !list2arg.hasStereotype(myAnnotation)
    }
}
