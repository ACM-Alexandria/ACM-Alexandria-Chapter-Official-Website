package com.acm.acmwebsite.service.past;

import com.acm.acmwebsite.feature.dto.past.*;
import com.acm.acmwebsite.feature.entity.past.*;
import com.acm.acmwebsite.feature.repository.past.*;
import com.acm.acmwebsite.feature.service.past.*;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.PastBoardMapper;
import com.acm.acmwebsite.feature.service.SystemSettingsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.context.ApplicationEventPublisher;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PastHighBoardServiceTest {
    private final PastHighBoardRepository repository = mock(PastHighBoardRepository.class);
    private final SystemSettingsService settings = mock(SystemSettingsService.class);
    private final PastSeasonPolicy policy = spy(new PastSeasonPolicy(settings));
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final PastBoardMapper mapper = Mappers.getMapper(PastBoardMapper.class);
    private PastHighBoardService service;
    private PastHighBoardRequest request;

    @BeforeEach
    void setUp() {
        when(settings.getCurrentSeason()).thenReturn(2026);
        service = new PastHighBoardService(repository, mapper, policy, events);
        request = new PastHighBoardRequest();
        request.setSeason(2025);
        request.setName("Snapshot name");
        request.setRole("Chair");
        request.setDisplayOrder(2);
        request.setImageUrl("https://example.org/photo");
    }

    @Test
    void createsSnapshotAndPublishesChange() {
        when(repository.save(any())).thenAnswer(call -> {
            PastHighBoard entity = call.getArgument(0);
            entity.setId(9L);
            return entity;
        });
        var response = service.create(request);
        assertEquals(9L, response.getId());
        assertEquals(2025, response.getSeason());
        assertEquals("Snapshot name", response.getUserName());
        assertEquals("Chair", response.getRole());
        assertEquals(2, response.getOrder());
        assertEquals("https://example.org/photo", response.getProfileImageUrl());
        verify(policy).validate(2025);
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void listsThroughTheOrderedFinderAndMapsInsideTheService() {
        var entity = new PastHighBoard();
        entity.setSeason(2025);
        entity.setName("Stored name");
        when(repository.findBySeasonOrdered(2025)).thenReturn(List.of(entity));
        var result = service.list(2025);
        assertEquals("Stored name", result.getFirst().getUserName());
        verify(policy).validate(2025);
        verifyNoInteractions(events);
    }

    @Test
    void updatesExistingRecordAndAllowsOptionalFieldsToBeCleared() {
        var entity = new PastHighBoard();
        entity.setId(9L);
        entity.setSeason(2025);
        entity.setImageUrl("https://example.org/old");
        request.setImageUrl(null);
        when(repository.findById(9L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        var result = service.update(9L, request);
        assertEquals(2025, result.getSeason());
        assertNull(result.getProfileImageUrl());
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void rejectsChangedSeasonBeforeSaving() {
        var entity = new PastHighBoard();
        entity.setSeason(2024);
        when(repository.findById(9L)).thenReturn(Optional.of(entity));
        assertThrows(IllegalArgumentException.class, () -> service.update(9L, request));
        verify(repository, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void missingUpdateAndDeleteReturnNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.update(9L, request));
        assertThrows(ResourceNotFoundException.class, () -> service.delete(9L));
        verifyNoInteractions(events);
    }

    @Test
    void deleteValidatesSeasonAndPublishesChange() {
        var entity = new PastHighBoard();
        entity.setSeason(2025);
        when(repository.findById(9L)).thenReturn(Optional.of(entity));
        service.delete(9L);
        verify(policy).validate(2025);
        verify(repository).delete(entity);
        verify(events).publishEvent(any(PastBoardsChangedEvent.class));
    }

    @Test
    void invalidSeasonCannotBeCreated() {
        doThrow(new IllegalArgumentException("Invalid season")).when(policy).validate(2025);
        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verifyNoInteractions(repository, events);
    }
}
