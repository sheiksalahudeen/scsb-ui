package org.recap.repository.jpa;

import org.recap.model.jpa.RequestStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by rajeshbabuk on 8/2/17.
 */
public interface RequestStatusDetailsRepository extends JpaRepository<RequestStatusEntity, Integer> {
    @Query(value="select requestStatus from RequestStatusEntity requestStatus where requestStatus.requestStatusDescription NOT IN ('PROCESSING ...')")
    List<RequestStatusEntity> findAllExceptProcessing();

    @Query(value="select requestStatusDescription from RequestStatusEntity requestStatus where requestStatus.requestStatusDescription NOT IN ('PROCESSING ...')")
    List<String> findAllRequestStatusDescExceptProcessing();
}

