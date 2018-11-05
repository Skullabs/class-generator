package generator.apt.samples;

import generator.apt.SimplifiedAST;
import generator.apt.SimplifiedASTContext;
import generator.apt.SimplifiedAbstractProcessor;
import lombok.Getter;

import javax.annotation.processing.SupportedAnnotationTypes;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@SupportedAnnotationTypes("generator.apt.samples.*")
public class IgnoredProcessor extends SimplifiedAbstractProcessor {

    @Getter
    List<SimplifiedAST.Type> types;


    public IgnoredProcessor() {
        super(
            singletonList(Ignored.class),
            singletonList(Ignored.class)
        );
    }

    @Override
    protected void process(Collection<SimplifiedAST.Type> types) {
        this.types = new ArrayList<>(types);
    }
}
