package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.service.UserService;
import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.BoardArchiveSummary;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastBoardResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubBoardRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubBoardResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeBoardRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeBoardResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastHighBoardRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastHighBoardResponse;
import com.acm.acmwebsite.feature.entity.ClubBoard;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.entity.PastClub;
import com.acm.acmwebsite.feature.entity.PastClubBoard;
import com.acm.acmwebsite.feature.entity.PastCommittee;
import com.acm.acmwebsite.feature.entity.PastCommitteeBoard;
import com.acm.acmwebsite.feature.entity.PastHighBoard;
import com.acm.acmwebsite.feature.entity.PastSeason;
import com.acm.acmwebsite.feature.event.CurrentBoardsArchivedEvent;
import com.acm.acmwebsite.feature.event.PastBoardsChangedEvent;
import com.acm.acmwebsite.feature.exception.BoardArchiveConflictException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.PastBoardMapper;
import com.acm.acmwebsite.feature.mapper.PastBoardSnapshotMapper;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.repository.CommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.repository.HighBoardRepository;
import com.acm.acmwebsite.feature.repository.PastClubBoardRepository;
import com.acm.acmwebsite.feature.repository.PastClubRepository;
import com.acm.acmwebsite.feature.repository.PastCommitteeBoardRepository;
import com.acm.acmwebsite.feature.repository.PastCommitteeRepository;
import com.acm.acmwebsite.feature.repository.PastHighBoardRepository;
import com.acm.acmwebsite.feature.repository.PastSeasonRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

// Everything about past boards: seasons, the five past-board tables, archiving and cache invalidation.
@Service
@RequiredArgsConstructor
public class PastSeasonsService {
    private final PastHighBoardRepository pastHighBoardRepository;
    private final PastSeasonRepository pastSeasonRepository;
    private final PastCommitteeRepository pastCommitteeRepository;
    private final PastCommitteeBoardRepository pastCommitteeBoardRepository;
    private final PastClubRepository pastClubRepository;
    private final PastClubBoardRepository pastClubBoardRepository;
    private final CommitteeRepository committeeRepository;
    private final ClubRepository clubRepository;
    private final HighBoardRepository highBoardRepository;
    private final CommitteeBoardRepository committeeBoardRepository;
    private final ClubBoardRepository clubBoardRepository;
    private final PastBoardMapper mapper;
    private final PastBoardSnapshotMapper snapshotMapper;
    private final SystemSettingsService systemSettingsService;
    private final UserService userService;
    private final ApplicationEventPublisher events;
    private final EntityManager entityManager;
    private final CacheManager cacheManager;

    // ----- seasons and public board view -----

    // Saved seasons plus any season that only exists through its board records.
    @Cacheable(CacheNames.BOARD_SEASONS)
    @Transactional(readOnly = true)
    public List<Integer> getSeasons() {
        return Stream.of(pastSeasonRepository.findAllSeasons(), pastHighBoardRepository.findDistinctSeasons(),
                        pastCommitteeRepository.findDistinctSeasons(), pastCommitteeBoardRepository.findDistinctSeasons(),
                        pastClubRepository.findDistinctSeasons(), pastClubBoardRepository.findDistinctSeasons())
                .flatMap(List::stream).distinct().sorted(Comparator.reverseOrder()).toList();
    }

    @Cacheable(value = CacheNames.PAST_BOARDS, key = "#season")
    @Transactional
    public PastBoardResponse getBySeason(Integer season) {
        validateStartingYear(season);
        if (!getSeasons().contains(season) || season >= systemSettingsService.getCurrentSeason()) {
            throw new ResourceNotFoundException("Past boards not found for season " + season);
        }
        var committeeMembers = listPastCommitteeBoards(season);
        var clubMembers = listPastClubBoards(season);
        var pastCommittees = listPastCommittees(season);
        for (var committee : pastCommittees) {
            committee.setBoardRoles(committeeMembers.stream()
                    .filter(member -> Objects.equals(member.getPastCommitteeId(), committee.getId()))
                    .toList());
        }
        var pastClubs = listPastClubs(season);
        for (var club : pastClubs) {
            club.setBoardRoles(clubMembers.stream()
                    .filter(member -> Objects.equals(member.getPastClubId(), club.getId()))
                    .toList());
        }
        return PastBoardResponse.builder().season(season)
                .highBoard(listPastHighBoards(season))
                .committees(pastCommittees).clubs(pastClubs).build();
    }

    @Transactional
    public void addPastSeason(Integer season) {
        validate(season);
        if (getSeasons().contains(season)) {
            throw new BoardArchiveConflictException("Season " + season + " already exists.");
        }
        pastSeasonRepository.save(new PastSeason(season));
        events.publishEvent(new PastBoardsChangedEvent());
    }

    // Moves every record of a season to a new year. The new year must be valid and not already in use.
    @Transactional
    public void updateSeason(Integer season, Integer newSeason) {
        if (!getSeasons().contains(season)) {
            throw new ResourceNotFoundException("Past season not found: " + season);
        }
        if (Objects.equals(season, newSeason)) {
            return;
        }
        validate(newSeason);
        if (getSeasons().contains(newSeason)) {
            throw new BoardArchiveConflictException("Season " + newSeason + " already exists.");
        }
        pastClubRepository.moveToSeason(season, newSeason);
        pastClubBoardRepository.moveToSeason(season, newSeason);
        pastCommitteeRepository.moveToSeason(season, newSeason);
        pastCommitteeBoardRepository.moveToSeason(season, newSeason);
        pastHighBoardRepository.moveToSeason(season, newSeason);
        pastSeasonRepository.deleteBySeason(season);
        if (!pastSeasonRepository.existsBySeason(newSeason)) {
            pastSeasonRepository.save(new PastSeason(newSeason));
        }
        events.publishEvent(new PastBoardsChangedEvent());
    }

    // Removes the season and every board record and member that belongs to it.
    @Transactional
    public void deleteSeason(Integer season) {
        if (!getSeasons().contains(season)) {
            throw new ResourceNotFoundException("Past season not found: " + season);
        }
        pastClubBoardRepository.deleteBySeason(season);
        pastCommitteeBoardRepository.deleteBySeason(season);
        pastHighBoardRepository.deleteBySeason(season);
        pastClubRepository.deleteBySeason(season);
        pastCommitteeRepository.deleteBySeason(season);
        pastSeasonRepository.deleteBySeason(season);
        events.publishEvent(new PastBoardsChangedEvent());
    }

    // ----- archiving the current board -----

    @Transactional
    public BoardArchiveSummary archive(Integer expectedSeason) {
        var settings = systemSettingsService.lockForArchive();
        Integer season = settings.getCurrentSeason();
        if (!Objects.equals(expectedSeason, season)) {
            throw new BoardArchiveConflictException("The current season is " + season
                    + ", not " + expectedSeason + ". Refresh and confirm the current season again. Nothing was archived.");
        }
        var summary = snapshot(season);
        summary.setUsersDowngraded(userService.demoteBoardMembers());
        clearBoards();
        summary.setNewCurrentSeason(systemSettingsService.incrementCurrentSeason(settings));
        events.publishEvent(new PastBoardsChangedEvent());
        events.publishEvent(new CurrentBoardsArchivedEvent());
        return summary;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public BoardArchiveSummary snapshot(Integer season) {
        if (pastHighBoardRepository.existsBySeason(season) || pastCommitteeRepository.existsBySeason(season)
                || pastCommitteeBoardRepository.existsBySeason(season) || pastClubRepository.existsBySeason(season)
                || pastClubBoardRepository.existsBySeason(season)) {
            throw new BoardArchiveConflictException("Season " + season + " already contains past board data. Nothing was archived.");
        }

        if (!pastSeasonRepository.existsBySeason(season)) {
            pastSeasonRepository.save(new PastSeason(season));
        }

        Map<Long, PastCommittee> committees = new HashMap<>();
        for (var committee : committeeRepository.findAll()) {
            committees.put(committee.getId(), pastCommitteeRepository.save(snapshotMapper.toPast(committee, season)));
        }
        Map<Long, PastClub> clubs = new HashMap<>();
        for (var club : clubRepository.findAll()) {
            clubs.put(club.getId(), pastClubRepository.save(snapshotMapper.toPast(club, season)));
        }

        var highBoard = highBoardRepository.findAllWithUser().stream()
                .sorted(Comparator.comparing(HighBoard::getOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(HighBoard::getId))
                .map(board -> snapshotMapper.toPast(board, season)).toList();
        pastHighBoardRepository.saveAll(highBoard);

        var committeeBoard = committeeBoardRepository.findAllWithUserAndCommittee().stream()
                .sorted(Comparator.comparing(CommitteeBoard::getOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(CommitteeBoard::getId))
                .map(board -> {
                    var past = snapshotMapper.toPast(board, season);
                    past.setPastCommittee(committees.get(board.getCommittee().getId()));
                    return past;
                }).toList();
        pastCommitteeBoardRepository.saveAll(committeeBoard);

        var clubBoard = clubBoardRepository.findAllWithUserAndClub().stream()
                .sorted(Comparator.comparing(ClubBoard::getOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ClubBoard::getId))
                .map(board -> {
                    var past = snapshotMapper.toPast(board, season);
                    past.setPastClub(clubs.get(board.getClub().getId()));
                    return past;
                }).toList();
        pastClubBoardRepository.saveAll(clubBoard);

        return BoardArchiveSummary.builder().archivedSeason(season)
                .highBoardMembers(highBoard.size()).committees(committees.size())
                .committeeBoardMembers(committeeBoard.size()).clubs(clubs.size())
                .clubBoardMembers(clubBoard.size()).build();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void clearBoards() {
        // Persist snapshots and role changes before bulk deletes bypass the persistence context.
        entityManager.flush();
        highBoardRepository.deleteAllInBatch();
        committeeBoardRepository.deleteAllInBatch();
        clubBoardRepository.deleteAllInBatch();
        entityManager.clear();
    }

    // ----- cache invalidation -----

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPastBoardsChanged(PastBoardsChangedEvent event) {
        Objects.requireNonNull(cacheManager.getCache(CacheNames.BOARD_SEASONS)).clear();
        Objects.requireNonNull(cacheManager.getCache(CacheNames.PAST_BOARDS)).clear();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCurrentBoardsArchived(CurrentBoardsArchivedEvent event) {
        Objects.requireNonNull(cacheManager.getCache(CacheNames.HIGH_BOARD)).clear();
        Objects.requireNonNull(cacheManager.getCache(CacheNames.COMMITTEES)).clear();
        Objects.requireNonNull(cacheManager.getCache(CacheNames.CLUBS)).clear();
    }

    // ----- season validation -----

    public void validateStartingYear(Integer season) {
        if (season == null || season < 2000) {
            throw new IllegalArgumentException("Season must be a starting year of at least 2000");
        }
    }

    public void validate(Integer season) {
        validateStartingYear(season);
        if (season >= systemSettingsService.getCurrentSeason()) {
            throw new IllegalArgumentException("Past season must be earlier than the current season");
        }
    }

    public void validateUpdate(Integer storedSeason, Integer requestedSeason) {
        if (!Objects.equals(storedSeason, requestedSeason)) {
            throw new IllegalArgumentException("Season cannot be changed after creation");
        }
        validate(storedSeason);
    }

    public void validateSameSeason(Integer parentSeason, Integer memberSeason) {
        if (!Objects.equals(parentSeason, memberSeason)) {
            throw new IllegalArgumentException("Member and past committee or club must have the same season");
        }
    }

    // ----- past clubs -----

    @Transactional
    public List<PastClubResponse> listPastClubs(Integer season) {
        validate(season);
        return pastClubRepository.findBySeasonOrderByNameAscIdAsc(season).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public PastClubResponse createPastClub(PastClubRequest request) {
        validate(request.getSeason());
        PastClub entity = mapper.toEntity(request);
        var response = mapper.toResponse(pastClubRepository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    @Transactional
    public PastClubResponse updatePastClub(Long id, PastClubRequest request) {
        PastClub entity = getPastClubById(id);
        validateUpdate(entity.getSeason(), request.getSeason());
        mapper.update(request, entity);
        var response = mapper.toResponse(pastClubRepository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    @Transactional
    public void deletePastClub(Long id) {
        PastClub entity = getPastClubById(id);
        validate(entity.getSeason());
        pastClubRepository.delete(entity);
        events.publishEvent(new PastBoardsChangedEvent());
    }

    @Transactional(readOnly = true)
    public PastClub getPastClubById(Long id) {
        return pastClubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Past club record not found with id " + id));
    }

    // ----- past club board members -----

    @Transactional
    public List<PastClubBoardResponse> listPastClubBoards(Integer season) {
        validate(season);
        return pastClubBoardRepository.findBySeasonOrdered(season).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public PastClubBoardResponse createPastClubBoard(PastClubBoardRequest request) {
        validate(request.getSeason());
        PastClub parent = getPastClubById(request.getPastClubId());
        validateSameSeason(parent.getSeason(), request.getSeason());
        PastClubBoard entity = mapper.toEntity(request);
        entity.setPastClub(parent);
        var response = mapper.toResponse(pastClubBoardRepository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    @Transactional
    public PastClubBoardResponse updatePastClubBoard(Long id, PastClubBoardRequest request) {
        PastClubBoard entity = findPastClubBoard(id);
        validateUpdate(entity.getSeason(), request.getSeason());
        PastClub parent = getPastClubById(request.getPastClubId());
        validateSameSeason(parent.getSeason(), request.getSeason());
        mapper.update(request, entity);
        entity.setPastClub(parent);
        var response = mapper.toResponse(pastClubBoardRepository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    @Transactional
    public void deletePastClubBoard(Long id) {
        PastClubBoard entity = findPastClubBoard(id);
        validate(entity.getSeason());
        pastClubBoardRepository.delete(entity);
        events.publishEvent(new PastBoardsChangedEvent());
    }

    private PastClubBoard findPastClubBoard(Long id) {
        return pastClubBoardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Past club board record not found with id " + id));
    }

    // ----- past committees -----

    @Transactional
    public List<PastCommitteeResponse> listPastCommittees(Integer season) {
        validate(season);
        return pastCommitteeRepository.findBySeasonOrderByNameAscIdAsc(season).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public PastCommitteeResponse createPastCommittee(PastCommitteeRequest request) {
        validate(request.getSeason());
        PastCommittee entity = mapper.toEntity(request);
        var response = mapper.toResponse(pastCommitteeRepository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    @Transactional
    public PastCommitteeResponse updatePastCommittee(Long id, PastCommitteeRequest request) {
        PastCommittee entity = getPastCommitteeById(id);
        validateUpdate(entity.getSeason(), request.getSeason());
        mapper.update(request, entity);
        var response = mapper.toResponse(pastCommitteeRepository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    @Transactional
    public void deletePastCommittee(Long id) {
        PastCommittee entity = getPastCommitteeById(id);
        validate(entity.getSeason());
        pastCommitteeRepository.delete(entity);
        events.publishEvent(new PastBoardsChangedEvent());
    }

    @Transactional(readOnly = true)
    public PastCommittee getPastCommitteeById(Long id) {
        return pastCommitteeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Past committee record not found with id " + id));
    }

    // ----- past committee board members -----

    @Transactional
    public List<PastCommitteeBoardResponse> listPastCommitteeBoards(Integer season) {
        validate(season);
        return pastCommitteeBoardRepository.findBySeasonOrdered(season).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public PastCommitteeBoardResponse createPastCommitteeBoard(PastCommitteeBoardRequest request) {
        validate(request.getSeason());
        PastCommittee parent = getPastCommitteeById(request.getPastCommitteeId());
        validateSameSeason(parent.getSeason(), request.getSeason());
        PastCommitteeBoard entity = mapper.toEntity(request);
        entity.setPastCommittee(parent);
        var response = mapper.toResponse(pastCommitteeBoardRepository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    @Transactional
    public PastCommitteeBoardResponse updatePastCommitteeBoard(Long id, PastCommitteeBoardRequest request) {
        PastCommitteeBoard entity = findPastCommitteeBoard(id);
        validateUpdate(entity.getSeason(), request.getSeason());
        PastCommittee parent = getPastCommitteeById(request.getPastCommitteeId());
        validateSameSeason(parent.getSeason(), request.getSeason());
        mapper.update(request, entity);
        entity.setPastCommittee(parent);
        var response = mapper.toResponse(pastCommitteeBoardRepository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    @Transactional
    public void deletePastCommitteeBoard(Long id) {
        PastCommitteeBoard entity = findPastCommitteeBoard(id);
        validate(entity.getSeason());
        pastCommitteeBoardRepository.delete(entity);
        events.publishEvent(new PastBoardsChangedEvent());
    }

    private PastCommitteeBoard findPastCommitteeBoard(Long id) {
        return pastCommitteeBoardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Past committee board record not found with id " + id));
    }

    // ----- past high board -----

    @Transactional
    public List<PastHighBoardResponse> listPastHighBoards(Integer season) {
        validate(season);
        return pastHighBoardRepository.findBySeasonOrdered(season).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public PastHighBoardResponse createPastHighBoard(PastHighBoardRequest request) {
        validate(request.getSeason());
        PastHighBoard entity = mapper.toEntity(request);
        var response = mapper.toResponse(pastHighBoardRepository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    @Transactional
    public PastHighBoardResponse updatePastHighBoard(Long id, PastHighBoardRequest request) {
        PastHighBoard entity = findPastHighBoard(id);
        validateUpdate(entity.getSeason(), request.getSeason());
        mapper.update(request, entity);
        var response = mapper.toResponse(pastHighBoardRepository.save(entity));
        events.publishEvent(new PastBoardsChangedEvent());
        return response;
    }

    @Transactional
    public void deletePastHighBoard(Long id) {
        PastHighBoard entity = findPastHighBoard(id);
        validate(entity.getSeason());
        pastHighBoardRepository.delete(entity);
        events.publishEvent(new PastBoardsChangedEvent());
    }

    private PastHighBoard findPastHighBoard(Long id) {
        return pastHighBoardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Past high board record not found with id " + id));
    }
}
