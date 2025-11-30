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
- [ ] None

### Phase 1: Resource Files
- [x] Create colors.xml palette (2025-11-29)
- [x] Add drawable icons across all densities (2025-11-29)
- [x] Create strings.xml with all UI strings (2025-11-29)
- [x] Create dimens.xml with spacing and sizing values (2025-11-29)
- [x] Update themes.xml for Material Design 3 (2025-11-29)

### Phase 2: Login Screen (25%)
- [ ] Create activity_login.xml layout

### Phase 3: Database Grid Screen (25%)
- [ ] Create activity_main.xml layout with grid
- [ ] Create item_weight_entry.xml with delete button

### Phase 4: SMS Notifications Screen (30%)
- [ ] Update AndroidManifest.xml with SMS permissions and telephony feature
- [ ] Create activity_settings.xml for SMS permissions UI

### Phase 5: Validation
- [ ] Build and validate project

### Completed
- [x] Create feature branch (2025-11-29)

---

## Blockers
None