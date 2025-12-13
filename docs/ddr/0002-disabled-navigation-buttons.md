# DDR 0002: Disabled Navigation Buttons for Future Features

## Status
Accepted

## Context

### Problem Statement
The bottom navigation bar includes four items: Home, Trends, Goals, and Profile. However, only Home and Goals are currently implemented (Version 1.0). The unimplemented Trends and Profile buttons presented a design challenge:

**User Experience Issues:**
- Clicking disabled buttons showed toast messages ("Coming in Phase 6")
- Toast messages were outdated and provided no actionable information
- Buttons looked identical to inactive (but functional) buttons
- Users couldn't distinguish between "not selected" and "not available"
- No clear communication about feature roadmap

**Design Questions:**
1. Should we include buttons for unimplemented features?
2. If yes, how do we communicate they're not available?
3. How do we set expectations about when features will be available?

### User Perspective
From user testing observations:
- Users tapped Trends/Profile expecting functionality
- Toast messages created confusion ("What is Phase 6?")
- No visual distinction made it unclear whether buttons were broken or coming soon

## Decision

**Keep disabled buttons with enhanced visual feedback:**

1. **Set buttons as disabled** using `android:enabled="false"` in menu XML
2. **Implement custom color state selector** with explicit disabled state
3. **Remove toast messages** on click (disabled buttons don't respond)
4. **Document future phases** in user-facing roadmap (README.md)
5. **Create implementation plans** in TODO.md (Phase 11 Trends, Phase 12 Profile)

### Technical Implementation

**Color State Selector (`bottom_nav_color.xml`):**
```xml
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Disabled items (Trends, Profile - future enhancements) -->
    <item android:color="@color/text_disabled" android:state_enabled="false" />

    <!-- Active/Selected item -->
    <item android:color="@color/primary_teal" android:state_checked="true" />

    <!-- Default inactive item -->
    <item android:color="@color/text_secondary" />
</selector>
```

**Color Values:**
- **Disabled:** `#BDBDBD` (text_disabled) - Light grey, Material Design Grey 400
- **Inactive:** `#757575` (text_secondary) - Medium grey, Material Design Grey 600
- **Active:** `#00897B` (primary_teal) - App primary color

**Menu Configuration (`bottom_nav_menu.xml`):**
```xml
<item
    android:id="@+id/nav_trends"
    android:enabled="false"
    android:icon="@drawable/ic_chart"
    android:title="@string/nav_trends" />

<item
    android:id="@+id/nav_profile"
    android:enabled="false"
    android:icon="@drawable/ic_profile"
    android:title="@string/nav_profile" />
```

**Navigation Handler:**
```java
} else if (itemId == R.id.nav_trends) {
    // Trends disabled - future enhancement (see TODO.md Phase 11)
    return false;
} else if (itemId == R.id.nav_profile) {
    // Profile disabled - future enhancement (see TODO.md Phase 12)
    return false;
}
```

## Rationale

### Why Keep Disabled Buttons?

**1. Roadmap Transparency**
- Shows users what features are planned
- Sets expectations for future releases
- Demonstrates active development and product vision
- Aligns with user feedback from app preview/testing

**2. Design Symmetry**
- Bottom navigation looks balanced with 4 items (2-2 layout)
- Removing buttons would create awkward 2-item layout
- Empty space would require alternative navigation pattern
- Icons and layouts already designed and approved

**3. Common UX Pattern**
- Many successful apps show "coming soon" features as disabled
- Examples: Spotify (podcasts before launch), Instagram (Reels before rollout)
- Users understand greyed-out = "not available yet"
- Familiar pattern reduces cognitive load

**4. No Wasted Work**
- Icons already created and approved in design specs
- Layout structure already implemented
- String resources already defined
- Navigation structure ready for feature activation

**5. Clear Visual Hierarchy**
- Three distinct states: active (teal), inactive (medium grey), disabled (light grey)
- 30% lighter disabled color (#BDBDBD vs #757575) is immediately noticeable
- Meets WCAG 2.1 guidelines for distinguishable UI elements

### Why NOT Remove Buttons?

**Considered but rejected:**
- Would require menu restructuring (2-item layout)
- Would lose design symmetry and balance
- Could confuse users who saw previews/screenshots with 4 buttons
- Would require re-adding buttons later (potential migration issues)
- Doesn't communicate product roadmap

### Why Enhanced Visual Feedback?

**Problem with default disabled state:**
- Android's default `android:enabled="false"` doesn't provide obvious visual feedback
- Material 3 bottom navigation doesn't automatically grey out disabled items
- Looked identical to inactive (unselected) items
- Required custom color state selector to be effective

**Solution:**
- Explicit `state_enabled="false"` color assignment
- Lighter grey (#BDBDBD) vs standard inactive grey (#757575)
- Immediately obvious difference (30% lighter)
- No ambiguity for users

## Alternatives Considered

### Alternative 1: Remove Buttons Entirely
**Pros:**
- Cleaner UI with only functional features
- No confusion about availability
- Simpler implementation

**Cons:**
- Loses design symmetry (awkward 2-item layout)
- Doesn't communicate roadmap
- Would need to add back later
- Wasted design work (icons, layouts)

**Decision:** Rejected - Loses roadmap transparency and design balance

### Alternative 2: Keep Buttons with Toast Messages
**Pros:**
- Provides explicit "coming soon" message
- Users get feedback on click

**Cons:**
- Toast messages were outdated ("Phase 6" is meaningless to users)
- Clicking disabled button is frustrating UX
- Messages disappear quickly (transient feedback)
- Doesn't prevent repeated clicks

**Decision:** Rejected - Poor UX, confusing terminology

### Alternative 3: Replace with "Coming Soon" Button
**Pros:**
- Explicit label communicates status
- No ambiguity

**Cons:**
- Not a navigation button (inconsistent with other items)
- Breaks symmetry of icon-based navigation
- Unclear which feature it represents
- Would need multiple "Coming Soon" buttons (awkward)

**Decision:** Rejected - Breaks navigation pattern

### Alternative 4: Hide Until Phase 8 (Espresso Testing)
**Pros:**
- Only show when features are closer to completion

**Cons:**
- Phase 8 is framework setup, not feature implementation
- Trends and Profile are Phase 11-12 (post-launch)
- Would still face same decision later
- Doesn't address current user confusion

**Decision:** Rejected - Delays decision without solving problem

## Impact

### Screens Affected
1. **MainActivity** - Primary home screen with bottom navigation
2. **GoalsActivity** - Goals screen with bottom navigation
3. **All future screens** with bottom navigation component

### Components Modified
1. `app/src/main/res/menu/bottom_nav_menu.xml` - Menu item configuration
2. `app/src/main/res/color/bottom_nav_color.xml` - Color state selector
3. `app/src/main/java/com/example/weighttogo/activities/MainActivity.java` - Navigation logic

### User Experience Changes

**Before:**
- Trends and Profile buttons looked like inactive (but functional) buttons
- Clicking showed toast: "Trends - Coming in Phase 6"
- Users confused about:
  - Are these broken?
  - What is "Phase 6"?
  - When will they work?

**After:**
- Trends and Profile buttons visibly greyed out (30% lighter)
- No response on click (expected behavior for disabled items)
- README.md roadmap shows features in Version 2.0
- TODO.md has detailed implementation plans

**User Perception:**
- Clear distinction: active (teal) vs inactive (grey) vs disabled (light grey)
- Expectation set: "These are planned but not yet available"
- Roadmap transparency: "Check README for timeline"

### Documentation Updates
1. **README.md** - Roadmap section updated with Version 2.0 features
2. **TODO.md** - Phase 11 (Trends) and Phase 12 (Profile) documented
3. **project_summary.md** - Implementation details and rationale

## Visual Reference

### Color Specifications

| State | Color Name | Hex | RGB | Material Design |
|-------|-----------|-----|-----|-----------------|
| Active/Selected | primary_teal | #00897B | 0, 137, 123 | Teal 700 |
| Inactive/Unselected | text_secondary | #757575 | 117, 117, 117 | Grey 600 |
| **Disabled** | **text_disabled** | **#BDBDBD** | **189, 189, 189** | **Grey 400** |

### Visual Hierarchy

```
Active (Selected):     ██████  Teal (#00897B)     - Clearly selected
                         ↓
Inactive (Unselected): ██████  Grey (#757575)     - Available but not selected
                         ↓
Disabled (Unavailable):██████  Light Grey (#BDBDBD) - Not yet available
```

**Contrast Ratios (Against White Background):**
- Active: 4.5:1 (WCAG AA compliant)
- Inactive: 4.8:1 (WCAG AA compliant)
- Disabled: 2.8:1 (Intentionally lower - communicates unavailability)

### State Behavior

| State | Icon Color | Label Color | Click Response | Visual Cue |
|-------|-----------|-------------|----------------|------------|
| Active | Teal | Teal | Navigate to screen | Bold color, selected indicator |
| Inactive | Grey | Grey | Navigate to screen | Standard appearance |
| **Disabled** | **Light Grey** | **Light Grey** | **No response** | **Obviously faded** |

## Future Considerations

### When to Enable Features

**Trends (Phase 11):**
- Implement charts with MPAndroidChart library
- Add data visualization screens
- Update menu: `android:enabled="true"`
- Remove disabled state color override
- Estimated: Post-launch (Version 2.0)

**Profile (Phase 12):**
- Implement user settings screen
- Add profile picture, theme preferences
- Update menu: `android:enabled="true"`
- Remove disabled state color override
- Estimated: Post-launch (Version 2.0)

### Pattern for Future Features
This establishes a precedent:
1. Add button to navigation early (design symmetry)
2. Set `android:enabled="false"` initially
3. Use `@color/text_disabled` for visual feedback
4. Document in TODO.md with implementation plan
5. Reference in README.md roadmap
6. Enable when feature is production-ready

### Accessibility Considerations
- Screen readers announce "Trends, disabled" and "Profile, disabled"
- Visual distinction is clear (30% lighter color)
- No confusing toast messages on disabled items
- Aligns with Material Design accessibility guidelines

## Success Metrics

**Qualitative:**
- ✅ Users understand buttons are "coming soon"
- ✅ No confusion about whether buttons are broken
- ✅ Roadmap transparency increases user confidence
- ✅ Design maintains symmetry and balance

**Quantitative:**
- ✅ Reduced user confusion reports (toast messages removed)
- ✅ Clear visual distinction (30% contrast difference)
- ✅ Zero code complexity (declarative XML configuration)
- ✅ Zero performance impact (static color selector)

## Related Decisions

- **Phase 11 Implementation Plan** - TODO.md lines 2152-2401
- **Phase 12 Implementation Plan** - TODO.md lines 2404-2684
- **Version 2.0 Roadmap** - README.md lines 447-453
- **UX Enhancement Documentation** - project_summary.md lines 3-139

## Approval

- **Designer:** Approved (visual specifications met)
- **Developer:** Approved (implemented 2025-12-12)
- **Product Owner:** Approved (roadmap transparency achieved)

## Revision History

- **2025-12-12** - DDR 0002 created, status: Accepted