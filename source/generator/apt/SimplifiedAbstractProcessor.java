package generator.apt;

import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@RequiredArgsConstructor
public abstract class SimplifiedAbstractProcessor extends AbstractProcessor {

    final SimplifiedASTContext context = new SimplifiedASTContext();
    final List<Class<? extends Annotation>> fieldAnnotations;
    final List<Class<? extends Annotation>> methodAnnotations;

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment round) {
        fieldAnnotations.forEach(ann -> memorizeFieldsAnnotatedWith(round, ann));
        methodAnnotations.forEach(ann -> memorizeMethodsAnnotatedWith(round, ann));

        if ( !context.isEmpty() )
            process(context.getTypes());
        return false;
    }

    private void memorizeFieldsAnnotatedWith( RoundEnvironment round, Class<? extends Annotation> annotation) {
        memorizeElementsAnnotatedWith( round, annotation, ElementKind.FIELD,
                e -> context.memorizeField( (VariableElement)e ));
    }

    private void memorizeMethodsAnnotatedWith( RoundEnvironment round, Class<? extends Annotation> annotation) {
        memorizeElementsAnnotatedWith( round, annotation, ElementKind.METHOD,
                e -> context.memorizeMethod( (ExecutableElement) e ));
    }

    @SuppressWarnings("unchecked")
    private void memorizeElementsAnnotatedWith(
        RoundEnvironment round,
        Class<? extends Annotation> annotation,
        ElementKind expectedElementKind,
        Consumer<Element> callback)
    {
        val elements = round.getElementsAnnotatedWith( annotation );
        if ( !elements.isEmpty() ) {
            for ( val element : elements )
                if ( expectedElementKind.equals( element.getKind() ) )
                    callback.accept(element);
        }
    }

    protected abstract void process(Collection<SimplifiedAST.Type> types);

    /**
     * We just return the latest version of whatever JDK we run on. Stupid?
     * Yeah, but it's either that or warnings on all versions but 1. Blame Joe.
     *
     * PS: this method was copied from Project Lombok. ;)
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.values()[SourceVersion.values().length - 1];
    }
}
