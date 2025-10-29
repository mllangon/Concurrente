package com.stark.sensors.web;

import com.stark.sensors.domain.Sensor;
import com.stark.sensors.dto.SensorEventDto;
import com.stark.sensors.repo.SensorRepository;
import com.stark.sensors.service.SensorIngestionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    private final SensorRepository sensorRepo;
    private final SensorIngestionService ingestionService;

    public SensorController(SensorRepository sensorRepo, SensorIngestionService ingestionService) {
        this.sensorRepo = sensorRepo;
        this.ingestionService = ingestionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SECURITY_ENGINEER')")
    public Sensor create(@RequestBody @Valid Sensor sensor) { return sensorRepo.save(sensor); }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECURITY_ENGINEER','OPERATOR')")
    public List<Sensor> list() { return sensorRepo.findAll(); }

    @GetMapping("/public")
    public List<Sensor> listPublic() { 
        return sensorRepo.findAll(); 
    }

    @PostMapping("/{id}/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN','SECURITY_ENGINEER')")
    public void ingest(@PathVariable("id") UUID id, @RequestBody @Valid SensorEventDto dto) {
        dto.setSensorId(id);
        ingestionService.ingestAsync(dto);
    }
}


