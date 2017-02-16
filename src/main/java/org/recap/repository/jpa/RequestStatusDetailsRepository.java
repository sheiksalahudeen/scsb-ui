package org.recap.repository.jpa;

import org.recap.model.jpa.RequestStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by rajeshbabuk on 8/2/17.
 */
public interface RequestStatusDetailsRepository extends JpaRepository<RequestStatusEntity, Integer> {
}

