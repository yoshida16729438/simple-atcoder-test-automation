import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * atcoder test helper
 */
public class AtCoderHelper {

    private static final String EXIT_COMMAND = "exit";
    private static final String CONTEST_PAGE_FORMAT = "https://atcoder.jp/contests/%s";
    private static final String TASK_PAGE_FORMAT = CONTEST_PAGE_FORMAT + "/tasks/%s_%s";
    private static HttpClient client;

    /**
     * status of state machine
     */
    private static enum Status {

        /**
         * change contest
         */
        contest,

        /**
         * change task
         */
        task,

        /**
         * execute test
         */
        test,

        /**
         * finish the application
         */
        exit;
    }

    /**
     * entry point
     * 
     * @param args boot parameter (not used)
     */
    public static void main(String[] args) {

        client = HttpClient.newHttpClient();
        printBootScreen();

        boolean exitFlag = false;
        try (Scanner sc = new Scanner(System.in)) {
            String contestName = getContestName(sc, "not set");
            String task = "not set";

            while (!exitFlag) {
                printSeparator();
                switch (getNextStatus(sc, contestName, task)) {
                    case contest:
                        String newContestName = getContestName(sc, contestName);
                        if (!newContestName.equals(contestName)) {
                            contestName = newContestName;
                            task = "not set";
                        }
                        break;

                    case task:
                        task = getTask(sc, contestName, task);
                        break;

                    case test:
                        executeTests();
                        break;

                    case exit:
                        exitFlag = true;
                        break;

                    default:
                        // noop
                }
            }
        }
        printExitScreen();
    }

    /**
     * print boot screen
     */
    private static void printBootScreen() {
        System.out.println();
        System.out.println("###########################");
        System.out.println("# AtCoder Helper for Java #");
        System.out.println("###########################");
        System.out.println();
        System.out.println("At first, please input contest name.");
        System.out.println();
    }

    /**
     * print exit screen
     */
    private static void printExitScreen() {
        System.out.println("Bye");
        System.out.println();
    }

    /**
     * print separator
     */
    private static void printSeparator() {
        System.out.println("-----------------------------------------------");
    }

    /**
     * get contest name from stdin and try to access contest page
     * 
     * @param sc                 scanner instance to get from
     * @param currentContestName contest name currently set
     * @return new contest name or current contest name if exit
     */
    private static String getContestName(Scanner sc, String currentContestName) {
        do {
            System.out.println("Input contest name (ex. \"abc123\", \"arc200\")");
            System.out.println("Or input \"" + EXIT_COMMAND + "\" to finish the application");
            System.out.println("current contest: " + currentContestName);
            System.out.print("command?: ");

            String contestName = sc.nextLine();
            System.out.println();

            if (contestName.equals(EXIT_COMMAND)) {
                System.out.println("returning to menu...");
                System.out.println();
                return currentContestName;
            } else if (Pattern.matches("^[a-z]+[0-9]+$", contestName)) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(createContestPageUri(contestName)).GET().build();
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        System.out.println("Successfully changed to contest " + contestName);
                        System.out.println();
                        return contestName;
                    } else {
                        System.out.println("Failed to get contest page with status code " + response.statusCode());
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("Failed to send HTTP request:" + e.getLocalizedMessage());
                }
                System.out.println("Please try again");
                System.out.println();

            } else {
                System.out.println(
                        String.format("Invalid input: \"%s\" is neither contest name nor exit command", contestName));
            }
        } while (true);
    }

    /**
     * create contest page uri to access
     * 
     * @param contestName contest name
     * @return uri of contest page
     */
    private static URI createContestPageUri(String contestName) {
        return URI.create(String.format(CONTEST_PAGE_FORMAT, contestName));
    }

    /**
     * create task page uri to access
     * 
     * @param contestName contest name
     * @param task        task name
     * @return uri of task page
     */
    private static URI createTaskPageUri(String contestName, String task) {
        return URI.create(String.format(TASK_PAGE_FORMAT, contestName, contestName, task));
    }

    /**
     * get next status to move to
     * 
     * @param sc                 scanner instance
     * @param currentContestName current contest name
     * @param currentTask        current task name
     * @return next status to move to
     */
    private static Status getNextStatus(Scanner sc, String currentContestName, String currentTask) {
        final String contestCommand = "contest";
        final String taskCommand = "task";
        final String testCommand = "test";
        do {
            System.out
                    .println(String.format("[current contest: %s, current task: %s]", currentContestName, currentTask));
            System.out.println("Input one of the commands below.");
            System.out.println(contestCommand + ": switch contest");
            System.out.println(taskCommand + ": switch task in current contest");
            System.out.println(testCommand + ": execute tests");
            System.out.println(EXIT_COMMAND + ": return to menu");
            System.out.println();
            System.out.print("command?: ");

            String command = sc.nextLine();
            System.out.println();

            switch (command) {
                case contestCommand:
                    return Status.contest;
                case taskCommand:
                    return Status.task;
                case testCommand:
                    return Status.test;
                case EXIT_COMMAND:
                    return Status.exit;
                default:
                    System.out.println("Undefined command \"" + command + "\"");
                    System.out.println("Please try again");
                    System.out.println();
            }
        } while (true);
    }

    /**
     * get task name to move to
     * 
     * @param sc          scanner instance
     * @param contestName current contest name
     * @param currentTask current task name
     * @return new task name or current task name if exit
     */
    private static String getTask(Scanner sc, String contestName, String currentTask) {
        do {
            System.out.println(String.format("[current contest: %s, current task: %s]", contestName, currentTask));
            System.out.println("Input task (\"a\", \"d\", etc.)");
            System.out.println("Or input \"exit\" to return to menu");
            System.out.println();
            System.out.print("command?: ");

            String task = sc.nextLine();
            System.out.println();

            if (task.equals(EXIT_COMMAND)) {
                System.out.println("returning to menu...");
                System.out.println();
                return currentTask;
            }

            HttpRequest request = HttpRequest.newBuilder().uri(createTaskPageUri(contestName, task)).GET().build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    List<TestCase> samples = detectSamples(response.body());
                    if (samples.size() > 0) {
                        saveSamples(samples);
                        System.out.println(samples.size() + " sample(s) have been downloaded");
                        System.out.println();
                        return task;
                    }
                } else {
                    System.out.println("Failed to get task page with status code " + response.statusCode());
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Failed to send HTTP request:" + e.getLocalizedMessage());
            }
            System.out.println("Please try again");
            System.out.println();
        } while (true);
    }

    /**
     * detect sample data from problem page html
     * 
     * @param html problem page html
     * @return List of sample data
     */
    private static List<TestCase> detectSamples(String html) {
        // cut all text after "lang-en" to avoid data duplication and translate html
        // special characters
        String target = translateHtmlSpecialCharacters(html.substring(html.indexOf("入力例"), html.indexOf("lang-en")));

        // find <pre>test case here</pre>
        Pattern p = Pattern.compile("<pre>(?:(?!</pre>)(?!<var>)[\\S\\s])*</pre>");
        Matcher m = p.matcher(target);

        List<String> matchedValues = new ArrayList<>();
        while (m.find()) {
            String group = m.group().replaceAll("\\r\\n|\\r|\\n", System.lineSeparator());

            // get data between <pre> and </pre>
            matchedValues.add(group.substring(group.indexOf(">") + 1, group.lastIndexOf("<")));
        }

        if (matchedValues.size() == 0 || matchedValues.size() % 2 == 1) {
            System.out.println("Failed to detect test data");
            return List.of();
        }

        List<TestCase> result = new ArrayList<>();
        for (int i = 0; i < matchedValues.size() / 2; i++) {
            result.add(new TestCase(matchedValues.get(i * 2), matchedValues.get(i * 2 + 1)));
        }

        return result;
    }

    /**
     * translate html special characters
     * 
     * @param source source text of html
     * @return converted text
     */
    private static String translateHtmlSpecialCharacters(String source) {

        // set default replace characters
        Map<String, String> map = new HashMap<>();
        map.put("&amp;", "&");
        map.put("&quot;", "\"");
        map.put("&apos;", "'");
        map.put("&nbsp;", " ");
        map.put("&lt;", "<");
        map.put("&gt;", ">");

        // find replace target indexes
        Pattern p = Pattern.compile("&(amp|quot|apos|nbsp|lt|gt|#\\d+|#x[0-9A-Fa-f]+);");
        Matcher m = p.matcher(source);

        StringBuilder builder = new StringBuilder();
        int pos = 0;

        // replace all matched text
        while (m.find()) {
            builder.append(source.substring(pos, m.start()));

            String matched = m.group();

            // translate character with code point
            if (!map.containsKey(matched)) {
                int st;
                int radix;
                if (matched.charAt(2) == 'x') {
                    st = 3;
                    radix = 16;
                } else {
                    st = 2;
                    radix = 10;
                }

                int[] codePoints = new int[] { Integer.parseInt(matched.substring(st, matched.length() - 1), radix) };
                map.put(matched, new String(codePoints, 0, 1));
            }

            builder.append(map.get(matched));

            pos = m.end();
        }

        builder.append(source.substring(pos));

        return builder.toString();
    }

    /**
     * save samples to file
     * 
     * @param samples test case data
     */
    private static void saveSamples(List<TestCase> samples) {

        // remove all current files
        Path inputDir = Paths.get(Constants.TEST_INPUT_FOLDER);
        Path answerDir = Paths.get(Constants.TEST_ANSWER_FOLDER);

        deleteFileTree(inputDir);
        deleteFileTree(answerDir);

        createDirectory(inputDir);
        createDirectory(answerDir);

        IntStream.range(0, samples.size()).forEach(ind -> {
            saveTestData(ind + 1, samples.get(ind), inputDir, answerDir);
        });
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

    /**
     * create directory
     * 
     * @param path to create
     */
    private static void createDirectory(Path path) {
        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            System.out.println("Failed to create directories: " + e.getLocalizedMessage());
        }
    }

    /**
     * save test data to files
     * 
     * @param sampleNo  sample number
     * @param sample    actual sample
     * @param inputDir  directory to save input data into
     * @param answerDir directory to save output data into
     */
    private static void saveTestData(int sampleNo, TestCase sample, Path inputDir, Path answerDir) {
        final String fileName = String.format("test%d.txt", sampleNo);
        saveToFile(inputDir.resolve(fileName), sample.input);
        saveToFile(answerDir.resolve(fileName), sample.output);
    }

    /**
     * save text to file
     * 
     * @param path    to save to
     * @param content to save
     */
    private static void saveToFile(Path path, String content) {
        try (var writer = new FileWriter(path.toFile())) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println(String.format("Failed to save a file \"%s\": %s", path.getFileName().toString(),
                    e.getLocalizedMessage()));
        }
    }

    /**
     * execute tests with currently downloaded test data
     */
    private static void executeTests() {
        try {
            Process process = new ProcessBuilder("powershell", "./execute_tests.ps1").start();
            BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder stdOutBuilder = new StringBuilder();
            StringBuilder stdErrBuilder = new StringBuilder();

            boolean continueRead = true;
            while (continueRead) {
                continueRead = false;

                String out = stdOutReader.readLine();
                if (out != null) {
                    continueRead = true;
                    stdOutBuilder.append(out);
                    stdOutBuilder.append("\n");
                }

                String err = stdErrReader.readLine();
                if (err != null) {
                    continueRead = true;
                    stdErrBuilder.append(err);
                    stdErrBuilder.append("\n");
                }
            }
            stdOutReader.close();
            stdErrReader.close();
            
            if(stdOutBuilder.length()>0){
                System.out.println();
                System.out.print(stdOutBuilder.toString());
            }

            if(stdErrBuilder.length()>0){
                System.out.println();
                System.out.print(stdErrBuilder.toString());
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to execute test: " + e.getLocalizedMessage());
        }
        System.out.println();
    }

    /**
     * test case data class
     */
    private static class TestCase {

        /**
         * input data of test case
         */
        public final String input;

        /**
         * expected output data of test case
         */
        public final String output;

        /**
         * constructor
         * 
         * @param input  of test case
         * @param output of test case
         */
        public TestCase(String input, String output) {
            this.input = input;
            this.output = output;
        }
    }
}
