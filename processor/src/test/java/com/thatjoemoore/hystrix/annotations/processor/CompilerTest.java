package com.thatjoemoore.hystrix.annotations.processor;

import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.*;
import javax.xml.transform.Source;
import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by adm.jmooreoa on 9/2/14
 */
@RunWith(JUnit4.class)
public abstract class CompilerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static JavaCompiler javaCompiler;
    private StandardJavaFileManager fileManager;
    private DiagnosticCollector<JavaFileObject> collector;

    @BeforeClass
    public static void initCompiler() {
        javaCompiler = ToolProvider.getSystemJavaCompiler();
    }

    @Before
    public final void initClass() {
        collector = new DiagnosticCollector<>();
        fileManager = javaCompiler.getStandardFileManager(collector, Locale.US, Charset.defaultCharset());
    }

    protected final CompilationResult compile(String... classes) throws IOException {
        StringWriter stdout = new StringWriter();

        File temp = folder.newFolder();
//        File temp = new File("/tmp/comp-test-" + System.currentTimeMillis());
//        temp.mkdir();

        JavaCompiler.CompilationTask task = javaCompiler.getTask(new OutputStreamWriter(System.out), fileManager, collector, Arrays.asList("-d", temp.getAbsolutePath()),
                null, fileManager.getJavaFileObjects(classes));

        Boolean result = task.call();
        String output = stdout.toString();

        return new CompilationResult(result, output, collector.getDiagnostics(), temp);
    }

    protected static class CompilationResult {
        final Boolean result;
        final String stdout;
        final List<Diagnostic<? extends JavaFileObject>> diagnostics;
        final File outputDirectory;

        public CompilationResult(Boolean result, String stdout, List<Diagnostic<? extends JavaFileObject>> diagnostics, File outputDirectory) {
            this.result = result;
            this.stdout = stdout;
            this.diagnostics = diagnostics;
            this.outputDirectory = outputDirectory;
        }

        public void assertSameContent(String expectedFilePath, String generatedFilePath) throws IOException {
            File genFile = new File(outputDirectory, generatedFilePath);

            SourceFile expected;
            SourceFile generated;

            try (InputStream expStream = getClass().getResourceAsStream(expectedFilePath);
                 InputStream genStream = new FileInputStream(genFile)) {
                expected = readFile(expStream);
                generated = readFile(genStream);
            }

            char[] expArray = expected.contents;
            char[] genArray = generated.contents;

//            System.out.println(new String(expArray));
//            System.out.println(new String(genArray));

            assertEquals("Generated file is not the same length as expected file", expected.contents.length, generated.contents.length);
            for (int i = 0, len = expected.contents.length; i < len; i++) {
                if (expArray[i] != genArray[i]) {
                    FileLine expLine = expected.lines[i];
                    FileLine genLine = generated.lines[i];

                    final String expLineNo = Integer.toString(expLine.number);
                    final String genLineNo = Integer.toString(genLine.number);

                    String indicator1 = "\t                 ";
                    for (int j = 0, len2 = expLineNo.length() + i - expLine.startIndex; j < len2; j++) {
                        indicator1 += " ";
                    }
                    String indicator2 = "\t                 ";
                    for (int j = 0, len2 = genLineNo.length() + i - genLine.startIndex; j < len2; j++) {
                        indicator2 += " ";
                    }

                    fail("Files did not match.\n " +
                            "\tExpected: line " + expLineNo + ": " + expLine.original + "\n" +
                            indicator1 + "^\n" +
                            "\tActual:   line " + genLineNo + ": " + genLine.original + "\n" + indicator2 + "^");
                }
            }
        }


        @Override
        public String toString() {
            return "CompilationResult{" +
                    "result=" + result +
                    ", stdout='" + stdout + '\'' +
                    ", diagnostics=" + diagnostics +
                    ", outputDirectory=" + outputDirectory +
                    '}';
        }
    }

    @AfterClass
    public static void killCompiler() {
        javaCompiler = null;
    }

    protected static SourceFile readFile(InputStream file) throws IOException {
        List<String> lines = IOUtils.readLines(file);
        CharArrayWriter writer = new CharArrayWriter();

        final List<FileLine> fileLines = new ArrayList<>();

        int current = 0;
        for (final ListIterator<String> iterator = lines.listIterator(); iterator.hasNext(); ) {
            final String each = iterator.next();
            if (each.trim().isEmpty()) {
                continue;
            }
            final String trimmed = each.replaceAll("\\s+", " ");

            final int start = current;
            current += trimmed.length();
            final int end = current;

            writer.append(trimmed);

            FileLine line = new FileLine(iterator.previousIndex(), start, end, trimmed);
            for (int i = 0, len = end - start; i < len; i++) {
                fileLines.add(line);
            }

//            System.out.println(trimmed);
//            System.out.println(writer.toString().substring(start, end));
        }
//        System.out.println(writer.toString());
        return new SourceFile(writer.toCharArray(), fileLines.toArray(new FileLine[fileLines.size()]));
    }

    private static List<String> lines(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return IOUtils.readLines(fis);
        }
    }

    protected static final class SourceFile {

        private final char[] contents;
        private final FileLine[] lines;

        public SourceFile(char[] contents, FileLine[] lines) {
            this.contents = contents;
            this.lines = lines;
            if (contents.length != lines.length) {
                throw new IllegalArgumentException("contents and lines must be the same length");
            }
        }

    }

    protected static final class FileLine {
        private final int number;
        private final int startIndex;
        private final int endIndex;
        private final String original;

        public FileLine(int number, int startIndex, int endIndex, String original) {
            this.number = number;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.original = original;
        }
    }

}
