package com.creatorhire.repository;

import java.util.List;
import com.creatorhire.entity.Report;
import com.creatorhire.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatus(ReportStatus status);
}
