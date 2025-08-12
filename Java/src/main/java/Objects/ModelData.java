package Objects;

/**
 * Represents a single tennis match record from the dataset.
 * Stores only raw data as read from the CSV file.
 */
public class ModelData {

    // --- Tournament info ---
    /** Unique ID for the tournament */
    private final String tourney_id;
    /** Name of the tournament */
    private final String tourney_name;
    /** Court surface (Hard, Clay, Grass, Carpet) */
    private final String surface;
    /** Number of players in the draw */
    private final int draw_size;
    /** Tournament level (G=Grand Slam, M=Masters 1000, A=ATP 500, B=ATP 250) */
    private final String tourney_level;
    /** Tournament start date (YYYYMMDD) */
    private final int tourney_date;

    // --- Match info ---
    /** Match number within the tournament */
    private final int match_num;

    // --- Winner info ---
    private final int winner_id;           // Player ID of winner
    private final String winner_seed;      // Seed number (may be null/empty)
    private final String winner_entry;     // Special entry code (WC=Wild Card, Q=Qualifier, etc.)
    private final String winner_name;      // Full name of winner
    private final String winner_hand;      // Playing hand (R=Right, L=Left)
    private final Integer winner_ht;       // Height in cm (nullable)
    private final String winner_ioc;       // 3-letter country code
    private final Double winner_age;       // Age in years
    private final Integer winner_rank;     // ATP rank at match time
    private final Integer winner_rank_points; // Ranking points at match time

    // --- Loser info ---
    private final int loser_id;
    private final String loser_seed;
    private final String loser_entry;
    private final String loser_name;
    private final String loser_hand;
    private final Integer loser_ht;
    private final String loser_ioc;
    private final Double loser_age;
    private final Integer loser_rank;
    private final Integer loser_rank_points;

    // --- Match stats ---
    private final String score;            // Match score (e.g., 6-4 3-6 7-6)
    private final int best_of;              // Best of 3 or 5 sets
    private final String round;            // Tournament round (e.g., R32, QF, SF, F)
    private final Integer minutes;         // Duration in minutes (nullable)

    // Winner stats
    private final Integer w_ace;
    private final Integer w_df;
    private final Integer w_svpt;
    private final Integer w_1stIn;
    private final Integer w_1stWon;
    private final Integer w_2ndWon;
    private final Integer w_SvGms;
    private final Integer w_bpSaved;
    private final Integer w_bpFaced;

    // Loser stats
    private final Integer l_ace;
    private final Integer l_df;
    private final Integer l_svpt;
    private final Integer l_1stIn;
    private final Integer l_1stWon;
    private final Integer l_2ndWon;
    private final Integer l_SvGms;
    private final Integer l_bpSaved;
    private final Integer l_bpFaced;

    // --- Constructor ---
    public ModelData(String tourney_id, String tourney_name, String surface, int draw_size,
                     String tourney_level, int tourney_date, int match_num,
                     int winner_id, String winner_seed, String winner_entry, String winner_name, String winner_hand,
                     Integer winner_ht, String winner_ioc, Double winner_age, Integer winner_rank, Integer winner_rank_points,
                     int loser_id, String loser_seed, String loser_entry, String loser_name, String loser_hand,
                     Integer loser_ht, String loser_ioc, Double loser_age, Integer loser_rank, Integer loser_rank_points,
                     String score, int best_of, String round, Integer minutes,
                     Integer w_ace, Integer w_df, Integer w_svpt, Integer w_1stIn, Integer w_1stWon,
                     Integer w_2ndWon, Integer w_SvGms, Integer w_bpSaved, Integer w_bpFaced,
                     Integer l_ace, Integer l_df, Integer l_svpt, Integer l_1stIn, Integer l_1stWon,
                     Integer l_2ndWon, Integer l_SvGms, Integer l_bpSaved, Integer l_bpFaced) {

        this.tourney_id = tourney_id;
        this.tourney_name = tourney_name;
        this.surface = surface;
        this.draw_size = draw_size;
        this.tourney_level = tourney_level;
        this.tourney_date = tourney_date;
        this.match_num = match_num;
        this.winner_id = winner_id;
        this.winner_seed = winner_seed;
        this.winner_entry = winner_entry;
        this.winner_name = winner_name;
        this.winner_hand = winner_hand;
        this.winner_ht = winner_ht;
        this.winner_ioc = winner_ioc;
        this.winner_age = winner_age;
        this.winner_rank = winner_rank;
        this.winner_rank_points = winner_rank_points;
        this.loser_id = loser_id;
        this.loser_seed = loser_seed;
        this.loser_entry = loser_entry;
        this.loser_name = loser_name;
        this.loser_hand = loser_hand;
        this.loser_ht = loser_ht;
        this.loser_ioc = loser_ioc;
        this.loser_age = loser_age;
        this.loser_rank = loser_rank;
        this.loser_rank_points = loser_rank_points;
        this.score = score;
        this.best_of = best_of;
        this.round = round;
        this.minutes = minutes;
        this.w_ace = w_ace;
        this.w_df = w_df;
        this.w_svpt = w_svpt;
        this.w_1stIn = w_1stIn;
        this.w_1stWon = w_1stWon;
        this.w_2ndWon = w_2ndWon;
        this.w_SvGms = w_SvGms;
        this.w_bpSaved = w_bpSaved;
        this.w_bpFaced = w_bpFaced;
        this.l_ace = l_ace;
        this.l_df = l_df;
        this.l_svpt = l_svpt;
        this.l_1stIn = l_1stIn;
        this.l_1stWon = l_1stWon;
        this.l_2ndWon = l_2ndWon;
        this.l_SvGms = l_SvGms;
        this.l_bpSaved = l_bpSaved;
        this.l_bpFaced = l_bpFaced;
    }

    // --- Getters ---
    public String getTourney_id() { return tourney_id; }
    public String getTourney_name() { return tourney_name; }
    public String getSurface() { return surface; }
    public int getDraw_size() { return draw_size; }
    public String getTourney_level() { return tourney_level; }
    public int getTourney_date() { return tourney_date; }
    public int getMatch_num() { return match_num; }
    public int getWinner_id() { return winner_id; }
    public String getWinner_seed() { return winner_seed; }
    public String getWinner_entry() { return winner_entry; }
    public String getWinner_name() { return winner_name; }
    public String getWinner_hand() { return winner_hand; }
    public Integer getWinner_ht() { return winner_ht; }
    public String getWinner_ioc() { return winner_ioc; }
    public Double getWinner_age() { return winner_age; }
    public Integer getWinner_rank() { return winner_rank; }
    public Integer getWinner_rank_points() { return winner_rank_points; }
    public int getLoser_id() { return loser_id; }
    public String getLoser_seed() { return loser_seed; }
    public String getLoser_entry() { return loser_entry; }
    public String getLoser_name() { return loser_name; }
    public String getLoser_hand() { return loser_hand; }
    public Integer getLoser_ht() { return loser_ht; }
    public String getLoser_ioc() { return loser_ioc; }
    public Double getLoser_age() { return loser_age; }
    public Integer getLoser_rank() { return loser_rank; }
    public Integer getLoser_rank_points() { return loser_rank_points; }
    public String getScore() { return score; }
    public int getBest_of() { return best_of; }
    public String getRound() { return round; }
    public Integer getMinutes() { return minutes; }
    public Integer getW_ace() { return w_ace; }
    public Integer getW_df() { return w_df; }
    public Integer getW_svpt() { return w_svpt; }
    public Integer getW_1stIn() { return w_1stIn; }
    public Integer getW_1stWon() { return w_1stWon; }
    public Integer getW_2ndWon() { return w_2ndWon; }
    public Integer getW_SvGms() { return w_SvGms; }
    public Integer getW_bpSaved() { return w_bpSaved; }
    public Integer getW_bpFaced() { return w_bpFaced; }
    public Integer getL_ace() { return l_ace; }
    public Integer getL_df() { return l_df; }
    public Integer getL_svpt() { return l_svpt; }
    public Integer getL_1stIn() { return l_1stIn; }
    public Integer getL_1stWon() { return l_1stWon; }
    public Integer getL_2ndWon() { return l_2ndWon; }
    public Integer getL_SvGms() { return l_SvGms; }
    public Integer getL_bpSaved() { return l_bpSaved; }
    public Integer getL_bpFaced() { return l_bpFaced; }
}
