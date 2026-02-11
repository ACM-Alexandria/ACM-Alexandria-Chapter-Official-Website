package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClubService {
    private final ClubRepository clubRepository;
    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
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
            club.setGoogleFormUrl(updatedClub.getGoogleFormUrl());
            club.setSocialMediaLinks(updatedClub.getSocialMediaLinks());
            return clubRepository.save(club);
                }
        ).orElseThrow(()->new RuntimeException("Club not found"));
    }
    public void deleteClubById(long id) {
        clubRepository.deleteById(id);
    }
}
