// Import necessary Duke and Apache Commons CSV libraries
import edu.duke.*;
import org.apache.commons.csv.*;
import java.io.File; // Needed for iterating through selected files

public class PrintSelectedFiles {

    /**
     * Reads and prints the contents of CSV files selected by the user.
     * Assumes the CSV files do not have a header row.
     */
    public void printFiles() {
        // 1. Create a DirectoryResource object. This will open a file selection dialog.
        System.setProperty("user.dir", "C:\\Users\\inouy\\Downloads\\us_babynames_small\\testing"); // Example path structure

        DirectoryResource dr = new DirectoryResource();

        // 2. Iterate through the files the user selects in the dialog.
        //    Navigate to "C:\Users\inouy\Download\Us_babynames_small\testing"
        //    and select the yobXXXXshort.csv files.
        for (File f : dr.selectedFiles()) {
            System.out.println("---- Reading file: " + f.getName() + " ----");

            // 3. Create a FileResource for the current selected file.
            FileResource fr = new FileResource(f);

            // 4. Get a CSVParser for the file.
            //    The 'false' argument tells the parser that the file DOES NOT have a header row.
            CSVParser parser = fr.getCSVParser(false);

            // 5. Iterate through each record (line) in the CSV file.
            for (CSVRecord record : parser) {
                // 6. Access data by numerical index (0, 1, 2) because there's no header.
                String name = record.get(0);
                String gender = record.get(1);
                String numBorn = record.get(2);

                // 7. Print the data from the current record, separated by tabs for readability.
                System.out.println(name + "\t" + gender + "\t" + numBorn);
            }
            System.out.println("---- Finished file: " + f.getName() + " ----\n");
        }
    }

    /**
     * Main method to run the printFiles method.
     */
    public static void main(String[] args) {
        PrintSelectedFiles psf = new PrintSelectedFiles();
        psf.printFiles();
    }
}