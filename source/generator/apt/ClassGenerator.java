package generator.apt;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.Writer;

@RequiredArgsConstructor
public class ClassGenerator {

    final MustacheFactory mf = new DefaultMustacheFactory();
    final String templateName;

    public void write( Writer writer, SimplifiedAST.Type type ){
        val mustache = mf.compile(templateName);
        mustache.execute( writer, type );
    }

    public static ClassGenerator with( String templateName ) {
        return new ClassGenerator( templateName );
    }
}
