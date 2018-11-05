package generator.apt.samples;

class AnnotatedClassDelegated {

    final generator.apt.samples.AnnotatedClass target = new generator.apt.samples.AnnotatedClass();

    @generator.apt.samples.Ignored()
    java.lang.String name(){
        return target.name;
    }


    @generator.apt.samples.Ignored()
    int sum(){
        return target.sum();
    }

}