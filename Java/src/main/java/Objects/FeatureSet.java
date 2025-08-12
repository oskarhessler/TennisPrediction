package Objects;

/**
 * Container for engineered match features.
 */
public class FeatureSet {
    // Winner serve stats
    public double winnerFirstServePct;
    public double winnerFirstServeWinPct;
    public double winnerSecondServeWinPct;
    public double winnerAcePct;
    public double winnerDoubleFaultPct;

    // Loser serve stats
    public double loserFirstServePct;
    public double loserFirstServeWinPct;
    public double loserSecondServeWinPct;
    public double loserAcePct;
    public double loserDoubleFaultPct;

    // Break points
    public double winnerBreakPointsSavedPct;
    public double loserBreakPointsSavedPct;

    // Return points
    public double winnerReturnPointsWonPct;
    public double loserReturnPointsWonPct;

    // Total points
    public int winnerTotalPointsWon;
    public int loserTotalPointsWon;
    public double winnerTotalPointsWonPct;
    public double loserTotalPointsWonPct;

    // Physical differences
    public double heightDifference;
    public double ageDifference;

    // Ranking differences
    public int rankDifference;
    public int rankPointsDifference;

    // Experience
    public double winnerExperienceFactor;
    public double loserExperienceFactor;

    // Tournament metadata
    public boolean isGrandSlam;
    public boolean isBestOfFive;
}
