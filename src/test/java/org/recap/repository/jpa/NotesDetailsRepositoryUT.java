package org.recap.repository.jpa;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rajeshbabuk on 29/10/16.
 */
public class NotesDetailsRepositoryUT extends BaseTestCase {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    NotesDetailsRepository notesDetailsRepository;

    @Autowired
    RequestTypeDetailsRepository requestTypeDetailsRepository;

    @Test
    public void saveNotesEntity() throws Exception {
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsSingleItem();
        RequestItemEntity requestItemEntity = createRequestItem(bibliographicEntity.getItemEntities().get(0).getItemId());
        NotesEntity notesEntity = new NotesEntity();
        notesEntity.setItemId(bibliographicEntity.getItemEntities().get(0).getItemId());
        notesEntity.setRequestId(requestItemEntity.getRequestId());
        notesEntity.setNotes("Test Notes");

        NotesEntity savedNotesEntity = notesDetailsRepository.save(notesEntity);
        entityManager.refresh(savedNotesEntity);
        assertNotNull(savedNotesEntity);
        assertNotNull(savedNotesEntity.getNotesId());
    }

    @Test
    public void getNotes() throws Exception{
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsSingleItem();
        RequestItemEntity requestItemEntity = createRequestItem(bibliographicEntity.getItemEntities().get(0).getItemId());
        NotesEntity notesEntity = new NotesEntity();
        notesEntity.setItemId(bibliographicEntity.getItemEntities().get(0).getItemId());
        notesEntity.setRequestId(requestItemEntity.getRequestId());
        notesEntity.setNotes("Test Notes");

        NotesEntity savedNotesEntity = notesDetailsRepository.save(notesEntity);
        entityManager.refresh(savedNotesEntity);
        NotesEntity notesEntity1 = notesDetailsRepository.findByItemId(savedNotesEntity.getItemId());
        assertNotNull(notesEntity1);
        assertEquals("Test Notes",notesEntity1.getNotes());
    }

    public RequestItemEntity createRequestItem(int itemId) throws Exception {
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionCode("UOC");
        institutionEntity.setInstitutionName("University of Chicago");
        InstitutionEntity entity = institutionDetailRepository.save(institutionEntity);
        assertNotNull(entity);

        PatronEntity patronEntity = new PatronEntity();
        patronEntity.setInstitutionIdentifier(entity.getInstitutionCode());
        patronEntity.setInstitutionId(entity.getInstitutionId());
        patronEntity.setEmailId("hamalatha.s@htcindia.com");
        PatronEntity savedPatronEntity = patronDetailsRepository.save(patronEntity);
        assertNotNull(savedPatronEntity);


        RequestTypeEntity requestTypeEntity = new RequestTypeEntity();
        requestTypeEntity.setRequestTypeCode("Recallhold");
        requestTypeEntity.setRequestTypeDesc("Recallhold");
        RequestTypeEntity savedRequestTypeEntity = requestTypeDetailsRepository.save(requestTypeEntity);
        assertNotNull(savedRequestTypeEntity);

        RequestItemEntity requestItemEntity = new RequestItemEntity();
        requestItemEntity.setItemId(itemId);
        requestItemEntity.setRequestTypeId(savedRequestTypeEntity.getRequestTypeId());
        requestItemEntity.setRequestingInstitutionId(1);
        requestItemEntity.setPatronId(savedPatronEntity.getPatronId());
        requestItemEntity.setStopCode("test");
        requestItemEntity.setCreatedDate(new Date());
        requestItemEntity.setRequestExpirationDate(new Date());
        requestItemEntity.setRequestExpirationDate(new Date());
        requestItemEntity.setCreatedBy("test");
        requestItemEntity.setEmailId("test@gmail.com");
        requestItemEntity.setRequestStatusId(4);
        RequestItemEntity savedRequestItemEntity = requestItemDetailsRepository.save(requestItemEntity);
        return savedRequestItemEntity;
    }

    public BibliographicEntity saveBibSingleHoldingsSingleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setCreatedBy("tst");
        bibliographicEntity.setLastUpdatedBy("tst");
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setCreatedBy("tst");
        holdingsEntity.setLastUpdatedBy("tst");
        holdingsEntity.setOwningInstitutionId(1);
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setBarcode("123");
        itemEntity.setCallNumber("x.12321");
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCallNumberType("1");
        itemEntity.setCustomerCode("123");
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("tst");
        itemEntity.setLastUpdatedBy("tst");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        return savedBibliographicEntity;

    }
}