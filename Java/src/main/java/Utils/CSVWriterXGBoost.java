package Utils;

import Objects.MatchFeatures;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Writes a list of MatchFeatures to a CSV file for XGBoost training.
 */
public class CSVWriterXGBoost {

    /**
     * Write features to CSV at specified path.
     * Uses MatchFeatures.csvHeader() and MatchFeatures.toCSVRecord().
     *
     * @param matches    list of MatchFeatures
     * @param outputPath path to write CSV (ex: "TennisPredictor3.0/Data/train_data.csv")
     * @throws IOException if write fails
     */
    public static void writeFeaturesToCSV(List<MatchFeatures> matches, String outputPath) throws IOException {
        if (matches == null) throw new IllegalArgumentException("matches must not be null");

        Path out = Paths.get(outputPath);
        // create parent dirs if missing
        if (out.getParent() != null && Files.notExists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }

        // use DEFAULT with header and record printing via CSVPrinter
        CSVFormat fmt = CSVFormat.DEFAULT
                .withRecordSeparator(System.lineSeparator());

        try (BufferedWriter bw = Files.newBufferedWriter(out);
             CSVPrinter printer = new CSVPrinter(bw, fmt)) {

            // header
            String[] header = MatchFeatures.csvHeader();
            printer.printRecord((Object[]) header);

            // rows
            for (MatchFeatures mf : matches) {
                List<Object> row = mf.toCSVRecord();
                printer.printRecord(row);
            }

            printer.flush();
            System.out.println("CSV successfully written to: " + out.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing CSV to " + out.toAbsolutePath() + ": " + e.getMessage());
            throw e;
        }
    }
}
