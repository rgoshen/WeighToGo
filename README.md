# 🎉 Weigh to Go!

> **"You've got this—pound for pound."**

A simple, effective Android mobile application for daily weight tracking and goal achievement. Built with Java and Android Studio for CS 360: Mobile Architecture & Programming at Southern New Hampshire University.

![Android CI](https://github.com/rgoshen-snhu/WeighToGo/actions/workflows/android-ci.yml/badge.svg)
![Android](https://img.shields.io/badge/Android-14+-3DDC84?style=flat&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.2+-02303A?style=flat&logo=gradle&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

---
![login screen](./previews/weight_tracker_login-register.jpg)

![dashboard](./previews/weight_tracker_main.jpg)

![entry screen](./previews/weight_tracker_weight_entry.jpg)

![goals](./previews/weight_tracker_goals.jpg)

![goals entry](./previews/weight_tracker_goals_entry.jpg)

![settings](./previews/weight_tracker_settings.jpg)
---
## 📱 About

**Weigh to Go!** is a streamlined weight tracking application designed to help users monitor their daily weight and progress toward personal health goals. The app focuses on essential features without overwhelming complexity—secure login, daily weight entry, historical data display, goal setting, and achievement notifications.

### Target Users

- **Weight-Loss Seekers** - Individuals actively working to reduce weight
- **Health Maintenance Monitors** - Users maintaining current weight or tracking for medical purposes  
- **Family Health Managers** - Parents/caregivers tracking weight for multiple family members

---

## ✨ Features

### Core Functionality

| Feature | Description |
|---------|-------------|
| 🔐 **User Authentication** | Secure login and registration with encrypted credentials |
| ⚖️ **Daily Weight Logging** | Quick entry with date picker and unit toggle (lbs/kg) |
| 📊 **Weight History** | Chronological display with trend indicators |
| 🎯 **Goal Setting** | Set and track progress toward target weight |
| 📱 **SMS Notifications** | Text message alerts for goals, milestones, and daily reminders |
| 🔔 **Smart Notifications** | Push notification alerts when you reach your goal |
| ♿ **Accessibility** | Built-in accessibility settings on every screen |

### UI Highlights

- **Progress Dashboard** - Visual progress bar showing journey completion
- **Trend Badges** - Color-coded indicators (↓ green, ↑ red, — orange)
- **Quick Stats** - At-a-glance metrics (total lost, remaining, streak)
- **Custom Numpad** - Large touch targets for easy weight input
- **Material Design 3** - Modern, health-focused teal color scheme

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Android Studio** | Ladybug (2024.2.1+) | IDE |
| **Java** | 21 | Primary language |
| **Gradle** | 8.2+ | Build system |
| **Android SDK** | 34 (Android 14) | Target platform |
| **Min SDK** | 26 (Android 8.0) | Minimum supported |
| **SQLite** | Built-in | Local database |
| **Material Components** | 1.11.0+ | UI components |

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- [Android Studio](https://developer.android.com/studio) (Ladybug 2024.2.1 or newer)
- [Java Development Kit (JDK)](https://adoptium.net/) 21 or higher
- Android SDK 34 (installed via Android Studio SDK Manager)
- Git (for version control)

### Hardware Requirements

- **RAM:** 8 GB minimum (16 GB recommended)
- **Disk Space:** 8 GB for Android Studio + 4 GB for Android SDK
- **Screen Resolution:** 1280 x 800 minimum

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/rgoshen-snhu/WeighToGo.git
cd WeighToGo
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select **File → Open**
3. Navigate to the cloned `WeighToGo` directory
4. Click **OK** and wait for Gradle sync to complete

### 3. Configure SDK (if needed)

1. Go to **File → Project Structure → SDK Location**
2. Ensure Android SDK path is set correctly
3. Download Android 14 (API 34) if not installed:
   - **Tools → SDK Manager → SDK Platforms**
   - Check "Android 14.0 (UpsideDownCake)"
   - Click **Apply**

### 4. Build the Project

```bash
# Via command line
./gradlew build

# Or in Android Studio
# Build → Make Project (Ctrl+F9 / Cmd+F9)
```

### 5. Run the App

**On Emulator:**
1. **Tools → Device Manager → Create Device**
2. Select Pixel 7 (or similar)
3. Download system image for API 34
4. Click ▶️ Run (Shift+F10)

**On Physical Device:**
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect via USB
4. Select your device from the dropdown
5. Click ▶️ Run

---

## 📁 Project Structure

```
WeighToGo/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/rickgoshen/weightogo/
│   │   │   │   ├── activities/
│   │   │   │   │   ├── LoginActivity.java
│   │   │   │   │   ├── MainActivity.java
│   │   │   │   │   ├── WeightEntryActivity.java
│   │   │   │   │   └── SmsNotificationsActivity.java
│   │   │   │   ├── adapters/
│   │   │   │   │   └── WeightHistoryAdapter.java
│   │   │   │   ├── database/
│   │   │   │   │   ├── WeighToGoDBHelper.java
│   │   │   │   │   └── WeighToGoDAO.java
│   │   │   │   ├── models/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── WeightEntry.java
│   │   │   │   │   ├── GoalWeight.java
│   │   │   │   │   ├── Achievement.java
│   │   │   │   │   └── UserPreference.java
│   │   │   │   ├── utils/
│   │   │   │   │   ├── ValidationUtils.java
│   │   │   │   │   ├── NotificationHelper.java
│   │   │   │   │   ├── SmsNotificationUtils.java
│   │   │   │   │   ├── PasswordUtils.java
│   │   │   │   │   └── SessionManager.java
│   │   │   │   └── constants/
│   │   │   │       └── AppConstants.java
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_login.xml
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_weight_entry.xml
│   │   │   │   │   ├── activity_sms_notifications.xml
│   │   │   │   │   └── item_weight_history.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── styles.xml
│   │   │   │   │   └── dimens.xml
│   │   │   │   ├── drawable/
│   │   │   │   ├── mipmap-*/
│   │   │   │   └── menu/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   │   └── java/com/rickgoshen/weightogo/
│   │   └── androidTest/
│   │       └── java/com/rickgoshen/weightogo/
│   └── build.gradle
├── docs/
|   ├── adr/               # Architecture Decision Records
    │   └── 0001-*.md
    ├── ddr/               # Design Decision Records
    │   └── 0001-*.md
│   ├── architecture/
│   │   └── WeighToGo_Database_Architecture.md
│   ├── design/
│   │   ├── Weight_Tracker_Figma_Design_Specifications.md
│   │   └── Weight_Tracker_Figma_Quick_Start_Guide.md
│   ├── api/
│   │   └── (future API documentation)
│   └── user-guide/
│   │    └── (future user documentation)
│   ├── requirements/
│   │   ├── CS360_Project_Three_Requirments.md
│   │   ├── CS360_Project_Two_Requirments.md
│   │   ├── Weight_Tracker_App_Requirements_v1.md
│   │   └── Weight_Tracker_App_Requirements_v2.md
├── previews/
│   ├── weight_tracker_login.html
│   ├── weight_tracker_dashboard.html
│   ├── weight_tracker_weight_entry.html
│   └── weight_tracker_sms_notifications.html
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── README.md
├── LICENSE.md
├── CONTRIBUTING.md
└── .gitignore
```

### 📂 Directory Descriptions

| Directory | Purpose |
|-----------|---------|
| `app/` | Main Android application module |
| `app/src/main/java/` | Java source code organized by feature |
| `app/src/main/res/` | Android resources (layouts, values, drawables) |
| `app/src/test/` | Unit tests (JUnit) |
| `app/src/androidTest/` | Instrumented tests (Espresso) |
| `docs/` | Project documentation |
| `docs/architecture/` | Database schema and system architecture |
| `docs/design/` | UI/UX design specifications and Figma guides |
| `docs/api/` | API documentation (future) |
| `docs/user-guide/` | End-user documentation (future) |
| `previews/` | Interactive HTML mockups for UI screens |
| `gradle/` | Gradle wrapper files |

---

## 🗄️ Database Schema

The app uses SQLite with five normalized tables. For complete documentation including SQL scripts, Java implementations, and DAO patterns, see [`docs/architecture/WeighToGo_Database_Architecture.md`](./docs/architecture/WeighToGo_Database_Architecture.md).

### `users`
| Column | Type | Constraints |
|--------|------|-------------|
| `user_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| `username` | TEXT | NOT NULL, UNIQUE |
| `email` | TEXT | UNIQUE |
| `phone_number` | TEXT | E.164 format for SMS |
| `password_hash` | TEXT | NOT NULL (SHA-256) |
| `salt` | TEXT | NOT NULL |
| `display_name` | TEXT | |
| `created_at` | TEXT | DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | TEXT | DEFAULT CURRENT_TIMESTAMP |
| `is_active` | INTEGER | DEFAULT 1 |
| `last_login` | TEXT | |

### `daily_weights`
| Column | Type | Constraints |
|--------|------|-------------|
| `weight_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| `user_id` | INTEGER | FOREIGN KEY → users |
| `weight_value` | REAL | NOT NULL |
| `weight_unit` | TEXT | DEFAULT 'lbs' |
| `weight_date` | TEXT | NOT NULL |
| `notes` | TEXT | |
| `created_at` | TEXT | DEFAULT CURRENT_TIMESTAMP |
| `is_deleted` | INTEGER | DEFAULT 0 |

### `goal_weights`
| Column | Type | Constraints |
|--------|------|-------------|
| `goal_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| `user_id` | INTEGER | FOREIGN KEY → users |
| `goal_weight` | REAL | NOT NULL |
| `goal_unit` | TEXT | DEFAULT 'lbs' |
| `start_weight` | REAL | |
| `target_date` | TEXT | |
| `is_achieved` | INTEGER | DEFAULT 0 |
| `achieved_date` | TEXT | |
| `created_at` | TEXT | DEFAULT CURRENT_TIMESTAMP |
| `is_active` | INTEGER | DEFAULT 1 |

### `achievements`
| Column | Type | Constraints |
|--------|------|-------------|
| `achievement_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| `user_id` | INTEGER | FOREIGN KEY → users |
| `goal_id` | INTEGER | FOREIGN KEY → goal_weights |
| `type` | TEXT | NOT NULL |
| `title` | TEXT | NOT NULL |
| `description` | TEXT | |
| `achieved_at` | TEXT | DEFAULT CURRENT_TIMESTAMP |
| `is_notified` | INTEGER | DEFAULT 0 |

### `user_preferences`
| Column | Type | Constraints |
|--------|------|-------------|
| `preference_id` | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| `user_id` | INTEGER | FOREIGN KEY → users |
| `pref_key` | TEXT | NOT NULL |
| `pref_value` | TEXT | |
| `created_at` | TEXT | DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | TEXT | DEFAULT CURRENT_TIMESTAMP |

**Preference Keys:** `weight_unit`, `theme`, `notifications_enabled`, `sms_notifications_enabled`, `sms_goal_alerts`, `sms_milestone_alerts`, `sms_reminder_enabled`, `reminder_time`

---

## 📚 Documentation

Comprehensive project documentation is available in the [`docs/`](docs/) folder:

### Architecture
| Document | Description |
|----------|-------------|
| [Database Architecture](./docs/architecture/WeighToGo_Database_Architecture.md) | Complete SQLite schema, ER diagrams, SQL scripts, Java DAOs, and best practices |

### Design
| Document | Description |
|----------|-------------|
| [Figma Design Specifications](./docs/Weight_Tracker_Figma_Design_Specifications.md) | Complete UI specifications with colors, typography, spacing, and component details |
| [Figma Quick Start Guide](./docs/Weight_Tracker_Figma_Quick_Start_Guide.md) | Step-by-step guide for building UI screens in Figma |

> **Note:** The Project Structure section shows the recommended folder organization with subfolders (`architecture/`, `design/`, `api/`, `user-guide/`) for future scalability.

---

## 🎨 Design System

### Color Palette

| Name | Hex | Usage |
|------|-----|-------|
| Primary Teal | `#00897B` | Primary actions, headers |
| Primary Dark | `#00695C` | Gradients, pressed states |
| Accent Green | `#4CAF50` | Success, positive trends |
| Warning Orange | `#FF9800` | Neutral states |
| Error Red | `#F44336` | Errors, negative trends |

### Typography

- **Headlines:** Poppins (Bold, SemiBold)
- **Body:** Source Sans Pro (Regular)

### Spacing

- Based on 8px grid system
- Touch targets: Minimum 48dp (Android requirement)

---

## 🧪 Testing

### Run Unit Tests

```bash
./gradlew test
```

### Run Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

### Test Coverage

```bash
./gradlew jacocoTestReport
```

---

## 📦 Building for Release

### Generate Signed APK

1. **Build → Generate Signed Bundle / APK**
2. Select **APK**
3. Create or select keystore
4. Choose **release** build variant
5. Click **Finish**

### Build via Command Line

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing config)
./gradlew assembleRelease
```

Output location: `app/build/outputs/apk/`

---

## 🔒 Permissions

The app requires the following permissions:

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SEND_SMS" />
```

| Permission | Purpose | Required |
|------------|---------|----------|
| `POST_NOTIFICATIONS` | Goal achievement and reminder alerts | Optional |
| `SEND_SMS` | SMS notifications for goals, milestones, and reminders | Optional |

> **Note:** Both permissions require explicit user consent at runtime (Android 6.0+). Users can use the app without granting these permissions, but notification features will be disabled.

---

## 🗺️ Roadmap

### Version 1.0 (Current)
- [x] User authentication
- [x] Daily weight logging
- [x] Weight history display
- [x] Goal weight setting and tracking
- [x] Goal achievement notifications
- [x] SMS notifications for goals, milestones, and reminders
- [x] Global weight unit preferences (lbs/kg)

> **Note:** Bottom navigation includes disabled "Trends" and "Profile" buttons (greyed out). These features are planned for post-launch (see Version 2.0 below).

### Version 1.1 (Planned)
- [ ] Export weight data to CSV
- [ ] Dark mode support
- [ ] Data backup/restore functionality
- [ ] Enhanced SMS reminder scheduling

### Version 2.0 (Future)
- [ ] **Trends Screen** - Interactive charts, progress visualization, and analytics (see [TODO.md Phase 11](TODO.md))
- [ ] **Profile Management** - User settings, personal info, account management (see [TODO.md Phase 12](TODO.md))
- [ ] Cloud sync across devices
- [ ] BMI calculator and health metrics
- [ ] Wear OS companion app
- [ ] Multiple user profiles per device

---

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on:

- Code style
- Commit messages
- Pull request process
- Issue reporting

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

---

## 👨‍💻 Author

**Rick Goshen**

- Course: CS 360 - Mobile Architecture & Programming
- Institution: Southern New Hampshire University
- Term: November 2025

---

## 🙏 Acknowledgments

- [Material Design](https://material.io/) - Design guidelines
- [Android Developers](https://developer.android.com/) - Documentation
- [Google Fonts](https://fonts.google.com/) - Poppins & Source Sans Pro
- Southern New Hampshire University - CS 360 course materials

---

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/rgoshen-snhu/WeighToGo/issues) page
2. Review the [Wiki](https://github.com/rgoshen-snhu/WeighToGo/wiki) (if available)
3. Create a new issue with detailed information

---

<p align="center">
  <strong>Weigh to Go!</strong> — You've got this, pound for pound. 🎉
</p>
