# FIGMA QUICK START GUIDE
## Weigh to Go! Mobile App - Complete UI Design (4 Screens)

**App Name:** Weigh to Go!  
**Tagline:** "You've got this—pound for pound."  
**Time Required:** ~2.5-3 hours (all four screens)  
**Skill Level:** Beginner-friendly with detailed instructions  
**Developer:** Rick Goshen  
**Course:** CS 360 - Mobile Architecture & Programming

---

## 🎯 OVERVIEW

This guide walks you through creating four complete screens for the **Weigh to Go!** app:
1. **Login/Registration Screen** (~40 min)
2. **Main Dashboard Screen** (~50 min)
3. **Weight Entry Screen** (~40 min)
4. **SMS Notifications Screen** (~40 min)

Each screen follows Material Design 3 principles and Android Design Guidelines.

---

## 🎨 INITIAL SETUP (10 minutes)

### Step 1: Create New Figma File
1. Open Figma (figma.com or desktop app)
2. Click "New Design File"
3. Name it: `CS360_WeightToGo_UI_RickGoshen`

### Step 2: Create Four Frames
1. Press `F` (frame tool) or click frame icon
2. Select "iPhone 13 / 13 Pro" (375×812) from right sidebar
3. Create 4 frames side by side
4. Rename frames:
   - Frame 1: `01_Login_Registration`
   - Frame 2: `02_Main_Dashboard`
   - Frame 3: `03_Weight_Entry`
   - Frame 4: `04_SMS_Notifications`

### Step 3: Setup Grid System
1. Select each frame
2. Right panel → Layout Grid section
3. Click `+` to add grid
4. Change to "Rows" with 8px spacing
5. This ensures consistent 8px spacing system

---

## 🎨 COLOR STYLES - Health/Wellness Palette (5 minutes)

### Create Color Styles (Critical Step!)
1. Click "Local styles" icon (4 dots) in right panel
2. Click `+` next to "Fill"
3. Create these styles:

```
Primary/Teal            #00897B
Primary/Dark            #00695C
Primary/Light           #4DB6AC
Accent/Green            #4CAF50
Success/Light           #E8F5E9
Warning/Orange          #FF9800
Error/Red               #F44336
Surface/White           #FFFFFF
Surface/Variant         #E0F2F1
Background              #F5F5F5
Text/Primary            #212121
Text/Secondary          #757575
Divider                 #E0E0E0
```

**Pro tip:** After creating, you can reuse by clicking the color style icon!

---

## 📝 TEXT STYLES (5 minutes)

### Create Text Styles
1. Press `T` for text tool
2. Create text, then: Right panel → Text section → Style dropdown → Create style
3. Create these styles:

```
App Name (Branding):
  Font: Poppins Bold
  Size: 32px / Line height: 38px
  Color: Surface/White

Screen Title:
  Font: Poppins SemiBold
  Size: 18px / Line height: 24px
  Color: Text/Primary

Section Title:
  Font: Poppins SemiBold
  Size: 18px / Line height: 22px
  Color: Text/Primary

Card Title:
  Font: Poppins SemiBold
  Size: 16px / Line height: 20px
  Color: Text/Primary

Body Text:
  Font: Source Sans Pro Regular
  Size: 14px / Line height: 20px
  Color: Text/Secondary

Button Text:
  Font: Poppins SemiBold
  Size: 16px / Line height: 20px
  Color: Surface/White

Small Text:
  Font: Source Sans Pro Regular
  Size: 12-13px / Line height: 18px
  Color: Text/Secondary

Large Number:
  Font: Poppins Bold
  Size: 64px / Line height: 1
  Color: Primary/Teal

Stat Value:
  Font: Poppins Bold
  Size: 28px / Line height: 32px
  Color: Text/Primary
```

---

# 📱 SCREEN 1: LOGIN/REGISTRATION (~40 min)

## PART 1.1: Status Bar (2 min)

1. **Rectangle tool (R)** → Draw 375×44 at top
2. Fill: Transparent (we'll use gradient behind)
3. **Text (T)** → "9:41" left side, Poppins SemiBold, 14px, White
4. **Text (T)** → Add signal icons on right (use emojis: 📶 📡 🔋)
5. **Group all** (Cmd/Ctrl + G) → Rename: `Status Bar`

---

## PART 1.2: Gradient Header Background (3 min)

1. **Rectangle (R)** → Draw 375×294 (status bar + branding area)
2. Position: Top of frame (Y=0)
3. Fill: Linear gradient
   - Click "Solid" → change to "Linear"
   - First stop: #00897B (Primary/Teal)
   - Second stop: #00695C (Primary/Dark)
   - Angle: 180° (top to bottom)
4. Rename: `Header Gradient`

---

## PART 1.3: App Branding Section (5 min)

**App Icon:**
1. **Rectangle (R)** → 80×80
2. Position: Center horizontally, Y=84
3. Fill: rgba(255,255,255,0.2)
4. Corner radius: 24px
5. Effect → Drop Shadow: 0, 0, 0, 0 (none needed)
6. Stroke: 2px rgba(255,255,255,0.3)
7. **Text (T)** → "🎉" (40px, centered in rectangle)

**App Name:**
1. **Text (T)** → "Weigh to Go!"
2. Font: Poppins Bold, 32px, White
3. Center horizontally, Y=180

**Tagline:**
1. **Text (T)** → "You've got this—pound for pound."
2. Font: Source Sans Pro, 16px, White (opacity 90%)
3. Center horizontally, Y=218

**Group all branding** → Rename: `Branding Section`

---

## PART 1.4: Auth Card Container (3 min)

1. **Rectangle (R)** → 375×518 (remaining height)
2. Position: Y=294 (overlapping slightly with header)
3. Fill: #FFFFFF
4. Corner radius: 32px 32px 0 0 (top corners only)
5. Effect → Drop Shadow: 0px, -10px, 40px, rgba(0,0,0,0.1)
6. Rename: `Auth Card Container`

---

## PART 1.5: Auth Tab Toggle (5 min)

1. **Rectangle (R)** → 327×52 (full width minus padding)
2. Position: X=24, Y=326
3. Fill: #E0F2F1 (Surface/Variant)
4. Corner radius: 12px

**Sign In Tab (Active):**
1. **Rectangle (R)** → 159.5×44
2. Position: X=28, Y=330
3. Fill: #FFFFFF
4. Corner radius: 10px
5. Effect → Drop Shadow: 0, 2px, 8px, rgba(0,137,123,0.15)
6. **Text (T)** → "Sign In"
   - Poppins SemiBold, 14px, #00897B (Primary/Teal)
   - Center in tab rectangle

**Register Tab (Inactive):**
1. **Rectangle (R)** → 159.5×44 (duplicate from above)
2. Position: X=187.5, Y=330
3. Fill: Transparent
4. **Text (T)** → "Register"
   - Poppins SemiBold, 14px, #757575 (Text/Secondary)
   - Center in tab rectangle

**Group all tabs** → Rename: `Auth Toggle`

---

## PART 1.6: Form Fields (10 min)

**Email/Username Field:**

1. Create label:
   - **Text (T)** → "Email or Username"
   - Font: Poppins Medium, 13px, #757575
   - Position: X=24, Y=394

2. Create input container:
   - **Rectangle (R)** → 327×52
   - Position: X=24, Y=414
   - Fill: #FFFFFF
   - Stroke: 2px #E0E0E0 (Divider)
   - Corner radius: 14px

3. Create icon:
   - **Text (T)** → "👤" (20px)
   - Position: X=40, Y=428 (left side of input)

4. Create placeholder:
   - **Text (T)** → "Enter your email"
   - Font: Source Sans Pro, 16px, #BDBDBD
   - Position: X=72, Y=428

**Group all** → Rename: `Email Input`

**Password Field (similar structure):**

1. Label: "Password" at Y=478
2. Input container at Y=498
3. Lock icon: "🔒" left side
4. Placeholder: "Enter your password"
5. Eye icon: "👁️" right side (for toggle)

**Group all** → Rename: `Password Input`

---

## PART 1.7: Forgot Password Link (2 min)

1. **Text (T)** → "Forgot Password?"
2. Font: Source Sans Pro SemiBold, 13px, #00897B
3. Position: Right-aligned at X=256, Y=566

---

## PART 1.8: Primary Sign In Button (4 min)

1. **Rectangle (R)** → 327×56
2. Position: X=24, Y=590
3. Fill: Linear gradient (135°)
   - First stop: #00897B
   - Second stop: #00695C
4. Corner radius: 16px
5. Effect → Drop Shadow: 0, 4px, 16px, rgba(0,137,123,0.35)

6. **Text (T)** → "Sign In  →"
   - Poppins SemiBold, 16px, White
   - Center in button

**Make this a Component:**
- Select button group
- Right-click → "Create Component"
- Name: `Primary Button`

---

## PART 1.9: Social Login Section (5 min)

**Divider:**
1. **Line (L)** → 140px wide, 1px, #E0E0E0, Y=666
2. **Text (T)** → "or continue with" (13px, #757575)
3. **Line (L)** → 140px wide on right side

**Social Buttons:**
1. Three 100×48 rectangles with:
   - Fill: #FFFFFF
   - Stroke: 2px #E0E0E0
   - Corner radius: 12px
2. Center letters: "G", "f", "🍎"
3. Space 12px apart horizontally

**Group all** → Rename: `Social Login`

---

## PART 1.10: Terms Text & Accessibility FAB (3 min)

**Terms:**
1. **Text (T)** → Multi-line:
   ```
   By continuing, you agree to our
   Terms of Service and Privacy Policy
   ```
2. Font: 12px, #757575, center-aligned
3. Make links (Terms, Privacy) bold with color #00897B

**Accessibility FAB:**
1. **Ellipse (O)** → 48×48
2. Position: Bottom-right (X=303, Y=740)
3. Fill: #212121
4. **Text (T)** → "♿" (20px, white, centered)
5. Effect → Drop Shadow: 0, 4px, 12px, rgba(0,0,0,0.25)

**Make FAB a Component** → Name: `Accessibility FAB`

---

# 📊 SCREEN 2: MAIN DASHBOARD (~50 min)

## PART 2.1: Status Bar & Header (5 min)

**Status Bar:**
1. Copy from Login screen or recreate
2. Background: Transparent (over teal header)

**Header Background:**
1. **Rectangle (R)** → 375×160
2. Fill: Linear gradient (180°) #00897B → #00695C
3. Position: Top (Y=0)

**Greeting Section:**
1. **Text (T)** → "Good morning," (14px, white, opacity 90%)
2. Position: X=24, Y=60
3. **Text (T)** → "Rick 👋" (22px, Poppins Bold, white)
4. Position: X=24, Y=78

**Header Action Buttons:**
1. Two 40×40 rounded rectangles
2. Fill: rgba(255,255,255,0.2)
3. Corner radius: 12px
4. Icons: "🔔" and "⚙️" centered
5. Position: Right side, 12px apart

**Group all** → Rename: `Dashboard Header`

---

## PART 2.2: Progress Card ⭐ KEY FEATURE (15 min)

**Card Container:**
1. **Rectangle (R)** → 335×200
2. Position: X=20, Y=120 (overlapping header by 40px)
3. Fill: #FFFFFF
4. Corner radius: 24px
5. Effect → Drop Shadow: 0, 8px, 32px, rgba(0,105,92,0.15)

**Progress Header Row:**
1. **Text (T)** → "Progress to Goal" (Poppins SemiBold, 16px, #212121)
2. **Text (T)** → "You're doing great! Keep it up!" (13px, #757575)
3. Position: Left side with 24px padding

**Trend Badge:**
1. **Rectangle (R)** → Auto width × 28
2. Fill: #E8F5E9 (Success/Light)
3. Corner radius: 20px
4. **Text (T)** → "↓ 2.5 lbs" (13px, Poppins SemiBold, #4CAF50)
5. Position: Top-right of card

**Stats Row (Start | Current | Goal):**

Create 3 stat items with dividers:

Stat 1 (Start):
1. **Text (T)** → "185" (Poppins Bold, 28px, #212121)
2. **Text (T)** → "START" (12px, #757575, uppercase)
3. Center in first third

Divider:
1. **Rectangle (R)** → 1×40, #E0E0E0

Stat 2 (Current):
1. **Text (T)** → "172" (Poppins Bold, 28px, #00897B)
2. **Text (T)** → "CURRENT" (12px, #757575, uppercase)
3. Center in middle third

Stat 3 (Goal):
1. **Text (T)** → "165" (Poppins Bold, 28px, #212121)
2. **Text (T)** → "GOAL" (12px, #757575, uppercase)
3. Center in last third

**Progress Bar:**
1. **Rectangle (R)** → 287×12 (background)
2. Fill: #E0F2F1
3. Corner radius: 12px
4. Position: X=44, Y=282

5. **Rectangle (R)** → 195×12 (fill - 68% of 287)
6. Fill: Linear gradient (90°) #00897B → #4CAF50
7. Corner radius: 12px

8. **Ellipse (O)** → 20×20 (handle)
9. Fill: #FFFFFF
10. Stroke: 3px #4CAF50
11. Position: Right end of fill bar

**Progress Labels:**
1. **Text (T)** → "Started Oct 1" (12px, #757575) - left
2. **Text (T)** → "68% Complete" (12px, #757575) - right

**Group all card elements** → Rename: `Progress Card`

---

## PART 2.3: Quick Stats Row (8 min)

Create 3 stat cards side by side:

**Each Card:**
1. **Rectangle (R)** → 105×80
2. Fill: #FFFFFF
3. Corner radius: 16px
4. Effect → Drop Shadow: 0, 2px, 12px, rgba(0,0,0,0.06)

**Card 1 (Total Lost):**
- Icon: "📉" (24px)
- Value: "-13" (Poppins Bold, 18px)
- Label: "Total Lost" (11px, #757575)

**Card 2 (To Goal):**
- Icon: "🎯" (24px)
- Value: "7" (Poppins Bold, 18px)
- Label: "lbs to Goal" (11px, #757575)

**Card 3 (Streak):**
- Icon: "🔥" (24px)
- Value: "14" (Poppins Bold, 18px)
- Label: "Day Streak" (11px, #757575)

**Position cards with 12px gaps**
**Group all** → Rename: `Quick Stats`

---

## PART 2.4: History Section (12 min)

**Section Header:**
1. **Text (T)** → "Recent Entries" (Poppins SemiBold, 18px, #212121)
2. **Text (T)** → "View All" (14px, Poppins SemiBold, #00897B)
3. Position as row with space-between

**History Item Card (Create as Component):**

1. **Rectangle (R)** → 335×80
2. Fill: #FFFFFF
3. Corner radius: 16px
4. Effect → Drop Shadow: 0, 2px, 8px, rgba(0,0,0,0.04)

**Date Badge:**
1. **Rectangle (R)** → 48×48
2. Fill: #E0F2F1
3. Corner radius: 12px
4. **Text (T)** → "26" (Poppins Bold, 18px, #00897B)
5. **Text (T)** → "NOV" (10px, #757575, uppercase)

**Weight Info:**
1. **Text (T)** → "172.0 lbs" (Poppins Bold, 20px, #212121)
2. **Text (T)** → "Today, 7:32 AM" (13px, #757575)

**Change Badge:**
1. **Rectangle (R)** → Auto × 32
2. Fill: #E8F5E9 (green for down) or #FFEBEE (red for up)
3. Corner radius: 8px
4. **Text (T)** → "↓ 0.5" or "↑ 0.5" (14px, SemiBold)

**Make this a Component with Variants:**
- Variant: Change Direction (down, up, same)
- down: Green background, "↓"
- up: Red background, "↑"
- same: Orange background, "—"

**Duplicate to create 4 history items**

---

## PART 2.5: FAB & Bottom Navigation (8 min)

**Primary FAB (Add Weight):**
1. **Rectangle (R)** → 64×64
2. Fill: Linear gradient (135°) #00897B → #00695C
3. Corner radius: 20px
4. Effect → Drop Shadow: 0, 8px, 24px, rgba(0,137,123,0.4)
5. **Text (T)** → "+" (28px, white, bold)
6. Position: Bottom-right (X=287, Y=700)

**Make Component** → Name: `Add Weight FAB`

**Bottom Navigation:**
1. **Rectangle (R)** → 375×80
2. Fill: #FFFFFF
3. Border top: 1px #E0E0E0
4. Position: Bottom of frame

**4 Nav Items (equal width = 93.75px each):**

Each item contains:
- Icon (24px emoji)
- Label (11px text)
- Stacked vertically, centered

Items:
1. "🏠" + "Home" (active: #00897B)
2. "📊" + "Trends" (inactive: #757575)
3. "🎯" + "Goals" (inactive: #757575)
4. "👤" + "Profile" (inactive: #757575)

**Make Component with Variants** → Name: `Bottom Navigation`

---

# ⚖️ SCREEN 3: WEIGHT ENTRY (~40 min)

## PART 3.1: Status Bar & Navigation Header (4 min)

**Status Bar:**
1. Copy from previous screens
2. Background: #FFFFFF
3. Text/icons: Dark colors

**Navigation Header:**
1. **Rectangle (R)** → 375×56
2. Fill: #FFFFFF
3. Border bottom: 1px #E0E0E0

**Back Button:**
1. **Rectangle (R)** → 40×40 (transparent)
2. **Text (T)** → "←" (24px, #212121)
3. Position: X=16, Y=52

**Title:**
1. **Text (T)** → "Log Weight"
2. Font: Poppins SemiBold, 18px, #212121
3. Center in header

---

## PART 3.2: Date Selector Card (8 min)

1. **Rectangle (R)** → 327×120
2. Position: X=24, Y=124
3. Fill: #FFFFFF
4. Corner radius: 20px
5. Effect → Drop Shadow: 0, 4px, 16px, rgba(0,0,0,0.06)

**Label:**
1. **Text (T)** → "ENTRY DATE" (13px, Poppins Medium, #757575, uppercase)
2. Position: Top-left with 20px padding

**Date Navigation:**

Left Arrow:
1. **Rectangle (R)** → 44×44
2. Fill: #E0F2F1
3. Corner radius: 12px
4. **Text (T)** → "‹" (20px, #00897B)

Date Display:
1. **Text (T)** → "26" (Poppins Bold, 32px, #00897B)
2. **Text (T)** → "Tuesday, November 26, 2025" (14px, #757575)
3. Center between arrows

Right Arrow:
1. Same as left arrow but "›"
2. Opacity: 30% (future dates disabled)

**Today Badge:**
1. **Rectangle (R)** → Auto × 24
2. Fill: #E8F5E9
3. Corner radius: 12px
4. **Text (T)** → "Today" (12px, SemiBold, #4CAF50)

**Group all** → Rename: `Date Selector`

---

## PART 3.3: Weight Input Card ⭐ KEY FEATURE (15 min)

1. **Rectangle (R)** → 327×440
2. Position: X=24, Y=260
3. Fill: #FFFFFF
4. Corner radius: 24px
5. Effect → Drop Shadow: 0, 8px, 32px, rgba(0,105,92,0.12)

**Card Header:**
1. **Text (T)** → "Enter Your Weight" (Poppins SemiBold, 16px)
2. **Text (T)** → "Step on the scale and log your reading" (14px, #757575)

**Weight Display:**
1. **Rectangle (R)** → 279×96
2. Fill: #E0F2F1
3. Corner radius: 20px
4. **Text (T)** → "172.0" (Poppins Bold, 64px, #00897B)
5. **Text (T)** → "lbs" (24px, #757575)

**Quick Adjust Buttons:**
Create 4 buttons in a row:
1. Each: 56×40
2. Fill: #FFFFFF
3. Stroke: 2px #4DB6AC
4. Corner radius: 12px
5. Labels: "-1", "-0.5", "+0.5", "+1" (14px, SemiBold, #00897B)

**Unit Toggle:**
1. Two 80×40 buttons
2. Active (lbs): Fill #00897B, text white
3. Inactive (kg): Fill white, stroke #E0E0E0, text #757575
4. Corner radius: 12px

**Number Pad (3×4 grid):**
1. Each button: 93×56
2. Fill: #FFFFFF
3. Stroke: 2px #E0E0E0
4. Corner radius: 16px
5. Numbers: 1-9, ".", 0, "⌫"
6. Font: Poppins SemiBold, 24px
7. Gap: 12px between buttons

**Save Button:**
1. **Rectangle (R)** → 279×60
2. Fill: Linear gradient (135°) #00897B → #00695C
3. Corner radius: 18px
4. Effect → Drop Shadow: 0, 6px, 20px, rgba(0,137,123,0.35)
5. **Text (T)** → "✓ Save Entry" (Poppins SemiBold, 18px, white)

**Previous Entry Hint:**
1. **Rectangle (R)** → 279×44
2. Fill: #E0F2F1
3. Corner radius: 12px
4. **Text (T)** → "Last entry: 172.5 lbs on Nov 25" (13px, #757575)

**Group all** → Rename: `Weight Input Card`

---

## PART 3.4: Accessibility FAB (2 min)

1. Copy from Login screen
2. Position: Bottom-left (X=24, Y=740)

---

# 📱 SCREEN 4: SMS NOTIFICATIONS (~40 minutes)

## PART 4.1: Screen Setup (2 min)

1. Select frame `04_SMS_Notifications`
2. Background: #F5F5F5
3. This screen handles SMS permission requests and notification preferences

---

## PART 4.2: Header with Back Navigation (5 min)

### Header Background:
1. **Rectangle (R)** → 375×140
2. Fill: Linear gradient (180°)
   - Top: #00897B
   - Bottom: #00695C
3. Position: X=0, Y=0

### Back Button:
1. **Rectangle (R)** → 40×40
2. Fill: rgba(255, 255, 255, 0.2)
3. Corner radius: 12px
4. Position: X=20, Y=56
5. **Text (T)** → "←" (20px, White, Poppins SemiBold)
6. Center in rectangle

### Header Text:
1. **Text (T)** → "SMS Notifications" (20px, White, Poppins SemiBold)
2. Position: X=76, Y=58
3. **Text (T)** → "Manage text message alerts" (14px, rgba(255,255,255,0.8))
4. Position: X=76, Y=82

**Group all** → Rename: `Header`

---

## PART 4.3: Permission Card (10 min)

### Card Container:
1. **Rectangle (R)** → 335×200
2. Fill: White
3. Corner radius: 16px
4. Shadow: X=0, Y=2, Blur=8, rgba(0,0,0,0.08)
5. Position: X=20, Y=160

### Permission Icon:
1. **Rectangle (R)** → 48×48
2. Fill: Linear gradient (135°, #00897B → #00695C)
3. Corner radius: 12px
4. Position: X=40, Y=180
5. **Text (T)** → "📱" (24px)
6. Center in icon rectangle

### Permission Title Row:
1. **Text (T)** → "SMS Permission" (16px, #212121, Poppins SemiBold)
2. Position: X=100, Y=184

### Status Badge:
1. **Rectangle (R)** → 70×24
2. Fill: #FFF3E0 (pending state)
3. Corner radius: 12px
4. **Text (T)** → "Required" (12px, #FF9800, Poppins SemiBold)
5. Position badge at X=265, Y=182

### Description Text:
1. **Text (T)** → "To send you celebratory text messages when you hit your goals and milestones, we need permission to send SMS messages from your device."
2. Style: 14px, #757575, Source Sans Pro
3. Width: 295px
4. Position: X=40, Y=220

### Permission Button:
1. **Rectangle (R)** → 295×48
2. Fill: Linear gradient (135°, #00897B → #00695C)
3. Corner radius: 12px
4. Position: X=40, Y=300
5. **Text (T)** → "📲 Grant SMS Permission" (14px, White, Poppins SemiBold)
6. Center in button

**Group all** → Rename: `Permission Card`

---

## PART 4.4: Phone Number Card (8 min)

### Card Container:
1. **Rectangle (R)** → 335×140
2. Fill: White
3. Corner radius: 16px
4. Shadow: X=0, Y=2, Blur=8, rgba(0,0,0,0.08)
5. Position: X=20, Y=376

### Card Title:
1. **Text (T)** → "Your Phone Number" (16px, #212121, Poppins SemiBold)
2. Position: X=40, Y=396

### Description:
1. **Text (T)** → "Enter the phone number where you'd like to receive SMS notifications."
2. Style: 13px, #757575
3. Position: X=40, Y=420

### Country Code Input:
1. **Rectangle (R)** → 60×48
2. Fill: #F5F5F5
3. Stroke: 2px, #E0E0E0
4. Corner radius: 12px
5. Position: X=40, Y=456
6. **Text (T)** → "+1" (16px, #212121)
7. Center in rectangle

### Phone Input:
1. **Rectangle (R)** → 223×48
2. Fill: #F5F5F5
3. Stroke: 2px, #E0E0E0
4. Corner radius: 12px
5. Position: X=108, Y=456
6. **Text (T)** → "(555) 867-5309" (16px, #212121)
7. Position: X=124, Y=470

**Group all** → Rename: `Phone Number Card`

---

## PART 4.5: Notification Options Card (12 min)

### Card Container:
1. **Rectangle (R)** → 335×340
2. Fill: White
3. Corner radius: 16px
4. Shadow: X=0, Y=2, Blur=8, rgba(0,0,0,0.08)
5. Position: X=20, Y=532

### Card Title:
1. **Text (T)** → "Notification Preferences" (16px, #212121, Poppins SemiBold)
2. Position: X=40, Y=552

### Create Toggle Switch Component:
**Toggle - ON State:**
1. **Rectangle (R)** → 52×28
2. Fill: Linear gradient (135°, #00897B → #4CAF50)
3. Corner radius: 14px
4. **Ellipse (O)** → 24×24
5. Fill: White
6. Shadow: X=0, Y=2, Blur=4, rgba(0,0,0,0.2)
7. Position: X=26 from left of track (right side)

**Toggle - OFF State:**
1. **Rectangle (R)** → 52×28
2. Fill: #E0E0E0
3. Corner radius: 14px
4. **Ellipse (O)** → 24×24
5. Fill: White
6. Position: X=2 from left of track (left side)

### Option 1 - Master Toggle:
1. **Text (T)** → "📬 Enable SMS Notifications" (14px, #212121, Poppins Medium)
2. **Text (T)** → "Master toggle for all text message alerts" (12px, #757575)
3. Add Toggle (ON) at right side
4. Position row at Y=580
5. Add divider line below: 1px, #F0F0F0

### Option 2 - Goal Alerts:
1. **Text (T)** → "🎉 Goal Reached Alerts" (14px, #212121, Poppins Medium)
2. **Text (T)** → "Get a text when you hit your target weight" (12px, #757575)
3. Add Toggle (ON) at right side
4. Position row at Y=640
5. Add divider line below

### Option 3 - Milestone Alerts:
1. **Text (T)** → "💪 Milestone Alerts" (14px, #212121, Poppins Medium)
2. **Text (T)** → "Celebrate 5, 10, 25, 50 lb milestones" (12px, #757575)
3. Add Toggle (ON) at right side
4. Position row at Y=700
5. Add divider line below

### Option 4 - Daily Reminders:
1. **Text (T)** → "⏰ Daily Reminders" (14px, #212121, Poppins Medium)
2. **Text (T)** → "Get a daily text reminder to log your weight" (12px, #757575)
3. Add Toggle (OFF) at right side
4. Position row at Y=760

### Test SMS Button:
1. **Rectangle (R)** → 295×44
2. Fill: White
3. Stroke: 2px, #00897B
4. Corner radius: 12px
5. Position: X=40, Y=816
6. **Text (T)** → "🧪 Send Test Message" (14px, #00897B, Poppins SemiBold)
7. Center in button

**Group all** → Rename: `Options Card`

---

## PART 4.6: Info Banner (3 min)

1. **Rectangle (R)** → 335×64
2. Fill: #E0F2F1
3. Corner radius: 12px
4. Position: X=20, Y=888

### Info Content:
1. **Text (T)** → "💡" (20px)
2. Position: X=36, Y=904
3. **Text (T)** → "Standard messaging rates may apply. You can disable SMS notifications at any time from this screen."
4. Style: 13px, #00695C, Width=267px
5. Position: X=68, Y=900

**Group all** → Rename: `Info Banner`

---

# ✅ FINAL TOUCHES (10 minutes)

## Add Annotations for All Screens

Create text boxes pointing to key features:

**Login Screen:**
- "Tab toggle for Sign In/Register"
- "Input validation indicators"
- "Social login options"

**Dashboard:**
- "Progress Card with visual bar"
- "Trend badges with color coding"
- "Quick Stats at-a-glance"
- "FAB for quick weight logging"

**Weight Entry:**
- "Date picker with navigation"
- "Large touch-target numpad"
- "Quick adjust ± buttons"
- "Previous entry context"

**SMS Notifications:**
- "Permission request card with status"
- "Phone number input with country code"
- "Toggle switches for each notification type"
- "Test SMS button for verification"

---

## Export for Assignment

1. Select each frame
2. Bottom-right: Export section
3. Add: "2x" size, PNG format
4. Export all four screens

### Suggested File Names:
- `WeighToGo_01_Login.png`
- `WeighToGo_02_Dashboard.png`
- `WeighToGo_03_WeightEntry.png`
- `WeighToGo_04_SMSNotifications.png`

---

## 📸 EXPORT CHECKLIST

For your assignment submission:

### Login Screen:
- [ ] Full screen export (2x PNG)
- [ ] Auth toggle close-up
- [ ] Form fields detail

### Dashboard:
- [ ] Full screen export (2x PNG)
- [ ] Progress Card close-up
- [ ] History item detail

### Weight Entry:
- [ ] Full screen export (2x PNG)
- [ ] Number pad detail
- [ ] Date selector close-up

### SMS Notifications:
- [ ] Full screen export (2x PNG)
- [ ] Permission card close-up
- [ ] Toggle switches detail

---

## 💡 PRO TIPS

### Auto Layout (Saves Time!)
1. Select containers with multiple items
2. Right-click → "Add auto layout" (or Shift + A)
3. Set consistent spacing (8px, 12px, 16px)
4. Elements auto-space and resize

### Components & Variants
- Create components for buttons, inputs, cards
- Add variants for states (active/inactive, pressed, error)
- Update main component → all instances update

### Helpful Plugins
- **Iconify** - Free icons
- **Unsplash** - Real photos
- **Stark** - Accessibility checker
- **Content Reel** - Placeholder data

### Keyboard Shortcuts
- `R` - Rectangle
- `T` - Text
- `O` - Ellipse
- `V` - Move tool
- `Cmd/Ctrl + D` - Duplicate
- `Cmd/Ctrl + G` - Group
- `Shift + A` - Auto layout
- `Cmd/Ctrl + /` - Quick search

---

## 🎯 QUALITY CHECKLIST

Before submitting, verify for ALL screens:

- [ ] All touch targets ≥44px (48dp Android requirement)
- [ ] All text ≥12px
- [ ] Color contrast checked (4.5:1 minimum)
- [ ] Consistent 8px spacing grid
- [ ] All elements properly aligned
- [ ] Text uses consistent styles
- [ ] Colors use color styles (not random hex)
- [ ] Components created for repeated elements
- [ ] Frames fit 375×812 exactly
- [ ] Exports are high-resolution (2x)

---

## 🚨 COMMON MISTAKES TO AVOID

1. **Don't use random colors** - Always use your color styles
2. **Don't make tiny touch targets** - Minimum 44px!
3. **Don't forget corner radius** - Consistent rounding (12px, 16px, 20px)
4. **Don't skip shadows** - They create depth hierarchy
5. **Don't mix fonts** - Poppins for headings, Source Sans Pro for body
6. **Don't forget to group** - Keeps layers organized
7. **Don't over-design** - Follow Material Design principles
8. **Don't skip exports** - You need PNGs for your documents!

---

## ⏱️ TIME BREAKDOWN

### Setup (10 min)
- Create file and frames: 3 min
- Color styles: 4 min
- Text styles: 3 min

### Login Screen (40 min)
- Status bar + gradient: 5 min
- Branding section: 5 min
- Auth card + toggle: 8 min
- Form fields: 10 min
- Button + social login: 7 min
- Terms + accessibility: 5 min

### Dashboard (50 min)
- Header + greeting: 5 min
- Progress Card: 15 min
- Quick Stats: 8 min
- History section: 12 min
- FAB + Bottom nav: 10 min

### Weight Entry (40 min)
- Nav header: 4 min
- Date selector: 8 min
- Weight input card: 15 min
- Number pad: 8 min
- Save button + hints: 5 min

### SMS Notifications (40 min)
- Header with back nav: 5 min
- Permission card: 10 min
- Phone number card: 8 min
- Options card with toggles: 12 min
- Info banner: 5 min

### Final Touches (10 min)
- Annotations: 5 min
- Exports: 5 min

**Total: ~3 hours**

---

## 🎓 NEED HELP?

**Figma Resources:**
- Figma YouTube channel - Official tutorials
- Figma Community - Templates and examples
- Figma Help Center - Documentation

**Material Design Resources:**
- material.io/design - Full design system
- material.io/components - Component specs

**Your Reference Files:**
- Weigh to Go! Design Specifications (exact measurements)
- HTML Preview files (visual reference)
- Weight Tracking App Requirements document

---

**You've got this, Rick! (Pound for pound! 🎉) With your development background, you'll pick up Figma quickly. The design is well-specified and follows Material Design 3 principles throughout. Take it screen by screen, and you'll have a professional UI design for all four screens in about 3 hours.**

**Questions while building? Feel free to ask!** 🚀
