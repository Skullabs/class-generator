package generator.apt;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.val;

import javax.lang.model.SourceVersion;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 */
public interface SimplifiedAST {

    AtomicInteger methodCounter = new AtomicInteger(0);

    @Data
    class Type extends Element {
        final String jdkGeneratedAnnotation = identifyGeneratedAnnotation();

        String canonicalName;
        List<Element> fields = new ArrayList<>();
        List<Method> methods = new ArrayList<>();
        boolean isInterface;
        boolean isAbstract;

        Type superclass;
        List<Type> interfaces = new ArrayList<>();

        public List<Type> getInheritedInterfaces() {
            val interfaces = new ArrayList<Type>();
            interfaces.addAll(getInterfaces());

            val superclass = getSuperclass();
            if (superclass != null)
                interfaces.addAll(superclass.getInheritedInterfaces());

            return interfaces;
        }

        public String getPackageName() {
            return canonicalName.replaceAll("(\\.[A-Z].*)", "");
        }

        public String getSimpleName() {
            return canonicalName.replaceAll(".*\\.([^.]+)$", "$1");
        }

        public String getGeneratedSimpleName() {
            return getSimpleName() + "Router";
        }

        public WrappedDataIterable getMethodsIterable() {
            return new WrappedDataIterable(methods.subList(1, methods.size()));
        }

        private String identifyGeneratedAnnotation() {
            val versionWithOldGeneratedAnnotation = SourceVersion.RELEASE_8.ordinal();
            if (SourceVersion.latestSupported().ordinal() > versionWithOldGeneratedAnnotation)
                return "javax.annotation.processing.Generated";
            else
                return "javax.annotation.Generated";
        }
        
        public String toString() {
            return "class " + canonicalName + "(interfaces = [" + stringify(interfaces) + "], superclass = "+ superclass +")";
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    class Method extends Element {

        final int counter = methodCounter.getAndIncrement();
        boolean constructor;
        List<Element> parameters = new ArrayList<>();

        public boolean isVoidMethod() {
            return "void".equals(type);
        }

        public WrappedDataIterable getParameterIterable() {
            return new WrappedDataIterable(parameters);
        }

        public String getParameterList() {
            return stringify(parameters.stream().map(e -> e.type + " " + e.name).collect(Collectors.toList()));
        }

        public String getArgumentList() {
            return stringify(parameters.stream().map(Element::getName).collect(Collectors.toList()));
        }

        public String toString() {
            return stringify(annotations, "\n") + "\n" + type + " " + name + "(" + stringify(parameters, "\n") + ")";
        }
    }

    @Data
    class Element {
        String name;
        String type;
        boolean isFinal = false;
        List<Annotation> annotations = new ArrayList<>();

        public Annotation getAnnotation(Class<?> clazz) {
            for (Annotation ann : annotations)
                if (clazz.getCanonicalName().equals(ann.type))
                    return ann;
            return null;
        }

        public String toString() {
            return stringify(annotations) + " " + type + " " + name;
        }
    }

    @EqualsAndHashCode(exclude = "parent")
    @Data
    class Annotation {
        Element parent;
        String type;
        Map<String, Object> parameters = new HashMap<>();

        public Object getValue() {
            return parameters.get("value");
        }

        public String toString() {
            return "@" + type + "(" + stringify(parameters.entrySet()) + ")";
        }
    }

    @Data
    class WrappedDataIterable implements Iterable<WrappedData>, Iterator<WrappedData> {

        final List<?> data;
        int cursor = 0;

        @Override
        public Iterator<WrappedData> iterator() {
            cursor = 0;
            return this;
        }

        @Override
        public boolean hasNext() {
            return cursor < data.size();
        }

        @Override
        public WrappedData next() {
            return new WrappedData(cursor == 0, data.get(cursor++));
        }
    }

    @Value
    class WrappedData {
        boolean first;
        Object data;
    }

    static String stringify(Iterable<?> iterable) {
        if (iterable == null)
            return "";
        return stringify(iterable, ", ");
    }

    static String stringify(Iterable<?> iterable, String delimiter) {
        List<String> strings = new ArrayList<>();
        for (Object param : iterable)
            strings.add(param.toString());
        return String.join(delimiter, strings);
    }
}
