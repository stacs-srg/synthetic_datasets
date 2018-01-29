package uk.ac.standrews.cs.synthetic_datasets.file_system;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class FS_snapshot {

    public static void main(String args[]) throws IOException {

        DatasetFileVisitor<Path> fv = new DatasetFileVisitor<>();

        File pathToSnapshot = new File("/Users/sic2/git/");
        Files.walkFileTree(pathToSnapshot.toPath(), fv);

        System.out.println("\n\n=================================");
        System.out.println("Number of files: " + fv.numberOfFiles);
        System.out.println("Size dataset: " + fv.totalSize + " (" + (fv.totalSize / (1024*1024*1.0)) + "MB)");
        System.out.println("Number of directories: " + fv.numberOfDirectories);

        // Info about data types
    }

    private static class DatasetFileVisitor<T> extends SimpleFileVisitor<Path> {

        int numberOfFiles = 0;
        int totalSize = 0;
        int numberOfDirectories = -1; // Do not count the parent directory
        Set<String> fileExtensions;
        ArrayList<Long> filesSize;

        DatasetFileVisitor() {
            fileExtensions = new LinkedHashSet<>();
            filesSize = new ArrayList<>();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            super.preVisitDirectory(dir, attrs);

            numberOfDirectories++;
            System.out.println("Directory: " + dir.toString());

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            System.out.println("\t\tFile: " + file);

            numberOfFiles++;
            totalSize += file.toFile().length();
            filesSize.add(file.toFile().length());

            String extension = FilenameUtils.getExtension(file.getFileName().toString());
            fileExtensions.add(extension);

            return FileVisitResult.CONTINUE;
        }
    }
}
