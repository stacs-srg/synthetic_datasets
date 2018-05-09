package uk.ac.standrews.cs.synthetic_datasets.text_dataset;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class RangeTextDatasetGenerator {

    public static void main(String[] args) throws IOException {

        Scanner in = new Scanner(System.in);

        System.out.println("Enter dataset configuration pathname.");
        System.out.println("Examples of path: ");
        System.out.println("\tsrc/main/resources/range_text_dataset_settings/dataset_1");
        System.out.println("\tsrc/main/resources/range_text_dataset_settings/dataset_2");
        String settingFilename = in.nextLine();

        Scanner datasetDefinition = new Scanner(new File(settingFilename));
        String datasetPath = datasetDefinition.next();
        int numberOfFiles = datasetDefinition.nextInt();
        String[] fileSizeRange = datasetDefinition.next().split("-");
        int minFileSize = Integer.parseInt(fileSizeRange[0].trim());
        int maxFileSize = Integer.parseInt(fileSizeRange[1].trim());
        int stepFileSize = datasetDefinition.nextInt();

        HashMap<String, Integer> wordsAndFrequency = new LinkedHashMap<>();
        while(datasetDefinition.hasNext()) {

            String[] wordFreq = datasetDefinition.next().split("-");
            String word = wordFreq[0].trim();
            int freq = Integer.parseInt(wordFreq[1].trim());

            wordsAndFrequency.put(word, freq);
        }

        createDataset(datasetPath, numberOfFiles, minFileSize, maxFileSize, stepFileSize, wordsAndFrequency);
    }

    private static void createDataset(String datasetPath, int numberOfFiles, int minFileSize, int maxFileSize, int stepFileSize, HashMap<String, Integer> words) throws IOException {

        for(int i = minFileSize, index = 0; i <= maxFileSize; i+= stepFileSize, index++) {
            createSubDataset(index, datasetPath + "/" + i, numberOfFiles, i, words);
        }

    }

    private static void createSubDataset(int subsetIndex, String subDatasetPath, int numberOfFiles, int filesize, HashMap<String, Integer> words) throws IOException {

        File subDatasetFolder = new File(subDatasetPath);
        if (subDatasetFolder.exists()) {
            throw new IOException("Dataset exists already. Remove it manually if you want to recreate it");
        } else {
            subDatasetFolder.mkdirs();
        }

        ArrayList<String> dictionary = loadDictionary();
        dictionary.removeAll(words.keySet()); // Make sure that the sets are not intersecting
        for(int i = 0; i < numberOfFiles; i++) {
            createFile(subsetIndex, i,subDatasetPath + "/file_" + i, filesize, filesize, dictionary, words);
        }
    }

    private static void createFile(int subsetIndex, int index, String filepath, int minFileSize, int maxFileSize, ArrayList<String> dictionary, HashMap<String, Integer> words) throws FileNotFoundException, UnsupportedEncodingException {

        int numberOfWords = 0;
        int currentSize = 0;
        int expectedSize = ThreadLocalRandom.current().nextInt(minFileSize, maxFileSize + 1);

        try (PrintWriter writer = new PrintWriter(filepath, "UTF-8")) {

            while(true) {

                numberOfWords++;
                String wordToAdd = pickWord(dictionary, words);
                int wordLength = wordToAdd.length();

                if ((currentSize + wordLength) >= expectedSize) {
                    int truncationLength = expectedSize - currentSize;
                    wordToAdd = wordToAdd.substring(0, truncationLength);
                    writer.print(wordToAdd);

                    currentSize += truncationLength;
                    break;
                }

                currentSize += wordLength;
                writer.print(wordToAdd);
            }
        }

        System.out.println("[ " + subsetIndex + " - " + index + " ] -- Created file: " + filepath + " || Size (bytes): " + currentSize + " || No. words: " + numberOfWords) ;
    }

    private static String pickWord(ArrayList<String> dictionary, HashMap<String, Integer> words) {

        for(Map.Entry<String, Integer> entry:words.entrySet()) {

            int pickedVal = ThreadLocalRandom.current().nextInt(0, entry.getValue() + 1);
            if (pickedVal == entry.getValue()) return entry.getKey() + " ";
        }

        int indexPos = ThreadLocalRandom.current().nextInt(0, dictionary.size());
        return dictionary.get(indexPos) + " ";
    }

    private static ArrayList<String> loadDictionary() throws FileNotFoundException {
        String dictionaryPath = "third-party/english-words/words.txt";
        Scanner dictionaryScanner = new Scanner(new File(dictionaryPath));

        ArrayList<String> words = new ArrayList<>();
        while(dictionaryScanner.hasNext()) {
            words.add(dictionaryScanner.next());
        }

        return words;
    }

}
