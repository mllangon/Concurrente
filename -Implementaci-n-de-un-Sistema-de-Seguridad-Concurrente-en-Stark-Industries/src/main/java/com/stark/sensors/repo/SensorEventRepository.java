package com.stark.sensors.repo;

import com.stark.sensors.domain.SensorEvent;
import com.stark.sensors.domain.SensorEvent.Severity;
import com.stark.sensors.domain.SensorType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorEventRepository extends JpaRepository<SensorEvent, UUID> {
    @Query("select e from SensorEvent e where (:type is null or e.type = :type) and (:severity is null or e.severity = :severity) and (:fromTs is null or e.ts >= :fromTs) and (:toTs is null or e.ts <= :toTs)")
    Page<SensorEvent> search(@Param("type") SensorType type,
                             @Param("severity") Severity severity,
                             @Param("fromTs") Instant fromTs,
                             @Param("toTs") Instant toTs,
                             Pageable pageable);
}



