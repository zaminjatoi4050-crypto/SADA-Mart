package com.sadamart.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_username", nullable = false, length = 64)
    private String actorUsername;

    @Column(name = "actor_role", nullable = false, length = 32)
    private String actorRole;

    @Column(name = "action_type", nullable = false, length = 64)
    private String actionType;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "entity_ref", length = 64)
    private String entityRef;

    @Column(nullable = false, length = 1000)
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected AuditLogEntry() { }

    public AuditLogEntry(String actorUsername, String actorRole, String actionType,
                         String entityType, String entityRef, String details) {
        this.actorUsername = actorUsername;
        this.actorRole = actorRole;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityRef = entityRef;
        this.details = details;
    }

    public Long getId() { return id; }
    public String getActorUsername() { return actorUsername; }
    public String getActorRole() { return actorRole; }
    public String getActionType() { return actionType; }
    public String getEntityType() { return entityType; }
    public String getEntityRef() { return entityRef; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
