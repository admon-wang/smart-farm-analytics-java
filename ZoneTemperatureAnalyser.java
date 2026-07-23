import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ZoneTemperatureAnalyser implements Analyser {

    @Override
    public HashMap<String, Double> process(ArrayList<SensorReading> data) {
        HashMap<String, Double> sumMap = new HashMap<>();
        HashMap<String, Integer> countMap = new HashMap<>();

        for (SensorReading sr : data) {
            // Output keys formatted: yyyy-MM-dd_ZONE
            String key = sr.getReadingDateTime().toString() + "_" + sr.getZone();
            sumMap.put(key, sumMap.getOrDefault(key, 0.0) + sr.getTemperature());
            countMap.put(key, countMap.getOrDefault(key, 0) + 1);
        }

        HashMap<String, Double> results = new HashMap<>();
        for (Map.Entry<String, Double> entry : sumMap.entrySet()) {
            String key = entry.getKey();
            double avg = entry.getValue() / countMap.get(key);
            
            // Rounded to 2 decimal places
            double roundedAvg = Math.round(avg * 100.0) / 100.0;
            results.put(key, roundedAvg);
        }

        return results;
    }
}