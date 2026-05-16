package com.acm.acmwebsite.feature.entity;

import com.acm.acmwebsite.User_Authentication.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "event_registration", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "event_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;

    @CreationTimestamp
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers", columnDefinition = "json")
    @Builder.Default
    private Map<Long, String> answers = new HashMap<>();
}
