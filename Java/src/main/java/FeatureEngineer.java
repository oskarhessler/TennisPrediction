import java.io.*;
import java.util.*;

public class FeatureEngineer {
    private List<MatchFeatures> matches;
    private Map<String, PlayerHistory> playerHistories;

    public FeatureEngineer(String csvPath) throws IOException {
        matches = new ArrayList<>();
        playerHistories = new HashMap<>();
        loadCsv(csvPath);
    }

    public List<MatchFeatures> getMatches() { return matches; }
    public Map<String, PlayerHistory> getPlayerHistories() { return playerHistories; }

    private void loadCsv(String csvPath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String headerLine = br.readLine();
            if (headerLine == null) return;
            String[] headers = headerLine.split(",", -1);
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < headers.length; i++) idx.put(headers[i].trim(), i);

            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",", -1);
                MatchFeatures mf = buildFromCols(cols, idx);
                matches.add(mf);
                // Optionally pre-populate playerHistories? Keep empty — FeatureTransformer will build chronological history.
            }
        }
    }

    private MatchFeatures buildFromCols(String[] c, Map<String, Integer> idx) {
        MatchFeatures m = new MatchFeatures();
        m.tourney_id = get(c, idx, "tourney_id");
        m.tourney_name = get(c, idx, "tourney_name");
        m.surface = get(c, idx, "surface");
        m.draw_size = parseInteger(get(c, idx, "draw_size"));
        m.tourney_level = get(c, idx, "tourney_level");
        m.tourney_date = parseInteger(get(c, idx, "tourney_date"));
        m.match_num = parseInteger(get(c, idx, "match_num"));
        m.winner_id = get(c, idx, "winner_id");
        m.winner_seed = parseInteger(get(c, idx, "winner_seed"));
        m.winner_entry = get(c, idx, "winner_entry");
        m.winner_name = get(c, idx, "winner_name");
        m.winner_hand = get(c, idx, "winner_hand");
        m.winner_ht = parseInteger(get(c, idx, "winner_ht"));
        m.winner_ioc = get(c, idx, "winner_ioc");
        m.winner_age = parseDouble(get(c, idx, "winner_age"));
        m.winner_rank = parseInteger(get(c, idx, "winner_rank"));
        m.winner_rank_points = parseInteger(get(c, idx, "winner_rank_points"));

        m.loser_id = get(c, idx, "loser_id");
        m.loser_seed = parseInteger(get(c, idx, "loser_seed"));
        m.loser_entry = get(c, idx, "loser_entry");
        m.loser_name = get(c, idx, "loser_name");
        m.loser_hand = get(c, idx, "loser_hand");
        m.loser_ht = parseInteger(get(c, idx, "loser_ht"));
        m.loser_ioc = get(c, idx, "loser_ioc");
        m.loser_age = parseDouble(get(c, idx, "loser_age"));
        m.loser_rank = parseInteger(get(c, idx, "loser_rank"));
        m.loser_rank_points = parseInteger(get(c, idx, "loser_rank_points"));

        m.score = get(c, idx, "score");
        m.best_of = parseInteger(get(c, idx, "best_of"));
        m.round = get(c, idx, "round");
        m.minutes = parseInteger(get(c, idx, "minutes"));

        m.w_ace = parseInteger(get(c, idx, "w_ace"));
        m.w_df = parseInteger(get(c, idx, "w_df"));
        m.w_svpt = parseInteger(get(c, idx, "w_svpt"));
        m.w_1stIn = parseInteger(get(c, idx, "w_1stIn"));
        m.w_1stWon = parseInteger(get(c, idx, "w_1stWon"));
        m.w_2ndWon = parseInteger(get(c, idx, "w_2ndWon"));
        m.w_SvGms = parseInteger(get(c, idx, "w_SvGms"));
        m.w_bpSaved = parseInteger(get(c, idx, "w_bpSaved"));
        m.w_bpFaced = parseInteger(get(c, idx, "w_bpFaced"));

        m.l_ace = parseInteger(get(c, idx, "l_ace"));
        m.l_df = parseInteger(get(c, idx, "l_df"));
        m.l_svpt = parseInteger(get(c, idx, "l_svpt"));
        m.l_1stIn = parseInteger(get(c, idx, "l_1stIn"));
        m.l_1stWon = parseInteger(get(c, idx, "l_1stWon"));
        m.l_2ndWon = parseInteger(get(c, idx, "l_2ndWon"));
        m.l_SvGms = parseInteger(get(c, idx, "l_SvGms"));
        m.l_bpSaved = parseInteger(get(c, idx, "l_bpSaved"));
        m.l_bpFaced = parseInteger(get(c, idx, "l_bpFaced"));

        return m;
    }

    private String get(String[] cols, Map<String, Integer> idx, String name) {
        Integer p = idx.get(name);
        if (p == null || p < 0 || p >= cols.length) return null;
        String v = cols[p].trim();
        return v.isEmpty() ? null : v;
    }

    private Integer parseInteger(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private Double parseDouble(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return null; }
    }
}
