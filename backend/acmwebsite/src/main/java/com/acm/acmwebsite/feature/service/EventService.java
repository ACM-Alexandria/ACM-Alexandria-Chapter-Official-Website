package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.EventCardDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.EventRegistration;
import com.acm.acmwebsite.feature.entity.EventFormQuestion;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.mapper.EventMapper;
import com.acm.acmwebsite.feature.util.QuestionValidationUtil;
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
    private final SubscriptionService subscriptionService;

    @Value("${google.sheets.events-folder-id:}")
    private String eventsFolderId;

    public EventService(EventRepository eventRepository, EventMapper eventMapper,
                        EventRegistrationRepository eventRegistrationRepository,
                        EventFormQuestionRepository eventFormQuestionRepository,
                        GoogleSheetsService googleSheetsService,
                        SubscriptionService subscriptionService) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.eventFormQuestionRepository = eventFormQuestionRepository;
        this.googleSheetsService = googleSheetsService;
        this.subscriptionService = subscriptionService;
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
        Event savedEvent = eventRepository.save(event);
        notifySubscribersAboutNewEvent(savedEvent);
        return savedEvent;
    }

    @Transactional
    public FormQuestionResponseDto createQuestion(Long eventId, FormQuestionRequestDto request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id " + eventId));

        EventFormQuestion question = EventFormQuestion.builder()
                .event(event)
                .questionText(QuestionValidationUtil.validateQuestionText(request))
                .questionType(QuestionValidationUtil.parseQuestionType(request))
                .isRequired(Boolean.TRUE.equals(request.getIsRequired()))
                .options(QuestionValidationUtil.normalizeOptions(request.getOptions()))
                .build();

        return toResponseDto(eventFormQuestionRepository.save(question));
    }

    @Transactional
    public FormQuestionResponseDto updateQuestion(Long eventId, Long questionId, FormQuestionRequestDto request) {
        EventFormQuestion question = eventFormQuestionRepository.findById(questionId)
                .filter(q -> q.getEvent() != null && q.getEvent().getId() == eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event question not found with id " + questionId));

        question.setQuestionText(QuestionValidationUtil.validateQuestionText(request));
        question.setQuestionType(QuestionValidationUtil.parseQuestionType(request));
        question.setIsRequired(Boolean.TRUE.equals(request.getIsRequired()));
        question.setOptions(QuestionValidationUtil.normalizeOptions(request.getOptions()));

        return toResponseDto(eventFormQuestionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long eventId, Long questionId) {
        EventFormQuestion question = eventFormQuestionRepository.findById(questionId)
                .filter(q -> q.getEvent() != null && q.getEvent().getId() == eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event question not found with id " + questionId));

        eventFormQuestionRepository.delete(question);
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

    private void notifySubscribersAboutNewEvent(Event event) {
        try {
            subscriptionService.sendMessageToAllActiveSubscribers(buildNewEventMessage(event));
        } catch (Exception e) {
        }
    }

    private Message buildNewEventMessage(Event event) {
        String eventTime = event.getEventTime() != null ? event.getEventTime().toString() : "To be announced";
        String location = event.getLocation() != null && !event.getLocation().isBlank()
                ? event.getLocation()
                : "To be announced";

        String body = """
                Hello,

                A new ACM Alexandria event has been created: %s.

                Time: %s
                Location: %s

                Stay tuned for more details on the website.

                Best regards,
                ACM Alexandria Student Chapter
                """.formatted(event.getName(), eventTime, location);

        return new Message("New ACM Alexandria Event: " + event.getName(), body);
    }

    private FormQuestionResponseDto toResponseDto(EventFormQuestion question) {
        return FormQuestionResponseDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .isRequired(question.getIsRequired())
                .options(question.getOptions())
                .build();
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
