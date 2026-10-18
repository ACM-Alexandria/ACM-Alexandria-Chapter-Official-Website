package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.feature.dto.past.PastClubBoardRequest;
import com.acm.acmwebsite.feature.dto.past.PastClubBoardResponse;
import com.acm.acmwebsite.feature.entity.past.PastClubBoard;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.PastBoardMapper;
import com.acm.acmwebsite.feature.repository.past.PastClubBoardRepository;
import com.acm.acmwebsite.feature.entity.past.PastClub;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PastClubBoardService {
    private final PastClubBoardRepository repository;
    private final PastBoardMapper mapper;
    private final PastSeasonPolicy seasonPolicy;
    private final ApplicationEventPublisher events;
    private final PastClubService clubService;

    public List<PastClubBoardResponse> list(Integer season) {
        seasonPolicy.validate(season);
        return repository.findBySeasonOrdered(season).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PastClubBoardResponse create(PastClubBoardRequest request) {
        seasonPolicy.validate(request.getSeason());
        PastClub parent = clubService.getById(request.getPastClubId());
        seasonPolicy.validateSameSeason(parent.getSeason(), request.getSeason());
        PastClubBoard entity = mapper.toEntity(request);
        entity.setPastClub(parent);
        var response = mapper.toResponse(repository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    public PastClubBoardResponse update(Long id, PastClubBoardRequest request) {
        PastClubBoard entity = find(id);
        seasonPolicy.validateUpdate(entity.getSeason(), request.getSeason());
        PastClub parent = clubService.getById(request.getPastClubId());
        seasonPolicy.validateSameSeason(parent.getSeason(), request.getSeason());
        mapper.update(request, entity);
        entity.setPastClub(parent);
        var response = mapper.toResponse(repository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    public void delete(Long id) {
        PastClubBoard entity = find(id);
        seasonPolicy.validate(entity.getSeason());
        repository.delete(entity);
        events.publishEvent(new PastBoardsChangedEvent());
    }

    private PastClubBoard find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Past club board record not found with id " + id));
    }
}
