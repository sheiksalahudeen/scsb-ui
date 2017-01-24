package org.recap.controller;

import org.junit.Test;
import org.mockito.Mock;
import org.recap.BaseTestCase;
import org.recap.model.jpa.RoleEntity;
import org.recap.model.search.RolesForm;
import org.recap.repository.jpa.PermissionsDetailsRepository;
import org.recap.repository.jpa.RolesDetailsRepositorty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Created by hemalathas on 23/12/16.
 */
public class RolesControllerUT extends BaseTestCase {

    @Mock
    Model model;

    @Mock
    BindingResult bindingResult;

    @Autowired
    RolesController rolesController;

    @Autowired
    PermissionsDetailsRepository permissionsRepository;

    @Autowired
    RolesDetailsRepositorty rolesDetailsRepositorty;

    @Mock
    HttpServletRequest request;

    @Test
    public void testRoles(){
        RolesForm rolesForm = new RolesForm();
        rolesForm.setRoleName("SuperAdmin");
        ModelAndView modelAndView = rolesController.search(rolesForm,model);
        assertNotNull(modelAndView);
        Map rolesMap = new HashMap();
        rolesMap = modelAndView.getModel();
        rolesForm = (RolesForm) rolesMap.get("rolesForm");
        assertEquals(rolesForm.getRolesSearchResults().get(0).getRolesDescription(),"Admin for all the institutions");
    }

    @Test
    public void testCreateAndDeleteRole(){
        RolesForm rolesForm = new RolesForm();
        rolesForm.setNewRoleName("NewSearch");
        rolesForm.setNewRoleDescription("Ability to search SCSB and export results");
        rolesForm.setNewPermissionNames("CreateUser");
        ModelAndView modelAndView = rolesController.newRole(rolesForm, model);
        assertNotNull(modelAndView);
        Map rolesMap = new HashMap();
        rolesMap = modelAndView.getModel();
        rolesForm = (RolesForm) rolesMap.get("rolesForm");
        System.out.println(rolesForm.toString());
        assertEquals(rolesForm.getMessage(),"Role added successfully");

        RoleEntity roleEntity = rolesDetailsRepositorty.findByRoleName("NewSearch");
        assertNotNull(roleEntity);
        assertNotNull(roleEntity.getRoleId());

        RolesForm rolesForm1 = new RolesForm();
        rolesForm1.setRoleId(roleEntity.getRoleId());
        ModelAndView modelAndView1 = rolesController.delete(rolesForm1, model);
        assertNotNull(modelAndView1);
        rolesMap.clear();
        rolesMap = modelAndView1.getModel();
        rolesForm1 = (RolesForm) rolesMap.get("rolesForm");
        assertEquals(rolesForm1.getMessage(),"Role deleted successfully");

    }

    @Test
    public void search() throws Exception{
        RolesForm rolesForm = new RolesForm();
        rolesForm.setRoleName("Admin");
        rolesForm.setPermissionNames("CreateUser");
        ModelAndView modelAndView = rolesController.search(rolesForm,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void populatePermissionNames() throws Exception{
        RolesForm rolesForm = new RolesForm();
        ModelAndView modelAndView = rolesController.populatePermissionNames(model);
        assertNotNull(modelAndView);
        assertEquals("roles",modelAndView.getViewName());
    }

    @Test
    public void newRole() throws Exception{
        RolesForm rolesForm = new RolesForm();
        rolesForm.setNewRoleName("test");
        rolesForm.setNewRoleDescription("test Description");
        rolesForm.setNewPermissionNames("CreateUser");
        ModelAndView modelAndView = rolesController.newRole(rolesForm,model);
        assertNotNull(modelAndView);
        assertEquals("roles",modelAndView.getViewName());
    }

    @Test
    public void editRole() throws Exception{
        RolesForm rolesForm = new RolesForm();
        rolesForm.setRoleId(1);
        rolesForm.setRoleName("Admin");
        rolesForm.setPermissionNames("CreateUser");
        rolesForm.setRoleDescriptionForDelete("test desc");
        ModelAndView modelAndView = rolesController.editRole(rolesForm.getRoleId(),rolesForm.getRoleName(),rolesForm.getRoleDescription(),rolesForm.getPermissionNames());
        assertNotNull(modelAndView);
        assertEquals("roles",modelAndView.getViewName());
    }

    @Test
    public void saveEditedRole() throws Exception{
        RolesForm rolesForm = new RolesForm();
        rolesForm.setRoleId(1);
        rolesForm.setEditRoleName("Admin");
        String[] permissionName ={"CreateUser"};
        when(request.getParameterValues("permissionNames[]")).thenReturn(permissionName);
        rolesForm.setEditPermissionName(Arrays.asList(permissionName));
        rolesForm.setEditRoleDescription("test desc");
        ModelAndView modelAndView = rolesController.saveEditedRole(rolesForm.getRoleId(),rolesForm.getEditRoleName(),rolesForm.getEditRoleName(),model,request);
        assertNotNull(modelAndView);
        assertEquals("roles",modelAndView.getViewName());
    }
    @Test
    public void deleteRole() throws Exception{
        RolesForm rolesForm = new RolesForm();
        rolesForm.setRoleId(1);
        rolesForm.setRoleNameForDelete("Admin");
        rolesForm.setRoleDescriptionForDelete("test desc");
        rolesForm.setPermissionNamesForDelete("CreateUser");
        ModelAndView modelAndView = rolesController.deleteRole(rolesForm.getRoleId(),rolesForm.getRoleName(),rolesForm.getRoleDescription(),rolesForm.getPermissionNames(),10,1,2);
        assertNotNull(modelAndView);
        assertEquals("roles",modelAndView.getViewName());
    }

    @Test
    public void delete() throws Exception{
        RolesForm rolesForm = new RolesForm();
        rolesForm.setRoleId(1);
        rolesForm.setRoleNameForDelete("Admin");
        rolesForm.setRoleDescriptionForDelete("test desc");
        rolesForm.setPermissionNamesForDelete("CreateUser");
        ModelAndView modelAndView = rolesController.delete(rolesForm,model);
        assertNotNull(modelAndView);
        assertEquals("roles",modelAndView.getViewName());
    }

    @Test
    public void searchNext() throws Exception{
        RolesForm rolesForm = new RolesForm();
        rolesForm.setRoleName("Admin");
        rolesForm.setPermissionNames("CreateUser");
        rolesForm.setPageNumber(0);
        rolesForm.setPageSize(10);
        ModelAndView modelAndView = rolesController.searchNext(rolesForm,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void searchPrevious() throws Exception{
        RolesForm rolesForm = new RolesForm();
        rolesForm.setRoleName("Admin");
        rolesForm.setPermissionNames("CreateUser");
        rolesForm.setPageNumber(0);
        rolesForm.setPageSize(25);
        ModelAndView modelAndView = rolesController.searchPrevious(rolesForm,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void searchFirst() throws Exception{
        RolesForm rolesForm = new RolesForm();
        rolesForm.setRoleName("Admin");
        rolesForm.setPermissionNames("CreateUser");
        rolesForm.setPageNumber(0);
        rolesForm.setPageSize(25);
        ModelAndView modelAndView = rolesController.searchFirst(rolesForm,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void searchLast() throws Exception{
        RolesForm rolesForm = new RolesForm();
        rolesForm.setRoleName("Admin");
        rolesForm.setPermissionNames("CreateUser");
        rolesForm.setPageNumber(2);
        rolesForm.setPageSize(25);
        ModelAndView modelAndView = rolesController.searchLast(rolesForm,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void clearPage() {
        RolesForm rolesForm = new RolesForm();
        rolesForm.setNewRoleName("");
        rolesForm.setNewRoleDescription("");
        rolesForm.setNewPermissionNames("");
        rolesForm.setEditRoleName("");
        rolesForm.setEditRoleDescription("");
        rolesForm.setEditPermissionNames("");
        rolesForm.setErrorMessage("");
        rolesForm.setMessage("");
        rolesForm.setSelectedPermissionNames(new ArrayList<String>());
        ModelAndView modelAndView = rolesController.clearPage(rolesForm,model);
        assertNotNull(modelAndView);
        assertEquals("roles",modelAndView.getViewName());
    }

    @Test
    public void onPageSizeChange() throws Exception{
        RolesForm rolesForm = new RolesForm();
        rolesForm.setRoleName("Admin");
        rolesForm.setPermissionNames("CreateUser");
        rolesForm.setPageNumber(2);
        rolesForm.setPageSize(25);
        ModelAndView modelAndView = rolesController.onPageSizeChange(rolesForm,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }



}