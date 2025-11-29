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
**Last Updated**: November 2025  
**Version**: 2.0

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
Provide users with a simple, effective tool for monitoring daily weight and progress toward personal health goals through daily tracking, visual progress display, goal-oriented notifications, and optional SMS alerts for milestone achievements.

### Target Users
1. **Weight-Loss Seekers**: Individuals trying to lose weight for health or personal reasons
2. **Health Maintenance Monitors**: Users maintaining current weight or monitoring for medical purposes
3. **Family Health Managers**: Parents/caregivers tracking weight for multiple family members

### Core Value Proposition
Streamlined weight tracking without overwhelming complexity - focusing on essential features: secure login, daily weight entry, historical data display, goal setting, achievement notifications, and SMS alerts for milestone celebrations.

### Key Features
- **Secure Authentication**: User registration and login with hashed passwords
- **Daily Weight Tracking**: Simple weight entry with date selection
- **Weight History**: Scrollable grid view of all entries
- **Goal Management**: Set and track target weight goals
- **Achievement System**: Celebrate milestones and goal completions
- **Dual Notification System**: In-app notifications plus optional SMS alerts

---

## Functional Requirements

### FR-1: User Authentication System

#### FR-1.1: User Registration
**Priority**: Critical  
**Description**: Allow new users to create an account

**Acceptance Criteria**:
- User can enter username/email, phone number (optional), and password
- System validates username is unique
- System validates password meets minimum requirements (8+ characters, mixed case, number)
- System validates phone number format (E.164 international format) if provided
- System stores credentials securely with SHA-256 hashing and salt
- System prevents duplicate usernames
- User receives confirmation of successful registration
- System automatically logs user in after successful registration

**Database Impact**: Insert new record into `users` table

**UI Components**:
- Username/email input field
- Phone number input field (optional, for SMS notifications)
- Password input field
- Confirm password input field
- "Create Account" button
- Link to switch to login screen

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
**Description**: Allow users to securely log out

**Acceptance Criteria**:
- User can logout from within the app
- System clears user session data
- System returns to login screen
- System prevents access to data after logout

**UI Components**:
- Logout button/menu option

---

### FR-2: Daily Weight Management

#### FR-2.1: Add Daily Weight Entry
**Priority**: Critical  
**Description**: Allow users to record their weight for a specific date

**Acceptance Criteria**:
- User can enter weight value (numeric)
- User can select date (default to current date)
- User can add optional notes for the entry
- System validates weight is a positive number
- System validates date is not in the future
- System stores weight entry in database linked to user
- System updates weight history display immediately
- System allows only one entry per date (update if exists)
- System displays confirmation after successful save
- System triggers achievement check after save

**Database Impact**: Insert into `daily_weights` table

**UI Components**:
- Weight input field (numeric)
- Date picker (default: today)
- Notes input field (optional)
- "Save" button
- Confirmation message/toast

#### FR-2.2: View Weight History
**Priority**: Critical  
**Description**: Display all historical weight entries for the logged-in user

**Acceptance Criteria**:
- System displays all weight entries in chronological order (newest first)
- Each entry shows date, weight value, and trend indicator
- Display updates automatically when new entry is added
- Display shows "No entries yet" message if no data exists
- System loads data from database for current user only
- System excludes soft-deleted entries (is_deleted = 1)

**Database Impact**: Query `daily_weights` table filtered by user_id and is_deleted = 0

**UI Components**:
- Scrollable grid/list view
- Date column
- Weight column
- Trend indicator (up/down arrow)
- Empty state message

#### FR-2.3: Edit Weight Entry
**Priority**: Medium  
**Description**: Allow users to modify existing weight entries

**Acceptance Criteria**:
- User can select an existing entry
- User can modify the weight value and notes
- System updates database with new values
- System maintains original date
- System updates `updated_at` timestamp
- System displays confirmation

**Database Impact**: Update `daily_weights` table

#### FR-2.4: Delete Weight Entry
**Priority**: Low  
**Description**: Allow users to remove incorrect entries

**Acceptance Criteria**:
- User can select entry to delete
- System prompts for confirmation
- System performs soft delete (sets is_deleted = 1)
- System updates display
- System maintains data integrity for historical records

**Database Impact**: Update `daily_weights` table (soft delete)

---

### FR-3: Goal Weight Management

#### FR-3.1: Set Goal Weight
**Priority**: Critical  
**Description**: Allow users to set their target weight

**Acceptance Criteria**:
- User can enter goal weight value (numeric)
- User can optionally set a target date
- System validates goal weight is a positive number
- System stores goal weight in database with start weight reference
- System allows updating goal weight if already set
- System displays current goal weight
- System displays confirmation after save

**Database Impact**: Insert/Update `goal_weights` table

**UI Components**:
- Goal weight input field (numeric)
- Target date picker (optional)
- "Set Goal" button
- Current goal display
- Progress indicator
- Confirmation message

#### FR-3.2: View Goal Progress
**Priority**: High  
**Description**: Display current goal weight and progress to user

**Acceptance Criteria**:
- System displays current goal weight prominently
- System shows progress percentage toward goal
- System displays "No goal set" if no goal exists
- System indicates if goal has been achieved
- System loads from database for current user

**Database Impact**: Query `goal_weights` table filtered by user_id

---

### FR-4: In-App Notification System

#### FR-4.1: Goal Achievement Notification
**Priority**: Critical  
**Description**: Notify user when they reach their goal weight

**Acceptance Criteria**:
- System checks if current weight equals or falls below goal weight (for weight loss)
- System checks if current weight equals or exceeds goal weight (for weight gain)
- System sends Android notification when goal is achieved
- Notification appears as Android system notification
- Notification includes congratulatory message
- System only sends notification once per goal achievement
- System records achievement in `achievements` table

**Implementation Notes**:
- Trigger check when new weight entry is added
- Compare latest weight against goal weight
- Determine direction (loss vs. gain) based on starting weight vs. goal weight

**UI Components**:
- Android notification
- In-app congratulations dialog

#### FR-4.2: Milestone Notifications
**Priority**: High  
**Description**: Notify user when they reach weight milestones

**Acceptance Criteria**:
- System tracks milestones (every 5 lbs lost/gained toward goal)
- System sends notification for each milestone
- System records milestone in `achievements` table
- System prevents duplicate milestone notifications

**Milestone Examples**:
- First 5 lbs toward goal
- Every 5 lbs thereafter
- Halfway to goal
- 90% to goal

---

### FR-5: SMS Notification System

#### FR-5.1: SMS Notifications Settings
**Priority**: High  
**Description**: Allow users to configure SMS notification preferences

**Acceptance Criteria**:
- User can enable/disable SMS notifications globally
- User can enable/disable goal achievement SMS alerts
- User can enable/disable milestone SMS alerts
- User can enable/disable daily reminder SMS
- User can set preferred reminder time
- System validates phone number is configured before enabling
- System stores preferences in `user_preferences` table
- Settings persist across sessions

**Database Impact**: Insert/Update `user_preferences` table

**UI Components**:
- SMS Notifications master toggle
- Goal Alerts toggle
- Milestone Alerts toggle
- Daily Reminders toggle
- Reminder time picker
- Phone number display/edit link
- Save confirmation

#### FR-5.2: SMS Goal Achievement Alert
**Priority**: High  
**Description**: Send SMS when user achieves their goal weight

**Acceptance Criteria**:
- System sends SMS only if SMS notifications are enabled
- System sends SMS only if goal alerts are enabled
- System sends SMS to user's registered phone number
- SMS includes congratulatory message and goal weight achieved
- System handles SMS permission denial gracefully
- System logs SMS send attempt in achievements table
- System prevents duplicate SMS for same achievement

**SMS Content Example**:
```
🎉 Weigh to Go! Congratulations! You've reached your goal weight of [X] lbs! You've got this—pound for pound!
```

**Implementation Notes**:
- Check `user_preferences` before sending
- Use Android SmsManager API
- Handle SEND_SMS permission at runtime
- Provide fallback message if SMS fails

#### FR-5.3: SMS Milestone Alert
**Priority**: Medium  
**Description**: Send SMS when user reaches weight milestones

**Acceptance Criteria**:
- System sends SMS only if SMS notifications are enabled
- System sends SMS only if milestone alerts are enabled
- SMS includes milestone details and encouragement
- System tracks which milestones have triggered SMS
- System prevents duplicate SMS for same milestone

**SMS Content Examples**:
```
🔥 Weigh to Go! Amazing progress! You've lost 5 lbs toward your goal! Keep it up!
```
```
⭐ Weigh to Go! Halfway there! You've reached 50% of your goal! You've got this!
```

#### FR-5.4: SMS Daily Reminder
**Priority**: Low  
**Description**: Send daily SMS reminder to log weight

**Acceptance Criteria**:
- System sends SMS only if daily reminders are enabled
- SMS sent at user's preferred time
- SMS includes friendly reminder message
- System respects user's quiet hours (no SMS before 7 AM or after 9 PM unless configured)
- User can disable without affecting other SMS settings

**SMS Content Example**:
```
📊 Weigh to Go! Don't forget to log your weight today! Every entry brings you closer to your goal.
```

**Implementation Notes**:
- Use Android AlarmManager or WorkManager for scheduling
- Respect device Do Not Disturb settings
- Allow user to configure reminder time

#### FR-5.5: SMS Permission Handling
**Priority**: Critical  
**Description**: Properly request and handle SMS permission

**Acceptance Criteria**:
- System requests SEND_SMS permission at runtime when user enables SMS features
- System explains why permission is needed before requesting
- System gracefully handles permission denial
- System disables SMS features if permission denied
- System provides option to re-request permission
- System directs user to app settings if permission permanently denied

**UI Components**:
- Permission rationale dialog
- Settings redirect option
- Feature unavailable message

---

## Database Architecture

### Database Technology
**SQLite** - Local database storage on Android device

### Database Schema

#### Table 1: `users`
**Purpose**: Store user authentication credentials and profile information

| Column Name | Data Type | Constraints | Description |
|-------------|-----------|-------------|-------------|
| `user_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique identifier for each user |
| `username` | TEXT | NOT NULL, UNIQUE | User's login username |
| `email` | TEXT | UNIQUE | User's email address (optional) |
| `phone_number` | TEXT | | User's phone for SMS (E.164 format) |
| `password_hash` | TEXT | NOT NULL | SHA-256 hashed password |
| `salt` | TEXT | NOT NULL | Random salt for password hashing |
| `display_name` | TEXT | | User's preferred display name |
| `created_at` | TEXT | DEFAULT CURRENT_TIMESTAMP | Account creation date |
| `updated_at` | TEXT | DEFAULT CURRENT_TIMESTAMP | Last profile update |
| `is_active` | INTEGER | DEFAULT 1 | Account active status |
| `last_login` | TEXT | | Last successful login timestamp |

**Indexes**:
- Primary key index on `user_id`
- Unique index on `username`
- Unique index on `email` (if not null)

**Sample SQL**:
```sql
CREATE TABLE users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT UNIQUE,
    phone_number TEXT,
    password_hash TEXT NOT NULL,
    salt TEXT NOT NULL,
    display_name TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    is_active INTEGER DEFAULT 1,
    last_login TEXT
);

CREATE UNIQUE INDEX idx_username ON users(username);
CREATE UNIQUE INDEX idx_email ON users(email) WHERE email IS NOT NULL;
```

#### Table 2: `daily_weights`
**Purpose**: Store daily weight entries for all users

| Column Name | Data Type | Constraints | Description |
|-------------|-----------|-------------|-------------|
| `weight_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique identifier for each weight entry |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY | Reference to users table |
| `weight_value` | REAL | NOT NULL | Weight value in user's preferred unit |
| `weight_unit` | TEXT | DEFAULT 'lbs' | Unit of measurement (lbs/kg) |
| `entry_date` | TEXT | NOT NULL | Date of weight entry (YYYY-MM-DD) |
| `notes` | TEXT | | Optional notes for the entry |
| `created_at` | TEXT | DEFAULT CURRENT_TIMESTAMP | Timestamp when entry was created |
| `is_deleted` | INTEGER | DEFAULT 0 | Soft delete flag |

**Indexes**:
- Primary key index on `weight_id`
- Index on `user_id` (for efficient user-specific queries)
- Composite index on `(user_id, entry_date)` (for ensuring one entry per date per user)

**Sample SQL**:
```sql
CREATE TABLE daily_weights (
    weight_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    weight_value REAL NOT NULL,
    weight_unit TEXT DEFAULT 'lbs',
    entry_date TEXT NOT NULL,
    notes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    is_deleted INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE(user_id, entry_date)
);

CREATE INDEX idx_user_weights ON daily_weights(user_id);
CREATE INDEX idx_user_date ON daily_weights(user_id, entry_date);
CREATE INDEX idx_active_weights ON daily_weights(user_id, is_deleted);
```

#### Table 3: `goal_weights`
**Purpose**: Store goal weight for each user

| Column Name | Data Type | Constraints | Description |
|-------------|-----------|-------------|-------------|
| `goal_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique identifier for goal |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY | Reference to users table |
| `goal_weight` | REAL | NOT NULL | Target weight value |
| `goal_unit` | TEXT | DEFAULT 'lbs' | Unit of measurement |
| `start_weight` | REAL | | Weight when goal was set |
| `target_date` | TEXT | | Target date to achieve goal |
| `set_date` | TEXT | DEFAULT CURRENT_TIMESTAMP | When goal was set |
| `is_achieved` | INTEGER | DEFAULT 0 | Goal achievement flag |
| `achieved_date` | TEXT | | Date goal was achieved |
| `is_active` | INTEGER | DEFAULT 1 | Current active goal flag |

**Indexes**:
- Primary key index on `goal_id`
- Index on `user_id`
- Index on active goals `(user_id, is_active)`

**Sample SQL**:
```sql
CREATE TABLE goal_weights (
    goal_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    goal_weight REAL NOT NULL,
    goal_unit TEXT DEFAULT 'lbs',
    start_weight REAL,
    target_date TEXT,
    set_date TEXT DEFAULT CURRENT_TIMESTAMP,
    is_achieved INTEGER DEFAULT 0,
    achieved_date TEXT,
    is_active INTEGER DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_user_goals ON goal_weights(user_id);
CREATE INDEX idx_active_goal ON goal_weights(user_id, is_active);
```

#### Table 4: `achievements`
**Purpose**: Track user achievements and milestone completions

| Column Name | Data Type | Constraints | Description |
|-------------|-----------|-------------|-------------|
| `achievement_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY | Reference to users table |
| `goal_id` | INTEGER | FOREIGN KEY | Reference to goal_weights table |
| `type` | TEXT | NOT NULL | Achievement type (goal_reached, milestone, streak) |
| `title` | TEXT | NOT NULL | Achievement title |
| `description` | TEXT | | Achievement description |
| `achieved_at` | TEXT | DEFAULT CURRENT_TIMESTAMP | When achievement was earned |
| `is_notified` | INTEGER | DEFAULT 0 | In-app notification sent flag |
| `sms_sent` | INTEGER | DEFAULT 0 | SMS notification sent flag |

**Achievement Types**:
- `goal_reached`: User achieved their goal weight
- `milestone_5`: Lost/gained 5 lbs toward goal
- `milestone_10`: Lost/gained 10 lbs toward goal
- `milestone_halfway`: Reached 50% of goal
- `milestone_90`: Reached 90% of goal
- `first_entry`: First weight entry logged
- `streak_7`: 7-day logging streak
- `streak_30`: 30-day logging streak

**Sample SQL**:
```sql
CREATE TABLE achievements (
    achievement_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    goal_id INTEGER,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    achieved_at TEXT DEFAULT CURRENT_TIMESTAMP,
    is_notified INTEGER DEFAULT 0,
    sms_sent INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (goal_id) REFERENCES goal_weights(goal_id) ON DELETE SET NULL
);

CREATE INDEX idx_user_achievements ON achievements(user_id);
CREATE INDEX idx_achievement_type ON achievements(user_id, type);
```

#### Table 5: `user_preferences`
**Purpose**: Store user preferences and settings

| Column Name | Data Type | Constraints | Description |
|-------------|-----------|-------------|-------------|
| `preference_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique identifier |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY | Reference to users table |
| `pref_key` | TEXT | NOT NULL | Preference key name |
| `pref_value` | TEXT | | Preference value |
| `created_at` | TEXT | DEFAULT CURRENT_TIMESTAMP | When preference was created |
| `updated_at` | TEXT | DEFAULT CURRENT_TIMESTAMP | When preference was last updated |

**Preference Keys**:
| Key | Values | Description |
|-----|--------|-------------|
| `weight_unit` | `lbs`, `kg` | Preferred weight unit |
| `theme` | `light`, `dark`, `system` | App theme preference |
| `notifications_enabled` | `true`, `false` | In-app notifications |
| `sms_notifications_enabled` | `true`, `false` | SMS notifications master toggle |
| `sms_goal_alerts` | `true`, `false` | SMS for goal achievement |
| `sms_milestone_alerts` | `true`, `false` | SMS for milestones |
| `sms_reminder_enabled` | `true`, `false` | Daily SMS reminders |
| `reminder_time` | `HH:MM` | Daily reminder time (24-hour format) |

**Sample SQL**:
```sql
CREATE TABLE user_preferences (
    preference_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    pref_key TEXT NOT NULL,
    pref_value TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE(user_id, pref_key)
);

CREATE INDEX idx_user_prefs ON user_preferences(user_id);
CREATE UNIQUE INDEX idx_user_pref_key ON user_preferences(user_id, pref_key);
```

### Database Helper Class Structure

```java
public class WeightTrackerDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "weigh_to_go.db";
    private static final int DATABASE_VERSION = 2;
    
    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_DAILY_WEIGHTS = "daily_weights";
    public static final String TABLE_GOAL_WEIGHTS = "goal_weights";
    public static final String TABLE_ACHIEVEMENTS = "achievements";
    public static final String TABLE_USER_PREFERENCES = "user_preferences";
    
    // Users table columns
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_SALT = "salt";
    public static final String COLUMN_DISPLAY_NAME = "display_name";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";
    public static final String COLUMN_IS_ACTIVE = "is_active";
    public static final String COLUMN_LAST_LOGIN = "last_login";
    
    // Daily weights table columns
    public static final String COLUMN_WEIGHT_ID = "weight_id";
    public static final String COLUMN_WEIGHT_VALUE = "weight_value";
    public static final String COLUMN_WEIGHT_UNIT = "weight_unit";
    public static final String COLUMN_ENTRY_DATE = "entry_date";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_IS_DELETED = "is_deleted";
    
    // Goal weights table columns
    public static final String COLUMN_GOAL_ID = "goal_id";
    public static final String COLUMN_GOAL_WEIGHT = "goal_weight";
    public static final String COLUMN_GOAL_UNIT = "goal_unit";
    public static final String COLUMN_START_WEIGHT = "start_weight";
    public static final String COLUMN_TARGET_DATE = "target_date";
    public static final String COLUMN_SET_DATE = "set_date";
    public static final String COLUMN_IS_ACHIEVED = "is_achieved";
    public static final String COLUMN_ACHIEVED_DATE = "achieved_date";
    
    // Achievements table columns
    public static final String COLUMN_ACHIEVEMENT_ID = "achievement_id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_ACHIEVED_AT = "achieved_at";
    public static final String COLUMN_IS_NOTIFIED = "is_notified";
    public static final String COLUMN_SMS_SENT = "sms_sent";
    
    // User preferences table columns
    public static final String COLUMN_PREFERENCE_ID = "preference_id";
    public static final String COLUMN_PREF_KEY = "pref_key";
    public static final String COLUMN_PREF_VALUE = "pref_value";
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all tables
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_DAILY_WEIGHTS);
        db.execSQL(SQL_CREATE_GOAL_WEIGHTS);
        db.execSQL(SQL_CREATE_ACHIEVEMENTS);
        db.execSQL(SQL_CREATE_USER_PREFERENCES);
        
        // Create indexes
        createIndexes(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Migrate from version 1 to 2
            db.execSQL("ALTER TABLE users ADD COLUMN phone_number TEXT");
            db.execSQL("ALTER TABLE users ADD COLUMN email TEXT");
            db.execSQL(SQL_CREATE_ACHIEVEMENTS);
            db.execSQL(SQL_CREATE_USER_PREFERENCES);
        }
    }
    
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
```

### Entity Relationship Diagram

```
┌─────────────────┐       ┌──────────────────┐       ┌─────────────────┐
│     users       │       │  daily_weights   │       │  goal_weights   │
├─────────────────┤       ├──────────────────┤       ├─────────────────┤
│ PK user_id      │──┐    │ PK weight_id     │       │ PK goal_id      │
│    username     │  │    │ FK user_id       │───┐   │ FK user_id      │───┐
│    email        │  │    │    weight_value  │   │   │    goal_weight  │   │
│    phone_number │  │    │    weight_unit   │   │   │    goal_unit    │   │
│    password_hash│  │    │    entry_date    │   │   │    start_weight │   │
│    salt         │  │    │    notes         │   │   │    target_date  │   │
│    display_name │  │    │    created_at    │   │   │    set_date     │   │
│    created_at   │  │    │    is_deleted    │   │   │    is_achieved  │   │
│    updated_at   │  │    └──────────────────┘   │   │    achieved_date│   │
│    is_active    │  │                           │   │    is_active    │   │
│    last_login   │  │                           │   └─────────────────┘   │
└─────────────────┘  │                           │                         │
         │           │    ┌──────────────────┐   │   ┌─────────────────┐   │
         │           │    │   achievements   │   │   │user_preferences │   │
         │           │    ├──────────────────┤   │   ├─────────────────┤   │
         │           │    │ PK achievement_id│   │   │ PK preference_id│   │
         │           └────│ FK user_id       │   │   │ FK user_id      │───┘
         │                │ FK goal_id       │───┘   │    pref_key     │
         │                │    type          │       │    pref_value   │
         │                │    title         │       │    created_at   │
         │                │    description   │       │    updated_at   │
         │                │    achieved_at   │       └─────────────────┘
         │                │    is_notified   │
         │                │    sms_sent      │
         │                └──────────────────┘
         │
         └────────────────────────────────────────────────────────────────┘
                              One-to-Many Relationships
```

---

## User Interface Requirements

### UI-1: Login/Registration Screen

**Layout**: Single screen with toggle between login and registration modes

**Components**:
- App logo/title ("Weigh to Go!" with tagline)
- Username input field
- Email input field (registration mode, optional)
- Phone number input field (registration mode, optional with SMS explanation)
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
- Phone number field explains SMS notification feature
- Focus on simplicity and clarity

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
- Current goal weight display
- Menu button (for logout, settings, SMS notifications)

**Design Considerations**:
- Most recent entries at top
- Clear date formatting (e.g., "Today", "Yesterday", "Nov 8, 2025")
- Visual feedback for trends (up/down arrows with color coding)
- Empty state message if no entries
- Quick access to SMS notification settings

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
- Trigger achievement check on save

### UI-4: Goal Setting Screen/Dialog

**Layout**: Modal dialog or separate screen

**Components**:
- Current goal display (if exists)
- Goal weight input field (numeric keyboard)
- Unit display/toggle (lbs or kg)
- Target date picker (optional)
- Progress visualization (if goal exists)
- "Set Goal" / "Update Goal" button
- "Cancel" button

**Design Considerations**:
- Show current goal prominently
- Display progress toward existing goal
- Clear update vs. initial set distinction
- Validation feedback
- Success confirmation

### UI-5: SMS Notifications Settings Screen

**Layout**: Dedicated settings screen for SMS notification preferences

**Components**:
- Screen header: "SMS Notifications"
- Back navigation arrow
- Phone number display with edit option
- SMS Notifications master toggle (Material Switch)
- Notification type section (when master enabled):
  - Goal Alerts toggle with description
  - Milestone Alerts toggle with description
  - Daily Reminders toggle with description
- Reminder time picker (when daily reminders enabled)
- Permission status indicator
- Save/Apply button
- Help text explaining SMS features

**Design Considerations**:
- Clear toggle states (enabled/disabled visual feedback)
- Cascading enables (sub-options disabled when master is off)
- Permission request flow when enabling SMS
- Phone number validation and formatting
- Accessible touch targets (48dp minimum)
- Material Design 3 switch components

**Toggle Dependencies**:
```
SMS Notifications (Master)
├── Goal Alerts (requires master ON)
├── Milestone Alerts (requires master ON)
└── Daily Reminders (requires master ON)
    └── Reminder Time (requires daily reminders ON)
```

### UI-6: Navigation Structure

```
Login/Register Screen
    ↓ (on successful login)
Main Dashboard
    ├→ Add Weight Entry (dialog/screen)
    │   └→ Returns to Dashboard (with achievement check)
    ├→ Set Goal Weight (dialog/screen)
    │   └→ Returns to Dashboard
    └→ Menu
        ├→ SMS Notifications Settings
        │   └→ Returns to Dashboard
        ├→ Profile Settings (future)
        ├→ About
        └→ Logout
            └→ Returns to Login Screen
```

---

## Technical Implementation Details

### Tech-1: Android Components

#### Activities
- **LoginActivity**: Handle login/registration
- **MainActivity**: Main dashboard with weight history
- **AddWeightActivity** (or Dialog): Weight entry interface
- **GoalSettingActivity** (or Dialog): Goal management
- **SmsNotificationsActivity**: SMS notification settings

#### Fragments (Optional Alternative)
- **LoginFragment**
- **DashboardFragment**
- **WeightHistoryFragment**
- **SmsSettingsFragment**

#### Services
- **NotificationService**: Background service for goal achievement checks
- **SmsReminderService**: Scheduled service for daily SMS reminders

#### Broadcast Receivers
- **SmsReminderReceiver**: Receives alarm broadcasts for scheduled SMS
- **BootReceiver**: Re-schedules SMS reminders after device restart

### Tech-2: Data Access Layer

#### Database Manager Class
```java
public class WeightTrackerDAO {
    private WeightTrackerDBHelper dbHelper;
    private SQLiteDatabase database;
    
    // User methods
    public long createUser(String username, String email, String phone, String password);
    public User authenticateUser(String username, String password);
    public boolean updateUserPhone(int userId, String phoneNumber);
    public void updateLastLogin(int userId);
    
    // Weight entry methods
    public long addWeightEntry(int userId, double weight, String unit, String date, String notes);
    public List<WeightEntry> getWeightHistory(int userId);
    public WeightEntry getLatestWeight(int userId);
    public boolean updateWeightEntry(int weightId, double newWeight, String notes);
    public boolean softDeleteWeightEntry(int weightId);
    
    // Goal weight methods
    public long setGoalWeight(int userId, double goalWeight, double startWeight, String targetDate);
    public GoalWeight getActiveGoal(int userId);
    public boolean updateGoalWeight(int userId, double newGoalWeight);
    public boolean markGoalAchieved(int goalId);
    
    // Achievement methods
    public long recordAchievement(int userId, Integer goalId, String type, String title, String description);
    public List<Achievement> getUserAchievements(int userId);
    public boolean hasAchievement(int userId, String type);
    public boolean markAchievementNotified(int achievementId);
    public boolean markAchievementSmsSent(int achievementId);
    
    // Preference methods
    public void setPreference(int userId, String key, String value);
    public String getPreference(int userId, String key);
    public Map<String, String> getAllPreferences(int userId);
    public boolean isSmsEnabled(int userId);
    public boolean isSmsGoalAlertsEnabled(int userId);
    public boolean isSmsMilestoneAlertsEnabled(int userId);
    public boolean isSmsRemindersEnabled(int userId);
    public String getReminderTime(int userId);
}
```

#### Data Models
```java
public class User {
    private int userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String salt;
    private String displayName;
    private String createdAt;
    private String updatedAt;
    private boolean isActive;
    private String lastLogin;
    // Getters and setters
}

public class WeightEntry {
    private int weightId;
    private int userId;
    private double weightValue;
    private String weightUnit;
    private String entryDate;
    private String notes;
    private String createdAt;
    private boolean isDeleted;
    // Getters and setters
}

public class GoalWeight {
    private int goalId;
    private int userId;
    private double goalWeight;
    private String goalUnit;
    private double startWeight;
    private String targetDate;
    private String setDate;
    private boolean isAchieved;
    private String achievedDate;
    private boolean isActive;
    // Getters and setters
    
    public double getProgressPercentage(double currentWeight) {
        if (startWeight == goalWeight) return 100.0;
        double totalChange = Math.abs(startWeight - goalWeight);
        double currentChange = Math.abs(startWeight - currentWeight);
        return Math.min(100.0, (currentChange / totalChange) * 100);
    }
}

public class Achievement {
    private int achievementId;
    private int userId;
    private Integer goalId;
    private String type;
    private String title;
    private String description;
    private String achievedAt;
    private boolean isNotified;
    private boolean smsSent;
    // Getters and setters
}

public class UserPreference {
    private int preferenceId;
    private int userId;
    private String prefKey;
    private String prefValue;
    private String createdAt;
    private String updatedAt;
    // Getters and setters
}
```

### Tech-3: Notification Implementation

#### In-App Notification Manager
```java
public class AppNotificationManager {
    private static final String CHANNEL_ID = "weight_goal_channel";
    private static final String CHANNEL_NAME = "Weight Goal Notifications";
    private static final int GOAL_NOTIFICATION_ID = 1001;
    private static final int MILESTONE_NOTIFICATION_ID = 1002;
    
    public void createNotificationChannel(Context context);
    public void sendGoalAchievedNotification(Context context, double goalWeight);
    public void sendMilestoneNotification(Context context, String milestoneTitle, String message);
    public boolean checkGoalAchievement(double currentWeight, double goalWeight, double startWeight);
    public String checkMilestone(double currentWeight, double goalWeight, double startWeight);
}
```

#### SMS Notification Manager
```java
public class SmsNotificationManager {
    private Context context;
    private WeightTrackerDAO dao;
    
    // Permission handling
    public boolean hasSmsPermission();
    public void requestSmsPermission(Activity activity, int requestCode);
    public boolean shouldShowPermissionRationale(Activity activity);
    
    // SMS sending
    public boolean sendGoalAchievedSms(int userId, double goalWeight);
    public boolean sendMilestoneSms(int userId, String milestoneTitle, double progress);
    public boolean sendReminderSms(int userId);
    
    // Helper methods
    private boolean sendSms(String phoneNumber, String message);
    private String getUserPhoneNumber(int userId);
    private boolean isFeatureEnabled(int userId, String prefKey);
    
    // Message templates
    public static String getGoalAchievedMessage(double goalWeight) {
        return String.format("🎉 Weigh to Go! Congratulations! You've reached your goal weight of %.1f lbs! You've got this—pound for pound!", goalWeight);
    }
    
    public static String getMilestoneMessage(String milestone, double progress) {
        return String.format("🔥 Weigh to Go! %s You're %.0f%% of the way to your goal! Keep it up!", milestone, progress);
    }
    
    public static String getReminderMessage() {
        return "📊 Weigh to Go! Don't forget to log your weight today! Every entry brings you closer to your goal.";
    }
}
```

#### SMS Utility Class
```java
public class SmsNotificationUtils {
    
    /**
     * Check if device can send SMS
     */
    public static boolean canSendSms(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }
    
    /**
     * Send SMS using Android SmsManager
     */
    public static boolean sendSms(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            
            // Handle long messages
            if (message.length() > 160) {
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            }
            return true;
        } catch (Exception e) {
            Log.e("SmsUtils", "Failed to send SMS", e);
            return false;
        }
    }
    
    /**
     * Validate phone number format (E.164)
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        // E.164 format: +[country code][number], max 15 digits
        return phone.matches("^\\+[1-9]\\d{1,14}$");
    }
    
    /**
     * Format phone number to E.164
     */
    public static String formatPhoneNumber(String phone, String defaultCountryCode) {
        // Remove all non-digit characters except leading +
        String cleaned = phone.replaceAll("[^\\d+]", "");
        
        if (cleaned.startsWith("+")) {
            return cleaned;
        } else if (cleaned.startsWith("1") && cleaned.length() == 11) {
            return "+" + cleaned;
        } else if (cleaned.length() == 10) {
            return "+" + defaultCountryCode + cleaned;
        }
        return cleaned;
    }
}
```

#### Daily Reminder Scheduler
```java
public class SmsReminderScheduler {
    private static final int REMINDER_REQUEST_CODE = 2001;
    
    /**
     * Schedule daily SMS reminder
     */
    public static void scheduleReminder(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SmsReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            REMINDER_REQUEST_CODE, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Set reminder time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        
        // If time has passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        // Schedule repeating alarm
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        );
    }
    
    /**
     * Cancel scheduled reminder
     */
    public static void cancelReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SmsReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            REMINDER_REQUEST_CODE, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }
}
```

**Notification Trigger Points**:
- After user saves a new weight entry → check goal achievement and milestones
- Compare latest weight against goal weight
- Consider direction (loss vs. gain) based on relationship between starting weight and goal
- Check user preferences before sending any SMS
- Record notification/SMS sent status to prevent duplicates

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
  
- **Phone Number**:
  - Optional field
  - If provided, must be valid E.164 format
  - Example: +15551234567
  - Max 15 digits after country code
  
- **Weight Value**:
  - Not empty
  - Positive number
  - Reasonable range: 1-1000 lbs or 0.5-500 kg
  - Maximum 2 decimal places
  
- **Date**:
  - Valid date format
  - Not in the future
  - Not before reasonable start date (e.g., not before 1900)

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

// Note: User-specific preferences stored in user_preferences table
// SharedPreferences used only for session management
```

#### SQLite Database (for user data)
- All user, weight, goal, achievement, and preference data stored in SQLite
- Database persists across app restarts
- Foreign key constraints enforced
- Cascading deletes for data integrity

---

## Security & Privacy Requirements

### SEC-1: Password Security
**Priority**: Critical

**Requirements**:
- Passwords must be hashed using SHA-256 with unique salt
- Never store plain-text passwords
- Generate cryptographically secure random salt for each user
- Salt stored separately from hash

**Implementation**:
```java
public class PasswordUtils {
    
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }
    
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hash = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    public static boolean verifyPassword(String password, String salt, String storedHash) {
        String computedHash = hashPassword(password, salt);
        return computedHash.equals(storedHash);
    }
}
```

### SEC-2: User Data Isolation
**Priority**: Critical

**Requirements**:
- Each user can only access their own data
- All database queries must filter by current user_id
- Enforce foreign key constraints
- Use ON DELETE CASCADE to ensure data integrity
- Validate user_id on all operations

### SEC-3: Input Sanitization
**Priority**: High

**Requirements**:
- Sanitize all user inputs before database operations
- Use parameterized queries to prevent SQL injection
- Validate data types and ranges
- Escape special characters in display

**Example**:
```java
// CORRECT - Using parameterized queries
String query = "INSERT INTO daily_weights (user_id, weight_value, entry_date) VALUES (?, ?, ?)";
SQLiteStatement statement = db.compileStatement(query);
statement.bindLong(1, userId);
statement.bindDouble(2, weight);
statement.bindString(3, date);
statement.executeInsert();

// WRONG - Never concatenate user input into queries
// String query = "INSERT INTO daily_weights VALUES (" + userId + ", " + weight + ", '" + date + "')";
```

### SEC-4: Session Management
**Priority**: High

**Requirements**:
- Implement proper session timeout
- Clear session data on logout
- Secure session storage using SharedPreferences
- Validate session on app resume
- Update last_login timestamp

### SEC-5: SMS Security
**Priority**: High

**Requirements**:
- Store phone numbers securely in database
- Validate phone number format before storage
- Request SMS permission only when needed (just-in-time)
- Provide clear explanation for permission request
- Handle permission denial gracefully
- Do not send SMS without explicit user opt-in
- Log all SMS attempts for audit purposes
- Respect user's notification preferences

**Permission Handling**:
```java
// Check permission
if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) 
        != PackageManager.PERMISSION_GRANTED) {
    
    // Show rationale if needed
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
        showPermissionRationaleDialog();
    } else {
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.SEND_SMS}, 
            SMS_PERMISSION_REQUEST_CODE);
    }
} else {
    // Permission already granted
    enableSmsFeatures();
}
```

---

## User Experience Requirements

### UX-1: Performance

**Requirements**:
- App launch time: < 2 seconds
- Database query response: < 500ms
- Smooth scrolling in weight history (60 FPS)
- No visible lag when adding entries
- SMS sending should not block UI

**Implementation Strategies**:
- Use background threads for database operations
- Use AsyncTask, Executors, or modern alternatives for SMS sending
- Implement pagination for large weight history (if >100 entries)
- Cache frequently accessed data
- Use RecyclerView for efficient list rendering

### UX-2: Feedback & Confirmation

**Requirements**:
- Show loading indicators for operations > 200ms
- Display success messages (Toast) for completed actions
- Show error messages clearly with actionable guidance
- Provide visual feedback for button presses (ripple effect)
- Confirm SMS sent successfully or show failure message

### UX-3: Error Handling

**Requirements**:
- Graceful handling of all errors
- User-friendly error messages (avoid technical jargon)
- Prevent app crashes with try-catch blocks
- Log errors for debugging
- Handle SMS permission denial without crashing

**Common Error Scenarios**:
- Database connection failure
- Invalid input data
- SMS permission denied
- SMS send failure (no signal, invalid number)
- Storage space limitations

### UX-4: Accessibility

**Requirements**:
- All UI elements have content descriptions
- Minimum touch target size: 48dp x 48dp
- Sufficient color contrast (WCAG AA standards)
- Support for screen readers (TalkBack)
- Keyboard navigation support
- Toggle switches clearly indicate state

### UX-5: Responsiveness

**Requirements**:
- Support multiple screen sizes (phone, tablet)
- Layouts adapt to landscape and portrait orientations
- No horizontal scrolling (except where intentional)
- Appropriate text sizes for readability

---

## Testing Requirements

### TEST-1: Unit Testing

**Components to Test**:
- Database CRUD operations (all 5 tables)
- Input validation logic
- Password hashing/verification
- Goal achievement calculation
- Milestone detection logic
- Phone number validation and formatting
- SMS message generation
- Data model getters/setters

**Framework**: JUnit 4 or JUnit 5

**Sample Test Cases**:
```java
@Test
public void testAddWeightEntry_ValidData_ReturnsId() {
    WeightTrackerDAO dao = new WeightTrackerDAO(context);
    int userId = 1;
    double weight = 180.5;
    String date = "2025-11-08";
    
    long result = dao.addWeightEntry(userId, weight, "lbs", date, null);
    
    assertTrue(result > 0);
}

@Test
public void testPhoneValidation_ValidE164_ReturnsTrue() {
    assertTrue(SmsNotificationUtils.isValidPhoneNumber("+15551234567"));
    assertTrue(SmsNotificationUtils.isValidPhoneNumber("+442071234567"));
}

@Test
public void testPhoneValidation_InvalidFormat_ReturnsFalse() {
    assertFalse(SmsNotificationUtils.isValidPhoneNumber("5551234567"));
    assertFalse(SmsNotificationUtils.isValidPhoneNumber("555-123-4567"));
    assertFalse(SmsNotificationUtils.isValidPhoneNumber(""));
}

@Test
public void testGoalAchievement_WeightLoss_CorrectlyDetects() {
    double startWeight = 200.0;
    double goalWeight = 180.0;
    double currentWeight = 179.5;
    
    boolean achieved = notificationManager.checkGoalAchievement(
        currentWeight, goalWeight, startWeight);
    
    assertTrue(achieved);
}

@Test
public void testMilestoneDetection_FivePoundMilestone_Detects() {
    double startWeight = 200.0;
    double goalWeight = 180.0;
    double currentWeight = 195.0; // Lost 5 lbs
    
    String milestone = notificationManager.checkMilestone(
        currentWeight, goalWeight, startWeight);
    
    assertEquals("milestone_5", milestone);
}

@Test
public void testPasswordHash_SamePassword_SameHash() {
    String password = "TestPass123";
    String salt = PasswordUtils.generateSalt();
    
    String hash1 = PasswordUtils.hashPassword(password, salt);
    String hash2 = PasswordUtils.hashPassword(password, salt);
    
    assertEquals(hash1, hash2);
}

@Test
public void testPasswordVerify_CorrectPassword_ReturnsTrue() {
    String password = "TestPass123";
    String salt = PasswordUtils.generateSalt();
    String hash = PasswordUtils.hashPassword(password, salt);
    
    assertTrue(PasswordUtils.verifyPassword(password, salt, hash));
}
```

### TEST-2: Integration Testing

**Test Scenarios**:
- Complete user registration flow (with phone number)
- Login → Add weight → View history flow
- Set goal → Add weights → Receive notification flow
- Enable SMS → Achieve goal → Receive SMS flow
- Database persistence across app restarts
- Preference changes persist correctly

**Framework**: Espresso or AndroidJUnit

### TEST-3: UI Testing

**Test Scenarios**:
- All buttons and inputs are clickable/tappable
- Navigation flows work correctly
- Error messages display properly
- Screen rotations preserve data
- Keyboard interactions work correctly
- SMS settings toggles work correctly
- Permission dialogs display appropriately

**Framework**: Espresso

**Sample Test**:
```java
@Test
public void testSmsSettings_ToggleMasterSwitch_UpdatesSubOptions() {
    // Navigate to SMS settings
    onView(withId(R.id.menu_sms_settings)).perform(click());
    
    // Verify sub-options are disabled initially
    onView(withId(R.id.switch_goal_alerts)).check(matches(not(isEnabled())));
    
    // Enable master switch
    onView(withId(R.id.switch_sms_notifications)).perform(click());
    
    // Verify sub-options are now enabled
    onView(withId(R.id.switch_goal_alerts)).check(matches(isEnabled()));
}
```

### TEST-4: SMS-Specific Testing

**Test Scenarios**:
- [ ] SMS permission request displays correctly
- [ ] Permission rationale dialog shows when appropriate
- [ ] App handles permission denial gracefully
- [ ] SMS sends successfully with valid phone number
- [ ] App handles SMS send failure appropriately
- [ ] Duplicate SMS prevention works
- [ ] Daily reminders schedule correctly
- [ ] Reminders respect configured time
- [ ] SMS content is correct for each type

### TEST-5: Manual Testing Checklist

**Pre-Release Testing**:
- [ ] Install on physical device
- [ ] Test on multiple Android versions (API 26+)
- [ ] Test with various screen sizes
- [ ] Test in portrait and landscape
- [ ] Test with poor/no network (local features should work)
- [ ] Test database with large datasets (100+ entries)
- [ ] Test rapid user interactions (stress testing)
- [ ] Verify in-app notifications appear correctly
- [ ] Verify SMS notifications send correctly (with permission)
- [ ] Test SMS with invalid phone number
- [ ] Test logout and re-login
- [ ] Verify data persists after app closes
- [ ] Test SMS settings persistence

---

## Future Enhancements

### Phase 2 Features (After Course Completion)

1. **Data Visualization**
   - Line chart showing weight trends over time
   - BMI calculation and tracking
   - Weight loss/gain rate statistics
   - Progress percentage toward goal

2. **Enhanced Notifications**
   - ~~Daily reminder to weigh in~~ ✅ Implemented as SMS
   - ~~Customizable notification times~~ ✅ Implemented
   - ~~Milestone celebrations~~ ✅ Implemented
   - Push notifications via Firebase (cloud-based)

3. **Data Management**
   - Export weight history to CSV
   - Import data from other apps
   - Cloud backup and sync
   - Data sharing with healthcare providers

4. **Additional Features**
   - Multiple goal types (weight ranges, maintenance)
   - ~~Notes field for each weight entry~~ ✅ Implemented
   - Photo progress tracking
   - Body measurements tracking
   - Integration with fitness apps (Google Fit, etc.)

5. **Social Features**
   - Share progress with friends/family
   - Support groups/communities
   - Challenges and competitions

6. **Advanced UI**
   - Dark mode support
   - Customizable themes
   - Widget for home screen
   - Wear OS companion app

---

## Development Checklist

### Phase 1: Project Setup
- [ ] Create new Android Studio project
- [ ] Set up version control (Git)
- [ ] Configure minimum SDK version (26)
- [ ] Configure target SDK version (34)
- [ ] Add necessary permissions to AndroidManifest.xml
- [ ] Set up project structure (packages, folders)
- [ ] Create README.md with project description
- [ ] Add app icon and branding assets

### Phase 2: Database Implementation
- [ ] Create SQLiteOpenHelper class
- [ ] Define database schema (5 tables)
- [ ] Write SQL CREATE TABLE statements
- [ ] Implement onCreate() method
- [ ] Implement onUpgrade() method
- [ ] Create database manager/DAO class
- [ ] Write CRUD methods for users table
- [ ] Write CRUD methods for daily_weights table
- [ ] Write CRUD methods for goal_weights table
- [ ] Write CRUD methods for achievements table
- [ ] Write CRUD methods for user_preferences table
- [ ] Test database operations with unit tests

### Phase 3: Data Models
- [ ] Create User model class
- [ ] Create WeightEntry model class
- [ ] Create GoalWeight model class
- [ ] Create Achievement model class
- [ ] Create UserPreference model class
- [ ] Implement constructors, getters, setters
- [ ] Add toString() methods for debugging

### Phase 4: Authentication UI
- [ ] Design login/register layout (XML)
- [ ] Create LoginActivity class
- [ ] Implement registration logic (with phone number)
- [ ] Implement login logic
- [ ] Add input validation
- [ ] Implement password hashing (SHA-256 with salt)
- [ ] Add error message displays
- [ ] Test login/register flows

### Phase 5: Main Dashboard UI
- [ ] Design main dashboard layout (XML)
- [ ] Create MainActivity class
- [ ] Implement navigation from login to dashboard
- [ ] Display user greeting/info
- [ ] Display current weight and goal progress
- [ ] Add logout functionality
- [ ] Add menu with SMS settings option
- [ ] Verify session management

### Phase 6: Weight Entry Feature
- [ ] Design add weight entry layout (XML)
- [ ] Create AddWeightActivity or dialog
- [ ] Implement weight input field
- [ ] Implement date picker
- [ ] Implement notes field
- [ ] Add save button functionality
- [ ] Connect to database
- [ ] Implement validation
- [ ] Add success/error feedback
- [ ] Trigger achievement check on save
- [ ] Test weight entry flow

### Phase 7: Weight History Display
- [ ] Design weight history list item layout (XML)
- [ ] Create RecyclerView adapter
- [ ] Implement data binding
- [ ] Query database for user's weights
- [ ] Display weights in chronological order
- [ ] Add trend indicators
- [ ] Add empty state message
- [ ] Test with various data amounts
- [ ] Implement pull-to-refresh (optional)

### Phase 8: Goal Weight Feature
- [ ] Design goal setting layout (XML)
- [ ] Create goal setting activity/dialog
- [ ] Implement goal weight input
- [ ] Implement target date picker
- [ ] Add save goal functionality
- [ ] Display current goal on dashboard
- [ ] Add progress indicator
- [ ] Add goal update capability
- [ ] Test goal setting flow

### Phase 9: In-App Notification System
- [ ] Create notification channel
- [ ] Implement notification manager
- [ ] Write goal achievement check logic
- [ ] Write milestone detection logic
- [ ] Trigger notification on goal reached
- [ ] Trigger notification on milestone reached
- [ ] Design notification content
- [ ] Record achievements in database
- [ ] Test notification display
- [ ] Verify notifications trigger correctly
- [ ] Prevent duplicate notifications

### Phase 10: SMS Notification System
- [ ] Design SMS settings screen layout (XML)
- [ ] Create SmsNotificationsActivity
- [ ] Implement SMS permission handling
- [ ] Create permission rationale dialog
- [ ] Implement SmsNotificationUtils class
- [ ] Implement phone number validation
- [ ] Create SMS message templates
- [ ] Implement goal achievement SMS
- [ ] Implement milestone SMS
- [ ] Implement daily reminder SMS
- [ ] Create SmsReminderScheduler
- [ ] Create SmsReminderReceiver
- [ ] Implement preference storage
- [ ] Test SMS sending functionality
- [ ] Test permission denial handling
- [ ] Test daily reminder scheduling
- [ ] Verify duplicate prevention

### Phase 11: Testing & Refinement
- [ ] Write unit tests for database operations
- [ ] Write unit tests for validation logic
- [ ] Write unit tests for SMS utilities
- [ ] Write unit tests for password hashing
- [ ] Create integration tests
- [ ] Create UI tests for SMS settings
- [ ] Perform manual testing on device
- [ ] Test edge cases (empty data, large datasets)
- [ ] Test SMS with various phone number formats
- [ ] Fix bugs identified during testing
- [ ] Optimize performance
- [ ] Verify accessibility features

### Phase 12: Polish & Documentation
- [ ] Refine UI based on testing feedback
- [ ] Add app icon (all densities)
- [ ] Create splash screen (optional)
- [ ] Ensure consistent styling
- [ ] Verify error handling throughout
- [ ] Add code comments
- [ ] Create user documentation
- [ ] Update README with SMS features
- [ ] Prepare demo/presentation materials

### Phase 13: Deployment Preparation
- [ ] Test on multiple devices/emulators
- [ ] Verify all permissions are necessary
- [ ] Create signed APK
- [ ] Test signed APK installation
- [ ] Prepare app store listing (if publishing)
- [ ] Create screenshots for portfolio
- [ ] Document known limitations

---

## Key Metrics & Success Criteria

### Functionality Metrics
- [ ] All required features implemented and working
- [ ] Zero critical bugs
- [ ] < 5 minor bugs
- [ ] All database operations functional (5 tables)
- [ ] In-app notifications work reliably
- [ ] SMS notifications work reliably (with permission)
- [ ] Achievement tracking works correctly
- [ ] User preferences persist correctly

### Performance Metrics
- [ ] App launches in < 2 seconds
- [ ] Database queries complete in < 500ms
- [ ] No UI lag or stuttering
- [ ] Smooth scrolling (60 FPS)
- [ ] No memory leaks
- [ ] SMS sending doesn't block UI

### Code Quality Metrics
- [ ] All code properly commented
- [ ] Follows Java naming conventions
- [ ] No unused imports or variables
- [ ] Proper exception handling throughout
- [ ] Code organized logically
- [ ] Security best practices followed

### User Experience Metrics
- [ ] Intuitive navigation (no user confusion)
- [ ] Clear feedback for all actions
- [ ] Error messages are helpful
- [ ] Consistent UI design
- [ ] Accessible to all users
- [ ] SMS permission flow is clear

---

## Technical Constraints & Considerations

### Platform Constraints
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Storage**: Local SQLite only (no cloud requirements)
- **Network**: Not required for core features (offline-first app)
- **SMS**: Requires device with telephony capability

### Development Constraints
- **Timeline**: CS 360 course duration (~8 weeks)
- **Resources**: Individual developer, limited time
- **Tools**: Android Studio, Java, XML
- **Testing**: Limited to emulator and personal devices

### Design Constraints
- **Simplicity**: Keep UI simple and focused
- **Performance**: Must work smoothly on mid-range devices
- **Accessibility**: Follow Android accessibility guidelines
- **Localization**: English only (for initial version)

### SMS Constraints
- **Permission**: Requires SEND_SMS runtime permission
- **Carrier**: Dependent on user's cellular service
- **Cost**: Standard SMS rates may apply to user
- **Availability**: Not available on devices without telephony

---

## Appendix

### A. Useful Resources

**Android Development**:
- [Android Developer Documentation](https://developer.android.com/)
- [Material Design Guidelines](https://material.io/design)
- [SQLite Android Documentation](https://developer.android.com/training/data-storage/sqlite)
- [SMS Manager Documentation](https://developer.android.com/reference/android/telephony/SmsManager)
- [Runtime Permissions Guide](https://developer.android.com/training/permissions/requesting)

**Java Resources**:
- [Oracle Java Tutorials](https://docs.oracle.com/javase/tutorial/)
- [Effective Java by Joshua Bloch](https://www.oreilly.com/library/view/effective-java/9780134686097/)

**Course Materials**:
- CS 360 textbook chapters
- Android Studio tutorial resources
- Project guidelines and rubrics

### B. Android Permissions Required

```xml
<!-- AndroidManifest.xml -->

<!-- Required for in-app notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Required for SMS notifications -->
<uses-permission android:name="android.permission.SEND_SMS" />

<!-- Required for scheduling SMS reminders -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

<!-- Feature declaration for SMS -->
<uses-feature 
    android:name="android.hardware.telephony" 
    android:required="false" />
```

### C. Sample Package Structure

```
com.weighto.go/
├── activities/
│   ├── LoginActivity.java
│   ├── MainActivity.java
│   ├── AddWeightActivity.java
│   ├── GoalSettingActivity.java
│   └── SmsNotificationsActivity.java
├── database/
│   ├── WeightTrackerDBHelper.java
│   └── WeightTrackerDAO.java
├── models/
│   ├── User.java
│   ├── WeightEntry.java
│   ├── GoalWeight.java
│   ├── Achievement.java
│   └── UserPreference.java
├── adapters/
│   └── WeightHistoryAdapter.java
├── utils/
│   ├── ValidationUtils.java
│   ├── PasswordUtils.java
│   ├── SmsNotificationUtils.java
│   └── SmsReminderScheduler.java
├── notifications/
│   ├── AppNotificationManager.java
│   └── SmsNotificationManager.java
├── receivers/
│   ├── SmsReminderReceiver.java
│   └── BootReceiver.java
└── constants/
    └── AppConstants.java
```

### D. Git Commit Message Conventions

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
| 2.0 | Nov 2025 | Rick Goshen | Added SMS notification system (FR-5), expanded database to 5 tables, added UI-5 SMS settings screen, updated security requirements (SEC-5), added SMS testing requirements, updated development checklist |

---

**End of Requirements Document**

*This document will be updated throughout the development process as requirements are refined and features are implemented.*
