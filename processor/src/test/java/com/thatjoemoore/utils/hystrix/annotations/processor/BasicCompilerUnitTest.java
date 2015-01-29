package com.thatjoemoore.utils.hystrix.annotations.processor;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by adm.jmooreoa on 9/2/14
 */
@RunWith(JUnit4.class)
public class BasicCompilerUnitTest extends CompilerTest {

    @Test
    public void basic() throws IOException {
        CompilationResult result = compile(
                "src/test/java/com/thatjoemoore/utils/hystrix/annotations/processor/example/basic/ExampleService.java"
        );
        for (Diagnostic<? extends JavaFileObject> each : result.diagnostics) {
            System.out.println(each);
        }

        result.assertSameContent("/expected/basic/ExampleServiceDoSomething.java", "com/thatjoemoore/utils/hystrix/annotations/processor/example/basic/commands/ExampleServiceDoSomething.java");
        result.assertSameContent("/expected/basic/ExampleServiceHystrixWrapper.java", "com/thatjoemoore/utils/hystrix/annotations/processor/example/basic/commands/ExampleServiceHystrixWrapper.java");

//        echoFile(result, "com/test/TestFixtureMapper.java", "Mapper");
//        echoFile(result, "edu/byu/gaat/db/mappers/EmbeddableMapper.java", "Embeddable Mapper");
//        echoFile(result, "edu/byu/gaat/db/mappers/QueryFixtureQueries.java", "Queries");
        echoFile(result, "com/thatjoemoore/utils/hystrix/annotations/processor/example/basic/commands/ExampleServiceDoSomething.java", "Do Something Command");
        echoFile(result, "com/thatjoemoore/utils/hystrix/annotations/processor/example/basic/commands/ExampleServiceHystrixWrapper.java", "Wrapper Class");
        assertTrue(result.result);
    }

    private void echoFile(CompilationResult result, String file, String description) throws IOException {
        File f = new File(result.outputDirectory, file);

        System.out.println("==================================================================");
        System.out.println(" " + description + " Result");
        System.out.println("==================================================================");
        FileInputStream fis = new FileInputStream(f);
        IOUtils.copy(fis, System.out);
        IOUtils.closeQuietly(fis);
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    }


}
