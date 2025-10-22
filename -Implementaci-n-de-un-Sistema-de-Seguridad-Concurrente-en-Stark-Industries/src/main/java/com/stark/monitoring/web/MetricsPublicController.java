package com.stark.monitoring.web;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsPublicController {
    private final MeterRegistry meterRegistry;

    public MetricsPublicController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicMetrics() {
        List<String> meters = meterRegistry.getMeters().stream()
                .map(Meter::getId)
                .map(id -> id.getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("meters", meters));
    }
}



