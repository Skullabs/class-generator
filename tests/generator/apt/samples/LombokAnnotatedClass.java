package generator.apt.samples;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
class RequiredArgsAnnotatedClass {

    @Ignored final String name;
    @NonNull String surname;
    String address;
}

@NoArgsConstructor
class AllArgsAnnotatedClass {

    @Ignored String name;
    @Ignored String surname;

    AllArgsAnnotatedClass( String name ) {
        this.name = name;
    }
}
