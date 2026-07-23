import java.time.LocalDate;

public class SensorReading {
    private String sensorId;
    private String zone;
    private LocalDate readingDate;
    private double temperature;

    public SensorReading(String sensorId, String zone, LocalDate readingDate, double temperature) throws SensorDataException {
        if (readingDate == null) {
            throw new SensorDataException("Invalid Reading Date");
        }
        if (temperature < -30.0 || temperature > 60.0) {
            throw new SensorDataException("Invalid Temperature: " + temperature);
        }
        
        this.sensorId = sensorId;
        this.zone = zone;
        this.readingDate = readingDate;
        this.temperature = temperature;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getZone() {
        return zone;
    }

    public int getYear() {
        return readingDate.getYear();
    }

    public int getMonth() {
        return readingDate.getMonthValue();
    }

    public double getTemperature() {
        return temperature;
    }

    public LocalDate getReadingDateTime() {
        return readingDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SensorReading that = (SensorReading) obj;
        return sensorId.equals(that.sensorId) && readingDate.equals(that.readingDate);
    }

    @Override
    public String toString() {
        return "SensorReading{" +
                "sensorId='" + sensorId + '\'' +
                ", zone='" + zone + '\'' +
                ", readingDate=" + readingDate +
                ", temperature=" + temperature +
                '}';
    }
}
