package com.acm.acmwebsite.feature.service.past;

import com.acm.acmwebsite.feature.dto.past.BoardArchiveSummary;
import com.acm.acmwebsite.feature.entity.HighBoard;
import com.acm.acmwebsite.feature.entity.CommitteeBoard;
import com.acm.acmwebsite.feature.entity.ClubBoard;
import com.acm.acmwebsite.feature.entity.past.PastCommittee;
import com.acm.acmwebsite.feature.entity.past.PastClub;
import com.acm.acmwebsite.feature.exception.BoardArchiveConflictException;
import com.acm.acmwebsite.feature.mapper.PastBoardSnapshotMapper;
import com.acm.acmwebsite.feature.repository.*;
import com.acm.acmwebsite.feature.repository.past.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PastBoardSnapshotService {
    private final CommitteeRepository committeeRepository;
    private final ClubRepository clubRepository;
    private final HighBoardRepository highBoardRepository;
    private final CommitteeBoardRepository committeeBoardRepository;
    private final ClubBoardRepository clubBoardRepository;
    private final PastCommitteeRepository pastCommitteeRepository;
    private final PastClubRepository pastClubRepository;
    private final PastHighBoardRepository pastHighBoardRepository;
    private final PastCommitteeBoardRepository pastCommitteeBoardRepository;
    private final PastClubBoardRepository pastClubBoardRepository;
    private final PastBoardSnapshotMapper mapper;

    public BoardArchiveSummary snapshot(Integer season) {
        if (pastHighBoardRepository.existsBySeason(season) || pastCommitteeRepository.existsBySeason(season)
                || pastCommitteeBoardRepository.existsBySeason(season) || pastClubRepository.existsBySeason(season)
                || pastClubBoardRepository.existsBySeason(season)) {
            throw new BoardArchiveConflictException("Season " + season + " already contains past board data. Nothing was archived.");
        }

        Map<Long, PastCommittee> committees = new HashMap<>();
        for (var committee : committeeRepository.findAll()) {
            committees.put(committee.getId(), pastCommitteeRepository.save(mapper.toPast(committee, season)));
        }
        Map<Long, PastClub> clubs = new HashMap<>();
        for (var club : clubRepository.findAll()) {
            clubs.put(club.getId(), pastClubRepository.save(mapper.toPast(club, season)));
        }

        var highBoard = highBoardRepository.findAllWithUser().stream()
                .sorted(Comparator.comparing(HighBoard::getOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(HighBoard::getId))
                .map(board -> mapper.toPast(board, season)).toList();
        pastHighBoardRepository.saveAll(highBoard);

        var committeeBoard = committeeBoardRepository.findAllWithUserAndCommittee().stream()
                .sorted(Comparator.comparing(CommitteeBoard::getOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(CommitteeBoard::getId))
                .map(board -> {
                    var past = mapper.toPast(board, season);
                    past.setPastCommittee(committees.get(board.getCommittee().getId()));
                    return past;
                }).toList();
        pastCommitteeBoardRepository.saveAll(committeeBoard);

        var clubBoard = clubBoardRepository.findAllWithUserAndClub().stream()
                .sorted(Comparator.comparing(ClubBoard::getOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ClubBoard::getId))
                .map(board -> {
                    var past = mapper.toPast(board, season);
                    past.setPastClub(clubs.get(board.getClub().getId()));
                    return past;
                }).toList();
        pastClubBoardRepository.saveAll(clubBoard);

        return BoardArchiveSummary.builder().archivedSeason(season)
                .highBoardMembers(highBoard.size()).committees(committees.size())
                .committeeBoardMembers(committeeBoard.size()).clubs(clubs.size())
                .clubBoardMembers(clubBoard.size()).build();
    }
}
