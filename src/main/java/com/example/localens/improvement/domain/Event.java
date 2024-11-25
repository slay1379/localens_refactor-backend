package com.example.localens.improvement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event")
@Getter
@Setter
public class Event {

    @Id
    @Column(name = "event_uuid")
    private String eventUuid;

    private String eventName;
    private String eventImg;
    private String eventPlace;
    private LocalDateTime eventStart;
    private LocalDateTime eventEnd;
    private String info;
}
