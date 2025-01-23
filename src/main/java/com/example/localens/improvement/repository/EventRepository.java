package com.example.localens.improvement.repository;

import com.example.localens.improvement.domain.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findAllById(Iterable<String> eventUuids);
}
