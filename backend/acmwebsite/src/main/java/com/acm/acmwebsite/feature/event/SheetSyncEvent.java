package com.acm.acmwebsite.feature.event;

import com.acm.acmwebsite.feature.enums.RegistrationEntityType;
import org.springframework.context.ApplicationEvent;

public class SheetSyncEvent extends ApplicationEvent {

    private final RegistrationEntityType entityType;
    private final Long entityId;

    /**
     * Published after a registration is successfully saved to the database.
     * The listener picks this up post-commit and schedules a sheet sync.
     *
     * @param entityType the type of entity whose registrations changed
     * @param entityId the ID of that entity
     */
    public SheetSyncEvent(Object source, RegistrationEntityType entityType, Long entityId) {
        super(source);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public RegistrationEntityType getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }
}


