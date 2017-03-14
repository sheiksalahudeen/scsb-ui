package org.recap.repository.jpa;

import org.recap.model.jpa.InstitutionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by hemalathas on 22/6/16.
 */
public interface InstitutionDetailsRepository extends PagingAndSortingRepository<InstitutionEntity,Integer> {
    InstitutionEntity findByInstitutionId(Integer institutionId);
    InstitutionEntity findByInstitutionCode(String institutionCode);
    InstitutionEntity findByInstitutionName(String institutionName);

    @Query(value="select inst from InstitutionEntity inst where inst.institutionCode not in ('HTC')")
    List<InstitutionEntity> getInstitutionCodeForSuperAdmin();
}
