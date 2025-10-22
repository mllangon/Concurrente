package com.stark.access.repo;

import com.stark.access.domain.AccessLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {}




