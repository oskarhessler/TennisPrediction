import java.util.*;
import com.google.gson.Gson;

public class PlayerHistory {
    public String playerId;
    public double elo = 1500.0;
    public Map<String, Double> surfaceElo = new HashMap<>(); // surface-specific Elo
    private Deque<Integer> lastResults = new ArrayDeque<>();
    private int maxHistory = 50;

    public int matchesTotal = 0;
    public int winsTotal = 0;

    public PlayerHistory() {
        surfaceElo.put("Hard", 1500.0);
        surfaceElo.put("Clay", 1500.0);
        surfaceElo.put("Grass", 1500.0);
        surfaceElo.put("Carpet", 1500.0);
    }

    public void addMatch(boolean win) {
        matchesTotal++;
        if (win) winsTotal++;
        lastResults.addFirst(win ? 1 : 0);
        if (lastResults.size() > maxHistory) lastResults.removeLast();
    }

    public void addSurfaceMatch(String surface, boolean win) {
        addMatch(win);
        double curr = surfaceElo.getOrDefault(surface, 1500.0);
        double change = win ? 10.0 : -10.0;
        surfaceElo.put(surface, curr + change);
    }

    public double recentWinRate(int lastN) {
        if (lastResults.isEmpty()) return 0.5;
        int count = 0, total = 0;
        for (int v : lastResults) {
            if (total >= lastN) break;
            count += v;
            total++;
        }
        return total == 0 ? 0.5 : ((double) count) / total;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
