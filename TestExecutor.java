import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Main class to execute tests
 */
public class TestExecutor {

    /**
     * main method to execute all test cases(no need to edit)
     * 
     * @param args
     */
    public static void main(String[] args) {

        InputStream initialInput = System.in;
        PrintStream initialOutput = System.out;

        Path inputFolder = Paths.get(Constants.TEST_INPUT_FOLDER);
        Path outputFolder = Paths.get(Constants.TEST_OUTPUT_FOLDER);
        Path answerFolder = Paths.get(Constants.TEST_ANSWER_FOLDER);
        Map<String, String> map = new HashMap<>();  //keep test results

        //testing thread
        ExecutorService service = Executors.newSingleThreadExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });

        try {
            //clean old test results
            if (Files.exists(outputFolder)) {
                deleteFileTree(outputFolder);
            }

            Files.createDirectory(outputFolder);

            Files.list(inputFolder).forEach(inputFilePath -> {
                String fileName = inputFilePath.getFileName().toString();
                Path outputFilePath = outputFolder.resolve(fileName);

                LocalDateTime start, end;

                try (InputStream in = Files.newInputStream(inputFilePath, StandardOpenOption.READ);
                        PrintStream out = new PrintStream(outputFilePath.toFile())) {
                    // replace stdin and stdout to pass data to Main::main
                    System.setIn(in);
                    System.setOut(out);

                    start = LocalDateTime.now();

                    Future<LocalDateTime> future = service.submit(() -> {
                        Main.main(new String[0]);
                        return LocalDateTime.now();
                    });

                    try {
                        end = future.get(10, TimeUnit.SECONDS);

                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        future.cancel(true);
                        e.printStackTrace();
                        map.put(fileName, "execution timeout");
                        return;
                    }

                } catch (IOException e) {
                    map.put(fileName, "failed to execute: " + e.getLocalizedMessage());
                    return;
                }

                double elapsedSec = (double) Duration.between(start, end).toMillis() / 1000;

                String expected;
                String actual;
                try {
                    expected = Files.readString(answerFolder.resolve(fileName)).trim();
                    actual = Files.readString(outputFilePath).trim();
                } catch (IOException e) {
                    map.put(fileName, "failed to load result: " + e.getLocalizedMessage());
                    return;
                }

                if (expected.equals(actual)) {
                    map.put(fileName, "succeeded (elapsed = " + elapsedSec + " sec)");
                } else {
                    map.put(fileName, "failed");
                }
            });
        } catch (IOException e) {
            map.put("failed to execute", "list up failed: " + e.getLocalizedMessage());
        }

        System.setIn(initialInput);
        System.setOut(initialOutput);

        map.forEach((key, value) -> {
            System.out.println(String.format("%s %s", key, value));
        });

        try {
            service.shutdown();
            if(!service.awaitTermination(2, TimeUnit.SECONDS)){
                service.shutdownNow();
                if(!service.awaitTermination(2, TimeUnit.SECONDS)){
                    System.err.println("failed to close service");
                }
            }
        } catch (InterruptedException e) {
            System.err.println("failed to close service");
        }
    }

    /**
     * delete all files and directories below baseDir
     * 
     * @param baseDir root directory to delete
     */
    private static void deleteFileTree(Path baseDir) {
        if (!Files.exists(baseDir)) {
            return;
        }
        try {
            Files.walkFileTree(baseDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                };

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw exc;
                    }
                };

            });
        } catch (IOException e) {
            System.out.println("Failed to delete current test data: " + e.getLocalizedMessage());
        }
    }

}
