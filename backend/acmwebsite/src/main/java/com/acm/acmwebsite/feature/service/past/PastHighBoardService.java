package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.feature.dto.past.PastHighBoardRequest;
import com.acm.acmwebsite.feature.dto.past.PastHighBoardResponse;
import com.acm.acmwebsite.feature.entity.past.PastHighBoard;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.PastBoardMapper;
import com.acm.acmwebsite.feature.repository.past.PastHighBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PastHighBoardService {
    private final PastHighBoardRepository repository;
    private final PastBoardMapper mapper;
    private final PastSeasonPolicy seasonPolicy;
    private final ApplicationEventPublisher events;

    public List<PastHighBoardResponse> list(Integer season) {
        seasonPolicy.validate(season);
        return repository.findBySeasonOrdered(season).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public PastHighBoardResponse create(PastHighBoardRequest request) {
        seasonPolicy.validate(request.getSeason());
        PastHighBoard entity = mapper.toEntity(request);
        var response = mapper.toResponse(repository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    public PastHighBoardResponse update(Long id, PastHighBoardRequest request) {
        PastHighBoard entity = find(id);
        seasonPolicy.validateUpdate(entity.getSeason(), request.getSeason());
        mapper.update(request, entity);
        var response = mapper.toResponse(repository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    public void delete(Long id) {
        PastHighBoard entity = find(id);
        seasonPolicy.validate(entity.getSeason());
        repository.delete(entity);
        events.publishEvent(new PastBoardsChangedEvent());
    }

    private PastHighBoard find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Past high board record not found with id " + id));
    }
}
