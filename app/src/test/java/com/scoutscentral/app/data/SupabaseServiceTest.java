package com.scoutscentral.app.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

public class SupabaseServiceTest {

    private SupabaseService service;

    @Before
    public void setUp() {
        service = new SupabaseService();
    }

    @Test
    public void testParseLevel_validInput() throws Exception {
        // Use reflection to test private/package-private method if needed, 
        // but here we just test that the logic works correctly.
        ScoutLevel level = invokeParseLevel("KEFIR");
        assertEquals(ScoutLevel.KEFIR, level);
        
        level = invokeParseLevel("NACHSHON");
        assertEquals(ScoutLevel.NACHSHON, level);
    }

    @Test
    public void testParseLevel_invalidInput_returnsDefault() throws Exception {
        ScoutLevel level = invokeParseLevel("INVALID_LEVEL");
        assertEquals(ScoutLevel.KEFIR, level);
        
        level = invokeParseLevel(null);
        assertEquals(ScoutLevel.KEFIR, level);
    }

    @Test
    public void testInstructorInnerClass_initialization() {
        SupabaseService.Instructor instructor = new SupabaseService.Instructor("123", "Jane Doe");
        assertEquals("123", instructor.id);
        assertEquals("Jane Doe", instructor.name);
    }

    // Helper to access the private parseLevel method for testing logic
    private ScoutLevel invokeParseLevel(String input) throws Exception {
        Method method = SupabaseService.class.getDeclaredMethod("parseLevel", String.class);
        method.setAccessible(true);
        return (ScoutLevel) method.invoke(service, input);
    }
}
