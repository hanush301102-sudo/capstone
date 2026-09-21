package com.creatorhire.repository;

import java.util.List;
import com.creatorhire.entity.ApplicationSample;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationSampleRepository extends JpaRepository<ApplicationSample, Long> {

    List<ApplicationSample> findByApplicationId(Long applicationId);
}
