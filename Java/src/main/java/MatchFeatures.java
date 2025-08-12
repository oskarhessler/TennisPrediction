// MatchFeatures.java
public class MatchFeatures {
    public String tourney_id;
    public String tourney_name;
    public String surface;
    public Integer draw_size;
    public String tourney_level;
    public Integer tourney_date;
    public Integer match_num;
    public String winner_id;
    public Integer winner_seed;
    public String winner_entry;
    public String winner_name;
    public String winner_hand;
    public Integer winner_ht;
    public String winner_ioc;
    public Double winner_age;
    public Integer winner_rank;
    public Integer winner_rank_points;
    public String loser_id;
    public Integer loser_seed;
    public String loser_entry;
    public String loser_name;
    public String loser_hand;
    public Integer loser_ht;
    public String loser_ioc;
    public Double loser_age;
    public Integer loser_rank;
    public Integer loser_rank_points;
    public String score;
    public Integer best_of;
    public String round;
    public Integer minutes;
    public Integer w_ace;
    public Integer w_df;
    public Integer w_svpt;
    public Integer w_1stIn;
    public Integer w_1stWon;
    public Integer w_2ndWon;
    public Integer w_SvGms;
    public Integer w_bpSaved;
    public Integer w_bpFaced;
    public Integer l_ace;
    public Integer l_df;
    public Integer l_svpt;
    public Integer l_1stIn;
    public Integer l_1stWon;
    public Integer l_2ndWon;
    public Integer l_SvGms;
    public Integer l_bpSaved;
    public Integer l_bpFaced;

    public MatchFeatures() {
        // default constructor
    }

    // If you want a full constructor you can add it; not required for CSV-based population.

    // -------------- Getters (exact names your FeatureTransformer expects) --------------

    public String getSurface() { return surface; }
    public Integer getDraw_size() { return draw_size; }
    public String getTourney_level() { return tourney_level; }
    public Integer getTourney_date() { return tourney_date; }
    public Integer getMatch_num() { return match_num; }
    public Integer getBest_of() { return best_of; }
    public String getRound() { return round; }

    public String getWinner_id() { return winner_id; }
    public Integer getWinner_seed() { return winner_seed; }
    public String getWinner_entry() { return winner_entry; }
    public String getWinner_name() { return winner_name; }
    public String getWinner_hand() { return winner_hand; }
    public Integer getWinner_ht() { return winner_ht; }
    public String getWinner_ioc() { return winner_ioc; }
    public Double getWinner_age() { return winner_age; }
    public Integer getWinner_rank() { return winner_rank; }
    public Integer getWinner_rank_points() { return winner_rank_points; }

    public String getLoser_id() { return loser_id; }
    public Integer getLoser_seed() { return loser_seed; }
    public String getLoser_entry() { return loser_entry; }
    public String getLoser_name() { return loser_name; }
    public String getLoser_hand() { return loser_hand; }
    public Integer getLoser_ht() { return loser_ht; }
    public String getLoser_ioc() { return loser_ioc; }
    public Double getLoser_age() { return loser_age; }
    public Integer getLoser_rank() { return loser_rank; }
    public Integer getLoser_rank_points() { return loser_rank_points; }

    public String getScore() { return score; }
    public Integer getMinutes() { return minutes; }

    // Winner match stats getters (exact method names used in FeatureTransformer)
    public Integer getW_ace() { return w_ace; }
    public Integer getW_df() { return w_df; }
    public Integer getW_svpt() { return w_svpt; }
    public Integer getW_1stIn() { return w_1stIn; }
    public Integer getW_1stWon() { return w_1stWon; }
    public Integer getW_2ndWon() { return w_2ndWon; }
    public Integer getW_SvGms() { return w_SvGms; }
    public Integer getW_bpSaved() { return w_bpSaved; }
    public Integer getW_bpFaced() { return w_bpFaced; }

    // Loser match stats getters
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
