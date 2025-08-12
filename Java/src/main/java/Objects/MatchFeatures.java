package Objects;

import java.util.Arrays;
import java.util.List;

/**
 * Represents all engineered match features for XGBoost preparation.
 */
public class MatchFeatures {

    // Basic match info
    private String player1;      // Name of player 1
    private String player2;      // Name of player 2
    private String surface;      // Court surface (e.g. Hard, Clay, Grass)
    private String round;        // Tournament round (e.g. QF, SF, F)

    // Player stats
    private double player1Rank;       // Current ATP rank of player 1
    private double player2Rank;       // Current ATP rank of player 2
    private double player1Elo;        // Current Elo rating of player 1
    private double player2Elo;        // Current Elo rating of player 2

    // Head-to-head stats
    private int h2hWinsP1;            // H2H wins for player 1 vs player 2
    private int h2hWinsP2;            // H2H wins for player 2 vs player 1
    private double h2hWinRateP1;      // Win rate for player 1 in H2H
    private double h2hWinRateP2;      // Win rate for player 2 in H2H

    // Form stats
    private int formWinsP1;           // Wins in last N matches for player 1
    private int formLossesP1;         // Losses in last N matches for player 1
    private double formWinRateP1;     // Win rate in last N matches for player 1

    private int formWinsP2;           // Wins in last N matches for player 2
    private int formLossesP2;         // Losses in last N matches for player 2
    private double formWinRateP2;     // Win rate in last N matches for player 2

    // Odds
    private double oddsP1;            // Betting odds for player 1
    private double oddsP2;            // Betting odds for player 2

    // Rolling averages (last 5 matches)
    private double winnerRollingWinPctLast5;
    private double winnerRollingAceAvgLast5;
    private double winnerRollingDFAvgLast5;
    private double winnerRollingBPConversionLast5;

    private double loserRollingWinPctLast5;
    private double loserRollingAceAvgLast5;
    private double loserRollingDFAvgLast5;
    private double loserRollingBPConversionLast5;

    // Head-to-head win rates
    private double winnerH2HWinPct;
    private double loserH2HWinPct;

    // Surface-specific performance
    private double winnerSurfaceWinPct;
    private double loserSurfaceWinPct;
    private double winnerSurfaceAceRate;
    private double loserSurfaceAceRate;

    // Target label
    private int winner;               // 1 if player1 wins, 0 otherwise

    /**
     * Constructor for MatchFeatures.
     */
    public MatchFeatures(String player1, String player2, String surface, String round,
                         double player1Rank, double player2Rank,
                         double player1Elo, double player2Elo,
                         int h2hWinsP1, int h2hWinsP2, double h2hWinRateP1, double h2hWinRateP2,
                         int formWinsP1, int formLossesP1, double formWinRateP1,
                         int formWinsP2, int formLossesP2, double formWinRateP2,
                         double oddsP1, double oddsP2, double winnerRollingWinPctLast5,
                         double winnerRollingAceAvgLast5,
                         double winnerRollingDFAvgLast5,
                         double winnerRollingBPConversionLast5,
                         double loserRollingWinPctLast5,
                         double loserRollingAceAvgLast5,
                         double loserRollingDFAvgLast5,
                         double loserRollingBPConversionLast5,
                         double winnerH2HWinPct,
                         double loserH2HWinPct,
                         double winnerSurfaceWinPct,
                         double loserSurfaceWinPct,
                         double winnerSurfaceAceRate,
                         double loserSurfaceAceRate, int winner) {
        this.player1 = player1;
        this.player2 = player2;
        this.surface = surface;
        this.round = round;
        this.player1Rank = player1Rank;
        this.player2Rank = player2Rank;
        this.player1Elo = player1Elo;
        this.player2Elo = player2Elo;
        this.h2hWinsP1 = h2hWinsP1;
        this.h2hWinsP2 = h2hWinsP2;
        this.h2hWinRateP1 = h2hWinRateP1;
        this.h2hWinRateP2 = h2hWinRateP2;
        this.formWinsP1 = formWinsP1;
        this.formLossesP1 = formLossesP1;
        this.formWinRateP1 = formWinRateP1;
        this.formWinsP2 = formWinsP2;
        this.formLossesP2 = formLossesP2;
        this.formWinRateP2 = formWinRateP2;
        this.oddsP1 = oddsP1;
        this.oddsP2 = oddsP2;
        this.winnerRollingWinPctLast5 = winnerRollingWinPctLast5;
        this.winnerRollingAceAvgLast5 = winnerRollingAceAvgLast5;
        this.winnerRollingDFAvgLast5 = winnerRollingDFAvgLast5;
        this.winnerRollingBPConversionLast5 = winnerRollingBPConversionLast5;

        this.loserRollingWinPctLast5 = loserRollingWinPctLast5;
        this.loserRollingAceAvgLast5 = loserRollingAceAvgLast5;
        this.loserRollingDFAvgLast5 = loserRollingDFAvgLast5;
        this.loserRollingBPConversionLast5 = loserRollingBPConversionLast5;

        this.winnerH2HWinPct = winnerH2HWinPct;
        this.loserH2HWinPct = loserH2HWinPct;

        this.winnerSurfaceWinPct = winnerSurfaceWinPct;
        this.loserSurfaceWinPct = loserSurfaceWinPct;
        this.winnerSurfaceAceRate = winnerSurfaceAceRate;
        this.loserSurfaceAceRate = loserSurfaceAceRate;
        this.winner = winner;
    }

    // Getters
    public String getPlayer1() { return player1; }
    public String getPlayer2() { return player2; }
    public String getSurface() { return surface; }
    public String getRound() { return round; }
    public double getPlayer1Rank() { return player1Rank; }
    public double getPlayer2Rank() { return player2Rank; }
    public double getPlayer1Elo() { return player1Elo; }
    public double getPlayer2Elo() { return player2Elo; }
    public int getH2hWinsP1() { return h2hWinsP1; }
    public int getH2hWinsP2() { return h2hWinsP2; }
    public double getH2hWinRateP1() { return h2hWinRateP1; }
    public double getH2hWinRateP2() { return h2hWinRateP2; }
    public int getFormWinsP1() { return formWinsP1; }
    public int getFormLossesP1() { return formLossesP1; }
    public double getFormWinRateP1() { return formWinRateP1; }
    public int getFormWinsP2() { return formWinsP2; }
    public int getFormLossesP2() { return formLossesP2; }
    public double getFormWinRateP2() { return formWinRateP2; }
    public double getOddsP1() { return oddsP1; }
    public double getOddsP2() { return oddsP2; }
    public double getWinnerRollingWinPctLast5() { return winnerRollingWinPctLast5; }
    public double getWinnerRollingAceAvgLast5() { return winnerRollingAceAvgLast5; }
    public double getWinnerRollingDFAvgLast5() { return winnerRollingDFAvgLast5; }
    public double getWinnerRollingBPConversionLast5() { return winnerRollingBPConversionLast5; }

    public double getLoserRollingWinPctLast5() { return loserRollingWinPctLast5; }
    public double getLoserRollingAceAvgLast5() { return loserRollingAceAvgLast5; }
    public double getLoserRollingDFAvgLast5() { return loserRollingDFAvgLast5; }
    public double getLoserRollingBPConversionLast5() { return loserRollingBPConversionLast5; }

    public double getWinnerH2HWinPct() { return winnerH2HWinPct; }
    public double getLoserH2HWinPct() { return loserH2HWinPct; }

    public double getWinnerSurfaceWinPct() { return winnerSurfaceWinPct; }
    public double getLoserSurfaceWinPct() { return loserSurfaceWinPct; }
    public double getWinnerSurfaceAceRate() { return winnerSurfaceAceRate; }
    public double getLoserSurfaceAceRate() { return loserSurfaceAceRate; }
    public int getWinner() { return winner; }

    public static String[] csvHeader() {
        return new String[]{
                "winner_name", "loser_name",
                "winner_elo", "loser_elo",
                "winner_rank", "loser_rank",
                "winner_surface_winrate", "loser_surface_winrate",
                "winner_form_last5", "loser_form_last5",
                "h2h_winrate_winner", "h2h_winrate_loser",
                "label"
        };
    }

    public List<Object> toCSVRecord() {
        return Arrays.asList(
                getPlayer1(), getPlayer2(),                              // winner_name, loser_name
                getPlayer1Elo(), getPlayer2Elo(),                        // winner_elo, loser_elo
                getPlayer1Rank(), getPlayer2Rank(),                      // winner_rank, loser_rank
                getWinnerSurfaceWinPct(), getLoserSurfaceWinPct(),       // winner_surface_winrate, loser_surface_winrate
                getFormWinRateP1(), getFormWinRateP2(),                   // winner_form_last5, loser_form_last5
                getWinnerH2HWinPct(), getLoserH2HWinPct(),               // h2h_winrate_winner, h2h_winrate_loser
                getWinner()                                              // label
        );
    }


}
