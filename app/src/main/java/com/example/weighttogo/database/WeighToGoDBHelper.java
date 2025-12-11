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
     * Context Handling:
     * - Automatically uses Application context via context.getApplicationContext()
     * - Prevents memory leaks from Activity context references
     * - Safe to pass Activity or Application context - both work correctly
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

            // Create indexes for foreign key columns (performance optimization)
            db.execSQL("CREATE INDEX idx_weight_entries_user_id ON " + TABLE_WEIGHT_ENTRIES + "(user_id)");
            Log.d(TAG, "Created index: idx_weight_entries_user_id");

            db.execSQL("CREATE INDEX idx_goal_weights_user_id ON " + TABLE_GOAL_WEIGHTS + "(user_id)");
            Log.d(TAG, "Created index: idx_goal_weights_user_id");

            // Create index on weight_date for date-based queries (recent entries, date ranges, sorting)
            db.execSQL("CREATE INDEX idx_weight_entries_weight_date ON " + TABLE_WEIGHT_ENTRIES + "(weight_date)");
            Log.d(TAG, "Created index: idx_weight_entries_weight_date");

            // Create index on is_deleted for soft delete queries (WHERE is_deleted = 0)
            db.execSQL("CREATE INDEX idx_weight_entries_is_deleted ON " + TABLE_WEIGHT_ENTRIES + "(is_deleted)");
            Log.d(TAG, "Created index: idx_weight_entries_is_deleted");

            // Create index on is_active for finding active goal (common dashboard query)
            db.execSQL("CREATE INDEX idx_goal_weights_is_active ON " + TABLE_GOAL_WEIGHTS + "(is_active)");
            Log.d(TAG, "Created index: idx_goal_weights_is_active");

            // Create unique index on username (performance + uniqueness enforcement)
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
     * Currently drops and recreates all tables (acceptable for development).
     *
     * TODO: Implement production migration strategy
     * - For production: implement ALTER TABLE statements for schema changes
     * - Preserve user data during upgrades (no DROP TABLE)
     * - Use switch statement to handle incremental migrations (v1→v2, v2→v3, etc.)
     * - Test migration paths with sample data
     * - Consider using Room Persistence Library for automated migrations
     *
     * @param db the database
     * @param oldVersion the old database version
     * @param newVersion the new database version
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
