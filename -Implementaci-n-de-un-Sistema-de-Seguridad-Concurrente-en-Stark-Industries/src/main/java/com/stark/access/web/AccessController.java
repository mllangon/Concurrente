package com.stark.access.web;

import com.stark.access.domain.AccessLog;
import com.stark.access.repo.AccessLogRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/access")
public class AccessController {
    private final AccessLogRepository repo;
    private final MeterRegistry meterRegistry;

    public AccessController(AccessLogRepository repo, MeterRegistry meterRegistry) {
        this.repo = repo;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping("/logs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public AccessLog log(@RequestBody @Valid AccessLog log) {
        if (!log.isAuthorized()) {
            meterRegistry.counter("access.denied").increment();
        }
        return repo.save(log);
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public List<AccessLog> list() { return repo.findAll(); }
}




