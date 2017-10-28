package uk.ac.standrews.cs.synthetic_datasets.file_system;

import java.util.List;

/**
 * Stats for normal distributions
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class FS_stats {

    private int numberOfFiles;
    private double averageFileSize;
    private double stdDevFileSize;

    private int numberOfDirectories;
    private double averageFilesPerDirectory;
    private double stdDevFilesPerDirectory;

    private int minDepth;
    private int maxDepth;
    private double averageDepth;
    private double stdDevDepth;

    // [ (pdf, 103), (jpg, 3420), ... (type, #occurrences)]
    private List<Pair<String, Integer> > fileTypes;


    private class Pair<X, Y> {

        private X x;
        private Y y;

        public Pair(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        public X getX() {
            return x;
        }

        public Y getY() {
            return y;
        }
    }
}
