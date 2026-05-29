package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.EventCardDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.EventRegistration;
import com.acm.acmwebsite.feature.entity.EventFormQuestion;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.mapper.EventMapper;
import com.acm.acmwebsite.feature.repository.EventRepository;
import com.acm.acmwebsite.feature.repository.EventRegistrationRepository;
import com.acm.acmwebsite.feature.repository.EventFormQuestionRepository;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.exception.GoogleSheetsNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventFormQuestionRepository eventFormQuestionRepository;
    private final GoogleSheetsService googleSheetsService;

    @Value("${google.sheets.events-folder-id:}")
    private String eventsFolderId;

    public EventService(EventRepository eventRepository, EventMapper eventMapper,
                        EventRegistrationRepository eventRegistrationRepository,
                        EventFormQuestionRepository eventFormQuestionRepository,
                        GoogleSheetsService googleSheetsService) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.eventFormQuestionRepository = eventFormQuestionRepository;
        this.googleSheetsService = googleSheetsService;
    }

    public List<EventCardDto> getAllCards() {
        return eventRepository.findAll(Sort.by("eventTime").descending()).stream()
                .map(eventMapper::toEventCardDto)
                .toList();
    }

    public Optional<Event> getById(Long id) {
        return eventRepository.findById(id);
    }

    public Event createEvent(Event event) {
        if (event.getName() == null || event.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Event name is required");
        }
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, Event updatedEvent) {
        return eventRepository.findById(id).map(event -> {
            if (updatedEvent.getName() == null || updatedEvent.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Event name is required");
            }
            event.setName(updatedEvent.getName());
            event.setDescription(updatedEvent.getDescription());
            event.setImageUrl(updatedEvent.getImageUrl());
            event.setEventTime(updatedEvent.getEventTime());
            event.setLocation(updatedEvent.getLocation());
            return eventRepository.save(event);
        }).orElseThrow(() -> new RuntimeException("EVENT not found"));

    }

    public void deleteEvent(long id) {
        eventRepository.deleteById(id);
    }

    public Page<EventCardDto> getEventsByPage(int pageNumber) {
        pageNumber = Math.max(0, pageNumber);
        Pageable page = PageRequest.of(pageNumber, 6, Sort.by("eventTime").descending());
        return eventRepository.findAll(page).map(eventMapper::toEventCardDto);
    }

    public RegistrationAnalysisDto getRegistrationAnalysis(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + eventId));

        List<EventRegistration> registrations = eventRegistrationRepository.findByEventId(eventId);

        long alexCount = registrations.stream()
                .filter(r -> r.getUser() != null && Boolean.TRUE.equals(r.getUser().getIsAlexEngStudent()))
                .count();

        Map<String, Long> departments = registrations.stream()
                .filter(r -> r.getUser() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getUser().getDepartment() != null ? r.getUser().getDepartment().name() : "N/A",
                        Collectors.counting()
                ));

        Map<String, Long> batches = registrations.stream()
                .filter(r -> r.getUser() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getUser().getBatch() != null && !r.getUser().getBatch().trim().isEmpty() ? r.getUser().getBatch() : "N/A",
                        Collectors.counting()
                ));

        return RegistrationAnalysisDto.builder()
                .totalRegistrations(registrations.size())
                .alexUniStudentCount(alexCount)
                .nonAlexUniStudentCount(registrations.size() - alexCount)
                .departmentCounts(departments)
                .batchCounts(batches)
                .googleSheetUrl(event.getGoogleSheetUrl())
                .sheetLastUpdatedAt(event.getSheetLastUpdatedAt())
                .build();
    }

    @Transactional
    public RegistrationAnalysisDto syncRegistrationsSheet(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + eventId));

        List<EventRegistration> registrations = eventRegistrationRepository.findByEventId(eventId);
        List<EventFormQuestion> questions = eventFormQuestionRepository.findByEventId(eventId);

        List<List<Object>> rows = new ArrayList<>();
        // Row 1: Header/Title info
        rows.add(Arrays.asList("Event Name:", event.getName()));
        rows.add(Arrays.asList("Event Time:", event.getEventTime() != null ? event.getEventTime().toString() : "N/A"));
        rows.add(Collections.emptyList()); // empty row spacer

        // Row 4: Column Headers
        List<Object> headers = new ArrayList<>(Arrays.asList(
                "#", "Registeration ID", "Name", "Email", "Phone Number", "Is Alex Eng Student", "Batch", "Department"
        ));
        for (EventFormQuestion question : questions) {
            headers.add(question.getQuestionText());
        }
        rows.add(headers);

        // Rows 5+: Registrants Data
        int seqNum = 1;
        for (EventRegistration reg : registrations) {
            User user = reg.getUser();
            if (user == null) continue;

            List<Object> row = new ArrayList<>(Arrays.asList(
                    seqNum++,
                    reg.getId(),
                    user.getName() != null ? user.getName() : "",
                    user.getEmail() != null ? user.getEmail() : "",
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                    user.getIsAlexEngStudent() != null && user.getIsAlexEngStudent() ? "Yes" : "No",
                    user.getBatch() != null ? user.getBatch() : "",
                    user.getDepartment() != null ? user.getDepartment().name() : ""
            ));

            for (EventFormQuestion question : questions) {
                String answer = reg.getAnswers() != null ? reg.getAnswers().getOrDefault(question.getId(), "") : "";
                row.add(answer);
            }
            rows.add(row);
        }

        String spreadsheetUrl = event.getGoogleSheetUrl();
        String spreadsheetId = googleSheetsService.extractSpreadsheetId(spreadsheetUrl);

        boolean needsNewSheet = (spreadsheetId == null);

        if (!needsNewSheet) {
            try {
                // Try clearing and writing to existing sheet
                googleSheetsService.clearSpreadsheet(spreadsheetId);
                googleSheetsService.writeSpreadsheetData(spreadsheetId, rows);
                event.setSheetLastUpdatedAt(LocalDateTime.now());
                eventRepository.save(event);
            } catch (GoogleSheetsNotFoundException e) {
                needsNewSheet = true;
            }
        }

        if (needsNewSheet) {
            String title = "ACM Alexandria - Event: " + event.getName() + " - Registrations";
            String newUrl = googleSheetsService.createSpreadsheet(title, eventsFolderId);
            String newId = googleSheetsService.extractSpreadsheetId(newUrl);
            googleSheetsService.writeSpreadsheetData(newId, rows);

            event.setGoogleSheetUrl(newUrl);
            event.setSheetLastUpdatedAt(LocalDateTime.now());
            eventRepository.save(event);
        }

        return getRegistrationAnalysis(eventId);
    }
}
