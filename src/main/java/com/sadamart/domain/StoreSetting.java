package com.sadamart.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "store_settings")
public class StoreSetting {
    @Id
    @Column(name = "setting_key", nullable = false, length = 64)
    private String key;

    @Column(name = "setting_value", nullable = false, length = 500)
    private String value;

    @Column(name = "updated_by", nullable = false, length = 64)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected StoreSetting() { }

    public StoreSetting(String key, String value, String updatedBy) {
        this.key = key;
        this.value = value;
        this.updatedBy = updatedBy;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public void setValue(String value, String actor) {
        this.value = value;
        this.updatedBy = actor;
        this.updatedAt = Instant.now();
    }
    public String getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}
