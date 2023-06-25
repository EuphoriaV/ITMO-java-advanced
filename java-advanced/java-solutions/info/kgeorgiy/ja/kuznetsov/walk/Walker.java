package info.kgeorgiy.ja.kuznetsov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Walker {
    public static void walk(String[] args, boolean recursive) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Invalid arguments");
            return;
        }
        String inputFileName = args[0], outputFileName = args[1];
        Path path; //:NOTE: old IO
        try {
            path = Path.of(outputFileName);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (InvalidPathException | SecurityException | IOException e) {
            System.err.println(e.getMessage());
            return;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            try (Stream<String> lines = Files.lines(Path.of(inputFileName))) {
                lines.forEach(fileName -> {
                    try (Stream<Path> paths = Files.walk(Path.of(fileName), recursive ? Integer.MAX_VALUE : 0)) {
                        paths.filter(path1 -> Files.isRegularFile(path1) && !Files.isDirectory(path1)).forEach(path1 -> {
                            String hashSum = HashSumCounter.getHashSum(path1);
                            try {
                                writer.write(hashSum + " " + path1 + System.lineSeparator()); //:NOTE: \n
                            } catch (SecurityException | IOException e) {
                                System.err.println("Couldn't write to file: " + path1);
                            }
                        });
                    } catch (SecurityException | InvalidPathException | IOException e) {
                        try {
                            writer.write(HashSumCounter.getDefaultHashSum() + " " + fileName + System.lineSeparator());
                        } catch (SecurityException | IOException ex) {
                            System.err.println("Couldn't write to file: " + fileName);
                        }
                    }
                });
            } catch (SecurityException | InvalidPathException | IOException e) {
                System.err.println("Invalid input file: " + inputFileName);
            }
        } catch (SecurityException | IOException e) {
            System.err.println("Invalid output file: " + outputFileName);
        }
    }
}