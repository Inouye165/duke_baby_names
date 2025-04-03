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

public class NameRankLookup {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Prompt for the year, name, and gender.
        System.out.print("Enter the year: ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter the name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter the gender (M or F): ");
        String gender = scanner.nextLine().trim().toUpperCase();
        scanner.close();
        
        // Ask the user to select the CSV file for the given year.
        File selectedFile = selectFile("Select CSV file for year " + year);
        if (selectedFile == null) {
            System.out.println("No file selected. Exiting.");
            return;
        }
        
        // Look up the rank of the given name in the file.
        int rank = getRank(selectedFile, name, gender);
        if (rank == -1) {
            System.out.println("The name \"" + name + "\" (" + gender + ") was not found in the file for year " + year + ".");
        } else {
            System.out.println("The rank of \"" + name + "\" (" + gender + ") in year " + year + " is: " + rank);
        }
    }
    
    /**
     * Reads the selected CSV file and returns the rank of the given name for the specified gender.
     * It assumes that the CSV file contains records with columns: 
     * 0 = name, 1 = gender, 2 = count.
     * Rank 1 is the first matching record for the given gender.
     * Returns -1 if the name is not found.
     */
    private static int getRank(File file, String name, String gender) {
        int rank = 0;
        boolean found = false;
        try (Reader reader = Files.newBufferedReader(file.toPath());
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
             
            for (CSVRecord record : parser) {
                // Check only records that match the provided gender.
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
     * Opens a JFileChooser dialog to allow the user to select a CSV file.
     * Returns the selected file or null if no file is chosen.
     */
    private static File selectFile(String dialogTitle) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(dialogTitle);
        chooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        chooser.setFileFilter(filter);
        
        try {
            int returnValue = chooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile();
            }
        } catch (HeadlessException e) {
            System.err.println("File chooser is not supported in this environment.");
        }
        return null;
    }
}
