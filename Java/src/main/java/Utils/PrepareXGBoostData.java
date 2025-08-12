package Utils;

import Model.FeatureEngineer;
import Objects.MatchFeatures;
import Objects.ModelData;

import java.io.IOException;
import java.util.List;

public class PrepareXGBoostData {
    public static void main(String[] args) {
        String inputCSV = "TennisPredictor3.0/Data/merged.csv";
        String outputCSV = "TennisPredictor3.0/Data/train_data.csv";

        try {
            List<ModelData> raw = ParsingUtils.loadMatches(inputCSV);
            List<MatchFeatures> features = FeatureEngineer.generateFeatures(raw);
            CSVWriterXGBoost.writeFeaturesToCSV(features, outputCSV);
            System.out.println("Generated XGBoost CSV at " + outputCSV);
        } catch (IOException e) {
            System.err.println("Error building feature CSV: " + e.getMessage());
        }
    }
}
