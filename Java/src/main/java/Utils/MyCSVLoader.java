package Utils;

import Objects.MatchFeatures;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class MyCSVLoader {

    public static List<MatchFeatures> loadMatches(String filePath) {
        List<MatchFeatures> matches = new ArrayList<>();

        try (BufferedReader br = tryOpenWithFallback(filePath)) {
            String header = br.readLine(); // skip header
            if (header == null) return matches;

            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",", -1); // keep empty strings
                Map<String, String> row = parseRow(header, cols);

                // TODO: integrate FeatureEngineer call here
                // Example:
                // MatchFeatures mf = featureEngineer.engineerFeatures(row, rollingStats, h2hStats, surfaceStats);
                // matches.add(mf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matches;
    }

    private static BufferedReader tryOpenWithFallback(String filePath) throws IOException {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), Charset.defaultCharset()));
        } catch (Exception e) {
            return new BufferedReader(new InputStreamReader(new FileInputStream(filePath), Charset.forName("UTF-8")));
        }
    }

    private static Map<String, String> parseRow(String header, String[] cols) {
        Map<String, String> row = new HashMap<>();
        String[] headers = header.split(",", -1);
        for (int i = 0; i < headers.length && i < cols.length; i++) {
            row.put(headers[i], cols[i]);
        }
        return row;
    }
}
