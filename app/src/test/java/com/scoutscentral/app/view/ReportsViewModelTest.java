package com.scoutscentral.app.view;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.scoutscentral.app.model.data.DataAccsesLayer;
import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.ScoutLevel;
import com.scoutscentral.app.view_model.ReportsViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ReportsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DataAccsesLayer mockRepository;
    private ReportsViewModel viewModel;

    @Before
    public void setUp() {
        mockRepository = mock(DataAccsesLayer.class);
        viewModel = new ReportsViewModel(mockRepository);
    }

    @Test
    public void testGenerateSummary_Success() throws IOException, InterruptedException {
        // Arrange
        Scout scout = new Scout("1", "Test Scout", "", ScoutLevel.KEFIR, "", "", "", new ArrayList<>());
        String mockJson = "[{\"activities\": {\"title\": \"Camping\", \"date\": \"2024-12-25T10:00:00Z\"}}]";
        when(mockRepository.fetchScoutActivityHistory(anyString(), anyString(), anyString())).thenReturn(mockJson);

        CountDownLatch latch = new CountDownLatch(1);
        Observer<String> observer = s -> {
            if (s != null && s.contains("Camping")) {
                latch.countDown();
            }
        };
        
        viewModel.getSummary().observeForever(observer);

        // Act
        viewModel.generateSummary(scout, "01/01/2024", "31/12/2024");

        // Assert
        assertTrue("Test timed out", latch.await(5, TimeUnit.SECONDS));
        String result = viewModel.getSummary().getValue();
        assertNotNull(result);
        assertTrue(result.contains("Test Scout"));
        assertTrue(result.contains("Camping"));

        viewModel.getSummary().removeObserver(observer);
    }

    @Test
    public void testGenerateSummary_NoActivities() throws IOException, InterruptedException {
        // Arrange
        Scout scout = new Scout("1", "Test Scout", "", ScoutLevel.KEFIR, "", "", "", new ArrayList<>());
        when(mockRepository.fetchScoutActivityHistory(anyString(), anyString(), anyString())).thenReturn("[]");

        CountDownLatch latch = new CountDownLatch(1);
        Observer<String> observer = s -> {
            if (s != null && s.contains("לא נמצאו פעילויות")) {
                latch.countDown();
            }
        };
        viewModel.getSummary().observeForever(observer);

        // Act
        viewModel.generateSummary(scout, "01/01/2024", "31/12/2024");

        // Assert
        assertTrue("Test timed out", latch.await(5, TimeUnit.SECONDS));
        assertTrue(viewModel.getSummary().getValue().contains("לא נמצאו פעילויות"));

        viewModel.getSummary().removeObserver(observer);
    }
}
