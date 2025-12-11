package com.example.weighttogo.utils;

import android.util.Log;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for converting between Java 8 date/time types and SQLite-compatible strings.
 *
 * SQLite stores dates and times as TEXT in ISO-8601 format:
 * - Timestamps: "yyyy-MM-dd HH:mm:ss"
 * - Dates: "yyyy-MM-dd"
 *
 * This class provides bidirectional conversion with proper validation and error handling.
 *
 * This is a utility class with only static methods. It is declared final and has a private
 * constructor to prevent instantiation and inheritance.
 */
public final class DateTimeConverter {

    private static final String TAG = "DateTimeConverter";

    // SQLite-compatible timestamp format
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);

    // SQLite-compatible date format
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     *
     * @throws AssertionError if instantiation is attempted
     */
    private DateTimeConverter() {
        throw new AssertionError("DateTimeConverter is a utility class and should not be instantiated");
    }

    /**
     * Converts a LocalDateTime to SQLite-compatible timestamp string.
     *
     * @param dateTime the LocalDateTime to convert
     * @return timestamp string in "yyyy-MM-dd HH:mm:ss" format, or null if input is null
     */
    public static String toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            Log.w(TAG, "toTimestamp called with null LocalDateTime");
            return null;
        }

        try {
            return dateTime.format(TIMESTAMP_FORMATTER);
        } catch (DateTimeException e) {
            Log.e(TAG, "Error formatting LocalDateTime to timestamp: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converts a SQLite timestamp string to LocalDateTime.
     *
     * @param timestamp the timestamp string in "yyyy-MM-dd HH:mm:ss" format
     * @return LocalDateTime object, or null if input is null/invalid
     */
    public static LocalDateTime fromTimestamp(String timestamp) {
        if (timestamp == null || timestamp.trim().isEmpty()) {
            Log.w(TAG, "fromTimestamp called with null or empty string");
            return null;
        }

        try {
            return LocalDateTime.parse(timestamp, TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error parsing timestamp string: " + timestamp, e);
            return null;
        }
    }

    /**
     * Converts a LocalDate to SQLite-compatible date string.
     *
     * @param date the LocalDate to convert
     * @return date string in "yyyy-MM-dd" format, or null if input is null
     */
    public static String toDateString(LocalDate date) {
        if (date == null) {
            Log.w(TAG, "toDateString called with null LocalDate");
            return null;
        }

        try {
            return date.format(DATE_FORMATTER);
        } catch (DateTimeException e) {
            Log.e(TAG, "Error formatting LocalDate to date string: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converts a SQLite date string to LocalDate.
     *
     * @param dateString the date string in "yyyy-MM-dd" format
     * @return LocalDate object, or null if input is null/invalid
     */
    public static LocalDate fromDateString(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            Log.w(TAG, "fromDateString called with null or empty string");
            return null;
        }

        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Error parsing date string: " + dateString, e);
            return null;
        }
    }
}