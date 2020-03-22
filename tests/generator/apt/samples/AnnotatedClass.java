package generator.apt.samples;

public class AnnotatedClass extends Superclass implements Interface {

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

abstract class Superclass implements Runnable {

    @Override
    public void run() {
        System.out.println("I can run!");
    }
}

interface Interface {
    
}