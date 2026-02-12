package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.entity.SocialLink;
import com.acm.acmwebsite.feature.repository.SocialLinkRepository;
import com.acm.acmwebsite.feature.service.SocialLinkService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

public class SocialLinkServiceTest {
    @Mock
    private SocialLinkRepository socialLinkRepository;

    @InjectMocks
    private SocialLinkService socialLinkService;

    private SocialLink link() {
        SocialLink l = new SocialLink();
        l.setId(1L);
        l.setUrl("https://facebook.com/acm");
        l.setPlatform("FACEBOOK");
        return l;
    }



    @Test
    void getAllLinks_shouldReturnList() {
        when(socialLinkRepository.findAll())
                .thenReturn(List.of(link()));

        List<SocialLink> result = socialLinkService.getAllLinks();

        assertEquals(1, result.size());
        verify(socialLinkRepository).findAll();
    }



    @Test
    void getLinkById_found() {
        when(socialLinkRepository.findById(1L))
                .thenReturn(Optional.of(link()));

        Optional<SocialLink> result = socialLinkService.getLinkById(1L);

        assertTrue(result.isPresent());
        assertEquals("FACEBOOK", result.get().getPlatform());
    }

    @Test
    void getLinkById_notFound() {
        when(socialLinkRepository.findById(1L))
                .thenReturn(Optional.empty());

        Optional<SocialLink> result = socialLinkService.getLinkById(1L);

        assertTrue(result.isEmpty());
    }



    @Test
    void createSocialLink_shouldSave() {
        SocialLink l = link();
        when(socialLinkRepository.save(l)).thenReturn(l);

        SocialLink saved = socialLinkService.createSocialLink(l);

        assertEquals("FACEBOOK", saved.getPlatform());
        verify(socialLinkRepository).save(l);
    }



    @Test
    void updateSocialLink_success() {
        SocialLink existing = link();

        SocialLink updated = new SocialLink();
        updated.setUrl("https://x.com/acm");
        updated.setPlatform("TWITTER");

        when(socialLinkRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(socialLinkRepository.save(existing))
                .thenReturn(existing);

        SocialLink result = socialLinkService.updateSocialLink(1L, updated);

        assertEquals("https://x.com/acm", result.getUrl());
        assertEquals("TWITTER", result.getPlatform());

        verify(socialLinkRepository).save(existing);
    }


    @Test
    void updateSocialLink_notFound_shouldThrow() {
        when(socialLinkRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> socialLinkService.updateSocialLink(1L, new SocialLink())
        );

        assertEquals("Social Link not found", ex.getMessage());
    }


    @Test
    void deleteSocialLink_shouldCallRepository() {
        socialLinkService.deleteSocialLink(1L);
        verify(socialLinkRepository).deleteById(1L);
    }
}
