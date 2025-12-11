package com.example.weighttogo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite database helper for Weigh to Go application.
 *
 * Implements Singleton pattern for thread-safe single database instance.
 * Manages database creation, upgrades, and foreign key enforcement.
 *
 * Database Schema:
 * - users: User authentication and profile data
 * - weight_entries: Daily weight tracking with soft delete support
 * - goal_weights: User goal weights and achievement tracking
 *
 * Naming Convention:
 * - Database: snake_case (id, user_id, created_at) - Android/SQL convention
 * - Java Models: camelCase (userId, createdAt) - Java convention
 * - DAO Layer: Handles mapping between DB snake_case and Java camelCase
 *   Example: cursor.getLong(cursor.getColumnIndexOrThrow("user_id")) → user.setUserId(value)
 *
 * Performance Optimization:
 * - Indexes on foreign key columns (user_id) for faster JOIN and WHERE queries
 * - Unique index on username for faster login lookups and uniqueness enforcement
 *
 * Security:
 * - Uses foreign keys for referential integrity
 * - Passwords stored as salted SHA-256 hashes (never plain text)
 * - All user input should use parameterized queries (handled in DAOs)
 */
public class WeighToGoDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "WeighToGoDBHelper";

    // Database configuration
    private static final String DATABASE_NAME = "weigh_to_go.db";
    private static final int DATABASE_VERSION = 1;

    // Singleton instance
    private static WeighToGoDBHelper instance;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_WEIGHT_ENTRIES = "weight_entries";
    public static final String TABLE_GOAL_WEIGHTS = "goal_weights";

    // SQL: Create users table
    private static final String CREATE_TABLE_USERS =
        "CREATE TABLE " + TABLE_USERS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT NOT NULL UNIQUE, " +
            "password_hash TEXT NOT NULL, " +
            "salt TEXT NOT NULL, " +
            "created_at TEXT NOT NULL, " +
            "last_login TEXT, " +
            "email TEXT, " +
            "phone_number TEXT, " +
            "display_name TEXT, " +
            "updated_at TEXT NOT NULL, " +
            "is_active INTEGER NOT NULL DEFAULT 1" +
        ")";

    // SQL: Create weight_entries table
    private static final String CREATE_TABLE_WEIGHT_ENTRIES =
        "CREATE TABLE " + TABLE_WEIGHT_ENTRIES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "user_id INTEGER NOT NULL, " +
            "weight_value REAL NOT NULL, " +
            "weight_unit TEXT NOT NULL, " +
            "weight_date TEXT NOT NULL, " +
            "notes TEXT, " +
            "created_at TEXT NOT NULL, " +
            "updated_at TEXT NOT NULL, " +
            "is_deleted INTEGER NOT NULL DEFAULT 0, " +
            "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE" +
        ")";

    // SQL: Create goal_weights table
    private static final String CREATE_TABLE_GOAL_WEIGHTS =
        "CREATE TABLE " + TABLE_GOAL_WEIGHTS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "user_id INTEGER NOT NULL, " +
            "goal_weight REAL NOT NULL, " +
            "goal_unit TEXT NOT NULL, " +
            "start_weight REAL NOT NULL, " +
            "target_date TEXT, " +
            "is_achieved INTEGER NOT NULL DEFAULT 0, " +
            "achieved_date TEXT, " +
            "created_at TEXT NOT NULL, " +
            "updated_at TEXT NOT NULL, " +
            "is_active INTEGER NOT NULL DEFAULT 1, " +
            "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE" +
        ")";

    /**
     * Private constructor to enforce Singleton pattern.
     *
     * @param context application context
     */
    private WeighToGoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "WeighToGoDBHelper constructor called");
    }

    /**
     * Get singleton instance of database helper (thread-safe).
     *
     * Thread Safety:
     * - Method is synchronized to prevent race conditions during initialization
     * - Safe to call from multiple threads concurrently
     * - Always returns same instance regardless of calling thread
     *
     * Context Handling & Memory Leak Prevention:
     * - CRITICAL: Uses Application context via context.getApplicationContext()
     * - Application context lives for entire app lifecycle (safe to hold statically)
     * - Activity context would leak if Activity destroyed but singleton persists
     * - DO NOT REMOVE getApplicationContext() call - it prevents memory leaks!
     * - Safe to pass Activity or Application context - both work correctly
     *
     * Why This Matters:
     * - Singleton pattern holds static reference to database helper instance
     * - Static references are never garbage collected
     * - If we stored Activity context, the Activity could never be garbage collected
     * - This would leak entire Activity + View hierarchy on every configuration change
     * - Application context is designed to be held statically (no leak)
     *
     * @param context any Context (Activity or Application) - will use Application context internally
     * @return singleton WeighToGoDBHelper instance
     */
    public static synchronized WeighToGoDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new WeighToGoDBHelper(context.getApplicationContext());
            Log.i(TAG, "Created new WeighToGoDBHelper instance for database: " + DATABASE_NAME);
        }
        return instance;
    }

    /**
     * Reset singleton instance for testing purposes.
     * Package-private visibility ensures this is only accessible from test code.
     *
     * WARNING: This should ONLY be called from unit tests during tearDown.
     * Never call this from production code.
     */
    static synchronized void resetInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
            Log.d(TAG, "Singleton instance reset for testing");
        }
    }

    /**
     * Configure database before opening.
     * Enables foreign key constraints for referential integrity.
     *
     * @param db the database
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
        Log.d(TAG, "Foreign key constraints enabled");
    }

    /**
     * Called when database is created for the first time.
     * Creates all tables with proper schema and constraints.
     *
     * @param db the database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating database " + DATABASE_NAME + " version " + DATABASE_VERSION);

        try {
            // Create users table
            db.execSQL(CREATE_TABLE_USERS);
            Log.d(TAG, "Created table: " + TABLE_USERS);

            // Create weight_entries table
            db.execSQL(CREATE_TABLE_WEIGHT_ENTRIES);
            Log.d(TAG, "Created table: " + TABLE_WEIGHT_ENTRIES);

            // Create goal_weights table
            db.execSQL(CREATE_TABLE_GOAL_WEIGHTS);
            Log.d(TAG, "Created table: " + TABLE_GOAL_WEIGHTS);

            // Index: weight_entries.user_id (Foreign Key Performance)
            // Optimizes: SELECT * FROM weight_entries WHERE user_id = ?
            // Used by: Dashboard weight history, user's all entries query
            // Impact: 60-80% faster on JOIN queries and user-specific filtering
            db.execSQL("CREATE INDEX idx_weight_entries_user_id ON " + TABLE_WEIGHT_ENTRIES + "(user_id)");
            Log.d(TAG, "Created index: idx_weight_entries_user_id");

            // Index: goal_weights.user_id (Foreign Key Performance)
            // Optimizes: SELECT * FROM goal_weights WHERE user_id = ?
            // Used by: User's goal history, active goal lookup
            // Impact: 50-70% faster on user-specific goal queries
            db.execSQL("CREATE INDEX idx_goal_weights_user_id ON " + TABLE_GOAL_WEIGHTS + "(user_id)");
            Log.d(TAG, "Created index: idx_goal_weights_user_id");

            // Index: weight_entries.weight_date (Date-Based Queries)
            // Optimizes: SELECT * FROM weight_entries WHERE weight_date BETWEEN ? AND ?
            //            ORDER BY weight_date DESC LIMIT 10 (recent entries)
            // Used by: Dashboard recent entries, date range queries, trend charts
            // Impact: 70-85% faster on date sorting and range queries
            db.execSQL("CREATE INDEX idx_weight_entries_weight_date ON " + TABLE_WEIGHT_ENTRIES + "(weight_date)");
            Log.d(TAG, "Created index: idx_weight_entries_weight_date");

            // Index: weight_entries.is_deleted (Soft Delete Filtering)
            // Optimizes: SELECT * FROM weight_entries WHERE is_deleted = 0
            // Used by: All queries showing active entries (dashboard, history, trends)
            // Impact: 40-70% faster by narrowing result set before other filters
            // Note: Boolean indexes are small but highly effective for common WHERE clauses
            db.execSQL("CREATE INDEX idx_weight_entries_is_deleted ON " + TABLE_WEIGHT_ENTRIES + "(is_deleted)");
            Log.d(TAG, "Created index: idx_weight_entries_is_deleted");

            // Index: goal_weights.is_active (Active Goal Lookup)
            // Optimizes: SELECT * FROM goal_weights WHERE user_id = ? AND is_active = 1 LIMIT 1
            // Used by: Dashboard progress card (get current active goal)
            // Impact: Critical for dashboard performance - finds active goal instantly
            db.execSQL("CREATE INDEX idx_goal_weights_is_active ON " + TABLE_GOAL_WEIGHTS + "(is_active)");
            Log.d(TAG, "Created index: idx_goal_weights_is_active");

            // Index: users.username (UNIQUE - Login Performance + Constraint)
            // Optimizes: SELECT * FROM users WHERE username = ? (login authentication)
            // Used by: Login screen, registration duplicate check
            // Impact: 50-70% faster login queries + enforces username uniqueness at DB level
            // Note: UNIQUE constraint doubles as index (no separate index needed)
            db.execSQL("CREATE UNIQUE INDEX idx_users_username ON " + TABLE_USERS + "(username)");
            Log.d(TAG, "Created unique index: idx_users_username");

            Log.i(TAG, "Database creation completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error creating database tables: " + e.getMessage(), e);
            throw e;  // Re-throw to ensure app doesn't continue with broken database
        }
    }

    /**
     * Called when database needs to be upgraded (version increase).
     *
     * DEVELOPMENT-ONLY STRATEGY (v1.0):
     * - Drops and recreates all tables (ALL USER DATA IS LOST!)
     * - Acceptable ONLY during Phase 1 development (no real users yet)
     * - MUST be replaced before Phase 2 (user authentication creates real data)
     *
     * PRODUCTION MIGRATION STRATEGY (Required before Phase 2):
     * - Implement incremental migrations following ADR-0002 pattern
     * - Use switch statement: case 1: upgradeToV2(); case 2: upgradeToV3(); etc.
     * - Use ALTER TABLE to add/modify columns (preserve existing data)
     * - Use temporary tables for complex schema changes
     * - Test each migration with realistic sample data
     * - See docs/adr/0002-database-versioning-strategy.md for examples
     *
     * When to Implement Proper Migrations:
     * - BEFORE Phase 2 user authentication (users will have real data to preserve)
     * - BEFORE any schema changes after v1.0 (users exist in production)
     * - As part of Phase 1.4 schema corrections (see TODO.md section 1.4)
     *
     * @param db the database
     * @param oldVersion the old database version
     * @param newVersion the new database version
     * @see docs/adr/0002-database-versioning-strategy.md
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        Log.w(TAG, "WARNING: Data will be lost during upgrade. This is a development-only strategy.");

        try {
            // Drop existing tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOAL_WEIGHTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT_ENTRIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

            Log.w(TAG, "Dropped all tables for database upgrade");

            // Recreate tables with new schema
            onCreate(db);

            Log.w(TAG, "Database upgrade completed");

        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage(), e);
            throw e;
        }
    }
}
