# ScoutsCentral (צופים+) ⚜️
**The ultimate management tool for Scout tribe instructors.**

ScoutsCentral is a robust Android application designed to streamline the day-to-day operations of a Scout tribe. From managing member profiles and tracking attendance to communicating across multiple channels and generating AI-powered progress plans, the app brings modern technology to the world of scouting.

---

## 🚀 Key Features

### 📊 Comprehensive Dashboard
- **Statistics at a Glance**: View real-time tribe metrics (Total Scouts, Active Activities, Badges earned) in a clean 2x2 grid.
- **Activity Feed**: Stay updated with the latest tribe announcements and upcoming events.

### 👤 Member Management
- **Profile Control**: Add, edit, and manage scout profiles, including their level (Kefir, Ofer, etc.) and contact info.
- **Image Integration**: Capture profile pictures directly using the **Camera** or select them from the **Gallery**.
- **Progress Tracking**: Monitor badge history and attendance milestones.

### 📅 Activity Coordination
- **Event Scheduling**: Create new activities with dates, locations, and material lists.
- **Digital Attendance**: Track attendance for each activity using an intuitive multi-choice interface that syncs instantly to the cloud.
- **Visualization**: Customize activities with unique images uploaded from the device.

### 💬 Multi-Channel Communication
- **Smart Announcements**: Post messages to the app's internal feed.
- **External Integration**: One-click sharing to **WhatsApp** (targeted at tribe groups like "צופי מוצקין") or **Email**.
- **Modern Interface**: Includes character counters, input validation, and channel selection control.

### 🤖 AI-Powered Progress Plans
- **Personalized Growth**: Uses **Google Gemini AI** (Gemma-3-12b / Gemini 2.0 Flash) to analyze a scout's interests and skills to generate professional progress plans in Hebrew.
- **Fallback Logic**: Robust multi-model support ensuring reliability even during API rate limits.

### 📈 Detailed Reports
- **Graphical Data**: Visual representation of attendance and participation using **MPAndroidChart**.
- **Date Filtering**: Generate custom summaries for specific time ranges.

---

## 🛠 Technology Stack

- **Architecture**: MVVM (Model-View-ViewModel) for clean separation of concerns.
- **Language**: Java / Android SDK.
- **Backend**: **Supabase** (Cloud Database & Authentication).
- **AI Engine**: Google Generative AI (Gemini API).
- **Networking**: OkHttp 5 & Gson.
- **Image Loading**: Glide 5.
- **UI Components**:
    - Material Components (M3)
    - ViewBinding
    - ConstraintLayout (Optimized layouts)
    - ViewPager2 & TabLayout
- **Testing**: JUnit 4, Mockito, Robolectric (Unit/Integration), and Espresso (UI).
- **CI/CD**: Automated workflow in GitHub Actions.

---

## ⚙️ Setup & Installation

To run this project locally, you must configure your API keys:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/scoutscentral-android.git
   ```

2. **Environment Setup**:
   Create a `local.properties` file in the root directory and add the following:
   ```properties
   SUPABASE_URL=https://your-project-id.supabase.co
   SUPABASE_ANON_KEY=your-supabase-anon-key
   GEMINI_API_KEY=your-google-gemini-api-key
   ```

3. **Build the Project**:
   Open in Android Studio and click **Sync Project with Gradle Files**.

---

## 🧪 Testing

The project includes a comprehensive testing suite to ensure stability:

- **Run all tests via CLI**:
  ```bash
  ./gradlew test
  ```
- **Efficient Build & Test**:
  To build the APK and run tests in parallel using the build cache:
  ```bash
  ./gradlew testDebugUnitTest assembleDebug --parallel --build-cache
  ```
- **UI Tests**: Located in `src/androidTest`.
- **Unit & Robolectric Tests**: Located in `src/test`.

---

## 📱 Screenshots

| Login | Dashboard | Communication |
| :---: | :---: | :---: |
| ![Login](https://via.placeholder.com/200x400?text=Login+Screen) | ![Dashboard](https://via.placeholder.com/200x400?text=Dashboard) | ![Comm](https://via.placeholder.com/200x400?text=Communication) |

---

## 📄 License
This project was developed as a final assignment for a 5-unit Computer Science matriculation (Bagrut). All rights reserved.

**Developed by Roni Rivkin**
