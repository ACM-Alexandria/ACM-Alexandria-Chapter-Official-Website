package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.mapper.ClubMapper;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClubService {
    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;

    public ClubService(ClubRepository clubRepository, ClubMapper clubMapper) {
        this.clubRepository = clubRepository;
        this.clubMapper = clubMapper;
    }


    public Page<ClubCardDto> getClubsByPage(int pageNumber) {
        pageNumber = Math.max(0, pageNumber);
        Pageable pageable = PageRequest.of(pageNumber, 4, Sort.by("name").ascending());
        return clubRepository.findAll(pageable).map(clubMapper::toClubCardDto);
    }
    public Optional<Club> getClubById(long id) {
        return clubRepository.findById(id);
    }
    public Club createClub(Club club) {
        return clubRepository.save(club);
    }
    public Club updateClub(Long id,Club updatedClub) {
        return clubRepository.findById(id).map(club -> {
            club.setName(updatedClub.getName());
            club.setDescription(updatedClub.getDescription());
            club.setImageUrl(updatedClub.getImageUrl());
            club.setSocialMediaLinks(updatedClub.getSocialMediaLinks());
            return clubRepository.save(club);
                }
        ).orElseThrow(()->new RuntimeException("Club not found"));
    }
    public void deleteClubById(long id) {
        clubRepository.deleteById(id);
    }
}
