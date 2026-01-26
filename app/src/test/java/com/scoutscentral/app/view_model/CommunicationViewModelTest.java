package com.scoutscentral.app.view_model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.core.content.IntentCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.scoutscentral.app.model.Scout;
import com.scoutscentral.app.model.ScoutLevel;
import com.scoutscentral.app.model.data.DataAccessLayer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {28})
public class CommunicationViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private CommunicationViewModel viewModel;
    private DataAccessLayer mockedDataRepository;
    private Context mockContext;

    @Before
    public void setUp() {
        mockedDataRepository = mock(DataAccessLayer.class);
        mockContext = mock(Context.class);
        viewModel = new CommunicationViewModel(mockedDataRepository);
    }

    @Test
    public void testSendEmailToScouts_triggersCorrectIntent() {
        // Arrange
        String email1 = "amielnoy@gmail.com";
        String email2 = "amielnoy@gmail.com";
        
        // Fix: Use emptyList() and empty strings instead of nulls to satisfy Kotlin's non-null requirements
        List<Scout> scouts = Arrays.asList(
            new Scout("1", "Scout A", "", ScoutLevel.KEFIR, email1, "", "", Collections.emptyList()),
            new Scout("2", "Scout B", "", ScoutLevel.KEFIR, email2, "", "", Collections.emptyList())
        );
        MutableLiveData<List<Scout>> liveData = new MutableLiveData<>(scouts);
        when(mockedDataRepository.getScouts()).thenReturn(liveData);

        // Act
        viewModel.sendEmailToScouts(mockContext, "Subject", "Body Text");

        // Assert
        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(mockContext).startActivity(intentCaptor.capture());

        Intent actualIntent = intentCaptor.getValue();
        // The Intent created by ViewModel is a chooser
        assertEquals(Intent.ACTION_CHOOSER, actualIntent.getAction());
        
        // Use IntentCompat to avoid deprecation warning for getParcelableExtra
        Intent targetIntent = IntentCompat.getParcelableExtra(actualIntent, Intent.EXTRA_INTENT, Intent.class);
        
        assertEquals(Intent.ACTION_SENDTO, targetIntent.getAction());
        assertEquals("mailto:", targetIntent.getData().toString());
        
        String[] emails = targetIntent.getStringArrayExtra(Intent.EXTRA_EMAIL);
        assertEquals(2, emails.length);
        assertEquals(email1, emails[0]);
        assertEquals(email2, emails[1]);
        
        assertEquals("Subject", targetIntent.getStringExtra(Intent.EXTRA_SUBJECT));
        assertEquals("Body Text", targetIntent.getStringExtra(Intent.EXTRA_TEXT));
    }
}
