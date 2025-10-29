package com.stark.access.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
public class AccessLog {
    @Id
    @GeneratedValue
    private UUID id;

    private String personId;
    private String personName;
    private boolean authorized;
    private String location;
    private Instant ts = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public boolean isAuthorized() { return authorized; }
    public void setAuthorized(boolean authorized) { this.authorized = authorized; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Instant getTs() { return ts; }
    public void setTs(Instant ts) { this.ts = ts; }
}




