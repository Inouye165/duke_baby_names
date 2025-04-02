import edu.duke.*;
import org.apache.commons.csv.*;
import java.io.File;
import java.util.HashMap; // For aggregating totals
import java.util.Map;    // Map interface
import java.util.List;   // For sorting
import java.util.ArrayList; // For sorting
import java.util.Collections; // For sorting

/**
 * Processes selected baby name CSV files.
 * 1. Prints each record ranked by gender within the file.
 * 2. Prints analysis totals for each file individually.
 * 3. Calculates and prints grand totals across all selected files.
 * 4. Calculates and prints "all-time" rankings (Female, Male, Combined)
 * based on total births across all selected files.
 */
public class ProcessBabyNames {

    // --- Helper Record for Sorting ---
    record NameCount(String name, int count) implements Comparable<NameCount> {
        @Override
        public int compareTo(NameCount other) {
            return Integer.compare(other.count, this.count); // Descending count
        }
    }

    /**
     * Processes a single baby name CSV file:
     * 1. Prints each record prefixed with its gender-specific rank within the file.
     * 2. Updates maps tracking total births per name/gender for all-time ranking.
     * 3. Calculates and returns summary totals (births, gender counts, total names) for this file.
     *
     * @param fr                 FileResource representing the CSV file to analyze.
     * @param filename           The name of the file being processed.
     * @param femaleTotalsMap    Map to accumulate total female births per name across all files.
     * @param maleTotalsMap      Map to accumulate total male births per name across all files.
     * @param combinedTotalsMap  Map to accumulate total births per name (regardless of gender) across all files.
     * @return An int array containing [totalBirths, distinctGirlNames, distinctBoyNames, totalDistinctNames] for the given file summary.
     */
    public int[] processAndAnalyzeFile(FileResource fr, String filename,
                                       Map<String, Integer> femaleTotalsMap,
                                       Map<String, Integer> maleTotalsMap,
                                       Map<String, Integer> combinedTotalsMap) {
        // Per-file summary counters
        int totalBirths = 0;
        int totalGirlsNames = 0;
        int totalBoysNames = 0;
        int totalNames = 0;
        int[] results = new int[4];
        // Per-file rank counters
        int rankF = 0;
        int rankM = 0;

        System.out.println("Ranked Data for " + filename + ":");
        System.out.println("Rank\tName\tGender\tCount");
        System.out.println("--------------------------------------");

        CSVParser parser = fr.getCSVParser(false);

        for (CSVRecord record : parser) {
            totalNames++;
            String name = record.get(0);
            String gender = record.get(1);
            String numBornStr = record.get(2);
            int currentRank = 0;
            int currentBirths = 0;

             // --- Parse Births (used for totals and aggregation) ---
            try {
                currentBirths = Integer.parseInt(numBornStr);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Could not parse number '" + numBornStr + "' in record: " + record + " in file: " + filename);
                // Skip aggregation for this record if births invalid, but still print
            }
            totalBirths += currentBirths; // Add to per-file total births

            // --- Determine Rank and Update Gender Counts/Maps ---
            if (gender.equalsIgnoreCase("F")) {
                rankF++;
                currentRank = rankF;
                totalGirlsNames++;
                if (currentBirths > 0) { // Only aggregate if births were parsed ok
                   femaleTotalsMap.put(name, femaleTotalsMap.getOrDefault(name, 0) + currentBirths);
                }
            } else if (gender.equalsIgnoreCase("M")) {
                rankM++;
                currentRank = rankM;
                totalBoysNames++;
                 if (currentBirths > 0) { // Only aggregate if births were parsed ok
                   maleTotalsMap.put(name, maleTotalsMap.getOrDefault(name, 0) + currentBirths);
                 }
            } else {
                 System.err.println("Warning: Unexpected gender value '" + gender + "' in record: " + record + " in file: " + filename);
                 currentRank = 0;
            }

            // --- Update Combined Map (regardless of gender, if births valid) ---
             if (currentBirths > 0) {
                combinedTotalsMap.put(name, combinedTotalsMap.getOrDefault(name, 0) + currentBirths);
             }

            // --- Print the ranked data line for this file ---
            System.out.println(currentRank + "\t" + name + "\t" + gender + "\t" + numBornStr);

        } // End record loop

        // Store per-file summary results
        results[0] = totalBirths;
        results[1] = totalGirlsNames;
        results[2] = totalBoysNames;
        results[3] = totalNames;

        return results; // Return per-file summary totals
    }


    /**
     * Main processing method. Orchestrates file selection, per-file processing
     * (ranked printout, summary printout, aggregation), grand total calculation/printout,
     * and all-time ranking calculation/printout.
     */
    public void runAnalysis() { // Renamed for clarity
        // Initialize grand totals
        int grandTotalBirths = 0;
        int grandTotalGirlsNames = 0;
        int grandTotalBoysNames = 0;
        int grandTotalNames = 0;
        int filesProcessed = 0;

        // Initialize maps for all-time aggregation
        Map<String, Integer> femaleTotalsAllTime = new HashMap<>();
        Map<String, Integer> maleTotalsAllTime = new HashMap<>();
        Map<String, Integer> combinedTotalsAllTime = new HashMap<>();


        // Set starting directory
        System.setProperty("user.dir", "C:\\Users\\inouy\\Downloads\\us_babynames_small\\testing"); // Adjust if needed

        System.out.println("Opening file selection dialog. Please select files for analysis:");
        DirectoryResource dr = new DirectoryResource();

        // --- Process Each Selected File ---
        for (File f : dr.selectedFiles()) {
             System.out.println("\n==== Processing file: " + f.getName() + " ====");
             filesProcessed++;
             FileResource fr = new FileResource(f);

             // Call method to print ranked data, update aggregation maps, AND get per-file summary totals
             int[] fileResults = processAndAnalyzeFile(fr, f.getName(),
                                                      femaleTotalsAllTime, // Pass maps
                                                      maleTotalsAllTime,
                                                      combinedTotalsAllTime);

             // --- Print Individual File Summary Totals ---
             System.out.println("  --- Summary for " + f.getName() + " ---");
             System.out.println("    Total Births:        " + fileResults[0]);
             System.out.println("    Distinct Girl Names: " + fileResults[1]);
             System.out.println("    Distinct Boy Names:  " + fileResults[2]);
             System.out.println("    Total Distinct Names:" + fileResults[3]);
             System.out.println("  ------------------------------------");

             // --- Accumulate Grand Totals ---
             grandTotalBirths += fileResults[0];
             grandTotalGirlsNames += fileResults[1];
             grandTotalBoysNames += fileResults[2];
             grandTotalNames += fileResults[3];
        } // --- End File Loop ---

        // --- Print Grand Totals ---
        if (filesProcessed > 0) {
            System.out.println("\n==== Grand Totals Across " + filesProcessed + " File(s) ====");
            System.out.println("  Grand Total Births:        " + grandTotalBirths);
            System.out.println("  Grand Total Girl Names:    " + grandTotalGirlsNames);
            System.out.println("  Grand Total Boy Names:     " + grandTotalBoysNames);
            System.out.println("  Grand Total Distinct Names:" + grandTotalNames);
            System.out.println("======================================");

            // --- Calculate and Print All-Time Rankings ---
            printAllTimeRankings(femaleTotalsAllTime, maleTotalsAllTime, combinedTotalsAllTime, filesProcessed);

        } else {
            System.out.println("No files were selected or processed.");
        }
    }

    /**
     * Helper method to sort and print the aggregated all-time rankings.
     * @param femaleMap Map of female names to total births.
     * @param maleMap Map of male names to total births.
     * @param combinedMap Map of all names to total births.
     * @param fileCount Number of files processed for context.
     */
    private void printAllTimeRankings(Map<String, Integer> femaleMap,
                                      Map<String, Integer> maleMap,
                                      Map<String, Integer> combinedMap,
                                      int fileCount) {

        // Convert maps to lists for sorting
        List<NameCount> femaleList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : femaleMap.entrySet()) { femaleList.add(new NameCount(entry.getKey(), entry.getValue())); }

        List<NameCount> maleList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : maleMap.entrySet()) { maleList.add(new NameCount(entry.getKey(), entry.getValue())); }

        List<NameCount> combinedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : combinedMap.entrySet()) { combinedList.add(new NameCount(entry.getKey(), entry.getValue())); }

        // Sort lists (descending by count)
        Collections.sort(femaleList);
        Collections.sort(maleList);
        Collections.sort(combinedList);

        // Print Female Rankings
        System.out.println("\n==== All-Time Female Name Ranking (Across " + fileCount + " Files) ====");
        System.out.println("Rank\tName\tTotal Births"); System.out.println("--------------------------------------");
        if (femaleList.isEmpty()) { System.out.println("No female names found."); }
        else { for (int i = 0; i < femaleList.size(); i++) { NameCount nc = femaleList.get(i); System.out.println((i + 1) + "\t" + nc.name() + "\t" + nc.count()); } }

        // Print Male Rankings
        System.out.println("\n==== All-Time Male Name Ranking (Across " + fileCount + " Files) ====");
        System.out.println("Rank\tName\tTotal Births"); System.out.println("--------------------------------------");
        if (maleList.isEmpty()) { System.out.println("No male names found."); }
        else { for (int i = 0; i < maleList.size(); i++) { NameCount nc = maleList.get(i); System.out.println((i + 1) + "\t" + nc.name() + "\t" + nc.count()); } }

        // Print Combined Rankings
        System.out.println("\n==== All-Time Combined Name Ranking (Across " + fileCount + " Files) ====");
        System.out.println("Rank\tName\tTotal Births"); System.out.println("--------------------------------------");
         if (combinedList.isEmpty()) { System.out.println("No names found."); }
         else { for (int i = 0; i < combinedList.size(); i++) { NameCount nc = combinedList.get(i); System.out.println((i + 1) + "\t" + nc.name() + "\t" + nc.count()); } }

        System.out.println("==========================================================");
    }


    // --- getRank method remains available if needed ---
    public int getRank(int year, String name, String gender) { /* ... code from previous step ... */
        String filename = String.format("data/yob%dshort.csv", year); FileResource fr = null;
        try { fr = new FileResource(filename); } catch (Exception e) { System.err.println("Error opening file: " + filename + " - " + e.getMessage()); return -1; }
        CSVParser parser = fr.getCSVParser(false); int rank = 0;
        for (CSVRecord record : parser) { if (record.get(1).equalsIgnoreCase(gender)) { rank++; if (record.get(0).equalsIgnoreCase(name)) { return rank; } } } return -1;
     }
    // --- testGetRank method remains available if needed ---
     public void testGetRank() { /* ... code from previous step ... */
        System.out.println("\n==== Testing getRank ====");
        int rank1 = getRank(2012, "Mason", "M"); System.out.println("Rank of Mason (M) in 2012: " + rank1 + " (Expected: 2)");
        int rank2 = getRank(2012, "Mason", "F"); System.out.println("Rank of Mason (F) in 2012: " + rank2 + " (Expected: -1)");
        int rank3 = getRank(2012, "Sophia", "F"); System.out.println("Rank of Sophia (F) in 2012: " + rank3 + " (Expected: 1)");
        int rank4 = getRank(2012, "William", "M"); System.out.println("Rank of William (M) in 2012: " + rank4 + " (Expected: 5)");
        System.out.println("=======================");
     }

    /**
     * Main method. Calls the primary analysis method.
     */
    public static void main(String[] args) {
        ProcessBabyNames processor = new ProcessBabyNames();
        processor.runAnalysis(); // Call the main method that does everything
    }
}
