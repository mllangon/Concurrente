package com.stark.sensors.web;

import com.stark.sensors.domain.SensorEvent;
import com.stark.sensors.domain.SensorEvent.Severity;
import com.stark.sensors.domain.SensorType;
import com.stark.sensors.repo.SensorEventRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventQueryController {
    private final SensorEventRepository repo;

    public EventQueryController(SensorEventRepository repo) { this.repo = repo; }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECURITY_ENGINEER','OPERATOR','VIEWER')")
    public Page<SensorEvent> search(@RequestParam(required = false) SensorType type,
                                    @RequestParam(required = false) Severity severity,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        if (type == null && severity == null && from == null && to == null) {
            return repo.findAll(PageRequest.of(page, size));
        }
        return repo.search(type, severity, from, to, PageRequest.of(page, size));
    }
}



