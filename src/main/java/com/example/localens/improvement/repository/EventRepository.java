package com.example.localens.improvement.repository;

import com.example.localens.improvement.domain.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findAllById(Iterable<String> eventUuids);
}
