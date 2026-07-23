/*
 * Module code: CSIT213
 * Assignment name: Assignment 3
 * Student Number: 9069884
 * Full Name: Admon Wang Jianxin
 * Tutorial Group: T03F
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class FarmStat {
    private ArrayList<SensorReading> readingData;
    private ArrayList<String> errors;

    public FarmStat() {
        this.readingData = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public void load(String fileName) {
        File file = new File(fileName);
        try (Scanner scanner = new Scanner(file)) {
            int lineNum = 0;
            while (scanner.hasNextLine()) {
                lineNum++;
                String line = scanner.nextLine();
                String trimmed = line.trim();

                // Ignore empty lines and comment lines starting with #
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                String dateStr = parts.length > 0 ? parts[0].trim() : "";
                String idStr = parts.length > 1 ? parts[1].trim() : "";
                String zoneStr = parts.length > 2 ? parts[2].trim() : "";
                String tempStr = parts.length > 3 ? parts[3].trim() : "";

                // Validate Date
                LocalDate date = null;
                try {
                    if (dateStr.isEmpty()) throw new DateTimeParseException("Empty Date", dateStr, 0);
                    date = LocalDate.parse(dateStr);
                } catch (DateTimeParseException e) {
                    errors.add("Line " + lineNum + ": Invalid Reading Date");
                    continue; // Skip the rest of validation for this line
                }

                // Validate Sensor ID
                if (idStr.isEmpty()) {
                    errors.add("Line " + lineNum + ": Invalid Sensor ID");
                    continue;
                }

                // Validate Zone
                if (zoneStr.isEmpty()) {
                    errors.add("Line " + lineNum + ": Invalid Zone");
                    continue;
                }

                // Validate Temperature is numeric
                double temp = 0.0;
                try {
                    temp = Double.parseDouble(tempStr);
                } catch (NumberFormatException e) {
                    errors.add("Line " + lineNum + ": Invalid Temperature");
                    continue;
                }

                // Attempt to create the object (which validates temp bounds)
                try {
                    SensorReading sr = new SensorReading(idStr, zoneStr, date, temp);
                    readingData.add(sr);
                } catch (SensorDataException e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Input file not found: " + fileName);
        }

        // Write errors.txt
        try (PrintWriter writer = new PrintWriter("errors.txt")) {
            for (String error : errors) {
                writer.println(error);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could not create errors.txt");
        }
    }

    public int getSize() {
        return readingData.size();
    }

    public void process(Analyser analyser) {
        HashMap<String, Double> results = analyser.process(readingData);

        // Write results.txt
        try (PrintWriter writer = new PrintWriter("results.txt")) {
            for (Map.Entry<String, Double> entry : results.entrySet()) {
                // Ensure the exact formatting: <date>_<zone>: <average Temperature>
                writer.println(entry.getKey() + ": " + String.format(Locale.US, "%.2f", entry.getValue()));
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could not create results.txt");
        }
    }

    @Override
    public String toString() {
        return "FarmStat[loaded=" + readingData.size() + " records, errors=" + errors.size() + "]";
    }
}