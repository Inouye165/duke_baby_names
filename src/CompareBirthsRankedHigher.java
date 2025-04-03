import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.HeadlessException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CompareBirthsRankedHigher {

    public static void main(String[] args) {
        // Prompt the user for the name, gender, base year, and target year.
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the name (from the base year): ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter the gender (M or F): ");
        String gender = scanner.nextLine().trim().toUpperCase();
        System.out.print("Enter the base year (to get rank): ");
        int baseYear = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter the target year (to sum births for names ranked higher): ");
        int targetYear = Integer.parseInt(scanner.nextLine().trim());
        scanner.close();
        
        // Select the CSV file for the base year.
        File baseFile = selectSingleFile("Select CSV file for base year " + baseYear);
        if (baseFile == null) {
            System.out.println("No base year file selected.");
            return;
        }
        int fileBaseYear = getYearFromFilename(baseFile.getName());
        if (fileBaseYear != baseYear) {
            System.out.println("Warning: Selected base file's year (" + fileBaseYear 
                + ") does not match the entered base year (" + baseYear + ").");
        }
        
        // Select the CSV file for the target year.
        File targetFile = selectSingleFile("Select CSV file for target year " + targetYear);
        if (targetFile == null) {
            System.out.println("No target year file selected.");
            return;
        }
        int fileTargetYear = getYearFromFilename(targetFile.getName());
        if (fileTargetYear != targetYear) {
            System.out.println("Warning: Selected target file's year (" + fileTargetYear 
                + ") does not match the entered target year (" + targetYear + ").");
        }
        
        // Get the rank of the name in the base year file.
        int baseRank = getRankFromFile(baseFile, name, gender);
        if (baseRank == -1) {
            System.out.println("The name \"" + name + "\" (" + gender 
                + ") was not found in the base year file (" + baseYear + ").");
            return;
        }
        
        // In the target year file, sum the births for names (of the same gender)
        // that have a rank lower than the base rank (i.e., ranked higher).
        int totalBirthsHigher = getTotalBirthsForTopRanks(targetFile, gender, baseRank);
        
        System.out.println("In " + targetYear + ", the total number of " 
            + (gender.equals("F") ? "girls" : "boys")
            + " with names ranked higher than the rank of \"" + name + "\" in " + baseYear 
            + " (rank " + baseRank + ") is: " + totalBirthsHigher);
    }
    
    /**
     * Reads the CSV file and returns the rank of the given name for the specified gender.
     * Assumes the CSV file is sorted in order of ranking (most popular name first).
     * Returns -1 if the name is not found.
     */
    private static int getRankFromFile(File file, String name, String gender) {
        int rank = 0;
        boolean found = false;
        try (Reader reader = Files.newBufferedReader(file.toPath());
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            for (CSVRecord record : parser) {
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
     * Sums the number of births for records (of the specified gender) in the target file
     * whose rank is less than the given rank threshold.
     * That is, if the target rank is R, it sums the births for names ranked 1 to R-1.
     */
    private static int getTotalBirthsForTopRanks(File file, String gender, int rankThreshold) {
        int totalBirths = 0;
        int currentRank = 0;
        try (Reader reader = Files.newBufferedReader(file.toPath());
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            for (CSVRecord record : parser) {
                if (record.get(1).equalsIgnoreCase(gender)) {
                    currentRank++;
                    if (currentRank < rankThreshold) {
                        try {
                            int count = Integer.parseInt(record.get(2));
                            totalBirths += count;
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Could not parse count for record: " + record);
                        }
                    } else {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
            return -1;
        }
        return totalBirths;
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
    
    /**
     * Displays a JFileChooser for selecting a single CSV file.
     */
    private static File selectSingleFile(String dialogTitle) {
        JFileChooser chooser = new JFileChooser();
        try {
            chooser.setDialogTitle(dialogTitle);
            chooser.setMultiSelectionEnabled(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
            chooser.setFileFilter(filter);
            int returnValue = chooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile();
            }
        } catch (HeadlessException e) {
            System.err.println("Error: Cannot show file chooser in this environment.");
        } catch (Exception e) {
            System.err.println("Error during file selection: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
