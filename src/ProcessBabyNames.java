import edu.duke.*;
import org.apache.commons.csv.*;
import java.io.File;

/**
 * Processes selected baby name CSV files.
 * Prints analysis totals for each file individually.
 * Calculates and prints grand totals across all selected files at the end.
 */
public class ProcessBabyNames {

    /**
     * Analyzes a single baby name CSV file to calculate totals for that file.
     * Assumes CSV has no header and columns are: Name, Gender (F/M), Count.
     *
     * @param fr        FileResource representing the CSV file to analyze.
     * @param filename  The name of the file being processed (for error messages).
     * @return An int array containing [totalBirths, distinctGirlNames, distinctBoyNames, totalDistinctNames] for the given file.
     */
    public int[] analyzeBirthData(FileResource fr, String filename) {
        int totalBirths = 0;
        int totalGirlsNames = 0; // Counts distinct names listed as F in this file
        int totalBoysNames = 0;  // Counts distinct names listed as M in this file
        int totalNames = 0;      // Counts total distinct names (lines) in this file
        int[] results = new int[4]; // Array to hold results [births, girls, boys, total]

        // Get parser, ensuring no header row is expected
        CSVParser parser = fr.getCSVParser(false);

        for (CSVRecord record : parser) {
            totalNames++; // Each row represents a distinct name entry in the file

            // --- Calculate Total Births for this file ---
            int currentBirths = 0;
            try {
                currentBirths = Integer.parseInt(record.get(2));
            } catch (NumberFormatException e) {
                System.err.println("Warning: Could not parse number in record: " + record + " in file: " + filename);
            }
            totalBirths += currentBirths;

            // --- Count Names by Gender for this file ---
            String gender = record.get(1);
            if (gender.equalsIgnoreCase("F")) {
                totalGirlsNames++;
            } else if (gender.equalsIgnoreCase("M")) {
                totalBoysNames++;
            } else {
                 System.err.println("Warning: Unexpected gender value '" + gender + "' in record: " + record + " in file: " + filename);
            }
        }

        // Store results in the array
        results[0] = totalBirths;
        results[1] = totalGirlsNames;
        results[2] = totalBoysNames;
        results[3] = totalNames;

        return results; // Return the calculated totals for this file
    }


    /**
     * Processes selected CSV files, prints individual file summaries,
     * calculates grand totals, and prints the final grand total summary.
     */
    public void processSelectedFilesAndGetTotals() {
        // Initialize grand totals before processing any files
        int grandTotalBirths = 0;
        int grandTotalGirlsNames = 0;
        int grandTotalBoysNames = 0;
        int grandTotalNames = 0;
        int filesProcessed = 0;

        // Set starting directory
        System.setProperty("user.dir", "C:\\Users\\inouy\\Downloads\\us_babynames_small\\testing");

        System.out.println("Opening file selection dialog. Please select files for analysis:");
        DirectoryResource dr = new DirectoryResource();

        // Process each selected file
        for (File f : dr.selectedFiles()) {
             System.out.println("---- Processing file: " + f.getName() + " ----");
             filesProcessed++;
             FileResource fr = new FileResource(f);

             // Call the analysis method to get totals for the current file
             int[] fileResults = analyzeBirthData(fr, f.getName());

             // *** ADDED: Print Individual File Totals Here ***
             System.out.println("  Analysis Results for " + f.getName() + ":");
             System.out.println("    Total Births:        " + fileResults[0]); // Index 0: births
             System.out.println("    Distinct Girl Names: " + fileResults[1]); // Index 1: girls
             System.out.println("    Distinct Boy Names:  " + fileResults[2]); // Index 2: boys
             System.out.println("    Total Distinct Names:" + fileResults[3]); // Index 3: total names
             System.out.println("  ------------------------------------"); // Separator for file results


             // Add the results from this file to the grand totals
             grandTotalBirths += fileResults[0];
             grandTotalGirlsNames += fileResults[1];
             grandTotalBoysNames += fileResults[2];
             grandTotalNames += fileResults[3];
        }

        // --- Print Grand Totals After Processing All Files ---
        if (filesProcessed > 0) {
            System.out.println("\n==== Grand Totals Across " + filesProcessed + " File(s) ====");
            System.out.println("  Grand Total Births:        " + grandTotalBirths);
            System.out.println("  Grand Total Girl Names:    " + grandTotalGirlsNames);
            System.out.println("  Grand Total Boy Names:     " + grandTotalBoysNames);
            System.out.println("  Grand Total Distinct Names:" + grandTotalNames);
            System.out.println("======================================");
        } else {
            System.out.println("No files were selected or processed.");
        }
    }

    /**
     * Main method.
     */
    public static void main(String[] args) {
        ProcessBabyNames processor = new ProcessBabyNames();
        processor.processSelectedFilesAndGetTotals();
    }
}
