import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.HeadlessException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CountGenderNames {

    public static void main(String[] args) {
        // Ask the user to select a CSV file.
        File selectedFile = selectFile("Select a CSV file with baby names");
        if (selectedFile == null) {
            System.out.println("No file selected. Exiting.");
            return;
        }
        
        int totalMaleNames = 0;
        int totalFemaleNames = 0;
        
        // Process the file: assuming CSV structure: 0=name, 1=gender, 2=count.
        try (Reader reader = Files.newBufferedReader(selectedFile.toPath());
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
             
            for (CSVRecord record : parser) {
                String gender = record.get(1);
                if (gender.equalsIgnoreCase("M")) {
                    totalMaleNames++;
                } else if (gender.equalsIgnoreCase("F")) {
                    totalFemaleNames++;
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + selectedFile.getName() + " - " + e.getMessage());
            return;
        }
        
        // Print the results.
        System.out.println("Total Male Names: " + totalMaleNames);
        System.out.println("Total Female Names: " + totalFemaleNames);
    }
    
    /**
     * Opens a file chooser dialog to select a CSV file.
     * Returns the selected file or null if none is selected.
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
