package uk.ac.standrews.cs.synthetic_datasets.random;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class RandomFileGeneratorByRange {

    private static final int BUFFER_LENGTH = 4096;

    public static void main(String[] args) throws IOException {

        Scanner in = new Scanner(System.in);

        System.out.println("Output folder pathname:");
        System.out.println("\tdatasets/random/multiple_1");
        String folderPathname = in.nextLine();

        System.out.println("Number of files:");
        int numberOfFiles = in.nextInt();

        System.out.println("Minimum file size (in bytes):");
        System.out.println("\t1000 (1KB)");
        System.out.println("\t100000 (100KB)");
        System.out.println("\t1000000 (1MB)");
        System.out.println("\t10000000 (10MB)");
        System.out.println("\t100000000 (100MB)");
        int minFileSize = in.nextInt();

        System.out.println("Maximum file size (in bytes):");
        System.out.println("\t1000 (1KB)");
        System.out.println("\t100000 (100KB)");
        System.out.println("\t1000000 (1MB)");
        System.out.println("\t10000000 (10MB)");
        System.out.println("\t100000000 (100MB)");
        int maxFileSize = in.nextInt();

        System.out.println("Step size (in bytes):");
        int stepSize = in.nextInt();

        for(int fileSize = minFileSize; fileSize < maxFileSize + 1; fileSize += stepSize) {
            String subfolder = folderPathname + "/" + fileSize;

            File datasetFolder = new File(subfolder);
            if (datasetFolder.exists()) {
                throw new IOException("Dataset exists already. Remove it manually if you want to recreate it");
            } else {
                datasetFolder.mkdirs();
            }

            System.out.println("Creating dataset at folder: " + subfolder);
            for(int i = 0; i < numberOfFiles; i++) {

                String filePath = subfolder + "/" + i;
                try (FileOutputStream fos = new FileOutputStream(filePath)) {

                    writeToFile(fos, fileSize);
                }
            }
        }
    }

    private static void writeToFile(FileOutputStream fos, int size) throws IOException {

        Random random = new Random();
        byte[] bytes = new byte[BUFFER_LENGTH];

        int currentSize = 0;
        while(currentSize < size) {

            random.nextBytes(bytes);

            int tempSize = currentSize + BUFFER_LENGTH;
            int length = tempSize <= size ? BUFFER_LENGTH : size - currentSize;
            fos.write(bytes, 0, length);

            currentSize += length;
        }
    }


}
