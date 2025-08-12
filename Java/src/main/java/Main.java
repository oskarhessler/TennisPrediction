import ml.dmlc.xgboost4j.java.DMatrix;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String csv = "Data/2024.csv"; // your file
        FeatureEngineer fe = new FeatureEngineer(csv);

        // Optionally preload player histories (empty here)
        Map<String, PlayerHistory> initialHist = fe.getPlayerHistories();

        // Build transformer; symmetricAugmentation=true will produce two rows per match (balanced labels)
        FeatureTransformer ft = new FeatureTransformer(initialHist, true);

        DMatrix dmat = ft.transformToDMatrix(fe.getMatches());

        MatchFeatures first = fe.getMatches().get(0);
        List<Float> fv = ft.debugFeaturesForMatch(first, true); // winner first
        List<String> names = ft.getFeatureNames();
        for (int j=0; j<fv.size(); j++) {
            System.out.println(j + ": " + names.get(j) + " = " + fv.get(j));
        }

        System.out.println("DMatrix created: rows=" + dmat.rowNum());
        float[] labels = dmat.getLabel();
        System.out.println("First labels sample: " + Arrays.toString(Arrays.copyOf(labels, Math.min(20, labels.length))));
        // Optionally display feature names:
        System.out.println("Feature count: " + ft.getFeatureNames().size());
        System.out.println("First features names: " + ft.getFeatureNames());
    }
}

