package com.example.weighttogo.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.weighttogo.models.UserPreference;
import com.example.weighttogo.utils.DateTimeConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for user_preferences table.
 *
 * Provides generic key-value preference storage with UPSERT pattern using
 * SQLite's INSERT OR REPLACE. Includes type-safe convenience methods for
 * common preferences like weight_unit.
 *
 * This class follows the DAO pattern used by UserDAO, WeightEntryDAO, and GoalWeightDAO.
 */
public class UserPreferenceDAO {

    private static final String TAG = "UserPreferenceDAO";

    // Preference keys
    public static final String KEY_WEIGHT_UNIT = "weight_unit";

    // Valid weight units
    private static final String UNIT_LBS = "lbs";
    private static final String UNIT_KG = "kg";

    private final WeighToGoDBHelper dbHelper;

    /**
     * Constructor.
     *
     * @param dbHelper the database helper instance
     */
    public UserPreferenceDAO(@NonNull WeighToGoDBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Gets a preference value for a user, returning default if not found.
     *
     * @param userId the user ID
     * @param key the preference key
     * @param defaultValue the default value to return if key not found
     * @return the preference value, or defaultValue if not found
     */
    @NonNull
    public String getPreference(long userId, @NonNull String key, @NonNull String defaultValue) {
        Log.d(TAG, "getPreference: user_id=" + userId + ", key=" + key);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(
                WeighToGoDBHelper.TABLE_USER_PREFERENCES,
                new String[]{"pref_value"},
                "user_id = ? AND pref_key = ?",
                new String[]{String.valueOf(userId), key},
                null, null, null, "1"
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                String value = cursor.getString(0);
                Log.i(TAG, "getPreference: Found value for key=" + key);
                return value;
            }
        } catch (Exception e) {
            Log.e(TAG, "getPreference: Exception", e);
        }

        Log.d(TAG, "getPreference: Key not found, returning default");
        return defaultValue;
    }

    /**
     * Sets a preference value for a user (UPSERT).
     * Uses INSERT OR REPLACE to handle both insert and update.
     *
     * @param userId the user ID
     * @param key the preference key
     * @param value the preference value
     * @return true if successful, false otherwise
     */
    public boolean setPreference(long userId, @NonNull String key, @NonNull String value) {
        Log.d(TAG, "setPreference: user_id=" + userId + ", key=" + key);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("pref_key", key);
        values.put("pref_value", value);

        String now = DateTimeConverter.toTimestamp(LocalDateTime.now());
        values.put("created_at", now);
        values.put("updated_at", now);

        try {
            long result = db.insertWithOnConflict(
                    WeighToGoDBHelper.TABLE_USER_PREFERENCES,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
            );

            if (result > 0) {
                Log.i(TAG, "setPreference: Successfully set key=" + key);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "setPreference: Exception", e);
        }

        return false;
    }

    /**
     * Gets all preferences for a user (package-private for testing).
     * Used by tests to verify UPSERT behavior (no duplicate keys).
     *
     * @param userId the user ID
     * @return list of all preferences for the user (never null)
     */
    @NonNull
    List<UserPreference> getAllPreferences(long userId) {
        Log.d(TAG, "getAllPreferences: user_id=" + userId);

        List<UserPreference> preferences = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                WeighToGoDBHelper.TABLE_USER_PREFERENCES,
                null,
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        )) {
            while (cursor != null && cursor.moveToNext()) {
                preferences.add(mapCursorToUserPreference(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllPreferences: Exception", e);
        }

        return preferences;
    }

    /**
     * Maps a database cursor to a UserPreference object.
     *
     * @param cursor the cursor positioned at a row
     * @return UserPreference object with data from cursor
     */
    private UserPreference mapCursorToUserPreference(@NonNull Cursor cursor) {
        UserPreference pref = new UserPreference();
        pref.setPreferenceId(cursor.getLong(cursor.getColumnIndexOrThrow("preference_id")));
        pref.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        pref.setPrefKey(cursor.getString(cursor.getColumnIndexOrThrow("pref_key")));
        pref.setPrefValue(cursor.getString(cursor.getColumnIndexOrThrow("pref_value")));

        String createdStr = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
        String updatedStr = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"));
        pref.setCreatedAt(DateTimeConverter.fromTimestamp(createdStr));
        pref.setUpdatedAt(DateTimeConverter.fromTimestamp(updatedStr));

        return pref;
    }

    /**
     * Gets the weight unit preference (defaults to "lbs").
     *
     * @param userId the user ID
     * @return the weight unit ("lbs" or "kg")
     */
    @NonNull
    public String getWeightUnit(long userId) {
        Log.d(TAG, "getWeightUnit: user_id=" + userId);
        return getPreference(userId, KEY_WEIGHT_UNIT, UNIT_LBS);
    }
}
