// FeatureTransformer.java
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoostError;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;

/**
 * FeatureTransformer builds numeric feature rows from MatchFeatures,
 * does date conversion, imputes NaNs with column means, and returns a DMatrix.
 */
public class FeatureTransformer {
    private final Map<String, Integer> surfaceMap = Map.of("Hard",0,"Clay",1,"Grass",2,"Carpet",3);
    private final Map<String, Integer> handMap = Map.of("R",0,"L",1,"U",2);
    private final Map<String, Integer> roundMap = Map.of("R128",0,"R64",1,"R32",2,"R16",3,"QF",4,"SF",5,"F",6);
    private final Map<String, Integer> tourneyLevelMap = Map.of("G",0,"M",1,"A",2,"B",3,"F",4,"D",5);
    private final Map<String, Integer> entryMap = Map.of("Q",0,"WC",1,"LL",2,"SE",3,"",4,"D",5);

    private final Map<String, Integer> iocMap = new HashMap<>();
    private final Map<String, PlayerHistory> playerHistories;
    private final Map<String, Map<String, Integer>> h2hWins = new HashMap<>();
    private final boolean symmetricAugmentation;

    public FeatureTransformer(Map<String, PlayerHistory> playerHistories) {
        this(playerHistories, true);
    }

    public FeatureTransformer(Map<String, PlayerHistory> playerHistories, boolean symmetricAugmentation) {
        this.playerHistories = new HashMap<>(playerHistories);
        this.symmetricAugmentation = symmetricAugmentation;
    }

    /**
     * Main entry: transform matches -> DMatrix (with labels).
     * Steps:
     *  - build IOC map
     *  - iterate matches chronologically, compute feature rows (winner-first and optionally swapped)
     *  - update histories/h2h after each match (no leakage)
     *  - impute column means for NaNs
     *  - return DMatrix with labels
     */
    public DMatrix transformToDMatrix(List<MatchFeatures> matches) throws IOException, XGBoostError {
        if (matches == null || matches.isEmpty()) throw new IllegalArgumentException("No matches provided.");
        buildIocMap(matches);

        List<float[]> rows = new ArrayList<>();
        List<Float> labels = new ArrayList<>();

        for (MatchFeatures m : matches) {
            // winner-first row (label = 1)
            List<Float> fvWin = buildFeaturesForPair(m, true);
            rows.add(listToFloatArray(fvWin));
            labels.add(1.0f);

            if (symmetricAugmentation) {
                List<Float> fvLose = buildFeaturesForPair(m, false);
                rows.add(listToFloatArray(fvLose));
                labels.add(0.0f);
            }

            // update histories after computing features (prevent leakage)
            updateHistoriesWithMatch(m);
        }

        if (rows.isEmpty()) throw new IllegalStateException("No feature rows produced.");

        // Impute NaNs by column means
        int nCols = rows.get(0).length;
        int nRows = rows.size();
        double[] colSums = new double[nCols];
        int[] colCounts = new int[nCols];

        // accumulate sums (skip NaNs)
        for (float[] r : rows) {
            for (int c = 0; c < nCols; c++) {
                float v = r[c];
                if (!Float.isNaN(v)) {
                    colSums[c] += v;
                    colCounts[c]++;
                }
            }
        }
        float[] colMeans = new float[nCols];
        for (int c = 0; c < nCols; c++) {
            colMeans[c] = (colCounts[c] > 0) ? (float)(colSums[c] / colCounts[c]) : 0f; // fallback 0
        }

        // fill NaNs
        for (float[] r : rows) {
            for (int c = 0; c < nCols; c++) {
                if (Float.isNaN(r[c])) r[c] = colMeans[c];
            }
        }

        // Flatten and create DMatrix + set labels
        float[] flat = new float[nRows * nCols];
        for (int i = 0; i < nRows; i++) {
            System.arraycopy(rows.get(i), 0, flat, i * nCols, nCols);
        }
        float[] labelArr = new float[labels.size()];
        for (int i = 0; i < labels.size(); i++) labelArr[i] = labels.get(i);

        DMatrix dmat = new DMatrix(flat, nRows, nCols, Float.NaN);
        dmat.setLabel(labelArr);

        System.out.println("Built DMatrix rows=" + nRows + " cols=" + nCols + " (symmetricAug=" + symmetricAugmentation + ")");
        return dmat;
    }

    // Build features for a pair (player1 = winner if winnerIsPlayer1==true)
    private List<Float> buildFeaturesForPair(MatchFeatures m, boolean winnerIsPlayer1) {
        List<Float> f = new ArrayList<>();

        String p1Id = winnerIsPlayer1 ? m.getWinner_id() : m.getLoser_id();
        String p2Id = winnerIsPlayer1 ? m.getLoser_id() : m.getWinner_id();

        // --- Tournament metadata (we convert date into days-since-epoch + year + month)
        add(f, encode(surfaceMap, m.getSurface()));
        add(f, safeFloat(m.getDraw_size()));
        add(f, encode(tourneyLevelMap, m.getTourney_level()));

        // convert tourney_date int YYYYMMDD to epochDays/year/month
        int ymd = m.getTourney_date() == null ? -1 : m.getTourney_date();
        long daysSinceEpoch = convertYyyymmddToEpochDays(ymd);
        int year = extractYear(ymd);
        int month = extractMonth(ymd);
        add(f, (float) daysSinceEpoch); // days since epoch (large int fits in float for modern years)
        add(f, (float) year);
        add(f, (float) month);

        add(f, safeFloat(m.getMatch_num()));
        add(f, safeFloat(m.getBest_of()));
        add(f, encode(roundMap, m.getRound()));

        // Player1 basic
        add(f, safeFloat(winnerIsPlayer1 ? m.getWinner_seed() : m.getLoser_seed()));
        add(f, encode(entryMap, winnerIsPlayer1 ? m.getWinner_entry() : m.getLoser_entry()));
        add(f, encode(handMap, winnerIsPlayer1 ? m.getWinner_hand() : m.getLoser_hand()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getWinner_ht() : m.getLoser_ht()));
        add(f, safeFloat(encodeIoc(winnerIsPlayer1 ? m.getWinner_ioc() : m.getLoser_ioc())));
        add(f, safeFloat(winnerIsPlayer1 ? m.getWinner_age() : m.getLoser_age()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getWinner_rank() : m.getLoser_rank()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getWinner_rank_points() : m.getLoser_rank_points()));

        // Player2 basic
        add(f, safeFloat(winnerIsPlayer1 ? m.getLoser_seed() : m.getWinner_seed()));
        add(f, encode(entryMap, winnerIsPlayer1 ? m.getLoser_entry() : m.getWinner_entry()));
        add(f, encode(handMap, winnerIsPlayer1 ? m.getLoser_hand() : m.getWinner_hand()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getLoser_ht() : m.getWinner_ht()));
        add(f, safeFloat(encodeIoc(winnerIsPlayer1 ? m.getLoser_ioc() : m.getWinner_ioc())));
        add(f, safeFloat(winnerIsPlayer1 ? m.getLoser_age() : m.getWinner_age()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getLoser_rank() : m.getWinner_rank()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getLoser_rank_points() : m.getWinner_rank_points()));

        // Match stats - common field minutes
        add(f, safeFloat(m.getMinutes()));

        // Player1 match stats
        add(f, safeFloat(winnerIsPlayer1 ? m.getW_ace() : m.getL_ace()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getW_df() : m.getL_df()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getW_svpt() : m.getL_svpt()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getW_1stIn() : m.getL_1stIn()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getW_1stWon() : m.getL_1stWon()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getW_2ndWon() : m.getL_2ndWon()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getW_SvGms() : m.getL_SvGms()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getW_bpSaved() : m.getL_bpSaved()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getW_bpFaced() : m.getL_bpFaced()));

        // Player2 match stats (opposite)
        add(f, safeFloat(winnerIsPlayer1 ? m.getL_ace() : m.getW_ace()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getL_df() : m.getW_df()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getL_svpt() : m.getW_svpt()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getL_1stIn() : m.getW_1stIn()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getL_1stWon() : m.getW_1stWon()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getL_2ndWon() : m.getW_2ndWon()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getL_SvGms() : m.getW_SvGms()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getL_bpSaved() : m.getW_bpSaved()));
        add(f, safeFloat(winnerIsPlayer1 ? m.getL_bpFaced() : m.getW_bpFaced()));

        // H2H and form (use current history state)
        add(f, getH2hWinRate(p1Id, p2Id));
        add(f, getPlayerForm(p1Id, 10));
        add(f, getPlayerForm(p2Id, 10));

        // Score features
        float[] sf = extractScoreFeatures(m.getScore());
        for (float v : sf) add(f, v);

        // p1 surface elo (current)
        PlayerHistory ph = playerHistories.get(p1Id);
        double se = ph == null ? 1500.0 : ph.surfaceElo.getOrDefault(m.getSurface(), 1500.0);
        add(f, (float) se);

        return f;
    }

    // --- helpers and utilities ---

    private void updateHistoriesWithMatch(MatchFeatures m) {
        playerHistories.putIfAbsent(m.getWinner_id(), new PlayerHistory());
        playerHistories.putIfAbsent(m.getLoser_id(), new PlayerHistory());
        playerHistories.get(m.getWinner_id()).addSurfaceMatch(m.getSurface(), true);
        playerHistories.get(m.getLoser_id()).addSurfaceMatch(m.getSurface(), false);

        h2hWins.putIfAbsent(m.getWinner_id(), new HashMap<>());
        h2hWins.get(m.getWinner_id()).merge(m.getLoser_id(), 1, Integer::sum);
    }

    private float getH2hWinRate(String p1, String p2) {
        if (p1 == null || p2 == null) return 0.5f;
        int p1wins = h2hWins.getOrDefault(p1, Collections.emptyMap()).getOrDefault(p2, 0);
        int p2wins = h2hWins.getOrDefault(p2, Collections.emptyMap()).getOrDefault(p1, 0);
        int t = p1wins + p2wins;
        return t == 0 ? 0.5f : (float) p1wins / t;
    }

    private float getPlayerForm(String playerId, int lastN) {
        PlayerHistory ph = playerHistories.get(playerId);
        return ph == null ? 0.5f : (float) ph.recentWinRate(lastN);
    }

    private float[] listToFloatArray(List<Float> list) {
        float[] a = new float[list.size()];
        for (int i = 0; i < list.size(); i++) a[i] = list.get(i);
        return a;
    }

    private void buildIocMap(List<MatchFeatures> matches) {
        int counter = iocMap.size();
        for (MatchFeatures m : matches) {
            if (m.getWinner_ioc() != null && !iocMap.containsKey(m.getWinner_ioc()))
                iocMap.put(m.getWinner_ioc(), counter++);
            if (m.getLoser_ioc() != null && !iocMap.containsKey(m.getLoser_ioc()))
                iocMap.put(m.getLoser_ioc(), counter++);
        }
    }

    private void add(List<Float> dest, Number v) {
        dest.add(v == null ? Float.NaN : v.floatValue());
    }

    private float safeFloat(Number n) {
        return n == null ? Float.NaN : n.floatValue();
    }

    private int encode(Map<String,Integer> map, String value) {
        if (value == null) return -1;
        return map.getOrDefault(value, -1);
    }

    private int encodeIoc(String ioc) {
        if (ioc == null) return -1;
        return iocMap.getOrDefault(ioc, -1);
    }

    // Date conversion helpers
    private long convertYyyymmddToEpochDays(int yyyymmdd) {
        if (yyyymmdd <= 0) return 0L;
        int y = extractYear(yyyymmdd);
        int m = extractMonth(yyyymmdd);
        int d = extractDay(yyyymmdd);
        try {
            LocalDate dt = LocalDate.of(y, m, d);
            return dt.toEpochDay();
        } catch (DateTimeException e) {
            return 0L;
        }
    }
    private int extractYear(int yyyymmdd) {
        if (yyyymmdd <= 0) return 0;
        return yyyymmdd / 10000;
    }
    private int extractMonth(int yyyymmdd) {
        if (yyyymmdd <= 0) return 0;
        return (yyyymmdd / 100) % 100;
    }
    private int extractDay(int yyyymmdd) {
        if (yyyymmdd <= 0) return 0;
        return yyyymmdd % 100;
    }

    // Score parser - returns 6 features (same as before)
    private float[] extractScoreFeatures(String score) {
        if (score == null || score.isEmpty()) {
            return new float[]{Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, 0f};
        }
        int setsW = 0, setsL = 0;
        int gamesW = 0, gamesL = 0;
        int tiebreaks = 0, ret = 0;
        String[] parts = score.split(" ");
        for (String set : parts) {
            if (set.equalsIgnoreCase("RET") || set.equalsIgnoreCase("W/O")) { ret = 1; continue; }
            String[] g = set.split("-");
            if (g.length >= 2) {
                try {
                    int a = Integer.parseInt(g[0].replaceAll("\\D",""));
                    int b = Integer.parseInt(g[1].replaceAll("\\D",""));
                    gamesW += a; gamesL += b;
                    if (a > b) setsW++; else setsL++;
                    if (set.contains("(")) tiebreaks++;
                } catch (NumberFormatException ignored) {}
            }
        }
        float totalSets = setsW + setsL;
        float totalGames = gamesW + gamesL;
        return new float[]{
                totalSets > 0 ? (float)setsW/totalSets : Float.NaN,
                totalSets > 0 ? (float)setsL/totalSets : Float.NaN,
                totalGames > 0 ? (float)gamesW/totalGames : Float.NaN,
                totalGames > 0 ? (float)gamesL/totalGames : Float.NaN,
                totalSets > 0 ? (float)tiebreaks/totalSets : 0f,
                (float)ret
        };
    }

    // Feature names (reflects added date components)
    public List<String> getFeatureNames() {
        List<String> names = new ArrayList<>();
        names.add("surface_enc");
        names.add("draw_size");
        names.add("tourney_level_enc");
        names.add("tourney_date_days"); // converted
        names.add("tourney_date_year");
        names.add("tourney_date_month");
        names.add("match_num");
        names.add("best_of");
        names.add("round_enc");
        names.addAll(Arrays.asList("p1_seed","p1_entry_enc","p1_hand_enc","p1_ht","p1_ioc_enc","p1_age","p1_rank","p1_rank_points"));
        names.addAll(Arrays.asList("p2_seed","p2_entry_enc","p2_hand_enc","p2_ht","p2_ioc_enc","p2_age","p2_rank","p2_rank_points"));
        names.add("minutes");
        names.addAll(Arrays.asList("p1_ace","p1_df","p1_svpt","p1_1stIn","p1_1stWon","p1_2ndWon","p1_SvGms","p1_bpSaved","p1_bpFaced"));
        names.addAll(Arrays.asList("p2_ace","p2_df","p2_svpt","p2_1stIn","p2_1stWon","p2_2ndWon","p2_SvGms","p2_bpSaved","p2_bpFaced"));
        names.addAll(Arrays.asList("h2h_p1_vs_p2","p1_form","p2_form"));
        names.addAll(Arrays.asList("setsW_pct","setsL_pct","gamesW_pct","gamesL_pct","tiebreak_ratio","retired_flag"));
        names.add("p1_surface_elo");
        return names;
    }

    public Map<String,Integer> getIocMap() { return Collections.unmodifiableMap(iocMap); }

    // Persist encoders and feature names to disk (needs Gson on classpath)
    public void saveEncoders(Path outDir) throws IOException {
        if (!Files.exists(outDir)) Files.createDirectories(outDir);
        Gson g = new Gson();
        Files.writeString(outDir.resolve("ioc_map.json"), g.toJson(iocMap));
        Files.writeString(outDir.resolve("feature_names.json"), g.toJson(getFeatureNames()));
    }
    public List<Float> debugFeaturesForMatch(MatchFeatures m, boolean winnerIsPlayer1) {
        return buildFeaturesForPair(m, winnerIsPlayer1); // buildFeaturesForPair is already implemented
    }
}
