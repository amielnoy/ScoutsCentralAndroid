package com.scoutscentral.app.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.ScoutLevel;
import com.scoutscentral.app.model.data.DataAccsesLayer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class DataAccsesLayerTest {

    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    private DataAccsesLayer repository;

    @Before
    public void setUp() {
        repository = DataAccsesLayer.getInstance();
        repository.clearLocalData();
        repository.seedData();
    }

    @Test
    public void testSeedData_ScoutsNotEmpty() {
        List<Scout> scouts = repository.getScouts().getValue();
        assertNotNull("Scouts list should not be null", scouts);
        assertFalse("Scouts list should not be empty after seeding", scouts.isEmpty());
    }

    @Test
    public void testSeedData_ActivitiesNotEmpty() {
        List<Activity> activities = repository.getActivities().getValue();
        assertNotNull("Activities list should not be null", activities);
        assertFalse("Activities list should not be empty after seeding", activities.isEmpty());
    }

    @Test
    public void testAddScout_increasesCount() {
        List<Scout> initialScouts = repository.getScouts().getValue();
        assertNotNull(initialScouts);
        int initialCount = initialScouts.size();
        
        String testAvatar = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        repository.addScout("Test Scout", ScoutLevel.KEFIR, "test@example.com", testAvatar);
        
        List<Scout> scouts = repository.getScouts().getValue();
        assertNotNull(scouts);
        assertEquals(initialCount + 1, scouts.size());
        assertEquals("Test Scout", scouts.get(0).getName());
    }

    @Test
    public void testAddActivity_increasesCount() {
        List<Activity> initialActivities = repository.getActivities().getValue();
        assertNotNull(initialActivities);
        int initialCount = initialActivities.size();
        
        repository.addActivity("New Workshop", "2024-07-16T10:00:00Z", "Base", "Description");
        
        List<Activity> activities = repository.getActivities().getValue();
        assertNotNull(activities);
        assertEquals(initialCount + 1, activities.size());
        assertTrue(activities.stream().anyMatch(a -> a.getTitle().equals("New Workshop")));
    }

    @Test
    public void testRemoveScout_decreasesCount() {
        List<Scout> currentScouts = repository.getScouts().getValue();
        assertNotNull(currentScouts);
        assertFalse(currentScouts.isEmpty());
        
        String idToRemove = currentScouts.get(0).getId();
        int initialCount = currentScouts.size();
        
        repository.removeScout(idToRemove);
        
        List<Scout> remainingScouts = repository.getScouts().getValue();
        assertNotNull(remainingScouts);
        assertEquals(initialCount - 1, remainingScouts.size());
    }
}
