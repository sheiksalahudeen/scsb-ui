package org.recap.repository.jpa;

import org.recap.model.jpa.RequestTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by rajeshbabuk on 28/10/16.
 */
public interface RequestTypeDetailsRepository extends JpaRepository<RequestTypeEntity, Integer> {

    RequestTypeEntity findByRequestTypeCode(String requestTypeCode);

    @Query(value="select requestType from RequestTypeEntity requestType where requestType.requestTypeCode NOT IN ('EDD','BORROW DIRECT')")
    List<RequestTypeEntity> findAllExceptEDDAndBorrowDirect();

    @Query(value="select requestType from RequestTypeEntity requestType where requestType.requestTypeCode NOT IN ('BORROW DIRECT')")
    List<RequestTypeEntity> findAllExceptBorrowDirect();
}
