# Weight-Tracking Mobile Application - Technical Requirements Document

**Project Name**: Weigh to Go!  
**Tagline**: "You've got this—pound for pound."  
**Developer**: Rick Goshen  
**Course**: CS 360 - Mobile Architecture & Programming  
**Platform**: Android  
**Development Environment**: Android Studio  
**Primary Language**: Java  
**UI Framework**: XML Layouts  
**Target API Level**: 34 (Android 14)  
**Minimum SDK**: 26 (Android 8.0)  
**Last Updated**: December 2025  
**Version**: 1.0

---

## 📋 Table of Contents
1. [Executive Summary](#executive-summary)
2. [Functional Requirements](#functional-requirements)
3. [Database Architecture](#database-architecture)
4. [User Interface Requirements](#user-interface-requirements)
5. [Technical Implementation Details](#technical-implementation-details)
6. [Security & Privacy Requirements](#security--privacy-requirements)
7. [User Experience Requirements](#user-experience-requirements)
8. [Testing Requirements](#testing-requirements)
9. [Future Enhancements](#future-enhancements)
10. [Development Checklist](#development-checklist)

---

## Executive Summary

### Purpose
Provide users with a simple, effective tool for monitoring daily weight and progress toward personal health goals through daily tracking, visual progress display, goal-oriented notifications, and **SMS alerts** for milestone achievements and goal completions.

### Target Users
1. **Weight-Loss Seekers**: Individuals trying to lose weight for health or personal reasons
2. **Health Maintenance Monitors**: Users maintaining current weight or monitoring for medical purposes
3. **Family Health Managers**: Parents/caregivers tracking weight for multiple family members

### Core Value Proposition
Streamlined weight tracking without overwhelming complexity - focusing on essential features: secure login, daily weight entry, historical data display, goal s, achievement notifications, and **SMS alerts** for milestone celebrations.

### Key Features
- **Secure Authentication**: User registration and login with hashed passwords
- **Daily Weight Tracking**: Simple weight entry with date selection
- **Weight History**: Scrollable grid view of all entries
- **Goal Management**: Set and track target weight goals
- **Achievement System**: Celebrate milestones and goal completions
- **📱 SMS Notification System**: Optional text message alerts for goals, milestones, and daily reminders
- **Dual Notification System**: In-app notifications plus optional SMS alerts

---

## Functional Requirements

### FR-1: User Authentication System

#### FR-1.1: User Registration
**Priority**: Critical  
**Description**: Allow new users to create an account

**Acceptance Criteria**:
- User can enter username/email and password
- **User can optionally enter phone number for SMS notifications**
- System validates username is unique
- System validates password meets minimum requirements (8+ characters, mixed case, number)
- **System validates phone number format (E.164 international format) if provided**
- System stores credentials securely with SHA-256 hashing and salt
- System prevents duplicate usernames
- User receives confirmation of successful registration
- System automatically logs user in after successful registration
- **If phone number provided, system explains SMS notification feature**

**Database Impact**: Insert new record into `users` table with optional phone number

**UI Components**:
- Username/email input field
- **Phone number input field (optional, with SMS explanation)**
- Password input field
- Confirm password input field
- "Create Account" button
- Link to switch to login screen
- **Helper text explaining SMS notification benefits**

#### FR-1.2: User Login
**Priority**: Critical  
**Description**: Allow existing users to access their account

**Acceptance Criteria**:
- User can enter username and password
- System validates credentials against database using secure hash comparison
- System grants access if credentials are correct
- System displays error message if credentials are incorrect
- System maintains user session until logout
- System prevents unauthorized access to user data
- System updates `last_login` timestamp on successful login

**Database Impact**: Query `users` table for credential verification, update `last_login`

**UI Components**:
- Username input field
- Password input field
- "Login" button
- Link to switch to registration screen

#### FR-1.3: User Logout
**Priority**: High  
**Description**: Allow users to securely end their session

**Acceptance Criteria**:
- User can access logout option from main menu
- System clears user session data
- System returns user to login screen
- System prevents unauthorized access to previous user's data

**Database Impact**: None (session management only)

**UI Components**:
- Logout menu item
- Confirmation dialog (optional)

---

### FR-2: Weight Entry Management

#### FR-2.1: Add Weight Entry
**Priority**: Critical  
**Description**: Allow users to record their daily weight

**Acceptance Criteria**:
- User can enter weight value (positive number with up to 2 decimal places)
- User can select date for entry (default: today, cannot be future date)
- User can toggle between lbs and kg units
- User can optionally add notes (multiline text, max 500 characters)
- System validates weight is positive and within reasonable range (1-1000 lbs)
- System validates date is not in the future
- System saves entry to database with timestamp
- System displays success confirmation
- **System checks for goal achievement and triggers SMS notification if enabled**
- **System checks for milestone achievement (every 5 lbs lost) and triggers SMS if enabled**
- System refreshes weight history display

**Database Impact**: Insert new record into `daily_weights` table, query for achievements

**UI Components**:
- Weight input field (numeric keyboard)
- Unit toggle button (lbs/kg)
- Date picker button (opens calendar dialog)
- Notes input field (multiline, optional)
- "Save" button (primary action)
- "Cancel" button
- Success toast message

#### FR-2.2: View Weight History
**Priority**: Critical  
**Description**: Display chronological list of all weight entries

**Acceptance Criteria**:
- System displays all weight entries for current user
- Entries sorted by date (most recent first)
- Each entry shows: date, weight value, unit, trend indicator
- Trend indicator shows change from previous entry (↓ green, ↑ red, — orange for no change)
- System calculates and displays weight difference from previous entry
- Empty state message displayed if no entries exist
- System handles large datasets efficiently (pagination or virtual scrolling)

**Database Impact**: Query all `daily_weights` for current user, ordered by date descending

**UI Components**:
- RecyclerView or ListView for scrollable history
- Item layout showing: date, weight, trend indicator, difference
- Empty state view with motivational message
- Pull-to-refresh gesture (optional)

#### FR-2.3: Edit Weight Entry
**Priority**: High  
**Description**: Allow users to modify existing weight entries

**Acceptance Criteria**:
- User can select existing entry to edit
- User can modify weight value, date, and notes
- System validates modified data same as new entry
- System updates database record
- System refreshes display to show updated entry
- System displays success confirmation

**Database Impact**: Update record in `daily_weights` table

**UI Components**:
- Edit button/icon on each history item
- Same input fields as add entry (pre-populated)
- "Save Changes" button
- "Cancel" button

#### FR-2.4: Delete Weight Entry
**Priority**: High  
**Description**: Allow users to remove weight entries

**Acceptance Criteria**:
- User can select entry to delete
- System shows confirmation dialog before deletion
- System removes entry from database on confirmation
- System refreshes display to remove deleted entry
- System displays success confirmation
- **Critical: Delete button must be visible and functional on each grid row per Project Three rubric**

**Database Impact**: Delete record from `daily_weights` table

**UI Components**:
- Delete button/icon on each history item
- Confirmation dialog
- Success toast message

---

### FR-3: Goal Weight Management

#### FR-3.1: Set Goal Weight
**Priority**: Critical  
**Description**: Allow users to define their target weight

**Acceptance Criteria**:
- User can enter goal weight value (positive number)
- User can select target date (optional)
- User can specify goal type (lose weight or gain weight)
- System validates goal weight is positive
- System validates target date is in the future (if provided)
- System saves goal to database
- System displays goal on dashboard
- System calculates and displays progress percentage
- System displays success confirmation

**Database Impact**: Insert new record into `goal_weights` table, set previous goals to inactive

**UI Components**:
- Goal weight input field (numeric keyboard)
- Unit display (matches user's preferred unit)
- Target date picker (optional)
- Goal type selector (lose/gain)
- "Set Goal" button
- Success toast message

#### FR-3.2: Track Goal Progress
**Priority**: Critical  
**Description**: Monitor and display progress toward goal weight

**Acceptance Criteria**:
- System calculates current progress as percentage of goal completion
- System displays progress visually (progress bar)
- System shows: starting weight, current weight, goal weight, remaining amount
- System updates progress automatically when new weight entry added
- System displays encouragement messages based on progress
- **System triggers SMS notification when goal is achieved (100% progress)**
- System celebrates milestone progress (25%, 50%, 75%)

**Database Impact**: Query `daily_weights` and `goal_weights` tables, calculate progress

**UI Components**:
- Progress bar (visual indicator)
- Progress percentage text
- Starting weight label and value
- Current weight label and value (latest entry)
- Goal weight label and value
- Remaining amount label and value
- Motivational message based on progress

#### FR-3.3: Update Goal Weight
**Priority**: Medium  
**Description**: Allow users to modify existing goal

**Acceptance Criteria**:
- User can edit current active goal
- User can modify goal weight value
- User can modify target date
- System validates modified data same as new goal
- System updates database record
- System recalculates and displays updated progress
- System displays success confirmation

**Database Impact**: Update record in `goal_weights` table

**UI Components**:
- Edit button on goal display
- Same input fields as set goal (pre-populated)
- "Update Goal" button
- Success toast message

---

### FR-4: Achievement Notifications

#### FR-4.1: Goal Achievement Notification
**Priority**: High  
**Description**: Celebrate when user reaches goal weight

**Acceptance Criteria**:
- System detects when current weight reaches or passes goal weight
- System triggers celebration immediately after weight entry saved
- **System sends SMS notification if permission granted and user enabled goal notifications**
- System displays in-app notification/dialog with congratulatory message
- System offers option to set new goal
- System records achievement in database for history tracking

**Database Impact**: Query to detect goal achievement, insert into `achievements` table

**UI Components**:
- Celebration dialog with animation/confetti
- Congratulatory message
- "Set New Goal" button
- "Close" button

#### FR-4.2: Milestone Achievement Notification
**Priority**: Medium  
**Description**: Celebrate weight loss/gain milestones

**Acceptance Criteria**:
- System detects milestones (every 5 lbs or custom intervals)
- **System sends SMS notification if permission granted and milestone notifications enabled**
- System displays in-app toast or small notification
- System tracks all milestones achieved
- System prevents duplicate notifications for same milestone

**Database Impact**: Calculate total weight change, check milestone thresholds, insert into `achievements`

**UI Components**:
- Toast message or snackbar
- Milestone badge/icon
- Motivational message

---

### FR-5: SMS Notification System 🆕

#### FR-5.1: SMS Permission Management
**Priority**: Critical  
**Description**: Request and manage SMS sending permissions

**Acceptance Criteria**:
- System checks SMS permission status on settings screen load
- System displays current permission status clearly (granted/denied)
- System uses modern permission API (ActivityResultContracts)
- User can request SMS permission via prominent button
- System shows permission rationale dialog before system prompt (if needed)
- System handles all permission responses (granted, denied, permanently denied)
- System updates UI immediately after permission response
- **System never requests permission on app first launch (wait until user accesses settings)**
- System provides "Open App Settings" option if permission permanently denied
- **System ensures app continues to function fully if permission denied**

**Android Manifest Requirements**:
```xml
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-feature 
    android:name="android.hardware.telephony" 
    android:required="false" />
```

**Database Impact**: Query `user_preferences` table for SMS settings

**UI Components**:
- Permission status card with icon and text
- Permission status indicator (green checkmark or red warning)
- "Enable SMS Notifications" button (if not granted)
- "Open App Settings" button (if permanently denied)
- Permission explanation text
- Warning card explaining limited functionality if denied

**Implementation Notes**:
```java
// Use ActivityResultContracts.RequestPermission (API 23+)
private final ActivityResultLauncher<String> requestPermissionLauncher =
    registerForActivityResult(new ActivityResultContracts.RequestPermission(), 
        isGranted -> {
            updatePermissionUI(isGranted);
            savePermissionPreference(isGranted);
        }
    );

// Check permission before requesting
private boolean checkSmsPermission() {
    return ContextCompat.checkSelfPermission(this, 
        Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
}

// Request permission
private void requestSmsPermission() {
    if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
        showPermissionRationaleDialog(); // Explain why we need it
    } else {
        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
    }
}
```

#### FR-5.2: SMS Notification Preferences
**Priority**: High  
**Description**: Allow users to configure SMS notification types

**Acceptance Criteria**:
- User can enable/disable goal achievement SMS notifications
- User can enable/disable milestone achievement SMS notifications
- User can enable/disable daily reminder SMS notifications
- User can set time for daily reminder (if enabled)
- User can view/edit phone number for SMS delivery
- **System disables all SMS toggles if permission not granted**
- System saves all preferences to database
- System loads user's preferences on settings screen
- System validates phone number format if changed
- **System only sends SMS if permission granted AND specific notification type enabled**

**Database Impact**: Query and update `user_preferences` table

**UI Components**:
- Phone number display/edit field
- Switch/toggle for "Goal Achievement Notifications"
- Switch/toggle for "Milestone Notifications" (every 5 lbs)
- Switch/toggle for "Daily Reminders"
- Time picker for daily reminder (if enabled)
- "Save Preferences" button
- All toggles disabled if permission not granted

**Preference Storage Structure**:
```java
// user_preferences table
{
    user_id: int,
    phone_number: string (E.164 format),
    sms_goal_enabled: boolean,
    sms_milestone_enabled: boolean,
    sms_daily_reminder_enabled: boolean,
    daily_reminder_time: string (HH:MM format),
    updated_at: timestamp
}
```

#### FR-5.3: SMS Message Sending
**Priority**: High  
**Description**: Send SMS messages for enabled notification types

**Acceptance Criteria**:
- **System only sends SMS if permission is granted**
- **System checks user preferences before sending each SMS**
- **System never sends SMS if user disabled that notification type**
- System sends SMS for goal achievement (if enabled)
- System sends SMS for milestones (if enabled and every 5 lbs threshold crossed)
- System sends daily reminder SMS (if enabled and scheduled time reached)
- System uses user's phone number from preferences
- System handles SMS sending failures gracefully (log error, don't crash)
- System provides feedback if SMS fails to send
- System prevents duplicate SMS for same event

**SMS Message Templates**:
```
Goal Achievement:
"🎉 Congratulations! You've reached your goal weight of {goal_weight} lbs! 
Great work staying committed to your health journey. - Weigh to Go!"

Milestone (5 lbs):
"💪 Way to go! You've lost {total_lost} lbs so far. Keep up the great work! 
You're {remaining} lbs from your goal. - Weigh to Go!"

Daily Reminder:
"⚖️ Good morning! Don't forget to log your weight today. 
Stay consistent with your health goals! - Weigh to Go!"
```

**Implementation Notes**:
```java
/**
 * Sends SMS notification if permission granted and user preferences allow
 * @param messageType Type of notification (goal, milestone, daily)
 * @param customData Additional data for message (weight values, etc.)
 */
private void sendSMSNotification(String messageType, Map<String, Object> customData) {
    // 1. Check permission
    if (!checkSmsPermission()) {
        Log.i("SMS", "Permission not granted - skipping SMS");
        return;
    }
    
    // 2. Get user preferences
    UserPreferences prefs = dbHelper.getUserPreferences(currentUserId);
    
    // 3. Check if this notification type is enabled
    if (!isNotificationTypeEnabled(messageType, prefs)) {
        Log.i("SMS", "Notification type disabled - skipping SMS");
        return;
    }
    
    // 4. Get phone number
    String phoneNumber = prefs.getPhoneNumber();
    if (phoneNumber == null || phoneNumber.isEmpty()) {
        Log.w("SMS", "No phone number configured");
        return;
    }
    
    // 5. Build message
    String message = buildMessage(messageType, customData);
    
    // 6. Send SMS
    try {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Log.i("SMS", "Message sent successfully");
    } catch (Exception e) {
        Log.e("SMS", "Failed to send SMS", e);
        showErrorToast("Failed to send SMS notification");
    }
}
```

#### FR-5.4: Daily Reminder Scheduling
**Priority**: Medium  
**Description**: Schedule and send daily reminder SMS at user-specified time

**Acceptance Criteria**:
- User can set specific time for daily reminder (e.g., 8:00 AM)
- System uses AlarmManager or WorkManager for scheduling
- System sends SMS at specified time if enabled and permission granted
- System handles app not running (alarm/worker persists)
- System reschedules after device reboot
- User can disable daily reminders
- System stops scheduling if user disables or permission revoked

**Android Components Required**:
- AlarmManager (simple timing) or WorkManager (more robust)
- BroadcastReceiver to handle alarm/work trigger
- BOOT_COMPLETED receiver to reschedule after reboot

**Implementation Notes**:
```java
// Use WorkManager for reliability
OneTimeWorkRequest reminderWork = new OneTimeWorkRequest.Builder(
    DailyReminderWorker.class)
    .setInitialDelay(calculateDelayUntilReminderTime(), TimeUnit.MILLISECONDS)
    .build();

WorkManager.getInstance(context).enqueue(reminderWork);

// Worker class
public class DailyReminderWorker extends Worker {
    @Override
    public Result doWork() {
        // Check permission and preferences
        // Send SMS if enabled
        // Reschedule for next day
        return Result.success();
    }
}
```

#### FR-5.5: Graceful Degradation Without SMS
**Priority**: Critical  
**Description**: Ensure full app functionality when SMS permission denied

**Acceptance Criteria**:
- **App continues to function normally if SMS permission denied**
- All core features work without SMS (login, weight tracking, goals, history)
- **In-app notifications still work for achievements (toast/dialog)**
- UI clearly indicates SMS features unavailable
- User can still access settings to enable permission later
- No crashes or errors due to missing SMS permission
- App never repeatedly asks for permission if user denied

**Fallback Mechanisms**:
- Goal achievement → Show in-app dialog instead of SMS
- Milestones → Show toast notification instead of SMS
- Daily reminders → Show push notification instead of SMS (future enhancement)

**User Communication**:
- Clear messaging in settings: "SMS notifications require permission"
- Explanation of what features require SMS
- Guidance on how to enable permission if desired

---

## Database Architecture

### Overview
The app uses SQLite for local data persistence. Database consists of 5 normalized tables with foreign key relationships enforced.

### Database Schema

#### Table 1: users
```sql
CREATE TABLE users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT,
    phone_number TEXT,  -- E.164 format, optional, for SMS notifications
    password_hash TEXT NOT NULL,
    salt TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    is_active INTEGER DEFAULT 1
);

-- Index for faster login queries
CREATE INDEX idx_users_username ON users(username);
```

**Purpose**: Store user account information  
**Key Fields**:
- `user_id`: Primary key, auto-increment
- `username`: Unique identifier for login (indexed)
- `phone_number`: Optional, for SMS notifications (E.164 format: +15551234567)
- `password_hash`: SHA-256 hashed password
- `salt`: Random salt for password hashing

#### Table 2: weight_entries
```sql
CREATE TABLE weight_entries (
    entry_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    weight_value REAL NOT NULL,
    unit TEXT DEFAULT 'lbs' CHECK(unit IN ('lbs', 'kg')),
    date_recorded DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Indexes for faster queries
CREATE INDEX idx_weight_entries_user_date ON weight_entries(user_id, date_recorded DESC);
CREATE INDEX idx_weight_entries_user ON weight_entries(user_id);
```

**Purpose**: Store daily weight measurements  
**Key Fields**:
- `entry_id`: Primary key
- `user_id`: Foreign key to users table
- `weight_value`: Weight measurement (positive decimal)
- `unit`: Measurement unit (lbs or kg)
- `date_recorded`: Date of measurement
- `notes`: Optional user notes

**Constraints**:
- Foreign key cascade delete (delete user → delete all their entries)
- Check constraint on unit values

#### Table 3: goal_weights
```sql
CREATE TABLE goal_weights (
    goal_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    target_weight REAL NOT NULL,
    target_date DATE,
    goal_type TEXT CHECK(goal_type IN ('lose', 'gain')),
    starting_weight REAL,
    is_active INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    achieved_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Index for faster active goal queries
CREATE INDEX idx_goal_weights_user_active ON goal_weights(user_id, is_active);
```

**Purpose**: Store user weight goals  
**Key Fields**:
- `goal_id`: Primary key
- `user_id`: Foreign key to users
- `target_weight`: Goal weight value
- `goal_type`: Direction of change (lose or gain)
- `is_active`: Boolean flag (only one active goal per user)
- `achieved_at`: Timestamp when goal was reached (NULL if not achieved)

**Business Rules**:
- Only one active goal per user (enforced by application logic)
- Previous goals set to `is_active = 0` when new goal created
- Goal marked achieved when `current_weight` reaches `target_weight`

#### Table 4: achievements
```sql
CREATE TABLE achievements (
    achievement_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    achievement_type TEXT NOT NULL CHECK(achievement_type IN ('goal', 'milestone_5', 'milestone_10', 'streak_7', 'streak_30')),
    achievement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    weight_at_achievement REAL,
    goal_id INTEGER,
    description TEXT,
    sms_sent INTEGER DEFAULT 0,  -- Track if SMS notification was sent
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (goal_id) REFERENCES goal_weights(goal_id) ON DELETE SET NULL
);

-- Index for achievement queries
CREATE INDEX idx_achievements_user_type ON achievements(user_id, achievement_type);
```

**Purpose**: Track user achievements and milestones  
**Key Fields**:
- `achievement_id`: Primary key
- `achievement_type`: Type of achievement (goal reached, milestone, streak)
- `weight_at_achievement`: Weight when achievement earned
- `sms_sent`: Flag indicating if SMS notification was sent (0 = no, 1 = yes)

**Achievement Types**:
- `goal`: User reached goal weight
- `milestone_5`: Lost/gained 5 lbs
- `milestone_10`: Lost/gained 10 lbs
- `streak_7`: 7 consecutive days of tracking
- `streak_30`: 30 consecutive days of tracking

#### Table 5: user_preferences 🆕
```sql
CREATE TABLE user_preferences (
    preference_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    preferred_unit TEXT DEFAULT 'lbs' CHECK(preferred_unit IN ('lbs', 'kg')),
    sms_goal_enabled INTEGER DEFAULT 1,  -- Send SMS for goal achievement
    sms_milestone_enabled INTEGER DEFAULT 1,  -- Send SMS for milestones (every 5 lbs)
    sms_daily_reminder_enabled INTEGER DEFAULT 0,  -- Send daily reminder SMS
    daily_reminder_time TEXT DEFAULT '08:00',  -- Time for daily reminder (HH:MM)
    notification_sound_enabled INTEGER DEFAULT 1,
    theme_preference TEXT DEFAULT 'light' CHECK(theme_preference IN ('light', 'dark', 'system')),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Index for faster preference queries
CREATE INDEX idx_user_preferences_user ON user_preferences(user_id);
```

**Purpose**: Store user-specific app preferences and SMS notification settings  
**Key Fields**:
- `preference_id`: Primary key
- `user_id`: Foreign key to users (unique - one preference record per user)
- `preferred_unit`: Default weight unit
- `sms_goal_enabled`: Enable/disable SMS for goal achievements (default ON)
- `sms_milestone_enabled`: Enable/disable SMS for milestones (default ON)
- `sms_daily_reminder_enabled`: Enable/disable daily reminder SMS (default OFF)
- `daily_reminder_time`: Time for daily reminder in HH:MM format
- `notification_sound_enabled`: Enable/disable notification sounds
- `theme_preference`: UI theme selection

**Default Behavior**:
- Goal and milestone SMS enabled by default (assuming permission granted)
- Daily reminders disabled by default (opt-in)
- Light theme by default

**Integration with SMS System**:
- System checks these preferences before sending any SMS
- If `sms_goal_enabled = 0`, no SMS sent for goals (even if permission granted)
- If `sms_milestone_enabled = 0`, no SMS sent for milestones
- If `sms_daily_reminder_enabled = 0`, no daily SMS scheduled
- Changes to preferences immediately affect SMS behavior

### Database Helper Class Structure

```java
public class WeighToGoDBHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "WeighToGo.db";
    private static final int DATABASE_VERSION = 3;  // Incremented for user_preferences table
    
    // Table names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_WEIGHT_ENTRIES = "weight_entries";
    private static final String TABLE_GOAL_WEIGHTS = "goal_weights";
    private static final String TABLE_ACHIEVEMENTS = "achievements";
    private static final String TABLE_USER_PREFERENCES = "user_preferences";  // New table
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all 5 tables
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_WEIGHT_ENTRIES_TABLE);
        db.execSQL(CREATE_GOAL_WEIGHTS_TABLE);
        db.execSQL(CREATE_ACHIEVEMENTS_TABLE);
        db.execSQL(CREATE_USER_PREFERENCES_TABLE);  // New
        
        // Create indexes
        db.execSQL(CREATE_USERS_INDEX);
        db.execSQL(CREATE_WEIGHT_ENTRIES_INDEXES);
        db.execSQL(CREATE_GOAL_WEIGHTS_INDEX);
        db.execSQL(CREATE_ACHIEVEMENTS_INDEX);
        db.execSQL(CREATE_USER_PREFERENCES_INDEX);  // New
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Add user_preferences table
            db.execSQL(CREATE_USER_PREFERENCES_TABLE);
            db.execSQL(CREATE_USER_PREFERENCES_INDEX);
            
            // Add phone_number column to users table if not exists
            db.execSQL("ALTER TABLE users ADD COLUMN phone_number TEXT");
            
            // Create default preferences for existing users
            db.execSQL("INSERT INTO user_preferences (user_id) " +
                      "SELECT user_id FROM users");
        }
    }
    
    // User Preferences Methods (New)
    public UserPreferences getUserPreferences(int userId) { }
    public long insertUserPreferences(UserPreferences prefs) { }
    public int updateUserPreferences(UserPreferences prefs) { }
    public boolean isSMSEnabledForType(int userId, String notificationType) { }
    
    // Other existing methods...
}
```

### Entity Relationship Diagram

```
users (1) ──────< (M) weight_entries
  │
  ├────────< (M) goal_weights
  │
  ├────────< (M) achievements
  │
  └────────< (1) user_preferences  [NEW - one-to-one relationship]

goal_weights (1) ──────< (M) achievements (optional)
```

**Relationships**:
- One user has many weight entries (1:M)
- One user has many goals (1:M, but only one active)
- One user has many achievements (1:M)
- One user has one user_preferences record (1:1) 🆕
- One goal can have multiple achievements (1:M, optional)

---

## User Interface Requirements

### UI-1: Login/Registration Screen

**Layout**: Centered card or full-screen with branding

**Components**:
- App header (logo/icon with tagline)
- Username input field
- Email input field (registration mode, optional)
- **Phone number input field (registration mode, optional)**
  - **Helper text: "Optional - for SMS goal notifications"**
  - **Link to "Why do we need this?" explanation**
- Password input field
- Confirm password field (registration mode only)
- "Login" or "Create Account" button (primary action)
- "Switch to Register" / "Switch to Login" link/button
- Error message display area (hidden by default)

**Design Considerations**:
- Use Material Design 3 guidelines
- Clear visual hierarchy with app branding
- Keyboard-friendly input flow
- Error messages in red, clearly visible
- **Phone number field with country code selector (+1 for US)**
- **SMS explanation tooltip/dialog accessible from phone field**
- Focus on simplicity and clarity

**SMS-Related UI Elements** 🆕:
- Phone number input validates E.164 format (+15551234567)
- Optional helper text: "We'll send you texts when you hit goals!"
- Info icon that opens dialog explaining SMS features
- Registration doesn't require phone number (can add later in settings)

### UI-2: Main Dashboard Screen

**Layout**: Central hub displaying weight history and quick actions

**Components**:
- Header with app title and user greeting
- Current weight display (latest entry)
- Goal progress indicator (visual progress bar)
- "Add Weight" FAB button (prominent, easy to access)
- Weight history grid/list (scrollable)
  - Date column
  - Weight column
  - Trend indicators (up/down from previous entry)
  - **Delete button on each row (REQUIRED for Project Three)**
  - Edit button on each row
- Current goal weight display
- Menu button (for logout, settings, **SMS notifications**)

**Design Considerations**:
- Most recent entries at top
- Clear date formatting (e.g., "Today", "Yesterday", "Nov 8, 2025")
- Visual feedback for trends (up/down arrows with color coding)
- Empty state message if no entries
- **Quick access to SMS notification settings from menu**

### UI-3: Weight Entry Screen/Dialog

**Layout**: Modal dialog or separate screen

**Components**:
- Weight input field (numeric keyboard)
- Unit display/toggle (lbs or kg)
- Date picker (default to today)
- Notes input field (optional, multiline)
- "Save" button
- "Cancel" button

**Design Considerations**:
- Quick entry focus (minimal steps)
- Clear labels and hints
- Validation feedback (real-time)
- Success confirmation (toast message)
- **Trigger achievement check on save (may send SMS)**

### UI-4: Goal Setting Screen/Dialog

**Layout**: Modal dialog or separate screen

**Components**:
- Current goal display (if exists)
- Goal weight input field (numeric keyboard)
- Unit display/toggle (lbs or kg)
- Target date picker (optional)
- Goal type selector (lose weight / gain weight)
- "Set Goal" or "Update Goal" button
- "Cancel" button

**Design Considerations**:
- Clear visual distinction between lose/gain goals
- Motivational messaging
- Validation feedback
- Success confirmation

### UI-5: SMS Notification Settings Screen 🆕

**Priority**: Critical (Required for Project Three - 20% of grade)

**Layout**: Dedicated settings screen accessible from main menu

**Components Required**:

#### Permission Status Section
- **Permission Status Card** (top of screen)
  - Large icon (checkmark if granted, warning if denied)
  - Status text: "SMS Notifications Enabled" or "SMS Notifications Disabled"
  - Color coding (green for granted, red for denied, orange for not determined)

- **Enable SMS Button** (if permission not granted)
  - Primary action button
  - Text: "Enable SMS Notifications"
  - Opens system permission dialog

- **Permission Denied Warning Card** (if denied)
  - Warning icon
  - Explanation text: "SMS notifications are disabled. The app will continue to work, but you won't receive text messages for achievements."
  - "Open App Settings" button (if permanently denied)
  - Link to explain how to enable in system settings

#### Notification Preferences Section
- **Phone Number Display/Edit**
  - Shows current phone number (if set)
  - Edit icon to modify
  - Validation for E.164 format

- **Notification Type Toggles** (SwitchMaterial)
  - "Goal Achievement Notifications"
    - Subtitle: "Get texted when you reach your goal"
    - Enabled only if SMS permission granted
  - "Milestone Notifications"
    - Subtitle: "Celebrate every 5 lbs of progress"
    - Enabled only if SMS permission granted
  - "Daily Reminders"
    - Subtitle: "Daily text to log your weight"
    - Time picker appears when enabled
    - Enabled only if SMS permission granted

- **Daily Reminder Time Picker** (if enabled)
  - Time selector (AM/PM or 24-hour based on device settings)
  - Default: 8:00 AM

- **Save Preferences Button**
  - Primary action button at bottom
  - Saves all changes to database

#### Visual States
1. **Permission Granted State**:
   - Green checkmark icon
   - All toggles enabled
   - "Enable SMS" button hidden
   - Warning card hidden

2. **Permission Denied State**:
   - Red warning icon
   - All toggles disabled (grayed out)
   - "Enable SMS" button visible
   - Warning card visible

3. **Permission Permanently Denied State**:
   - Red warning icon
   - All toggles disabled
   - "Enable SMS" button hidden
   - Warning card visible with "Open App Settings" button

**Design Considerations**:
- Clear visual hierarchy (permission status most prominent)
- Immediate feedback on permission changes
- Graceful degradation (show what's unavailable and why)
- No confusing state (never show enabled toggles if permission denied)
- Help text explaining each notification type
- Accessibility considerations (switch labels, contrast ratios)

**User Flow Example**:
```
1. User opens Settings from main menu
2. Sees "SMS Notifications Disabled" with red warning
3. Clicks "Enable SMS Notifications" button
4. System shows permission dialog
5. User grants permission
6. UI updates immediately:
   - Status changes to "SMS Notifications Enabled" (green)
   - All toggles become enabled
   - Warning card disappears
7. User configures which notifications to receive
8. Clicks "Save Preferences"
9. Toast confirms: "Preferences saved"
```

**Error Handling**:
- Invalid phone number → Show inline error, prevent save
- Permission denied → Show clear explanation, don't show error
- Save failure → Show error toast, retry option

**Integration Points**:
- Links from main dashboard menu: "SMS Settings"
- Accessible from top-level settings (if more settings added later)
- Context help icons for each toggle
- Link to privacy policy regarding SMS usage

---

## Technical Implementation Details

### Tech-1: Password Hashing

**Algorithm**: SHA-256 with salt

**Implementation**:
```java
public class PasswordUtils {
    
    /**
     * Generates random salt for password hashing
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.DEFAULT);
    }
    
    /**
     * Hashes password with salt using SHA-256
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes());
            byte[] hash = digest.digest(password.getBytes());
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Verifies password against stored hash and salt
     */
    public static boolean verifyPassword(String password, String storedHash, String salt) {
        String hashedAttempt = hashPassword(password, salt);
        return hashedAttempt.equals(storedHash);
    }
}
```

### Tech-2: Date Handling

**Format**: ISO 8601 (YYYY-MM-DD) for database storage

**Implementation**:
```java
public class DateUtils {
    
    private static final SimpleDateFormat DB_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    
    private static final SimpleDateFormat DISPLAY_FORMAT = 
        new SimpleDateFormat("MMM d, yyyy", Locale.US);
    
    /**
     * Converts Date object to database format string
     */
    public static String formatForDatabase(Date date) {
        return DB_FORMAT.format(date);
    }
    
    /**
     * Converts Date object to user-friendly display string
     */
    public static String formatForDisplay(Date date) {
        // Handle today/yesterday
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        
        Calendar inputDate = Calendar.getInstance();
        inputDate.setTime(date);
        
        if (isSameDay(inputDate, today)) {
            return "Today";
        } else if (isSameDay(inputDate, yesterday)) {
            return "Yesterday";
        } else {
            return DISPLAY_FORMAT.format(date);
        }
    }
    
    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
```

### Tech-3: Achievement Detection and SMS Notification

**Trigger Points**: After saving new weight entry

**Implementation**:
```java
public class AchievementManager {
    
    private WeighToGoDBHelper dbHelper;
    private SMSNotificationManager smsManager;  // New
    private Context context;
    
    /**
     * Checks for achievements after weight entry saved
     * Triggers SMS notifications if appropriate
     */
    public void checkAchievements(int userId, double newWeight) {
        // Get current goal
        GoalWeight goal = dbHelper.getActiveGoal(userId);
        if (goal == null) return;
        
        // 1. Check goal achievement
        if (isGoalAchieved(newWeight, goal)) {
            recordAchievement(userId, "goal", newWeight, goal.getGoalId());
            showGoalAchievedDialog();
            
            // Send SMS if permission granted and enabled
            smsManager.sendGoalAchievedSMS(userId, goal.getTargetWeight());
        }
        
        // 2. Check milestone achievements (every 5 lbs)
        double startingWeight = goal.getStartingWeight();
        double totalChange = Math.abs(newWeight - startingWeight);
        int milestonesPassed = (int)(totalChange / 5);
        
        // Check if this is a new milestone
        int previousMilestones = dbHelper.getMilestoneCount(userId);
        if (milestonesPassed > previousMilestones) {
            recordAchievement(userId, "milestone_5", newWeight, goal.getGoalId());
            showMilestoneToast(totalChange);
            
            // Send SMS if permission granted and enabled
            smsManager.sendMilestoneSMS(userId, totalChange);
        }
    }
    
    private boolean isGoalAchieved(double currentWeight, GoalWeight goal) {
        if (goal.getGoalType().equals("lose")) {
            return currentWeight <= goal.getTargetWeight();
        } else {
            return currentWeight >= goal.getTargetWeight();
        }
    }
}
```

**SMS Notification Manager** 🆕:
```java
public class SMSNotificationManager {
    
    private Context context;
    private WeighToGoDBHelper dbHelper;
    
    /**
     * Sends SMS for goal achievement if conditions met
     * Conditions: Permission granted AND user enabled goal SMS
     */
    public void sendGoalAchievedSMS(int userId, double goalWeight) {
        // Check permission
        if (!hasSMSPermission()) {
            Log.i("SMS", "No SMS permission - using in-app notification only");
            return;
        }
        
        // Check user preferences
        UserPreferences prefs = dbHelper.getUserPreferences(userId);
        if (!prefs.isSmsGoalEnabled()) {
            Log.i("SMS", "Goal SMS disabled by user");
            return;
        }
        
        // Get phone number
        String phoneNumber = getPhoneNumber(userId);
        if (phoneNumber == null) {
            Log.w("SMS", "No phone number configured");
            return;
        }
        
        // Build message
        String message = String.format(
            "🎉 Congratulations! You've reached your goal weight of %.1f lbs! " +
            "Great work staying committed to your health journey. - Weigh to Go!",
            goalWeight
        );
        
        // Send SMS
        sendSMS(phoneNumber, message);
        
        // Record that SMS was sent
        dbHelper.markSMSSent(userId, "goal");
    }
    
    /**
     * Sends SMS for milestone if conditions met
     */
    public void sendMilestoneSMS(int userId, double totalLost) {
        if (!hasSMSPermission()) return;
        
        UserPreferences prefs = dbHelper.getUserPreferences(userId);
        if (!prefs.isSm sMilestoneEnabled()) return;
        
        String phoneNumber = getPhoneNumber(userId);
        if (phoneNumber == null) return;
        
        String message = String.format(
            "💪 Way to go! You've lost %.1f lbs so far. Keep up the great work! - Weigh to Go!",
            totalLost
        );
        
        sendSMS(phoneNumber, message);
        dbHelper.markSMSSent(userId, "milestone");
    }
    
    /**
     * Core SMS sending method
     */
    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.i("SMS", "Message sent successfully");
        } catch (Exception e) {
            Log.e("SMS", "Failed to send SMS", e);
            Toast.makeText(context, "Failed to send SMS notification", 
                Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Checks if app has SMS permission
     */
    private boolean hasSMSPermission() {
        return ContextCompat.checkSelfPermission(context, 
            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Gets user's phone number from database
     */
    private String getPhoneNumber(int userId) {
        User user = dbHelper.getUser(userId);
        return user != null ? user.getPhoneNumber() : null;
    }
}
```

### Tech-4: Input Validation

#### Validation Rules
- **Username**: 
  - Not empty
  - Length: 3-20 characters
  - Alphanumeric characters only
  - Must be unique in database
  
- **Password**: 
  - Not empty
  - Minimum 8 characters
  - At least one uppercase letter
  - At least one lowercase letter
  - At least one digit
  - Must match confirmation password (during registration)
  
- **Phone Number** 🆕:
  - Optional field
  - If provided, must be valid E.164 format
  - Example: +15551234567
  - Max 15 digits after country code
  - Validates using regex: `^\\+[1-9]\\d{1,14}$`
  
- **Weight Value**:
  - Not empty
  - Positive number
  - Reasonable range: 1-1000 lbs or 0.5-500 kg
  - Maximum 2 decimal places
  
- **Date**:
  - Valid date format
  - Not in the future
  - Not before reasonable start date (e.g., not before 1900)

**Phone Number Validation** 🆕:
```java
public class PhoneValidator {
    
    // E.164 international format regex
    private static final String E164_PATTERN = "^\\+[1-9]\\d{1,14}$";
    
    /**
     * Validates phone number in E.164 format
     * @param phoneNumber Phone number to validate
     * @return true if valid E.164 format
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return phoneNumber.matches(E164_PATTERN);
    }
    
    /**
     * Formats US phone number to E.164 format
     * @param phoneNumber 10-digit US phone number
     * @return E.164 formatted number (+1XXXXXXXXXX)
     */
    public static String formatUSPhoneNumber(String phoneNumber) {
        // Remove all non-digit characters
        String digits = phoneNumber.replaceAll("\\D", "");
        
        // If 10 digits (US number without country code)
        if (digits.length() == 10) {
            return "+1" + digits;
        }
        
        // If 11 digits starting with 1 (US number with country code)
        if (digits.length() == 11 && digits.startsWith("1")) {
            return "+" + digits;
        }
        
        // If already has +, return as-is
        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }
        
        return null; // Invalid format
    }
}
```

### Tech-5: Data Persistence

#### SharedPreferences (for app settings and session)
```java
// Store user session
SharedPreferences prefs = getSharedPreferences("WeighToGoPrefs", MODE_PRIVATE);
prefs.edit()
    .putInt("current_user_id", userId)
    .putString("username", username)
    .putBoolean("is_logged_in", true)
    .apply();

// Note: User-specific preferences now stored in user_preferences table
// SharedPreferences used only for session management
```

#### SQLite Database (for user data)
- All user, weight, goal, achievement, and **preference** data stored in SQLite
- Database persists across app restarts
- Foreign key constraints enforced
- Cascading deletes for data integrity

---

## Security & Privacy Requirements

### SEC-1: Password Security
**Priority**: Critical

**Requirements**:
- Never store passwords in plain text
- Use SHA-256 hashing with random salt
- Generate new salt for each user
- Store both hash and salt in database
- Use constant-time comparison for hash verification (prevent timing attacks)

**Implementation**: See Tech-1 PasswordUtils class

### SEC-2: User Data Isolation
**Priority**: Critical

**Requirements**:
- Each user can only access their own data
- All database queries filter by `user_id`
- Session management prevents unauthorized access
- User cannot view/modify another user's entries

**Implementation**:
```java
// Always include user_id in WHERE clause
public List<WeightEntry> getAllWeightEntries(int userId) {
    String query = "SELECT * FROM weight_entries WHERE user_id = ? ORDER BY date_recorded DESC";
    // ... query execution
}
```

### SEC-3: Input Sanitization
**Priority**: High

**Requirements**:
- Validate all user input before processing
- Sanitize inputs to prevent SQL injection
- Use parameterized queries exclusively
- Validate data types and ranges
- **Validate phone number format for SMS**

**Implementation**:
```java
// Always use parameterized queries
String sql = "INSERT INTO users (username, password_hash, salt, phone_number) VALUES (?, ?, ?, ?)";
SQLiteStatement statement = db.compileStatement(sql);
statement.bindString(1, username);
statement.bindString(2, passwordHash);
statement.bindString(3, salt);
statement.bindString(4, phoneNumber);  // Already validated
statement.executeInsert();
```

### SEC-4: Session Management
**Priority**: High

**Requirements**:
- Clear session data on logout
- Implement session timeout (optional, for enhanced security)
- Validate session before each protected operation
- Prevent session hijacking

**Implementation**:
```java
public boolean isUserLoggedIn() {
    SharedPreferences prefs = getSharedPreferences("WeighToGoPrefs", MODE_PRIVATE);
    return prefs.getBoolean("is_logged_in", false);
}

public void logout() {
    SharedPreferences prefs = getSharedPreferences("WeighToGoPrefs", MODE_PRIVATE);
    prefs.edit()
        .clear()  // Clear all session data
        .apply();
}
```

### SEC-5: SMS Permission Security 🆕
**Priority**: Critical (Required for Project Three)

**Requirements**:
- **Never request SMS permission on first app launch** (bad UX, violates Android guidelines)
- Request permission only when user accesses SMS settings
- Show rationale dialog explaining why permission needed (if user previously denied)
- Handle all permission states (granted, denied, permanently denied)
- **Never crash if permission denied** - app must function fully without SMS
- Never repeatedly prompt for permission (respect user choice)
- Provide "Open App Settings" option if user permanently denied
- Log all permission requests and responses for debugging
- **Never send SMS without explicit permission granted**
- Check permission status before every SMS send attempt
- Handle SmsManager exceptions gracefully

**Permission Request Flow**:
```java
// 1. User navigates to SMS settings
// 2. Check current permission status
private void checkPermissionStatus() {
    boolean hasPermission = ContextCompat.checkSelfPermission(this, 
        Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    
    updatePermissionUI(hasPermission);
}

// 3. User clicks "Enable SMS" button
// 4. Check if we should show rationale
private void requestSmsPermission() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, 
            Manifest.permission.SEND_SMS)) {
        // User previously denied - show explanation first
        showPermissionRationaleDialog();
    } else {
        // First time requesting or user hasn't seen rationale yet
        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
    }
}

// 5. Show rationale dialog
private void showPermissionRationaleDialog() {
    new AlertDialog.Builder(this)
        .setTitle("SMS Notifications")
        .setMessage("Weigh to Go would like to send you text messages when you reach " +
                   "your weight goals and milestones. This helps keep you motivated!\n\n" +
                   "The app works fine without this permission if you prefer not to enable it.")
        .setPositiveButton("Enable", (dialog, which) -> {
            requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        })
        .setNegativeButton("No Thanks", null)
        .show();
}

// 6. Handle permission result
private final ActivityResultLauncher<String> requestPermissionLauncher =
    registerForActivityResult(new ActivityResultContracts.RequestPermission(), 
        isGranted -> {
            if (isGranted) {
                // Permission granted
                updatePermissionUI(true);
                Toast.makeText(this, "SMS notifications enabled!", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                updatePermissionUI(false);
                
                // Check if permanently denied
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, 
                        Manifest.permission.SEND_SMS)) {
                    // Permanently denied - show "Open Settings" option
                    showPermanentlyDeniedWarning();
                }
            }
        }
    );
```

**Handling Permanently Denied Permission**:
```java
private void showPermanentlyDeniedWarning() {
    permissionDeniedCard.setVisibility(View.VISIBLE);
    openSettingsButton.setVisibility(View.VISIBLE);
    requestPermissionButton.setVisibility(View.GONE);
    
    openSettingsButton.setOnClickListener(v -> {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    });
}
```

**Security Best Practices**:
- Always check permission before sending SMS
- Never assume permission persists (user can revoke in system settings)
- Log permission checks and SMS send attempts
- Handle security exceptions from SmsManager
- Validate phone number before attempting SMS
- Respect user's notification preferences even if permission granted
- Never send SMS if user disabled that notification type
- Implement rate limiting to prevent SMS spam (e.g., max 10 SMS per day)

### SEC-6: Phone Number Privacy 🆕
**Priority**: High

**Requirements**:
- Phone numbers stored encrypted at rest (future enhancement)
- Phone numbers never logged in plain text
- Phone numbers never transmitted over network (local app only)
- User can delete phone number from database
- Phone number visible only to authenticated user
- No phone number sharing with third parties

**Implementation Notes**:
```java
// When logging, mask phone number
Log.i("SMS", "Sending SMS to: " + maskPhoneNumber(phoneNumber));

private String maskPhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.length() < 4) {
        return "***";
    }
    // Show only last 4 digits
    return "***" + phoneNumber.substring(phoneNumber.length() - 4);
}
```

---

## User Experience Requirements

### UX-1: Onboarding Experience
**Priority**: High

**Requirements**:
- Clear value proposition on login screen
- Simple registration process (3 fields minimum)
- Optional phone number collection with clear SMS explanation
- Immediate access after registration (auto-login)
- No SMS permission request during onboarding (wait until user accesses settings)
- Empty state guidance on first login ("Add your first weight entry!")

**Success Criteria**:
- User completes registration in < 2 minutes
- User understands app purpose immediately
- User knows how to add first weight entry

### UX-2: Data Entry Efficiency
**Priority**: Critical

**Requirements**:
- Weight entry accessible via prominent FAB button
- Numeric keyboard for weight input
- Date defaults to today (most common use case)
- Minimal required fields (weight only)
- Save action confirms with brief toast
- No unnecessary confirmation dialogs

**Success Criteria**:
- User can add weight entry in < 15 seconds
- User can complete entry with < 5 taps

### UX-3: Progress Visibility
**Priority**: High

**Requirements**:
- Visual progress indicator always visible on dashboard
- Clear display of: current weight, goal weight, progress percentage
- Trend indicators on weight history (up/down arrows)
- Color coding for positive/negative trends
- Celebration moments for achievements (dialogs, animations)
- SMS notifications for major milestones (if enabled)

**Success Criteria**:
- User understands current progress at a glance
- User feels motivated by visual feedback

### UX-4: Error Handling
**Priority**: High

**Requirements**:
- Clear, user-friendly error messages (no technical jargon)
- Inline validation with immediate feedback
- Helpful suggestions for fixing errors
- No app crashes for any user input
- Graceful handling of permission denials
- SMS fallback to in-app notifications

**Examples**:
- ❌ "SQLException: UNIQUE constraint failed"
- ✅ "This username is already taken. Please try another."

- ❌ "NullPointerException in validatePassword()"
- ✅ "Please enter a password."

- ❌ "Permission denied: SEND_SMS"
- ✅ "SMS notifications are disabled. You'll see in-app notifications instead!"

### UX-5: Accessibility
**Priority**: Medium

**Requirements**:
- All interactive elements have content descriptions
- Minimum touch target size: 48dp x 48dp
- Sufficient color contrast (WCAG AA minimum)
- Support for system font size scaling
- Keyboard navigation support
- Screen reader compatibility

**Implementation**:
```xml
<!-- Content descriptions for accessibility -->
<ImageButton
    android:id="@+id/add_weight_fab"
    android:contentDescription="Add new weight entry"
    ... />

<!-- Minimum touch target -->
<Button
    android:layout_width="wrap_content"
    android:layout_height="48dp"
    android:minHeight="48dp"
    ... />
```

### UX-6: SMS Notification Experience 🆕
**Priority**: High

**Requirements**:
- Clear explanation of SMS feature during registration
- Easy access to SMS settings from main menu
- Visual indication of SMS permission status (icon + text)
- Immediate UI feedback when permission granted/denied
- Clear explanation of what works without SMS permission
- No confusing "enable/disable" states when permission not granted
- SMS messages use friendly, motivational tone
- SMS messages include app name for context
- Option to disable SMS even after granting permission
- Individual control over SMS notification types

**SMS Message Requirements**:
- Use emojis for visual appeal (🎉, 💪, ⚖️)
- Keep messages under 160 characters when possible
- Include app name: "- Weigh to Go!"
- Use encouraging, positive language
- Personalize with user's data (weight values)
- Avoid medical claims or advice

**Success Criteria**:
- User understands SMS feature and how to enable it
- User can easily disable unwanted SMS types
- SMS messages feel personal and motivating
- App remains fully functional if SMS disabled

---

## Testing Requirements

### Test-1: Unit Tests
**Priority**: High

**Test Coverage**:
- Password hashing and verification
- Date formatting and parsing
- Input validation (all fields)
- Weight difference calculations
- Progress percentage calculations
- Achievement detection logic
- **Phone number validation (E.164 format)** 🆕
- **SMS permission status checks** 🆕

**Test Framework**: JUnit 4

**Example Tests**:
```java
@Test
public void testPasswordHashing() {
    String password = "Test123!";
    String salt = PasswordUtils.generateSalt();
    String hash = PasswordUtils.hashPassword(password, salt);
    
    assertTrue(PasswordUtils.verifyPassword(password, hash, salt));
    assertFalse(PasswordUtils.verifyPassword("WrongPass", hash, salt));
}

@Test
public void testPhoneNumberValidation() {
    // Valid E.164 formats
    assertTrue(PhoneValidator.isValidPhoneNumber("+15551234567"));
    assertTrue(PhoneValidator.isValidPhoneNumber("+442071234567"));
    
    // Invalid formats
    assertFalse(PhoneValidator.isValidPhoneNumber("5551234567"));
    assertFalse(PhoneValidator.isValidPhoneNumber("555-123-4567"));
    assertFalse(PhoneValidator.isValidPhoneNumber("+1555123456789012345"));
}
```

### Test-2: Integration Tests
**Priority**: Medium

**Test Scenarios**:
- End-to-end user registration and login flow
- Complete weight entry workflow (add, view, edit, delete)
- Goal setting and progress tracking
- Achievement detection and notification
- **SMS permission request and handling** 🆕
- **SMS sending with valid permissions** 🆕
- **App functionality without SMS permission** 🆕
- Database CRUD operations
- Multi-user data isolation

**Test Framework**: Espresso

**Example Tests**:
```java
@Test
public void testSMSPermissionFlow() {
    // Navigate to SMS settings
    onView(withId(R.id.menu_settings)).perform(click());
    onView(withText("SMS Notifications")).perform(click());
    
    // Verify permission status displayed
    onView(withId(R.id.permission_status_text)).check(matches(isDisplayed()));
    
    // Click enable button
    onView(withId(R.id.request_permission_button)).perform(click());
    
    // Verify system dialog shown (can't test beyond this point in Espresso)
}

@Test
public void testAppWorksWithoutSMS() {
    // Ensure SMS permission denied
    // Add weight entry
    onView(withId(R.id.add_weight_fab)).perform(click());
    onView(withId(R.id.weight_input)).perform(typeText("150.5"));
    onView(withId(R.id.save_button)).perform(click());
    
    // Verify entry saved and displayed
    onView(withText("150.5")).check(matches(isDisplayed()));
    
    // Verify no crash occurred
    onView(withId(R.id.weight_recycler_view)).check(matches(isDisplayed()));
}
```

### Test-3: UI Tests
**Priority**: Medium

**Test Cases**:
- All screens render correctly on different screen sizes
- Input fields validate and show errors appropriately
- Buttons respond to clicks
- Lists scroll smoothly
- Dialogs display and dismiss correctly
- Empty states show when no data
- **SMS settings screen displays all required components** 🆕
- **Permission states render correctly** 🆕
- **Toggles disabled when permission not granted** 🆕

**Test Devices**:
- Small screen (480x800)
- Normal screen (720x1280)
- Large screen (1080x1920)

### Test-4: SMS Functionality Tests 🆕
**Priority**: Critical (Required for Project Three)

**Manual Test Cases**:

1. **SMS Permission Request Test**:
   - Open app, navigate to SMS settings
   - Verify permission status shows "Disabled"
   - Tap "Enable SMS" button
   - Grant permission in system dialog
   - Verify permission status shows "Enabled"
   - Verify all toggles become enabled

2. **SMS Permission Denial Test**:
   - Open app, navigate to SMS settings
   - Tap "Enable SMS" button
   - Deny permission in system dialog
   - Verify permission status shows "Disabled"
   - Verify warning card appears
   - Verify toggles remain disabled
   - Verify app doesn't crash

3. **SMS Sending Test** (requires physical device with SIM):
   - Grant SMS permission
   - Enable goal achievement notifications
   - Enter phone number in settings
   - Add weight entries until goal reached
   - Verify SMS received on phone
   - Verify SMS contains correct weight value
   - Verify SMS includes app name

4. **SMS Disabled Test**:
   - Grant SMS permission
   - Disable goal achievement notifications
   - Add weight entries until goal reached
   - Verify NO SMS received
   - Verify in-app notification still shows

5. **No Phone Number Test**:
   - Grant SMS permission
   - Enable notifications
   - Leave phone number empty
   - Add weight entry triggering notification
   - Verify no crash
   - Verify in-app notification shows
   - Verify error logged (not SMS sent)

6. **Permanently Denied Permission Test**:
   - Deny SMS permission and select "Don't ask again"
   - Navigate to SMS settings
   - Verify "Enable SMS" button hidden
   - Verify "Open App Settings" button visible
   - Tap "Open App Settings"
   - Verify Android settings app opens to correct screen

**Automated Test Scenarios** (if using Espresso/UI Automator):
```java
@Test
public void testSMSSettingsUI() {
    // Navigate to SMS settings
    onView(withId(R.id.menu_settings)).perform(click());
    
    // Verify all required UI elements present
    onView(withId(R.id.permission_status_card)).check(matches(isDisplayed()));
    onView(withId(R.id.permission_status_icon)).check(matches(isDisplayed()));
    onView(withId(R.id.permission_status_text)).check(matches(isDisplayed()));
    onView(withId(R.id.goal_achievement_switch)).check(matches(isDisplayed()));
    onView(withId(R.id.milestone_switch)).check(matches(isDisplayed()));
    onView(withId(R.id.daily_reminder_switch)).check(matches(isDisplayed()));
}
```

### Test-5: Database Tests
**Priority**: High

**Test Cases**:
- User insertion with phone number
- User preferences creation with defaults
- SMS notification preferences update
- Achievement recording with SMS sent flag
- Foreign key cascade delete
- Concurrent access handling
- Data integrity constraints

---

## Future Enhancements

### Phase 2 Features (Post-CS360)
- Weight trend charts and graphs
- Photo progress tracking
- Export data to CSV
- Multiple goal tracking
- Custom reminder times
- BMI calculator
- **Push notifications as alternative to SMS**
- **Weekly progress summary SMS (if enabled)**

### Phase 3 Features
- Cloud backup and sync
- Multi-device support
- Social features (optional sharing)
- Integration with fitness trackers
- Meal planning integration
- **Scheduled SMS reports (weekly summaries)**
- **SMS upgrade to multimedia (MMS) for progress photos**

### Technical Improvements
- Migration to Room persistence library
- Kotlin conversion
- MVVM architecture refactoring
- Jetpack Compose UI migration
- Biometric authentication
- Dark theme support
- **End-to-end encryption for phone numbers**
- **SMS rate limiting and analytics**

---

## Development Checklist

### Sprint 1: Foundation (Project One - Complete)
- [x] Project proposal document
- [x] User needs analysis
- [x] Competitive analysis
- [x] Feature specification
- [x] Technology selection

### Sprint 2: UI Design (Project Two - Complete)
- [x] Login/Registration screen layout
- [x] Main Dashboard screen layout
- [x] Weight Entry screen/dialog layout
- [x] Goal Setting screen/dialog layout
- [x] Material Design 3 components
- [x] Color scheme and branding
- [x] Accessibility considerations
- [x] XML layouts for all screens

### Sprint 3: Database & Authentication (Project Three)
- [ ] Create database schema (5 tables including user_preferences)
- [ ] Implement WeighToGoDBHelper class
- [ ] Implement PasswordUtils class
- [ ] Create User, WeightEntry, GoalWeight, Achievement, UserPreferences model classes
- [ ] Implement login functionality
- [ ] Implement registration functionality
- [ ] Implement session management
- [ ] Test user authentication flow
- [ ] Test data isolation between users

### Sprint 4: Core Functionality (Project Three)
- [ ] Implement add weight entry functionality
- [ ] Implement view weight history functionality
- [ ] Implement edit weight entry functionality
- [ ] **Implement delete weight entry functionality (critical for rubric)**
- [ ] Implement goal setting functionality
- [ ] Implement progress tracking and display
- [ ] Test all CRUD operations
- [ ] Test weight trend calculations

### Sprint 5: SMS Notifications (Project Three) 🆕
- [ ] Add SMS permission to AndroidManifest.xml
- [ ] Add telephony feature to AndroidManifest.xml (required=false)
- [ ] Create SMS Settings screen layout (UI-5)
- [ ] Implement permission status display
- [ ] Implement permission request flow (ActivityResultContracts)
- [ ] Implement permission rationale dialog
- [ ] Implement "Open App Settings" for permanently denied
- [ ] Create SMSNotificationManager class
- [ ] Implement SMS sending for goal achievement
- [ ] Implement SMS sending for milestones
- [ ] Implement daily reminder scheduling (optional)
- [ ] Add phone number input/validation
- [ ] Create user_preferences table and DAO
- [ ] Implement notification type toggles
- [ ] Test permission request on emulator
- [ ] Test SMS sending on physical device
- [ ] Test app functionality without permission
- [ ] Test all SMS permission states (granted, denied, permanently denied)
- [ ] Verify no crashes when permission denied
- [ ] Verify SMS not sent when permission denied
- [ ] Verify SMS respects user preferences even when permission granted

### Sprint 6: Achievements & Polish (Project Three)
- [ ] Implement achievement detection logic
- [ ] Implement goal achieved celebration
- [ ] Implement milestone notifications
- [ ] Create achievement history screen (optional)
- [ ] Polish UI transitions and animations
- [ ] Add loading states and progress indicators
- [ ] Implement error handling throughout app
- [ ] Add input validation feedback
- [ ] Test edge cases and error scenarios

### Sprint 7: Testing & Debugging (Project Three)
- [ ] Write unit tests for PasswordUtils
- [ ] Write unit tests for PhoneValidator 🆕
- [ ] Write unit tests for DateUtils
- [ ] Write unit tests for achievement logic
- [ ] Write integration tests for authentication
- [ ] Write integration tests for weight entry workflow
- [ ] Write integration tests for SMS functionality 🆕
- [ ] Write UI tests for all screens
- [ ] Test on multiple screen sizes
- [ ] Test with large datasets (50+ entries)
- [ ] Test SMS on physical device 🆕
- [ ] Fix all identified bugs
- [ ] Performance testing and optimization

### Sprint 8: Documentation & Launch Plan (Project Three)
- [ ] Write app description for store listing
- [ ] Design app icon
- [ ] Create screenshots for all key screens
- [ ] Document Android version compatibility
- [ ] Document all requested permissions (including SEND_SMS)
- [ ] Write permission justifications
- [ ] Develop monetization strategy
- [ ] Create revenue projections
- [ ] Write complete 2-3 page launch plan document
- [ ] Proofread and format launch plan
- [ ] Generate signed APK (optional)
- [ ] Create project ZIP file for submission

### Sprint 9: Submission (Project Three)
- [ ] Final code review
- [ ] Clean project (remove unused resources)
- [ ] Verify all features functional
- [ ] Verify delete button works on grid rows
- [ ] Verify SMS permission flow works correctly
- [ ] Verify app works without SMS permission
- [ ] Test on emulator and physical device (if available)
- [ ] Create submission ZIP file
- [ ] Submit ZIP file and launch plan document
- [ ] Submit before deadline

---

## Development Best Practices

### Code Organization
```
com.rickgoshen.weightogo/
├── activities/
│   ├── LoginActivity.java
│   ├── MainActivity.java
│   ├── WeightEntryActivity.java
│   ├── GoalSettingActivity.java
│   └── SMSSettingsActivity.java  // New for Project Three
├── adapters/
│   └── WeightEntryAdapter.java
├── database/
│   ├── WeighToGoDBHelper.java
│   └── models/
│       ├── User.java
│       ├── WeightEntry.java
│       ├── GoalWeight.java
│       ├── Achievement.java
│       └── UserPreferences.java  // New for Project Three
├── utils/
│   ├── PasswordUtils.java
│   ├── DateUtils.java
│   ├── PhoneValidator.java  // New for Project Three
│   └── AchievementManager.java
└── notifications/
    └── SMSNotificationManager.java  // New for Project Three
```

### Naming Conventions
- Activities: `[Feature]Activity.java` (e.g., `LoginActivity.java`)
- Layouts: `activity_[feature].xml` (e.g., `activity_login.xml`)
- IDs: `[component]_[description]` (e.g., `button_sign_in`)
- Strings: `[screen]_[element]_[description]` (e.g., `login_button_sign_in`)
- Database columns: `snake_case` (e.g., `user_id`, `password_hash`)
- Java methods: `camelCase` verbs (e.g., `validatePassword()`)
- Java classes: `PascalCase` nouns (e.g., `PasswordUtils`)

### Git Commit Messages
Follow conventional commit format:
```
feat: Add SMS notification system
feat: Add user preferences storage
feat: Add achievement tracking

fix: Correct phone number validation
fix: Handle SMS permission denial

docs: Update requirements with SMS features
docs: Add database schema documentation

test: Add unit tests for SMS utilities
test: Add integration tests for notifications

refactor: Reorganize notification classes
refactor: Extract SMS logic to utility class

style: Format code per Java conventions

chore: Update dependencies
chore: Configure build settings
```

---

## Document Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | Nov 2025 | Rick Goshen | Initial comprehensive requirements document |
| 2.0 | Nov 2025 | Rick Goshen | Added SMS notification mentions in feature list |
| 3.0 | Dec 2025 | Rick Goshen | **Major update**: Added complete FR-5 SMS Notification System, UI-5 SMS Settings Screen, SEC-5 SMS Permission Security, SEC-6 Phone Number Privacy, updated database schema with user_preferences table, added phone_number field to users table, expanded testing requirements with SMS tests, updated development checklist with SMS tasks, added SMS-specific implementation details and code examples |

---

**End of Requirements Document**

*This document will be updated throughout the development process as requirements are refined and features are implemented.*
