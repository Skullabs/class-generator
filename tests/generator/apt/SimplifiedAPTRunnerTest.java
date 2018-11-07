package generator.apt;

import com.google.testing.compile.JavaFileObjects;
import generator.apt.samples.IgnoredProcessor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplifiedAPTRunnerTest {

	static final String SOURCE_DIR = "tests-resources";
	static final String OUTPUT_DIR = "output";

	SimplifiedAPTRunner runner;

	@Before
	public void setup() {
		final SimplifiedAPTRunner.Config config = new SimplifiedAPTRunner.Config();
		config.sourceDir = asList( new File( SOURCE_DIR ) );
		config.outputDir = asList( new File( OUTPUT_DIR ) );
		config.classOutputDir = asList( new File( OUTPUT_DIR ) );
		runner = new SimplifiedAPTRunner( config, ToolProvider.getSystemJavaCompiler() );
	}

	@Test @SneakyThrows
	public void example() throws IOException {
	    val clazz = new File( "tests/generator/apt/samples/AnnotatedClass.java" );
		val source = new SimplifiedAPTRunner.LocalJavaSource( clazz );
		val ignoredProcessor = new IgnoredProcessor();

		val result = runner.run( ignoredProcessor, source );
        result.printErrorsIfAny();
		assertTrue(result.isSuccess());

        val type = ignoredProcessor.getTypes().get(0);
        assertEquals( "generator.apt.samples.AnnotatedClass", type.getCanonicalName() );
        assertEquals( 1, type.getFields().size() );
        assertEquals( 2, type.getMethods().size() );
	}
}