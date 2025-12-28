package com.scoutscentral.app.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AuthStoreTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        AuthStore.clear(context);
    }

    @Test
    public void testSaveInstructor_persistsData() {
        AuthStore.saveInstructor(context, "inst-123", "John Doe");
        
        assertTrue(AuthStore.isLoggedIn(context));
        assertEquals("John Doe", AuthStore.getInstructorName(context));
    }

    @Test
    public void testClear_removesData() {
        AuthStore.saveInstructor(context, "inst-123", "John Doe");
        AuthStore.clear(context);
        
        assertFalse(AuthStore.isLoggedIn(context));
        assertEquals("", AuthStore.getInstructorName(context));
    }
}
