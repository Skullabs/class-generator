package generator.apt.samples;

public class AnnotatedClass {

    @Ignored String name;

    @Ignored int sum(
        @Important("p1") int p1, int p2
    ){
        return p1 + p2;
    }

    long sum( long p1, long p2 ) {
        return p1 + p2;
    }
}
