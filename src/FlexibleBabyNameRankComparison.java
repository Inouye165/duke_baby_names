import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class FlexibleBabyNameRankComparison {

    // Set the default folder where the CSV files are stored.
    private static final String DEFAULT_DATA_FOLDER = "C:\\Users\\inouy\\Downloads\\us_babynames\\us_babynames_by_year";
    // The suffix of the CSV files (adjust if needed).
    private static final String FILE_SUFFIX = ".csv";
    // CSV format used for reading the files.
    private static final CSVFormat CSV_INPUT_FORMAT = CSVFormat.DEFAULT;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Get input for name, gender, birth year, and target year.
        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter your gender (M or F): ");
        String gender = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter your birth year: ");
        int birthYear = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter the target year for comparison: ");
        int targetYear = Integer.parseInt(scanner.nextLine().trim());
        scanner.close();
        
        // Use a file chooser to select the file for the birth year.
        File birthFile = selectFileForYear("Select CSV file for your birth year (" + birthYear + ")", birthYear);
        if (birthFile == null) {
            System.out.println("No birth year file selected. Exiting.");
            return;
        }
        
        // Use a file chooser to select the file for the target year.
        File targetFile = selectFileForYear("Select CSV file for the target year (" + targetYear + ")", targetYear);
        if (targetFile == null) {
            System.out.println("No target year file selected. Exiting.");
            return;
        }
        
        // Look up the rank of the given name in the birth year file.
        int rank = getRank(birthFile, name, gender);
        if (rank == -1) {
            System.out.println("The name \"" + name + "\" (" + gender + ") was not found in the file for " + birthYear + ".");
            return;
        }
        
        // Look up the name in the target year file with the same rank.
        String targetName = getName(targetFile, rank, gender);
        if (targetName.equals("NO NAME")) {
            System.out.println("No name found at rank " + rank + " for gender " + gender + " in " + targetYear + ".");
            return;
        }
        
        // Display the result.
        String pronoun = gender.equals("F") ? "she" : "he";
        System.out.println(name + " born in " + birthYear + " would be " + targetName +
                           " if " + pronoun + " was born in " + targetYear + ".");
    }
    
    /**
     * Opens a JFileChooser (with the default folder set) to select a CSV file.
     * After selection, it checks if the file's name contains the expected year.
     * Returns the selected file or null if none is chosen.
     */
    private static File selectFileForYear(String dialogTitle, int expectedYear) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(DEFAULT_DATA_FOLDER));
        chooser.setDialogTitle(dialogTitle);
        chooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*" + FILE_SUFFIX + ")", "csv");
        chooser.setFileFilter(filter);
        
        int returnValue = chooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            int fileYear = getYearFromFilename(file.getName());
            if (fileYear != expectedYear) {
                System.out.println("Warning: The selected file's year (" + fileYear + ") does not match the expected year (" + expectedYear + ").");
            }
            return file;
        }
        return null;
    }
    
    /**
     * Returns the rank of the given name for the specified gender using the provided file.
     * Rank 1 is the most popular name. Returns -1 if the name is not found.
     */
    public static int getRank(File file, String name, String gender) {
        int rank = 0;
        boolean found = false;
        try (Reader reader = Files.newBufferedReader(file.toPath());
             CSVParser parser = CSV_INPUT_FORMAT.parse(reader)) {
            for (CSVRecord record : parser) {
                // CSV record structure: 0=name, 1=gender, 2=count.
                if (record.get(1).equalsIgnoreCase(gender)) {
                    rank++;
                    if (record.get(0).equalsIgnoreCase(name)) {
                        found = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
            return -1;
        }
        return found ? rank : -1;
    }
    
    /**
     * Returns the name from the given file that has the specified rank for the given gender.
     * If no such name is found, returns "NO NAME".
     */
    public static String getName(File file, int rank, String gender) {
        if (rank < 1) return "NO NAME";
        int currentRank = 0;
        try (Reader reader = Files.newBufferedReader(file.toPath());
             CSVParser parser = CSV_INPUT_FORMAT.parse(reader)) {
            for (CSVRecord record : parser) {
                if (record.get(1).equalsIgnoreCase(gender)) {
                    currentRank++;
                    if (currentRank == rank) {
                        return record.get(0);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
            return "NO NAME";
        }
        return "NO NAME";
    }
    
    /**
     * Extracts the year from the filename.
     * Assumes the filename starts with "yob" followed by a 4-digit year.
     */
    private static int getYearFromFilename(String filename) {
        try {
            if (filename != null && filename.toLowerCase().startsWith("yob") && filename.length() >= 7) {
                return Integer.parseInt(filename.substring(3, 7));
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            // Fall through.
        }
        System.err.println("Warning: Could not parse year from filename: " + filename);
        return -1;
    }
}
