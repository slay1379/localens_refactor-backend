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

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "event_img")
    private String eventImg;

    @Column(name = "event_place")
    private String eventPlace;

    @Column(name = "event_start")
    private LocalDateTime eventStart;

    @Column(name = "event_end")
    private LocalDateTime eventEnd;

    @Column(name = "info")
    private String info;
}
