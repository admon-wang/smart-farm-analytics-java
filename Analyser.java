import java.util.ArrayList;
import java.util.HashMap;

public interface Analyser {
    HashMap<String, Double> process(ArrayList<SensorReading> data);
}