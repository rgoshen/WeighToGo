# WEIGH TO GO! MOBILE APP - DESIGN SPECIFICATIONS
## CS 360 Module Assignment - Rick Goshen

**App Name:** Weigh to Go!  
**Tagline:** "You've got this—pound for pound."

---

## 📱 FIGMA DESIGN SPECIFICATIONS

### CANVAS SETUP

**Frame Dimensions (All Screens):**
- Width: 375px (iPhone SE / Standard mobile)
- Height: 812px (iPhone X/11/12 standard)
- Background: #F5F5F5 (Dashboard, Entry) / Gradient (Login)

**Three Screens:**
1. `01_Login_Registration` - Authentication flow
2. `02_Main_Dashboard` - Primary user interface
3. `03_Weight_Entry` - Data input interface
4. `04_SMS_Notifications` - SMS permission and preferences

---

## 🎨 COLOR PALETTE (Health/Wellness Theme)

### Primary Colors
| Name | Hex | Usage |
|------|-----|-------|
| **Primary Teal** | `#00897B` | Primary actions, headers, active states |
| **Primary Dark** | `#00695C` | Gradient endpoints, pressed states |
| **Primary Light** | `#4DB6AC` | Borders, secondary elements |

### Accent Colors
| Name | Hex | Usage |
|------|-----|-------|
| **Accent Green** | `#4CAF50` | Success states, positive trends |
| **Success Light** | `#E8F5E9` | Success backgrounds, badges |
| **Warning Orange** | `#FF9800` | Neutral/unchanged states |
| **Error Red** | `#F44336` | Error states, negative trends |

### Surface Colors
| Name | Hex | Usage |
|------|-----|-------|
| **Surface White** | `#FFFFFF` | Cards, inputs, backgrounds |
| **Surface Variant** | `#E0F2F1` | Tinted backgrounds, inactive tabs |
| **Background** | `#F5F5F5` | Page backgrounds |

### Text Colors
| Name | Hex | Usage |
|------|-----|-------|
| **Text Primary** | `#212121` | Headlines, important text |
| **Text Secondary** | `#757575` | Metadata, descriptions, labels |
| **Divider** | `#E0E0E0` | Lines, borders, separators |

### Gradient Definitions
```
Header Gradient:
  Type: Linear
  Angle: 180° (top to bottom)
  Stop 1: #00897B (0%)
  Stop 2: #00695C (100%)

Button Gradient:
  Type: Linear
  Angle: 135°
  Stop 1: #00897B (0%)
  Stop 2: #00695C (100%)

Progress Bar Gradient:
  Type: Linear
  Angle: 90° (left to right)
  Stop 1: #00897B (0%)
  Stop 2: #4CAF50 (100%)
```

---

## 📐 SPACING SYSTEM (8px Grid)

```
4px   - Micro spacing (icon padding, tight gaps)
8px   - Small spacing (gaps between related items, text margins)
12px  - Medium spacing (chip gaps, element separation)
16px  - Standard spacing (card padding, section margins)
20px  - Large spacing (main content padding, major sections)
24px  - XL spacing (screen edges, section headers)
32px  - XXL spacing (major component separation)
```

---

## 📝 TYPOGRAPHY

### Font Families
- **Headlines/UI:** Poppins (Google Fonts)
- **Body Text:** Source Sans Pro (Google Fonts)

### Type Scale

```
App Name (Branding):
  Font: Poppins Bold
  Size: 32px
  Line Height: 38px
  Letter Spacing: -0.5px
  Color: #FFFFFF

Screen Title:
  Font: Poppins SemiBold
  Size: 18px
  Line Height: 24px
  Color: #212121

Section Title:
  Font: Poppins SemiBold
  Size: 18px
  Line Height: 22px
  Color: #212121

Card Title:
  Font: Poppins SemiBold
  Size: 16px
  Line Height: 20px
  Color: #212121

Large Number Display:
  Font: Poppins Bold
  Size: 64px
  Line Height: 1
  Color: #00897B

Stat Value:
  Font: Poppins Bold
  Size: 28px
  Line Height: 32px
  Color: #212121 (or #00897B for current)

Body Text:
  Font: Source Sans Pro Regular
  Size: 14px
  Line Height: 20px
  Color: #757575

Button Text:
  Font: Poppins SemiBold
  Size: 16px
  Line Height: 20px
  Color: #FFFFFF

Input Text:
  Font: Source Sans Pro Regular
  Size: 16px
  Line Height: 24px
  Color: #212121

Small Text/Labels:
  Font: Source Sans Pro Regular
  Size: 12-13px
  Line Height: 18px
  Color: #757575
  
Micro Text:
  Font: Source Sans Pro Regular
  Size: 10-11px
  Line Height: 14px
  Color: #757575
```

---

# 📱 SCREEN 1: LOGIN/REGISTRATION

## Component Specifications

### 1.1 STATUS BAR
```
Position: Top of frame
Size: 375×44px
Background: Transparent (over gradient)

Time:
  Position: Left, X=24
  Font: Poppins SemiBold, 14px
  Color: #FFFFFF

Status Icons:
  Position: Right, X=320
  Size: 12px each
  Gap: 6px
  Color: #FFFFFF
```

---

### 1.2 HEADER GRADIENT
```
Position: Y=0
Size: 375×294px
Fill: Linear gradient 180°
  - #00897B (top)
  - #00695C (bottom)
```

---

### 1.3 APP BRANDING

**App Icon:**
```
Position: Center X, Y=84
Size: 80×80px
Background: rgba(255,255,255,0.2)
Border: 2px rgba(255,255,255,0.3)
Corner Radius: 24px
Icon: 🎉 (40px, centered)
Backdrop Filter: blur(10px)
```

**App Name:**
```
Position: Center X, Y=180
Text: "Weigh to Go!"
Font: Poppins Bold, 32px
Color: #FFFFFF
Letter Spacing: -0.5px
```

**Tagline:**
```
Position: Center X, Y=218
Text: "You've got this—pound for pound."
Font: Source Sans Pro, 16px
Color: #FFFFFF (opacity 90%)
```

---

### 1.4 AUTH CARD CONTAINER
```
Position: Y=294
Size: 375×518px
Fill: #FFFFFF
Corner Radius: 32px 32px 0 0 (top only)
Shadow: 0px, -10px, 40px, rgba(0,0,0,0.1)
Padding: 32px 24px
```

---

### 1.5 AUTH TAB TOGGLE
```
Container:
  Position: X=24, Y=326
  Size: 327×52px
  Fill: #E0F2F1
  Corner Radius: 12px
  Padding: 4px

Active Tab:
  Size: 159.5×44px
  Fill: #FFFFFF
  Corner Radius: 10px
  Shadow: 0, 2px, 8px, rgba(0,137,123,0.15)
  Text: Poppins SemiBold, 14px, #00897B

Inactive Tab:
  Size: 159.5×44px
  Fill: Transparent
  Text: Poppins SemiBold, 14px, #757575
```

---

### 1.6 FORM INPUT FIELD
```
Label:
  Font: Poppins Medium, 13px
  Color: #757575
  Margin Bottom: 8px

Input Container:
  Size: 327×52px
  Fill: #FFFFFF
  Border: 2px solid #E0E0E0
  Corner Radius: 14px
  Padding Left: 48px

Focus State:
  Border: 2px solid #00897B
  Shadow: 0, 0, 0, 4px rgba(0,137,123,0.1)

Input Icon:
  Position: Left, X=16 from container edge
  Size: 20px
  Color: #757575

Placeholder Text:
  Font: Source Sans Pro, 16px
  Color: #BDBDBD

Password Toggle Icon:
  Position: Right, X=16 from right edge
  Size: 18px
  Color: #757575
```

---

### 1.7 PRIMARY BUTTON
```
Size: 327×56px
Fill: Linear gradient 135°
  - #00897B → #00695C
Corner Radius: 16px
Shadow: 0, 4px, 16px, rgba(0,137,123,0.35)

Text:
  Font: Poppins SemiBold, 16px
  Color: #FFFFFF
  Content: "Sign In →"
  Align: Center

Hover State:
  Transform: translateY(-2px)
  Shadow: 0, 6px, 20px, rgba(0,137,123,0.45)

Touch Target: 56px (meets 48dp Android requirement)
```

---

### 1.8 SOCIAL LOGIN SECTION

**Divider:**
```
Lines: 1px, #E0E0E0, 140px each side
Text: "or continue with"
  Font: Source Sans Pro, 13px
  Color: #757575
Gap: 16px from lines
```

**Social Buttons:**
```
Each Button:
  Size: 100×48px
  Fill: #FFFFFF
  Border: 2px solid #E0E0E0
  Corner Radius: 12px
  Gap: 12px between buttons

Content: "G" / "f" / "🍎"
  Font: 20px, centered
  Color: #212121

Hover State:
  Border: 2px solid #00897B
  Fill: #E0F2F1
```

---

### 1.9 ACCESSIBILITY FAB
```
Position: Bottom-right, X=303, Y=740
Size: 48×48px
Fill: #212121
Corner Radius: 50% (circle)
Shadow: 0, 4px, 12px, rgba(0,0,0,0.25)

Icon: ♿
  Size: 20px
  Color: #FFFFFF
  Align: Center

Active State:
  Fill: #00897B
```

---

# 📊 SCREEN 2: MAIN DASHBOARD

## Component Specifications

### 2.1 STATUS BAR
```
Same as Login, but on teal background
Color: #FFFFFF
```

---

### 2.2 HEADER SECTION
```
Background:
  Position: Y=0
  Size: 375×160px
  Fill: Linear gradient 180°
    - #00897B → #00695C

Greeting Text:
  Position: X=24, Y=60
  "Good morning,"
    Font: Source Sans Pro, 14px
    Color: #FFFFFF (opacity 90%)
  
  "Rick 👋"
    Font: Poppins Bold, 22px
    Color: #FFFFFF

Header Action Buttons:
  Size: 40×40px each
  Fill: rgba(255,255,255,0.2)
  Corner Radius: 12px
  Backdrop Filter: blur(10px)
  Gap: 12px
  Icons: 🔔, ⚙️ (18px)
```

---

### 2.3 PROGRESS CARD ⭐ KEY FEATURE
```
Container:
  Position: X=20, Y=120
  Size: 335×200px
  Fill: #FFFFFF
  Corner Radius: 24px
  Shadow: 0, 8px, 32px, rgba(0,105,92,0.15)
  Padding: 24px
  Z-Index: 10 (overlaps header)

Header Row:
  Title: "Progress to Goal"
    Font: Poppins SemiBold, 16px
    Color: #212121
  
  Subtitle: "You're doing great! Keep it up!"
    Font: Source Sans Pro, 13px
    Color: #757575

Trend Badge:
  Fill: #E8F5E9
  Corner Radius: 20px
  Padding: 6px 12px
  Text: "↓ 2.5 lbs"
    Font: Poppins SemiBold, 13px
    Color: #4CAF50
  Position: Top-right

Stats Row:
  Layout: 3 columns with dividers
  
  Each Stat:
    Value:
      Font: Poppins Bold, 28px
      Color: #212121 (Start, Goal)
      Color: #00897B (Current - highlighted)
    
    Label:
      Font: Source Sans Pro, 12px
      Color: #757575
      Text-transform: uppercase
      Letter-spacing: 0.5px
  
  Dividers:
    Size: 1×40px
    Color: #E0E0E0

Progress Bar:
  Container:
    Size: 287×12px
    Fill: #E0F2F1
    Corner Radius: 12px
  
  Fill:
    Size: 195×12px (68% progress)
    Fill: Linear gradient 90°
      - #00897B → #4CAF50
    Corner Radius: 12px
  
  Handle:
    Size: 20×20px circle
    Fill: #FFFFFF
    Border: 3px solid #4CAF50
    Position: Right end of fill
    Shadow: 0, 2px, 8px, rgba(0,0,0,0.15)

Progress Labels:
  Font: Source Sans Pro, 12px
  Color: #757575
  Left: "Started Oct 1"
  Right: "68% Complete"
```

---

### 2.4 QUICK STATS CARDS
```
Container:
  Position: X=20, Y=340
  Layout: Horizontal, 3 cards
  Gap: 12px

Each Card:
  Size: 105×80px
  Fill: #FFFFFF
  Corner Radius: 16px
  Shadow: 0, 2px, 12px, rgba(0,0,0,0.06)
  Padding: 16px
  Text-align: center

Content:
  Icon: 24px emoji (📉, 🎯, 🔥)
  Value: Poppins Bold, 18px, #212121
  Label: Source Sans Pro, 11px, #757575

Cards:
  1. Icon: 📉, Value: "-13", Label: "Total Lost"
  2. Icon: 🎯, Value: "7", Label: "lbs to Goal"
  3. Icon: 🔥, Value: "14", Label: "Day Streak"
```

---

### 2.5 SECTION HEADER
```
Position: X=20, Y=440
Layout: Space-between

Title:
  Font: Poppins SemiBold, 18px
  Color: #212121

Action Link:
  Font: Poppins SemiBold, 14px
  Color: #00897B
  Text: "View All"
```

---

### 2.6 HISTORY ITEM CARD
```
Container:
  Size: 335×80px
  Fill: #FFFFFF
  Corner Radius: 16px
  Shadow: 0, 2px, 8px, rgba(0,0,0,0.04)
  Padding: 16px
  Margin Bottom: 12px

Date Badge:
  Size: 48×48px
  Fill: #E0F2F1
  Corner Radius: 12px
  
  Day:
    Font: Poppins Bold, 18px
    Color: #00897B
  
  Month:
    Font: Source Sans Pro, 10px
    Color: #757575
    Text-transform: uppercase

Weight Info:
  Position: Flex, left of change badge
  
  Weight Value:
    Font: Poppins Bold, 20px
    Color: #212121
    Format: "172.0 lbs"
  
  Time:
    Font: Source Sans Pro, 13px
    Color: #757575
    Format: "Today, 7:32 AM"

Change Badge:
  Size: Auto×32px
  Corner Radius: 8px
  Padding: 6px 10px
  Font: Poppins SemiBold, 14px
  
  Variants:
    Down (Positive):
      Fill: #E8F5E9
      Color: #4CAF50
      Content: "↓ 0.5"
    
    Up (Negative):
      Fill: #FFEBEE
      Color: #F44336
      Content: "↑ 0.5"
    
    Same (Neutral):
      Fill: #FFF3E0
      Color: #FF9800
      Content: "— 0.0"
```

---

### 2.7 ADD WEIGHT FAB
```
Position: X=287, Y=700
Size: 64×64px
Fill: Linear gradient 135°
  - #00897B → #00695C
Corner Radius: 20px
Shadow: 0, 8px, 24px, rgba(0,137,123,0.4)
Z-Index: 100

Icon: "+"
  Font: Poppins Bold, 28px
  Color: #FFFFFF

Hover/Active State:
  Transform: scale(1.05)
  Shadow: 0, 12px, 32px, rgba(0,137,123,0.5)

Touch Target: 64px (exceeds 48dp requirement)
```

---

### 2.8 BOTTOM NAVIGATION
```
Container:
  Position: Bottom of frame
  Size: 375×80px
  Fill: #FFFFFF
  Border-top: 1px solid #E0E0E0

Nav Items:
  Count: 4
  Width: 93.75px each (375/4)
  Layout: Vertical stack (icon + label)
  
  Icon:
    Size: 24px
    Color: #757575 (inactive) / #00897B (active)
  
  Label:
    Font: Source Sans Pro, 11px
    Color: #757575 (inactive) / #00897B (active)
    Active: SemiBold weight

Items:
  1. 🏠 Home (Active)
  2. 📊 Trends
  3. 🎯 Goals
  4. 👤 Profile
```

---

# ⚖️ SCREEN 3: WEIGHT ENTRY

## Component Specifications

### 3.1 STATUS BAR
```
Background: #FFFFFF
Time/Icons: #212121
```

---

### 3.2 NAVIGATION HEADER
```
Size: 375×56px
Fill: #FFFFFF
Border-bottom: 1px solid #E0E0E0

Back Button:
  Position: X=16
  Size: 40×40px touch target
  Icon: "←" (24px, #212121)
  Corner Radius: 12px

Title:
  Text: "Log Weight"
  Font: Poppins SemiBold, 18px
  Color: #212121
  Align: Center (accounting for back button space)
```

---

### 3.3 DATE SELECTOR CARD
```
Container:
  Position: X=24, Y=124
  Size: 327×120px
  Fill: #FFFFFF
  Corner Radius: 20px
  Shadow: 0, 4px, 16px, rgba(0,0,0,0.06)
  Padding: 20px

Label:
  Text: "ENTRY DATE"
  Font: Poppins Medium, 13px
  Color: #757575
  Text-transform: uppercase
  Letter-spacing: 0.5px
  Margin-bottom: 12px

Navigation Arrows:
  Size: 44×44px each
  Fill: #E0F2F1
  Corner Radius: 12px
  Icon: "‹" or "›" (20px, #00897B)
  
  Disabled State (future dates):
    Opacity: 30%
    Pointer-events: none

Date Display:
  Day:
    Font: Poppins Bold, 32px
    Color: #00897B
  
  Full Date:
    Font: Source Sans Pro, 14px
    Color: #757575
    Format: "Tuesday, November 26, 2025"

Today Badge:
  Fill: #E8F5E9
  Corner Radius: 12px
  Padding: 4px 12px
  Text: "Today"
    Font: Poppins SemiBold, 12px
    Color: #4CAF50
  Margin-top: 8px
```

---

### 3.4 WEIGHT INPUT CARD ⭐ KEY FEATURE
```
Container:
  Position: X=24, Y=260
  Size: 327×440px
  Fill: #FFFFFF
  Corner Radius: 24px
  Shadow: 0, 8px, 32px, rgba(0,105,92,0.12)
  Padding: 24px 24px 20px
  Flex-direction: column

Card Header:
  Title: "Enter Your Weight"
    Font: Poppins SemiBold, 16px
    Color: #212121
    Margin-bottom: 8px
  
  Subtitle: "Step on the scale and log your reading"
    Font: Source Sans Pro, 14px
    Color: #757575
    Margin-bottom: 24px

Weight Display:
  Container:
    Size: 279×96px
    Fill: #E0F2F1
    Corner Radius: 20px
    Padding: 24px
    Text-align: center
    Margin-bottom: 20px
  
  Value:
    Font: Poppins Bold, 64px
    Color: #00897B
    Content: "172.0"
  
  Unit:
    Font: Source Sans Pro, 24px
    Color: #757575
    Margin-left: 8px

Quick Adjust Buttons:
  Layout: Horizontal, 4 buttons
  Gap: 8px
  Margin-bottom: 16px
  Justify: center
  
  Each Button:
    Size: 56×40px
    Fill: #FFFFFF
    Border: 2px solid #4DB6AC
    Corner Radius: 12px
    Text: Poppins SemiBold, 14px, #00897B
    
    Active State:
      Fill: #E0F2F1
  
  Values: "-1", "-0.5", "+0.5", "+1"

Unit Toggle:
  Layout: Horizontal, 2 buttons
  Gap: 8px
  Justify: center
  Margin-bottom: 24px
  
  Each Button:
    Size: 80×40px
    Corner Radius: 12px
    Font: Poppins SemiBold, 14px
    
    Active State:
      Fill: #00897B
      Color: #FFFFFF
    
    Inactive State:
      Fill: #FFFFFF
      Border: 2px solid #E0E0E0
      Color: #757575

Number Pad:
  Layout: Grid, 3 columns × 4 rows
  Gap: 12px
  
  Each Button:
    Size: 93×56px
    Fill: #FFFFFF
    Border: 2px solid #E0E0E0
    Corner Radius: 16px
    Font: Poppins SemiBold, 24px
    Color: #212121
    
    Active/Pressed State:
      Fill: #E0F2F1
      Transform: scale(0.95)
    
    Special Buttons:
      Decimal (.): Font-size: 32px
      Backspace (⌫): Font-size: 20px, Color: #757575
  
  Values: 1, 2, 3, 4, 5, 6, 7, 8, 9, ., 0, ⌫

Save Button:
  Size: 279×60px
  Fill: Linear gradient 135°
    - #00897B → #00695C
  Corner Radius: 18px
  Shadow: 0, 6px, 20px, rgba(0,137,123,0.35)
  Margin-top: 20px
  
  Content:
    Icon: "✓"
    Text: "Save Entry"
    Font: Poppins SemiBold, 18px
    Color: #FFFFFF
    Gap: 10px
  
  Hover State:
    Transform: translateY(-2px)
    Shadow: 0, 8px, 24px, rgba(0,137,123,0.45)
  
  Touch Target: 60px (exceeds 48dp requirement)

Previous Entry Hint:
  Container:
    Size: 279×44px
    Fill: #E0F2F1
    Corner Radius: 12px
    Margin-top: 16px
    Padding: 12px
    Text-align: center
  
  Content:
    Font: Source Sans Pro, 13px
    Color: #757575
    Format: "Last entry: 172.5 lbs on Nov 25"
    
    Highlighted Value:
      Font: Poppins SemiBold
      Color: #00897B
```

---

### 3.5 ACCESSIBILITY FAB
```
Position: X=24, Y=740 (bottom-left)
Size: 44×44px
Fill: #212121
Corner Radius: 50%
Shadow: 0, 4px, 12px, rgba(0,0,0,0.25)

Icon: ♿ (18px, #FFFFFF)
```

---

# 📱 SCREEN 4: SMS NOTIFICATIONS

## Component Specifications

### 4.1 STATUS BAR
```
Background: Transparent (over gradient)
Time/Icons: #FFFFFF
```

---

### 4.2 HEADER WITH BACK NAVIGATION
```
Container:
  Position: X=0, Y=0
  Size: 375×140px
  Fill: Linear gradient (180°)
    Stop 1: #00897B (0%)
    Stop 2: #00695C (100%)
  Padding: 60px 20px 24px 20px

Back Button:
  Position: X=20, Y=56
  Size: 40×40px
  Fill: rgba(255, 255, 255, 0.2)
  Corner Radius: 12px
  
  Icon:
    Content: "←"
    Font: Poppins SemiBold, 20px
    Color: #FFFFFF
    Align: Center

Title Section:
  Position: X=76, Y=58
  
  Title:
    Text: "SMS Notifications"
    Font: Poppins SemiBold, 20px
    Color: #FFFFFF
  
  Subtitle:
    Text: "Manage text message alerts"
    Font: Source Sans Pro, 14px
    Color: rgba(255, 255, 255, 0.8)
    Margin-top: 4px
```

---

### 4.3 PERMISSION CARD ⭐ KEY FEATURE
```
Container:
  Position: X=20, Y=160
  Size: 335×200px
  Fill: #FFFFFF
  Corner Radius: 16px
  Shadow: 0, 2px, 8px, rgba(0,0,0,0.08)
  Padding: 20px

Permission Icon:
  Position: X=20 (relative to card)
  Size: 48×48px
  Fill: Linear gradient (135°, #00897B → #00695C)
  Corner Radius: 12px
  
  Emoji:
    Content: "📱"
    Font-size: 24px
    Align: Center

Permission Header Row:
  Position: X=80 (relative to card), Y=20
  
  Title:
    Text: "SMS Permission"
    Font: Poppins SemiBold, 16px
    Color: #212121

Status Badge:
  Position: Right-aligned
  Size: Auto × 24px
  Corner Radius: 12px
  Padding: 4px 8px
  
  Pending State:
    Fill: #FFF3E0
    Text: "Required"
    Text Color: #FF9800
    Font: Poppins SemiBold, 12px
  
  Granted State:
    Fill: #E8F5E9
    Text: "Granted"
    Text Color: #4CAF50
  
  Denied State:
    Fill: #FFEBEE
    Text: "Denied"
    Text Color: #F44336

Description:
  Position: X=20, Y=80 (relative to card)
  Width: 295px
  Text: "To send you celebratory text messages when you hit your goals and milestones, we need permission to send SMS messages from your device."
  Font: Source Sans Pro, 14px
  Color: #757575
  Line-height: 1.5
  Margin-bottom: 16px

Permission Button:
  Position: X=20, Y=140 (relative to card)
  Size: 295×48px
  Fill: Linear gradient (135°, #00897B → #00695C)
  Corner Radius: 12px
  
  Pending State:
    Icon: "📲"
    Text: "Grant SMS Permission"
    Font: Poppins SemiBold, 14px
    Color: #FFFFFF
    Align: Center (with 8px gap between icon and text)
  
  Granted State:
    Fill: #E8F5E9
    Icon: "✓"
    Text: "Permission Granted"
    Color: #4CAF50
  
  Denied State:
    Fill: Linear gradient (135°, #00897B → #00695C)
    Text: "Open Settings"
    Color: #FFFFFF
```

---

### 4.4 PHONE NUMBER CARD
```
Container:
  Position: X=20, Y=376
  Size: 335×140px
  Fill: #FFFFFF
  Corner Radius: 16px
  Shadow: 0, 2px, 8px, rgba(0,0,0,0.08)
  Padding: 20px

Card Title:
  Text: "Your Phone Number"
  Font: Poppins SemiBold, 16px
  Color: #212121
  Margin-bottom: 8px

Description:
  Text: "Enter the phone number where you'd like to receive SMS notifications."
  Font: Source Sans Pro, 13px
  Color: #757575
  Line-height: 1.4
  Margin-bottom: 16px

Phone Input Row:
  Display: Flex row
  Gap: 8px

Country Code Input:
  Size: 60×48px
  Fill: #F5F5F5
  Stroke: 2px solid #E0E0E0
  Corner Radius: 12px
  
  Text:
    Content: "+1"
    Font: Source Sans Pro, 16px
    Color: #212121
    Align: Center
  
  State: Read-only (US default)

Phone Number Input:
  Size: 223×48px (flex: 1)
  Fill: #F5F5F5
  Stroke: 2px solid #E0E0E0
  Corner Radius: 12px
  Padding: 14px 16px
  
  Text:
    Font: Source Sans Pro, 16px
    Color: #212121
  
  Placeholder:
    Text: "(555) 123-4567"
    Color: #9E9E9E
  
  Focus State:
    Stroke: 2px solid #00897B
```

---

### 4.5 NOTIFICATION OPTIONS CARD ⭐ KEY FEATURE
```
Container:
  Position: X=20, Y=532
  Size: 335×340px
  Fill: #FFFFFF
  Corner Radius: 16px
  Shadow: 0, 2px, 8px, rgba(0,0,0,0.08)
  Padding: 20px

Card Title:
  Text: "Notification Preferences"
  Font: Poppins SemiBold, 16px
  Color: #212121
  Margin-bottom: 4px

Option Item (Repeated Component):
  Size: 295 × 60px
  Padding: 16px 0
  Border-bottom: 1px solid #F0F0F0 (except last)
  Display: Flex row, space-between
  
  Content Side:
    Flex: 1
    Padding-right: 16px
    
    Title:
      Font: Poppins Medium, 14px
      Color: #212121
      Display: Flex row
      Gap: 8px
      
      Emoji: 16px
    
    Description:
      Font: Source Sans Pro, 12px
      Color: #757575
      Line-height: 1.4
      Margin-top: 4px
  
  Toggle Side:
    Align: flex-end

Toggle Switch Component:
  Size: 52×28px
  Corner Radius: 14px
  Transition: 300ms ease
  
  Track OFF:
    Fill: #E0E0E0
  
  Track ON:
    Fill: Linear gradient (135°, #00897B → #4CAF50)
  
  Knob:
    Size: 24×24px
    Fill: #FFFFFF
    Corner Radius: 50%
    Shadow: 0, 2px, 4px, rgba(0,0,0,0.2)
    
    OFF Position: X=2 (left)
    ON Position: X=26 (right, transform: translateX(24px))

Option 1 - Master Toggle:
  Emoji: "📬"
  Title: "Enable SMS Notifications"
  Description: "Master toggle for all text message alerts"
  Default State: ON

Option 2 - Goal Alerts:
  Emoji: "🎉"
  Title: "Goal Reached Alerts"
  Description: "Get a text when you hit your target weight"
  Default State: ON

Option 3 - Milestone Alerts:
  Emoji: "💪"
  Title: "Milestone Alerts"
  Description: "Celebrate 5, 10, 25, 50 lb milestones"
  Default State: ON

Option 4 - Daily Reminders:
  Emoji: "⏰"
  Title: "Daily Reminders"
  Description: "Get a daily text reminder to log your weight"
  Default State: OFF

Test SMS Button:
  Position: Below options, centered
  Size: 295×44px
  Fill: #FFFFFF
  Stroke: 2px solid #00897B
  Corner Radius: 12px
  Margin-top: 16px
  
  Content:
    Icon: "🧪"
    Text: "Send Test Message"
    Font: Poppins SemiBold, 14px
    Color: #00897B
    Gap: 8px
    Align: Center
```

---

### 4.6 INFO BANNER
```
Container:
  Position: X=20, Y=888
  Size: 335×64px
  Fill: #E0F2F1
  Corner Radius: 12px
  Padding: 16px
  Display: Flex row
  Gap: 12px
  Align-items: flex-start

Info Icon:
  Content: "💡"
  Font-size: 20px
  Flex-shrink: 0

Info Text:
  Text: "Standard messaging rates may apply. You can disable SMS notifications at any time from this screen."
  Font: Source Sans Pro, 13px
  Color: #00695C
  Line-height: 1.5
  Width: 267px
```

---

## 🔐 PERMISSION FLOW STATES

### State 1: Permission Not Yet Requested
```
Permission Card:
  Status Badge: "Required" (#FFF3E0 bg, #FF9800 text)
  Button: "📲 Grant SMS Permission" (primary gradient)
  
Phone Card: Visible, editable
Options Card: Visible, toggles disabled (grayed out)
Info Banner: Visible
```

### State 2: Permission Granted
```
Permission Card:
  Status Badge: "Granted" (#E8F5E9 bg, #4CAF50 text)
  Button: "✓ Permission Granted" (#E8F5E9 bg, #4CAF50 text)
  
Phone Card: Visible, editable
Options Card: Visible, toggles enabled
Info Banner: Visible
Test SMS Button: Enabled
```

### State 3: Permission Denied
```
Permission Card:
  Status Badge: "Denied" (#FFEBEE bg, #F44336 text)
  Button: "Open Settings" (primary gradient)
  Description: Updated to explain how to enable in Settings
  
Phone Card: Visible but grayed
Options Card: Visible, toggles disabled (grayed out)
Info Banner: Updated text about Settings
```

---

### 4.7 ACCESSIBILITY CONSIDERATIONS
```
Touch Targets:
  - All toggles: 52×28px track + larger touch area
  - Permission button: 295×48px (exceeds 44px minimum)
  - Back button: 40×40px
  - Test SMS button: 295×44px

Content Descriptions:
  - Toggle switches: "Enable [notification type], currently [on/off]"
  - Permission button: Announces current state
  - Status badge: Read by screen reader

Focus Order:
  1. Back button
  2. Permission card (button if actionable)
  3. Phone number input
  4. Each toggle in order
  5. Test SMS button
  6. Info banner

Color Contrast:
  - All text meets WCAG AA (4.5:1 ratio)
  - Status badges have sufficient contrast
  - Toggle states clearly distinguishable
```

---

### Login/Registration Screen

| Element | Action | Data Collected |
|---------|--------|----------------|
| Auth Toggle | Switch form type | Selected mode |
| Email Input | Text entry | Email/username string |
| Password Input | Secure text entry | Password (hashed) |
| Password Toggle | Show/hide password | None |
| Sign In Button | Submit credentials | Auth attempt timestamp |
| Forgot Password | Navigate to recovery | User identifier |
| Social Buttons | OAuth flow | OAuth token |
| Accessibility FAB | Open settings | Preference changes |

### Main Dashboard Screen

| Element | Action | Data Collected |
|---------|--------|----------------|
| Notification Bell | View notifications | View timestamp |
| Settings Gear | Open settings | None |
| Progress Card | Expand details | View timestamp |
| Quick Stats | Tap for details | Selected stat |
| History Item | View entry detail | Entry ID |
| View All | Load full history | Scroll position |
| Add FAB | Open weight entry | None |
| Bottom Nav | Switch screens | Selected tab |

### Weight Entry Screen

| Element | Action | Data Collected |
|---------|--------|----------------|
| Back Button | Return to dashboard | None |
| Date Arrows | Navigate dates | Selected date |
| Quick Adjust | Modify weight value | Adjustment amount |
| Unit Toggle | Switch lbs/kg | Preferred unit |
| Number Pad | Input weight digits | Weight value |
| Backspace | Delete digit | None |
| Save Button | Store entry | Weight, date, timestamp |
| Accessibility FAB | Open settings | Preference changes |

### SMS Notifications Screen

| Element | Action | Data Collected |
|---------|--------|----------------|
| Back Button | Return to previous screen | None |
| Permission Button | Request SEND_SMS permission | Permission grant status |
| Country Code | Display country prefix | None (read-only) |
| Phone Input | Enter phone number | Phone number (E.164) |
| Master Toggle | Enable/disable all SMS | sms_notifications_enabled |
| Goal Alerts Toggle | Enable goal notifications | sms_goal_alerts |
| Milestone Toggle | Enable milestone notifications | sms_milestone_alerts |
| Daily Reminder Toggle | Enable daily reminders | sms_reminder_enabled |
| Test SMS Button | Send test message | Test timestamp, delivery status |

---

## 📐 ANDROID DESIGN GUIDELINES COMPLIANCE

### Material Design 3 Components Used
✅ Elevated cards with proper shadows and corner radius
✅ Floating Action Button (FAB) for primary action
✅ Bottom Navigation with proper tab structure
✅ Proper elevation hierarchy throughout
✅ Touch ripple effects on interactive elements
✅ Consistent color theming with surface colors

### Accessibility Standards
✅ Touch targets minimum 44px (Android 48dp minimum exceeded)
✅ Color contrast ratio 4.5:1 (WCAG AA compliant)
✅ Text sizing 12px minimum throughout
✅ Screen reader labels on all interactive elements
✅ Accessibility toggle FAB on every screen
✅ Focus indicators for keyboard navigation
✅ Semantic heading structure

### Touch & Gesture Support
✅ Tap: All button/card interactions
✅ Long-press: Potential for context menus (future)
✅ Swipe: Date navigation in entry screen
✅ Pull-to-refresh: History list (implementation ready)
✅ Input gestures: Number pad interactions

---

## 🏗️ FIGMA LAYER STRUCTURE

### Screen 1: Login/Registration
```
📱 01_Login_Registration (375×812)
├── 📊 Status Bar
│   ├── Time (Text)
│   └── Icons (Group)
├── 🎨 Header Gradient (Rectangle)
├── 🏷️ Branding Section
│   ├── App Icon (Frame) - 🎉
│   ├── App Name (Text) - "Weigh to Go!"
│   └── Tagline (Text) - "You've got this—pound for pound."
└── 📄 Auth Card Container
    ├── Auth Toggle (Component)
    │   ├── Active Tab
    │   └── Inactive Tab
    ├── 📝 Form Section (Auto Layout ↓)
    │   ├── Email Input (Component)
    │   ├── Password Input (Component)
    │   └── Forgot Password Link
    ├── 🔘 Primary Button (Component)
    ├── ➖ Divider Section
    ├── 🌐 Social Buttons (Auto Layout →)
    ├── 📜 Terms Text
    └── ♿ Accessibility FAB (Component)
```

### Screen 2: Main Dashboard
```
📱 02_Main_Dashboard (375×812)
├── 📊 Status Bar
├── 🎨 Header Section
│   ├── Header Gradient
│   ├── Greeting (Auto Layout ↓)
│   └── Header Actions (Auto Layout →)
├── 📈 Progress Card ⭐ (Component)
│   ├── Header Row (Auto Layout →)
│   ├── Stats Row (Auto Layout →)
│   ├── Progress Bar
│   └── Progress Labels
├── 📊 Quick Stats (Auto Layout →)
│   ├── Stat Card 1 (Component)
│   ├── Stat Card 2 (Component)
│   └── Stat Card 3 (Component)
├── 📝 History Section
│   ├── Section Header
│   └── History List (Auto Layout ↓)
│       ├── History Item 1 (Component)
│       ├── History Item 2 (Component)
│       ├── History Item 3 (Component)
│       └── History Item 4 (Component)
├── ➕ Add Weight FAB (Component) ⭐
├── ♿ Accessibility FAB (Component)
└── 📍 Bottom Navigation (Component)
```

### Screen 3: Weight Entry
```
📱 03_Weight_Entry (375×812)
├── 📊 Status Bar
├── 🔙 Navigation Header
│   ├── Back Button
│   └── Screen Title
├── 📅 Date Selector Card (Component)
│   ├── Label
│   ├── Navigation Arrows
│   ├── Date Display
│   └── Today Badge
└── ⚖️ Weight Input Card ⭐ (Component)
    ├── Card Header
    ├── Weight Display
    ├── Quick Adjust (Auto Layout →)
    ├── Unit Toggle (Auto Layout →)
    ├── Number Pad (Grid 3×4)
    ├── Save Button (Component)
    └── Previous Entry Hint
└── ♿ Accessibility FAB (Component)
```

### Screen 4: SMS Notifications
```
📱 04_SMS_Notifications (375×812)
├── 📊 Status Bar (transparent)
├── 🔙 Header with Back Navigation
│   ├── Gradient Background
│   ├── Back Button
│   ├── Screen Title
│   └── Subtitle
├── 🔐 Permission Card ⭐ (Component)
│   ├── Permission Icon
│   ├── Title + Status Badge
│   ├── Description Text
│   └── Permission Button
├── 📞 Phone Number Card (Component)
│   ├── Card Title
│   ├── Description
│   └── Phone Input Row (Auto Layout →)
│       ├── Country Code Input
│       └── Phone Number Input
├── ⚙️ Notification Options Card ⭐ (Component)
│   ├── Card Title
│   ├── Option Items (Auto Layout ↓)
│   │   ├── Master Toggle Row
│   │   ├── Goal Alerts Row
│   │   ├── Milestone Alerts Row
│   │   └── Daily Reminders Row
│   └── Test SMS Button
└── 💡 Info Banner (Component)
    ├── Info Icon
    └── Info Text
```

⭐ = Key feature components

---

## 📸 EXPORT SETTINGS

### For Assignment Submission

**All Screens:**
- Format: PNG
- Resolution: 2x (750×1624)
- Include annotations layer if helpful

### Suggested File Names:
```
WeighToGo_01_Login.png
WeighToGo_02_Dashboard.png
WeighToGo_03_WeightEntry.png
WeighToGo_04_SMSNotifications.png
WeighToGo_01_Login_Detail_AuthToggle.png
WeighToGo_02_Dashboard_Detail_ProgressCard.png
WeighToGo_03_WeightEntry_Detail_Numpad.png
WeighToGo_04_SMSNotifications_Detail_Toggles.png
```

---

## 🔄 PROTOTYPE INTERACTIONS

### Key Flows to Prototype

**Screen Transitions:**
1. Login → Dashboard (after successful auth)
2. Dashboard FAB → Weight Entry
3. Weight Entry Back → Dashboard
4. Dashboard Settings → SMS Notifications
5. SMS Notifications Back → Dashboard

**Within-Screen Interactions:**
- Login: Tab toggle animation
- Dashboard: History item expand
- Entry: Number pad input, date navigation
- SMS: Toggle switches, permission button states

### Transition Specifications
```
Screen Change:
  Type: Smart Animate
  Duration: 300ms
  Easing: Ease Out

Tab/Toggle:
  Type: Instant
  Duration: 0ms

Button Press:
  Type: Dissolve
  Duration: 150ms

Card Expand:
  Type: Smart Animate
  Duration: 250ms
```

---

## ✅ DESIGN VALIDATION CHECKLIST

### All Screens
- [ ] Frame size: 375×812px
- [ ] 8px grid alignment verified
- [ ] Color styles applied (no hardcoded hex)
- [ ] Text styles applied consistently
- [ ] All touch targets ≥44px
- [ ] Accessibility FAB present
- [ ] Shadow hierarchy correct

### Login Screen
- [ ] Gradient renders correctly
- [ ] Tab toggle functional in prototype
- [ ] Form fields properly spaced
- [ ] Social buttons aligned

### Dashboard Screen
- [ ] Progress card overlaps header correctly
- [ ] Stats cards evenly spaced
- [ ] History items use component
- [ ] FAB positioned correctly
- [ ] Bottom nav items equal width

### Weight Entry Screen
- [ ] Date selector shows today
- [ ] Number pad grid aligned
- [ ] Save button prominent
- [ ] Previous entry shows context

### SMS Notifications Screen
- [ ] Header gradient matches login
- [ ] Back button touch target ≥40px
- [ ] Permission card status badge visible
- [ ] Phone input fields aligned
- [ ] All toggle switches functional
- [ ] Test SMS button styled correctly
- [ ] Info banner positioned at bottom

---

## 🎓 COURSE CONCEPTS APPLIED

### From CS 360 Chapters

**Chapter 2 - Layouts & Widgets:**
- LinearLayout concepts (vertical stacks)
- ConstraintLayout positioning
- CardView implementations
- Input field patterns

**Chapter 3 - Activities & Intents:**
- Screen navigation design
- Data passing between screens
- Activity lifecycle considerations

**Chapter 4 - Menus, Dialogs & Touch:**
- Bottom navigation pattern
- Dialog-style cards
- Touch target requirements
- Gesture support design

---

**END OF SPECIFICATIONS**

Rick, use this document as your complete reference for building all four **Weigh to Go!** screens in Figma. Every measurement, color, typography setting, and interaction is specified according to Android Design Guidelines and Material Design 3 principles. This ensures your UI design aligns with the technical requirements document and will translate smoothly to Java/XML implementation in Android Studio.

*You've got this—pound for pound!* 🎉
