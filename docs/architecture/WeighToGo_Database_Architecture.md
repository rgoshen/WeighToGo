# 🎉 Weigh to Go! - SQLite Database Architecture

> **"You've got this—pound for pound."**

Complete database design and implementation guide for the Weigh to Go! Android application.

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Entity Relationship Diagram](#entity-relationship-diagram)
3. [Database Schema](#database-schema)
4. [Table Specifications](#table-specifications)
5. [Indexes & Performance](#indexes--performance)
6. [SQL Creation Scripts](#sql-creation-scripts)
7. [Java Implementation](#java-implementation)
8. [Data Access Object (DAO)](#data-access-object-dao)
9. [Common Queries](#common-queries)
10. [Migration Strategy](#migration-strategy)
11. [Security Considerations](#security-considerations)
12. [Best Practices](#best-practices)

---

## Overview

### Database Summary

| Property | Value |
|----------|-------|
| **Database Name** | `weigh_to_go.db` |
| **Version** | 1 |
| **Tables** | 5 |
| **Platform** | Android SQLite |
| **Min SDK** | 26 (Android 8.0) |

### Core Entities

| Entity | Purpose |
|--------|---------|
| `users` | User account management and authentication |
| `daily_weights` | Daily weight entries with timestamps |
| `goal_weights` | Target weight goals with tracking |
| `achievements` | Milestone achievements and notifications |
| `user_preferences` | App settings and user preferences |

### Design Principles

1. **Normalized Structure** - 3NF (Third Normal Form) to minimize redundancy
2. **Referential Integrity** - Foreign key constraints for data consistency
3. **Soft Deletes** - `is_deleted` flags preserve data history
4. **Audit Trail** - `created_at` and `updated_at` timestamps on all tables
5. **Indexing Strategy** - Optimized for common query patterns
6. **Security First** - Password hashing, no plain text storage

---

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        WEIGH TO GO! DATABASE ERD                            │
└─────────────────────────────────────────────────────────────────────────────┘

    ┌──────────────────┐
    │      users       │
    ├──────────────────┤
    │ PK user_id       │───────┐
    │    username      │       │
    │    email         │       │
    │    password_hash │       │
    │    salt          │       │
    │    display_name  │       │
    │    created_at    │       │
    │    updated_at    │       │
    │    is_active     │       │
    │    last_login    │       │
    └──────────────────┘       │
             │                 │
             │ 1:N             │ 1:N
             │                 │
             ▼                 │
    ┌──────────────────┐       │
    │  daily_weights   │       │
    ├──────────────────┤       │
    │ PK weight_id     │       │
    │ FK user_id       │───────┤
    │    weight_value  │       │
    │    weight_unit   │       │
    │    weight_date   │       │
    │    notes         │       │
    │    created_at    │       │
    │    updated_at    │       │
    │    is_deleted    │       │
    └──────────────────┘       │
                               │
             ┌─────────────────┘
             │
             │ 1:N
             ▼
    ┌──────────────────┐       ┌──────────────────┐
    │   goal_weights   │       │ user_preferences │
    ├──────────────────┤       ├──────────────────┤
    │ PK goal_id       │       │ PK preference_id │
    │ FK user_id       │───────│ FK user_id       │
    │    goal_weight   │       │    pref_key      │
    │    goal_unit     │       │    pref_value    │
    │    start_weight  │       │    created_at    │
    │    target_date   │       │    updated_at    │
    │    is_achieved   │       └──────────────────┘
    │    achieved_date │
    │    created_at    │
    │    updated_at    │
    │    is_active     │
    └──────────────────┘
             │
             │ 1:N
             ▼
    ┌──────────────────┐
    │   achievements   │
    ├──────────────────┤
    │ PK achievement_id│
    │ FK user_id       │
    │ FK goal_id       │
    │    type          │
    │    title         │
    │    description   │
    │    achieved_at   │
    │    is_notified   │
    └──────────────────┘

RELATIONSHIPS:
─────────────
users (1) ──────< (N) daily_weights    : One user has many weight entries
users (1) ──────< (N) goal_weights     : One user has many goals
users (1) ──────< (N) user_preferences : One user has many preferences
users (1) ──────< (N) achievements     : One user has many achievements
goal_weights (1) ──< (N) achievements  : One goal can have many achievements
```

---

## Database Schema

### Schema Overview

```sql
-- Database: weigh_to_go.db
-- Version: 1
-- Created: November 2025
-- Author: Rick Goshen
-- Course: CS 360 - Mobile Architecture & Programming

-- Enable foreign key support (required for SQLite)
PRAGMA foreign_keys = ON;

-- Set journal mode for better performance
PRAGMA journal_mode = WAL;

-- Set synchronous mode
PRAGMA synchronous = NORMAL;
```

---

## Table Specifications

### 1. `users` Table

Stores user account information and authentication credentials.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `user_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique user identifier |
| `username` | TEXT | NOT NULL, UNIQUE | Login username (3-30 chars) |
| `email` | TEXT | UNIQUE | Optional email address |
| `phone_number` | TEXT | | Phone number for SMS notifications |
| `password_hash` | TEXT | NOT NULL | SHA-256 hashed password |
| `salt` | TEXT | NOT NULL | Random salt for password hashing |
| `display_name` | TEXT | | User's display name |
| `created_at` | TEXT | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Account creation timestamp |
| `updated_at` | TEXT | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| `is_active` | INTEGER | NOT NULL, DEFAULT 1 | Account status (0=inactive, 1=active) |
| `last_login` | TEXT | | Last successful login timestamp |

**Business Rules:**
- Username must be unique and 3-30 characters
- Password stored as salted SHA-256 hash
- Email is optional but must be unique if provided
- Phone number stored in E.164 format (e.g., +15551234567) for SMS notifications
- Soft delete via `is_active` flag

---

### 2. `daily_weights` Table

Stores daily weight entries for tracking.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `weight_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique weight entry ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY | Reference to users table |
| `weight_value` | REAL | NOT NULL | Weight value (numeric) |
| `weight_unit` | TEXT | NOT NULL, DEFAULT 'lbs' | Unit: 'lbs' or 'kg' |
| `weight_date` | TEXT | NOT NULL | Date of entry (YYYY-MM-DD) |
| `notes` | TEXT | | Optional notes for entry |
| `created_at` | TEXT | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Entry creation timestamp |
| `updated_at` | TEXT | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| `is_deleted` | INTEGER | NOT NULL, DEFAULT 0 | Soft delete flag |

**Business Rules:**
- One entry per user per date (enforced by unique index)
- Weight must be positive (CHECK constraint)
- Weight unit must be 'lbs' or 'kg'
- Soft delete preserves history

**Indexes:**
- `idx_weights_user_date` - Composite on (user_id, weight_date)
- `idx_weights_date` - On weight_date for date range queries

---

### 3. `goal_weights` Table

Stores weight goals and targets.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `goal_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique goal ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY | Reference to users table |
| `goal_weight` | REAL | NOT NULL | Target weight value |
| `goal_unit` | TEXT | NOT NULL, DEFAULT 'lbs' | Unit: 'lbs' or 'kg' |
| `start_weight` | REAL | | Starting weight when goal was set |
| `target_date` | TEXT | | Target date to achieve goal |
| `is_achieved` | INTEGER | NOT NULL, DEFAULT 0 | Goal achieved flag |
| `achieved_date` | TEXT | | Date goal was achieved |
| `created_at` | TEXT | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Goal creation timestamp |
| `updated_at` | TEXT | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| `is_active` | INTEGER | NOT NULL, DEFAULT 1 | Active goal flag |

**Business Rules:**
- Only one active goal per user at a time
- Goal weight must be positive
- Achieved date auto-set when is_achieved = 1
- Historical goals preserved for progress tracking

---

### 4. `achievements` Table

Stores milestone achievements and celebration events.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `achievement_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique achievement ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY | Reference to users table |
| `goal_id` | INTEGER | FOREIGN KEY | Optional reference to goal |
| `achievement_type` | TEXT | NOT NULL | Type of achievement |
| `title` | TEXT | NOT NULL | Achievement title |
| `description` | TEXT | | Detailed description |
| `value` | REAL | | Associated value (e.g., pounds lost) |
| `achieved_at` | TEXT | NOT NULL, DEFAULT CURRENT_TIMESTAMP | When achieved |
| `is_notified` | INTEGER | NOT NULL, DEFAULT 0 | Notification sent flag |

**Achievement Types:**
- `GOAL_REACHED` - Target weight achieved
- `FIRST_ENTRY` - First weight logged
- `STREAK_7` - 7-day logging streak
- `STREAK_30` - 30-day logging streak
- `MILESTONE_5` - Lost 5 lbs/kg
- `MILESTONE_10` - Lost 10 lbs/kg
- `MILESTONE_25` - Lost 25 lbs/kg
- `MILESTONE_50` - Lost 50 lbs/kg
- `NEW_LOW` - New lowest weight

---

### 5. `user_preferences` Table

Stores user settings and preferences.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `preference_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique preference ID |
| `user_id` | INTEGER | NOT NULL, FOREIGN KEY | Reference to users table |
| `pref_key` | TEXT | NOT NULL | Preference key name |
| `pref_value` | TEXT | NOT NULL | Preference value |
| `created_at` | TEXT | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| `updated_at` | TEXT | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Standard Preference Keys:**
- `weight_unit` - Default weight unit ('lbs' or 'kg')
- `theme` - App theme ('light', 'dark', 'system')
- `notifications_enabled` - Enable push notifications ('true', 'false')
- `sms_notifications_enabled` - Enable SMS notifications ('true', 'false')
- `sms_goal_alerts` - Send SMS when goal reached ('true', 'false')
- `sms_reminder_enabled` - Send daily reminder via SMS ('true', 'false')
- `sms_milestone_alerts` - Send SMS for weight milestones ('true', 'false')
- `reminder_time` - Daily reminder time ('HH:MM')
- `first_day_of_week` - Week start day ('sunday', 'monday')
- `date_format` - Date display format

**Unique Constraint:** (user_id, pref_key)

---

## Indexes & Performance

### Index Strategy

```sql
-- Users table indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
CREATE INDEX idx_users_active ON users(is_active);

-- Daily weights table indexes (most critical for performance)
CREATE UNIQUE INDEX idx_weights_user_date ON daily_weights(user_id, weight_date) 
    WHERE is_deleted = 0;
CREATE INDEX idx_weights_date ON daily_weights(weight_date);
CREATE INDEX idx_weights_user_created ON daily_weights(user_id, created_at DESC);

-- Goal weights table indexes
CREATE INDEX idx_goals_user_active ON goal_weights(user_id, is_active);
CREATE INDEX idx_goals_achieved ON goal_weights(is_achieved);

-- Achievements table indexes
CREATE INDEX idx_achievements_user ON achievements(user_id);
CREATE INDEX idx_achievements_unnotified ON achievements(user_id, is_notified) 
    WHERE is_notified = 0;
CREATE INDEX idx_achievements_type ON achievements(achievement_type);

-- User preferences table indexes
CREATE UNIQUE INDEX idx_prefs_user_key ON user_preferences(user_id, pref_key);
```

### Query Optimization Notes

| Query Pattern | Optimized By |
|--------------|--------------|
| Login lookup | `idx_users_username` |
| Get user's weights by date range | `idx_weights_user_date` |
| Get latest weight | `idx_weights_user_created` |
| Get active goal | `idx_goals_user_active` |
| Get pending notifications | `idx_achievements_unnotified` |
| Get user preference | `idx_prefs_user_key` |

---

## SQL Creation Scripts

### Complete Database Creation Script

```sql
-- ============================================================================
-- WEIGH TO GO! DATABASE CREATION SCRIPT
-- Version: 1
-- ============================================================================

-- Enable foreign keys
PRAGMA foreign_keys = ON;

-- ============================================================================
-- TABLE: users
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    user_id         INTEGER PRIMARY KEY AUTOINCREMENT,
    username        TEXT    NOT NULL UNIQUE,
    email           TEXT    UNIQUE,
    phone_number    TEXT,
    password_hash   TEXT    NOT NULL,
    salt            TEXT    NOT NULL,
    display_name    TEXT,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    is_active       INTEGER NOT NULL DEFAULT 1,
    last_login      TEXT,
    
    -- Constraints
    CHECK (length(username) >= 3 AND length(username) <= 30),
    CHECK (is_active IN (0, 1))
);

-- ============================================================================
-- TABLE: daily_weights
-- ============================================================================
CREATE TABLE IF NOT EXISTS daily_weights (
    weight_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL,
    weight_value    REAL    NOT NULL,
    weight_unit     TEXT    NOT NULL DEFAULT 'lbs',
    weight_date     TEXT    NOT NULL,
    notes           TEXT,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    is_deleted      INTEGER NOT NULL DEFAULT 0,
    
    -- Foreign Keys
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Constraints
    CHECK (weight_value > 0),
    CHECK (weight_value < 1500), -- Reasonable max weight
    CHECK (weight_unit IN ('lbs', 'kg')),
    CHECK (is_deleted IN (0, 1)),
    CHECK (weight_date LIKE '____-__-__') -- YYYY-MM-DD format
);

-- ============================================================================
-- TABLE: goal_weights
-- ============================================================================
CREATE TABLE IF NOT EXISTS goal_weights (
    goal_id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL,
    goal_weight     REAL    NOT NULL,
    goal_unit       TEXT    NOT NULL DEFAULT 'lbs',
    start_weight    REAL,
    target_date     TEXT,
    is_achieved     INTEGER NOT NULL DEFAULT 0,
    achieved_date   TEXT,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    is_active       INTEGER NOT NULL DEFAULT 1,
    
    -- Foreign Keys
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Constraints
    CHECK (goal_weight > 0),
    CHECK (goal_weight < 1500),
    CHECK (goal_unit IN ('lbs', 'kg')),
    CHECK (is_achieved IN (0, 1)),
    CHECK (is_active IN (0, 1)),
    CHECK (start_weight IS NULL OR start_weight > 0)
);

-- ============================================================================
-- TABLE: achievements
-- ============================================================================
CREATE TABLE IF NOT EXISTS achievements (
    achievement_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id           INTEGER NOT NULL,
    goal_id           INTEGER,
    achievement_type  TEXT    NOT NULL,
    title             TEXT    NOT NULL,
    description       TEXT,
    value             REAL,
    achieved_at       TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    is_notified       INTEGER NOT NULL DEFAULT 0,
    
    -- Foreign Keys
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (goal_id) REFERENCES goal_weights(goal_id) ON DELETE SET NULL,
    
    -- Constraints
    CHECK (achievement_type IN (
        'GOAL_REACHED', 'FIRST_ENTRY', 'STREAK_7', 'STREAK_30',
        'MILESTONE_5', 'MILESTONE_10', 'MILESTONE_25', 'MILESTONE_50',
        'NEW_LOW'
    )),
    CHECK (is_notified IN (0, 1))
);

-- ============================================================================
-- TABLE: user_preferences
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_preferences (
    preference_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL,
    pref_key        TEXT    NOT NULL,
    pref_value      TEXT    NOT NULL,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    
    -- Foreign Keys
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Unique constraint for user + key combination
    UNIQUE (user_id, pref_key)
);

-- ============================================================================
-- INDEXES
-- ============================================================================

-- Users indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email) WHERE email IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);

-- Daily weights indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_weights_user_date 
    ON daily_weights(user_id, weight_date) WHERE is_deleted = 0;
CREATE INDEX IF NOT EXISTS idx_weights_date ON daily_weights(weight_date);
CREATE INDEX IF NOT EXISTS idx_weights_user_created 
    ON daily_weights(user_id, created_at DESC);

-- Goal weights indexes
CREATE INDEX IF NOT EXISTS idx_goals_user_active ON goal_weights(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_goals_achieved ON goal_weights(is_achieved);

-- Achievements indexes
CREATE INDEX IF NOT EXISTS idx_achievements_user ON achievements(user_id);
CREATE INDEX IF NOT EXISTS idx_achievements_unnotified 
    ON achievements(user_id, is_notified) WHERE is_notified = 0;
CREATE INDEX IF NOT EXISTS idx_achievements_type ON achievements(achievement_type);

-- User preferences indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_prefs_user_key 
    ON user_preferences(user_id, pref_key);

-- ============================================================================
-- TRIGGERS
-- ============================================================================

-- Auto-update updated_at timestamp for users
CREATE TRIGGER IF NOT EXISTS trg_users_updated_at
    AFTER UPDATE ON users
    FOR EACH ROW
BEGIN
    UPDATE users SET updated_at = datetime('now', 'localtime')
    WHERE user_id = NEW.user_id;
END;

-- Auto-update updated_at timestamp for daily_weights
CREATE TRIGGER IF NOT EXISTS trg_weights_updated_at
    AFTER UPDATE ON daily_weights
    FOR EACH ROW
BEGIN
    UPDATE daily_weights SET updated_at = datetime('now', 'localtime')
    WHERE weight_id = NEW.weight_id;
END;

-- Auto-update updated_at timestamp for goal_weights
CREATE TRIGGER IF NOT EXISTS trg_goals_updated_at
    AFTER UPDATE ON goal_weights
    FOR EACH ROW
BEGIN
    UPDATE goal_weights SET updated_at = datetime('now', 'localtime')
    WHERE goal_id = NEW.goal_id;
END;

-- Auto-set achieved_date when goal is achieved
CREATE TRIGGER IF NOT EXISTS trg_goal_achieved
    AFTER UPDATE OF is_achieved ON goal_weights
    FOR EACH ROW
    WHEN NEW.is_achieved = 1 AND OLD.is_achieved = 0
BEGIN
    UPDATE goal_weights 
    SET achieved_date = datetime('now', 'localtime'),
        is_active = 0
    WHERE goal_id = NEW.goal_id;
END;

-- Auto-update updated_at timestamp for user_preferences
CREATE TRIGGER IF NOT EXISTS trg_prefs_updated_at
    AFTER UPDATE ON user_preferences
    FOR EACH ROW
BEGIN
    UPDATE user_preferences SET updated_at = datetime('now', 'localtime')
    WHERE preference_id = NEW.preference_id;
END;

-- ============================================================================
-- VIEWS (Optional - for common queries)
-- ============================================================================

-- View: User's latest weight
CREATE VIEW IF NOT EXISTS v_user_latest_weight AS
SELECT 
    u.user_id,
    u.username,
    u.display_name,
    dw.weight_value,
    dw.weight_unit,
    dw.weight_date,
    dw.created_at
FROM users u
LEFT JOIN daily_weights dw ON u.user_id = dw.user_id AND dw.is_deleted = 0
WHERE dw.weight_date = (
    SELECT MAX(dw2.weight_date) 
    FROM daily_weights dw2 
    WHERE dw2.user_id = u.user_id AND dw2.is_deleted = 0
);

-- View: User progress summary
CREATE VIEW IF NOT EXISTS v_user_progress AS
SELECT 
    u.user_id,
    u.username,
    g.goal_weight,
    g.start_weight,
    (SELECT MIN(weight_value) FROM daily_weights WHERE user_id = u.user_id AND is_deleted = 0) AS lowest_weight,
    (SELECT MAX(weight_value) FROM daily_weights WHERE user_id = u.user_id AND is_deleted = 0) AS highest_weight,
    (SELECT weight_value FROM daily_weights WHERE user_id = u.user_id AND is_deleted = 0 ORDER BY weight_date DESC LIMIT 1) AS current_weight,
    (SELECT COUNT(*) FROM daily_weights WHERE user_id = u.user_id AND is_deleted = 0) AS total_entries,
    (SELECT COUNT(*) FROM achievements WHERE user_id = u.user_id) AS total_achievements
FROM users u
LEFT JOIN goal_weights g ON u.user_id = g.user_id AND g.is_active = 1
WHERE u.is_active = 1;

-- ============================================================================
-- END OF SCRIPT
-- ============================================================================
```

---

## Java Implementation

### Database Helper Class

```java
package com.rickgoshen.weightogo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite Database Helper for Weigh to Go! application.
 * Handles database creation, upgrades, and provides database access.
 * 
 * @author Rick Goshen
 * @version 1.0
 */
public class WeighToGoDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "WeighToGoDBHelper";
    
    // Database Info
    private static final String DATABASE_NAME = "weigh_to_go.db";
    private static final int DATABASE_VERSION = 1;
    
    // Singleton instance
    private static WeighToGoDBHelper instance;
    
    // ========================================================================
    // TABLE NAMES
    // ========================================================================
    public static final String TABLE_USERS = "users";
    public static final String TABLE_DAILY_WEIGHTS = "daily_weights";
    public static final String TABLE_GOAL_WEIGHTS = "goal_weights";
    public static final String TABLE_ACHIEVEMENTS = "achievements";
    public static final String TABLE_USER_PREFERENCES = "user_preferences";
    
    // ========================================================================
    // USERS TABLE COLUMNS
    // ========================================================================
    public static final String COL_USER_ID = "user_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE_NUMBER = "phone_number";
    public static final String COL_PASSWORD_HASH = "password_hash";
    public static final String COL_SALT = "salt";
    public static final String COL_DISPLAY_NAME = "display_name";
    public static final String COL_IS_ACTIVE = "is_active";
    public static final String COL_LAST_LOGIN = "last_login";
    
    // ========================================================================
    // DAILY WEIGHTS TABLE COLUMNS
    // ========================================================================
    public static final String COL_WEIGHT_ID = "weight_id";
    public static final String COL_WEIGHT_VALUE = "weight_value";
    public static final String COL_WEIGHT_UNIT = "weight_unit";
    public static final String COL_WEIGHT_DATE = "weight_date";
    public static final String COL_NOTES = "notes";
    public static final String COL_IS_DELETED = "is_deleted";
    
    // ========================================================================
    // GOAL WEIGHTS TABLE COLUMNS
    // ========================================================================
    public static final String COL_GOAL_ID = "goal_id";
    public static final String COL_GOAL_WEIGHT = "goal_weight";
    public static final String COL_GOAL_UNIT = "goal_unit";
    public static final String COL_START_WEIGHT = "start_weight";
    public static final String COL_TARGET_DATE = "target_date";
    public static final String COL_IS_ACHIEVED = "is_achieved";
    public static final String COL_ACHIEVED_DATE = "achieved_date";
    
    // ========================================================================
    // ACHIEVEMENTS TABLE COLUMNS
    // ========================================================================
    public static final String COL_ACHIEVEMENT_ID = "achievement_id";
    public static final String COL_ACHIEVEMENT_TYPE = "achievement_type";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_VALUE = "value";
    public static final String COL_ACHIEVED_AT = "achieved_at";
    public static final String COL_IS_NOTIFIED = "is_notified";
    
    // ========================================================================
    // USER PREFERENCES TABLE COLUMNS
    // ========================================================================
    public static final String COL_PREFERENCE_ID = "preference_id";
    public static final String COL_PREF_KEY = "pref_key";
    public static final String COL_PREF_VALUE = "pref_value";
    
    // ========================================================================
    // COMMON COLUMNS
    // ========================================================================
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";
    
    // ========================================================================
    // WEIGHT UNITS
    // ========================================================================
    public static final String UNIT_LBS = "lbs";
    public static final String UNIT_KG = "kg";
    
    // ========================================================================
    // ACHIEVEMENT TYPES
    // ========================================================================
    public static final String ACHIEVEMENT_GOAL_REACHED = "GOAL_REACHED";
    public static final String ACHIEVEMENT_FIRST_ENTRY = "FIRST_ENTRY";
    public static final String ACHIEVEMENT_STREAK_7 = "STREAK_7";
    public static final String ACHIEVEMENT_STREAK_30 = "STREAK_30";
    public static final String ACHIEVEMENT_MILESTONE_5 = "MILESTONE_5";
    public static final String ACHIEVEMENT_MILESTONE_10 = "MILESTONE_10";
    public static final String ACHIEVEMENT_MILESTONE_25 = "MILESTONE_25";
    public static final String ACHIEVEMENT_MILESTONE_50 = "MILESTONE_50";
    public static final String ACHIEVEMENT_NEW_LOW = "NEW_LOW";
    
    // ========================================================================
    // PREFERENCE KEYS
    // ========================================================================
    public static final String PREF_WEIGHT_UNIT = "weight_unit";
    public static final String PREF_THEME = "theme";
    public static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String PREF_SMS_NOTIFICATIONS_ENABLED = "sms_notifications_enabled";
    public static final String PREF_SMS_GOAL_ALERTS = "sms_goal_alerts";
    public static final String PREF_SMS_REMINDER_ENABLED = "sms_reminder_enabled";
    public static final String PREF_SMS_MILESTONE_ALERTS = "sms_milestone_alerts";
    public static final String PREF_REMINDER_TIME = "reminder_time";
    public static final String PREF_FIRST_DAY_OF_WEEK = "first_day_of_week";
    public static final String PREF_DATE_FORMAT = "date_format";

    // ========================================================================
    // CREATE TABLE STATEMENTS
    // ========================================================================
    
    private static final String CREATE_TABLE_USERS = 
        "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
        COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
        COL_EMAIL + " TEXT UNIQUE, " +
        COL_PHONE_NUMBER + " TEXT, " +
        COL_PASSWORD_HASH + " TEXT NOT NULL, " +
        COL_SALT + " TEXT NOT NULL, " +
        COL_DISPLAY_NAME + " TEXT, " +
        COL_CREATED_AT + " TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), " +
        COL_UPDATED_AT + " TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), " +
        COL_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1, " +
        COL_LAST_LOGIN + " TEXT, " +
        "CHECK (length(" + COL_USERNAME + ") >= 3 AND length(" + COL_USERNAME + ") <= 30), " +
        "CHECK (" + COL_IS_ACTIVE + " IN (0, 1))" +
        ");";
    
    private static final String CREATE_TABLE_DAILY_WEIGHTS = 
        "CREATE TABLE IF NOT EXISTS " + TABLE_DAILY_WEIGHTS + " (" +
        COL_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_USER_ID + " INTEGER NOT NULL, " +
        COL_WEIGHT_VALUE + " REAL NOT NULL, " +
        COL_WEIGHT_UNIT + " TEXT NOT NULL DEFAULT 'lbs', " +
        COL_WEIGHT_DATE + " TEXT NOT NULL, " +
        COL_NOTES + " TEXT, " +
        COL_CREATED_AT + " TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), " +
        COL_UPDATED_AT + " TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), " +
        COL_IS_DELETED + " INTEGER NOT NULL DEFAULT 0, " +
        "FOREIGN KEY (" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
        "CHECK (" + COL_WEIGHT_VALUE + " > 0), " +
        "CHECK (" + COL_WEIGHT_VALUE + " < 1500), " +
        "CHECK (" + COL_WEIGHT_UNIT + " IN ('lbs', 'kg')), " +
        "CHECK (" + COL_IS_DELETED + " IN (0, 1))" +
        ");";
    
    private static final String CREATE_TABLE_GOAL_WEIGHTS = 
        "CREATE TABLE IF NOT EXISTS " + TABLE_GOAL_WEIGHTS + " (" +
        COL_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_USER_ID + " INTEGER NOT NULL, " +
        COL_GOAL_WEIGHT + " REAL NOT NULL, " +
        COL_GOAL_UNIT + " TEXT NOT NULL DEFAULT 'lbs', " +
        COL_START_WEIGHT + " REAL, " +
        COL_TARGET_DATE + " TEXT, " +
        COL_IS_ACHIEVED + " INTEGER NOT NULL DEFAULT 0, " +
        COL_ACHIEVED_DATE + " TEXT, " +
        COL_CREATED_AT + " TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), " +
        COL_UPDATED_AT + " TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), " +
        COL_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1, " +
        "FOREIGN KEY (" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
        "CHECK (" + COL_GOAL_WEIGHT + " > 0), " +
        "CHECK (" + COL_GOAL_WEIGHT + " < 1500), " +
        "CHECK (" + COL_GOAL_UNIT + " IN ('lbs', 'kg')), " +
        "CHECK (" + COL_IS_ACHIEVED + " IN (0, 1)), " +
        "CHECK (" + COL_IS_ACTIVE + " IN (0, 1))" +
        ");";
    
    private static final String CREATE_TABLE_ACHIEVEMENTS = 
        "CREATE TABLE IF NOT EXISTS " + TABLE_ACHIEVEMENTS + " (" +
        COL_ACHIEVEMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_USER_ID + " INTEGER NOT NULL, " +
        COL_GOAL_ID + " INTEGER, " +
        COL_ACHIEVEMENT_TYPE + " TEXT NOT NULL, " +
        COL_TITLE + " TEXT NOT NULL, " +
        COL_DESCRIPTION + " TEXT, " +
        COL_VALUE + " REAL, " +
        COL_ACHIEVED_AT + " TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), " +
        COL_IS_NOTIFIED + " INTEGER NOT NULL DEFAULT 0, " +
        "FOREIGN KEY (" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
        "FOREIGN KEY (" + COL_GOAL_ID + ") REFERENCES " + TABLE_GOAL_WEIGHTS + "(" + COL_GOAL_ID + ") ON DELETE SET NULL, " +
        "CHECK (" + COL_IS_NOTIFIED + " IN (0, 1))" +
        ");";
    
    private static final String CREATE_TABLE_USER_PREFERENCES = 
        "CREATE TABLE IF NOT EXISTS " + TABLE_USER_PREFERENCES + " (" +
        COL_PREFERENCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_USER_ID + " INTEGER NOT NULL, " +
        COL_PREF_KEY + " TEXT NOT NULL, " +
        COL_PREF_VALUE + " TEXT NOT NULL, " +
        COL_CREATED_AT + " TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), " +
        COL_UPDATED_AT + " TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), " +
        "FOREIGN KEY (" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
        "UNIQUE (" + COL_USER_ID + ", " + COL_PREF_KEY + ")" +
        ");";

    // ========================================================================
    // INDEX CREATION STATEMENTS
    // ========================================================================
    
    private static final String[] CREATE_INDEXES = {
        // Users indexes
        "CREATE INDEX IF NOT EXISTS idx_users_username ON " + TABLE_USERS + "(" + COL_USERNAME + ");",
        "CREATE INDEX IF NOT EXISTS idx_users_email ON " + TABLE_USERS + "(" + COL_EMAIL + ") WHERE " + COL_EMAIL + " IS NOT NULL;",
        "CREATE INDEX IF NOT EXISTS idx_users_active ON " + TABLE_USERS + "(" + COL_IS_ACTIVE + ");",
        
        // Daily weights indexes
        "CREATE UNIQUE INDEX IF NOT EXISTS idx_weights_user_date ON " + TABLE_DAILY_WEIGHTS + 
            "(" + COL_USER_ID + ", " + COL_WEIGHT_DATE + ") WHERE " + COL_IS_DELETED + " = 0;",
        "CREATE INDEX IF NOT EXISTS idx_weights_date ON " + TABLE_DAILY_WEIGHTS + "(" + COL_WEIGHT_DATE + ");",
        "CREATE INDEX IF NOT EXISTS idx_weights_user_created ON " + TABLE_DAILY_WEIGHTS + 
            "(" + COL_USER_ID + ", " + COL_CREATED_AT + " DESC);",
        
        // Goal weights indexes
        "CREATE INDEX IF NOT EXISTS idx_goals_user_active ON " + TABLE_GOAL_WEIGHTS + 
            "(" + COL_USER_ID + ", " + COL_IS_ACTIVE + ");",
        "CREATE INDEX IF NOT EXISTS idx_goals_achieved ON " + TABLE_GOAL_WEIGHTS + "(" + COL_IS_ACHIEVED + ");",
        
        // Achievements indexes
        "CREATE INDEX IF NOT EXISTS idx_achievements_user ON " + TABLE_ACHIEVEMENTS + "(" + COL_USER_ID + ");",
        "CREATE INDEX IF NOT EXISTS idx_achievements_unnotified ON " + TABLE_ACHIEVEMENTS + 
            "(" + COL_USER_ID + ", " + COL_IS_NOTIFIED + ") WHERE " + COL_IS_NOTIFIED + " = 0;",
        "CREATE INDEX IF NOT EXISTS idx_achievements_type ON " + TABLE_ACHIEVEMENTS + "(" + COL_ACHIEVEMENT_TYPE + ");",
        
        // User preferences indexes
        "CREATE UNIQUE INDEX IF NOT EXISTS idx_prefs_user_key ON " + TABLE_USER_PREFERENCES + 
            "(" + COL_USER_ID + ", " + COL_PREF_KEY + ");"
    };

    // ========================================================================
    // CONSTRUCTOR & SINGLETON
    // ========================================================================
    
    /**
     * Private constructor for singleton pattern.
     */
    private WeighToGoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    /**
     * Get singleton instance of database helper.
     * 
     * @param context Application context
     * @return Database helper instance
     */
    public static synchronized WeighToGoDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new WeighToGoDBHelper(context.getApplicationContext());
        }
        return instance;
    }

    // ========================================================================
    // DATABASE LIFECYCLE METHODS
    // ========================================================================
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database...");
        
        // Enable foreign keys
        db.execSQL("PRAGMA foreign_keys = ON;");
        
        // Create tables
        db.execSQL(CREATE_TABLE_USERS);
        Log.d(TAG, "Created table: " + TABLE_USERS);
        
        db.execSQL(CREATE_TABLE_DAILY_WEIGHTS);
        Log.d(TAG, "Created table: " + TABLE_DAILY_WEIGHTS);
        
        db.execSQL(CREATE_TABLE_GOAL_WEIGHTS);
        Log.d(TAG, "Created table: " + TABLE_GOAL_WEIGHTS);
        
        db.execSQL(CREATE_TABLE_ACHIEVEMENTS);
        Log.d(TAG, "Created table: " + TABLE_ACHIEVEMENTS);
        
        db.execSQL(CREATE_TABLE_USER_PREFERENCES);
        Log.d(TAG, "Created table: " + TABLE_USER_PREFERENCES);
        
        // Create indexes
        for (String indexSql : CREATE_INDEXES) {
            db.execSQL(indexSql);
        }
        Log.d(TAG, "Created " + CREATE_INDEXES.length + " indexes");
        
        Log.d(TAG, "Database creation complete!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        // Handle migrations based on version
        if (oldVersion < 2) {
            // Future: Migration from v1 to v2
            // Example: db.execSQL("ALTER TABLE users ADD COLUMN new_column TEXT;");
        }
        
        // Add more version checks as needed
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Downgrading database from version " + oldVersion + " to " + newVersion);
        // Handle downgrade if necessary (usually recreate)
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign keys on each connection
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================
    
    /**
     * Get current timestamp in SQLite format.
     * 
     * @return Current timestamp string
     */
    public static String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
            java.util.Locale.getDefault()).format(new java.util.Date());
    }
    
    /**
     * Get current date in SQLite format.
     * 
     * @return Current date string (YYYY-MM-DD)
     */
    public static String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", 
            java.util.Locale.getDefault()).format(new java.util.Date());
    }
    
    /**
     * Convert weight between units.
     * 
     * @param value Weight value
     * @param fromUnit Source unit
     * @param toUnit Target unit
     * @return Converted weight value
     */
    public static double convertWeight(double value, String fromUnit, String toUnit) {
        if (fromUnit.equals(toUnit)) {
            return value;
        }
        
        if (UNIT_LBS.equals(fromUnit) && UNIT_KG.equals(toUnit)) {
            return value * 0.453592; // lbs to kg
        } else if (UNIT_KG.equals(fromUnit) && UNIT_LBS.equals(toUnit)) {
            return value * 2.20462; // kg to lbs
        }
        
        return value; // Unknown conversion, return original
    }
}
```

---

## Data Access Object (DAO)

### User DAO Interface & Implementation

```java
package com.rickgoshen.weightogo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.rickgoshen.weightogo.models.User;
import com.rickgoshen.weightogo.utils.PasswordUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User operations.
 */
public class UserDAO {
    
    private static final String TAG = "UserDAO";
    private final WeighToGoDBHelper dbHelper;
    
    public UserDAO(Context context) {
        this.dbHelper = WeighToGoDBHelper.getInstance(context);
    }
    
    /**
     * Create a new user account.
     * 
     * @param username Username
     * @param password Plain text password (will be hashed)
     * @param email Optional email
     * @param displayName Optional display name
     * @return User ID if successful, -1 if failed
     */
    public long createUser(String username, String password, String email, String displayName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            // Generate salt and hash password
            String salt = PasswordUtils.generateSalt();
            String passwordHash = PasswordUtils.hashPassword(password, salt);
            
            ContentValues values = new ContentValues();
            values.put(WeighToGoDBHelper.COL_USERNAME, username.toLowerCase().trim());
            values.put(WeighToGoDBHelper.COL_PASSWORD_HASH, passwordHash);
            values.put(WeighToGoDBHelper.COL_SALT, salt);
            
            if (email != null && !email.isEmpty()) {
                values.put(WeighToGoDBHelper.COL_EMAIL, email.toLowerCase().trim());
            }
            
            if (displayName != null && !displayName.isEmpty()) {
                values.put(WeighToGoDBHelper.COL_DISPLAY_NAME, displayName.trim());
            }
            
            long userId = db.insertOrThrow(WeighToGoDBHelper.TABLE_USERS, null, values);
            Log.d(TAG, "Created user with ID: " + userId);
            return userId;
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating user: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Authenticate user with username and password.
     * 
     * @param username Username
     * @param password Plain text password
     * @return User object if authenticated, null if failed
     */
    public User authenticate(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String[] columns = {
            WeighToGoDBHelper.COL_USER_ID,
            WeighToGoDBHelper.COL_USERNAME,
            WeighToGoDBHelper.COL_EMAIL,
            WeighToGoDBHelper.COL_PASSWORD_HASH,
            WeighToGoDBHelper.COL_SALT,
            WeighToGoDBHelper.COL_DISPLAY_NAME,
            WeighToGoDBHelper.COL_IS_ACTIVE
        };
        
        String selection = WeighToGoDBHelper.COL_USERNAME + " = ? AND " + 
                          WeighToGoDBHelper.COL_IS_ACTIVE + " = 1";
        String[] selectionArgs = { username.toLowerCase().trim() };
        
        Cursor cursor = db.query(
            WeighToGoDBHelper.TABLE_USERS,
            columns,
            selection,
            selectionArgs,
            null, null, null
        );
        
        try {
            if (cursor.moveToFirst()) {
                String storedHash = cursor.getString(
                    cursor.getColumnIndexOrThrow(WeighToGoDBHelper.COL_PASSWORD_HASH));
                String salt = cursor.getString(
                    cursor.getColumnIndexOrThrow(WeighToGoDBHelper.COL_SALT));
                
                // Verify password
                if (PasswordUtils.verifyPassword(password, salt, storedHash)) {
                    User user = cursorToUser(cursor);
                    
                    // Update last login
                    updateLastLogin(user.getUserId());
                    
                    Log.d(TAG, "User authenticated: " + username);
                    return user;
                }
            }
            
            Log.d(TAG, "Authentication failed for: " + username);
            return null;
            
        } finally {
            cursor.close();
        }
    }
    
    /**
     * Get user by ID.
     */
    public User getUserById(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = WeighToGoDBHelper.COL_USER_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };
        
        Cursor cursor = db.query(
            WeighToGoDBHelper.TABLE_USERS,
            null,
            selection,
            selectionArgs,
            null, null, null
        );
        
        try {
            if (cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }
    
    /**
     * Check if username exists.
     */
    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = WeighToGoDBHelper.COL_USERNAME + " = ?";
        String[] selectionArgs = { username.toLowerCase().trim() };
        
        Cursor cursor = db.query(
            WeighToGoDBHelper.TABLE_USERS,
            new String[] { WeighToGoDBHelper.COL_USER_ID },
            selection,
            selectionArgs,
            null, null, null
        );
        
        try {
            return cursor.getCount() > 0;
        } finally {
            cursor.close();
        }
    }
    
    /**
     * Update last login timestamp.
     */
    private void updateLastLogin(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(WeighToGoDBHelper.COL_LAST_LOGIN, WeighToGoDBHelper.getCurrentTimestamp());
        
        db.update(
            WeighToGoDBHelper.TABLE_USERS,
            values,
            WeighToGoDBHelper.COL_USER_ID + " = ?",
            new String[] { String.valueOf(userId) }
        );
    }
    
    /**
     * Convert cursor row to User object.
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(WeighToGoDBHelper.COL_USER_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(WeighToGoDBHelper.COL_USERNAME)));
        
        int emailIndex = cursor.getColumnIndex(WeighToGoDBHelper.COL_EMAIL);
        if (emailIndex >= 0 && !cursor.isNull(emailIndex)) {
            user.setEmail(cursor.getString(emailIndex));
        }
        
        int displayNameIndex = cursor.getColumnIndex(WeighToGoDBHelper.COL_DISPLAY_NAME);
        if (displayNameIndex >= 0 && !cursor.isNull(displayNameIndex)) {
            user.setDisplayName(cursor.getString(displayNameIndex));
        }
        
        return user;
    }
}
```

### Weight Entry DAO

```java
package com.rickgoshen.weightogo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.rickgoshen.weightogo.models.WeightEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Weight Entry operations.
 */
public class WeightEntryDAO {
    
    private static final String TAG = "WeightEntryDAO";
    private final WeighToGoDBHelper dbHelper;
    
    public WeightEntryDAO(Context context) {
        this.dbHelper = WeighToGoDBHelper.getInstance(context);
    }
    
    /**
     * Add or update a weight entry for a specific date.
     * If an entry already exists for the date, it will be updated.
     * 
     * @param userId User ID
     * @param weightValue Weight value
     * @param weightUnit Weight unit ('lbs' or 'kg')
     * @param weightDate Date (YYYY-MM-DD format)
     * @param notes Optional notes
     * @return Weight entry ID
     */
    public long saveWeightEntry(long userId, double weightValue, String weightUnit, 
                                 String weightDate, String notes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // Check if entry exists for this date
        WeightEntry existing = getWeightByDate(userId, weightDate);
        
        ContentValues values = new ContentValues();
        values.put(WeighToGoDBHelper.COL_USER_ID, userId);
        values.put(WeighToGoDBHelper.COL_WEIGHT_VALUE, weightValue);
        values.put(WeighToGoDBHelper.COL_WEIGHT_UNIT, weightUnit);
        values.put(WeighToGoDBHelper.COL_WEIGHT_DATE, weightDate);
        values.put(WeighToGoDBHelper.COL_NOTES, notes);
        values.put(WeighToGoDBHelper.COL_IS_DELETED, 0);
        
        if (existing != null) {
            // Update existing entry
            values.put(WeighToGoDBHelper.COL_UPDATED_AT, WeighToGoDBHelper.getCurrentTimestamp());
            
            db.update(
                WeighToGoDBHelper.TABLE_DAILY_WEIGHTS,
                values,
                WeighToGoDBHelper.COL_WEIGHT_ID + " = ?",
                new String[] { String.valueOf(existing.getWeightId()) }
            );
            
            Log.d(TAG, "Updated weight entry ID: " + existing.getWeightId());
            return existing.getWeightId();
        } else {
            // Insert new entry
            long weightId = db.insert(WeighToGoDBHelper.TABLE_DAILY_WEIGHTS, null, values);
            Log.d(TAG, "Created weight entry ID: " + weightId);
            return weightId;
        }
    }
    
    /**
     * Get weight entry for a specific date.
     */
    public WeightEntry getWeightByDate(long userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = WeighToGoDBHelper.COL_USER_ID + " = ? AND " +
                          WeighToGoDBHelper.COL_WEIGHT_DATE + " = ? AND " +
                          WeighToGoDBHelper.COL_IS_DELETED + " = 0";
        String[] selectionArgs = { String.valueOf(userId), date };
        
        Cursor cursor = db.query(
            WeighToGoDBHelper.TABLE_DAILY_WEIGHTS,
            null,
            selection,
            selectionArgs,
            null, null, null
        );
        
        try {
            if (cursor.moveToFirst()) {
                return cursorToWeightEntry(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }
    
    /**
     * Get user's latest weight entry.
     */
    public WeightEntry getLatestWeight(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = WeighToGoDBHelper.COL_USER_ID + " = ? AND " +
                          WeighToGoDBHelper.COL_IS_DELETED + " = 0";
        String[] selectionArgs = { String.valueOf(userId) };
        String orderBy = WeighToGoDBHelper.COL_WEIGHT_DATE + " DESC";
        
        Cursor cursor = db.query(
            WeighToGoDBHelper.TABLE_DAILY_WEIGHTS,
            null,
            selection,
            selectionArgs,
            null, null, orderBy, "1"
        );
        
        try {
            if (cursor.moveToFirst()) {
                return cursorToWeightEntry(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }
    
    /**
     * Get weight entries for a date range.
     */
    public List<WeightEntry> getWeightsByDateRange(long userId, String startDate, String endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<WeightEntry> entries = new ArrayList<>();
        
        String selection = WeighToGoDBHelper.COL_USER_ID + " = ? AND " +
                          WeighToGoDBHelper.COL_WEIGHT_DATE + " >= ? AND " +
                          WeighToGoDBHelper.COL_WEIGHT_DATE + " <= ? AND " +
                          WeighToGoDBHelper.COL_IS_DELETED + " = 0";
        String[] selectionArgs = { String.valueOf(userId), startDate, endDate };
        String orderBy = WeighToGoDBHelper.COL_WEIGHT_DATE + " DESC";
        
        Cursor cursor = db.query(
            WeighToGoDBHelper.TABLE_DAILY_WEIGHTS,
            null,
            selection,
            selectionArgs,
            null, null, orderBy
        );
        
        try {
            while (cursor.moveToNext()) {
                entries.add(cursorToWeightEntry(cursor));
            }
        } finally {
            cursor.close();
        }
        
        return entries;
    }
    
    /**
     * Get all weight entries for a user (paginated).
     */
    public List<WeightEntry> getAllWeights(long userId, int limit, int offset) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<WeightEntry> entries = new ArrayList<>();
        
        String selection = WeighToGoDBHelper.COL_USER_ID + " = ? AND " +
                          WeighToGoDBHelper.COL_IS_DELETED + " = 0";
        String[] selectionArgs = { String.valueOf(userId) };
        String orderBy = WeighToGoDBHelper.COL_WEIGHT_DATE + " DESC";
        String limitStr = limit + " OFFSET " + offset;
        
        Cursor cursor = db.query(
            WeighToGoDBHelper.TABLE_DAILY_WEIGHTS,
            null,
            selection,
            selectionArgs,
            null, null, orderBy, limitStr
        );
        
        try {
            while (cursor.moveToNext()) {
                entries.add(cursorToWeightEntry(cursor));
            }
        } finally {
            cursor.close();
        }
        
        return entries;
    }
    
    /**
     * Get weight statistics for a user.
     */
    public WeightStats getWeightStats(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT " +
            "MIN(" + WeighToGoDBHelper.COL_WEIGHT_VALUE + ") AS min_weight, " +
            "MAX(" + WeighToGoDBHelper.COL_WEIGHT_VALUE + ") AS max_weight, " +
            "AVG(" + WeighToGoDBHelper.COL_WEIGHT_VALUE + ") AS avg_weight, " +
            "COUNT(*) AS total_entries " +
            "FROM " + WeighToGoDBHelper.TABLE_DAILY_WEIGHTS + " " +
            "WHERE " + WeighToGoDBHelper.COL_USER_ID + " = ? AND " +
            WeighToGoDBHelper.COL_IS_DELETED + " = 0";
        
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(userId) });
        
        try {
            if (cursor.moveToFirst()) {
                WeightStats stats = new WeightStats();
                stats.minWeight = cursor.getDouble(cursor.getColumnIndexOrThrow("min_weight"));
                stats.maxWeight = cursor.getDouble(cursor.getColumnIndexOrThrow("max_weight"));
                stats.avgWeight = cursor.getDouble(cursor.getColumnIndexOrThrow("avg_weight"));
                stats.totalEntries = cursor.getInt(cursor.getColumnIndexOrThrow("total_entries"));
                return stats;
            }
            return new WeightStats();
        } finally {
            cursor.close();
        }
    }
    
    /**
     * Soft delete a weight entry.
     */
    public boolean deleteWeight(long weightId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(WeighToGoDBHelper.COL_IS_DELETED, 1);
        values.put(WeighToGoDBHelper.COL_UPDATED_AT, WeighToGoDBHelper.getCurrentTimestamp());
        
        int rowsAffected = db.update(
            WeighToGoDBHelper.TABLE_DAILY_WEIGHTS,
            values,
            WeighToGoDBHelper.COL_WEIGHT_ID + " = ?",
            new String[] { String.valueOf(weightId) }
        );
        
        return rowsAffected > 0;
    }
    
    /**
     * Get logging streak (consecutive days with entries).
     */
    public int getCurrentStreak(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT " + WeighToGoDBHelper.COL_WEIGHT_DATE + " " +
            "FROM " + WeighToGoDBHelper.TABLE_DAILY_WEIGHTS + " " +
            "WHERE " + WeighToGoDBHelper.COL_USER_ID + " = ? AND " +
            WeighToGoDBHelper.COL_IS_DELETED + " = 0 " +
            "ORDER BY " + WeighToGoDBHelper.COL_WEIGHT_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(userId) });
        
        try {
            int streak = 0;
            String expectedDate = WeighToGoDBHelper.getCurrentDate();
            
            while (cursor.moveToNext()) {
                String entryDate = cursor.getString(0);
                
                if (entryDate.equals(expectedDate)) {
                    streak++;
                    // Calculate previous day
                    expectedDate = getPreviousDate(expectedDate);
                } else {
                    break;
                }
            }
            
            return streak;
        } finally {
            cursor.close();
        }
    }
    
    /**
     * Convert cursor row to WeightEntry object.
     */
    private WeightEntry cursorToWeightEntry(Cursor cursor) {
        WeightEntry entry = new WeightEntry();
        entry.setWeightId(cursor.getLong(cursor.getColumnIndexOrThrow(WeighToGoDBHelper.COL_WEIGHT_ID)));
        entry.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(WeighToGoDBHelper.COL_USER_ID)));
        entry.setWeightValue(cursor.getDouble(cursor.getColumnIndexOrThrow(WeighToGoDBHelper.COL_WEIGHT_VALUE)));
        entry.setWeightUnit(cursor.getString(cursor.getColumnIndexOrThrow(WeighToGoDBHelper.COL_WEIGHT_UNIT)));
        entry.setWeightDate(cursor.getString(cursor.getColumnIndexOrThrow(WeighToGoDBHelper.COL_WEIGHT_DATE)));
        
        int notesIndex = cursor.getColumnIndex(WeighToGoDBHelper.COL_NOTES);
        if (notesIndex >= 0 && !cursor.isNull(notesIndex)) {
            entry.setNotes(cursor.getString(notesIndex));
        }
        
        entry.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(WeighToGoDBHelper.COL_CREATED_AT)));
        entry.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(WeighToGoDBHelper.COL_UPDATED_AT)));
        
        return entry;
    }
    
    /**
     * Get previous date string.
     */
    private String getPreviousDate(String dateStr) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", 
                java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(dateStr);
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return dateStr;
        }
    }
    
    /**
     * Weight statistics inner class.
     */
    public static class WeightStats {
        public double minWeight = 0;
        public double maxWeight = 0;
        public double avgWeight = 0;
        public int totalEntries = 0;
    }
}
```

---

## Common Queries

### Authentication Queries

```sql
-- Login verification
SELECT user_id, username, email, password_hash, salt, display_name
FROM users
WHERE username = ? AND is_active = 1;

-- Check username availability
SELECT COUNT(*) FROM users WHERE username = ?;

-- Update last login
UPDATE users 
SET last_login = datetime('now', 'localtime')
WHERE user_id = ?;
```

### Weight Tracking Queries

```sql
-- Get latest weight for user
SELECT * FROM daily_weights
WHERE user_id = ? AND is_deleted = 0
ORDER BY weight_date DESC
LIMIT 1;

-- Get weights for date range
SELECT * FROM daily_weights
WHERE user_id = ? 
  AND weight_date >= ? 
  AND weight_date <= ?
  AND is_deleted = 0
ORDER BY weight_date DESC;

-- Get weight change from start
SELECT 
    (SELECT weight_value FROM daily_weights 
     WHERE user_id = ? AND is_deleted = 0 
     ORDER BY weight_date ASC LIMIT 1) AS start_weight,
    (SELECT weight_value FROM daily_weights 
     WHERE user_id = ? AND is_deleted = 0 
     ORDER BY weight_date DESC LIMIT 1) AS current_weight;

-- Calculate progress percentage
SELECT 
    CASE 
        WHEN g.start_weight = g.goal_weight THEN 100.0
        ELSE ROUND(
            ((g.start_weight - dw.weight_value) / (g.start_weight - g.goal_weight)) * 100, 
            1
        )
    END AS progress_percent
FROM goal_weights g
JOIN (
    SELECT weight_value 
    FROM daily_weights 
    WHERE user_id = ? AND is_deleted = 0 
    ORDER BY weight_date DESC LIMIT 1
) dw
WHERE g.user_id = ? AND g.is_active = 1;
```

### Goal Tracking Queries

```sql
-- Get active goal
SELECT * FROM goal_weights
WHERE user_id = ? AND is_active = 1
LIMIT 1;

-- Check if goal achieved
SELECT g.*, dw.weight_value AS current_weight
FROM goal_weights g
JOIN daily_weights dw ON g.user_id = dw.user_id
WHERE g.user_id = ? 
  AND g.is_active = 1 
  AND g.is_achieved = 0
  AND dw.is_deleted = 0
  AND dw.weight_date = (
      SELECT MAX(weight_date) 
      FROM daily_weights 
      WHERE user_id = ? AND is_deleted = 0
  )
  AND dw.weight_value <= g.goal_weight;

-- Mark goal as achieved
UPDATE goal_weights
SET is_achieved = 1,
    achieved_date = datetime('now', 'localtime'),
    is_active = 0,
    updated_at = datetime('now', 'localtime')
WHERE goal_id = ?;
```

### Achievement Queries

```sql
-- Get unnotified achievements
SELECT * FROM achievements
WHERE user_id = ? AND is_notified = 0
ORDER BY achieved_at DESC;

-- Mark achievement as notified
UPDATE achievements
SET is_notified = 1
WHERE achievement_id = ?;

-- Check for new low weight achievement
SELECT COUNT(*) FROM daily_weights
WHERE user_id = ? 
  AND weight_value < ?
  AND is_deleted = 0;

-- Get all achievements for user
SELECT * FROM achievements
WHERE user_id = ?
ORDER BY achieved_at DESC;
```

### Statistics Queries

```sql
-- Weight statistics summary
SELECT 
    MIN(weight_value) AS lowest_weight,
    MAX(weight_value) AS highest_weight,
    AVG(weight_value) AS average_weight,
    COUNT(*) AS total_entries,
    MIN(weight_date) AS first_entry_date,
    MAX(weight_date) AS last_entry_date
FROM daily_weights
WHERE user_id = ? AND is_deleted = 0;

-- Weekly averages
SELECT 
    strftime('%Y-%W', weight_date) AS week,
    AVG(weight_value) AS avg_weight,
    MIN(weight_value) AS min_weight,
    MAX(weight_value) AS max_weight,
    COUNT(*) AS entries
FROM daily_weights
WHERE user_id = ? AND is_deleted = 0
GROUP BY strftime('%Y-%W', weight_date)
ORDER BY week DESC
LIMIT 12;

-- Monthly progress
SELECT 
    strftime('%Y-%m', weight_date) AS month,
    (SELECT weight_value FROM daily_weights dw2 
     WHERE dw2.user_id = dw.user_id 
       AND strftime('%Y-%m', dw2.weight_date) = strftime('%Y-%m', dw.weight_date)
       AND dw2.is_deleted = 0
     ORDER BY dw2.weight_date ASC LIMIT 1) AS month_start,
    (SELECT weight_value FROM daily_weights dw2 
     WHERE dw2.user_id = dw.user_id 
       AND strftime('%Y-%m', dw2.weight_date) = strftime('%Y-%m', dw.weight_date)
       AND dw2.is_deleted = 0
     ORDER BY dw2.weight_date DESC LIMIT 1) AS month_end
FROM daily_weights dw
WHERE user_id = ? AND is_deleted = 0
GROUP BY strftime('%Y-%m', weight_date)
ORDER BY month DESC;
```

---

## Migration Strategy

### Version Management

```java
/**
 * Database migration handler.
 */
public class DatabaseMigration {
    
    /**
     * Handle upgrade from version 1 to version 2.
     */
    public static void migrateV1toV2(SQLiteDatabase db) {
        // Example: Add new column
        // db.execSQL("ALTER TABLE users ADD COLUMN profile_image TEXT;");
        
        // Example: Add new table
        // db.execSQL("CREATE TABLE IF NOT EXISTS ...");
        
        // Example: Migrate data
        // db.execSQL("UPDATE users SET new_column = old_column;");
    }
    
    /**
     * Handle upgrade from version 2 to version 3.
     */
    public static void migrateV2toV3(SQLiteDatabase db) {
        // Future migrations
    }
}
```

### Migration Best Practices

1. **Never drop tables** in production - use ALTER TABLE
2. **Add new columns as nullable** or with defaults
3. **Test migrations** thoroughly before release
4. **Backup data** before major migrations
5. **Use transactions** for complex migrations

---

## Security Considerations

### Password Hashing Utility

```java
package com.rickgoshen.weightogo.utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing utilities using SHA-256 with salt.
 */
public class PasswordUtils {
    
    private static final int SALT_LENGTH = 32;
    
    /**
     * Generate a random salt.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hash a password with salt using SHA-256.
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify a password against stored hash.
     */
    public static boolean verifyPassword(String password, String salt, String storedHash) {
        String computedHash = hashPassword(password, salt);
        return computedHash.equals(storedHash);
    }
}
```

### Security Best Practices

| Practice | Implementation |
|----------|----------------|
| Password Storage | SHA-256 with unique salt per user |
| SQL Injection | Parameterized queries only |
| Data at Rest | Android Keystore for sensitive data |
| Session Management | Encrypted SharedPreferences |
| Input Validation | Server-side and client-side validation |
| Phone Number Storage | E.164 format, validated before storage |

---

## SMS Notification Integration

### SMS Permission Requirements

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.SEND_SMS" />
```

### SMS Notification Utility Class

```java
package com.rickgoshen.weightogo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Utility class for sending SMS notifications.
 * Handles permission checking and message sending for goal achievements,
 * milestones, and daily reminders.
 * 
 * @author Rick Goshen
 * @version 1.0
 */
public class SmsNotificationUtils {
    
    private static final String TAG = "SmsNotificationUtils";
    public static final int SMS_PERMISSION_REQUEST_CODE = 101;
    
    // Message templates
    private static final String MSG_GOAL_REACHED = 
        "🎉 Weigh to Go! Congratulations! You've reached your goal weight of %.1f %s! " +
        "You've got this—pound for pound!";
    
    private static final String MSG_MILESTONE = 
        "💪 Weigh to Go! Amazing progress! You've lost %.1f %s from your starting weight!";
    
    private static final String MSG_NEW_LOW = 
        "📉 Weigh to Go! New personal best! You've reached a new low of %.1f %s!";
    
    private static final String MSG_STREAK = 
        "🔥 Weigh to Go! You're on a %d-day logging streak! Keep it up!";
    
    private static final String MSG_DAILY_REMINDER = 
        "⚖️ Weigh to Go! Don't forget to log your weight today. You've got this!";
    
    /**
     * Check if SMS permission is granted.
     * 
     * @param context Application context
     * @return true if permission granted, false otherwise
     */
    public static boolean hasSmsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Request SMS permission from user.
     * 
     * @param activity Activity requesting permission
     */
    public static void requestSmsPermission(Activity activity) {
        ActivityCompat.requestPermissions(
            activity,
            new String[] { Manifest.permission.SEND_SMS },
            SMS_PERMISSION_REQUEST_CODE
        );
    }
    
    /**
     * Send SMS notification if permission granted and phone number is valid.
     * 
     * @param context Application context
     * @param phoneNumber Recipient phone number in E.164 format
     * @param message Message to send
     * @return true if sent successfully, false otherwise
     */
    public static boolean sendSms(Context context, String phoneNumber, String message) {
        if (!hasSmsPermission(context)) {
            Log.w(TAG, "SMS permission not granted");
            return false;
        }
        
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.w(TAG, "Phone number is empty");
            return false;
        }
        
        try {
            SmsManager smsManager = SmsManager.getDefault();
            
            // Handle long messages (SMS limit is 160 chars)
            if (message.length() > 160) {
                java.util.ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            }
            
            Log.d(TAG, "SMS sent to " + phoneNumber.substring(0, 4) + "****");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send goal reached notification.
     */
    public static boolean sendGoalReachedSms(Context context, String phoneNumber, 
                                               double goalWeight, String unit) {
        String message = String.format(MSG_GOAL_REACHED, goalWeight, unit);
        return sendSms(context, phoneNumber, message);
    }
    
    /**
     * Send milestone notification.
     */
    public static boolean sendMilestoneSms(Context context, String phoneNumber, 
                                            double weightLost, String unit) {
        String message = String.format(MSG_MILESTONE, weightLost, unit);
        return sendSms(context, phoneNumber, message);
    }
    
    /**
     * Send new low weight notification.
     */
    public static boolean sendNewLowSms(Context context, String phoneNumber, 
                                         double newLow, String unit) {
        String message = String.format(MSG_NEW_LOW, newLow, unit);
        return sendSms(context, phoneNumber, message);
    }
    
    /**
     * Send streak notification.
     */
    public static boolean sendStreakSms(Context context, String phoneNumber, int streakDays) {
        String message = String.format(MSG_STREAK, streakDays);
        return sendSms(context, phoneNumber, message);
    }
    
    /**
     * Send daily reminder notification.
     */
    public static boolean sendDailyReminderSms(Context context, String phoneNumber) {
        return sendSms(context, phoneNumber, MSG_DAILY_REMINDER);
    }
    
    /**
     * Validate phone number format (basic E.164 validation).
     * 
     * @param phoneNumber Phone number to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        
        // E.164 format: + followed by 1-15 digits
        return phoneNumber.matches("^\\+[1-9]\\d{1,14}$");
    }
    
    /**
     * Format phone number to E.164 format (US numbers).
     * 
     * @param phoneNumber Raw phone number input
     * @return E.164 formatted number or null if invalid
     */
    public static String formatToE164(String phoneNumber) {
        if (phoneNumber == null) return null;
        
        // Remove all non-digit characters
        String digits = phoneNumber.replaceAll("[^\\d]", "");
        
        // Handle US numbers
        if (digits.length() == 10) {
            return "+1" + digits;
        } else if (digits.length() == 11 && digits.startsWith("1")) {
            return "+" + digits;
        } else if (digits.length() > 10 && digits.length() <= 15) {
            return "+" + digits;
        }
        
        return null; // Invalid format
    }
}
```

### SMS Notification Triggers

| Event | SMS Preference Key | Message Type |
|-------|-------------------|--------------|
| Goal weight achieved | `sms_goal_alerts` | Goal Reached |
| 5/10/25/50 lbs lost | `sms_milestone_alerts` | Milestone |
| New lowest weight | `sms_milestone_alerts` | New Low |
| 7/30 day streak | `sms_milestone_alerts` | Streak |
| Daily reminder time | `sms_reminder_enabled` | Daily Reminder |

### Integration Example

```java
// In WeightEntryDAO after saving a new weight entry
public void checkAndSendSmsNotifications(Context context, long userId, double newWeight) {
    UserDAO userDAO = new UserDAO(context);
    PreferenceDAO prefDAO = new PreferenceDAO(context);
    
    User user = userDAO.getUserById(userId);
    
    // Check if user has SMS enabled and phone number
    if (!user.hasSmsCapability()) return;
    
    boolean smsEnabled = prefDAO.getBooleanPreference(userId, 
        WeighToGoDBHelper.PREF_SMS_NOTIFICATIONS_ENABLED, false);
    if (!smsEnabled) return;
    
    String phoneNumber = user.getPhoneNumber();
    String unit = prefDAO.getPreference(userId, WeighToGoDBHelper.PREF_WEIGHT_UNIT, "lbs");
    
    // Check for goal reached
    if (prefDAO.getBooleanPreference(userId, WeighToGoDBHelper.PREF_SMS_GOAL_ALERTS, false)) {
        GoalWeightDAO goalDAO = new GoalWeightDAO(context);
        GoalWeight goal = goalDAO.getActiveGoal(userId);
        
        if (goal != null && newWeight <= goal.getGoalWeight()) {
            SmsNotificationUtils.sendGoalReachedSms(context, phoneNumber, 
                goal.getGoalWeight(), unit);
        }
    }
    
    // Check for milestones
    if (prefDAO.getBooleanPreference(userId, WeighToGoDBHelper.PREF_SMS_MILESTONE_ALERTS, false)) {
        WeightStats stats = getWeightStats(userId);
        double totalLost = stats.maxWeight - newWeight;
        
        // Check milestone thresholds
        if (totalLost >= 50 && !hasAchievement(userId, "MILESTONE_50")) {
            SmsNotificationUtils.sendMilestoneSms(context, phoneNumber, 50, unit);
        } else if (totalLost >= 25 && !hasAchievement(userId, "MILESTONE_25")) {
            SmsNotificationUtils.sendMilestoneSms(context, phoneNumber, 25, unit);
        }
        // ... etc
    }
}
```

---

## Best Practices

### Database Access Guidelines

1. **Use singleton pattern** for DBHelper to avoid connection leaks
2. **Close cursors** in finally blocks or try-with-resources
3. **Use transactions** for multiple related operations
4. **Run queries on background threads** (never on UI thread)
5. **Use parameterized queries** to prevent SQL injection

### Performance Tips

1. **Create indexes** for frequently queried columns
2. **Use LIMIT/OFFSET** for pagination
3. **Avoid SELECT *** - specify columns needed
4. **Use partial indexes** where appropriate
5. **Analyze query plans** with EXPLAIN QUERY PLAN

### Code Example: Transaction Usage

```java
SQLiteDatabase db = dbHelper.getWritableDatabase();
db.beginTransaction();
try {
    // Multiple operations
    db.insert(...);
    db.update(...);
    db.delete(...);
    
    db.setTransactionSuccessful();
} finally {
    db.endTransaction();
}
```

### Code Example: Background Thread Query

```java
// Using AsyncTask (deprecated but still works)
new AsyncTask<Void, Void, List<WeightEntry>>() {
    @Override
    protected List<WeightEntry> doInBackground(Void... voids) {
        return weightEntryDAO.getAllWeights(userId, 20, 0);
    }
    
    @Override
    protected void onPostExecute(List<WeightEntry> entries) {
        adapter.setEntries(entries);
    }
}.execute();

// Recommended: Use Executor
ExecutorService executor = Executors.newSingleThreadExecutor();
Handler handler = new Handler(Looper.getMainLooper());

executor.execute(() -> {
    List<WeightEntry> entries = weightEntryDAO.getAllWeights(userId, 20, 0);
    handler.post(() -> adapter.setEntries(entries));
});
```

---

## Summary

### Quick Reference

| Component | File/Class |
|-----------|------------|
| Database Helper | `WeighToGoDBHelper.java` |
| User DAO | `UserDAO.java` |
| Weight Entry DAO | `WeightEntryDAO.java` |
| Goal Weight DAO | `GoalWeightDAO.java` |
| Achievement DAO | `AchievementDAO.java` |
| Password Utils | `PasswordUtils.java` |
| SMS Notification Utils | `SmsNotificationUtils.java` |
| Model Classes | `User.java`, `WeightEntry.java`, etc. |

### Database Statistics

| Metric | Value |
|--------|-------|
| Tables | 5 |
| Indexes | 12 |
| Triggers | 5 |
| Views | 2 |
| Total Columns | 46 |

---

<p align="center">
  <strong>Weigh to Go!</strong> — You've got this, pound for pound. 🎉
</p>
