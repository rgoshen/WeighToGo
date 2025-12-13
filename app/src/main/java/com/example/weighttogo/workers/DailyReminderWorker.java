package com.example.weighttogo.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.weighttogo.database.AchievementDAO;
import com.example.weighttogo.database.UserDAO;
import com.example.weighttogo.database.UserPreferenceDAO;
import com.example.weighttogo.database.WeighToGoDBHelper;
import com.example.weighttogo.database.WeightEntryDAO;
import com.example.weighttogo.models.WeightEntry;
import com.example.weighttogo.utils.SMSNotificationManager;
import com.example.weighttogo.utils.SessionManager;

import java.time.LocalDate;

/**
 * WorkManager Worker for sending daily reminder SMS.
 *
 * Scheduled as periodic work (24-hour interval) to remind users
 * to log their weight if they haven't already done so today.
 *
 * Behavior:
 * - Returns Result.success() if no logged-in user (skip reminder)
 * - Returns Result.success() if user already logged weight today (skip reminder)
 * - Calls SMSNotificationManager.sendDailyReminderSms() if user hasn't logged
 * - Returns Result.success() if SMS sent or skipped
 * - Returns Result.retry() if SMS sending failed (will retry later)
 *
 * Scheduling:
 * Scheduled from SettingsActivity when user enables daily reminders
 * (see handleReminderToggle() method).
 *
 * Phase 7.6 - Daily Reminder System
 */
public class DailyReminderWorker extends Worker {

    private static final String TAG = "DailyReminderWorker";

    /**
     * Constructor required by WorkManager.
     *
     * @param context Application context
     * @param params  Worker parameters
     */
    public DailyReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Executes daily reminder logic.
     *
     * Checks if user logged weight today, and sends SMS reminder if not.
     *
     * @return Result.success() if reminder sent/skipped, Result.retry() if failed
     */
    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        Log.d(TAG, "doWork: Daily reminder worker started");

        // Get current user ID from SessionManager
        long userId = SessionManager.getInstance(context).getCurrentUserId();
        if (userId == -1) {
            Log.d(TAG, "doWork: No logged in user, skipping reminder");
            return Result.success();
        }

        Log.d(TAG, "doWork: Checking reminder for user_id=" + userId);

        // Initialize managers
        WeighToGoDBHelper dbHelper = WeighToGoDBHelper.getInstance(context);
        WeightEntryDAO weightEntryDAO = new WeightEntryDAO(dbHelper);
        AchievementDAO achievementDAO = new AchievementDAO(dbHelper);
        UserDAO userDAO = new UserDAO(dbHelper);
        UserPreferenceDAO userPreferenceDAO = new UserPreferenceDAO(dbHelper);
        SMSNotificationManager smsManager = SMSNotificationManager.getInstance(context,
                userDAO, userPreferenceDAO, achievementDAO);

        // Check if user logged weight today
        LocalDate today = LocalDate.now();
        WeightEntry todayEntry = weightEntryDAO.getWeightEntryForDate(userId, today);

        if (todayEntry != null) {
            Log.d(TAG, "doWork: User already logged weight today, skipping reminder");
            return Result.success();
        }

        Log.d(TAG, "doWork: User has not logged weight today, attempting to send reminder SMS");

        // Send reminder SMS
        // Note: sendDailyReminderSms() returns false if:
        // - User has no phone number (configuration issue - don't retry)
        // - Reminders are disabled (user preference - don't retry)
        // - SMS notifications are disabled (user preference - don't retry)
        // - Actual SMS send failed (could retry, but will be handled by next scheduled run)
        // Always return success to avoid unnecessary retries
        boolean sent = smsManager.sendDailyReminderSms(userId);

        if (sent) {
            Log.i(TAG, "doWork: Daily reminder SMS sent to user " + userId);
        } else {
            Log.d(TAG, "doWork: Daily reminder SMS not sent (user config or send failure)");
        }

        return Result.success();
    }
}
