import java.time.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class StudentAssertionTest {

	public static void main(String[] args) {
		// Make sure assertions are enabled
		boolean enabled = false;
		assert enabled = true; // if assertions enabled, this runs and sets enabled=true
		if (!enabled) {
			throw new RuntimeException("Assertions are NOT enabled! Run with: java -ea SensorReadingAssertTest");
		}

		try {
			testSensorReading();
			testInterfaceAnalyser();
			testZoneTemperatureAnalyser();
			testFarmStat();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	

	 public static void testFarmStat() throws Exception {
	        // Ensure assertions enabled
	        boolean enabled = false;
	        assert enabled = true;
	        if (!enabled) {
	            throw new RuntimeException("Assertions are NOT enabled! Run with: java -ea FarmStatAssertTest");
	        }

	        // 1) Ensure data.csv exists in current folder
	        Path dataCsv = Paths.get("data.csv");
	        assert Files.exists(dataCsv) : "data.csv not found in current folder: " + dataCsv.toAbsolutePath();

	        // 2) Run FarmStat.load()
	        FarmStat app = new FarmStat();
	        app.load("data.csv");

	        // Valid data lines = 12 (the first 12 lines are valid readings)
	        // Invalid lines are commented + 4 invalid data lines => they should not be loaded.
	        assert app.getSize() == 12 : "Expected 12 valid readings, but got " + app.getSize();

	        // 3) Verify errors.txt content (order not important)
	        Path errorsFile = Paths.get("errors.txt");
	        assert Files.exists(errorsFile) : "errors.txt was not created in current folder.";

	        Set<String> actualErrors = linesAsTrimmedSet(errorsFile);

	        // Line numbers include comment lines because FarmStat increments line count for every line read
	        // (including lines starting with '#').
	        //
	        // Based on your new data.csv:
	        // 13: #invalid date
	        // 14: 2025-13-11,... -> invalid reading date
	        // 15: #Invalid Temp
	        // 16: 2025-03-11,... AAA -> invalid temperature (parse)
	        // 17: #Invalid Temp - out of range
	        // 18: 2025-03-11,... -31.0 -> SensorDataException
	        // 19: #Invalid Temp - out of range
	        // 20: 2025-03-11,... 61.0 -> SensorDataException
	        Set<String> expectedErrors = new HashSet<>(Arrays.asList(
	                "Line 14: Invalid Reading Date",
	                "Line 16: Invalid Temperature",
	                "Line 18: Invalid Temperature: -31.0",
	                "Line 20: Invalid Temperature: 61.0"
	        ));

	        assert actualErrors.equals(expectedErrors)
	                : "errors.txt content mismatch.\nExpected: " + expectedErrors + "\nActual:   " + actualErrors;

	        // 4) Process with ZoneTemperatureAnalyser -> results.txt
	        app.process(new ZoneTemperatureAnalyser());

	        // 5) Verify results.txt content (order not important)
	        Path resultsFile = Paths.get("results.txt");
	        assert Files.exists(resultsFile) : "results.txt was not created in current folder.";

	        Map<String, Double> actualResults = parseResultsFile(resultsFile);

	        // Expected computed averages (rounded to 2 decimals by ZoneTemperatureAnalyser)
	        Map<String, Double> expectedResults = new HashMap<>();
	        expectedResults.put("2025-03-10_ZoneA", 26.23); // (25.5+26.1+27.1)/3 = 26.2333 -> 26.23
	        expectedResults.put("2025-03-10_ZoneB", 25.97); // (25.3+25.8+26.8)/3 = 25.9666 -> 25.97
	        expectedResults.put("2025-04-10_ZoneA", 27.37); // (26.3+27.2+28.6)/3 = 27.3666 -> 27.37
	        expectedResults.put("2025-04-10_ZoneB", 23.43); // (23.8+24.4+22.1)/3 = 23.4333 -> 23.43

	        assert actualResults.size() == expectedResults.size()
	                : "Expected " + expectedResults.size() + " result entries, but got " + actualResults.size()
	                + "\nActual keys: " + actualResults.keySet();

	        // Compare each expected key/value with tolerance
	        double eps = 1e-9;
	        for (Map.Entry<String, Double> exp : expectedResults.entrySet()) {
	            String key = exp.getKey();
	            assert actualResults.containsKey(key)
	                    : "Missing key in results.txt: " + key + "\nActual keys: " + actualResults.keySet();

	            double actual = actualResults.get(key);
	            double expected = exp.getValue();

	            assert Math.abs(actual - expected) < eps
	                    : "Value mismatch for key '" + key + "'. Expected " + expected + " but got " + actual;
	        }

	        System.out.println("All FarmStat assertion tests PASSED.");
	        System.out.println("Checked files in: " + Paths.get(".").toAbsolutePath().normalize());
	    }

	    // ---------- Helpers ----------

	    /** Reads file lines into a Set of trimmed non-empty lines (order not important). */
	    private static Set<String> linesAsTrimmedSet(Path file) throws IOException {
	        List<String> lines = Files.readAllLines(file);
	        Set<String> set = new HashSet<>();
	        for (String line : lines) {
	            String t = line.trim();
	            if (!t.isEmpty()) set.add(t);
	        }
	        return set;
	    }

	    /**
	     * Parse results.txt format:
	     *   <key> : <value>
	     * Example:
	     *   2025-03-10_ZoneA : 26.23
	     *
	     * Returns map of key->double.
	     */
	    private static Map<String, Double> parseResultsFile(Path file) throws IOException {
	        List<String> lines = Files.readAllLines(file);
	        Map<String, Double> map = new HashMap<>();

	        for (String line : lines) {
	            String trimmed = line.trim();
	            if (trimmed.isEmpty()) continue;

	            String[] parts = trimmed.split("\\s*:\\s*");
	            assert parts.length == 2 : "Invalid results line format: '" + trimmed + "'";

	            String key = parts[0].trim();

	            double value;
	            try {
	                value = Double.parseDouble(parts[1].trim());
	            } catch (NumberFormatException e) {
	                throw new AssertionError("Invalid numeric value in results line: '" + trimmed + "'");
	            }

	            map.put(key, value);
	        }

	        return map;
	    }


	
	
	

	// ##########################################################################################
	private static void testZoneTemperatureAnalyser() {
		testImplementsAnalyserInZoneTemperatureAnalyser();
		testHasCorrectProcessMethodInZoneTemperatureAnalyser();
		System.out.println("All ZoneTemperatureAnalyser assertion tests PASSED.");
	}

	/** Test that ZoneTemperatureAnalyser implements the Analyser interface */
	private static void testImplementsAnalyserInZoneTemperatureAnalyser() {
		// Option 1: Check via "instanceof" (simple & clear)
		ZoneTemperatureAnalyser zta = new ZoneTemperatureAnalyser();
		assert (zta instanceof Analyser)
				: "ZoneTemperatureAnalyser should implement Analyser (instanceof check failed).";

		// Option 2: Check via reflection (robust)
		Class<?>[] interfaces = ZoneTemperatureAnalyser.class.getInterfaces();
		boolean found = false;
		for (Class<?> i : interfaces) {
			if (i.equals(Analyser.class)) {
				found = true;
				break;
			}
		}
		assert found : "ZoneTemperatureAnalyser should declare 'implements Analyser'.";
	}

	/**
	 * Test that ZoneTemperatureAnalyser has a process method with correct signature
	 */
	private static void testHasCorrectProcessMethodInZoneTemperatureAnalyser() {
		try {
			// getMethod checks PUBLIC methods (including those from interface)
			Method m = ZoneTemperatureAnalyser.class.getMethod("process", ArrayList.class);

			// Return type should be HashMap
			assert m.getReturnType().equals(HashMap.class)
					: "process must return HashMap<String, Double> (raw type check failed). Found: "
							+ m.getReturnType().getName();

			// Must be public
			assert Modifier.isPublic(m.getModifiers()) : "process method must be public.";

			// Not static
			assert !Modifier.isStatic(m.getModifiers()) : "process method must NOT be static.";

		} catch (NoSuchMethodException e) {
			assert false : "Missing method: public HashMap<String, Double> process(ArrayList<SensorReading> data)";
		}
	}

	// ##########################################################################################
	private static void testInterfaceAnalyser() {
		testIsInterface();
		testHasCorrectProcessMethodInInterfaceAnalyser();
		System.out.println("All Analyser assertion tests PASSED.");

	}

	private static void testIsInterface() {
		assert Analyser.class.isInterface() : "Analyser should be an interface, but is not.";
	}

	private static void testHasCorrectProcessMethodInInterfaceAnalyser() {
		Method[] methods = Analyser.class.getDeclaredMethods();
		boolean found = false;

		for (Method m : methods) {
			if (m.getName().equals("process")) {
				// Check parameter types
				Class<?>[] params = m.getParameterTypes();
				assert params.length == 1 : "process should take exactly 1 argument";

				assert params[0].equals(ArrayList.class) : "process should take ArrayList<SensorReading> as parameter";

				// Check return type
				assert m.getReturnType().equals(HashMap.class) : "process should return HashMap<String, Double>";

				// Check public & abstract (interface methods are implicitly public abstract)
				assert Modifier.isPublic(m.getModifiers()) : "process should be public";

				found = true;
			}
		}

		assert found
				: "Analyser interface must declare method: HashMap<String, Double> process(ArrayList<SensorReading> data)";
	}

	// ##########################################################################################
	private static void testSensorReading() {
		testValidConstructionAndGetters();
		testNullDateThrows();
		testTempBelowMinThrows();
		testTempAboveMaxThrows();
		testBoundaryTempsAllowed();
		testEqualsSameSensorIdSameDateTrue();
		testEqualsSameSensorIdDifferentDateFalse();
		testEqualsDifferentSensorIdSameDateFalse();
		testEqualsNullAndDifferentTypeFalse();
		testToStringContainsFields();

		System.out.println("All SensorReading assertion tests PASSED.");
	}

	private static void testValidConstructionAndGetters() {
		try {
			LocalDate date = LocalDate.of(2026, 1, 18);
			SensorReading sr = new SensorReading("S001", "ZoneA", date, 25.5);

			assert "S001".equals(sr.getSensorId()) : "sensorId getter failed";
			assert "ZoneA".equals(sr.getZone()) : "zone getter failed";
			assert date.equals(sr.getReadingDateTime()) : "readingDate getter failed";
			assert sr.getYear() == 2026 : "getYear failed";
			assert sr.getMonth() == 1 : "getMonth failed";
			assert Math.abs(sr.getTemperature() - 25.5) < 1e-9 : "temperature getter failed";

		} catch (SensorDataException e) {
			assert false : "Valid construction should not throw, but threw: " + e.getMessage();
		}
	}

	private static void testNullDateThrows() {
		try {
			new SensorReading("S001", "ZoneA", null, 25.0);
			assert false : "Expected SensorDataException for null date, but none was thrown";
		} catch (SensorDataException e) {
			assert e.getMessage() != null : "Exception message should not be null";
			assert e.getMessage().toLowerCase().contains("invalid reading date")
					: "Message should mention invalid reading date. Actual: " + e.getMessage();
		}
	}

	private static void testTempBelowMinThrows() {
		try {
			new SensorReading("S001", "ZoneA", LocalDate.of(2026, 1, 1), -30.01);
			assert false : "Expected SensorDataException for temperature below -30.0";
		} catch (SensorDataException e) {
			assert e.getMessage().contains("Invalid Temperature")
					: "Message should mention Invalid Temperature. Actual: " + e.getMessage();
		}
	}

	private static void testTempAboveMaxThrows() {
		try {
			new SensorReading("S001", "ZoneA", LocalDate.of(2026, 1, 1), 60.01);
			assert false : "Expected SensorDataException for temperature above 60.0";
		} catch (SensorDataException e) {
			assert e.getMessage().contains("Invalid Temperature")
					: "Message should mention Invalid Temperature. Actual: " + e.getMessage();
		}
	}

	private static void testBoundaryTempsAllowed() {
		try {
			SensorReading low = new SensorReading("S001", "ZoneA", LocalDate.of(2026, 1, 1), -30.0);
			SensorReading high = new SensorReading("S002", "ZoneB", LocalDate.of(2026, 1, 1), 60.0);

			assert Math.abs(low.getTemperature() - (-30.0)) < 1e-9 : "Boundary -30.0 should be allowed";
			assert Math.abs(high.getTemperature() - 60.0) < 1e-9 : "Boundary 60.0 should be allowed";
		} catch (SensorDataException e) {
			assert false : "Boundary temperatures should not throw: " + e.getMessage();
		}
	}

	private static void testEqualsSameSensorIdSameDateTrue() {
		try {
			LocalDate date = LocalDate.of(2026, 1, 18);
			SensorReading a = new SensorReading("S001", "ZoneA", date, 20.0);
			SensorReading b = new SensorReading("S001", "ZoneB", date, 30.0); // zone/temp differs

			assert a.equals(b) : "equals should be true when sensorId and date match";
			assert b.equals(a) : "equals should be symmetric";
		} catch (SensorDataException e) {
			assert false : "Should not throw for valid readings: " + e.getMessage();
		}
	}

	private static void testEqualsSameSensorIdDifferentDateFalse() {
		try {
			SensorReading a = new SensorReading("S001", "ZoneA", LocalDate.of(2026, 1, 18), 20.0);
			SensorReading b = new SensorReading("S001", "ZoneA", LocalDate.of(2026, 1, 19), 20.0);

			assert !a.equals(b) : "equals should be false when dates differ";
		} catch (SensorDataException e) {
			assert false : "Should not throw for valid readings: " + e.getMessage();
		}
	}

	private static void testEqualsDifferentSensorIdSameDateFalse() {
		try {
			LocalDate date = LocalDate.of(2026, 1, 18);
			SensorReading a = new SensorReading("S001", "ZoneA", date, 20.0);
			SensorReading b = new SensorReading("S002", "ZoneA", date, 20.0);

			assert !a.equals(b) : "equals should be false when sensorIds differ";
		} catch (SensorDataException e) {
			assert false : "Should not throw for valid readings: " + e.getMessage();
		}
	}

	private static void testEqualsNullAndDifferentTypeFalse() {
		try {
			SensorReading sr = new SensorReading("S001", "ZoneA", LocalDate.of(2026, 1, 18), 20.0);

			assert !sr.equals(null) : "equals should be false for null";
			assert !sr.equals("not a SensorReading") : "equals should be false for different type";
		} catch (SensorDataException e) {
			assert false : "Should not throw for valid readings: " + e.getMessage();
		}
	}

	private static void testToStringContainsFields() {
		try {
			SensorReading sr = new SensorReading("S001", "ZoneA", LocalDate.of(2026, 1, 18), 25.5);
			String s = sr.toString();

			assert s.contains("S001") : "toString should contain sensorId";
			assert s.contains("ZoneA") : "toString should contain zone";
			assert s.contains("2026-01-18") : "toString should contain date";
			assert s.contains("25.5") : "toString should contain temperature";
		} catch (SensorDataException e) {
			assert false : "Should not throw for valid readings: " + e.getMessage();
		}
	}
}