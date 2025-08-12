package Utils;

import Objects.ModelData;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ParsingUtils - robust CSV loader for the tennis merged.csv.
 *
 * - Attempts strict Apache Commons CSV parse (UTF-8, then ISO-8859-1).
 * - If strict parse fails, uses a tolerant logical-record parser that accumulates lines
 *   until quotes are balanced (handles embedded newlines inside quoted fields).
 */
public class ParsingUtils {

    private static final Logger LOG = Logger.getLogger(ParsingUtils.class.getName());

    /**
     * Load merged CSV into list of ModelData.
     *
     * @param csvFilePath path like "TennisPredictor3.0/Data/merged.csv"
     * @return list of ModelData
     * @throws IOException on I/O error or if parsing fails for all charsets
     */
    public static List<ModelData> loadMatches(String csvFilePath) throws IOException {
        Path path = Paths.get(csvFilePath);

        // try UTF-8 first, then ISO-8859-1
        List<ModelData> matches = tryParseWithCharset(path, StandardCharsets.UTF_8);
        if (matches == null) {
            LOG.warning("UTF-8 strict parse failed; retrying with ISO-8859-1");
            matches = tryParseWithCharset(path, Charset.forName("ISO-8859-1"));
        }

        if (matches == null) {
            throw new IOException("Failed to parse CSV with supported charsets");
        }

        return matches;
    }

    /**
     * Attempt strict parse with Apache Commons CSV; fall back to tolerant parser on failure.
     * Returns null on failure (so caller can try other charset).
     */
    private static List<ModelData> tryParseWithCharset(Path path, Charset charset) {
        List<ModelData> matches = new ArrayList<>();
        CSVFormat fmt = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreEmptyLines()
                .withTrim()
                .withIgnoreSurroundingSpaces();

        // 1) Strict parse using Commons CSV
        try (BufferedReader br = Files.newBufferedReader(path, charset);
             CSVParser parser = new CSVParser(br, fmt)) {

            for (CSVRecord record : parser) {
                java.util.function.Function<String, String> s = (name) -> get(record, name);
                ModelData md = buildModelDataFromRecord(s);
                matches.add(md);
            }
            return matches;
        } catch (Exception e) {
            LOG.log(Level.FINE, "Strict parse failed with charset " + charset + ": " + e.getMessage());
            // fall through to tolerant parser
        }

        // 2) Tolerant parser fallback
        try {
            return parseTolerantCSV(path, charset);
        } catch (Exception e) {
            LOG.log(Level.FINE, "Tolerant parse failed with charset " + charset + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Tolerant parser: read header, then accumulate physical lines until quotes balanced,
     * then split into fields and map to headers.
     */
    private static List<ModelData> parseTolerantCSV(Path path, Charset charset) throws IOException {
        List<ModelData> matches = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(path, charset)) {
            String headerLine = br.readLine();
            if (headerLine == null) return matches;

            List<String> headers = parseCSVLineList(headerLine);

            String physical;
            StringBuilder logical = new StringBuilder();
            int lineNo = 1;
            while ((physical = br.readLine()) != null) {
                lineNo++;
                if (logical.length() > 0) logical.append("\n");
                logical.append(physical);

                String candidate = logical.toString();

                // continue reading if quotes aren't balanced (handles newlines within fields)
                if (!quotesBalanced(candidate)) {
                    continue;
                }

                // We now have a logical record - split it.
                List<String> cols = parseCSVLineList(candidate);

                // pad if shorter than header
                if (cols.size() < headers.size()) {
                    for (int i = cols.size(); i < headers.size(); i++) cols.add("");
                }

                // Supplier function by column name
                java.util.function.Function<String, String> s = (name) -> {
                    for (int i = 0; i < headers.size(); i++) {
                        if (name.equals(headers.get(i))) {
                            return cols.size() > i ? (cols.get(i) == null ? "" : cols.get(i).trim()) : "";
                        }
                    }
                    return "";
                };

                // Build ModelData and add
                try {
                    ModelData md = buildModelDataFromRecord(s);
                    matches.add(md);
                } catch (Exception ex) {
                    LOG.log(Level.FINE, "Failed to build ModelData at logical line " + lineNo + ": " + ex.getMessage());
                    // skip bad row but continue (or you can throw if you prefer)
                }

                // reset accumulator
                logical.setLength(0);
            }
        }

        return matches;
    }

    /**
     * Build ModelData from a supplier function that returns String by column name.
     * Uses the same parse helpers as the rest of your pipeline.
     */
    private static ModelData buildModelDataFromRecord(java.util.function.Function<String, String> s) {
        return new ModelData(
                s.apply("tourney_id"),
                s.apply("tourney_name"),
                s.apply("surface"),
                parseIntOrDefault(s.apply("draw_size"), 0),
                s.apply("tourney_level"),
                parseIntOrDefault(s.apply("tourney_date"), 0),
                parseIntOrDefault(s.apply("match_num"), 0),

                parseIntOrDefault(s.apply("winner_id"), 0),
                emptyToNull(s.apply("winner_seed")),
                emptyToNull(s.apply("winner_entry")),
                s.apply("winner_name"),
                emptyToNull(s.apply("winner_hand")),
                parseInteger(s.apply("winner_ht")),
                emptyToNull(s.apply("winner_ioc")),
                parseDouble(s.apply("winner_age")),
                parseInteger(s.apply("winner_rank")),
                parseInteger(s.apply("winner_rank_points")),

                parseIntOrDefault(s.apply("loser_id"), 0),
                emptyToNull(s.apply("loser_seed")),
                emptyToNull(s.apply("loser_entry")),
                s.apply("loser_name"),
                emptyToNull(s.apply("loser_hand")),
                parseInteger(s.apply("loser_ht")),
                emptyToNull(s.apply("loser_ioc")),
                parseDouble(s.apply("loser_age")),
                parseInteger(s.apply("loser_rank")),
                parseInteger(s.apply("loser_rank_points")),

                emptyToNull(s.apply("score")),
                parseIntOrDefault(s.apply("best_of"), 3),
                s.apply("round"),
                parseInteger(s.apply("minutes")),

                parseInteger(s.apply("w_ace")),
                parseInteger(s.apply("w_df")),
                parseInteger(s.apply("w_svpt")),
                parseInteger(s.apply("w_1stIn")),
                parseInteger(s.apply("w_1stWon")),
                parseInteger(s.apply("w_2ndWon")),
                parseInteger(s.apply("w_SvGms")),
                parseInteger(s.apply("w_bpSaved")),
                parseInteger(s.apply("w_bpFaced")),

                parseInteger(s.apply("l_ace")),
                parseInteger(s.apply("l_df")),
                parseInteger(s.apply("l_svpt")),
                parseInteger(s.apply("l_1stIn")),
                parseInteger(s.apply("l_1stWon")),
                parseInteger(s.apply("l_2ndWon")),
                parseInteger(s.apply("l_SvGms")),
                parseInteger(s.apply("l_bpSaved")),
                parseInteger(s.apply("l_bpFaced"))
        );
    }

    /**
     * Small helpers
     */
    private static String emptyToNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Integer parseInteger(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parseIntOrDefault(String s, int def) {
        if (s == null || s.trim().isEmpty()) return def;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static Double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Double.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Safe CSVRecord getter for Commons CSV (returns empty string if missing).
     */
    private static String get(CSVRecord record, String name) {
        try {
            if (record.isMapped(name)) {
                String v = record.get(name);
                return v == null ? "" : v.trim();
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Parse a logical CSV record into a list of values (handles quotes and escaped quotes).
     */
    private static List<String> parseCSVLineList(String line) {
        List<String> result = new ArrayList<>();
        if (line == null) return result;

        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // escaped quote
                    cur.append('"');
                    i++; // skip next
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        result.add(cur.toString());

        // trim whitespace
        for (int i = 0; i < result.size(); i++) {
            result.set(i, result.get(i) == null ? "" : result.get(i).trim());
        }
        return result;
    }

    /**
     * Returns true if non-escaped double-quote count is even.
     */
    private static boolean quotesBalanced(String s) {
        if (s == null) return true;
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '"') {
                // skip escaped pair
                if (i + 1 < s.length() && s.charAt(i + 1) == '"') {
                    i++;
                    continue;
                }
                count++;
            }
        }
        return (count % 2) == 0;
    }
}
