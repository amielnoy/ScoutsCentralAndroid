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

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {28}) // Use a stable SDK for Robolectric
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
    public void loginWithEmptyCredentials_showsError() {
        onView(withId(R.id.login_submit)).perform(click());
        // Robolectric handles Snackbars/Toasts in the same view hierarchy
        onView(withText("אנא מלאו אימייל וסיסמה")).check(matches(isDisplayed()));
    }

    @Test
    public void loginWithInvalidCredentials_showsAuthError() {
        onView(withId(R.id.login_email)).perform(typeText("wrong@scouts.com"), closeSoftKeyboard());
        onView(withId(R.id.login_password)).perform(typeText("123456"), closeSoftKeyboard());

        onView(withId(R.id.login_submit)).perform(click());

        // In Robolectric, we don't need Thread.sleep for UI tasks, 
        // but we verify the immediate UI response.
        onView(withText("פרטי התחברות שגויים")).check(matches(isDisplayed()));
    }
}
