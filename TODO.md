# TODO.md - Weigh to Go! Project Two

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