# FarmStat: Analyzing Sensor Data for Optimal Farm Management

## 1. 📌 Project Title & Short Summary

**FarmStat** is a Java application designed to process and analyze sensor data from a farm. It reads temperature readings from a CSV file, identifies and logs any data inconsistencies, calculates average temperatures per zone and day, and outputs these statistics. I built this project to gain practical experience in file I/O, data validation, object-oriented design, and implementing custom exception handling in Java.

## 2. 🎯 The Problem & Goal

Modern agriculture increasingly relies on data to optimize operations. This project addresses the need to efficiently process raw sensor data, which can often be messy and contain errors. The primary goal was to develop a robust system that can:

*   Read and parse sensor readings from a CSV file.
*   Validate data for accuracy and completeness, logging any issues.
*   Calculate meaningful statistics (average temperature per zone per day).
*   Present the processed data in a clean, usable format.

This project simulates a real-world scenario where accurate data processing is crucial for making informed decisions in farm management.

## 3. 🛠️ How It Works (Step-by-Step Architecture)

FarmStat is structured to handle data processing in a clear, modular way:

*   **`FarmStat.java`**: This is the main orchestrator.
    *   It manages a collection of `SensorReading` objects.
    *   The `load(String fileName)` method reads data from a specified CSV file. It iterates through each line, splitting it into components (date, ID, zone, temperature).
    *   **Input Validation**: Crucially, it validates each field:
        *   **Date**: Checks for empty or incorrectly formatted dates using `LocalDate.parse`.
        *   **Sensor ID & Zone**: Ensures these are not empty.
        *   **Temperature**: Verifies it's a valid number and within the acceptable range of -30.0 to 60.0 degrees Celsius (handled by `SensorReading`).
    *   Any validation errors are collected in an `errors` list and written to `errors.txt`.
    *   Valid readings are stored in the `readingData` `ArrayList`.
    *   The `process(Analyser analyser)` method takes an `Analyser` object and uses it to compute statistics from the loaded data. The results are then written to `results.txt`.
    *   **Design Decision**: Using a `HashMap` for `errors` and `results` allows for efficient lookups and aggregation of data. The `load` method's early `continue` statements on validation failure prevent incomplete data from being processed, ensuring data integrity.

*   **`SensorReading.java`**: Represents a single data point.
    *   It encapsulates the `sensorId`, `zone`, `readingDate`, and `temperature`.
    *   The constructor enforces data integrity by throwing a `SensorDataException` if the date is null or the temperature is outside the valid range (-30°C to 60°C). This is an example of **defensive programming**, ensuring that only valid `SensorReading` objects can be created.

*   **`SensorDataException.java`**: A custom exception class.
    *   Extends `Exception` to provide specific error handling for invalid sensor data, making error messages more informative.

*   **`Analyser.java`**: An interface defining the contract for data analysis.
    *   This promotes **modularity and extensibility**. Different analysis strategies can be implemented by classes that implement this interface.

*   **`ZoneTemperatureAnalyser.java`**: Implements the `Analyser` interface.
    *   This class calculates the average temperature for each unique combination of date and zone.
    *   It iterates through the `SensorReading` data, summing temperatures and counting readings for each `(date, zone)` key.
    *   **Key Design Decision**: It uses `HashMap.getOrDefault` to simplify the logic for accumulating sums and counts, avoiding null checks and making the code cleaner. The results are formatted to two decimal places.

## 4. 🚀 Getting Started & Execution

1.  **Compile the Java code:**
    ```bash
    javac *.java
    ```

2.  **Run the `FarmStat` application:**
    ```bash
    java FarmStat
    ```
    *(Note: The `FarmStat` class expects the input data file to be named `data.csv` in the same directory. If your input file has a different name, you would need to modify the `main` method or how `FarmStat` is invoked to specify the filename.)*

**Example:**

Assuming you have a `data.csv` file with the following content:

```csv
2023-01-15,S001,ZoneA,25.5
2023-01-15,S002,ZoneB,26.0
2023-01-16,S001,ZoneA,24.8
2023-01-16,S003,ZoneA,25.0
2023-01-16,S004,ZoneC,22.1
invalid-date,S005,ZoneD,20.0
2023-01-17,S006,ZoneB,
2023-01-17,S007,ZoneB,27.5
2023-01-17,S008,ZoneB,28.0
2023-01-18,S009,ZoneA,70.0 
```

**Running the application would produce:**

*   **`errors.txt`**: Containing logs of data inconsistencies.
    ```
    Line 6: Invalid Reading Date
    Line 7: Invalid Temperature
    Line 9: Invalid Temperature: 70.0
    ```
*   **`results.txt`**: Containing the calculated average temperatures.
    ```
    2023-01-15_ZoneA: 25.50
    2023-01-15_ZoneB: 26.00
    2023-01-16_ZoneA: 24.90
    2023-01-16_ZoneC: 22.10
    2023-01-17_ZoneB: 27.75
    ```

## 5. 💡 Key Takeaways & What I Learned

*   **Robust File I/O and Parsing**: Gained hands-on experience reading from CSV files, handling potential `FileNotFoundException`, and parsing string data into appropriate types.
*   **Defensive Programming & Exception Handling**: Learned to anticipate and handle potential errors gracefully using custom exceptions (`SensorDataException`) and standard Java exceptions, ensuring the program remains stable even with malformed input.
*   **Object-Oriented Design**: Practiced designing classes (`FarmStat`, `SensorReading`) with clear responsibilities and utilized an interface (`Analyser`) to promote flexibility and adherence to the Open/Closed Principle.
*   **Data Validation Strategies**: Implemented multiple layers of validation, from basic checks (non-empty strings) to complex ones (date formats, numeric ranges), and effectively logging errors to a separate file.
