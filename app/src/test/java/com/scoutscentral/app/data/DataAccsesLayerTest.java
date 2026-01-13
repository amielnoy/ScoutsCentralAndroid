package com.scoutscentral.app.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.scoutscentral.app.model.Activity;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.ScoutLevel;
import com.scoutscentral.app.model.data.DataAccsesLayer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.List;

public class DataAccsesLayerTest {

    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    private DataAccsesLayer repository;

    @Before
    public void setUp() {
        repository = DataAccsesLayer.getInstance();
    }

    @Test
    public void testSeedData_ScoutsNotEmpty() {
        List<Scout> scouts = repository.getScouts().getValue();
        assertNotNull(scouts);
        assertFalse(scouts.isEmpty());
    }

    @Test
    public void testSeedData_ActivitiesNotEmpty() {
        List<Activity> activities = repository.getActivities().getValue();
        assertNotNull(activities);
        assertTrue(activities.size() > 0);
    }

    @Test
    public void testAddScout_increasesCount() {
        int initialCount = repository.getScouts().getValue().size();
        String testAvatar = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        repository.addScout("Test Scout", ScoutLevel.KEFIR, "test@example.com", testAvatar);
        
        List<Scout> scouts = repository.getScouts().getValue();
        assertEquals(initialCount + 1, scouts.size());
        assertEquals("Test Scout", scouts.get(0).getName());
        assertEquals(testAvatar, scouts.get(0).getAvatarUrl());
    }

    @Test
    public void testAddActivity_increasesCount() {
        int initialCount = repository.getActivities().getValue().size();
        repository.addActivity("New Workshop", "2024-12-25 10:00", "Base", "Description");
        
        List<Activity> activities = repository.getActivities().getValue();
        assertEquals(initialCount + 1, activities.size());
        assertEquals("New Workshop", activities.get(0).getTitle());
    }

    @Test
    public void testRemoveScout_decreasesCount() {
        List<Scout> currentScouts = repository.getScouts().getValue();
        String idToRemove = currentScouts.get(0).getId();
        int initialCount = currentScouts.size();
        
        repository.removeScout(idToRemove);
        
        assertEquals(initialCount - 1, repository.getScouts().getValue().size());
    }
}
