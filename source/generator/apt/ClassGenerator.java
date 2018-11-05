package generator.apt;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.RequiredArgsConstructor;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ClassGenerator {

    final Map<String, Object> params = new HashMap<>();
    final Mustache template;

    public ClassGenerator set(SimplifiedAST.Type type) {
        return set( "type", type );
    }

    public ClassGenerator set( String key, SimplifiedAST.Type type ){
        params.put( key, type );
        return this;
    }

    public void write( Writer writer ) {
        template.execute(writer, params);
    }

    public static ClassGenerator with( String templateName ) {
        final MustacheFactory mf = new DefaultMustacheFactory();
        return new ClassGenerator( mf.compile( templateName ) );
    }
}
