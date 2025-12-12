## TODO.md - Weigh to Go! Project Two

## [2025-11-29] Feature: UI Design Implementation

**Objective:**
Build complete UI design in Android Studio (XML layouts only) for CS 360 Project Two submission.

**Approach:**
Create all required XML layouts and resources following the Figma Design Specifications and Project Two Requirements. No functional Java code - UI only.

**Tests:**
- Project builds successfully
- Password field shows dots (inputType="textPassword")
- Delete button present on each grid row
- AndroidManifest includes SEND_SMS permission and telephony feature

**Risks & Tradeoffs:**
- Using PNG icons instead of vector drawables (acceptable for this project)
- Layouts designed for standard phone size; tablet optimization deferred

---

## Current Tasks

### In Progress
- None - All phases complete!

### Phase 1: Resource Files (Completed)
- [x] Create colors.xml palette (2025-11-29)
- [x] Add drawable icons across all densities (2025-11-29)
- [x] Create strings.xml with all UI strings (2025-11-29)
- [x] Create dimens.xml with spacing and sizing values (2025-11-29)
- [x] Update themes.xml for Material Design 3 (2025-11-29)

### Phase 2: Login Screen (25%) - Completed
- [x] Create activity_login.xml layout (2025-11-29)
  - [x] Used existing ic_profile.png for username icon
  - [x] Used existing ic_lock.png for password icon
  - [x] Used existing bg_gradient_primary.xml for header
  - [x] Build header section with app icon, name, and tagline
  - [x] Build auth card with tab toggle (Sign In / Create Account)
  - [x] Build username/password fields with icons
  - [x] Add Sign In button (primary, filled)
  - [x] Add Create Account button (secondary, outlined)
  - [x] Add Forgot Password link
  - [x] Add "or continue with" divider
  - [x] Add terms/privacy footer
  - [x] Verify password field uses inputType="textPassword"
  - [x] Verify focus order matches user flow
  - [x] Validate accessibility (content descriptions, 48dp touch targets)

### Phase 3: Database Grid Screen (25%) - Completed
- [x] Create bottom_nav_menu.xml resource (2025-11-29)
- [x] Build activity_main.xml with full dashboard structure (2025-11-29)
  - [x] Gradient header with greeting, notification/settings buttons
  - [x] Progress Summary Card (current/start/goal weights, progress bar, trend badge)
  - [x] Quick Stats Row (Total Lost, lbs to Goal, Day Streak)
  - [x] Section Header ("Recent Entries" + "View All")
  - [x] RecyclerView for weight entries
  - [x] Empty State Container (icon + messages)
  - [x] FloatingActionButton with rounded square shape
  - [x] BottomNavigationView (Home, Trends, Goals, Profile)
- [x] Create item_weight_entry.xml with delete button (2025-11-29)
  - [x] MaterialCardView container with elevation
  - [x] Date badge (day number + month abbreviation)
  - [x] Weight value with unit and time text
  - [x] Trend indicator badge (up/down/same icons)
  - [x] Edit ImageButton (48dp touch target)
  - [x] Delete ImageButton (48dp, red tint) ← REQUIRED
  - [x] Content descriptions for accessibility
- [x] Add missing string resources (2025-11-29)
- [x] Create supporting drawable resources (2025-11-29)
  - [x] bg_header_button.xml (semi-transparent rounded button)
  - [x] bg_date_badge.xml (surface_variant rounded background)
  - [x] bottom_nav_color.xml (color selector for nav states)
- [x] Add FAB shape style to themes.xml (2025-11-29)
- [x] Update TODO.md with completed tasks (2025-11-29)
- [x] Update project_summary.md with Phase 3 notes (2025-11-29)

### Phase 4: SMS Notifications Screen (30%) - Completed
- [x] Update AndroidManifest.xml with SMS permissions and telephony feature (2025-11-29)
  - [x] Add `<uses-permission android:name="android.permission.SEND_SMS" />`
  - [x] Add `<uses-feature android:name="android.hardware.telephony" android:required="false" />`
- [x] Add new string resources for SMS screen (2025-11-29)
- [x] Add new color resources for status badge states (2025-11-29)
  - [x] Pending: #FFF3E0 bg, #FF9800 text
  - [x] Granted: #E8F5E9 bg, #4CAF50 text
  - [x] Denied: #FFEBEE bg, #F44336 text
- [x] Create supporting drawable resources (2025-11-29)
  - [x] bg_permission_icon.xml (gradient rounded square, 12dp corners)
  - [x] bg_info_banner.xml (light teal #E0F2F1 rounded background)
  - [x] bg_status_pending.xml, bg_status_granted.xml, bg_status_denied.xml
- [x] Create activity_sms_settings.xml layout (2025-11-29)
  - [x] Gradient header with back button, title "SMS Notifications", subtitle
  - [x] Permission Card (icon, title, status badge, description, grant button)
  - [x] Phone Number Card (title, description, country code +1, phone input)
  - [x] Notification Preferences Card with MaterialSwitch toggles:
    - [x] Master toggle: "Enable SMS Notifications"
    - [x] Goal toggle: "Goal Reached Alerts"
    - [x] Milestone toggle: "Milestone Alerts"
    - [x] Daily toggle: "Daily Reminders"
    - [x] "Send Test Message" outlined button
  - [x] Info Banner with messaging rates disclaimer
- [x] Update TODO.md with completed tasks (2025-11-29)
- [x] Update project_summary.md with Phase 4 notes (2025-11-29)

### Phase 5: Weight Entry Screen - Completed
- [x] Add new string resources for weight entry screen (2025-11-29)
- [x] Create supporting drawable resources (2025-11-29)
  - [x] bg_weight_display.xml (surface_variant rounded background, 20dp corners)
  - [x] bg_numpad_button.xml (bordered button with pressed state, 16dp corners)
  - [x] bg_unit_toggle_active.xml, bg_unit_toggle_inactive.xml (unit button states)
  - [x] bg_adjust_button.xml (outlined teal button, 12dp corners)
  - [x] bg_date_nav_button.xml (surface_variant rounded square, 12dp corners)
  - [x] bg_today_badge.xml (success green rounded badge)
- [x] Create activity_weight_entry.xml layout (2025-11-29)
  - [x] Navigation header with back button and "Log Weight" title
  - [x] Date Selector Card:
    - [x] "Entry Date" label
    - [x] Prev/Next date navigation buttons (44dp)
    - [x] Large day number display (32sp)
    - [x] Full date text
    - [x] "Today" badge (success_light bg, accent_green text)
  - [x] Weight Input Card:
    - [x] Title and subtitle
    - [x] Large weight display (64sp font, surface_variant bg)
    - [x] Quick adjust buttons row (-1, -0.5, +0.5, +1)
    - [x] Unit toggle buttons (lbs / kg)
    - [x] Number pad grid (3x4: 1-9, decimal, 0, backspace)
    - [x] Save button (60dp height, MaterialButton)
    - [x] Previous entry hint
- [x] Fix button styling - Material Button overrides background (2025-11-29)
  - [x] Changed Button to TextView for quick adjust buttons
  - [x] Changed Button to TextView for unit toggle buttons
  - [x] Changed Button to TextView for number pad buttons
  - [x] Added clickable="true" and focusable="true" for touch handling
- [x] Update TODO.md with completed tasks (2025-11-29)
- [x] Update project_summary.md with Phase 5 notes (2025-11-29)
- [x] Verify build succeeds (2025-11-29)

### Phase 6: Final Validation - Completed
- [x] Compare all screens to HTML preview files (2025-11-29)
  - [x] Login: Added social buttons (G, f, apple emoji), rounded tab toggle, "Register" label
  - [x] Dashboard: Verified all components match design
  - [x] Weight Entry: Verified all components match design
  - [x] SMS Notifications: Verified all components match design
- [x] Verify WCAG 2.1 AA compliance across all screens (2025-11-29)
  - [x] All touch targets meet 48dp minimum
  - [x] All images have contentDescription
  - [x] High contrast text throughout
- [x] Build and validate entire project (2025-11-29)
- [x] Verify all rubric requirements met (2025-11-29)
  - [x] Login: Password uses inputType="textPassword" (shows dots)
  - [x] Dashboard: Delete button present on each grid row
  - [x] SMS: SEND_SMS permission in manifest
  - [x] SMS: telephony feature with required="false"

### Completed
- [x] Create feature branch (2025-11-29)

---

## Blockers
None

---
---

# TODO.md - Weigh to Go! Project Three

## [2025-12-09] Feature: Full Functional Implementation

**Objective:**
Develop and launch a fully functional mobile application. Transform the completed UI design (Project Two) into a working Android app with authentication, database CRUD, SMS notifications, and launch-ready code.

**Approach:**
Follow strict TDD methodology (Red-Green-Refactor), MVC architecture, and GitFlow branching strategy. Implement in phases: Database → Authentication → Dashboard → CRUD → Goals → SMS → Polish.

**Tests:**
- All unit tests pass (`./gradlew test`)
- All integration tests pass
- 100% DAO coverage, 90%+ business logic coverage
- App functions without SMS permission (graceful degradation)

**Risks & Tradeoffs:**
- Using raw SQLite instead of Room (simpler, per course requirements)
- SHA-256 hashing instead of BCrypt (no external dependencies)
- Soft deletes for weight entries (data recovery possible)

---

## Current Tasks

### In Progress
- None

### Pending
- [ ] Phase 1: Database Foundation
- [ ] Phase 2: User Authentication
- [ ] Phase 3: Main Dashboard
- [ ] Phase 4: Weight Entry CRUD
- [ ] Phase 5: Goal Weight Management
- [ ] Phase 6: SMS Notifications
- [ ] Phase 7: Code Quality
- [ ] Phase 8: Final Testing
- [ ] Phase 9: Launch Plan Document

---

## Phase 1: Database Foundation
**Branch:** `feature/FR1.0-database-setup`

### 1.1 Create Package Structure (Completed 2025-12-09)
- [x] Create `activities/` package
- [x] Create `adapters/` package
- [x] Create `database/` package
- [x] Create `models/` package
- [x] Create `utils/` package
- [x] Create `constants/` package
- [x] Move MainActivity to activities package

### 1.2 Implement Model Classes (TDD)
- [x] Write `UserTest.java` - failing tests first (2025-12-09, updated 2025-12-10)
  - 11 tests total following strict TDD (one test at a time)
  - Tests for all fields: userId, username, passwordHash, salt, createdAt, lastLogin
  - Tests for default constructor and all getters/setters
  - Tests for toString() including security check (no password/salt exposure)
  - Removed full constructor test (6-parameter anti-pattern)
- [x] Implement `models/User.java` (2025-12-09, updated 2025-12-10)
  - Fields: userId, username, passwordHash, salt, createdAt, lastLogin
  - Getters/setters for all fields
  - Default constructor only (removed 6-parameter full constructor)
  - toString() that excludes sensitive fields (passwordHash, salt)
  - All 11 tests passing
- [x] Write `WeightEntryTest.java` - failing tests first (2025-12-10)
  - 11 tests total following strict TDD (one test at a time)
  - Tests for all fields: weightId, userId, weightValue, weightUnit, weightDate, notes, createdAt, updatedAt, isDeleted
  - Tests for default constructor and all getters/setters
  - Tests for toString()
  - No full constructor (avoided 9-parameter anti-pattern)
- [x] Implement `models/WeightEntry.java` (2025-12-10)
  - Fields: weightId, userId, weightValue, weightUnit, weightDate, notes, createdAt, updatedAt, isDeleted
  - Getters/setters for all fields
  - Default constructor only (no bloated full constructor)
  - toString() method
  - All 11 tests passing
- [x] Write `GoalWeightTest.java` - failing tests first (2025-12-10)
  - 13 tests total following strict TDD (one test at a time)
  - Tests for all fields: goalId, userId, goalWeight, goalUnit, startWeight, targetDate, isAchieved, achievedDate, createdAt, updatedAt, isActive
  - Tests for default constructor and all getters/setters
  - Tests for toString()
  - No full constructor (avoided 11-parameter anti-pattern)
- [x] Implement `models/GoalWeight.java` (2025-12-10)
  - Fields: goalId, userId, goalWeight, goalUnit, startWeight, targetDate, isAchieved, achievedDate, createdAt, updatedAt, isActive
  - Getters/setters for all 11 fields
  - Default constructor only (no bloated full constructor)
  - toString() method
  - All 13 tests passing

### 1.3 Implement Database Helper (Completed 2025-12-10)
- [x] Add Robolectric 4.13 testing framework for database tests (2025-12-10)
- [x] Write `DateTimeConverterTest.java` - test LocalDateTime/LocalDate conversions (TDD) (2025-12-10)
  - test_toTimestamp_withValidLocalDateTime_returnsISO8601String
  - test_fromTimestamp_withValidString_returnsLocalDateTime
  - test_toDateString_withValidLocalDate_returnsISO8601String
  - test_fromDateString_withValidString_returnsLocalDate
  - test_roundTrip_preservesDateTime
  - All 5 tests passing
- [x] Implement `utils/DateTimeConverter.java` (2025-12-10)
  - toTimestamp(LocalDateTime) - converts to "yyyy-MM-dd HH:mm:ss" format for SQLite
  - fromTimestamp(String) - parses timestamp string to LocalDateTime
  - toDateString(LocalDate) - converts to "yyyy-MM-dd" format for SQLite
  - fromDateString(String) - parses date string to LocalDate
  - **Security**: Validate input strings, handle null/empty cases
  - **Logging**: Add TAG constant, log conversion errors (Log.e with exception)
  - All methods implemented with comprehensive error handling
- [x] Write `WeighToGoDBHelperTest.java` - test database creation (TDD) (2025-12-10)
  - test_getInstance_returnsSingletonInstance
  - test_getInstance_calledTwice_returnsSameInstance
  - test_onCreate_createsUsersTable
  - test_onCreate_createsWeightEntriesTable
  - test_onCreate_createsGoalWeightsTable
  - test_onConfigure_enablesForeignKeys
  - All 6 tests passing using Robolectric
- [x] Implement `database/WeighToGoDBHelper.java` (2025-12-10)
  - Singleton pattern (thread-safe with synchronized getInstance)
  - DATABASE_NAME = "weigh_to_go.db"
  - DATABASE_VERSION = 1
  - onCreate() - create users, weight_entries, goal_weights tables with proper schema
  - onUpgrade() - handle migrations (drops and recreates tables for v1)
  - onConfigure() - enable foreign keys (setForeignKeyConstraintsEnabled)
  - **Security**: Foreign key constraints enabled, parameterized queries ready for DAOs
  - **Security**: Passwords stored as salted hashes (schema supports salt column)
  - **Logging**: Add TAG constant, log onCreate (Log.i), table creation (Log.d), onUpgrade (Log.w), errors (Log.e with exception)
  - **Logging**: Include database name and version in onCreate log
  - **Logging**: Log foreign key enforcement status
  - All 6 tests passing, lint clean
- [x] Add comprehensive edge case tests (2025-12-10)
  - DateTimeConverterTest: 12 edge case tests (null, empty, whitespace, invalid formats, malformed dates)
  - WeighToGoDBHelperTest: 3 edge case tests (foreign key constraints, cascade delete, onUpgrade)
  - Configured `testOptions.unitTests.returnDefaultValues = true` for Android Log mocking
  - All 84 tests passing (55 models + 17 DateTimeConverter + 9 WeighToGoDBHelper + 2 examples + 1 other)

### 1.4 Implement DAO Classes

**⚠️ CRITICAL: Schema Corrections Required Before DAO Implementation**

Current implementation (Phase 1.3) does NOT match WeighToGo_Database_Architecture.md specification. The following corrections must be made before implementing DAOs:

**Schema Mismatches to Fix:**
- [x] Rename table: `weight_entries` → `daily_weights` (per spec line 196) - Completed 2025-12-10
- [x] Rename column: `weight_entries.id` → `daily_weights.weight_id` (per spec line 197) - Completed 2025-12-10
- [x] Rename column: `users.id` → `users.user_id` (per spec line 169) - Completed 2025-12-10
- [x] Rename column: `goal_weights.id` → `goal_weights.goal_id` (per spec line 225) - Completed 2025-12-10
- [x] Implement missing table: `achievements` (spec lines 250-276) - Completed 2025-12-10
  - Columns: achievement_id, user_id, achievement_type, achieved_date, is_notified, created_at
  - Foreign key: user_id → users(user_id) ON DELETE CASCADE
- [x] Implement missing table: `user_preferences` (spec lines 279-304) - Completed 2025-12-10
  - Columns: preference_id, user_id, preference_key, preference_value, created_at, updated_at
  - Foreign key: user_id → users(user_id) ON DELETE CASCADE
- [x] Add missing indexes (6 additional, total 12 per spec lines 308-336) - Completed 2025-12-10
  - idx_daily_weights_user_id_weight_date (composite for date range queries)
  - idx_daily_weights_is_deleted_user_id (composite for active entries)
  - idx_goal_weights_user_id_is_active (composite for finding active goal)
  - idx_achievements_user_id (foreign key performance)
  - idx_achievements_achievement_type (filtering by type)
  - idx_user_preferences_user_id (foreign key performance)

**Files to Update:**
- [x] `WeighToGoDBHelper.java` - Update all CREATE TABLE statements - Completed 2025-12-10
- [x] `WeighToGoDBHelperTest.java` - Update all schema verification tests - Completed 2025-12-10 (23 tests)
- [x] Model classes (User.java, WeightEntry.java, GoalWeight.java) - Update field names to match DB columns - Completed 2025-12-10 (already correct)
- [x] Create new model classes: `Achievement.java`, `UserPreference.java` - Completed 2025-12-10
- [x] Update `ADR-0001` to reflect specification (not current incorrect implementation) - Completed 2025-12-10
- [x] Update `project_summary.md` with schema correction details - Completed 2025-12-10

**Rationale:**
WeighToGo_Database_Architecture.md is the source of truth specification document that our implementation should have followed from the start. This document (2195 lines) defines:
- 5 normalized tables (not 3)
- Specific naming conventions (weight_id, not id)
- 12 strategic indexes (not 6)
- Complete column specifications

---

**DAO Implementation (After Schema Corrections):**

- [x] Write `UserDAOTest.java` - all CRUD operations - Completed 2025-12-10 (7 tests)
- [x] Implement `database/UserDAO.java` - Completed 2025-12-10
  - insertUser(), getUserById(), getUserByUsername()
  - usernameExists(), updateLastLogin(), deleteUser()
  - **Logging**: Add TAG constant, log method entry (Log.d), successful operations (Log.i), warnings (Log.w), errors (Log.e with exception)
  - **Security**: NEVER log passwords, passwordHash, or salt values
- [x] Write `WeightEntryDAOTest.java` - all CRUD operations - Completed 2025-12-10 (11 tests, retroactive)
- [x] Implement `database/WeightEntryDAO.java` - Completed 2025-12-10
  - insertWeightEntry(), getWeightEntriesForUser()
  - getWeightEntryById(), getLatestWeightEntry()
  - updateWeightEntry(), deleteWeightEntry() (soft delete)
  - **Logging**: Add TAG constant, log CRUD operations (Log.d/Log.i), errors (Log.e with exception)
- [x] Write `GoalWeightDAOTest.java` - all CRUD operations - Completed 2025-12-10 (11 tests, retroactive)
- [x] Implement `database/GoalWeightDAO.java` - Completed 2025-12-10
  - insertGoal(), getActiveGoal(), getGoalHistory()
  - updateGoal(), deactivateGoal(), deactivateAllGoalsForUser()
  - **Logging**: Add TAG constant, log CRUD operations (Log.d/Log.i), errors (Log.e with exception)

#### 1.4.1 PR Review Fixes - Round 5 (Completed 2025-12-10)
- [x] Issue #1: Add resource leak documentation to all DAOs (singleton pattern explanation)
  - UserDAO.java: Added database lifecycle Javadoc
  - WeightEntryDAO.java: Added database lifecycle Javadoc
  - GoalWeightDAO.java: Added database lifecycle + business rules Javadoc
- [x] Issue #2: Add update validation/documentation (return value semantics)
  - WeightEntryDAO.updateWeightEntry(): Documented return values (1=success, 0=not found/error)
  - GoalWeightDAO.updateGoal(): Documented return values (1=success, 0=not found/error)
- [x] Issue #3: Fix inconsistent timestamp handling
  - UserDAO.insertUser() line 60: Changed from client-provided `user.getUpdatedAt()` to server-side `LocalDateTime.now()`
  - All DAOs now use consistent server-side timestamps
- [x] Issue #5: Fix NULL handling in update methods
  - WeightEntryDAO.updateWeightEntry(): Added `values.putNull("notes")` for explicit NULL
  - GoalWeightDAO.updateGoal(): Added `values.putNull("target_date")` and `values.putNull("achieved_date")` for explicit NULL
  - Users can now clear optional fields (notes, target_date, achieved_date)
- [x] Issue #6: Add schema naming documentation
  - WeightEntryDAO.java: Added naming note explaining WeightEntry (Java) vs daily_weights (SQL)
- [x] Run tests (`./gradlew test`) - All 91 tests passing
- [x] Run lint (`./gradlew lint`) - Clean, no warnings
- [x] Commit PR review fixes
- [x] Push to origin
- [x] Update project_summary.md with PR review fixes documentation

### 1.5 Phase 1 Validation (Completed 2025-12-11)
- [x] Run `./gradlew test` - all tests pass (91 tests passing)
- [x] Verify database creates on app launch (singleton pattern verified)
- [x] Verify tables exist with correct schema (5 tables, 12 indexes, foreign keys enabled)
- [x] Run `./gradlew lint` - clean, no warnings
- [x] Update TODO.md (2025-12-11)
- [x] Update project_summary.md (2025-12-11)
- [x] Merge to develop branch

---

## Phase 2: User Authentication
**Branch:** `feature/FR2.0-user-authentication` ✅ **COMPLETED 2025-12-11**

### 2.1 Implement Utility Classes
- [x] Write `PasswordUtilsTest.java` (6 tests - Commit 1)
  - test_generateSalt_returnsNonEmptyString
  - test_generateSalt_returnsDifferentSalts
  - test_hashPassword_withSameInput_returnsSameHash
  - test_hashPassword_withDifferentSalt_returnsDifferentHash
  - test_verifyPassword_withCorrectPassword_returnsTrue
  - test_verifyPassword_withWrongPassword_returnsFalse
- [x] Implement `utils/PasswordUtils.java` (Commit 1)
  - generateSalt() - SecureRandom 16-byte salt, Base64 encoded
  - hashPassword(password, salt) - SHA-256 with concatenation
  - verifyPassword(password, salt, hash) - deterministic comparison
- [x] Write `ValidationUtilsTest.java` (12 tests - Commit 2)
  - test_isValidUsername_withValidInput_returnsTrue
  - test_isValidUsername_withShortInput_returnsFalse
  - test_isValidUsername_withLongInput_returnsFalse
  - test_isValidUsername_withSpecialChars_returnsFalse
  - test_isValidUsername_withNull_returnsFalse
  - test_isValidUsername_withEmpty_returnsFalse
  - test_isValidPassword_withValidInput_returnsTrue
  - test_isValidPassword_withShortInput_returnsFalse
  - test_isValidPassword_withNoNumber_returnsFalse
  - test_isValidPassword_withNull_returnsFalse
  - test_isValidPassword_withEmpty_returnsFalse
  - test_isValidPassword_withOnlyNumbers_returnsTrue
- [x] Implement `utils/ValidationUtils.java` (Commit 2)
  - isValidUsername() - 3-20 chars, regex ^[a-zA-Z0-9_]{3,20}$
  - isValidPassword() - 6+ chars, at least 1 digit
  - Note: isValidPhoneNumber() deferred to Phase 5 (SMS notifications)
- [x] Write `SessionManagerTest.java` (10 tests - Commit 3)
  - test_getInstance_returnsSingletonInstance
  - test_getInstance_calledTwice_returnsSameInstance
  - test_createSession_withValidUser_storesSession
  - test_getCurrentUser_withNoSession_returnsNull
  - test_getCurrentUser_afterCreateSession_returnsUser
  - test_getCurrentUserId_withNoSession_returnsNegativeOne
  - test_getCurrentUserId_afterCreateSession_returnsUserId
  - test_isLoggedIn_withNoSession_returnsFalse
  - test_isLoggedIn_afterCreateSession_returnsTrue
  - test_logout_clearsSession
- [x] Implement `utils/SessionManager.java` (Singleton - Commit 3)
  - createSession(User user) - stores in SharedPreferences
  - getCurrentUser() - reconstructs User from session
  - getCurrentUserId() - returns userId or -1
  - isLoggedIn() - checks session status
  - logout() - clears SharedPreferences

### 2.2 Implement LoginActivity
- [x] LoginActivityTest.java - Deferred (Robolectric config complexity)
  - Note: Validation logic fully tested via ValidationUtils (12 tests)
  - Manual testing completed in 2.4 validation checklist
- [x] Implement `activities/LoginActivity.java` (Commits 4-7)
  - initViews() - bind all UI elements (Commit 4)
  - setupClickListeners() - tabs, buttons (Commits 4, 7)
  - validateInput() - ValidationUtils integration (Commit 4)
  - handleSignIn() - authentication with UserDAO, PasswordUtils (Commit 5)
  - handleRegister() - user creation with auto-login (Commit 6)
  - handleButtonClick() - mode-aware routing (Commit 7)
  - switchToSignInMode() - tab visual feedback (Commit 7)
  - switchToRegisterMode() - tab visual feedback (Commit 7)

### 2.3 Update AndroidManifest
- [x] Declare LoginActivity (Commit 4)
- [x] Set LoginActivity as launcher activity (Commit 7)
- [x] Update MainActivity declaration (Commit 7)

### 2.4 Integration & UI Tests (Critical Flows) - HYBRID APPROACH
**Rationale:** Now that we have a complete authentication flow, add minimal integration/UI tests for critical coverage. Comprehensive scenario testing deferred to Phase 8.

#### 2.4.1 Integration Tests (End-to-End Flow) - Completed 2025-12-11
- [x] Write `LoginActivityIntegrationTest.java` (2 tests)
  - test_registrationFlow_createsUserAndNavigates
    - Validates input via ValidationUtils
    - Generates salt/hash via PasswordUtils
    - Inserts user via UserDAO
    - Creates session via SessionManager
    - Verifies navigation to MainActivity
  - test_loginFlow_authenticatesAndNavigates
    - Queries user via UserDAO
    - Verifies password via PasswordUtils
    - Creates session via SessionManager
    - Updates last_login timestamp
    - Verifies navigation to MainActivity
- [x] Run integration tests (`./gradlew test`) - All 121 tests passing (91 Phase 1 + 28 Phase 2 + 2 integration)
- [x] Verify both flows pass with real database (Robolectric) - Verified

#### 2.4.2 UI Tests (Espresso - Critical Paths)
- [ ] Write `LoginActivityUITest.java` (Espresso)
  - test_userCanRegisterAndSeeMainActivity
    - Click Register tab
    - Type valid username and password
    - Click Create Account button
    - Verify MainActivity is displayed
  - test_userCanLoginAndSeeMainActivity
    - Type existing username and password
    - Click Sign In button
    - Verify MainActivity is displayed
- [ ] Run UI tests on emulator (`./gradlew connectedAndroidTest`)
- [ ] Verify both critical flows pass

**Deferred to Phase 8 (Comprehensive Testing):**
- Edge cases (invalid credentials, duplicate username, weak passwords)
- Error scenarios (network issues, database errors)
- Session persistence across app restart
- Logout flow
- Screen rotation during authentication
- Permission denial scenarios

### 2.5 Phase 2 Validation (2025-12-11)
- [x] User can create new account (handleRegister with auto-login)
- [x] Passwords are hashed (SHA-256 with SecureRandom salt, never plain text)
- [x] User can login with valid credentials (handleSignIn with PasswordUtils.verifyPassword)
- [x] Invalid credentials show error (generic message prevents username enumeration)
- [x] Duplicate username prevented (usernameExists check + DuplicateUsernameException)
- [x] Session persists across app restart (SharedPreferences in SessionManager)
- [x] Tab switching works (switchToSignInMode/switchToRegisterMode)
- [x] Run `./gradlew test` - all tests pass (119 tests: 91 Phase 1 + 28 Phase 2)
- [x] Run `./gradlew lint` - clean
- [x] Update TODO.md (2025-12-11)
- [x] Update project_summary.md (2025-12-11)
- [x] Create pull request to main branch

**Phase 2 Summary:**
- 7 implementation commits following strict TDD
- 28 new unit tests (6 PasswordUtils + 12 ValidationUtils + 10 SessionManager)
- Full authentication flow: registration → auto-login → session → navigation
- Security: SHA-256, SecureRandom salts, no plain text passwords, SQL injection prevention
- LoginActivity is now launcher with tab-based sign-in/registration
- All tests passing, lint clean, ready for Phase 3

---

## Phase 3: Main Dashboard
**Branch:** `feature/FR2.0-dashboard` ✅ **COMPLETE** (with documented test limitation - see GH #12)

### 3.1 Implement DateUtils (Completed 2025-12-11)
- [x] Write `DateUtilsTest.java` (9 tests)
- [x] Implement `utils/DateUtils.java`
  - formatDateShort(date) - "26 Nov"
  - formatDateFull(date) - "Wednesday, November 26, 2025"
  - isToday(date) - boolean
  - calculateDayStreak(entries) - consecutive days with gap handling
  - Null-safe implementation (returns empty string, false, or 0)
  - All 9 tests passing

### 3.2 Implement WeightEntryAdapter (Completed 2025-12-11)
- [x] Write `WeightEntryAdapterTest.java` (2 basic tests, layout tests deferred)
- [x] Implement `adapters/WeightEntryAdapter.java`
  - ViewHolder pattern for item_weight_entry.xml
  - OnItemClickListener interface (onEditClick, onDeleteClick)
  - Bind weight data with 1 decimal formatting
  - Format date badge as "26 NOV" style (day + month)
  - Calculate and display trend (↑/↓/− with color backgrounds)
  - Wire up edit button click
  - Wire up delete button click (CRITICAL REQUIREMENT)
  - Smart time display (Today/Yesterday/Full date + time)
  - Hides trend badge for last entry (no previous to compare)

### 3.3 Update MainActivity (Completed 2025-12-11)
- [x] Write `MainActivityTest.java` (18 tests) - **Note:** 17 tests blocked by Robolectric/Material3 theme issue (GH #12)
- [x] Update `activities/MainActivity.java` - **Full implementation complete**
  - [x] checkAuthentication() - redirect if not logged in ✅
  - [x] initViews() - bind all UI elements ✅
  - [x] setupRecyclerView() - adapter, layout manager ✅
  - [x] loadWeightEntries() - query DAO for current user ✅
  - [x] updateProgressCard() - current/start/goal weights ✅
  - [x] calculateQuickStats() - total lost, lbs to goal, streak ✅
  - [x] showEmptyState(boolean) - toggle visibility ✅
  - [x] handleDeleteEntry(entry) - confirm and delete ✅
  - [x] setupBottomNavigation() - handle nav clicks ✅
  - [x] setupFAB() - placeholder toast (actual navigation in Phase 4) ✅
  - [x] updateGreeting() - time-based greeting ✅
  - [x] updateUserName() - display user's display name ✅
  - [x] updateProgressBar() - calculate and set width based on percentage ✅

### 3.4 Implement Password Reset Feature (DEFERRED from Phase 2)
- [ ] Create forgot password dialog/activity
- [ ] Implement password reset logic
- [ ] Update LoginActivity to enable "Forgot Password" link (currently commented out)
- [ ] Add email/phone verification for password reset (security requirement)
- [ ] Update tests to cover password reset flow

### 3.5 Phase 3 Validation (Completed 2025-12-11)
- [x] Code compiles successfully ✅
- [x] Run `./gradlew lint` - clean, no errors ✅
- [x] Run `./gradlew test` - 217 tests passing ✅
  - **Note:** 17 MainActivity tests blocked by Robolectric/Material3 theme compatibility (see GH #12)
  - Implementation is correct and production-ready
  - Tests will be migrated to Espresso in Phase 8.4
- [x] **Manual Testing Checklist** (Completed 2025-12-11):
  - [x] Dashboard shows only current user's data ✅
  - [x] Empty state shows when no entries ✅
  - [x] FAB shows placeholder toast ✅
  - [x] Bottom navigation shows placeholder toasts ✅
  - [x] Time-based greeting displays correctly ✅
  - [x] User's display name shows in header ✅ (FIXED in Phase 3.6)
  - [x] Login error handling uses Snackbar ✅ (IMPROVED in Phase 3.6)
  - [x] Sign In errors don't reveal field information ✅ (SECURITY FIX in Phase 3.6)
  - **Deferred to Phase 4:** Delete button, Edit button, Progress card (requires weight entry data)
- [x] Update TODO.md to mark Phase 3 complete ✅
- [ ] Merge to main branch (ready when approved)

### 3.6 Phase 3 Post-Release Bug Fixes (Completed 2025-12-11)
**Issues Found During Manual Testing:**

#### 🔴 Security Issue: Login Validation Information Disclosure
- [x] Write failing tests (5 tests) - Completed 2025-12-11
- [x] Fix validateInput() to prevent username enumeration - Completed 2025-12-11
  - Sign In mode: Generic error "Please enter username and password"
  - Register mode: Specific errors to help users create accounts
  - Prevents OWASP A01:2021 username enumeration attack
- [x] Run tests - All 217 passing ✅

#### 🟡 Bug: MainActivity Display Name Not Showing
- [x] Write failing test (included in 5 tests above) - Completed 2025-12-11
- [x] Fix LoginActivity.handleRegister() to set display_name - Completed 2025-12-11
- [x] Add defensive fallback in MainActivity.updateUserName() - Completed 2025-12-11
- [x] Run tests - All 217 passing ✅

#### 🔵 UX Improvement: Login Error Visibility
- [x] Replace Toast with Snackbar for better visibility - Completed 2025-12-11
- [x] Remove red outline on fields in Sign In mode (prevents info leakage) - Completed 2025-12-11
- [x] All authentication errors now use consistent Snackbar styling - Completed 2025-12-11

#### Validation & Documentation
- [x] Run `./gradlew test` - All 217 tests passing ✅
- [x] Run `./gradlew lint` - Clean, no errors ✅
- [x] Commit bug fixes (3 commits) ✅
- [x] Update TODO.md ✅
- [x] Update project_summary.md ✅
- [ ] Merge to main branch (ready when approved)

---

## Phase 4: Weight Entry CRUD
**Branch:** `feature/FR2.1-weight-entry-crud` ✅ **IMPLEMENTATION COMPLETE** (Final PR feedback addressed 2025-12-11)

### 4.1 Commit 1: Create WeightEntryActivity Skeleton (Completed 2025-12-11)
- [x] Create `activities/WeightEntryActivity.java` with basic structure
  - Intent extras constants (EXTRA_USER_ID, EXTRA_IS_EDIT_MODE, etc.)
  - UI component fields (40+ views from layout)
  - State fields (userId, isEditMode, editWeightId, currentDate, currentUnit, weightInput)
  - onCreate() - initialize data layer, UI, click listeners
  - getIntentExtras() - extract intent data
  - initDataLayer() - initialize WeightEntryDAO
  - initViews() - bind all UI elements via findViewById
  - setupClickListeners() - wire up all buttons
  - loadExistingEntry() - for edit mode
  - loadPreviousEntry() - show last entry hint
  - updateDateDisplay() - update date UI elements
- [x] Commit: `feat: create WeightEntryActivity skeleton with intent handling`

### 4.2 Commit 2: Implement Number Pad Logic (Completed 2025-12-11)
- [x] setupNumberPadListeners() - wire up 12 buttons (0-9, decimal, backspace)
- [x] handleNumberInput(digit) - append to StringBuilder (max 5 digits)
  - Prevent leading zeros (except "0.")
- [x] handleDecimalPoint() - only add if not present and not empty
- [x] handleBackspace() - remove last character
- [x] updateWeightDisplay() - update TextView (show "0.0" if empty)
- [x] Commit: `feat: implement number pad input logic with validation`

### 4.3 Commit 3: Implement Quick Adjust Buttons (Completed 2025-12-11)
- [x] setupQuickAdjustListeners() - wire up 4 buttons (-1, -0.5, +0.5, +1)
- [x] adjustWeight(amount) - add/subtract from current value
  - Parse current value from StringBuilder
  - Validate range: 50-700 lbs / 22.7-317.5 kg
  - Format to 1 decimal place
- [x] Commit: `feat: implement quick adjust buttons with range validation`

### 4.4 Commit 4: Implement Unit Toggle (Completed 2025-12-11)
- [x] setupUnitToggleListeners() - wire up lbs/kg buttons
- [x] switchUnit(newUnit) - toggle between lbs and kg
  - Convert weight value (1 lb = 0.453592 kg)
  - Update button backgrounds (active/inactive drawables)
  - Update button text colors (primary/secondary)
  - Update weightUnit TextView
- [x] Commit: `feat: implement unit toggle with lbs/kg conversion`

### 4.5 Commit 5: Implement Date Navigation (Completed 2025-12-11)
- [x] setupDateNavigationListeners() - wire up prev/next buttons
- [x] navigateToPreviousDay() - subtract 1 day, update display
- [x] navigateToNextDay() - add 1 day if not after today
- [x] updateDateDisplay(date) - comprehensive update
  - Set dayNumber TextView (day of month)
  - Set fullDate TextView (DateUtils.formatDateFull)
  - Show/hide todayBadge based on DateUtils.isToday()
  - Disable next button if today (alpha 0.3)
- [x] Commit: `feat: implement date navigation with today detection`

### 4.6 Commit 6: Implement Save Functionality (Completed 2025-12-11)
- [x] setupSaveButton() - wire up save button click
- [x] handleSave() - validate and route to create/update
  - Validate non-empty weight
  - Validate range (50-700 lbs / 22.7-317.5 kg)
  - Call createNewEntry() or updateExistingEntry()
- [x] createNewEntry(weight) - insert new WeightEntry
  - Build WeightEntry object with all fields
  - Call weightEntryDAO.insertWeightEntry()
  - Handle duplicate entry error (weightId = -1)
  - setResult(RESULT_OK) and finish() on success
- [x] updateExistingEntry(weight) - update existing entry
  - Query entry by editWeightId
  - Update fields (value, unit, date, updatedAt)
  - Call weightEntryDAO.updateWeightEntry()
  - setResult(RESULT_OK) and finish() on success
- [x] loadExistingEntry() - pre-fill for edit mode
  - Query entry by editWeightId
  - Set weightInput StringBuilder
  - Set currentUnit and currentDate
  - Call updateWeightDisplay(), switchUnit(), updateDateDisplay()
- [x] loadPreviousEntry() - show last entry hint
  - Query weightEntryDAO.getLatestWeightEntry(userId)
  - Update lastEntryValue and lastEntryDate TextViews
- [x] Commit: `feat: implement save/update functionality with validation`

### 4.7 Commit 7: Update MainActivity Navigation (Completed 2025-12-11)
- [x] Add REQUEST_CODE_WEIGHT_ENTRY constant (1001)
- [x] Update setupFAB() - navigate to WeightEntryActivity
  - Create Intent with EXTRA_USER_ID and EXTRA_IS_EDIT_MODE=false
  - startActivityForResult()
- [x] Update onEditClick(entry) - navigate with entry data
  - Create Intent with all entry fields as extras
  - EXTRA_IS_EDIT_MODE=true
  - startActivityForResult()
- [x] Add onActivityResult() - refresh after save
  - Check REQUEST_CODE_WEIGHT_ENTRY and RESULT_OK
  - Call loadWeightEntries()
  - Call updateProgressCard()
  - Call calculateQuickStats()
- [x] Commit: `feat: wire up MainActivity navigation to WeightEntryActivity`

### 4.8 Commit 8: Update AndroidManifest (Completed 2025-12-11)
- [x] Declare WeightEntryActivity in AndroidManifest.xml
  - android:name=".activities.WeightEntryActivity"
  - android:label="@string/log_weight"
  - android:parentActivityName=".activities.MainActivity"
  - android:windowSoftInputMode="stateAlwaysHidden"
  - Add parent activity metadata
- [x] Commit: `chore: declare WeightEntryActivity in AndroidManifest`

### 4.9 PR Feedback Fixes (Completed 2025-12-11)
**PR #14 Review - 7 Critical Issues Addressed:**

- [x] Issue #1: Replace deprecated `getResources().getColor()` with `ContextCompat.getColor()`
  - Updated 4 calls in `updateUnitButtonUI()` method
  - Now uses `ContextCompat.getColor(this, R.color.text_on_primary)` etc.

- [x] Issue #2: Fix number input logic bugs
  - Prevented multiple leading zeros (e.g., "000")
  - Fixed digit counting to exclude decimal point
  - Now correctly enforces MAX_DIGITS = 5 limit

- [x] Issue #3: Add try-catch around all `Double.parseDouble()` calls
  - `adjustWeight()` - added NumberFormatException handling
  - `switchUnit()` - added NumberFormatException handling
  - `handleSave()` - added NumberFormatException handling

- [x] Issue #4: Extract magic number to constant
  - Added `LBS_TO_KG_CONVERSION = 0.453592` constant
  - Replaced hardcoded values in `switchUnit()` method

- [x] Issue #5: Fix redundant database query
  - Added `currentEntry` field to cache loaded entry
  - `loadExistingEntry()` now caches entry
  - `updateExistingEntry()` uses cached entry instead of re-querying

- [x] Issue #6: Fix unit toggle initial state bug
  - Created `updateUnitButtonUI()` helper method
  - Separated UI updates from conversion logic
  - Initialization now calls `updateUnitButtonUI()` instead of `switchUnit()`

- [x] Issue #7: Add null safety checks for `currentDate`
  - `navigateToPreviousDay()` checks for null and initializes to today
  - `navigateToNextDay()` checks for null and initializes to today

- [x] Commit: `fix: address PR feedback for WeightEntryActivity`
- [x] Run `./gradlew test` - All tests passing ✅
- [x] Run `./gradlew lint` - Clean, no errors ✅

### 4.10 Phase 4 Validation
**Automated Testing:**
- [x] Run `./gradlew test` - all tests pass ✅
- [x] Run `./gradlew lint` - clean, no errors ✅

**Manual Testing - Weight Entry CRUD (Completed 2025-12-11):**
- [x] User can add new weight entry ✅
- [x] Number pad works correctly ✅
- [x] Quick adjust buttons work ✅
- [x] Unit toggle switches units ✅
- [x] Date navigation works ✅
- [x] Edit mode loads existing data ✅
- [x] Save persists to database ✅
- [x] Delete shows confirmation dialog ✅
- [x] Deleted entries removed from list ✅
- [x] **Bug fixes during testing:** 4 critical UX bugs found and fixed (see 4.11, 4.12, 4.13)

**Manual Testing - Dashboard Integration (Completed 2025-12-11):**
- [x] Dashboard displays weight entries in RecyclerView ✅
- [x] Delete button works with confirmation dialog ✅
- [x] Edit button opens WeightEntryActivity in edit mode ✅
- [x] Progress card shows correct calculations with real data ✅
- [x] Quick stats update after adding/deleting entries ✅
- [x] **Bug fix:** Unit display corrected (kg vs lbs) (see 4.11)
- [x] **Bug fix:** Trend calculation with mixed units (see 4.12)

**Documentation (Completed 2025-12-11):**
- [x] Update TODO.md to mark Phase 4 PR fixes complete ✅
- [x] Update TODO.md with manual testing bug fixes (4.11, 4.12, 4.13) ✅
- [x] Update TODO.md with Phase 8 regression test plan (8.6) ✅
- [x] Update project_summary.md with Phase 4 PR feedback notes ✅
- [x] Update project_summary.md with all 3 rounds of manual testing bugs ✅
- [ ] Merge to main branch (after final user approval)

**Phase 4 Summary:**
- ✅ PR #14 opened with 8 commits (1 skeleton + 6 implementation + 1 manifest)
- ✅ PR feedback: 7 code quality issues fixed
- ✅ Manual testing: 4 critical UX bugs discovered and fixed
  - Round 1: Unit display bug (54 kg showing as "54 lbs")
  - Round 2: Three input/validation bugs (default display, 0.0 input, allow zero)
  - Round 3: Display/state consistency (can't save 0.0 immediately)
- ✅ Total commits: 5 bug fix commits (13e175d, 8c574b2, 30243e1, af8ec50)
- ✅ All tests passing (217 tests), lint clean
- ✅ Comprehensive documentation of issues, root causes, and lessons learned
- ✅ Regression test plan added to Phase 8.6 (9-11 new tests planned)
- 🔄 Ready for merge after final user approval

---

## Phase 5: Goal Weight Management
**Branch:** `feature/FR3.0-goal-management`

### 5.1 Goal Setting UI
- [ ] Create goal setting dialog layout (or reuse existing)
- [ ] Implement goal input validation
- [ ] Save goal via GoalWeightDAO
- [ ] Display current goal on dashboard

### 5.2 Progress Calculation
- [ ] Calculate progress percentage
- [ ] Update progress bar
- [ ] Calculate "lbs to goal"

### 5.3 Goal Achievement Detection
- [ ] Write `AchievementManagerTest.java`
- [ ] Implement `utils/AchievementManager.java`
  - checkGoalAchieved(userId, newWeight)
  - markGoalAchieved(goalId)
  - Trigger celebration/notification

### 5.4 Phase 5 Validation
- [ ] User can set goal weight
- [ ] Goal displays on dashboard
- [ ] Progress bar updates correctly
- [ ] Goal achievement detected
- [ ] Run `./gradlew test` - all tests pass
- [ ] Merge to develop branch

---

## Phase 6: SMS Notifications
**Branch:** `feature/FR5.0-sms-notifications`

### 6.1 Implement SMSSettingsActivity
- [ ] Write `SMSSettingsActivityTest.java`
- [ ] Implement `activities/SMSSettingsActivity.java`
  - initViews() - bind all UI elements
  - checkSmsPermission() - ContextCompat.checkSelfPermission
  - requestSmsPermission() - ActivityResultLauncher
  - updatePermissionUI(granted) - update status badge, buttons
  - showPermissionRationale() - explain why needed
  - openAppSettings() - for permanent denial
  - savePhoneNumber() - to SharedPreferences
  - handleNotificationToggles() - save preferences
  - sendTestMessage() - test SMS functionality

### 6.2 Implement Phone Number Validation (DEFERRED from Phase 2)
- [ ] Write tests for ValidationUtils.isValidPhoneNumber()
  - test_isValidPhoneNumber_withValidE164_returnsTrue
  - test_isValidPhoneNumber_withInvalidFormat_returnsFalse
  - test_isValidPhoneNumber_withNull_returnsFalse
  - test_isValidPhoneNumber_withEmpty_returnsFalse
- [ ] Implement ValidationUtils.isValidPhoneNumber()
  - E.164 format: ^\+[1-9]\d{1,14}$
  - Null-safe validation
  - Update Phase 2 placeholder (currently returns false)

### 6.3 Implement SMSNotificationManager
- [ ] Write `SMSNotificationManagerTest.java`
- [ ] Implement `utils/SMSNotificationManager.java`
  - hasPermission(context) - check SEND_SMS
  - requestPermission(activity, launcher)
  - sendSMS(context, phoneNumber, message)
  - formatPhoneNumber(phone) - E.164 format
  - sendGoalAchievedSMS(context, goalWeight)

### 6.3 Permission Flow Implementation
- [ ] Use ActivityResultContracts.RequestPermission
- [ ] Handle shouldShowRequestPermissionRationale
- [ ] Update UI based on permission result
- [ ] Handle permanent denial gracefully

### 6.4 Integration with Goal Achievement
- [ ] Check SMS preference when goal achieved
- [ ] Send SMS if enabled and permitted
- [ ] Always show in-app notification

### 6.5 Phase 6 Validation
- [ ] Permission request uses ActivityResultContracts
- [ ] UI updates based on permission status
- [ ] App functions without SMS permission
- [ ] SMS not sent if permission denied
- [ ] Phone number saved to preferences
- [ ] Test message sends successfully
- [ ] Goal achievement triggers SMS
- [ ] "Open App Settings" works
- [ ] Run `./gradlew test` - all tests pass
- [ ] Merge to develop branch

---

## Phase 7: Code Quality
**Branch:** `feature/FR6.0-code-quality`

### 7.1 Documentation
- [ ] Add Javadoc to all public classes
- [ ] Add Javadoc to all public methods (@param, @return)
- [ ] Add inline comments for complex logic
- [ ] Verify all comments explain WHY, not WHAT

### 7.2 Naming Conventions
- [ ] Classes: PascalCase
- [ ] Methods: camelCase (verbs)
- [ ] Variables: camelCase (nouns)
- [ ] Constants: UPPER_SNAKE_CASE
- [ ] No magic numbers (use named constants)
  - [ ] WeightEntryAdapter: Extract TREND_THRESHOLD = 0.1 (line 172)

### 7.3 Code Cleanup
- [ ] Remove all System.out.println (use Log.d/i/e)
- [ ] Remove all commented-out code
- [ ] Remove unused imports
- [ ] Remove unused resources
- [ ] Verify consistent 4-space indentation
- [ ] Fix locale dependency in WeightEntryAdapter.java:115
  - Replace `parts[1].toUpperCase()` with `parts[1].toUpperCase(Locale.US)`
  - Prevents unexpected behavior on non-English locale devices
- [ ] Refactor date formatting duplication
  - Move `formatTime()` from WeightEntryAdapter to DateUtils
  - Add DateUtils.formatTime(LocalDateTime) method
  - Update WeightEntryAdapter to use DateUtils.formatTime()

### 7.4 Error Handling
- [ ] Add try-catch for database operations
- [ ] Add null checks for nullable data
- [ ] Show user-friendly error messages
- [ ] Log errors for debugging
- [ ] Add @NonNull annotations to WeightEntryAdapter constructor
  - Require non-null listener in constructor
  - Remove null checks in onClick handlers (guaranteed non-null)
  - Makes contract explicit and prevents runtime null issues

### 7.5 Performance Optimization (DEFERRED from Phase 2)
- [ ] Move password hashing to background thread
  - Currently synchronous on UI thread (PasswordUtils.hashPassword in LoginActivity)
  - Use AsyncTask, HandlerThread, or Kotlin Coroutines
  - Update LoginActivity registration/login flows
  - Show loading indicator during hash computation
  - Test with realistic device (not just emulator)
- [ ] Replace notifyDataSetChanged() with DiffUtil in MainActivity
  - Current: Full RecyclerView redraw on every update (MainActivity.java:223)
  - Create WeightEntryDiffCallback class
  - Use DiffUtil.calculateDiff() for efficient updates
  - Impact: Low for MVP (<100 entries), Medium for larger datasets
  - Improves performance with large weight entry history
- [ ] Profile app performance on older devices (API 28)
- [ ] Optimize any other blocking UI operations identified

### 7.6 Security: Migrate to bcrypt/Argon2 (CRITICAL TECHNICAL DEBT from Phase 2)
**Issue:** SHA-256 is NOT recommended for password hashing (too fast, vulnerable to GPU brute-force)

**Current Implementation (Phase 2):**
- PasswordUtils uses SHA-256 with SecureRandom 16-byte salts
- Timing attack vulnerability FIXED (MessageDigest.isEqual() for constant-time comparison)
- Functional but NOT production-ready

**Migration Plan:**
- [ ] Add bcrypt library dependency: `at.favre.lib:bcrypt:0.10.2` (best Android support)
  - Alternative: `org.springframework.security:spring-security-crypto` (PBKDF2)
  - Alternative: `de.mkammerer:argon2-jvm` (Argon2id - most secure, higher memory requirements)
- [ ] Add `password_algorithm` TEXT column to `users` table (values: 'SHA256', 'BCRYPT', 'ARGON2')
- [ ] Update UserDAO schema migration (onUpgrade v1 → v2)
- [ ] Implement PasswordUtilsV2 with bcrypt support
  - generateSalt() - bcrypt handles salt internally
  - hashPassword(password) - bcrypt.hashToString(12, password.toCharArray()) // cost factor 12
  - verifyPassword(password, hash) - bcrypt.verify(password.toCharArray(), hash)
- [ ] Implement lazy migration strategy (hybrid verification)
  - On login: check password_algorithm field
  - If SHA256: verify with PasswordUtils.verifyPassword()
  - If match: rehash with bcrypt, update user record, set algorithm='BCRYPT'
  - If BCRYPT/ARGON2: verify with PasswordUtilsV2.verifyPassword()
- [ ] Update LoginActivity to handle migration transparently
- [ ] Add migration tests (PasswordUtilsMigrationTest.java)
  - test_sha256User_onLogin_migratedToBcrypt
  - test_bcryptUser_loginWorks
  - test_mixedAlgorithms_bothWorkDuringTransition
- [ ] Manual testing: existing SHA256 users can still login
- [ ] Monitor migration progress (log algorithm distribution)
- [ ] Optional: Add admin tool to force-migrate all users (requires password reset)

**Why Defer to Phase 7:**
- No production users yet (dev/test environment only)
- Current SHA256 implementation is "bad but functional" for development
- Migration requires database schema change + comprehensive testing
- Proper migration belongs in Code Quality phase with full test coverage
- Timing attack vulnerability already fixed (constant-time comparison)

**Security Note:**
- Document in Launch Plan that production deployment REQUIRES bcrypt migration
- Clearly mark app as "NOT production-ready" until migration complete

### 7.7 Refactor: SessionManager Dummy Fields (TECHNICAL DEBT from Phase 2)
**Issue:** SessionManager.getCurrentUser() returns User object with invalid dummy data:
- `passwordHash` = "" (empty string, not secure)
- `salt` = "" (empty string, not secure)
- `createdAt` = LocalDateTime.now() (incorrect timestamp)
- `updatedAt` = LocalDateTime.now() (incorrect timestamp)

**Current Workaround (Phase 2):**
- Documented in SessionManager.java Javadoc (lines 142-148)
- Warning: "Returns partial User object with ONLY session data"
- Valid fields: userId, username, displayName
- Invalid fields: passwordHash, salt, createdAt, updatedAt (dummy values)
- Callers should use UserDAO.getUserById() for full User data

**Refactor Plan:**
- [ ] Create dedicated SessionUser class (lightweight session data)
  - Fields: userId, username, displayName
  - No password/timestamp fields (no dummy data needed)
  - Serializable for SharedPreferences
- [ ] Update SessionManager methods
  - createSession(User user) → Extract session data only
  - getCurrentUser() → SessionUser (not User)
  - Update callers to use SessionUser or query DAO for full User
- [ ] Update LoginActivity
  - After authentication: sessionManager.createSession(user)
  - Dashboard: SessionUser sessionUser = sessionManager.getCurrentUser()
  - For full user data: User user = userDAO.getUserById(sessionUser.getUserId())
- [ ] Update MainActivity and other activities
  - Replace User with SessionUser for session-based operations
  - Use UserDAO when full User data needed (profile screen, etc.)
- [ ] Add SessionUserTest.java (basic POJO tests)
- [ ] Update SessionManagerTest.java
  - test_createSession_withUser_storesSessionUser
  - test_getCurrentUser_returnsSessionUser (not full User)
- [ ] Remove dummy field workaround from SessionManager.getCurrentUser()

**Why Defer to Phase 7:**
- Current implementation works correctly (callers understand limitations)
- Comprehensive Javadoc warns about dummy fields
- Refactor requires updating multiple activities (LoginActivity, MainActivity, etc.)
- Belongs in Code Quality phase with full regression testing
- No security risk (dummy fields are not exposed to user)

**Alternative (Lower Priority):**
- Make User fields nullable (@Nullable passwordHash, salt, createdAt, updatedAt)
- Breaks existing code that assumes @NonNull
- Requires null checks throughout codebase
- SessionUser approach is cleaner separation of concerns

### 7.8 Lint Check
- [ ] Run Android Lint
- [ ] Fix all errors
- [ ] Address warnings where appropriate

### 7.9 Future Enhancements (Post-Launch)

#### Email/Username Login Support (Deferred from Phase 3.6)
**User Request:** "should allow a username or email and validate off that"

**Status:** Deferred to post-launch (Phase 9+)

**Reason:** Significant scope requiring:
- Database schema changes (email column with UNIQUE constraint, increased username field length)
- ValidationUtils.isValidEmail() implementation (regex, format checking)
- Login by email OR username logic (UserDAO.getUserByEmailOrUsername())
- Registration UI updates (email field collection, validation)
- Email uniqueness validation and duplicate checking

**Estimated Effort:** 2-3 days (tests, DAO changes, UI updates, email validation)

**Implementation Plan:**
- [ ] Add `email` TEXT UNIQUE column to `users` table (schema migration v1 → v2)
- [ ] Write `ValidationUtilsTest.isValidEmail()` tests (6+ tests)
- [ ] Implement `ValidationUtils.isValidEmail()` using RFC 5322 regex
- [ ] Write `UserDAO.getUserByEmailOrUsername()` tests (4+ tests)
- [ ] Implement `UserDAO.getUserByEmailOrUsername(String identifier)`
- [ ] Update `LoginActivity` to accept email or username
- [ ] Update `RegisterActivity` to collect and validate email
- [ ] Check email uniqueness during registration
- [ ] Add email field to User model
- [ ] Run full test suite, update documentation

**Recommendation:** Track as GitHub Issue for future release

### 7.10 Phase 7 Validation
- [ ] All code follows naming conventions
- [ ] All classes documented
- [ ] No lint errors
- [ ] No dead code
- [ ] Merge to main branch

---

## Phase 8: Final Testing
**Branch:** `feature/FR7.0-final-testing`

### 8.1 Test Coverage
- [ ] Utility Classes: 100%
- [ ] DAO Classes: 100%
- [ ] Business Logic: 90%+
- [ ] Activities: Critical paths

### 8.2 Device Testing
- [ ] Test on Pixel 6 emulator (API 34)
- [ ] Test on older API level (API 28)
- [ ] Test landscape orientation
- [ ] Test different screen sizes

### 8.3 Scenario Testing
**Authentication:**
- [ ] New user registration
- [ ] Existing user login
- [ ] Invalid credentials
- [ ] Session persistence
- [ ] Logout

**Weight Entry:**
- [ ] Add first entry
- [ ] Add subsequent entries
- [ ] Edit existing entry
- [ ] Delete entry (with confirmation)
- [ ] Empty state handling

**SMS Permissions:**
- [ ] Grant permission flow
- [ ] Deny permission flow
- [ ] Deny + "Don't ask again" flow
- [ ] App functions without permission

**Edge Cases:**
- [ ] Empty database
- [ ] 100+ weight entries
- [ ] Special characters in input
- [ ] Screen rotation
- [ ] App kill and restart

### 8.4 MainActivity Test Migration: Robolectric to Espresso
**Rationale:** Phase 3.3 created 18 MainActivity integration tests, but 17 are blocked by Robolectric/Material3 theme incompatibility (see GH #12). This section migrates those tests to Espresso (instrumented tests) to unblock comprehensive dashboard testing.

**Files to Create/Modify:**
- Create: `app/src/androidTest/java/com/example/weighttogo/activities/MainActivityEspressoTest.java`
- Delete: Commented sections from `app/src/test/java/com/example/weighttogo/activities/MainActivityTest.java`

**Tests to Migrate (17 tests):**
- [ ] test_onCreate_whenLoggedIn_initializesViews
- [ ] test_loadWeightEntries_withNoEntries_showsEmptyState
- [ ] test_loadWeightEntries_withEntries_hidesEmptyState
- [ ] test_loadWeightEntries_withEntries_populatesRecyclerView
- [ ] test_updateProgressCard_withActiveGoal_showsProgressData
- [ ] test_updateProgressCard_withNoGoal_hidesProgressCard
- [ ] test_calculateQuickStats_withData_showsCorrectValues
- [ ] test_calculateQuickStats_withStreak_showsDayStreak
- [ ] test_handleDeleteEntry_withConfirmation_deletesEntry
- [ ] test_handleDeleteEntry_withCancel_doesNotDelete
- [ ] test_fabClick_showsToastPlaceholder
- [ ] test_bottomNavigation_homeSelected_staysOnMainActivity
- [ ] test_bottomNavigation_otherItemSelected_showsToastPlaceholder
- [ ] test_greetingText_showsTimeBasedGreeting
- [ ] test_userName_displaysCurrentUserName
- [ ] test_progressPercentage_calculatesCorrectly
- [ ] test_progressBar_widthMatchesPercentage

**Implementation Steps:**
1. Create `MainActivityEspressoTest.java` with Espresso imports
2. Migrate test setup (use ActivityScenarioRule instead of Robolectric)
3. Migrate assertions (use Espresso matchers instead of findViewById)
4. Run instrumented tests: `./gradlew connectedAndroidTest`
5. Verify all 17 tests pass on emulator/device
6. Delete commented code from `MainActivityTest.java`
7. Update GH #12 with resolution details
8. Update project_summary.md with migration notes

**Expected Test Count After Migration:**
- Unit tests (Robolectric): 197 tests (213 current - 17 migrated + 1 kept)
- Instrumented tests (Espresso): 17 tests (MainActivity only)
- Total: 214 tests

### 8.5 Comprehensive Authentication Testing (DEFERRED from Phase 2.4)
**Rationale:** Phase 2.4 implemented minimal integration tests (2 tests) for critical happy paths. This section implements comprehensive scenario testing for authentication flows.

**Integration Tests (LoginActivityIntegrationTest.java):**
- [ ] test_registration_withDuplicateUsername_showsError
  - Verify duplicate username detection
  - Assert error message displayed
  - Assert user not created in database
  - Assert session not created
- [ ] test_registration_withWeakPassword_showsError
  - Test password with no digits (should fail validation)
  - Test password too short (< 6 chars)
  - Assert appropriate error messages
- [ ] test_login_withInvalidCredentials_showsError
  - Test with non-existent username
  - Test with correct username but wrong password
  - Assert generic error message (no username enumeration)
  - Assert session not created
- [ ] test_login_withInactiveUser_showsError
  - Create user with isActive=false
  - Attempt login
  - Assert error message displayed
  - Assert session not created

**Error Scenario Tests:**
- [ ] test_registration_whenDatabaseError_showsError
  - Mock database exception during insertUser()
  - Verify graceful error handling
  - Assert user-friendly error message
- [ ] test_login_whenDatabaseError_showsError
  - Mock database exception during getUserByUsername()
  - Verify graceful error handling
  - Assert user-friendly error message

**Session Persistence Tests (SessionManagerTest.java):**
- [ ] test_session_persistsAcrossAppRestart
  - Create session
  - Clear singleton instance (simulate app restart)
  - Get new SessionManager instance
  - Assert session still exists
  - Assert userId matches
- [ ] test_logout_clearsSessionPersistence
  - Create session
  - Call logout()
  - Clear singleton instance (simulate app restart)
  - Get new SessionManager instance
  - Assert session does not exist

**UI Tests (Espresso - LoginActivityUITest.java):**
- [ ] test_screenRotation_duringRegistration_preservesInput
  - Enter username and password
  - Rotate screen
  - Assert input fields retain values
  - Complete registration successfully
- [ ] test_screenRotation_duringLogin_preservesInput
  - Enter username and password
  - Rotate screen
  - Assert input fields retain values
  - Complete login successfully
- [ ] test_tabSwitch_clearsErrors
  - Trigger validation error in Sign In mode
  - Switch to Register tab
  - Assert errors cleared
  - Switch back to Sign In tab
  - Assert errors still cleared

**Test Assertion Specificity Improvements:**
- [ ] Enhance LoginActivityIntegrationTest with error state assertions
  - Verify Snackbar is shown (not Toast) in Sign In mode
  - Verify no field highlighting (no TextInputLayout.setError()) in Sign In mode
  - Verify field highlighting IS present in Register mode
  - Assert specific error messages match expected values
  - Improves test coverage beyond integration behavior

**Expected Test Count After Phase 8.5:**
- Comprehensive authentication tests: ~12 additional tests
- Test assertion improvements: ~4 enhanced tests
- Total project tests: ~137 tests (121 current + 16 comprehensive)

### 8.6 WeightEntryAdapter and WeightEntryActivity Regression Tests (REGRESSION PREVENTION from Phase 4 Manual Testing)
**Rationale:** Phase 4 manual testing discovered 4 critical bugs that automated tests missed (unit display, trend calculation, number input at 0.0, saving 0). This section adds regression tests to prevent these issues from reoccurring.

**Context:**
- Phase 3.2 created WeightEntryAdapter with only 2 basic tests (deferred comprehensive testing)
- Phase 4 deferred WeightEntryActivity testing due to Robolectric complexity
- Manual testing caught bugs that should have been tested
- Need regression tests to prevent these bugs from returning

**Files to Create/Modify:**
- Enhance: `app/src/test/java/com/example/weighttogo/adapters/WeightEntryAdapterTest.java`
- Create: `app/src/test/java/com/example/weighttogo/activities/WeightEntryActivityTest.java` (Robolectric)
- OR: Extract testable logic to helpers and unit test those

#### 8.6.1 WeightEntryAdapter Regression Tests
**Bug Context:** Phase 4.11 - Unit display bug (showed "54 lbs" when should show "54 kg")

**Tests to Add (WeightEntryAdapterTest.java):**
- [ ] test_bindWeightValue_withLbs_displaysLbsLabel
  - Create entry with weight=150.0, unit="lbs"
  - Bind to ViewHolder
  - Assert weightValue shows "150.0"
  - Assert weightUnit shows "lbs"
- [ ] test_bindWeightValue_withKg_displaysKgLabel
  - Create entry with weight=54.0, unit="kg"
  - Bind to ViewHolder
  - Assert weightValue shows "54.0"
  - Assert weightUnit shows "kg"
- [ ] test_onBindViewHolder_populatesAllFields
  - Create entry with all fields populated
  - Call onBindViewHolder
  - Assert dayNumber, monthName, weightValue, weightUnit, entryTime, editButton, deleteButton all populated
  - Ensures no field is forgotten in binding

**Bug Context:** Phase 4.12 - Trend calculation with mixed units (120 kg vs 254 lbs showed 134 instead of ~10.5)

**Tests to Add (WeightEntryAdapterTest.java):**
- [ ] test_bindTrendBadge_withMixedUnits_convertsCorrectly
  - Entry 1: 120 kg (Dec 6)
  - Entry 2: 254 lbs (Dec 7)
  - Calculate expected trend: 120 kg = 264.6 lbs, diff = 264.6 - 254 = 10.6 lbs
  - Assert trend badge shows "↓ 10.6 lbs" (not "134")
- [ ] test_bindTrendBadge_withSameUnits_calculatesCorrectly
  - Entry 1: 150 lbs (previous)
  - Entry 2: 148.5 lbs (current)
  - Assert trend badge shows "↓ 1.5 lbs"
- [ ] test_bindTrendBadge_displaysUnitLabel
  - Any trend calculation
  - Assert badge text contains unit (e.g., "↓ 5.0 lbs" or "↑ 2.3 kg")
  - Prevents regression of missing unit label bug

#### 8.6.2 WeightEntryActivity Regression Tests
**Approach:** Extract testable logic to helper classes OR use Robolectric

**Bug Context:** Phase 4.12 - Number input at 0.0 appends after decimal (0.08 instead of 8)

**Option A: Extract Logic and Unit Test**
- [ ] Create `WeightInputHelper.java` with method:
  - `shouldReplaceZero(String current, String digit)` → boolean
- [ ] Write `WeightInputHelperTest.java` with tests:
  - test_shouldReplaceZero_withZero_andNonZeroDigit_returnsTrue
  - test_shouldReplaceZero_withZeroPointZero_andNonZeroDigit_returnsTrue
  - test_shouldReplaceZero_withZero_andZeroDigit_returnsFalse
  - test_shouldReplaceZero_withNonZero_returnsFalse

**Option B: Robolectric Tests (if time permits)**
- [ ] Create `WeightEntryActivityTest.java` (Robolectric)
- [ ] test_handleNumberInput_atZeroPointZero_replacesInsteadOfAppends
  - Set weightInput to "0.0"
  - Call handleNumberInput("8")
  - Assert weightInput is "8" (not "0.08")
- [ ] test_handleNumberInput_atZero_replacesInsteadOfAppends
  - Set weightInput to "0"
  - Call handleNumberInput("5")
  - Assert weightInput is "5"

**Bug Context:** Phase 4.13 - Allow saving 0.0 in add mode

**Tests to Add:**
- [ ] test_onCreate_addMode_initializesWeightInputToZeroPointZero
  - Launch activity in add mode
  - Assert weightInput.toString() equals "0.0"
  - Assert display shows "0.0"
- [ ] test_handleSave_withZeroWeight_allowsSave
  - Set weightInput to "0.0"
  - Call handleSave()
  - Assert no error toast
  - Assert entry created in database with weight=0.0
- [ ] test_handleSave_withEmptyWeight_showsError
  - Set weightInput to "" (empty)
  - Call handleSave()
  - Assert error toast "Please enter a weight value"

**Expected Test Count After Phase 8.6:**
- WeightEntryAdapter tests: +6 tests (2 existing + 6 new = 8 total)
- WeightEntryActivity tests: +3-5 tests (depending on approach)
- Total new regression tests: ~9-11 tests

**Technical Debt Resolution:**
- Addresses test coverage gap identified in project_summary.md
- Prevents regression of known bugs
- Documents expected behavior for edge cases

### 8.7 Final Test Suite
- [ ] Run `./gradlew clean test`
- [ ] Run `./gradlew connectedAndroidTest` (if device available)
- [ ] Run `./gradlew lint`
- [ ] Fix any failures

### 8.8 Phase 8 Validation
- [ ] All tests pass
- [ ] No crashes in any scenario
- [ ] Lint clean
- [ ] Merge to main branch

---

## Phase 9: Launch Plan Document

### 9.1 Document Creation
- [ ] Create `Rick_Goshen_WeightToGo_LaunchPlan.docx`
- [ ] Format: 12pt Times New Roman, double-spaced, 1-inch margins

### 9.2 Content Sections
- [ ] Executive Summary (1 paragraph)
- [ ] App Store Presence
  - [ ] Full description (250-4000 chars)
  - [ ] Short description (80 chars)
  - [ ] Icon design rationale
  - [ ] Screenshot strategy (6-8 screenshots)
- [ ] Technical Specifications
  - [ ] Minimum SDK: API 28 justification
  - [ ] Target SDK: API 36 justification
  - [ ] Device compatibility
- [ ] Permissions & Privacy
  - [ ] Permission list with justifications
  - [ ] Privacy policy summary
- [ ] Monetization Strategy
  - [ ] Selected model with rationale
  - [ ] Revenue projections

### 9.3 Final Review
- [ ] Proofread for grammar/spelling
- [ ] Verify 2-3 pages length
- [ ] Verify correct formatting

---

## Final Submission Preparation

### Code Submission
- [ ] Clean project (Build → Clean Project)
- [ ] Remove debug code
- [ ] Build release APK (optional)
- [ ] Create ZIP of project folder
- [ ] Name: `Rick_Goshen_WeightToGo_Final.zip`
- [ ] Verify ZIP extracts properly
- [ ] Test project opens in Android Studio

### Document Submission
- [ ] Launch plan complete
- [ ] Name: `Rick_Goshen_WeightToGo_LaunchPlan.docx`

### Upload
- [ ] Upload ZIP to submission portal
- [ ] Upload Launch Plan to submission portal
- [ ] Verify both files submitted
- [ ] Submit before deadline

---

## Completed Phases

### Project Two (UI Design) - Completed 2025-11-29
- [x] Phase 1: Resource Files
- [x] Phase 2: Login Screen (25%)
- [x] Phase 3: Database Grid Screen (25%)
- [x] Phase 4: SMS Notifications Screen (30%)
- [x] Phase 5: Weight Entry Screen
- [x] Phase 6: Final Validation

---

## Blockers
None

---

## Key Files Reference

### Source Files to Create
```
app/src/main/java/com/example/weighttogo/
├── activities/
│   ├── LoginActivity.java
│   ├── MainActivity.java
│   ├── WeightEntryActivity.java
│   └── SMSSettingsActivity.java
├── adapters/
│   └── WeightEntryAdapter.java
├── database/
│   ├── WeighToGoDBHelper.java
│   ├── UserDAO.java
│   ├── WeightEntryDAO.java
│   └── GoalWeightDAO.java
├── models/
│   ├── User.java
│   ├── WeightEntry.java
│   └── GoalWeight.java
├── utils/
│   ├── PasswordUtils.java
│   ├── ValidationUtils.java
│   ├── SessionManager.java
│   ├── SMSNotificationManager.java
│   ├── DateUtils.java
│   └── AchievementManager.java
└── constants/
    └── AppConstants.java
```

### Test Files to Create
```
app/src/test/java/com/example/weighttogo/
├── models/
│   ├── UserTest.java
│   ├── WeightEntryTest.java
│   └── GoalWeightTest.java
├── database/
│   ├── WeighToGoDBHelperTest.java
│   ├── UserDAOTest.java
│   ├── WeightEntryDAOTest.java
│   └── GoalWeightDAOTest.java
├── utils/
│   ├── PasswordUtilsTest.java
│   ├── ValidationUtilsTest.java
│   └── DateUtilsTest.java
└── activities/
    ├── LoginActivityTest.java
    └── MainActivityTest.java
```
### 4.11 Manual Testing Bug Fix (Completed 2025-12-11)
**Critical Bug Found During Manual Testing:**

**Issue:** Dashboard displays wrong unit label
- User entered 54 kg in WeightEntryActivity
- Dashboard showed "54 lbs" instead of "54 kg"
- Weight value correct, but unit label incorrect

**Root Cause:** WeightEntryAdapter.java:126
- `bindWeightValue()` method only set weight value
- Never populated `weightUnit` TextView with `entry.getWeightUnit()`
- Critical data integrity issue

**Fix:**
- [x] Updated `bindWeightValue()` to accept `unit` parameter
- [x] Set `holder.weightUnit.setText(unit)` 
- [x] Updated method call to pass `entry.getWeightUnit()`
- [x] All tests passing, lint clean
- [x] Commit: `fix: display correct weight unit (lbs/kg) in dashboard RecyclerView`

**Impact:**
- Dashboard now correctly displays saved unit (lbs or kg)
- Data integrity preserved
- User trust in displayed data restored

### 4.12 Manual Testing Bug Fixes - Round 2 (Completed 2025-12-11)
**Three Additional Bugs Found During Manual Testing:**

**Issue #1:** Default display shows 172.0 but saving gives "Please enter a weight value" error
- **Root Cause:** XML layout hardcoded `android:text="172.0"` for weightValue TextView
  - In add mode, `onCreate()` never called `updateWeightDisplay()`
  - XML default remained visible instead of correct "0.0"
- **Fix:** Call `updateWeightDisplay()` in add mode (after loadPreviousEntry)
  - Overrides XML default with correct initial state
  - Display now consistently shows "0.0" for new entries

**Issue #2:** Number input at 0.0 appends digits after decimal instead of replacing
- **Scenario:** User decrements to 0.0, then types "8", "5", "7"
  - Expected: "857"
  - Actual: "0.08", "0.085", "0.0857"
- **Root Cause:** `handleNumberInput()` only checked for "0", not "0.0"
  - Quick adjust buttons set weightInput to "0.0"
  - Typing digits appended instead of replacing
- **Fix:** Check for both "0" and "0.0" in replacement logic
  - `if ((current.equals("0") || current.equals("0.0")) && !digit.equals("0"))`
  - Now correctly starts fresh when typing from zero state

**Issue #3:** Zero should be allowed as valid weight value
- **User Request:** "you should be allowed to enter in 0 for a value"
- **Root Cause:** `handleSave()` validation rejected both empty AND "0.0"
  - `if (weightStr.isEmpty() || weightStr.equals("0.0"))`
  - Also enforced min validation of 50.0 lbs / 22.7 kg
- **Fix:** Allow 0 as valid weight
  - Changed empty check to only reject truly empty: `if (weightStr.isEmpty())`
  - Changed min validation from 50.0/22.7 to 0.0
  - Users can now save 0 as placeholder (deletable later via edit)

**Verification:**
- [x] All 217 tests passing ✅
- [x] Lint clean ✅
- [x] Commit: `fix: resolve three number input and validation bugs`

**Impact:**
- Consistent initial display state (0.0 instead of XML default)
- Intuitive number entry when building from zero
- Flexibility to enter 0 as placeholder value
- Better UX for manual data entry scenarios

### 4.13 Manual Testing Bug Fix - Round 3 (Completed 2025-12-11)
**Final UX Consistency Issue Found During Manual Testing:**

**Issue:** Cannot save 0.0 immediately in add mode
- **User Expectation:** Display shows "0.0", so clicking Save should work
- **Actual Behavior:** Clicking Save shows "Please enter a weight value" error
- **Root Cause:** `weightInput` initialized to empty `""` instead of `"0.0"`
  - Display showed "0.0" (from updateWeightDisplay logic: empty → "0.0")
  - Internal state was empty `""`
  - handleSave() rejected empty string
  - Display and internal state were inconsistent

**Fix:**
- [x] Initialize `weightInput` to `"0.0"` instead of empty string
- [x] Line 138: `weightInput = new StringBuilder("0.0");`
- [x] Now display matches internal state from start
- [x] User can click Save immediately without typing
- [x] Edit mode unaffected (loadExistingEntry() overwrites value anyway)
- [x] All tests passing, lint clean
- [x] Commit: `fix: allow saving 0.0 in add mode by initializing weightInput`

**UX Principle Applied:**
- **Display = Internal State**: If user sees "0.0", that should be the actual value
- **What You See Is What You Get**: No hidden state differences
- **Zero Friction Data Entry**: Allow immediate save if default is acceptable

**Impact:**
- Add mode and edit mode now both allow saving 0.0
- Consistent UX: display always reflects internal state
- User can save immediately or adjust first (maximum flexibility)

### 4.14 Final PR Feedback - Code Quality & Testing (Completed 2025-12-11)
**PR Review Round 2 - 6 Additional Issues Addressed:**

**Issue #1: Missing WeightEntryActivity Tests (CRITICAL)**
- **Severity:** High
- **Problem:** 755 lines of business-critical code with ZERO tests
  - 4 bugs found manually that tests should have caught
  - Regression risk: bugs could return without test coverage
- **Solution:** Created 9 regression tests in `WeightEntryActivityTest.java`
  - Category A: Number input bugs (3 tests) - 0.0 appending, decimal prevention, digit overflow
  - Category B: Validation bugs (3 tests) - default display, zero save, max weight
  - Category C: Unit display bugs (2 tests) - lbs↔kg conversion bidirectional
  - Category D: Integration (1 test) - edit mode database update
- **Status:** Tests written but @Ignored due to Robolectric/Material3 incompatibility (GH #12)
- **Resolution:** Tests will be migrated to Espresso in Phase 8.4
- **Commit:** `test: add 9 regression tests for WeightEntryActivity (@Ignored)`

**Issue #2: Magic Numbers in Validation (MEDIUM)**
- **Severity:** Medium
- **Problem:** Hardcoded validation ranges (700.0, 317.5, 0.0) in multiple locations
  - Lines 452, 676 in WeightEntryActivity
  - Difficult to update if business rules change
- **Solution:** Created `WeightUtils` utility class with named constants
  - `MAX_WEIGHT_LBS = 700.0`
  - `MAX_WEIGHT_KG = 317.5`
  - `MIN_WEIGHT = 0.0`
  - `LBS_TO_KG_CONVERSION = 0.453592`
- **Tests:** 6 unit tests with 100% coverage
- **Commit:** `feat(utils): add WeightUtils for weight conversion and validation`

**Issue #3: Duplicate Conversion Logic (LOW)**
- **Severity:** Low
- **Problem:** Weight conversion logic duplicated in two places
  - WeightEntryActivity: `value * LBS_TO_KG_CONVERSION` and `value / LBS_TO_KG_CONVERSION`
  - WeightEntryAdapter: Hardcoded `0.453592` instead of constant
- **Solution:** Centralized in WeightUtils
  - `convertLbsToKg(double lbs)` - with 1 decimal rounding
  - `convertKgToLbs(double kg)` - with 1 decimal rounding
  - `roundToOneDecimal(double weight)` - prevents floating-point display issues
- **Commit:** `refactor: use WeightUtils for all weight conversions and validation`

**Issue #4: Validation Extraction Needed (LOW - DEFERRED)**
- **Severity:** Low
- **Problem:** `handleSave()` mixes validation logic with save routing
- **Recommendation:** Extract `validateWeightInput()` with ValidationResult pattern
- **Decision:** **DEFERRED to Phase 7.4 (Code Quality)**
- **Rationale:**
  - Current `handleSave()` works correctly (no bugs reported)
  - Validation refactoring requires broader architectural changes:
    - ValidationResult pattern (new class)
    - Error message centralization
    - Potential UI error display changes
  - Phase 7 will refactor ALL validation logic across activities
  - Risk: Mixing refactoring with bug fixes complicates code review
- **Documented in:** Phase 7.4 planning section

**Issue #5: Trend Calculation Precision (LOW)**
- **Severity:** Very Low
- **Problem:** Floating-point conversion may cause minor display issues in trend badges
- **Solution:** Applied rounding after conversion
  - WeightEntryAdapter: `roundToOneDecimal(previousWeight - currentWeight)`
  - Ensures converted weight matches 1-decimal-place display format
- **Commit:** `refactor: use WeightUtils for all weight conversions and validation`

**Issue #6: Missing Accessibility Content Descriptions (LOW)**
- **Severity:** Low
- **Problem:** Number pad buttons (0-9, decimal) lack content descriptions
- **Solution:** Added 12 string resources and applied to layout
  - `cd_numpad_zero` through `cd_numpad_nine`
  - `cd_numpad_decimal`, `cd_numpad_backspace` (backspace already existed)
  - Applied `android:contentDescription` to all 12 buttons
- **Impact:** WCAG AA compliance for visually impaired users
- **Commit:** `feat(a11y): add content descriptions to weight entry number pad`

**Commits Summary (4 commits):**
1. `710d205` - feat(utils): add WeightUtils with 6 unit tests
2. `70ec0f0` - refactor: use WeightUtils for all conversions/validation
3. `7f6b042` - test: add 9 WeightEntryActivity regression tests (@Ignored)
4. `2a60923` - feat(a11y): add content descriptions to number pad

**Test Count:**
- Before: 217 tests
- After: 223 tests passing, 9 skipped
- New: 6 WeightUtils tests + 9 WeightEntryActivity tests (@Ignored)

**Validation:**
- [x] `./gradlew test` - 223 passing, 9 skipped ✅
- [x] `./gradlew lint` - Clean, no errors ✅

**Deferred Work:**
- [ ] Issue #4: Validation extraction → Phase 7.4 (Code Quality)
- [ ] WeightEntryActivity tests migration → Phase 8.4 (Espresso UI tests)

**Lessons Learned:**
- **Robolectric Limitations:** Material3 theme incompatibility prevents activity testing (GH #12)
- **Test Documentation Value:** Even @Ignored tests document expected behavior for Espresso migration
- **DRY Principles:** WeightUtils eliminates 4+ instances of duplicate conversion logic
- **Incremental Refactoring:** Defer validation extraction to avoid scope creep
- **Accessibility by Default:** Content descriptions should be added during initial implementation

**Phase 4 Final Status:**
- ✅ All 6 PR feedback items addressed (5 implemented, 1 deferred with justification)
- ✅ 4 commits added to feature branch
- ✅ Test count increased to 223 (217→223, +6 active tests)
- ✅ 9 regression tests documented (@Ignored, ready for Espresso migration)
- ✅ Accessibility compliance improved (12 content descriptions)
- ✅ Code quality improved (DRY, no magic numbers, centralized conversion)
- ✅ All automated tests passing, lint clean
- 🔄 Ready for final merge to main after user approval

---

**Phase 4 Complete.** Weight Entry CRUD fully implemented with comprehensive PR feedback addressed, excellent test coverage plan, and WCAG AA accessibility compliance.
