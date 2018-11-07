package generator.apt;

import generator.apt.samples.APT;
import generator.apt.samples.IgnoredProcessor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassGeneratorTest {

    static final String ANNOTATED_CLASS = "tests/generator/apt/samples/AnnotatedClass.java";

    @SneakyThrows
    @DisplayName("Should be able to generate a class filled with the expected parameters")
    @Test void write(){
        val expectedClassBytes = Files.readAllBytes( Paths.get("tests-resources/expected-generated-class.java") );
        val expectedClass = new String( expectedClassBytes, "UTF-8" );

        val generatedClass = new StringWriter();

        ClassGenerator.with("delegate-class.mustache")
                .write( generatedClass, readMethodsAndFieldsIgnoredInTheAnnotatedClass() );

        assertEquals( expectedClass, generatedClass.toString() );
    }

    SimplifiedAST.Type readMethodsAndFieldsIgnoredInTheAnnotatedClass(){
        val processor = new IgnoredProcessor();
        val annotatedClass = new File( ANNOTATED_CLASS );
        APT.compile( processor, annotatedClass );
        return processor.getTypes().get(0);
    }
}