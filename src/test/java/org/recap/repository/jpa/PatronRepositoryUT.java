package org.recap.repository.jpa;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.PatronEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.assertEquals;


/**
 * Created by dharmendrag on 6/2/17.
 */
public class PatronRepositoryUT extends BaseTestCase {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void createPatron(){
        PatronEntity patronEntity = new PatronEntity();
        patronEntity.setPatronId(1);
        patronEntity.setInstitutionId(1);
        patronEntity.setInstitutionIdentifier("test");
        patronEntity.setEmailId("testmail@example.org");

        PatronEntity savedPatron=patronDetailsRepository.saveAndFlush(patronEntity);
        entityManager.refresh(savedPatron);

        assertEquals(patronEntity.getInstitutionId(),savedPatron.getInstitutionId());
        assertEquals(patronEntity.getPatronId(),savedPatron.getInstitutionId());
        assertEquals(patronEntity.getInstitutionIdentifier(),savedPatron.getInstitutionIdentifier());
        assertEquals(patronEntity.getEmailId(),savedPatron.getEmailId());

    }
}
