package generator.apt.samples;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertEquals( Superclass.class.getCanonicalName(), type.getSuperclass().getCanonicalName() );

        assertEquals( 1, type.getInterfaces().size() );
        assertEquals( Interface.class.getCanonicalName(), type.getInterfaces().get(0).getCanonicalName() );

        assertEquals( 2, type.getInheritedInterfaces().size() );
        assertEquals( Interface.class.getCanonicalName(), type.getInheritedInterfaces().get(0).getCanonicalName() );
        assertEquals( Runnable.class.getCanonicalName(), type.getInheritedInterfaces().get(1).getCanonicalName() );

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
        assertEquals( "\"p1\"", firstParameter.getAnnotations().get(0).getValue() );
    }

    @DisplayName("SHOULD memorize All Args lombok constructors")
    @Test void process1() {
        val annotatedClass = new File( "tests/generator/apt/samples/LombokAnnotatedClass.java" );
        APT.compile( processor, annotatedClass );

        val type = processor.types.get(0);

        val constructor = type.getMethods().get(2);
        assertTrue( constructor.isConstructor() );
        assertEquals( 3, constructor.getParameters().size() );
        
        assertEquals( "name", constructor.getParameters().get(0).getName() );
        assertEquals( "surname", constructor.getParameters().get(1).getName() );
        assertEquals( "address", constructor.getParameters().get(2).getName() );
    }

    @DisplayName("SHOULD memorize RequiredArgs lombok constructors")
    @Test void process2() {
        val annotatedClass = new File( "tests/generator/apt/samples/LombokAnnotatedClass.java" );
        APT.compile( processor, annotatedClass );

        val type = processor.types.get(0);

        val constructor = type.getMethods().get(1);
        assertTrue( constructor.isConstructor() );
        assertEquals( 2, constructor.getParameters().size() );
        
        assertEquals( "name", constructor.getParameters().get(0).getName() );
        assertEquals( "surname", constructor.getParameters().get(1).getName() );
    }

    @DisplayName("SHOULD memorize NoArgs lombok constructors")
    @Test void process3() {
        val annotatedClass = new File( "tests/generator/apt/samples/LombokAnnotatedClass.java" );
        APT.compile( processor, annotatedClass );

        val type = processor.types.get(1);

        val constructor = type.getMethods().get(1);
        assertTrue( constructor.isConstructor() );
        assertEquals( 0, constructor.getParameters().size() );
    }
}
