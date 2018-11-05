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

import static org.junit.jupiter.api.Assertions.*;

class ClassGeneratorTest {

    static final String ANNOTATED_CLASS = "tests/generator/apt/samples/AnnotatedClass.java";

    @DisplayName("Should be able to store parameters")
    @Test void set(){
        val generator = ClassGenerator.with("delegate-class.mustache");
        generator.set("data", new SimplifiedAST.Type() );

        val data = generator.params.get("data");
        assertEquals( new SimplifiedAST.Type(), data );
    }

    @DisplayName("Parameters with no keys will be stored in the 'type' key")
    @Test void set1(){
        val generator = ClassGenerator.with("delegate-class.mustache");
        generator.set(new SimplifiedAST.Type() );

        val data = generator.params.get("type");
        assertEquals( new SimplifiedAST.Type(), data );
    }

    @SneakyThrows
    @DisplayName("Should be able to generate a class filled with the expected parameters")
    @Test void write(){
        val expectedClassBytes = Files.readAllBytes( Paths.get("tests-resources/expected-generated-class.java") );
        val expectedClass = new String( expectedClassBytes, "UTF-8" );

        val generatedClass = new StringWriter();

        ClassGenerator.with("delegate-class.mustache")
                .set( readMethodsAndFieldsIgnoredInTheAnnotatedClass() )
                .write( generatedClass );

        assertEquals( expectedClass, generatedClass.toString() );
    }

    SimplifiedAST.Type readMethodsAndFieldsIgnoredInTheAnnotatedClass(){
        val processor = new IgnoredProcessor();
        val annotatedClass = new File( ANNOTATED_CLASS );
        APT.compile( processor, annotatedClass );
        return processor.getTypes().get(0);
    }
}