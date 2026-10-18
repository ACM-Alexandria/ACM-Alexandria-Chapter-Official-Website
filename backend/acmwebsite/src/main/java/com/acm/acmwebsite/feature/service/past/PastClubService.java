package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.feature.dto.past.PastClubRequest;
import com.acm.acmwebsite.feature.dto.past.PastClubResponse;
import com.acm.acmwebsite.feature.entity.past.PastClub;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.PastBoardMapper;
import com.acm.acmwebsite.feature.repository.past.PastClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PastClubService {
    private final PastClubRepository repository;
    private final PastBoardMapper mapper;
    private final PastSeasonPolicy seasonPolicy;
    private final ApplicationEventPublisher events;

    public List<PastClubResponse> list(Integer season) {
        seasonPolicy.validate(season);
        return repository.findBySeasonOrderByNameAscIdAsc(season).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PastClubResponse create(PastClubRequest request) {
        seasonPolicy.validate(request.getSeason());
        PastClub entity = mapper.toEntity(request);
        var response = mapper.toResponse(repository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    public PastClubResponse update(Long id, PastClubRequest request) {
        PastClub entity = getById(id);
        seasonPolicy.validateUpdate(entity.getSeason(), request.getSeason());
        mapper.update(request, entity);
        var response = mapper.toResponse(repository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    public void delete(Long id) {
        PastClub entity = getById(id);
        seasonPolicy.validate(entity.getSeason());
        repository.delete(entity);
        events.publishEvent(new PastBoardsChangedEvent());
    }

    @Transactional(readOnly = true)
    public PastClub getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Past club record not found with id " + id));
    }
}
