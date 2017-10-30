package uk.ac.standrews.cs.synthetic_datasets.web_crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class Crawler {

    private static final String CRAWLER_ENDPOINTS_FILE = "src/main/resources/crawler_endpoints.txt";
    private static final String CRAWLER_DATASET = "datasets/crawler/";
    private static final int DOWNLOAD_BUFFER_SIZE = 4096;

    private static final int NUMBER_OF_FILES_LIMIT = 2048;
    private static final int DATASET_SIZE_LIMIT_MB = 10 * 1024 * 1024;

    private static int numberOfFiles;
    private static int datasetSize;
    private static HashMap<String, Long> visitedSites;

    public static void main(String[] args) {

        visitedSites = new LinkedHashMap<>();
        Queue<String> endPoints = new LinkedList<>(readEndPoints(CRAWLER_ENDPOINTS_FILE));

        while(!endPoints.isEmpty() && numberOfFiles < NUMBER_OF_FILES_LIMIT && datasetSize < DATASET_SIZE_LIMIT_MB) {
            try {
                crawl(endPoints);
            } catch (IOException| URISyntaxException | CrawlerException e) {
                System.err.println("Unable to process all endPoints");
                continue;
            }
        }
    }

    private static List<String> readEndPoints(String filepath) {

        List<String> endPoints = new LinkedList<>();

        File file = new File(filepath);
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)){

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                endPoints.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return endPoints;
    }

    private static void crawl(Queue<String> endPoints) throws IOException, URISyntaxException, CrawlerException {

        String uriToCrawl = endPoints.poll();
        System.err.println("Crawling " + uriToCrawl);
        visitedSites.put(uriToCrawl, System.nanoTime());

        boolean isHTTPFamily = uriToCrawl.startsWith("http://") || uriToCrawl.startsWith("https://");
        if (isHTTPFamily) {
            addContent(uriToCrawl);
        } else {
            System.err.println("Not HTTP Resource " + uriToCrawl);
            throw new CrawlerException();
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(uriToCrawl).openConnection();

        boolean redirect = false;
        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)
                redirect = true;
        }

        if (redirect) { // get redirect url from "location" header field
            uriToCrawl = connection.getHeaderField("Location");
        }
        connection = (HttpURLConnection) new URL(uriToCrawl).openConnection();


        String contentType = connection.getHeaderField("Content-Type");
        if (contentType == null) {
            throw new CrawlerException();
        }

        boolean isHTML = contentType.startsWith("text/") || contentType.equals("application/xml") || contentType.equals("application/xhtml+xml");
        if (!isHTML) {
            System.err.println("Resource is not an HTML page " + uriToCrawl);
            throw new CrawlerException();
        }

        Document doc = Jsoup.connect(uriToCrawl).get();

        Elements links = doc.select("a[href]");
        Elements srcs = doc.select("[src]");

        for (Element src : srcs) {
            String srcURI = src.attr("abs:src");
            if (!visitedSites.containsKey(srcURI)) { // TODO - expire key after X time
                endPoints.add(srcURI);
            }
        }

        for(Element link:links) {
            String linkURI = link.attr("abs:href");

            if (!visitedSites.containsKey(linkURI)) { // TODO - expire key after X time
                endPoints.add(linkURI);
            }
        }
    }

    private static void addContent(String srcURI) throws URISyntaxException, IOException {

        createFolderForHost(srcURI);

        String path = targetFilePath(srcURI);
        File targetFile = new File(CRAWLER_DATASET + path);

        if (!targetFile.exists()) {

            makePath(targetFile.getPath());
            downloadUsingStream(srcURI, targetFile.getPath());

            numberOfFiles++;
            datasetSize += targetFile.length();

            System.out.println("data saved to " + targetFile.getAbsolutePath());
        }
    }

    private static void downloadUsingStream(String urlStr, String file) throws IOException{
        URL url = new URL(urlStr);

        try (BufferedInputStream bis = new BufferedInputStream(url.openStream());
             FileOutputStream fis = new FileOutputStream(file)) {

            byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
            int count = 0;
            while ((count = bis.read(buffer, 0, DOWNLOAD_BUFFER_SIZE)) != -1) {
                fis.write(buffer, 0, count);
            }

        }
    }

    private static String targetFilePath(String srcURI) throws URISyntaxException {

        URI uri = new URI(srcURI);
        String path = srcURI.substring(srcURI.indexOf("://") + 3);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String retval;
        if (path.equals(uri.getHost())) {
            retval = uri.getHost() + "/" + path;
        } else {
            retval = path;
        }

        System.out.println("Saving content from URI " + srcURI + " to path " + retval);
        return retval;
    }

    private static void createFolderForHost(String srcURI) throws URISyntaxException {

        URI uri = new URI(srcURI);
        makePath(CRAWLER_DATASET + uri.getHost() + "/");
    }

    private static void makePath(String path) {
        java.io.File file = new java.io.File(path);
        java.io.File parent = file.getParentFile();
        if (parent != null)
            parent.mkdirs();

        if (path.endsWith("/")) {
            file.mkdir();
        }
    }

}
