package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.feature.dto.past.PastCommitteeBoardRequest;
import com.acm.acmwebsite.feature.dto.past.PastCommitteeBoardResponse;
import com.acm.acmwebsite.feature.entity.past.PastCommitteeBoard;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.PastBoardMapper;
import com.acm.acmwebsite.feature.repository.past.PastCommitteeBoardRepository;
import com.acm.acmwebsite.feature.entity.past.PastCommittee;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PastCommitteeBoardService {
    private final PastCommitteeBoardRepository repository;
    private final PastBoardMapper mapper;
    private final PastSeasonPolicy seasonPolicy;
    private final ApplicationEventPublisher events;
    private final PastCommitteeService committeeService;

    public List<PastCommitteeBoardResponse> list(Integer season) {
        seasonPolicy.validate(season);
        return repository.findBySeasonOrdered(season).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PastCommitteeBoardResponse create(PastCommitteeBoardRequest request) {
        seasonPolicy.validate(request.getSeason());
        PastCommittee parent = committeeService.getById(request.getPastCommitteeId());
        seasonPolicy.validateSameSeason(parent.getSeason(), request.getSeason());
        PastCommitteeBoard entity = mapper.toEntity(request);
        entity.setPastCommittee(parent);
        var response = mapper.toResponse(repository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    public PastCommitteeBoardResponse update(Long id, PastCommitteeBoardRequest request) {
        PastCommitteeBoard entity = find(id);
        seasonPolicy.validateUpdate(entity.getSeason(), request.getSeason());
        PastCommittee parent = committeeService.getById(request.getPastCommitteeId());
        seasonPolicy.validateSameSeason(parent.getSeason(), request.getSeason());
        mapper.update(request, entity);
        entity.setPastCommittee(parent);
        var response = mapper.toResponse(repository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    public void delete(Long id) {
        PastCommitteeBoard entity = find(id);
        seasonPolicy.validate(entity.getSeason());
        repository.delete(entity);
        events.publishEvent(new PastBoardsChangedEvent());
    }

    private PastCommitteeBoard find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Past committee board record not found with id " + id));
    }
}
