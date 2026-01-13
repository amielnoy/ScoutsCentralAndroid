# ScoutsCentral UML Class Diagram

This diagram represents the architectural structure of the ScoutsCentral Android application, highlighting the MVVM pattern, the data layer, and the integration with external services (Supabase and Gemini AI).

```mermaid
classDiagram
    %% Package: Model
    class Scout {
        +String id
        +String name
        +String avatarUrl
        +ScoutLevel level
        +String contact
        +String interests
        +String skills
        +List~String~ activityHistory
    }
    
    class Activity {
        +String id
        +String title
        +String date
        +String location
        +String description
        +String imageUrl
        +setImageUrl(String url)
    }
    
    class Announcement {
        +String id
        +String title
        +String message
        +String date
    }

    class ScoutLevel {
        <<enumeration>>
        KEFIR
        OFER
        NACHSHON
        A
    }

    %% Package: Data Layer (DAL / Repository)
    class DataAccsesLayer {
        <<Singleton>>
        -MutableLiveData~List~Scout~~ scouts
        -MutableLiveData~List~Activity~~ activities
        -SupabaseService supabaseService
        +getInstance() DataAccsesLayer
        +getScouts() LiveData
        +addScout(name, level, contact, avatar)
        +updateActivity(activity)
        +saveAttendance(activityId, scoutIds)
    }

    class SupabaseService {
        +fetchScouts() List~Scout~
        +upsertActivity(activity)
        +authenticateInstructor(email, password)
    }

    class GeminiService {
        +generateProgressPlan(scout, interests, skills) String
        -callGeminiApi(version, model, prompt)
    }

    class AuthStore {
        +saveInstructor(context, id, name)
        +isLoggedIn(context) boolean
        +getInstructorName(context) String
        +clear(context)
    }

    %% Package: ViewModels
    class DashboardViewModel {
        -DataAccsesLayer repository
        +getScouts() LiveData
        +getActivities() LiveData
        +refresh()
    }

    class ProgressViewModel {
        -GeminiService geminiService
        -DataAccsesLayer repository
        +generatePlan(scout, interests, skills)
        +getGeneratedPlan() LiveData
    }

    class MembersViewModel {
        -DataAccsesLayer repository
        +addScout(name, level, contact, avatar)
        +updateScout(scout)
        +removeScout(id)
    }

    class ActivitiesViewModel {
        -DataAccsesLayer repository
        +addActivity(title, date, location, desc)
        +updateActivity(activity)
        +deleteActivity(id)
    }

    %% Package: Views (Fragments & Activities)
    class MainActivity {
        +onCreate()
    }

    class LoginActivity {
        -LoginViewModel viewModel
        +testSupabaseService static
        +testExecutor static
    }

    class DashboardFragment {
        -DashboardViewModel viewModel
        -ActivityRowAdapter adapter
    }

    class MembersFragment {
        -MembersViewModel viewModel
        -MemberAdapter adapter
        +onAvatarClick(scout)
        +showMemberDialog(scout)
    }

    class ActivitiesFragment {
        -ActivitiesViewModel viewModel
        -ActivityCardAdapter adapter
        +onImageClick(activity)
    }

    %% Relationships
    Scout --> ScoutLevel
    DataAccsesLayer o-- SupabaseService
    DataAccsesLayer o-- Scout
    DataAccsesLayer o-- Activity
    
    DashboardViewModel --> DataAccsesLayer
    MembersViewModel --> DataAccsesLayer
    ActivitiesViewModel --> DataAccsesLayer
    ProgressViewModel --> GeminiService
    ProgressViewModel --> DataAccsesLayer
    
    DashboardFragment ..> DashboardViewModel : observes
    MembersFragment ..> MembersViewModel : observes
    ActivitiesFragment ..> ActivitiesViewModel : observes
    
    LoginActivity ..> AuthStore : uses
    MainActivity ..> AuthStore : uses
    
    MembersFragment --> MemberAdapter : uses
    ActivitiesFragment --> ActivityCardAdapter : uses
```

### Architectural Notes:
- **MVVM Pattern**: Enforces strict separation between UI (Fragments), Logic (ViewModels), and Data (Models).
- **Repository Pattern**: `DataAccsesLayer` centralizes data access, providing a clean API for ViewModels while hiding the complexity of Supabase integration.
- **Dependency Injection**: Static test hooks in `LoginActivity` enable high-quality integration testing via Robolectric.
- **Observer Pattern**: `LiveData` is used throughout to ensure the UI remains reactive to asynchronous data changes.
- **AI Integration**: `GeminiService` implements a robust multi-model fallback logic to provide high availability for AI-generated content.
