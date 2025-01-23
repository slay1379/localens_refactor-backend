package com.example.localens.improvement.repository;

import com.example.localens.improvement.domain.Event;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findAllById(Iterable<String> eventUuids);

    @Query("SELECT e FROM Event e WHERE e.eventUuid IN :eventUuids")
    List<Event> findAllByEventUuidIn(@Param("eventUuids") List<UUID> eventUuids);
}
