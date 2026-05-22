# Manual Test Scenarios - Phase 8

## Overview

This document contains manual test scenarios for features that cannot be automated with Espresso due to framework limitations. These tests must be executed manually on a real Android device.

**Espresso Limitations:**
- Cannot verify Toast message content (GH #49)
- Cannot interact with system permission dialogs
- Cannot test actual SMS sending (requires real SIM card)
- Cannot test push notifications (system-level)
- Cannot test WorkManager time-based execution (requires waiting hours)

**Test Environment Requirements:**
- Real Android device (not emulator) for SMS tests
- Android 13+ device for notification permission tests
- Active SIM card for SMS sending tests
- Test phone number configured in Settings

---

## Test Execution Instructions

For each test scenario:
1. Follow the **Test Steps** in order
2. Record **Actual Result** in the provided column
3. Mark test as **PASS** if actual matches expected, **FAIL** otherwise
4. For failures, document the issue in **Notes** column

**Status Legend:**
- ☐ Not Started
- ✓ PASS
- ✗ FAIL

---

## 1. Toast Message Verification

**Purpose:** Verify that toast messages display correct text for user feedback.

**Background:** Espresso cannot verify toast message content (GH #49 - limitation with Material3 theming). Visual confirmation required.

| Test Case | Test Steps | Expected Result | Actual Result | Status | Notes |
|-----------|------------|----------------|---------------|--------|-------|
| 1.1 Weight Unit Changed | 1. Open Settings<br>2. Click "kg" toggle | Toast: "Weight unit updated to kg" appears for 2 seconds | | ☐ | |
| 1.2 Phone Number Saved | 1. Open Settings<br>2. Enter phone "2025551234"<br>3. Press Enter or navigate away | Toast: "Phone number saved" appears for 2 seconds | | ☐ | |
| 1.3 Weight Validation Error | 1. Open WeightEntry<br>2. Enter weight "800" lbs<br>3. Click Save | Toast: "Weight must be between 0.0 and 700.0 lbs" appears for 3 seconds | | ☐ | |
| 1.4 Entry Saved Success | 1. Open WeightEntry<br>2. Enter valid weight "150.5" lbs<br>3. Click Save | Toast: "Entry saved successfully" appears for 2 seconds | | ☐ | |

---

## 2. SMS Permission Dialog Handling

**Purpose:** Verify that system permission dialogs appear and are handled correctly.

**Background:** Espresso cannot interact with system permission dialogs (outside app sandbox).

| Test Case | Test Steps | Expected Result | Actual Result | Status | Notes |
|-----------|------------|----------------|---------------|--------|-------|
| 2.1 Permission Dialog Appears | 1. Open Settings<br>2. Click "Grant SMS Permission" button | Android system permission dialog appears with "Allow" and "Deny" options | | ☐ | |
| 2.2 Grant Permission | 1. Trigger permission dialog<br>2. Click "Allow" in system dialog | - Dialog closes<br>- Status badge changes to "Granted" (green checkmark)<br>- Button changes to "Revoke Permission" | | ☐ | |
| 2.3 Deny Permission | 1. Trigger permission dialog<br>2. Click "Deny" in system dialog | - Dialog closes<br>- Status badge changes to "Denied" (red X)<br>- Button remains "Grant Permission" | | ☐ | |
| 2.4 Deny with "Don't Ask Again" | 1. Trigger permission dialog<br>2. Check "Don't ask again"<br>3. Click "Deny" | - Dialog closes<br>- Status badge shows "Denied"<br>- Button disabled or shows "Open Settings" | | ☐ | |

---

## 3. Actual SMS Sending (Real Device Required)

**Purpose:** Verify that SMS messages are sent correctly to configured phone number.

**Prerequisites:**
- Real Android device with active SIM card
- Test phone number configured in Settings
- Access to phone number to receive SMS

| Test Case | Test Steps | Expected Result | Actual Result | Status | Notes |
|-----------|------------|----------------|---------------|--------|-------|
| 3.1 Send Test SMS | 1. Open Settings<br>2. Configure phone number<br>3. Click "Send Test Message" button | SMS received on configured phone:<br>"Test message from Weigh to Go!" | | ☐ | Record phone number used:<br><br>SMS timestamp: |
| 3.2 Goal Achieved SMS | 1. Set goal weight to 150.0 lbs<br>2. Add weight entry of 150.0 lbs<br>3. System detects goal achieved | SMS received:<br>"Congratulations! You've reached your goal weight of 150.0 lbs!" | | ☐ | Record actual SMS text:<br><br>Timestamp: |
| 3.3 Milestone Reached SMS | 1. Add weight entries to reach 5 lbs lost<br>2. System detects milestone | SMS received:<br>"Great progress! You've lost 5.0 lbs. Keep it up!" | | ☐ | Record actual SMS text:<br><br>Timestamp: |

---

## 4. Push Notification Permission (Android 13+)

**Purpose:** Verify that notification permission is requested and notifications work correctly on Android 13+.

**Prerequisites:**
- Android 13 (API 33) or higher device

| Test Case | Test Steps | Expected Result | Actual Result | Status | Notes |
|-----------|------------|----------------|---------------|--------|-------|
| 4.1 Notification Permission Dialog (Android 13+) | 1. Install app on Android 13+ device<br>2. Open app for first time | System permission dialog appears requesting notification access | | ☐ | Android version:<br><br>API level: |
| 4.2 Notification Channel Created | 1. Grant notification permission<br>2. Go to device Settings → Apps → Weigh to Go → Notifications | Notification channels created:<br>- "Goal Alerts"<br>- "Milestone Alerts"<br>- "Daily Reminders" | | ☐ | List visible channels: |
| 4.3 Test Notification Appears | 1. Trigger a notification (e.g., goal achieved)<br>2. Pull down notification tray | Notification appears with:<br>- App icon<br>- Title: "Goal Achieved!"<br>- Message: Goal details | | ☐ | Screenshot attached: Y/N |

---

## 5. Daily Reminder Scheduling (WorkManager)

**Purpose:** Verify that WorkManager correctly schedules and executes daily reminder tasks.

**Background:** Cannot test time-based execution in automated tests (requires waiting hours).

**Note:** This test requires significant time investment (24+ hours).

| Test Case | Test Steps | Expected Result | Actual Result | Status | Notes |
|-----------|------------|----------------|---------------|--------|-------|
| 5.1 Enable Reminder Schedules Task | 1. Open Settings<br>2. Enable "Daily Reminders" toggle<br>3. Set time to 9:00 AM<br>4. Use ADB to check WorkManager:<br>`adb shell dumpsys jobscheduler` | WorkManager task scheduled:<br>- Task ID visible in jobscheduler<br>- Next run time: tomorrow 9:00 AM | | ☐ | ADB output:<br><br>Scheduled time: |
| 5.2 Reminder Executes at Scheduled Time | 1. Wait until 9:00 AM next day<br>2. Check for notification/SMS | At 9:00 AM:<br>- Notification appears: "Daily Weight Reminder"<br>- OR SMS sent (if SMS enabled) | | ☐ | Execution time:<br><br>Notification received: Y/N<br>SMS received: Y/N |
| 5.3 Disable Reminder Cancels Task | 1. Enable reminder (if not already)<br>2. Disable "Daily Reminders" toggle<br>3. Use ADB to check WorkManager:<br>`adb shell dumpsys jobscheduler` | WorkManager task cancelled:<br>- Task ID no longer in jobscheduler<br>- No reminder sent at 9:00 AM | | ☐ | ADB output after disable:<br><br>Verified no reminder: Y/N |

---

## 6. Cross-Activity Preference Propagation

**Purpose:** Verify that user preferences (weight unit) are correctly shared and displayed across all activities.

**Background:** Tests preference persistence and real-time UI updates across Activity boundaries.

| Test Case | Test Steps | Expected Result | Actual Result | Status | Notes |
|-----------|------------|----------------|---------------|--------|-------|
| 6.1 Settings → WeightEntry Propagation | 1. Open Settings<br>2. Change unit to "kg"<br>3. Navigate back<br>4. Open WeightEntry screen | WeightEntry screen displays:<br>- Weight label: "Weight (kg)"<br>- Placeholder: "Enter weight in kg"<br>- Quick adjust buttons show kg increments | | ☐ | Screenshot attached: Y/N |
| 6.2 Settings → MainActivity Propagation | 1. Open Settings<br>2. Change unit to "lbs"<br>3. Navigate back to MainActivity | MainActivity displays:<br>- Weight history entries show "lbs"<br>- Chart Y-axis labeled "Weight (lbs)"<br>- Current weight shows "lbs" | | ☐ | Screenshot attached: Y/N |
| 6.3 Preference Persists After App Restart | 1. Open Settings<br>2. Change unit to "kg"<br>3. Close app completely (swipe away from recents)<br>4. Reopen app | - MainActivity shows "kg"<br>- Settings shows "kg" selected<br>- WeightEntry shows "kg" | | ☐ | Verified all screens: Y/N |

---

## Test Summary

**Execution Date:** _______________
**Tester Name:** _______________
**Device Information:**
- Model: _______________
- Android Version: _______________
- API Level: _______________

**Results:**
- Total Test Cases: 18
- Passed: _____
- Failed: _____
- Not Executed: _____

**Overall Status:** ☐ PASS  ☐ FAIL  ☐ PARTIAL

**Notes:**
(Record any issues, blockers, or observations here)

---

## Known Issues

| Issue ID | Test Case | Description | Severity | Workaround | Status |
|----------|-----------|-------------|----------|------------|--------|
| GH #49 | 1.1-1.4 | Espresso cannot verify Toast content with Material3 | Low | Manual verification | Open |
| | | | | | |

---

## References

- **GitHub Issue #49:** Espresso Toast verification limitation with Material3
- **Android Testing Best Practices:** https://developer.android.com/training/testing/fundamentals
- **WorkManager Testing Guide:** https://developer.android.com/topic/libraries/architecture/workmanager/how-to/testing-210

---

**Document Version:** 1.0
**Last Updated:** 2025-12-14
**Author:** Phase 8 Test Coverage Enhancement
