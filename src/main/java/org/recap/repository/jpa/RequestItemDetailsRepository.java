package org.recap.repository.jpa;

import org.recap.model.jpa.RequestItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by rajeshbabuk on 26/10/16.
 */
public interface RequestItemDetailsRepository extends JpaRepository<RequestItemEntity, Integer>, JpaSpecificationExecutor {

    RequestItemEntity findByRequestId(@Param("requestId") Integer requestId);

    @Query(value = "select request from RequestItemEntity request where request.requestStatusId = (select requestStatusId from RequestStatusEntity requestStatus where requestStatus.requestStatusDescription = :status)")
    Page<RequestItemEntity> findByStatus(Pageable pageable, @Param("status") String status);

    @Query(value = "select request from RequestItemEntity request where request.itemId = (select itemId from ItemEntity item where item.barcode = :itemBarcode) and request.requestStatusId in (select requestStatusId from RequestStatusEntity requestStatus where requestStatus.requestStatusCode in ('RETRIEVAL_ORDER_PLACED', 'RECALL ORDER PLACED', 'EDD ORDER PLACED'))")
    Page<RequestItemEntity> findByItemBarcodeAndActive(Pageable pageable, @Param("itemBarcode") String itemBarcode);

    @Query(value = "select request from RequestItemEntity request where request.patronId = (select patronId from PatronEntity patron where patron.institutionIdentifier = :patronBarcode) and request.requestStatusId in (select requestStatusId from RequestStatusEntity requestStatus where requestStatus.requestStatusCode in ('RETRIEVAL_ORDER_PLACED', 'RECALL ORDER PLACED', 'EDD ORDER PLACED'))")
    Page<RequestItemEntity> findByPatronBarcodeAndActive(Pageable pageable, @Param("patronBarcode") String patronBarcode);

    @Query(value = "select request from RequestItemEntity request where request.patronId = (select patronId from PatronEntity patron where patron.institutionIdentifier = :patronBarcode) and request.itemId = (select itemId from ItemEntity item where item.barcode = :itemBarcode) and request.requestStatusId = (select requestStatusId from RequestStatusEntity requestStatus where requestStatus.requestStatusDescription = :status)")
    Page<RequestItemEntity> findByPatronBarcodeAndItemBarcodeAndStatus(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("itemBarcode") String itemBarcode, @Param("status") String status);

    @Query(value = "select request from RequestItemEntity request where request.patronId = (select patronId from PatronEntity patron where patron.institutionIdentifier = :patronBarcode) and request.itemId = (select itemId from ItemEntity item where item.barcode = :itemBarcode) and request.requestStatusId in (select requestStatusId from RequestStatusEntity requestStatus where requestStatus.requestStatusCode in ('RETRIEVAL_ORDER_PLACED', 'RECALL ORDER PLACED', 'EDD ORDER PLACED'))")
    Page<RequestItemEntity> findByPatronBarcodeAndItemBarcodeAndActive(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("itemBarcode") String itemBarcode);

    @Query(value = "select request from RequestItemEntity request where request.patronId = (select patronId from PatronEntity patron where patron.institutionIdentifier = :patronBarcode) and request.requestStatusId = (select requestStatusId from RequestStatusEntity requestStatus where requestStatus.requestStatusDescription = :status)")
    Page<RequestItemEntity> findByPatronBarcodeAndStatus(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("status") String status);

    @Query(value = "select request from RequestItemEntity request where request.itemId = (select itemId from ItemEntity item where item.barcode = :itemBarcode) and request.requestStatusId = (select requestStatusId from RequestStatusEntity requestStatus where requestStatus.requestStatusDescription = :status)")
    Page<RequestItemEntity> findByItemBarcodeAndStatus(Pageable pageable, @Param("itemBarcode") String itemBarcode, @Param("status") String status);

    @Query(value = "select request from RequestItemEntity request where request.requestStatusId in (select requestStatusId from RequestStatusEntity requestStatus where requestStatus.requestStatusCode in ('RETRIEVAL_ORDER_PLACED', 'RECALL_RETRIEVAL_ORDER_PLACED'))")
    Page<RequestItemEntity> findAllActive(Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM REQUEST_ITEM_T , REQUEST_TYPE_T,ITEM_T WHERE REQUEST_ITEM_T.REQUEST_TYPE_ID=REQUEST_TYPE_T.REQUEST_TYPE_ID " +
            "AND REQUEST_ITEM_T.ITEM_ID=ITEM_T.ITEM_ID " +
            "AND REQUEST_ITEM_T.REQUEST_TYPE_ID IN (SELECT REQUEST_TYPE_ID FROM REQUEST_TYPE_T WHERE REQUEST_TYPE_CODE IN ('RETRIEVAL', 'RECALL', 'EDD')) " +
            "AND REQUEST_ITEM_T.CREATED_DATE >= :fromDate AND REQUEST_ITEM_T.CREATED_DATE<= :toDate " +
            "AND ITEM_T.OWNING_INST_ID = :itemOwningInstId " +
            "AND REQUEST_ITEM_T.REQUESTING_INST_ID IN (:requestingInstIds)", nativeQuery = true)
    long getIlRequestCounts(@Param("fromDate") Date fromDate,
                            @Param("toDate") Date toDate,
                            @Param("itemOwningInstId") int itemOwningInstId,
                            @Param("requestingInstIds") List<Integer> requestingInstIds);

    @Query(value = "SELECT COUNT(*) FROM REQUEST_ITEM_T ,REQUEST_TYPE_T, ITEM_T WHERE REQUEST_ITEM_T.REQUEST_TYPE_ID=REQUEST_TYPE_T.REQUEST_TYPE_ID " +
            "AND REQUEST_ITEM_T.ITEM_ID=ITEM_T.ITEM_ID " +
            "AND REQUEST_ITEM_T.CREATED_DATE >= :fromDate AND REQUEST_ITEM_T.CREATED_DATE <= :toDate " +
            "AND REQUEST_ITEM_T.REQUEST_TYPE_ID = (SELECT REQUEST_TYPE_ID FROM REQUEST_TYPE_T WHERE REQUEST_TYPE_CODE = :requestTypeCode) " +
            "AND ITEM_T.OWNING_INST_ID = :itemOwningInstId", nativeQuery = true)
    long getBDHoldRecallRetrievalRequestCounts(@Param("fromDate") Date fromDate,
                                               @Param("toDate") Date toDate,
                                               @Param("itemOwningInstId") int itemOwningInstId,
                                               @Param("requestTypeCode") String requestTypeCode);

    @Query(value = "SELECT COUNT(*) FROM REQUEST_ITEM_T , REQUEST_TYPE_T , ITEM_T WHERE REQUEST_ITEM_T.REQUEST_TYPE_ID=REQUEST_TYPE_T.REQUEST_TYPE_ID " +
            "AND REQUEST_ITEM_T.ITEM_ID=ITEM_T.ITEM_ID " +
            "AND REQUEST_ITEM_T.CREATED_DATE >= :fromDate AND REQUEST_ITEM_T.CREATED_DATE <= :toDate " +
            "AND REQUEST_ITEM_T.REQUEST_TYPE_ID IN (SELECT REQUEST_TYPE_ID FROM REQUEST_TYPE_T WHERE REQUEST_TYPE_CODE IN (:requestTypeCodes)) " +
            "AND ITEM_T.COLLECTION_GROUP_ID IN (:collectionGroupIds) " +
            "AND ITEM_T.OWNING_INST_ID = :itemOwningInstId ", nativeQuery = true)
    long getPhysicalAndEDDCounts(@Param("fromDate") Date fromDate,
                                 @Param("toDate") Date toDate,
                                 @Param("itemOwningInstId") int itemOwningInstId,
                                 @Param("collectionGroupIds") List<Integer> collectionGroupIds,
                                 @Param("requestTypeCodes") List<String> requestTypeCodes);

}
