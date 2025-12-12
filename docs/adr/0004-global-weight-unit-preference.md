# ADR-0004: Global Weight Unit Preference Architecture

- **Date**: 2025-12-12
- **Status**: Accepted

## Context

The WeighToGo application currently allows users to select weight units (lbs/kg) on a per-entry basis in both WeightEntryActivity and GoalDialogFragment. This approach creates several problems:

**Problems with Current Per-Entry Unit Selection:**
- Users rarely switch units between entries (unit preference is typically static)
- Unit toggles clutter the UI in WeightEntryActivity and GoalDialogFragment
- Conversion complexity required throughout the codebase when entries use mixed units
- Not industry standard - most weight tracking apps use a global unit preference
- Cognitive overhead for users who must see and potentially interact with unit selection for every entry
- Data fragmentation makes trend calculations more complex

**User Request:** *"I think we should not allow the user from entry to entry to select lbs or kg. I think there should be a settings for this."*

**Current State:**
- `daily_weights` table has `weight_unit` column (TEXT) storing unit per entry
- WeightEntryActivity has lbs/kg toggle buttons with conversion logic
- GoalDialogFragment has lbs/kg toggle buttons with conversion logic
- `user_preferences` table exists but UserPreferenceDAO has not been implemented yet

**Requirements:**
- Simplify UX by moving unit selection to a single settings location
- Maintain backward compatibility with existing weight entries
- Support multiple users with different unit preferences (each user has own preference)
- Provide global default for new users (lbs for US market)
- Ensure existing data displays correctly during migration period

## Decision

We will implement a **global user preference system** for weight units with the following architecture:

### 1. Preference Storage: UserPreferenceDAO

**Create new DAO for user preferences:**
```java
public class UserPreferenceDAO {
    // Generic key-value storage
    public String getPreference(long userId, String key, String defaultValue);
    public boolean setPreference(long userId, String key, String value);

    // Convenience methods for weight unit
    public String getWeightUnit(long userId);  // Default: "lbs"
    public boolean setWeightUnit(long userId, String unit);  // Validates "lbs" or "kg"
}
```

**Implementation details:**
- Uses `user_preferences` table (already exists in database schema)
- Implements INSERT OR REPLACE for upsert operations
- Validates weight unit values ("lbs" or "kg" only)
- Returns default "lbs" if preference doesn't exist
- Scoped by `user_id` (multi-user support)

### 2. Migration Strategy: Keep Column (Option A)

**Keep existing `weight_unit` column in `daily_weights` table**

**Rationale:**
- ✅ Backward compatible - no data loss
- ✅ Historical accuracy - preserves what user actually entered
- ✅ Simple implementation - no migration scripts required
- ✅ Lower risk - can deploy immediately without breaking existing data
- ✅ Future enhancement possible: "Convert all entries to [unit]" button

**How it works:**
1. New entries use global preference from UserPreferenceDAO (ignore per-entry toggle)
2. Existing entries display in their originally stored units
3. Conversion happens at display time when units don't match

**Alternative (Rejected): Remove Column**
- ❌ Requires data migration script
- ❌ Loses historical accuracy of what user entered
- ❌ Higher risk of data corruption
- ❌ Complex rollback if issues arise

### 3. Settings Screen Architecture: Unified SettingsActivity

**Create single `SettingsActivity` with multiple preference sections:**

```java
public class SettingsActivity extends AppCompatActivity {
    private UserPreferenceDAO userPrefDAO;
    private TextView unitLbs, unitKg;

    private void setupWeightUnitToggle() {
        String currentUnit = userPrefDAO.getWeightUnit(userId);
        updateWeightUnitUI(currentUnit);

        unitLbs.setOnClickListener(v -> saveWeightUnit("lbs"));
        unitKg.setOnClickListener(v -> saveWeightUnit("kg"));
    }

    private void saveWeightUnit(String unit) {
        if (userPrefDAO.setWeightUnit(userId, unit)) {
            Toast.makeText(this, "Weight unit updated to " + unit, LENGTH_SHORT).show();
            updateWeightUnitUI(unit);
        }
    }
}
```

**Preference sections (Material Cards):**
1. **Weight Preferences Card** (NEW - Phase 6.0)
   - Weight unit toggle (lbs / kg)
   - Description: "Choose your preferred unit for weight tracking"
2. **SMS Notification Preferences Card** (Future - Phase 7)
   - Permission status
   - Phone number input
   - SMS notification toggles
3. **App Preferences Card** (Future - Phase 8)
   - Theme, date format, data export

**File organization:**
- Rename `activity_sms_settings.xml` → `activity_settings.xml`
- Create `SettingsActivity.java` (combines all preferences)
- Weight preferences card positioned BEFORE SMS card

### 4. Activity Refactoring

**WeightEntryActivity changes:**
```java
// REMOVE:
- unitLbs, unitKg TextView fields
- setupUnitToggleListeners() method
- switchUnit() method
- updateUnitButtonUI() method

// ADD:
- UserPreferenceDAO field
- Load unit in onCreate(): currentUnit = userPrefDAO.getWeightUnit(userId);

// KEEP:
- weightUnit TextView (read-only display)
- Weight validation logic (WeightUtils)
```

**GoalDialogFragment changes:**
```java
// REMOVE:
- unitLbs, unitKg fields
- setupUnitToggle() method
- updateUnitButtonUI() method
- selectedUnit state variable

// ADD:
- UserPreferenceDAO field
- Load unit in onCreate(): String goalUnit = userPrefDAO.getWeightUnit(userId);

// KEEP:
- Goal weight input field
- Target date picker
- Weight conversion logic (for displaying current weight)
```

### 5. Data Flow

**Before (Per-Entry):**
```
User opens WeightEntryActivity
  → Sees unit toggle (lbs/kg)
  → Selects unit for THIS entry
  → Saves entry with selected unit
  → Stored in daily_weights.weight_unit column
```

**After (Global Preference):**
```
User opens SettingsActivity (one time)
  → Selects preferred unit (lbs/kg)
  → Saves to user_preferences table

User opens WeightEntryActivity
  → Loads global preference from UserPreferenceDAO
  → No unit toggle visible
  → Saves entry with global preference unit
  → Stored in daily_weights.weight_unit column (for backward compatibility)
```

### 6. Default Behavior

**New users (no preference set):**
- Default weight unit: "lbs"
- Rationale: US market is primary target, imperial system is default

**Existing users (preference not set):**
- First launch after upgrade: defaults to "lbs"
- User can change in SettingsActivity
- Existing entries retain their original units

## Rationale

### Why Global Preference Over Per-Entry?

| Aspect | Per-Entry (Current) | Global Preference (New) | Decision |
|--------|---------------------|-------------------------|----------|
| **User Behavior** | Users rarely switch units | Unit preference is typically static | **Global** matches real usage |
| **Industry Standard** | Not standard | MyFitnessPal, Lose It!, Noom all use global | **Global** is industry norm |
| **UX Complexity** | 2 toggle buttons per screen | 1 setting, set once | **Global** is simpler |
| **Data Consistency** | Mixed units require conversion | Consistent units simplify logic | **Global** reduces complexity |
| **Cognitive Load** | User sees toggle every entry | User sets once, forgets | **Global** reduces friction |

### Why Keep weight_unit Column?

| Approach | Backward Compatible | Data Loss Risk | Implementation Complexity |
|----------|---------------------|----------------|---------------------------|
| **Keep Column** | ✅ Yes | ✅ None | ✅ Low (no migration) |
| Remove Column | ❌ No (requires migration) | ⚠️ Medium (script errors) | ❌ High (migration script) |

### Why UserPreferenceDAO Instead of SharedPreferences?

| Technology | Pros | Cons | Decision |
|------------|------|------|----------|
| **UserPreferenceDAO** | ✅ Database consistency<br>✅ Multi-user support<br>✅ Foreign key constraints<br>✅ Centralized storage | ❌ Slightly more code | **CHOSEN** - Consistent with app architecture |
| SharedPreferences | ✅ Simple API<br>✅ Built-in Android | ❌ No multi-user scoping<br>❌ No referential integrity<br>❌ Separate data store | Rejected - Not suitable for multi-user |

### Why Unified SettingsActivity?

**Benefits:**
- Single location for all user preferences (better UX)
- Reusable infrastructure for future settings (theme, notifications, etc.)
- Consistent Material Design 3 pattern (card-based sections)
- Easier navigation (one settings button instead of multiple)

**Alternative (Rejected): Separate WeightSettingsActivity**
- ❌ More navigation complexity
- ❌ Fragmented settings experience
- ❌ Duplicate header/layout code

## Consequences

### Positive

- ✅ **Simplified UX**: Users set unit preference once, not per entry
- ✅ **Industry Standard**: Matches behavior of MyFitnessPal, Lose It!, Noom
- ✅ **Reduced Cognitive Load**: No unit decision required during weight entry
- ✅ **Cleaner UI**: Removes 2 toggle buttons from WeightEntryActivity
- ✅ **Consistent Data**: Future entries use same unit (easier trend calculation)
- ✅ **Multi-User Support**: Each user has own preference (foreign key constraint)
- ✅ **Extensible**: UserPreferenceDAO supports future settings (theme, date format, etc.)
- ✅ **Backward Compatible**: Existing entries retain original units (no data loss)
- ✅ **Low Risk**: No database migration required (keep column strategy)

### Negative

- ❌ **Migration Period**: Existing users will have mixed-unit entries until they enter more data
- ❌ **Additional DAO**: Adds ~200 lines of code (UserPreferenceDAO)
- ❌ **Display Conversion Required**: Must convert to preferred unit when displaying mixed-unit data
- ❌ **Settings Discovery**: Users must find SettingsActivity to change preference

### Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| User confusion about preference location | Medium | Low | Add clear description: "Applies to all future entries" |
| Multiple users sharing device | Low | Medium | Preferences scoped by `user_id` with FK constraint |
| Existing entries display wrong units | Low | High | Keep `weight_unit` column; convert at display time |
| Settings screen becomes cluttered | Medium | Low | Use Material Cards for visual separation |
| Performance degradation | Very Low | Low | UserPreferenceDAO reads indexed by `(user_id, pref_key)` |

## Implementation Details

### UserPreferenceDAO Implementation

```java
public class UserPreferenceDAO {
    private static final String TAG = "UserPreferenceDAO";
    private static final String PREF_KEY_WEIGHT_UNIT = "weight_unit";
    private static final String DEFAULT_WEIGHT_UNIT = "lbs";

    private final WeighToGoDBHelper dbHelper;

    public String getPreference(long userId, String key, String defaultValue) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
            WeighToGoDBHelper.TABLE_USER_PREFERENCES,
            new String[]{"pref_value"},
            "user_id = ? AND pref_key = ?",
            new String[]{String.valueOf(userId), key},
            null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }

        return defaultValue;
    }

    public boolean setPreference(long userId, String key, String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("pref_key", key);
        values.put("pref_value", value);
        values.put("created_at", LocalDateTime.now().format(ISO_DATETIME_FORMATTER));
        values.put("updated_at", LocalDateTime.now().format(ISO_DATETIME_FORMATTER));

        // INSERT OR REPLACE leverages UNIQUE(user_id, pref_key) constraint
        long result = db.insertWithOnConflict(
            WeighToGoDBHelper.TABLE_USER_PREFERENCES,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        );

        return result > 0;
    }

    public String getWeightUnit(long userId) {
        return getPreference(userId, PREF_KEY_WEIGHT_UNIT, DEFAULT_WEIGHT_UNIT);
    }

    public boolean setWeightUnit(long userId, String unit) {
        // Validate input
        if (!"lbs".equals(unit) && !"kg".equals(unit)) {
            Log.e(TAG, "Invalid weight unit: " + unit);
            return false;
        }

        return setPreference(userId, PREF_KEY_WEIGHT_UNIT, unit);
    }
}
```

### Layout Changes

**activity_settings.xml (NEW card):**
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/spacing_medium">

    <LinearLayout
        android:orientation="vertical"
        android:padding="@dimen/spacing_medium">

        <TextView
            android:text="@string/weight_preferences_title"
            android:textSize="@dimen/text_size_large"
            android:textColor="@color/text_primary" />

        <TextView
            android:text="@string/weight_unit_description"
            android:textColor="@color/text_secondary" />

        <!-- Unit Toggle -->
        <LinearLayout
            android:orientation="horizontal"
            android:layout_marginTop="@dimen/spacing_small">

            <TextView
                android:id="@+id/unit_lbs"
                android:text="@string/lbs"
                android:background="@drawable/bg_unit_toggle_active" />

            <TextView
                android:id="@+id/unit_kg"
                android:text="@string/kg"
                android:background="@drawable/bg_unit_toggle_inactive" />
        </LinearLayout>
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

## Testing Strategy

### Unit Tests (23 new tests)

**UserPreferenceDAOTest.java (10 tests):**
- `test_getPreference_withNonExistentKey_returnsDefaultValue`
- `test_setPreference_withValidData_returnsTrue`
- `test_setPreference_thenGet_returnsCorrectValue`
- `test_setPreference_twice_updatesValue` (REPLACE behavior)
- `test_getWeightUnit_withNoPreference_returnsDefaultLbs`
- `test_setWeightUnit_withValidLbs_returnsTrue`
- `test_setWeightUnit_withValidKg_returnsTrue`
- `test_setWeightUnit_withInvalidUnit_returnsFalse`
- `test_setWeightUnit_thenGet_returnsCorrectUnit`
- `test_getPreference_withMultipleUsers_isolatesData`

**WeightEntryActivityPreferenceTest.java (3 tests):**
- `test_onCreate_loadsGlobalWeightUnit`
- `test_onCreate_withUserPreferringKg_initializesKgUnit`
- `test_onCreate_withNoPreference_defaultsToLbs`

**GoalDialogFragmentPreferenceTest.java (2 tests):**
- `test_onCreate_loadsGlobalWeightUnit`
- `test_unitToggle_doesNotExist`

**SettingsActivityTest.java (4 tests):**
- `test_onCreate_loadsCurrentWeightUnit`
- `test_clickLbsToggle_savesLbsPreference`
- `test_clickKgToggle_savesKgPreference`
- `test_saveWeightUnit_showsConfirmationToast`

**WeightUnitPreferenceIntegrationTest.java (4 tests):**
- `test_userChangesUnitInSettings_affectsNewWeightEntries`
- `test_userChangesUnitInSettings_affectsNewGoals`
- `test_existingEntriesRetainOriginalUnits`
- `test_multipleUsersHaveIsolatedPreferences`

**Total:** 23 new tests (14 unit + 9 integration)

### Manual Testing Checklist

- [ ] Fresh install defaults to lbs
- [ ] Change to kg in Settings → WeightEntryActivity opens in kg
- [ ] Change to lbs in Settings → GoalDialogFragment opens in lbs
- [ ] Existing entries display in stored units (mixed units OK)
- [ ] Unit conversion works correctly (WeightUtils)
- [ ] Multi-user: User A=kg, User B=lbs (isolation verified)

## Future Considerations

### Phase 8 Enhancement: Bulk Unit Conversion

**Future feature for Phase 8:**
```java
// SettingsActivity.java
private void showBulkConversionDialog() {
    new AlertDialog.Builder(this)
        .setTitle("Convert All Entries?")
        .setMessage("Convert all your weight entries to " + selectedUnit + "?")
        .setPositiveButton("Convert", (dialog, which) -> {
            int count = weightEntryDAO.convertAllEntriesToUnit(userId, selectedUnit);
            Toast.makeText(this, "Converted " + count + " entries", LENGTH_SHORT).show();
        })
        .setNegativeButton("Cancel", null)
        .show();
}
```

**Benefits:**
- Eliminates mixed-unit data
- Simplifies trend calculations
- Optional (doesn't break current functionality)

### Additional Preference Support

**Future preferences using UserPreferenceDAO:**
- `theme` - "light" | "dark" | "system"
- `date_format` - "MM/dd/yyyy" | "dd/MM/yyyy" | "yyyy-MM-dd"
- `first_day_of_week` - "sunday" | "monday"
- `notifications_enabled` - "true" | "false"
- `sms_phone_number` - E.164 format string

**Generic design enables easy extension:**
```java
userPrefDAO.setPreference(userId, "theme", "dark");
String theme = userPrefDAO.getPreference(userId, "theme", "light");
```

## Alternatives Considered

### Alternative 1: Remove weight_unit Column (Rejected)

**Approach:**
- Remove `weight_unit` column from `daily_weights` table
- Store unit only in `user_preferences`
- Migrate all existing entries to user's current preference

**Pros:**
- Cleaner schema (no redundant column)
- All entries guaranteed same unit
- Simpler display logic (no conversion)

**Cons:**
- ❌ Requires database migration script (onUpgrade)
- ❌ Loses historical accuracy (what user actually entered)
- ❌ Higher risk of data corruption during migration
- ❌ Complex rollback if migration fails
- ❌ Not backward compatible

**Decision:** Rejected - risk outweighs benefit

### Alternative 2: SharedPreferences Instead of UserPreferenceDAO (Rejected)

**Approach:**
- Store weight unit in SharedPreferences
- Read on activity launch

**Pros:**
- Simpler API (fewer lines of code)
- Built-in Android mechanism

**Cons:**
- ❌ No multi-user scoping (all users share same preference)
- ❌ No referential integrity (can't cascade delete)
- ❌ Separate data store (not consistent with app architecture)
- ❌ Harder to query all user preferences

**Decision:** Rejected - inconsistent with app architecture

### Alternative 3: Separate WeightSettingsActivity (Rejected)

**Approach:**
- Create dedicated `WeightSettingsActivity` for weight preferences
- Keep SMS settings separate

**Pros:**
- Focused single-purpose screen
- Easier to find weight-specific settings

**Cons:**
- ❌ More navigation complexity (multiple settings screens)
- ❌ Fragmented user experience
- ❌ Duplicate header/footer code
- ❌ Harder to add new preference sections later

**Decision:** Rejected - unified SettingsActivity is better UX

## References

- [Android Data Storage Best Practices](https://developer.android.com/training/data-storage)
- [Material Design 3 - Cards](https://m3.material.io/components/cards)
- [SQLite INSERT OR REPLACE](https://www.sqlite.org/lang_conflict.html)
- Industry standards: MyFitnessPal, Lose It!, Noom (all use global unit preference)

## Related Documents

- ADR-0001: Initial Database Architecture (user_preferences table schema)
- DDR-0001: Weight Unit Preference UX Simplification (companion design decision)
- TODO.md Phase 6.0: Implementation tasks

## Supersedes

None - this is a new architectural decision.

## Superseded By

None currently.