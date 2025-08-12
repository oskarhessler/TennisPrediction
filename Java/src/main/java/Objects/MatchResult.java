package Objects;

import java.time.LocalDate;

public class MatchResult {
    private String player1;
    private String player2;
    private String surface;
    private String round;
    private int winner; // 1 if player1 wins, 2 if player2 wins
    private LocalDate date;
    private int player1Rank;
    private int player2Rank;
    private double player1Elo;
    private double player2Elo;

    public MatchResult(String player1, String player2, String surface, String round,
                       int winner, LocalDate date, int player1Rank, int player2Rank,
                       double player1Elo, double player2Elo) {
        this.player1 = player1;
        this.player2 = player2;
        this.surface = surface;
        this.round = round;
        this.winner = winner;
        this.date = date;
        this.player1Rank = player1Rank;
        this.player2Rank = player2Rank;
        this.player1Elo = player1Elo;
        this.player2Elo = player2Elo;
    }

    public String getPlayer1() { return player1; }
    public String getPlayer2() { return player2; }
    public String getSurface() { return surface; }
    public String getRound() { return round; }
    public int getWinner() { return winner; }
    public LocalDate getDate() { return date; }
    public int getPlayer1Rank() { return player1Rank; }
    public int getPlayer2Rank() { return player2Rank; }
    public double getPlayer1Elo() { return player1Elo; }
    public double getPlayer2Elo() { return player2Elo; }
}
