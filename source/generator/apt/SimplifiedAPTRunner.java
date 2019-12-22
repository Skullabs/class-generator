package generator.apt;

import lombok.Value;
import lombok.val;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import javax.annotation.processing.Processor;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

import static java.util.Arrays.asList;

public class SimplifiedAPTRunner {

	final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
	final List<String> compilerOptionsForProcOnly = asList( "-proc:only" );

	final JavaCompiler compiler;
	final StandardJavaFileManager fileManager;
	final Config config;

	public SimplifiedAPTRunner(Config config, JavaCompiler compiler ) {
		this.config = config;
		this.compiler = compiler;
		this.fileManager = createFileManager();
	}

	public APTResult run( Processor processor, JavaFileObject... compilationUnits ) {
		final List<JavaFileObject> compilationUnitsAsList = asList( compilationUnits );
		return run( compilationUnitsAsList, Collections.singletonList(processor));
	}

	public APTResult run(Iterable<? extends JavaFileObject> compilationUnits, Iterable<? extends Processor> processors) {
		final CompilationTask task = compiler.getTask( null, fileManager, diagnostics, compilerOptionsForProcOnly, null, compilationUnits );
		task.setProcessors( processors );
		final boolean success = task.call();
		final List<Diagnostic<? extends JavaFileObject>> generatedDiagnostics = diagnostics.getDiagnostics();
		return new APTResult( success, generatedDiagnostics );
	}

	private StandardJavaFileManager createFileManager() {
		try {
			ensureThatConfigDirectoriesExists( config );
			final StandardJavaFileManager fileManager = compiler.getStandardFileManager( diagnostics, null, null );
			if ( config.classPath != null )
				fileManager.setLocation( StandardLocation.CLASS_PATH, config.classPath );
			fileManager.setLocation( StandardLocation.CLASS_OUTPUT, config.classOutputDir );
			fileManager.setLocation( StandardLocation.SOURCE_PATH, config.sourceDir );
			fileManager.setLocation( StandardLocation.SOURCE_OUTPUT, config.outputDir );
			return fileManager;
		} catch ( final IOException e ) {
			throw new IllegalStateException( e );
		}
	}

	static void ensureThatConfigDirectoriesExists( Config config ) {
		ensureThatConfigDirectoryExists( config.sourceDir, false );
		ensureThatConfigDirectoryExists( config.outputDir, true );
		ensureThatConfigDirectoryExists( config.classOutputDir, true );
	}

	static void ensureThatConfigDirectoryExists( List<File> dirs, boolean forceCreate ) {
		for ( final File dir : dirs )
			ensureThatConfigDirectoryExists( dir, forceCreate );
	}

	static void ensureThatConfigDirectoryExists( File dir, boolean forceCreate ) {
		if ( !dir.exists() )
			if ( !forceCreate || !dir.mkdirs() )
				throw new IllegalStateException( "Directory does not exists (or could not be created): " + dir );
	}

	static public class Config {

		public List<File> sourceDir = asList( file( "source" ), file( "src/main/java" ) );
		public List<File> outputDir = asList( file( "target" ), file( "output" ) );
		public List<File> classOutputDir = outputDir;
		public List<File> classPath;

		private static File file( String path ) {
			return new File( path );
		}
	}

	@Value
	static public class APTResult {

		final boolean success;
		final List<Diagnostic<? extends JavaFileObject>> diagnostics;

		public void printErrorsIfAny(){
			printErrorsIfAny(System.out::println);
		}

		public void printErrorsIfAny( Consumer<Diagnostic<? extends JavaFileObject>> writer ) {
            for (final Diagnostic<? extends JavaFileObject> diagnostic : getDiagnostics())
                writer.accept(diagnostic);
		}

		public void failInCaseOfError() {
            failInCaseOfError(d -> {
				System.out.println(d);
				if (d.getKind().equals(Diagnostic.Kind.ERROR)) {
					throw new IllegalStateException(d.getMessage(Locale.getDefault()));
				}
			});
		}

		public void failInCaseOfError( Consumer<Diagnostic<? extends JavaFileObject>> writer ) {
            for (final Diagnostic<? extends JavaFileObject> diagnostic : getDiagnostics())
                writer.accept(diagnostic);
		}
	}

	static public class LocalJavaSource extends SimpleJavaFileObject {

		final File file;

		public LocalJavaSource(File file) {
			super(file.toURI(), Kind.SOURCE);
			this.file = file;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			val bytes = Files.readAllBytes( file.toPath() );
			return new String( bytes, Charset.defaultCharset() );
		}
	}
}