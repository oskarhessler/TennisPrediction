package Model;

import Objects.*;
import  Utils.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * FeatureEngineer - computes rolling averages, H2H and surface stats and produces MatchFeatures.
 *
 * Usage:
 *  - instantiate with a list of ModelData (chronologically ordered)
 *  - call buildAll to get List<MatchFeatures>
 *
 * Notes:
 *  - Assumes input ModelData list is sorted by date ascending (old -> new).
 *  - Rolling window size can be changed by ROLLING_WINDOW.
 */
public class FeatureEngineer {

    private static final Logger LOG = Logger.getLogger(FeatureEngineer.class.getName());

    private final List<ModelData> allMatches; // raw matches in chronological order
    private final int ROLLING_WINDOW = 5; // last N matches

    // per-player history (ordered list of ModelData indices or references)
    private final Map<String, LinkedList<ModelData>> historyByPlayer = new HashMap<>();

    // per-player per-surface stats (wins / total) maintained incrementally
    private final Map<String, Map<String, int[]>> surfaceCountsByPlayer = new HashMap<>();

    // h2h counters: map[playerA][playerB] = winsPlayerAAgainstB
    private final Map<String, Map<String, Integer>> h2hWins = new HashMap<>();

    public FeatureEngineer(List<ModelData> matchesChronological) {
        this.allMatches = matchesChronological;
    }

    /**
     * Build features for every match in allMatches.
     * Returns list of MatchFeatures in the same order.
     */
    public List<MatchFeatures> buildAll() {
        List<MatchFeatures> result = new ArrayList<>();

        for (ModelData m : allMatches) {
            // Determine names - use winner_name/loser_name and map to player1/player2 consistently.
            String winnerName = m.getWinner_name();
            String loserName = m.getLoser_name();

            // Keep CSV ordering mapping: player1 = winner_name, player2 = loser_name
            String player1 = winnerName;
            String player2 = loserName;
            String surface = m.getSurface();

            // Get current ranks / elos (if null, set default)
            double player1Rank = (m.getWinner_rank() != null) ? m.getWinner_rank() : 9999;
            double player2Rank = (m.getLoser_rank() != null) ? m.getLoser_rank() : 9999;
            double player1Elo = player1Rank > 0 && player1Rank < 9999 ? 2000.0 - player1Rank : 1500.0; // lightweight proxy if no Elo
            double player2Elo = player2Rank > 0 && player2Rank < 9999 ? 2000.0 - player2Rank : 1500.0;

            // Calculate H2H counts & rates BEFORE current match (important: use historyByPlayer)
            int h2hP1Wins = getH2hWins(player1, player2);
            int h2hP2Wins = getH2hWins(player2, player1);
            double h2hRateP1 = computeRate(h2hP1Wins, h2hP1Wins + h2hP2Wins);
            double h2hRateP2 = computeRate(h2hP2Wins, h2hP1Wins + h2hP2Wins);

            // Form stats: last N matches for both players
            int[] formP1 = getRecentWinCounts(player1, ROLLING_WINDOW);
            int[] formP2 = getRecentWinCounts(player2, ROLLING_WINDOW);
            int formWinsP1 = formP1[0], formTotalP1 = formP1[1];
            int formWinsP2 = formP2[0], formTotalP2 = formP2[1];
            double formRateP1 = computeRate(formWinsP1, formTotalP1);
            double formRateP2 = computeRate(formWinsP2, formTotalP2);

            // Rolling averages from last N (win pct, aces avg, df avg, bp conversion)
            double winPctP1 = getRollingWinPct(player1, ROLLING_WINDOW);
            double winPctP2 = getRollingWinPct(player2, ROLLING_WINDOW);

            double aceAvgP1 = getRollingDoubleStatAvg(player1, "aces", ROLLING_WINDOW);
            double aceAvgP2 = getRollingDoubleStatAvg(player2, "aces", ROLLING_WINDOW);

            double dfAvgP1 = getRollingDoubleStatAvg(player1, "double_faults", ROLLING_WINDOW);
            double dfAvgP2 = getRollingDoubleStatAvg(player2, "double_faults", ROLLING_WINDOW);

            double bpConvP1 = getRollingBPConversion(player1, ROLLING_WINDOW);
            double bpConvP2 = getRollingBPConversion(player2, ROLLING_WINDOW);

            // Surface-specific performance (win% and ace rate)
            double surfaceWinPctP1 = getSurfaceWinPct(player1, surface);
            double surfaceWinPctP2 = getSurfaceWinPct(player2, surface);

            double surfaceAceRateP1 = getSurfaceAvgDoubleStat(player1, surface, "aces");
            double surfaceAceRateP2 = getSurfaceAvgDoubleStat(player2, surface, "aces");

            // Odds (if you have odds in ModelData, else 0)
            double oddsP1 = 0.0;
            double oddsP2 = 0.0;

            // Build MatchFeatures object.
            MatchFeatures mf = new MatchFeatures(
                    player1, player2, surface, m.getRound(),
                    player1Rank, player2Rank, player1Elo, player2Elo,
                    h2hP1Wins, h2hP2Wins, h2hRateP1, h2hRateP2,
                    formWinsP1, Math.max(0, formTotalP1 - formWinsP1), formRateP1,
                    formWinsP2, Math.max(0, formTotalP2 - formWinsP2), formRateP2,
                    oddsP1, oddsP2,
                    winPctP1, aceAvgP1, dfAvgP1, bpConvP1,
                    winPctP2, aceAvgP2, dfAvgP2, bpConvP2,
                    h2hRateP1, h2hRateP2,
                    surfaceWinPctP1, surfaceWinPctP2,
                    surfaceAceRateP1, surfaceAceRateP2,
                    1 // label: since player1 is winner_name we set 1
            );

            result.add(mf);

            // AFTER creating features for this match, update history maps with current match
            pushToHistory(m);
            updateH2HAfterMatch(m);
            updateSurfaceAfterMatch(m);
        }

        return result;
    }

    // -------------------------
    // History builders / helpers
    // -------------------------
    private void pushToHistory(ModelData m) {
        // add to winner's and loser's history list (we store ModelData references)
        String winnerName = m.getWinner_name();
        String loserName = m.getLoser_name();

        historyByPlayer.computeIfAbsent(winnerName, k -> new LinkedList<>()).add(m);
        historyByPlayer.computeIfAbsent(loserName, k -> new LinkedList<>()).add(m);

        // Cap lists to reasonable size (e.g., 200) to avoid memory blow-up
        trimHistory(winnerName, 200);
        trimHistory(loserName, 200);
    }

    private void trimHistory(String player, int cap) {
        LinkedList<ModelData> list = historyByPlayer.get(player);
        if (list == null) return;
        while (list.size() > cap) list.removeFirst();
    }

    private void updateH2HAfterMatch(ModelData m) {
        String w = m.getWinner_name();
        String l = m.getLoser_name();
        // increment w vs l
        h2hWins.computeIfAbsent(w, k -> new HashMap<>());
        Map<String, Integer> winsMap = h2hWins.get(w);
        winsMap.put(l, winsMap.getOrDefault(l, 0) + 1);
    }

    private void updateSurfaceAfterMatch(ModelData m) {
        String w = m.getWinner_name();
        String l = m.getLoser_name();
        String surface = m.getSurface();

        surfaceCountsByPlayer.computeIfAbsent(w, k -> new HashMap<>());
        surfaceCountsByPlayer.computeIfAbsent(l, k -> new HashMap<>());

        Map<String, int[]> wMap = surfaceCountsByPlayer.get(w);
        Map<String, int[]> lMap = surfaceCountsByPlayer.get(l);

        int[] wCounts = wMap.getOrDefault(surface, new int[]{0,0}); // [wins, total]
        wCounts[0] = wCounts[0] + 1; // winner won on this surface
        wCounts[1] = wCounts[1] + 1;
        wMap.put(surface, wCounts);

        int[] lCounts = lMap.getOrDefault(surface, new int[]{0,0});
        lCounts[1] = lCounts[1] + 1; // loser played on this surface but lost
        lMap.put(surface, lCounts);
    }

    // -------------------------
    // Feature computation helpers
    // -------------------------
    private int getH2hWins(String player, String opponent) {
        if (!h2hWins.containsKey(player)) return 0;
        return h2hWins.get(player).getOrDefault(opponent, 0);
    }

    private double getH2hWinRate(String player, String opponent) {
        int a = getH2hWins(player, opponent);
        int b = getH2hWins(opponent, player);
        return computeRate(a, a + b);
    }

    private int[] getRecentWinCounts(String player, int n) {
        LinkedList<ModelData> hist = historyByPlayer.getOrDefault(player, new LinkedList<>());
        int wins = 0;
        int total = 0;
        Iterator<ModelData> it = hist.descendingIterator(); // newest first
        while (it.hasNext() && total < n) {
            ModelData m = it.next();
            // identify if player was winner in that match
            if (player.equals(m.getWinner_name())) wins++;
            total++;
        }
        return new int[]{wins, total};
    }

    private double getRollingWinPct(String player, int n) {
        int[] counts = getRecentWinCounts(player, n);
        return computeRate(counts[0], counts[1]);
    }

    // For generic double stat (aces, double_faults) stored in ModelData fields,
    // we read from winner/loser columns based on player's role in that match
    private double getRollingDoubleStatAvg(String player, String field, int window) {
        LinkedList<ModelData> hist = historyByPlayer.getOrDefault(player, new LinkedList<>());
        int taken = 0;
        double sum = 0.0;
        Iterator<ModelData> it = hist.descendingIterator();
        while (it.hasNext() && taken < window) {
            ModelData m = it.next();
            Integer val = null;
            // if player was winner
            if (player.equals(m.getWinner_name())) {
                switch (field) {
                    case "aces": val = m.getW_ace(); break;
                    case "double_faults": val = m.getW_df(); break;
                    default: val = 0; break;
                }
            } else if (player.equals(m.getLoser_name())) {
                switch (field) {
                    case "aces": val = m.getL_ace(); break;
                    case "double_faults": val = m.getL_df(); break;
                    default: val = 0; break;
                }
            }
            if (val != null) {
                sum += val;
                taken++;
            }
        }
        return taken > 0 ? sum / taken : 0.0;
    }

    private double getRollingBPConversion(String player, int window) {
        LinkedList<ModelData> hist = historyByPlayer.getOrDefault(player, new LinkedList<>());
        int taken = 0;
        double sum = 0.0;
        Iterator<ModelData> it = hist.descendingIterator();
        while (it.hasNext() && taken < window) {
            ModelData m = it.next();
            Double conv = null;
            if (player.equals(m.getWinner_name())) {
                Integer saved = m.getW_bpSaved();
                Integer faced = m.getW_bpFaced();
                conv = (faced != null && faced > 0) ? (double) saved / faced : 0.0;
            } else if (player.equals(m.getLoser_name())) {
                Integer saved = m.getL_bpSaved();
                Integer faced = m.getL_bpFaced();
                conv = (faced != null && faced > 0) ? (double) saved / faced : 0.0;
            }
            if (conv != null) {
                sum += conv;
                taken++;
            }
        }
        return taken > 0 ? sum / taken : 0.0;
    }

    private double getSurfaceWinPct(String player, String surface) {
        Map<String, int[]> map = surfaceCountsByPlayer.getOrDefault(player, Collections.emptyMap());
        int[] counts = map.getOrDefault(surface, new int[]{0,0}); // [wins, total]
        return computeRate(counts[0], counts[1]);
    }

    private double getSurfaceAvgDoubleStat(String player, String surface, String field) {
        // average of stat only on matches played on given surface
        LinkedList<ModelData> hist = historyByPlayer.getOrDefault(player, new LinkedList<>());
        int taken = 0;
        double sum = 0.0;
        Iterator<ModelData> it = hist.descendingIterator();
        while (it.hasNext()) {
            ModelData m = it.next();
            if (!surface.equalsIgnoreCase(m.getSurface())) continue;
            Integer val = null;
            if (player.equals(m.getWinner_name())) {
                if ("aces".equals(field)) val = m.getW_ace();
                else if ("double_faults".equals(field)) val = m.getW_df();
            } else if (player.equals(m.getLoser_name())) {
                if ("aces".equals(field)) val = m.getL_ace();
                else if ("double_faults".equals(field)) val = m.getL_df();
            }
            if (val != null) {
                sum += val;
                taken++;
            }
        }
        return taken > 0 ? sum / taken : 0.0;
    }

    private double computeRate(int wins, int total) {
        return total > 0 ? (double) wins / total : 0.0;
    }

    // -------------------------
    // CSV loading / compatibility helpers (Apache Commons CSV)
    // -------------------------

    /**
     * Reads the merged CSV into a list of ModelData rows using Apache Commons CSV.
     * Expects header line to contain exact column names used below.
     * Tries UTF-8 first, falls back to ISO-8859-1 if there's an IO/decoding problem.
     */
    public static List<ModelData> loadMatches(String csvFilePath) throws IOException {
        return ParsingUtils.loadMatches(csvFilePath);
    }

    public static List<MatchFeatures> generateFeatures(List<ModelData> rawMatches) {
        if (rawMatches == null || rawMatches.isEmpty()) return Collections.emptyList();

        // Defensive copy + sort by tourney_date ascending (YYYYMMDD int)
        List<ModelData> sorted = new ArrayList<>(rawMatches);
        sorted.sort(Comparator.comparingInt(ModelData::getTourney_date));

        FeatureEngineer fe = new FeatureEngineer(sorted);
        return fe.buildAll();
    }

}
