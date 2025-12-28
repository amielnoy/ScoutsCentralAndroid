package com.scoutscentral.app.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class AuthStoreTest {

    private Context mockContext;
    private SharedPreferences mockPrefs;
    private SharedPreferences.Editor mockEditor;
    private Map<String, String> preferenceMap;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        mockPrefs = mock(SharedPreferences.class);
        mockEditor = mock(SharedPreferences.Editor.class);
        preferenceMap = new HashMap<>();

        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);

        // Mock Editor behavior
        when(mockEditor.putString(anyString(), anyString())).thenAnswer(invocation -> {
            preferenceMap.put(invocation.getArgument(0), invocation.getArgument(1));
            return mockEditor;
        });
        when(mockEditor.clear()).thenAnswer(invocation -> {
            preferenceMap.clear();
            return mockEditor;
        });

        // Mock Prefs behavior
        when(mockPrefs.contains(anyString())).thenAnswer(invocation -> 
            preferenceMap.containsKey(invocation.getArgument(0)));
        when(mockPrefs.getString(anyString(), anyString())).thenAnswer(invocation -> 
            preferenceMap.getOrDefault(invocation.getArgument(0), invocation.getArgument(1)));
    }

    @Test
    public void testSaveInstructor_persistsData() {
        AuthStore.saveInstructor(mockContext, "inst-123", "John Doe");
        
        assertTrue(AuthStore.isLoggedIn(mockContext));
        assertEquals("John Doe", AuthStore.getInstructorName(mockContext));
    }

    @Test
    public void testClear_removesData() {
        AuthStore.saveInstructor(mockContext, "inst-123", "John Doe");
        AuthStore.clear(mockContext);
        
        assertFalse(AuthStore.isLoggedIn(mockContext));
        assertEquals("", AuthStore.getInstructorName(mockContext));
    }
}
