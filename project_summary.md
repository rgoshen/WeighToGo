# Project Summary - Weigh to Go!

## [2025-11-29] Phase 1: Resource Files - Completed

### Work Completed
- Created `strings.xml` with all UI strings for login, dashboard, SMS notifications, dialogs, error/success messages, bottom navigation, and accessibility content descriptions
- Created `dimens.xml` with 8dp grid spacing system, corner radii, button heights, icon sizes, text sizes, elevations, and component-specific dimensions
- Updated `themes.xml` with Material Design 3 theme including:
  - Primary/secondary color configuration
  - Surface and background colors
  - Status bar styling
  - Custom text appearance styles (Display, Headline, Title, Body, Label)
  - Custom button styles (Primary, Secondary)
  - Custom card and input field styles
  - Login-specific theme with transparent status bar

### Issues Encountered
None

### Corrections Made
None

### Lessons Learned
- Icons and colors were already prepared by the user before implementation started, which streamlined the resource file creation

### Technical Debt
None identified

---

## [2025-11-29] Phase 2: Login Screen - Completed

### Work Completed
- Created `activity_login.xml` with complete login/registration UI including:
  - Gradient header with app logo, name ("Weigh to Go!"), and tagline
  - Tab toggle for switching between Sign In and Create Account modes
  - Username input field with ic_profile icon
  - Password input field with ic_lock icon and password visibility toggle
  - Password field uses `inputType="textPassword"` to display dots (rubric requirement)
  - Forgot Password link with proper touch target (48dp)
  - Sign In button (primary, filled style)
  - "or continue with" divider
  - Create Account button (secondary, outlined style)
  - Terms and Privacy footer text

### Issues Encountered
1. **Icon overlap with hint text** - Initially used `app:startIconDrawable` with floating hints, causing icons to overlap with hint text inside the input fields

### Corrections Made
1. Added `app:expandedHintEnabled="false"` to TextInputLayout to keep hints as labels above the input box rather than floating inside, preventing overlap with start icons
2. Changed username icon from `ic_person` to `ic_profile` for better visual consistency with the app's icon set

### Lessons Learned
- When using `startIconDrawable` in Material TextInputLayout, use `app:expandedHintEnabled="false"` to prevent hint text from overlapping with the icon
- Existing PNG icons across density folders work well - no need to create new vector drawables

### Technical Debt
None identified

---

## [2025-11-29] Phase 3: Database Grid Screen - Completed

### Work Completed
- Created `bottom_nav_menu.xml` with four navigation items (Home, Trends, Goals, Profile)
- Created `activity_main.xml` dashboard layout matching the preview design:
  - Gradient header with greeting text ("Good morning,") and user name
  - Notification and settings icon buttons with semi-transparent backgrounds
  - Progress card with "Your Progress" title, trend badge, and motivational subtitle
  - Current/Start/Goal weight display with progress bar
  - Quick stats row (Total Lost, lbs to Goal, Day Streak cards)
  - "Recent Entries" section header with "View All" link
  - RecyclerView for weight history items
  - Empty state container with icon and messages
  - FloatingActionButton with rounded square shape (20dp corner radius)
  - BottomNavigationView with color selector for states
- Created `item_weight_entry.xml` RecyclerView item layout:
  - MaterialCardView with elevation and rounded corners
  - Date badge with day number and month abbreviation
  - Weight value with unit and time text
  - Trend indicator badge (up/down/same)
  - Edit and Delete ImageButtons (48dp touch targets, delete has red tint)
  - **Delete button per row - CRITICAL RUBRIC REQUIREMENT**
- Created supporting resources:
  - `bg_header_button.xml` - Semi-transparent (#33FFFFFF) rounded rectangle
  - `bg_date_badge.xml` - Surface variant colored rounded background
  - `bottom_nav_color.xml` - Color selector for checked/unchecked states
  - Added FAB shape style to themes.xml
  - Added new strings: progress_to_goal, total_lost, lbs_to_goal, day_streak

### Issues Encountered
1. **Initial layout mismatch with design previews** - First implementation used a toolbar-based design with grid headers (DATE | WEIGHT | TREND | ACTIONS) instead of the card-based design shown in the HTML/PNG preview files
2. **Build errors from previous phase** - Style name typo (`TextAppearance.WeighToGo.Headline` instead of `TextAppearance.WeightToGo.Headline`) and invalid `minHeight="match_parent"` in activity_login.xml
3. **Creating unnecessary XML drawables** - Created XML drawable resources when PNG files already existed in the project

### Corrections Made
1. Completely revised `activity_main.xml` and `item_weight_entry.xml` to match the preview designs in `/previews/weight_tracker_dashboard.html` and `/previews/weight_tracker_entry.html`
2. Fixed the style name typo (added missing 't' in WeightToGo)
3. Fixed activity_login.xml by changing `minHeight="match_parent"` to `layout_height="match_parent"`
4. Only created essential XML drawables (bg_header_button, bg_date_badge, bottom_nav_color) that don't have existing PNG equivalents

### Lessons Learned
- **ALWAYS check preview/mockup files before implementing** - The HTML and PNG preview files in `/previews/` folder show the actual intended design and should be the primary reference
- Design specifications documents describe concepts, but preview files show exact implementation
- Avoid creating XML drawables when PNG resources already exist in the project
- The design uses a modern card-based approach rather than a traditional grid/table layout

### Technical Debt
None identified

---

## [2025-11-29] Phase 4: SMS Notifications Screen - Completed

### Work Completed
- Updated `AndroidManifest.xml` with required SMS permissions:
  - `<uses-permission android:name="android.permission.SEND_SMS" />`
  - `<uses-feature android:name="android.hardware.telephony" android:required="false" />`
  - The `required="false"` ensures app works on devices without SMS capability (CRITICAL RUBRIC REQUIREMENT)
- Created `activity_sms_settings.xml` layout matching the preview design:
  - Gradient header with back button, "SMS Notifications" title, and subtitle
  - Permission Card with:
    - 48dp permission icon with gradient background
    - "SMS Permission" title with status badge (Required/Granted/Denied states)
    - Description text explaining why permission is needed
    - "Grant SMS Permission" primary button
  - Phone Number Card with:
    - Title and description
    - Country code input (+1, disabled/fixed)
    - Phone number input field with inputType="phone"
  - Notification Preferences Card with MaterialSwitch toggles:
    - Master toggle: "Enable SMS Notifications"
    - Goal Reached Alerts toggle
    - Milestone Alerts toggle
    - Daily Reminders toggle
    - "Send Test Message" outlined button
  - Info Banner with messaging rates disclaimer
- Added new color resources for permission status badges:
  - Pending: #FFF3E0 bg, #FF9800 text
  - Granted: #E8F5E9 bg, #4CAF50 text
  - Denied: #FFEBEE bg, #F44336 text
  - Info banner: #E0F2F1 bg, #00695C text
- Created supporting drawable resources:
  - `bg_permission_icon.xml` - Gradient rounded square (12dp corners)
  - `bg_info_banner.xml` - Light teal rounded background
  - `bg_status_pending.xml`, `bg_status_granted.xml`, `bg_status_denied.xml`
- Updated `strings.xml` with all SMS screen text:
  - Permission card strings
  - Phone number card strings
  - Notification preferences strings
  - Info banner text
  - Content descriptions for accessibility

### Issues Encountered
None - referenced the preview file (`weight_tracker_sms_notifications.html`) before implementation

### Corrections Made
1. Changed info banner icon from `ic_info` (which doesn't exist) to `ic_notification` (which exists in the project)

### Lessons Learned
- Checking available icons before referencing them in layouts prevents build errors
- MaterialSwitch is the modern replacement for SwitchCompat in Material Design 3
- Using `android:required="false"` on `<uses-feature>` is essential for app store compatibility on devices without telephony

### Technical Debt
None identified

---

## [2025-11-29] Phase 5: Weight Entry Screen - Completed

### Work Completed
- Created `activity_weight_entry.xml` - the screen opened when user taps FAB on dashboard:
  - Navigation header with back button and centered "Log Weight" title
  - Date Selector Card with:
    - "Entry Date" label (uppercase, letter-spaced)
    - Previous/Next date navigation buttons (44dp, surface_variant bg)
    - Large day number (32sp, primary_teal)
    - Full date text (e.g., "Tuesday, November 26, 2025")
    - "Today" badge with success green styling
  - Weight Input Card with:
    - Title "Enter Your Weight" and subtitle
    - Large weight display (64sp, primary_teal, surface_variant background)
    - Quick adjust buttons (-1, -0.5, +0.5, +1) with outlined teal styling
    - Unit toggle (lbs/kg) with active/inactive states
    - Custom number pad (3x4 GridLayout, 56dp buttons, 16dp corners)
    - Save button (60dp height, MaterialButton with check icon)
    - Previous entry hint showing last recorded weight
- Created supporting drawable resources:
  - `bg_weight_display.xml` - 20dp rounded surface_variant background
  - `bg_numpad_button.xml` - Bordered button with pressed state
  - `bg_unit_toggle_active.xml` - Solid primary_teal background
  - `bg_unit_toggle_inactive.xml` - Bordered inactive state
  - `bg_adjust_button.xml` - Outlined teal with pressed state
  - `bg_date_nav_button.xml` - Surface_variant with pressed state
  - `bg_today_badge.xml` - Success green badge background
- Added new strings for weight entry screen:
  - Entry date, weight entry title/subtitle
  - Quick adjust button labels
  - Number pad characters
  - Content descriptions for accessibility

### Issues Encountered
1. **Missing ic_next icon** - Preview design showed a right arrow for next date navigation
2. **Material Button overrides custom backgrounds** - All buttons (quick adjust, unit toggle, number pad) rendered as solid teal instead of white with borders because Material Design 3 `Button` widget ignores `android:background` and applies its own theme styling

### Corrections Made
1. Used `android:rotation="180"` on ic_back to create a right-pointing arrow for the next date button, avoiding the need to create a new icon
2. Changed all `Button` elements to `TextView` for custom-styled buttons:
   - Quick adjust buttons (-1, -0.5, +0.5, +1)
   - Unit toggle buttons (lbs, kg)
   - Number pad buttons (0-9, decimal, backspace)
   - Added `android:clickable="true"` and `android:focusable="true"` to maintain touch handling
   - Added `android:gravity="center"` for proper text alignment

### Lessons Learned
- `android:rotation` can be used to flip/rotate icons, avoiding the need for duplicate assets
- GridLayout with `layout_columnWeight` distributes button widths evenly across the number pad
- 60dp save button exceeds Android's 48dp minimum touch target requirement
- **Material Design 3 `Button` widget overrides `android:background`** - Use `TextView` with `clickable="true"` and `focusable="true"` when you need custom drawable backgrounds that the theme should not override
- `TextView` is a valid alternative to `Button` for clickable elements when custom styling is required

### Technical Debt
None identified

---

## [2025-11-29] Phase 6: Final Validation & Login Screen Updates - Completed

### Work Completed
- Replaced "Create Account" button with social login buttons (Google, Facebook, Apple) to match HTML preview design
- Removed redundant "or continue with → Create Account" flow (tabs already handle Sign In / Create Account)
- Added content description strings for social login buttons
- Verified WCAG 2.1 AA compliance across all screens:
  - All interactive elements meet 48dp minimum touch target
  - All ImageButtons and ImageViews have contentDescription (or @null for decorative)
  - High contrast text throughout (dark on light, white on dark)
  - Logical focus order for keyboard/accessibility navigation
  - Text sizes use sp units for user scaling

### Issues Encountered
1. **Redundant Create Account button** - Original layout had "or continue with" leading to a Create Account button, but the tab toggle already had Create Account option
2. **Accessibility FAB considered but removed** - No accessibility icon (♿) available, and the FAB would require Java code for functionality

### Corrections Made
1. Replaced Create Account button with 3 social login buttons (G, f, 🍎) matching the HTML preview
2. Decided against accessibility FAB - ensured WCAG compliance through proper implementation instead
3. Changed tab labels from "Sign In / Create Account" to "Sign In / Register" to match HTML preview
4. Added rounded corners to tab toggle container and active tab state

### Lessons Learned
- Social login buttons can use simple text labels (G, f) as placeholders until proper brand icons are integrated
- WCAG compliance is better achieved through proper implementation (touch targets, content descriptions, contrast) rather than a dedicated accessibility settings button
- Decorative images should use `contentDescription="@null"` to be ignored by screen readers
- Tab toggle styling requires separate drawables for container (12dp corners) and active state (10dp corners) to achieve nested rounded appearance

### Technical Debt
- Social login buttons use text placeholders - should be replaced with proper brand icons/SDKs when implementing OAuth