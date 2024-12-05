package com.example.localens.improvement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event")
@Getter
public class Event {

    @Id
    @Column(name = "event_uuid", columnDefinition = "BINARY(16)")
    private UUID eventUuid;

    @Column(name = "event_name", length = 255, nullable = false)
    private String eventName;

    @Column(name = "event_img", length = 255)
    private String eventImg;

    @Column(name = "event_place", length = 255)
    private String eventPlace;

    @Column(name = "event_start")
    private LocalDateTime eventStart;

    @Column(name = "event_end")
    private LocalDateTime eventEnd;

    @Column(name = "info", length = 255)
    private String info;

    @Column(name = "event_place_int")
    private int eventPlaceInt;
}
