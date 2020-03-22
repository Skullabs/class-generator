package generator.apt;

import java.util.*;
import java.util.stream.Collectors;
import javax.lang.model.element.*;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

import generator.apt.SimplifiedAST.*;
import lombok.*;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;

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
        val newParameters = extractMethodParameters(method);
        val name = method.getSimpleName().toString();
        val generatedMethod = (SimplifiedAST.Method) new SimplifiedAST.Method()
                .setConstructor("<init>".equals(name))
                .setParameters(newParameters)
                .setType(method.getReturnType().toString())
                .setName(name);

        return (SimplifiedAST.Method) generatedMethod
                .setAnnotations(loadAnnotations(method, generatedMethod));
    }

    private List<SimplifiedAST.Element> extractMethodParameters(ExecutableElement method ){
        val parameters = method.getParameters();
        return asParameterList(parameters);
    }

    private Type getCachedType( Element element ){
        val typeElement = (TypeElement) element.getEnclosingElement();
        return cachedTypes.computeIfAbsent(
                typeElement.asType().toString(),
                t -> createTypeFrom(t, typeElement));
    }

    private Type createTypeFrom(String canonicalName, TypeElement type) {
        val newType = new SimplifiedAST.Type()
                .setAbstract(isAbstract(type.getModifiers()))
                .setInterface(type.getKind().equals(ElementKind.INTERFACE))
                .setInterfaces(loadInterfacesFrom(type))
                .setSuperclass(loadSuperclassFrom(type))
                .setCanonicalName(canonicalName)
                .setMethods(new ArrayList<>());
        newType.setAnnotations( loadAnnotations(type, newType) );
        memorizeConstructors(newType, type);
        return newType;
    }

    private List<Type> loadInterfacesFrom(TypeElement type) {
        val interfaces = new ArrayList<Type>();
        for (val interfaceTypeMirror : type.getInterfaces()) {
            val interfaceDeclaredType = (DeclaredType)interfaceTypeMirror;
            val interfaceType = (TypeElement) interfaceDeclaredType.asElement();
            val canonicalName = interfaceType.asType().toString();
            interfaces.add(createTypeFrom(canonicalName, interfaceType));
        }
        return interfaces;
    }

    private Type loadSuperclassFrom(TypeElement type) {
        val foundSuperclass = type.getSuperclass();

        if (foundSuperclass.getKind() != TypeKind.NONE) {
            val superclassTypeMirror = (DeclaredType)foundSuperclass;
            val superclass = (TypeElement) superclassTypeMirror.asElement();
            val canonicalName = superclass.asType().toString();
            return createTypeFrom(canonicalName, superclass);
        }

        return null;
    }

    private boolean isAbstract(Set<Modifier> modifiers){
        return modifiers.contains(ABSTRACT);
    }

    private void memorizeConstructors( Type newType, TypeElement type ){
        val elements = type.getEnclosedElements();
        for (val element : elements)
            if (ElementKind.CONSTRUCTOR.equals(element.getKind()))
                newType.methods.add(createMethod((ExecutableElement) element));

        memorizeLombokConstructors(newType, type);
    }

    private void memorizeLombokConstructors(Type newType, TypeElement type) {
        memorizeLombokRequiredArgConstructor(newType, type);
        memorizeLombokAllArgConstructor(newType, type);
        memorizeLombokNoArgConstructor(newType, type);
    }

    private void memorizeLombokRequiredArgConstructor(Type newType, TypeElement type){
        val ann = type.getAnnotation(RequiredArgsConstructor.class);
        if (ann != null) {
            System.out.println(newType.canonicalName + " has RequiredArgsConstructor constructor");
            val constructor = (SimplifiedAST.Method) new SimplifiedAST.Method()
                    .setConstructor(true)
                    .setParameters(extractRequiredFields(type))
                    .setName("<init>");

            newType.methods.add(constructor);
        }
    }

    private List<SimplifiedAST.Element> extractRequiredFields(TypeElement type) {
        return extractFields(type).stream()
            .filter( it -> it.isFinal || it.getAnnotation(NonNull.class) != null )
            .collect( Collectors.toList() );
    }

    private void memorizeLombokAllArgConstructor(Type newType, TypeElement type){
        val ann = type.getAnnotation(AllArgsConstructor.class);
        if (ann != null) {
            System.out.println(newType.canonicalName + " has AllArgsConstructor constructor");
            val constructor = (SimplifiedAST.Method) new SimplifiedAST.Method()
                    .setConstructor(true)
                    .setParameters(extractFields(type))
                    .setName("<init>");

            newType.methods.add(constructor);
        }
    }

    private void memorizeLombokNoArgConstructor(Type newType, TypeElement type){
        val ann = type.getAnnotation(NoArgsConstructor.class);
        if (ann != null) {
            System.out.println(newType.canonicalName + " has NoArgsConstructor constructor");
            newType.methods.add((SimplifiedAST.Method)
                new SimplifiedAST.Method()
                    .setConstructor(true)
                    .setName("<init>"));
        }
    }

    private List<SimplifiedAST.Element> extractFields(TypeElement type) {
        val fields = type.getEnclosedElements()
            .stream()
            .filter( it -> it.getKind() == ElementKind.FIELD )
            .map( it -> (VariableElement)it )
            .collect(Collectors.toList());
        return asParameterList(fields);
    }

    private List<SimplifiedAST.Element> asParameterList(List<? extends VariableElement> variables){
        val newParameters = new ArrayList<SimplifiedAST.Element>();
        for (val parameter : variables)
            newParameters.add(createParameter(parameter));
        return newParameters;
    }

    private SimplifiedAST.Element createParameter(VariableElement parameter) {
        val typeCanonicalName = getCanonicalName(parameter);
        val param = new SimplifiedAST.Element()
                .setName(parameter.getSimpleName().toString())
                .setType(typeCanonicalName)
                .setFinal(parameter.getModifiers().contains(FINAL));

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
}
