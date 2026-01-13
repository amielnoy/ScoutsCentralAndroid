package com.scoutscentral.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.scoutscentral.app.model.data.SupabaseService;
import com.scoutscentral.app.view.LoginActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {28})
public class LoginActivityIntegrationTest {

    private SupabaseService mockService;
    private ActivityScenario<LoginActivity> scenario;

    @Before
    public void setUp() throws IOException {
        mockService = mock(SupabaseService.class);
        when(mockService.isConfigured()).thenReturn(true);
        
        // Ensure static hooks are ready before launch
        LoginActivity.testSupabaseService = mockService;
        LoginActivity.testExecutor = Runnable::run;
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        LoginActivity.testSupabaseService = null;
        LoginActivity.testExecutor = null;
    }

    @Test
    public void loginLayout_isDisplayed() {
        scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.login_email)).check(matches(isDisplayed()));
        onView(withId(R.id.login_password)).check(matches(isDisplayed()));
        onView(withId(R.id.login_submit)).check(matches(isDisplayed()));
    }

    @Test
    public void loginWithEmptyCredentials_showsError() {
        scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.login_submit)).perform(click());
        
        // Process UI tasks to show Snackbar
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        ShadowLooper.idleMainLooper();
        
        // Check for text appearing in the hierarchy
        onView(withText("אנא מלאו אימייל וסיסמה")).check(matches(isDisplayed()));
    }

    @Test
    public void loginWithInvalidCredentials_showsAuthError() throws IOException {
        // Arrange
        when(mockService.authenticateInstructor(anyString(), anyString())).thenReturn(null);

        // Act
        scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.login_email)).perform(typeText("wrong@scouts.com"), closeSoftKeyboard());
        onView(withId(R.id.login_password)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.login_submit)).perform(click());

        // Process all main thread tasks
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        ShadowLooper.idleMainLooper();
        
        // Assert: Verify the error message is displayed
        onView(withText("פרטי התחברות שגויים")).check(matches(isDisplayed()));
        
        // Assert: Verify progress bar is hidden
        onView(withId(R.id.login_progress)).check(matches(not(isDisplayed())));
    }
}
