package vn.viettel.quanlycongno.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CsvUtils {

    /**
     * Parses a CSV line, properly handling quoted fields
     * @param line The CSV line to parse
     * @return Array of fields from the CSV line
     */
    public static String[] parseCsvLine(String line) {
        List<String> fields = Arrays.stream(line.split(","))
                .map(String::trim)
                .map(s -> s.replaceAll("^\"|\"$", "")) // Remove surrounding quotes
                .toList();

        return fields.toArray(new String[0]);
    }

    /**
     * Escapes a field for CSV output
     * @param field The field to escape
     * @return The escaped field
     */
    public static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Sets a string value in the DTO from the CSV values to a Date
     * @param columnIndexes Map of column names to their indexes
     * @param values Array of values from the CSV line
     * @param key The key for the value to set
     */
    public static void setDateValue(Map<String, Integer> columnIndexes,
                                    String[] values,
                                    SimpleDateFormat dateFormat,
                                    String key,
                                    ValueSetter<Date> setter
                                    ) throws ParseException {
        if (columnIndexes.containsKey(key)) {
            String val = values[columnIndexes.get(key)].trim();
            if (!val.isEmpty()) {
                setter.setValue(dateFormat.parse(val));
            }
        }
    }

    /**
     * Sets a string value in the DTO from the CSV values to a BigDecimal
     * @param columnIndexes Map of column names to their indexes
     * @param values Array of values from the CSV line
     * @param key The key for the value to set
     */
    public static void setNumericValue(Map<String, Integer> columnIndexes,
                                       String[] values,
                                       String key,
                                       ValueSetter<BigDecimal> setter) {
        if (columnIndexes.containsKey(key)) {
            String val = values[columnIndexes.get(key)].trim();
            if (!val.isEmpty()) {
                setter.setValue(new BigDecimal(val));
            }
        }
    }

    /**
     * Sets a string value in the DTO from the CSV values to a String
     * @param columnIndexes Map of column names to their indexes
     * @param columnName The name of the column to set
     * @param values Array of values from the CSV line
     * @param setter The setter to call with the value
     */
    public static void setStringValue(Map<String, Integer> columnIndexes,
                                      String columnName,
                                      String[] values,
                                      ValueSetter<String> setter) {
        if (columnIndexes.containsKey(columnName)) {
            String val = values[columnIndexes.get(columnName)].trim();
            if (!val.isEmpty()) {
                setter.setValue(val);
            }
        }
    }

    public interface ValueSetter<T> {
        void setValue(T value);
    }
}