package generator.apt.samples;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.util.Locale;

import static com.google.testing.compile.Compiler.javac;

@UtilityClass
public class APT {

    @SneakyThrows
    public Compilation compile(Processor processor, File...classFiles) {
        val compiler = javac()
                .withProcessors( processor );

        val javaFiles = new JavaFileObject[classFiles.length];
        for ( int i=0; i<classFiles.length; i++ ) {
            val classFile = classFiles[i];
            javaFiles[i] = JavaFileObjects.forResource( classFile.toURI().toURL() );
        }

        val compilation = compiler.compile( javaFiles );
        print( compilation.errors() );
        print( compilation.diagnostics() );
        print( compilation.notes() );
        print( compilation.warnings() );
        return compilation;
    }

    private void print(ImmutableList<Diagnostic<? extends JavaFileObject>> logs ) {
        logs.forEach( d -> {
            System.out.println( String.format( "%s %s (%s:%s)\n\t>%s", d.getKind(), d.getMessage( Locale.ENGLISH ), d.getLineNumber(), d.getColumnNumber(), d.getCode() ) );
        });
    }
}
