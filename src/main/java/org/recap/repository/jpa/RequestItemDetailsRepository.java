package org.recap.repository.jpa;

import org.recap.model.jpa.RequestItemEntity;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
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

    @Query(value = "select request from RequestItemEntity request inner join  request.requestStatusEntity status where status.requestStatusDescription = :status")
    Page<RequestItemEntity> findByStatus(Pageable pageable, @Param("status") String status);

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item inner join request.requestStatusEntity status where item.barcode = :itemBarcode and status.requestStatusCode in ('RETRIEVAL_ORDER_PLACED','RECALL_ORDER_PLACED', 'EDD_ORDER_PLACED')")
    Page<RequestItemEntity> findByItemBarcodeAndActive(Pageable pageable, @Param("itemBarcode") String itemBarcode);

    @Query(value = "select request from RequestItemEntity request inner join request.requestStatusEntity status where request.patronId = :patronBarcode and status.requestStatusCode in ('RETRIEVAL_ORDER_PLACED', 'RECALL_ORDER_PLACED', 'EDD_ORDER_PLACED')")
    Page<RequestItemEntity> findByPatronBarcodeAndActive(Pageable pageable, @Param("patronBarcode") String patronBarcode);

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item inner join request.requestStatusEntity status where request.patronId = :patronBarcode and request.requestingInstitutionId = :institutionId and item.barcode = :itemBarcode and status.requestStatusCode.requestStatusDescription = :status")
    Page<RequestItemEntity> findByPatronBarcodeAndItemBarcodeAndStatusAndInstitution(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("itemBarcode") String itemBarcode, @Param("status") String status,@Param("institutionId")Integer institutionId);

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item inner join request.requestStatusEntity status where request.patronId = :patronBarcode and item.barcode = :itemBarcode and request.requestingInstitutionId = :institutionId and status.requestStatusCode in ('RETRIEVAL_ORDER_PLACED', 'RECALL_ORDER_PLACED', 'EDD_ORDER_PLACED')")
    Page<RequestItemEntity> findByPatronBarcodeAndItemBarcodeAndActiveAndInstitution(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("itemBarcode") String itemBarcode, @Param("institutionId")Integer institutionId);

    @Query(value = "select request from RequestItemEntity request inner join request.requestStatusEntity status where request.patronId = :patronBarcode and status.requestStatusDescription = :status")
    Page<RequestItemEntity> findByPatronBarcodeAndStatus(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("status") String status);

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item inner join request.requestStatusEntity status where item.barcode = :itemBarcode and status.requestStatusDescription = :status")
    Page<RequestItemEntity> findByItemBarcodeAndStatus(Pageable pageable, @Param("itemBarcode") String itemBarcode, @Param("status") String status);

    @Query(value = "select request from RequestItemEntity request inner join request.requestStatusEntity status where status.requestStatusCode in ('RETRIEVAL_ORDER_PLACED', 'RECALL_ORDER_PLACED','EDD_ORDER_PLACED')")
    Page<RequestItemEntity> findAllActive(Pageable pageable);

    @Query(value = "select request from RequestItemEntity request where request.patronId = :patronBarcode")
    Page<RequestItemEntity> findByPatronBarcode(Pageable pageable, @Param("patronBarcode") String patronBarcode);

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item where item.barcode = :itemBarcode")
    Page<RequestItemEntity> findByItemBarcode(Pageable pageable, @Param("itemBarcode") String itemBarcode);

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item where request.patronId = :patronBarcode and item.barcode = :itemBarcode")
    Page<RequestItemEntity> findByPatronBarcodeAndItemBarcode(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("itemBarcode") String itemBarcode);

    @Query(value = "select requestItemEntity from RequestItemEntity requestItemEntity inner join requestItemEntity.itemEntity ie inner join requestItemEntity.requestStatusEntity as rse  where ie.barcode = :itemBarcode and rse.requestStatusCode= :requestStatusCode ")
    RequestItemEntity findByItemBarcodeAndRequestStaCode(@Param("itemBarcode") String itemBarcode, @Param("requestStatusCode") String requestStatusCode) throws IncorrectResultSizeDataAccessException;

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

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item where request.patronId = :patronBarcode and request.requestingInstitutionId = :institutionId and item.barcode = :itemBarcode")
    Page<RequestItemEntity> findByPatronBarcodeAndItemBarcodeAndInstitution(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("itemBarcode") String itemBarcode,@Param("institutionId")Integer institutionId);

    @Query(value = "select request from RequestItemEntity request inner join request.requestStatusEntity status where request.patronId = :patronBarcode and request.requestingInstitutionId = :institutionId and status.requestStatusCode in ('RETRIEVAL_ORDER_PLACED', 'RECALL_ORDER_PLACED', 'EDD_ORDER_PLACED')")
    Page<RequestItemEntity> findByPatronBarcodeAndActiveAndInstitution(Pageable pageable, @Param("patronBarcode") String patronBarcode,@Param("institutionId")Integer institutionId);

    @Query(value = "select request from RequestItemEntity request inner join request.requestStatusEntity status where request.patronId = :patronBarcode and request.requestingInstitutionId = :institutionId and status.requestStatusDescription = :status")
    Page<RequestItemEntity> findByPatronBarcodeAndStatusAndInstitution(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("status") String status,@Param("institutionId")Integer institutionId);

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item inner join request.requestStatusEntity status where request.patronId = :patronBarcode and item.barcode = :itemBarcode and status.requestStatusCode.requestStatusDescription = :status")
    Page<RequestItemEntity> findByPatronBarcodeAndItemBarcodeAndStatus(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("itemBarcode") String itemBarcode, @Param("status")String status);

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item inner join request.requestStatusEntity status where request.patronId = :patronBarcode and item.barcode = :itemBarcode and status.requestStatusCode in ('RETRIEVAL_ORDER_PLACED', 'RECALL_ORDER_PLACED', 'EDD_ORDER_PLACED')")
    Page<RequestItemEntity> findByPatronBarcodeAndItemBarcodeAndActive(Pageable pageable, @Param("patronBarcode") String patronBarcode, @Param("itemBarcode") String itemBarcode);

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item inner join request.requestStatusEntity status where item.barcode = :itemBarcode and request.requestingInstitutionId = :institutionId and status.requestStatusCode in ('RETRIEVAL_ORDER_PLACED','RECALL_ORDER_PLACED', 'EDD_ORDER_PLACED')")
    Page<RequestItemEntity> findByItemBarcodeAndActiveAndInstitution(Pageable pageable, @Param("itemBarcode") String itemBarcode,@Param("institutionId")Integer institutionId);

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item inner join request.requestStatusEntity status where item.barcode = :itemBarcode and request.requestingInstitutionId = :institutionId and status.requestStatusDescription = :status")
    Page<RequestItemEntity> findByItemBarcodeAndStatusAndInstitution(Pageable pageable, @Param("itemBarcode") String itemBarcode, @Param("status") String status,@Param("institutionId")Integer institutionId);

    @Query(value = "select request from RequestItemEntity request where request.patronId = :patronBarcode and request.requestingInstitutionId = :institutionId")
    Page<RequestItemEntity> findByPatronBarcodeAndInstitution(Pageable pageable, @Param("patronBarcode") String patronBarcode,@Param("institutionId")Integer institutionId);

    @Query(value = "select request from RequestItemEntity request inner join request.itemEntity item where item.barcode = :itemBarcode and request.requestingInstitutionId = :institutionId")
    Page<RequestItemEntity> findByItemBarcodeAndInstitution(Pageable pageable, @Param("itemBarcode") String itemBarcode,@Param("institutionId")Integer institutionId);

    @Query(value = "select request from RequestItemEntity request inner join request.requestStatusEntity status where request.requestingInstitutionId = :institutionId and status.requestStatusCode in ('RETRIEVAL_ORDER_PLACED', 'RECALL_ORDER_PLACED','EDD_ORDER_PLACED')")
    Page<RequestItemEntity> findAllActiveAndInstitution(Pageable pageable,@Param("institutionId")Integer institutionId);

    @Query(value = "select request from RequestItemEntity request inner join  request.requestStatusEntity status where request.requestingInstitutionId = :institutionId and status.requestStatusDescription = :status")
    Page<RequestItemEntity> findByStatusAndInstitution(Pageable pageable, @Param("status") String status,@Param("institutionId")Integer institutionId);
}
