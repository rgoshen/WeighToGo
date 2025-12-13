package com.example.weighttogo.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.weighttogo.database.UserDAO;
import com.example.weighttogo.database.UserPreferenceDAO;
import com.example.weighttogo.models.User;

/**
 * Singleton manager for sending SMS notifications.
 *
 * Handles SMS sending for:
 * - Goal achievements
 * - Milestone alerts (5, 10, 25 lbs lost)
 * - Daily reminders
 *
 * **Permissions Required:**
 * - android.permission.SEND_SMS (all Android versions)
 * - android.permission.POST_NOTIFICATIONS (Android 13+)
 *
 * **User Preferences:**
 * - sms_notifications_enabled (master toggle)
 * - sms_goal_alerts (goal reached alerts)
 * - sms_milestone_alerts (milestone alerts)
 * - sms_reminder_enabled (daily reminder)
 *
 * **Thread Safety:** Singleton pattern with synchronized getInstance()
 */
public class SMSNotificationManager {

    private static final String TAG = "SMSNotificationManager";

    // Singleton instance
    private static SMSNotificationManager instance;

    // Preference keys (used by SettingsActivity and UserPreferenceDAO)
    public static final String KEY_SMS_ENABLED = "sms_notifications_enabled";
    public static final String KEY_GOAL_ALERTS = "sms_goal_alerts";
    public static final String KEY_MILESTONE_ALERTS = "sms_milestone_alerts";
    public static final String KEY_REMINDER_ENABLED = "sms_reminder_enabled";

    // Dependencies
    private final Context context;
    private final UserDAO userDAO;
    private final UserPreferenceDAO userPreferenceDAO;

    /**
     * Private constructor for singleton pattern.
     *
     * @param context Application context
     * @param userDAO UserDAO instance
     * @param userPreferenceDAO UserPreferenceDAO instance
     */
    private SMSNotificationManager(@NonNull Context context,
                                    @NonNull UserDAO userDAO,
                                    @NonNull UserPreferenceDAO userPreferenceDAO) {
        this.context = context.getApplicationContext();
        this.userDAO = userDAO;
        this.userPreferenceDAO = userPreferenceDAO;
    }

    /**
     * Gets singleton instance of SMSNotificationManager.
     * Thread-safe with synchronized block.
     *
     * @param context Application context
     * @param userDAO UserDAO instance
     * @param userPreferenceDAO UserPreferenceDAO instance
     * @return Singleton instance
     */
    public static synchronized SMSNotificationManager getInstance(@NonNull Context context,
                                                                   @NonNull UserDAO userDAO,
                                                                   @NonNull UserPreferenceDAO userPreferenceDAO) {
        if (instance == null) {
            instance = new SMSNotificationManager(context, userDAO, userPreferenceDAO);
            Log.d(TAG, "getInstance: Created new SMSNotificationManager instance");
        }
        return instance;
    }

    /**
     * Checks if SEND_SMS permission is granted.
     *
     * @return true if permission granted, false otherwise
     */
    public boolean hasSmsSendPermission() {
        boolean hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "hasSmsSendPermission: " + hasPermission);
        return hasPermission;
    }

    /**
     * Checks if POST_NOTIFICATIONS permission is granted (Android 13+).
     * Returns true on Android 12 and below (permission not required).
     *
     * @return true if permission granted or not required, false otherwise
     */
    public boolean hasPostNotificationsPermission() {
        // Android 12 and below don't require POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "hasPostNotificationsPermission: true (Android < 13, permission not required)");
            return true;
        }

        // Android 13+ requires POST_NOTIFICATIONS
        boolean hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "hasPostNotificationsPermission: " + hasPermission + " (Android 13+)");
        return hasPermission;
    }

    /**
     * Checks if SMS can be sent for a user.
     *
     * Checks:
     * 1. User has phone number
     * 2. SMS notifications enabled in preferences
     * 3. SEND_SMS permission granted
     * 4. POST_NOTIFICATIONS permission granted (Android 13+)
     *
     * @param userId User ID to check
     * @return true if all conditions met, false otherwise
     */
    public boolean canSendSms(long userId) {
        Log.d(TAG, "canSendSms: Checking for user_id=" + userId);

        // Check user has phone number
        User user = userDAO.getUserById(userId);
        if (user == null || user.getPhoneNumber() == null) {
            Log.w(TAG, "canSendSms: User or phone number not found");
            return false;
        }

        // Check SMS notifications enabled
        String smsEnabled = userPreferenceDAO.getPreference(userId, KEY_SMS_ENABLED, "false");
        if (!"true".equals(smsEnabled)) {
            Log.d(TAG, "canSendSms: SMS notifications disabled in preferences");
            return false;
        }

        // Check permissions
        if (!hasSmsSendPermission()) {
            Log.w(TAG, "canSendSms: SEND_SMS permission not granted");
            return false;
        }

        if (!hasPostNotificationsPermission()) {
            Log.w(TAG, "canSendSms: POST_NOTIFICATIONS permission not granted");
            return false;
        }

        Log.d(TAG, "canSendSms: All conditions met, can send SMS");
        return true;
    }

    /**
     * Sends SMS for goal achievement.
     * Stub implementation - will be implemented in Commit 13.
     *
     * @param userId User ID
     * @param goalWeight Goal weight achieved
     * @param unit Weight unit (lbs/kg)
     * @return false (stub)
     */
    public boolean sendGoalAchievedSms(long userId, double goalWeight, String unit) {
        Log.d(TAG, "sendGoalAchievedSms: Stub - not implemented yet");
        return false;
    }

    /**
     * Sends SMS for milestone achievement.
     * Stub implementation - will be implemented in Commit 13.
     *
     * @param userId User ID
     * @param milestone Milestone amount (e.g., 5, 10, 25)
     * @param unit Weight unit (lbs/kg)
     * @return false (stub)
     */
    public boolean sendMilestoneSms(long userId, int milestone, String unit) {
        Log.d(TAG, "sendMilestoneSms: Stub - not implemented yet");
        return false;
    }

    /**
     * Sends daily reminder SMS.
     * Stub implementation - will be implemented in Commit 13.
     *
     * @param userId User ID
     * @return false (stub)
     */
    public boolean sendDailyReminderSms(long userId) {
        Log.d(TAG, "sendDailyReminderSms: Stub - not implemented yet");
        return false;
    }
}
