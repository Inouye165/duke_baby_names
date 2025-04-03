// Standard Java I/O and NIO imports
import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
// Standard Java Collections imports
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
// NOTE: Comparator is NOT needed if NameCount implements Comparable
// import java.util.Comparator;
import java.util.Scanner;
// Standard Java Swing File Chooser imports
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.HeadlessException; // For environments without GUI support
// Apache Commons CSV imports (assuming library is available)
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


/**
 * Processes selected baby name CSV files using standard Java libraries.
 * Includes functionalities from the MiniProject Exercise Guide.
 * Final cleaned version, verified no duplicates, corrected CSV handling.
 */
public class ProcessBabyNames {

    // Define the base path to your data files - ADJUST IF NECESSARY
    private static final String DATA_FOLDER_PATH = "C:\\Users\\inouy\\Downloads\\us_babynames_small\\testing";
    // Define file suffix - change to ".csv" for full dataset
    private static final String FILE_SUFFIX = "short.csv";

    // Define CSV format (comma-separated, no header, read by index)
    // Using CSVFormat.DEFAULT is simplest for index-based access without headers.
    private static final CSVFormat CSV_INPUT_FORMAT = CSVFormat.DEFAULT;

    // --- Helper Record for Sorting ---
    record NameCount(String name, int count) implements Comparable<NameCount> {
        @Override
        public int compareTo(NameCount other) { return Integer.compare(other.count, this.count); } // Descending
    }

    // --- Method 1: printFileSummary ---
    /**
     * Calculates and prints the total births, number of girls' names,
     * number of boys' names, and total names for the *first* file selected by the user.
     * Uses JFileChooser for consistent file selection dialog.
     */
    public void printFileSummary() {
        System.out.println("\n==== File Summary Calculation ====");
        File selectedFile = selectSingleFile("Select File for Summary");
        if (selectedFile == null) {
             System.out.println("No file was selected for summary.");
             System.out.println("==============================");
             return;
        }

        String filename = selectedFile.getName();
        System.out.println("Generating summary for: " + filename);
        int totalBirths = 0; int totalGirlsNames = 0; int totalBoysNames = 0; int totalNames = 0;

        // Use try-with-resources for automatic closing
        try (Reader reader = Files.newBufferedReader(selectedFile.toPath());
             CSVParser parser = CSV_INPUT_FORMAT.parse(reader)) { // Use format.parse(reader)

            for (CSVRecord record : parser) {
                totalNames++;
                int currentBirths = 0;
                try { currentBirths = Integer.parseInt(record.get(2)); }
                catch (NumberFormatException nfe) { System.err.println("Warning: Could not parse number in record: " + record + " in " + filename); }
                totalBirths += currentBirths;
                String gender = record.get(1);
                if (gender.equalsIgnoreCase("F")) { totalGirlsNames++; }
                else if (gender.equalsIgnoreCase("M")) { totalBoysNames++; }
                else { System.err.println("Warning: Unexpected gender value '" + gender + "' in record: " + record + " in " + filename); }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename + " - " + e.getMessage());
        }
        // Print Summary
        System.out.println("  --- Summary for " + filename + " ---");
        System.out.println("    Total Births:        " + totalBirths); System.out.println("    Distinct Girl Names: " + totalGirlsNames);
        System.out.println("    Distinct Boy Names:  " + totalBoysNames); System.out.println("    Total Distinct Names:" + totalNames);
        System.out.println("  -----------------------------"); System.out.println("==============================");
    }

    // --- Method 2: getRank ---
    /**
     * Finds the rank of a given name for a specific gender in a specific year.
     * Rank 1 is the most popular name for that gender.
     */
    public int getRank(int year, String name, String gender) {
        String yearFilename = String.format("yob%d%s", year, FILE_SUFFIX);
        String fullPath = DATA_FOLDER_PATH + File.separator + yearFilename;
        int rank = 0; boolean found = false;

        try (Reader reader = Files.newBufferedReader(Paths.get(fullPath));
             CSVParser parser = CSV_INPUT_FORMAT.parse(reader)) { // Use format.parse(reader)
            for (CSVRecord record : parser) {
                if (record.get(1).equalsIgnoreCase(gender)) {
                    rank++;
                    if (record.get(0).equalsIgnoreCase(name)) {
                        found = true; break;
                    }
                }
            }
        } catch (IOException e) { return -1; } // File not found or error
        return found ? rank : -1;
    }

    // --- Method 3: getName ---
    /**
     * Finds the name at a specific rank for a given gender and year.
     * Rank 1 is the most popular name.
     */
     public String getName(int year, int rank, String gender) {
        if (rank < 1) return "NO NAME";
        String yearFilename = String.format("yob%d%s", year, FILE_SUFFIX);
        String fullPath = DATA_FOLDER_PATH + File.separator + yearFilename;
        int currentRank = 0;

        try (Reader reader = Files.newBufferedReader(Paths.get(fullPath));
             CSVParser parser = CSV_INPUT_FORMAT.parse(reader)) { // Use format.parse(reader)
            for (CSVRecord record : parser) {
                if (record.get(1).equalsIgnoreCase(gender)) {
                    currentRank++;
                    if (currentRank == rank) { return record.get(0); }
                }
            }
        } catch (IOException e) { return "NO NAME"; } // File not found or error
        return "NO NAME"; // Rank not found
    }

    // --- Method 4: whatIsNameInYear ---
    /**
     * Determines what a name would be in a different year based on equivalent popularity rank.
     * Prints the result to the console.
     */
    public void whatIsNameInYear(String name, int year, int newYear, String gender) {
        int originalRank = getRank(year, name, gender); if (originalRank == -1) { System.out.println("Could not find rank for " + name + " (" + gender + ") in " + year + "."); return; }
        String newName = getName(newYear, originalRank, gender); if (newName.equals("NO NAME")) { System.out.println("No name found at rank " + originalRank + " for gender " + gender + " in " + newYear + "."); return; }
        String pronoun = gender.equalsIgnoreCase("F") ? "she" : "he"; System.out.println(name + " born in " + year + " would be " + newName + " if " + pronoun + " was born in " + newYear + ".");
    }

    // --- Method 5: processAndAnalyzeFile ---
    /**
     * Processes a single baby name CSV file: prints ranked data, updates aggregation maps, returns summary totals.
     */
    public int[] processAndAnalyzeFile(File fileToProcess, String filename,
                                       Map<String, Integer> femaleTotalsMap,
                                       Map<String, Integer> maleTotalsMap,
                                       Map<String, Integer> combinedTotalsMap) {
        int totalBirths = 0; int totalGirlsNames = 0; int totalBoysNames = 0; int totalNames = 0;
        int[] results = new int[4]; int rankF = 0; int rankM = 0;

        System.out.println("Ranked Data for " + filename + ":");
        System.out.println("Rank\tName\tGender\tCount"); System.out.println("--------------------------------------");

        try (Reader reader = Files.newBufferedReader(fileToProcess.toPath());
             CSVParser parser = CSV_INPUT_FORMAT.parse(reader)) { // Use format.parse(reader)
            for (CSVRecord record : parser) {
                totalNames++; String name = record.get(0); String gender = record.get(1); String numBornStr = record.get(2);
                int currentRank = 0; int currentBirths = 0;
                try { currentBirths = Integer.parseInt(numBornStr); }
                catch (NumberFormatException e) { System.err.println("Warning: Could not parse number '" + numBornStr + "' in record: " + record + " in file: " + filename); }
                totalBirths += currentBirths;

                if (gender.equalsIgnoreCase("F")) { rankF++; currentRank = rankF; totalGirlsNames++; if (currentBirths > 0) femaleTotalsMap.put(name, femaleTotalsMap.getOrDefault(name, 0) + currentBirths); }
                else if (gender.equalsIgnoreCase("M")) { rankM++; currentRank = rankM; totalBoysNames++; if (currentBirths > 0) maleTotalsMap.put(name, maleTotalsMap.getOrDefault(name, 0) + currentBirths); }
                else { System.err.println("Warning: Unexpected gender value '" + gender + "' in record: " + record + " in file: " + filename); currentRank = 0; }
                if (currentBirths > 0) combinedTotalsMap.put(name, combinedTotalsMap.getOrDefault(name, 0) + currentBirths);
                System.out.println(currentRank + "\t" + name + "\t" + gender + "\t" + numBornStr);
            }
        } catch (IOException e) { System.err.println("Error reading file: " + filename + " - " + e.getMessage()); }
        results[0] = totalBirths; results[1] = totalGirlsNames; results[2] = totalBoysNames; results[3] = totalNames;
        return results;
    }

    // --- Method 6: runAnalysis ---
    /**
     * Main processing method using JFileChooser. Orchestrates per-file analysis,
     * grand totals, and all-time ranking.
     */
    public void runAnalysis() {
        int grandTotalBirths = 0; int grandTotalGirlsNames = 0; int grandTotalBoysNames = 0; int grandTotalNames = 0;
        Map<String, Integer> femaleTotalsAllTime = new HashMap<>(); Map<String, Integer> maleTotalsAllTime = new HashMap<>(); Map<String, Integer> combinedTotalsAllTime = new HashMap<>();

        File[] selectedFiles = selectMultipleFiles("Select Baby Name Data File(s) for Analysis");
        int filesProcessed = selectedFiles.length;
        if (filesProcessed == 0) { System.out.println("No files were selected or processed."); return; }

        for (File f : selectedFiles) {
             System.out.println("\n==== Processing file: " + f.getName() + " ====");
             int[] fileResults = processAndAnalyzeFile(f, f.getName(), femaleTotalsAllTime, maleTotalsAllTime, combinedTotalsAllTime);
             System.out.println("  --- Summary for " + f.getName() + " ---");
             System.out.println("    Total Births:        " + fileResults[0]); System.out.println("    Distinct Girl Names: " + fileResults[1]);
             System.out.println("    Distinct Boy Names:  " + fileResults[2]); System.out.println("    Total Distinct Names:" + fileResults[3]);
             System.out.println("  ------------------------------------");
             grandTotalBirths += fileResults[0]; grandTotalGirlsNames += fileResults[1]; grandTotalBoysNames += fileResults[2]; grandTotalNames += fileResults[3];
        }

        System.out.println("\n==== Grand Totals Across " + filesProcessed + " File(s) ====");
        System.out.println("  Grand Total Births:        " + grandTotalBirths); System.out.println("  Grand Total Girl Names:    " + grandTotalGirlsNames);
        System.out.println("  Grand Total Boy Names:     " + grandTotalBoysNames); System.out.println("  Grand Total Distinct Names:" + grandTotalNames);
        System.out.println("======================================");
        printAllTimeRankings(femaleTotalsAllTime, maleTotalsAllTime, combinedTotalsAllTime, filesProcessed);
    }

    // --- Method 7: printAllTimeRankings ---
    /**
     * Helper method to sort and print the aggregated all-time rankings.
     */
     private void printAllTimeRankings(Map<String, Integer> femaleMap, Map<String, Integer> maleMap, Map<String, Integer> combinedMap, int fileCount) {
        List<NameCount> femaleList = new ArrayList<>(); for (Map.Entry<String, Integer> entry : femaleMap.entrySet()) { femaleList.add(new NameCount(entry.getKey(), entry.getValue())); }
        List<NameCount> maleList = new ArrayList<>(); for (Map.Entry<String, Integer> entry : maleMap.entrySet()) { maleList.add(new NameCount(entry.getKey(), entry.getValue())); }
        List<NameCount> combinedList = new ArrayList<>(); for (Map.Entry<String, Integer> entry : combinedMap.entrySet()) { combinedList.add(new NameCount(entry.getKey(), entry.getValue())); }
        Collections.sort(femaleList); Collections.sort(maleList); Collections.sort(combinedList);
        System.out.println("\n==== All-Time Female Name Ranking (Across " + fileCount + " Files) ===="); System.out.println("Rank\tName\tTotal Births"); System.out.println("--------------------------------------"); if (femaleList.isEmpty()) { System.out.println("No female names found."); } else { for (int i = 0; i < femaleList.size(); i++) { NameCount nc = femaleList.get(i); System.out.println((i + 1) + "\t" + nc.name() + "\t" + nc.count()); } }
        System.out.println("\n==== All-Time Male Name Ranking (Across " + fileCount + " Files) ===="); System.out.println("Rank\tName\tTotal Births"); System.out.println("--------------------------------------"); if (maleList.isEmpty()) { System.out.println("No male names found."); } else { for (int i = 0; i < maleList.size(); i++) { NameCount nc = maleList.get(i); System.out.println((i + 1) + "\t" + nc.name() + "\t" + nc.count()); } }
        System.out.println("\n==== All-Time Combined Name Ranking (Across " + fileCount + " Files) ===="); System.out.println("Rank\tName\tTotal Births"); System.out.println("--------------------------------------"); if (combinedList.isEmpty()) { System.out.println("No names found."); } else { for (int i = 0; i < combinedList.size(); i++) { NameCount nc = combinedList.get(i); System.out.println((i + 1) + "\t" + nc.name() + "\t" + nc.count()); } }
        System.out.println("==========================================================");
    }

    // --- Method 8: yearOfHighestRank ---
    /**
     * Finds the year (among selected files) where the given name and gender
     * had the highest rank (lowest rank number).
     */
    public int yearOfHighestRank(String name, String gender) {
        int highestRankSoFar = Integer.MAX_VALUE; int yearOfHighestRank = -1;
        System.out.println("\nFinding year of highest rank for " + name + " (" + gender + ")");
        File[] selectedFiles = selectMultipleFiles("Select files to find highest rank year");
        if (selectedFiles.length == 0) { System.out.println("No files selected."); return -1; }

        for (File f : selectedFiles) {
            int currentYear = getYearFromFilename(f.getName()); if (currentYear == -1) { continue; }
            int currentRank = getRank(currentYear, name, gender);
            if (currentRank != -1) {
                 System.out.println("  Found rank " + currentRank + " in year " + currentYear);
                if (currentRank < highestRankSoFar) { highestRankSoFar = currentRank; yearOfHighestRank = currentYear; }
            }
        }
        if (yearOfHighestRank == -1) { System.out.println("Name/gender combination not found."); }
        else { System.out.println("Highest rank (" + highestRankSoFar + ") was in year: " + yearOfHighestRank); }
        return yearOfHighestRank;
    }

    // --- Method 9: getAverageRank ---
    /**
     * Calculates the average rank of a name/gender across selected files.
     */
    public double getAverageRank(String name, String gender) {
        double totalRank = 0.0; int rankCount = 0;
        System.out.println("\nCalculating average rank for " + name + " (" + gender + ")");
        File[] selectedFiles = selectMultipleFiles("Select files to calculate average rank");
         if (selectedFiles.length == 0) { System.out.println("No files selected."); return -1.0; }

        for (File f : selectedFiles) {
            int currentYear = getYearFromFilename(f.getName()); if (currentYear == -1) { continue; }
            int currentRank = getRank(currentYear, name, gender);
            if (currentRank != -1) {
                System.out.println("  Found rank " + currentRank + " in year " + currentYear);
                totalRank += currentRank; rankCount++;
            }
        }
        if (rankCount == 0) { System.out.println("Name/gender combination not found."); return -1.0; }
        else { double average = totalRank / rankCount; System.out.println("Average rank across " + rankCount + " file(s): " + average); return average; }
    }

    // --- Method 10: getTotalBirthsRankedHigher ---
    /**
     * Calculates the total number of births for names of the same gender
     * ranked higher than the given name in a specific year.
     */
    public int getTotalBirthsRankedHigher(int year, String name, String gender) {
        String yearFilename = String.format("yob%d%s", year, FILE_SUFFIX);
        String fullPath = DATA_FOLDER_PATH + File.separator + yearFilename;
        int totalBirthsHigher = 0; boolean targetFound = false;

        System.out.println("\nCalculating total births ranked higher than " + name + " (" + gender + ") in " + year);

        try (Reader reader = Files.newBufferedReader(Paths.get(fullPath));
             CSVParser parser = CSV_INPUT_FORMAT.parse(reader)) { // Use format.parse(reader)
            for (CSVRecord record : parser) {
                if (record.get(1).equalsIgnoreCase(gender)) {
                    String currentName = record.get(0);
                    if (currentName.equalsIgnoreCase(name)) { targetFound = true; break; }
                    try { totalBirthsHigher += Integer.parseInt(record.get(2)); }
                    catch (NumberFormatException e) { System.err.println("Warning: Could not parse number for " + currentName + " in getTotalBirthsRankedHigher"); }
                }
            }
        } catch (IOException e) { System.err.println("Error reading file " + fullPath + " in getTotalBirthsRankedHigher: " + e.getMessage()); return -1; }

        if (!targetFound) { System.out.println("Warning: Target name " + name + " (" + gender + ") not found in " + year + "."); }
        System.out.println("Total births ranked higher: " + totalBirthsHigher);
        return totalBirthsHigher;
    }

    // --- Helper Method: getYearFromFilename ---
    private int getYearFromFilename(String filename) {
        try { if (filename != null && filename.toLowerCase().startsWith("yob") && filename.length() >= 7) { return Integer.parseInt(filename.substring(3, 7)); } }
        catch (NumberFormatException | IndexOutOfBoundsException e) { /* Fall through */ }
        System.err.println("Warning: Could not parse year from filename: " + filename); return -1;
    }

    // --- Helper Methods for JFileChooser ---
    private File[] selectMultipleFiles(String dialogTitle) {
        JFileChooser chooser = new JFileChooser(); try { chooser.setCurrentDirectory(new File(DATA_FOLDER_PATH)); chooser.setDialogTitle(dialogTitle); chooser.setMultiSelectionEnabled(true); FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv"); chooser.setFileFilter(filter); int returnValue = chooser.showOpenDialog(null); if (returnValue == JFileChooser.APPROVE_OPTION) { File[] files = chooser.getSelectedFiles(); return files == null ? new File[0] : files; } } catch (HeadlessException e) { System.err.println("Error: Cannot show file chooser in this environment."); } catch (Exception e) { System.err.println("Error during file selection: " + e.getMessage()); e.printStackTrace(); } return new File[0];
    }
    private File selectSingleFile(String dialogTitle) {
        JFileChooser chooser = new JFileChooser(); try { chooser.setCurrentDirectory(new File(DATA_FOLDER_PATH)); chooser.setDialogTitle(dialogTitle); chooser.setMultiSelectionEnabled(false); FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv"); chooser.setFileFilter(filter); int returnValue = chooser.showOpenDialog(null); if (returnValue == JFileChooser.APPROVE_OPTION) { return chooser.getSelectedFile(); } } catch (HeadlessException e) { System.err.println("Error: Cannot show file chooser in this environment."); } catch (Exception e) { System.err.println("Error during file selection: " + e.getMessage()); e.printStackTrace(); } return null;
    }

    // --- Test Methods ---
    public void testGetRank() { System.out.println("\n==== Testing getRank ===="); int rank1 = getRank(2012, "Mason", "M"); System.out.println("Rank of Mason (M) in 2012: " + rank1 + " (Expected: 2)"); int rank2 = getRank(2012, "Mason", "F"); System.out.println("Rank of Mason (F) in 2012: " + rank2 + " (Expected: -1)"); int rank3 = getRank(2012, "Sophia", "F"); System.out.println("Rank of Sophia (F) in 2012: " + rank3 + " (Expected: 1)"); int rank4 = getRank(2012, "William", "M"); System.out.println("Rank of William (M) in 2012: " + rank4 + " (Expected: 5)"); System.out.println("======================="); }
    public void testGetName() { System.out.println("\n==== Testing getName ===="); String name1 = getName(2012, 1, "F"); System.out.println("Name at rank 1 (F) in 2012: " + name1 + " (Expected: Sophia)"); String name2 = getName(2012, 3, "F"); System.out.println("Name at rank 3 (F) in 2012: " + name2 + " (Expected: Isabella)"); String name3 = getName(2012, 2, "M"); System.out.println("Name at rank 2 (M) in 2012: " + name3 + " (Expected: Mason)"); String name4 = getName(2012, 6, "M"); System.out.println("Name at rank 6 (M) in 2012: " + name4 + " (Expected: NO NAME)"); String name5 = getName(2012, 0, "F"); System.out.println("Name at rank 0 (F) in 2012: " + name5 + " (Expected: NO NAME)"); String name6 = getName(2025, 1, "F"); System.out.println("Name at rank 1 (F) in 2025: " + name6 + " (Expected: NO NAME)"); System.out.println("====================="); }
    public void testWhatIsNameInYear() { System.out.println("\n==== Testing whatIsNameInYear ===="); System.out.print("Test 1: "); whatIsNameInYear("Isabella", 2012, 2014, "F"); System.out.println(); System.out.print("Test 2: "); whatIsNameInYear("Sophia", 2012, 2013, "F"); System.out.println(); System.out.print("Test 3: "); whatIsNameInYear("Mason", 2012, 2013, "M"); System.out.println(); System.out.print("Test 4: "); whatIsNameInYear("NoName", 2012, 2014, "F"); System.out.println(); System.out.print("Test 5: "); whatIsNameInYear("Sophia", 2012, 2015, "F"); System.out.println(); System.out.println("=============================="); }
    /** Tests the yearOfHighestRank method. */
    public void testYearOfHighestRank() { System.out.println("\n==== Testing yearOfHighestRank ===="); int year1 = yearOfHighestRank("Mason", "M"); System.out.println("--> Expected: 2012, Got: " + year1); int year2 = yearOfHighestRank("Sophia", "F"); System.out.println("--> Expected: 2012, Got: " + year2); int year3 = yearOfHighestRank("NonExistent", "F"); System.out.println("--> Expected: -1, Got: " + year3); System.out.println("==============================="); }
    /** Tests the getAverageRank method. */
    public void testGetAverageRank() { System.out.println("\n==== Testing getAverageRank ===="); double avg1 = getAverageRank("Mason", "M"); System.out.println("--> Expected: 3.0, Got: " + avg1); double avg2 = getAverageRank("Jacob", "M"); System.out.println("--> Expected: ~2.67, Got: " + avg2); double avg3 = getAverageRank("NonExistent", "F"); System.out.println("--> Expected: -1.0, Got: " + avg3); System.out.println("============================"); }
    /** Tests the getTotalBirthsRankedHigher method. */
    public void testGetTotalBirthsRankedHigher() { System.out.println("\n==== Testing getTotalBirthsRankedHigher ===="); int total1 = getTotalBirthsRankedHigher(2012, "Ethan", "M"); System.out.println("--> Expected: 15, Got: " + total1); int total2 = getTotalBirthsRankedHigher(2012, "Sophia", "F"); System.out.println("--> Expected: 0, Got: " + total2); int total3 = getTotalBirthsRankedHigher(2012, "Isabella", "F"); System.out.println("--> Expected: 19, Got: " + total3); int total4 = getTotalBirthsRankedHigher(2012, "NonExistent", "F"); System.out.println("--> (Name not found) Total births higher: " + total4); System.out.println("===================================="); }


    // --- Susan Method ---
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); System.out.println("--- Baby Name Analysis ---");
        System.out.print("Enter the first name given at birth (for analysis): "); String userBirthName = scanner.nextLine();
        System.out.print("Enter the birth year for " + userBirthName + ": "); int userBirthYear = -1;
        try { userBirthYear = Integer.parseInt(scanner.nextLine()); } catch (NumberFormatException e) { System.err.println("Invalid year entered. Cannot perform name comparison."); userBirthName = ""; }
        String userGender = "";
        if (!userBirthName.isEmpty()) { System.out.print("Enter the gender for " + userBirthName + " (F or M): "); userGender = scanner.nextLine().toUpperCase(); if (!userGender.equals("F") && !userGender.equals("M")) { System.err.println("Invalid gender entered. Assuming 'F' for comparison."); userGender = "F"; } }
        scanner.close();

        System.out.println("\nStarting analysis...");
        ProcessBabyNames processor = new ProcessBabyNames();
        processor.runAnalysis(); // Run main analysis using JFileChooser

        System.out.println("\n--- Your Name Comparison ---");
        int targetYear = 2014;
        if (userBirthYear != -1 && !userBirthName.isEmpty() && !userGender.isEmpty()) { processor.whatIsNameInYear(userBirthName, userBirthYear, targetYear, userGender); }
        else { System.out.println("Skipping name comparison due to invalid input earlier."); }
        System.out.println("--------------------------");

        // --- Optional: Uncomment to run specific tests ---
        // Note: printFileSummary, testYearOfHighestRank, testGetAverageRank will open file dialogs.
        // System.out.println("\n--- Running Test Methods ---");
        // processor.printFileSummary();
        // processor.testGetRank();
        // processor.testGetName();
        // processor.testWhatIsNameInYear();
        // processor.testYearOfHighestRank();
        // processor.testGetAverageRank();
        // processor.testGetTotalBirthsRankedHigher();
        // System.out.println("--- Finished Test Methods ---");

        System.out.println("\nAnalysis complete.");
    }
} 
