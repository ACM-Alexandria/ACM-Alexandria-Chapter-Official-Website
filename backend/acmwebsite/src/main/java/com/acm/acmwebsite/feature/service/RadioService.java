package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.dto.RadioEpisodeDto;
import com.acm.acmwebsite.feature.dto.RadioSeasonDto;
import com.acm.acmwebsite.feature.entity.RadioEpisode;
import com.acm.acmwebsite.feature.entity.RadioSeason;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.RadioMapper;
import com.acm.acmwebsite.feature.repository.RadioEpisodeRepository;
import com.acm.acmwebsite.feature.repository.RadioSeasonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RadioService {

    private final RadioSeasonRepository radioSeasonRepository;
    private final RadioEpisodeRepository radioEpisodeRepository;
    private final RadioMapper radioMapper;
    private static final Logger logger = LoggerFactory.getLogger(RadioService.class);
    public RadioService(RadioSeasonRepository radioSeasonRepository, RadioEpisodeRepository radioEpisodeRepository, RadioMapper radioMapper) {
        this.radioSeasonRepository = radioSeasonRepository;
        this.radioEpisodeRepository = radioEpisodeRepository;
        this.radioMapper = radioMapper;
    }

    // ── Season CRUD ──

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.RADIO, key = "'RadioSeasonsPage_' + #pageNumber")
    public Page<RadioSeasonDto> getSeasonsByPage(int pageNumber) {
        logger.info("fetching seasons by page from database...");
        pageNumber = Math.max(0, pageNumber);
        Pageable pageable = PageRequest.of(pageNumber, 4, Sort.by(Sort.Direction.DESC, "seasonNumber"));
        return radioSeasonRepository.findAll(pageable).map(radioMapper::toSeasonDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.RADIO, key = "'RadioSeason_' + #id", unless = "#result == null")
    public Optional<RadioSeasonDto> getSeasonById(Long id) {
        logger.info("fetching season by id from database...");
        return radioSeasonRepository.findById(id).map(radioMapper::toSeasonDto);
    }

    @CacheEvict(value = CacheNames.RADIO, allEntries = true)
    public RadioSeasonDto createSeason(RadioSeasonDto seasonDto) {
        if (seasonDto.getSeasonNumber() == null) {
            throw new IllegalArgumentException("Season number is required");
        }
        
        // Check uniqueness of season number
        if (radioSeasonRepository.findBySeasonNumber(seasonDto.getSeasonNumber()).isPresent()) {
            throw new IllegalArgumentException("Season number already exists: " + seasonDto.getSeasonNumber());
        }

        RadioSeason season = radioMapper.toSeason(seasonDto);
        return radioMapper.toSeasonDto(radioSeasonRepository.save(season));
    }

    @CacheEvict(value = CacheNames.RADIO, allEntries = true)
    public RadioSeasonDto updateSeason(Long id, RadioSeasonDto seasonDto) {
        if (seasonDto.getSeasonNumber() == null) {
            throw new IllegalArgumentException("Season number is required");
        }

        RadioSeason season = radioSeasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Season not found with id: " + id));

        // Check uniqueness if number changes
        if (!season.getSeasonNumber().equals(seasonDto.getSeasonNumber())) {
            if (radioSeasonRepository.findBySeasonNumber(seasonDto.getSeasonNumber()).isPresent()) {
                throw new IllegalArgumentException("Season number already exists: " + seasonDto.getSeasonNumber());
            }
        }

        season.setSeasonNumber(seasonDto.getSeasonNumber());
        season.setImageUrl(seasonDto.getImageUrl());

        return radioMapper.toSeasonDto(radioSeasonRepository.save(season));
    }

    @CacheEvict(value = CacheNames.RADIO, allEntries = true)
    public void deleteSeason(Long id) {
        if (!radioSeasonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Season not found with id: " + id);
        }
        radioSeasonRepository.deleteById(id);
    }

    // ── Episode CRUD ──

    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.RADIO, key = "'RadioEpisodes_' + #seasonId + '_page_' + #pageNumber")
    public Page<RadioEpisodeDto> getEpisodesBySeason(Long seasonId, int pageNumber) {
        logger.info("fetching episodes by season id from database...");
        pageNumber = Math.max(0, pageNumber);
        Pageable pageable = PageRequest.of(pageNumber, 5, Sort.by(Sort.Direction.ASC, "episodeNumber"));
        return radioEpisodeRepository.findByRadioSeasonId(seasonId, pageable).map(radioMapper::toEpisodeDto);
    }

    @CacheEvict(value = CacheNames.RADIO, allEntries = true)
    public RadioEpisodeDto createEpisode(RadioEpisodeDto episodeDto) {
        if (episodeDto.getRadioSeasonId() == null) {
            throw new IllegalArgumentException("Season ID is required");
        }
        if (episodeDto.getEpisodeNumber() == null) {
            throw new IllegalArgumentException("Episode number is required");
        }
        if (episodeDto.getTitle() == null || episodeDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Episode title is required");
        }
        if (episodeDto.getUrl() == null || episodeDto.getUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Episode URL is required");
        }
        if (episodeDto.getImageUrl() == null || episodeDto.getImageUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Episode image URL is required");
        }

        RadioSeason season = radioSeasonRepository.findById(episodeDto.getRadioSeasonId())
                .orElseThrow(() -> new ResourceNotFoundException("Season not found with id: " + episodeDto.getRadioSeasonId()));

        RadioEpisode episode = radioMapper.toEpisode(episodeDto, season);
        return radioMapper.toEpisodeDto(radioEpisodeRepository.save(episode));
    }

    @CacheEvict(value = CacheNames.RADIO, allEntries = true)
    public RadioEpisodeDto updateEpisode(Long id, RadioEpisodeDto episodeDto) {
        if (episodeDto.getEpisodeNumber() == null) {
            throw new IllegalArgumentException("Episode number is required");
        }
        if (episodeDto.getTitle() == null || episodeDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Episode title is required");
        }
        if (episodeDto.getUrl() == null || episodeDto.getUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Episode URL is required");
        }
        if (episodeDto.getImageUrl() == null || episodeDto.getImageUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Episode image URL is required");
        }

        RadioEpisode episode = radioEpisodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found with id: " + id));

        episode.setEpisodeNumber(episodeDto.getEpisodeNumber());
        episode.setTitle(episodeDto.getTitle());
        episode.setImageUrl(episodeDto.getImageUrl());
        episode.setUrl(episodeDto.getUrl());
        episode.setGuest(episodeDto.getGuest());
        episode.setHost(episodeDto.getHost());

        return radioMapper.toEpisodeDto(radioEpisodeRepository.save(episode));
    }

    @CacheEvict(value = CacheNames.RADIO, allEntries = true)
    public void deleteEpisode(Long id) {
        if (!radioEpisodeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Episode not found with id: " + id);
        }
        radioEpisodeRepository.deleteById(id);
    }
}
