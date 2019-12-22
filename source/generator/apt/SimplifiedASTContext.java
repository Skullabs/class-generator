package generator.apt;

import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.element.Element;

import generator.apt.SimplifiedAST.*;
import lombok.val;

public class SimplifiedASTContext {

    public Map<String, Type> cachedTypes = new HashMap<>();

    /**
     * @return true if any method, field or class have already been memorized.
     */
    public boolean isEmpty() {
        return cachedTypes.isEmpty();
    }

    /**
     * @return all saved types in this context. Note that every invocation
     * of this method will flush its content.
     */
    public Collection<Type> getTypes() {
        val types = new ArrayList<Type>(cachedTypes.values());
        cachedTypes = new HashMap<>();
        return types;
    }

    /**
     * Memorize a field for further usage.
     *
     * @param variable to be memorized
     */
    public void memorizeField(VariableElement variable){
        getCachedType( variable )
            .fields.add(createField(variable));
    }

    private SimplifiedAST.Element createField(VariableElement variable) {
        val field = new SimplifiedAST.Element()
            .setName( variable.getSimpleName().toString() )
            .setType( variable.asType().toString() );

        return field.setAnnotations(loadAnnotations( variable, field ));
    }

    /**
     * Memorize a method for further usage.
     *
     * @param method to be memorized
     */
    public void memorizeMethod(ExecutableElement method) {
        getCachedType(method)
            .methods.add(createMethod(method));
    }

    private Method createMethod(ExecutableElement method) {
        val newParameters = readMethodParameters(method);
        val name = method.getSimpleName().toString();
        val generatedMethod = (SimplifiedAST.Method) new SimplifiedAST.Method()
                .setConstructor("<init>".equals(name))
                .setParameters(newParameters)
                .setType(method.getReturnType().toString())
                .setName(name);

        return (SimplifiedAST.Method) generatedMethod
                .setAnnotations(loadAnnotations(method, generatedMethod));
    }

    private List<SimplifiedAST.Element> readMethodParameters(ExecutableElement method ){
        val parameters = method.getParameters();
        val newParameters = new ArrayList<SimplifiedAST.Element>();
        for (val parameter : parameters)
            newParameters.add(createParameter(parameter));
        return newParameters;
    }

    private SimplifiedAST.Element createParameter(VariableElement parameter) {
        val typeCanonicalName = getCanonicalName(parameter);
        val param = new SimplifiedAST.Element()
                .setName(parameter.getSimpleName().toString())
                .setType(typeCanonicalName);

        param.setAnnotations(loadAnnotations(parameter, param));

        return param;
    }

    private List<SimplifiedAST.Annotation> loadAnnotations(Element element, SimplifiedAST.Element parent) {
        val newAnnotations = new ArrayList<SimplifiedAST.Annotation>();
        val annotations = element.getAnnotationMirrors();
        for (val annotation : annotations)
            newAnnotations.add(createAnnotation(annotation).setParent(parent));
        return newAnnotations;
    }

    private SimplifiedAST.Annotation createAnnotation(AnnotationMirror annotation) {
        val type = annotation.getAnnotationType().asElement().asType().toString();
        val elementValues = annotation.getElementValues();
        val values = new HashMap<String, Object>();
        for (val e : elementValues.entrySet())
            values.put(
                    e.getKey().getSimpleName().toString(),
                    e.getValue().toString()
            );

        return new SimplifiedAST.Annotation()
                .setParameters(values).setType(type);
    }

    private static String getCanonicalName(Element parameter) {
        val simpleName = parameter.asType().toString();
        return getCanonicalName( simpleName );
    }

    private static String getCanonicalName(String simpleName) {
        return simpleName.replaceAll( "<.*", "" );
    }

    private Type getCachedType( Element element ){
        val typeElement = (TypeElement) element.getEnclosingElement();
        return cachedTypes.computeIfAbsent(
                typeElement.asType().toString(),
                t -> createTypeFrom(t, typeElement));
    }

    private Type createTypeFrom(String canonicalName, TypeElement type) {
        val newType = new SimplifiedAST.Type()
                .setCanonicalName(canonicalName)
                .setMethods(new ArrayList<>());
        newType.setAnnotations( loadAnnotations(type, newType) );
        memorizeConstructors(newType, type);
        return newType;
    }

    private void memorizeConstructors( Type newType, TypeElement type ){
        val elements = type.getEnclosedElements();
        for (val element : elements)
            if (ElementKind.CONSTRUCTOR.equals(element.getKind()))
                newType.methods.add(createMethod((ExecutableElement) element));
    }
}
