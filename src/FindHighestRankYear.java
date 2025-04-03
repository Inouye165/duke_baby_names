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

public class FindHighestRankYear {

    public static void main(String[] args) {
        // Get user input for name and gender.
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter the gender (M or F): ");
        String gender = scanner.nextLine().trim().toUpperCase();
        scanner.close();
        
        // Select CSV files (e.g., from 1880 to 2014) using JFileChooser.
        File[] files = selectMultipleFiles("Select CSV files (1880-2014)");
        if (files.length == 0) {
            System.out.println("No files selected.");
            return;
        }
        
        int bestRank = Integer.MAX_VALUE;
        int bestYear = -1;
        
        // Process each file.
        for (File file : files) {
            int currentYear = getYearFromFilename(file.getName());
            if (currentYear == -1) {
                continue;
            }
            int rank = getRankFromFile(file, name, gender);
            // If the name is found in this file and has a better rank (lower number)
            if (rank != -1 && rank < bestRank) {
                bestRank = rank;
                bestYear = currentYear;
            }
        }
        
        if (bestYear != -1) {
            System.out.println("The name " + name + " (" + gender + ") has its highest rank (rank " 
                + bestRank + ") in the year: " + bestYear);
        } else {
            System.out.println("The name " + name + " (" + gender + ") was not found in any selected file.");
        }
    }
    
    // Reads a CSV file and returns the rank of the given name for the specified gender.
    // Rank 1 is the first occurrence among records matching the gender.
    private static int getRankFromFile(File file, String name, String gender) {
        int rank = 0;
        boolean found = false;
        try (Reader reader = Files.newBufferedReader(file.toPath());
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            for (CSVRecord record : parser) {
                // Assuming each record: index 0 = name, 1 = gender, 2 = count.
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
        }
        return found ? rank : -1;
    }
    
    // Extracts the year from the filename.
    // Assumes the filename starts with "yob" followed by a 4-digit year.
    private static int getYearFromFilename(String filename) {
        try {
            if (filename != null && filename.toLowerCase().startsWith("yob") && filename.length() >= 7) {
                return Integer.parseInt(filename.substring(3, 7));
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            // Ignore and fall through.
        }
        System.err.println("Warning: Could not parse year from filename: " + filename);
        return -1;
    }
    
    // Displays a file chooser for multiple CSV files.
    private static File[] selectMultipleFiles(String dialogTitle) {
        JFileChooser chooser = new JFileChooser();
        try {
            chooser.setDialogTitle(dialogTitle);
            chooser.setMultiSelectionEnabled(true);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
            chooser.setFileFilter(filter);
            int returnValue = chooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File[] files = chooser.getSelectedFiles();
                return files == null ? new File[0] : files;
            }
        } catch (HeadlessException e) {
            System.err.println("Error: Cannot show file chooser in this environment.");
        } catch (Exception e) {
            System.err.println("Error during file selection: " + e.getMessage());
            e.printStackTrace();
        }
        return new File[0];
    }
}
