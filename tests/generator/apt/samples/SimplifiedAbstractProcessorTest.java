package generator.apt.samples;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SimplifiedAbstractProcessorTest {

    IgnoredProcessor processor = new IgnoredProcessor();

    @DisplayName("Is possible to memorize elements with specific Annotation.")
    @Test void process(){
        val annotatedClass = new File( "tests/generator/apt/samples/AnnotatedClass.java" );
        APT.compile( processor, annotatedClass );

        assertEquals( 1, processor.types.size() );

        val type = processor.types.get(0);
        assertEquals( "generator.apt.samples.AnnotatedClass", type.getCanonicalName() );
        assertEquals( 1, type.getFields().size() );
        assertEquals( 2, type.getMethods().size() );

        val ignoredField = type.getFields().get(0);
        assertEquals( "name", ignoredField.getName() );
        assertEquals( String.class.getCanonicalName(), ignoredField.getType() );
        assertEquals( 1, ignoredField.getAnnotations().size() );
        assertEquals( "@generator.apt.samples.Ignored()", ignoredField.getAnnotations().get(0).toString() );

        val ignoredMethod = type.getMethods().get(1);
        assertEquals( "sum", ignoredMethod.getName() );
        assertEquals( 2, ignoredMethod.getParameters().size() );

        val firstParameter = ignoredMethod.getParameters().get(0);
        assertEquals( 1, firstParameter.getAnnotations().size() );
        assertEquals( "@generator.apt.samples.Important(value=\"p1\")", firstParameter.getAnnotations().get(0).toString() );
    }
}
