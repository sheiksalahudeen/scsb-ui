package org.recap.repository.jpa;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.jpa.RoleEntity;
import org.recap.model.jpa.UsersEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Created by akulak on 24/1/17.
 */
public class UserDetailsRepositoryUT extends BaseTestCase {

    @Autowired
    UserDetailsRepository userDetailsRepository;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    RolesDetailsRepositorty rolesDetailsRepositorty;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void findByLoginId(){
        UsersEntity usersEntity = saveUser("testUt", "desc", 1, Arrays.asList(1, 2), "testUt@mail.com");
        assertNotNull(usersEntity);
        UsersEntity usersEntity1 = userDetailsRepository.findByLoginId(usersEntity.getLoginId());
        assertNotNull(usersEntity1);
        checkUserEntityResults(usersEntity, usersEntity1);
    }


    @Test
    public void findByUserId(){
        UsersEntity usersEntity = saveUser("testUt", "desc", 1, Arrays.asList(1, 2), "testUt@mail.com");
        assertNotNull(usersEntity);
        UsersEntity usersEntity1 = userDetailsRepository.findByUserId(usersEntity.getUserId());
        assertNotNull(usersEntity1);
        checkUserEntityResults(usersEntity, usersEntity1);

    }

    @Test
    public void findAll(){
        UsersEntity usersEntity = saveUser("testUt", "desc", 1, Arrays.asList(1, 2), "testUt@mail.com");
        assertNotNull(usersEntity);
        Page<UsersEntity> usersEntity1 = userDetailsRepository.findAll(new PageRequest(1, 10));
        assertNotNull(usersEntity1);
    }

    @Test
    public void findByInstitutionEntity(){
        UsersEntity usersEntity = saveUser("testUt", "desc", 1, Arrays.asList(1, 2), "testUt@mail.com");
        assertNotNull(usersEntity);
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionId(1);
        Page<UsersEntity> usersEntity1 = userDetailsRepository.findByInstitutionEntity(institutionEntity ,new PageRequest(1, 10));
        assertNotNull(usersEntity1);
    }

    @Test
    public void findByLoginIdPage(){
        UsersEntity usersEntity = saveUser("testUt", "desc", 1, Arrays.asList(1, 2), "testUt@mail.com");
        assertNotNull(usersEntity);
        Page<UsersEntity> usersEntity1 = userDetailsRepository.findByLoginId(usersEntity.getLoginId(),new PageRequest(1, 10));
        assertNotNull(usersEntity1);
    }

    @Test
    public void findByLoginIdAndInstitutionEntity(){
        UsersEntity usersEntity = saveUser("testUt", "desc", 1, Arrays.asList(1, 2), "testUt@mail.com");
        assertNotNull(usersEntity);
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionId(1);
        Page<UsersEntity> usersEntity1 = userDetailsRepository.findByLoginIdAndInstitutionEntity(usersEntity.getLoginId(),institutionEntity ,new PageRequest(1, 10));
        assertNotNull(usersEntity1);
    }

    @Test
    public void findByLoginIdAndInstitutionId(){
        UsersEntity usersEntity = saveUser("testUt", "desc", 1, Arrays.asList(1, 2), "testUt@mail.com");
        assertNotNull(usersEntity);
        UsersEntity usersEntity1 = userDetailsRepository.findByLoginIdAndInstitutionId(usersEntity.getLoginId(),usersEntity.getInstitutionId());
        assertNotNull(usersEntity1);
        checkUserEntityResults(usersEntity, usersEntity1);

    }

    @Test
    public void findByEmailId(){
        UsersEntity usersEntity = saveUser("testUt", "desc", 1, Arrays.asList(1, 2), "testUt@mail.com");
        assertNotNull(usersEntity);
        Page<UsersEntity> usersEntity1 = userDetailsRepository.findByEmailId(usersEntity.getEmailId(), new PageRequest(1, 10));
        assertNotNull(usersEntity1);
    }

    @Test
    public void findByEmailIdAndInstitutionEntity(){
        UsersEntity usersEntity = saveUser("testUt", "desc", 1, Arrays.asList(1, 2), "testUt@mail.com");
        assertNotNull(usersEntity);
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionId(1);
        Page<UsersEntity> usersEntity1 = userDetailsRepository.findByEmailIdAndInstitutionEntity(usersEntity.getEmailId(),institutionEntity,new PageRequest(1, 10));
        assertNotNull(usersEntity1);
    }

    @Test
    public void findByLoginIdAndEmailId(){
        UsersEntity usersEntity = saveUser("testUt", "desc", 1, Arrays.asList(1, 2), "testUt@mail.com");
        assertNotNull(usersEntity);
        Page<UsersEntity> usersEntity1 = userDetailsRepository.findByLoginIdAndEmailId(usersEntity.getEmailId(), usersEntity.getEmailId(),new PageRequest(1, 10));
        assertNotNull(usersEntity1);
    }

    @Test
    public void findByLoginIdAndEmailIdAndInstitutionEntity(){
        UsersEntity usersEntity = saveUser("testUt", "desc", 1, Arrays.asList(1, 2), "testUt@mail.com");
        assertNotNull(usersEntity);
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionId(1);
        Page<UsersEntity> usersEntity1 = userDetailsRepository.findByLoginIdAndEmailIdAndInstitutionEntity(usersEntity.getLoginId(),usersEntity.getEmailId(),institutionEntity,new PageRequest(1, 10));
        assertNotNull(usersEntity1);
    }


    private UsersEntity saveUser(String networkLoginId, String userDescription, Integer institutionId, List<Integer> roleIds, String userEmailId) {
        UsersEntity usersEntity = new UsersEntity();
        UsersEntity savedUsersEntity = null;
        usersEntity.setLoginId(networkLoginId);
        usersEntity.setUserDescription(userDescription);
        usersEntity.setInstitutionId(institutionId);
        usersEntity.setEmailId(userEmailId);
        usersEntity.setCreatedDate(new Date());
        usersEntity.setCreatedBy("admin");
        usersEntity.setLastUpdatedDate(new Date());
        usersEntity.setLastUpdatedBy("admin");
        InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionId(usersEntity.getInstitutionId());
        if (institutionEntity != null) {
            usersEntity.setInstitutionEntity(institutionEntity);
        }
        List<RoleEntity> roleEntityList = rolesDetailsRepositorty.findByRoleIdIn(roleIds);
        if (roleEntityList != null) {
            usersEntity.setUserRole(roleEntityList);
        }
        savedUsersEntity = userDetailsRepository.saveAndFlush(usersEntity);
        entityManager.refresh(savedUsersEntity);
        return savedUsersEntity;
    }

    private void checkUserEntityResults(UsersEntity usersEntity, UsersEntity usersEntity1) {
        assertEquals(usersEntity.getLoginId(),usersEntity1.getLoginId());
        assertEquals(usersEntity.getUserDescription(),usersEntity1.getUserDescription());
        assertEquals(usersEntity.getInstitutionId(),usersEntity1.getInstitutionId());
        assertEquals(usersEntity.getUserRole(),usersEntity1.getUserRole());
        assertEquals(usersEntity.getEmailId(),usersEntity1.getEmailId());
    }


}
