package com.stark.sensors.repo;

import com.stark.sensors.domain.Sensor;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, UUID> {}




