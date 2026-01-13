package com.scoutscentral.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.scoutscentral.app.view.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityIntegrationTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void loginLayout_isDisplayed() {
        onView(withId(R.id.login_email)).check(matches(isDisplayed()));
        onView(withId(R.id.login_password)).check(matches(isDisplayed()));
        onView(withId(R.id.login_submit)).check(matches(isDisplayed()));
    }

    @Test
    public void loginWithEmptyCredentials_showsError() throws InterruptedException {
        // Click login button without entering anything
        onView(withId(R.id.login_submit)).perform(click());

        // Wait a short moment for the Snackbar animation to complete
        Thread.sleep(500);

        // Correct text for empty credentials validation
        onView(withText("אנא מלאו אימייל וסיסמה")).check(matches(isDisplayed()));
    }

    @Test
    public void loginWithInvalidCredentials_showsAuthError() throws InterruptedException {
        // Enter dummy data
        onView(withId(R.id.login_email)).perform(typeText("wrong@scouts.com"), closeSoftKeyboard());
        onView(withId(R.id.login_password)).perform(typeText("123456"), closeSoftKeyboard());

        onView(withId(R.id.login_submit)).perform(click());

        // Wait for the simulated network response and animation
        Thread.sleep(2000); 

        // Correct text for authentication failure
        onView(withText("פרטי התחברות שגויים")).check(matches(isDisplayed()));
    }
}
