package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.feature.dto.past.PastCommitteeRequest;
import com.acm.acmwebsite.feature.dto.past.PastCommitteeResponse;
import com.acm.acmwebsite.feature.entity.past.PastCommittee;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.PastBoardMapper;
import com.acm.acmwebsite.feature.repository.past.PastCommitteeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PastCommitteeService {
    private final PastCommitteeRepository repository;
    private final PastBoardMapper mapper;
    private final PastSeasonPolicy seasonPolicy;
    private final ApplicationEventPublisher events;

    public List<PastCommitteeResponse> list(Integer season) {
        seasonPolicy.validate(season);
        return repository.findBySeasonOrderByNameAscIdAsc(season).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PastCommitteeResponse create(PastCommitteeRequest request) {
        seasonPolicy.validate(request.getSeason());
        PastCommittee entity = mapper.toEntity(request);
        var response = mapper.toResponse(repository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    public PastCommitteeResponse update(Long id, PastCommitteeRequest request) {
        PastCommittee entity = getById(id);
        seasonPolicy.validateUpdate(entity.getSeason(), request.getSeason());
        mapper.update(request, entity);
        var response = mapper.toResponse(repository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    public void delete(Long id) {
        PastCommittee entity = getById(id);
        seasonPolicy.validate(entity.getSeason());
        repository.delete(entity);
        events.publishEvent(new PastBoardsChangedEvent());
    }

    @Transactional(readOnly = true)
    public PastCommittee getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Past committee record not found with id " + id));
    }
}
