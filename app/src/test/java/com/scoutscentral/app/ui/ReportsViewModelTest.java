package com.scoutscentral.app.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.scoutscentral.app.data.DataRepository;
import com.scoutscentral.app.data.Scout;
import com.scoutscentral.app.data.ScoutLevel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ReportsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ReportsViewModel viewModel;
    private DataRepository mockRepository;
    private MockedStatic<DataRepository> mockedStaticRepo;

    @Before
    public void setUp() {
        mockRepository = mock(DataRepository.class);
        mockedStaticRepo = mockStatic(DataRepository.class);
        mockedStaticRepo.when(DataRepository::getInstance).thenReturn(mockRepository);
        viewModel = new ReportsViewModel();
    }

    @After
    public void tearDown() {
        mockedStaticRepo.close();
    }

    @Test
    public void testGenerateSummary_Success() throws IOException, InterruptedException {
        // Arrange
        Scout scout = new Scout("1", "Test Scout", "", ScoutLevel.KEFIR, "", "", "", new ArrayList<>());
        String mockJson = "[{\"activities\": {\"title\": \"Camping\", \"date\": \"2024-12-25T10:00:00Z\"}}]";
        when(mockRepository.fetchScoutActivityHistory(anyString(), anyString(), anyString())).thenReturn(mockJson);

        CountDownLatch latch = new CountDownLatch(1);
        Observer<String> observer = s -> {
            if (s.contains("Camping")) {
                latch.countDown();
            }
        };
        viewModel.getSummary().observeForever(observer);

        // Act
        viewModel.generateSummary(scout, "01/01/2024", "31/12/2024");

        // Assert
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        String result = viewModel.getSummary().getValue();
        assertNotNull(result);
        assertTrue(result.contains("סיכום השתתפות עבור Test Scout"));
        assertTrue(result.contains("Camping"));
        assertTrue(result.contains("סך הכל פעילויות: 1"));

        viewModel.getSummary().removeObserver(observer);
    }

    @Test
    public void testGenerateSummary_NoActivities() throws IOException, InterruptedException {
        // Arrange
        Scout scout = new Scout("1", "Test Scout", "", ScoutLevel.KEFIR, "", "", "", new ArrayList<>());
        when(mockRepository.fetchScoutActivityHistory(anyString(), anyString(), anyString())).thenReturn("[]");

        CountDownLatch latch = new CountDownLatch(1);
        Observer<String> observer = s -> {
            if (s.contains("לא נמצאו פעילויות")) {
                latch.countDown();
            }
        };
        viewModel.getSummary().observeForever(observer);

        // Act
        viewModel.generateSummary(scout, "01/01/2024", "31/12/2024");

        // Assert
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertTrue(viewModel.getSummary().getValue().contains("לא נמצאו פעילויות"));

        viewModel.getSummary().removeObserver(observer);
    }
}
