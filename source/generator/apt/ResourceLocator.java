package generator.apt;

import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileManager;
import java.io.IOException;
import java.net.URI;

/**
 * Locates resource locations where Annotation Processors can create
 * files on or read files from.
 */
@RequiredArgsConstructor
public class ResourceLocator {

    final ProcessingEnvironment processingEnv;
    final JavaFileManager.Location outputLocation;

    /**
     * Locates a resource within the Annotation Processor runtime environment.
     * 
     * @param resourcePath expected resource to be located
     * @return the URI representing the located resource
     * @throws IOException If the file cannot be opened
     * @throws IllegalArgumentException If the given string violates RFC&nbsp;2396
     */
    public URI locate(String resourcePath) throws IOException {
        val resource = processingEnv.getFiler().getResource( this.outputLocation, "", resourcePath );
        return resource.toUri();
    }
}
