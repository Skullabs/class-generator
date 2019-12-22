package generator.apt.samples;

import generator.apt.SimplifiedAST;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class TypeTest {

    final SimplifiedAST.Type type = new SimplifiedAST.Type();

    @EnabledOnJre(JRE.JAVA_8)
    @Test @DisplayName("SHOULD load generated class")
    void canLoadGeneratedClassForJdk8OrInferior(){
        val jdkGeneratedAnnotation = type.getJdkGeneratedAnnotation();
        assertEquals("javax.annotation.Generated", jdkGeneratedAnnotation);
    }

    @DisabledOnJre({JRE.JAVA_8, JRE.OTHER})
    @Test @DisplayName("SHOULD load generated class")
    void canLoadGeneratedClassForJdk9OrSuperior(){
        val jdkGeneratedAnnotation = type.getJdkGeneratedAnnotation();
        assertEquals("javax.annotation.processing.Generated", jdkGeneratedAnnotation);
    }

    @Test @DisplayName("SHOULD load generated class")
    void canLoadGeneratedClass(){
        val loaded = annotationClassFrom(type.getJdkGeneratedAnnotation());
        assertNotNull(loaded);
    }

    @SneakyThrows
    <T extends Annotation> Class<T> annotationClassFrom(String canonicalName){
        return (Class<T>) Class.forName(canonicalName);
    }
}
