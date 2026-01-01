package com.scoutscentral.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Looper;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.scoutscentral.app.model.data.SupabaseService;
import com.scoutscentral.app.view.LoginActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {28})
public class LoginActivityIntegrationTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    private SupabaseService mockService;

    @Before
    public void setUp() throws IOException {
        mockService = mock(SupabaseService.class);
        // Ensure configuration is "valid" for the test
        when(mockService.isConfigured()).thenReturn(true);
        
        // Setup the mock on the activity instance
        activityRule.getScenario().onActivity(activity -> {
            activity.setSupabaseService(mockService);
        });
    }

    @Test
    public void loginLayout_isDisplayed() {
        onView(withId(R.id.login_email)).check(matches(isDisplayed()));
        onView(withId(R.id.login_password)).check(matches(isDisplayed()));
        onView(withId(R.id.login_submit)).check(matches(isDisplayed()));
    }

    @Test
    public void loginWithEmptyCredentials_showsError() {
        onView(withId(R.id.login_submit)).perform(click());
        onView(withText("אנא מלאו אימייל וסיסמה")).check(matches(isDisplayed()));
    }

    @Test
    public void loginWithInvalidCredentials_showsAuthError() throws IOException {
        // Arrange: Make the mock service return null (invalid login)
        when(mockService.authenticateInstructor(anyString(), anyString())).thenReturn(null);

        // Act: Enter credentials and click submit
        onView(withId(R.id.login_email)).perform(typeText("wrong@scouts.com"), closeSoftKeyboard());
        onView(withId(R.id.login_password)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.login_submit)).perform(click());

        // Assert: Verify the error snackbar appears
        // We use Robolectric's shadow looper to ensure background tasks and runOnUiThread callbacks execute
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        
        onView(withText("פרטי התחברות שגויים")).check(matches(isDisplayed()));
    }
}
