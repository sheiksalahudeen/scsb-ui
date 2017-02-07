package org.recap.repository.jpa;

import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.HoldingsPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by hemalathas on 21/6/16.
 */
@RepositoryRestResource(collectionResourceRel = "holdings", path = "holdings")
public interface HoldingsDetailsRepository extends JpaRepository<HoldingsEntity, HoldingsPK> {

    HoldingsEntity findByHoldingsId(Integer holdingsId);
}
