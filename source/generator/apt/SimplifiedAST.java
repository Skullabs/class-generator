package generator.apt;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lombok.*;

/**
 *
 */
public interface SimplifiedAST {

    AtomicInteger methodCounter = new AtomicInteger(0);

    @Data
    class Type {
        String canonicalName;
        List<Element> fields = new ArrayList<>();
        List<Method> methods = new ArrayList<>();

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

        public String toString() {
            return "class " + canonicalName + "{\n\n" + stringify(methods, "\n\n") + "\n\n}";
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
        List<Annotation> annotations = new ArrayList<>();

        protected Annotation getAnnotation(Class<?> clazz) {
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
        return stringify(iterable, ", ");
    }

    static String stringify(Iterable<?> iterable, String delimiter) {
        List<String> strings = new ArrayList<>();
        for (Object param : iterable)
            strings.add(param.toString());
        return String.join(delimiter, strings);
    }
}
