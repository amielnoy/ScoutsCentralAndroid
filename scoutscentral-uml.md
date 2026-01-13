
    %% =======================
    %% Package: Model
    %% =======================
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
        ARIE
    }

    %% =======================
    %% Package: Data Layer (DAL / Repository)
    %% =======================
    class DataAccessLayer {
        <<Singleton>>
        -MutableLiveData~List~Scout~~ scouts
        -MutableLiveData~List~Activity~~ activities
        -SupabaseService supabaseService
        +getInstance() DataAccessLayer
        +getScouts() LiveData~List~Scout~~
        +getActivities() LiveData~List~Activity~~
        +addScout(String name, ScoutLevel level, String contact, String avatarUrl)
        +updateActivity(Activity activity)
        +saveAttendance(String activityId, List~String~ scoutIds)
    }

    class SupabaseService {
        +fetchScouts() List~Scout~
        +upsertActivity(Activity activity)
        +authenticateInstructor(String email, String password) boolean
    }

    class GeminiService {
        +generateProgressPlan(Scout scout, String interests, String skills) String
        -callGeminiApi(String version, String model, String prompt) String
    }

    class AuthStore {
        +saveInstructor(Context context, String id, String name)
        +isLoggedIn(Context context) boolean
        +getInstructorName(Context context) String
        +clear(Context context)
    }

    %% =======================
    %% Package: ViewModels
    %% =======================
    class DashboardViewModel {
        -DataAccessLayer repository
        +getScouts() LiveData~List~Scout~~
        +getActivities() LiveData~List~Activity~~
        +refresh()
    }

    class ProgressViewModel {
        -GeminiService geminiService
        -DataAccessLayer repository
        +generatePlan(Scout scout, String interests, String skills)
        +getGeneratedPlan() LiveData~String~
    }

    class MembersViewModel {
        -DataAccessLayer repository
        +addScout(String name, ScoutLevel level, String contact, String avatarUrl)
        +updateScout(Scout scout)
        +removeScout(String id)
    }

    class ActivitiesViewModel {
        -DataAccessLayer repository
        +addActivity(String title, String date, String location, String desc)
        +updateActivity(Activity activity)
        +deleteActivity(String id)
    }

    class LoginViewModel {
        -SupabaseService supabaseService
        +login(String email, String password) boolean
    }

    %% =======================
    %% Package: Views (Activities & Fragments)
    %% =======================
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
        +onAvatarClick(Scout scout)
        +showMemberDialog(Scout scout)
    }

    class ActivitiesFragment {
        -ActivitiesViewModel viewModel
        -ActivityCardAdapter adapter
        +onImageClick(Activity activity)
    }

    %% =======================
    %% UI Adapters
    %% =======================
    class MemberAdapter
    class ActivityCardAdapter
    class ActivityRowAdapter

    %% Placeholder for Android type references
    class Context

    %% =======================
    %% Relationships
    %% =======================
    Scout --> ScoutLevel

    DataAccessLayer o-- SupabaseService
    DataAccessLayer o-- Scout
    DataAccessLayer o-- Activity

    DashboardViewModel --> DataAccessLayer
    MembersViewModel --> DataAccessLayer
    ActivitiesViewModel --> DataAccessLayer
    ProgressViewModel --> GeminiService
    ProgressViewModel --> DataAccessLayer

    LoginViewModel --> SupabaseService

    DashboardFragment ..> DashboardViewModel : observes
    MembersFragment ..> MembersViewModel : observes
    ActivitiesFragment ..> ActivitiesViewModel : observes

    LoginActivity ..> AuthStore : uses
    MainActivity ..> AuthStore : uses

    MembersFragment --> MemberAdapter : uses
    ActivitiesFragment --> ActivityCardAdapter : uses
    DashboardFragment --> ActivityRowAdapter : uses
