import ml.dmlc.xgboost4j.java.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XGBoostTrainPredict {

    public static void main(String[] args) throws XGBoostError, IOException {
        // Assume you have a DMatrix 'trainMat' already created and labeled
        DMatrix trainMat = getYourPreparedDMatrix();

        // Set training parameters
        HashMap<String, Object> params = new HashMap<>();
        params.put("eta", 0.1);
        params.put("max_depth", 6);
        params.put("objective", "binary:logistic");  // Binary classification (win/loss)
        params.put("eval_metric", "logloss");

        // Number of boosting rounds
        int rounds = 10;

        // Train the model
        Booster booster = XGBoost.train(trainMat, params, rounds, new HashMap<>(), null, null);

        // Prepare test data matrix (replace with your own test DMatrix)
        DMatrix testMat = getYourTestDMatrix();

        // Make predictions on test data
        float[][] predictions = booster.predict(testMat);

        // Output predictions
        for (int i = 0; i < predictions.length; i++) {
            System.out.printf("Match %d win probability: %.4f\n", i + 1, predictions[i][0]);
        }
    }

    // Placeholder for how you get your prepared training DMatrix
    private static DMatrix getYourPreparedDMatrix() throws XGBoostError, IOException {
        // Your code to create or load DMatrix here

        return getYourTestDMatrix();
    }

    // Placeholder for your test data DMatrix
    private static DMatrix getYourTestDMatrix() throws IOException, XGBoostError {
        // Your code to create or load test DMatrix here
        String csv = "Data/2024.csv";
        FeatureEngineer fe = new FeatureEngineer(csv);

        // Optionally preload player histories (empty here)
        Map<String, PlayerHistory> initialHist = fe.getPlayerHistories();

        // Build transformer; symmetricAugmentation=true will produce two rows per match (balanced labels)
        FeatureTransformer ft = new FeatureTransformer(initialHist, true);

        DMatrix dmat = ft.transformToDMatrix(fe.getMatches());
        return dmat;
    }
}
