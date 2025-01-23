package com.example.localens.improvement.repository;

import com.example.localens.improvement.domain.Event;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e FROM Event e WHERE SUBSTRING(e.eventUuid, 1, 14) IN :prefixes")
    List<Event> findAllByEventUuidPrefix(@Param("prefixes") List<String> prefixes);
}
