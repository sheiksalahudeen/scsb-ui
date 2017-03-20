package org.recap.controller;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCase;
import org.recap.RecapConstants;
import org.recap.model.jpa.RoleEntity;
import org.recap.model.jpa.UsersEntity;
import org.recap.model.usermanagement.UserDetailsForm;
import org.recap.model.usermanagement.UserForm;
import org.recap.model.usermanagement.UserRoleForm;
import org.recap.model.usermanagement.UserRoleService;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.repository.jpa.RolesDetailsRepositorty;
import org.recap.repository.jpa.UserDetailsRepository;
import org.recap.security.UserManagementService;
import org.recap.util.UserAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Created by akulak on 24/1/17.
 */
public class UserRoleControllerUT extends BaseTestCase {

    private static final Logger logger = LoggerFactory.getLogger(UserRoleController.class);

    @Autowired
    UserRoleController userRoleController;

    @Mock
    UserRoleController mockedUserRoleController;

    @Mock
    UserDetailsRepository userDetailsRepository;

    @Autowired
    RolesDetailsRepositorty rolesDetailsRepositorty;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Mock
    private UserAuthUtil userAuthUtil;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    Model model;

    @Mock
    BindingResult bindingResult;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpSession session;

    @Autowired
    LoginController loginController;

    @Test
    public void showUserRoles() throws Exception{
        when(request.getSession()).thenReturn(session);
        usersSessionAttributes();
        UserDetailsForm userDetailsForm = new UserDetailsForm();
        userDetailsForm.setSuperAdmin(true);
        userDetailsForm.setLoginInstitutionId(1);
        Mockito.when(mockedUserRoleController.getUserAuthUtil()).thenReturn(userAuthUtil);
        Mockito.when(mockedUserRoleController.getUserRoleService()).thenReturn(userRoleService);
        Mockito.when(mockedUserRoleController.getLogger()).thenReturn(logger);
        Mockito.when(mockedUserRoleController.getUserAuthUtil().getUserDetails(session, RecapConstants.BARCODE_RESTRICTED_PRIVILEGE)).thenReturn(userDetailsForm);
        Mockito.when(mockedUserRoleController.getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_USER_ROLE_URL, (UsernamePasswordToken) session.getAttribute(RecapConstants.USER_TOKEN))).thenReturn(true);
        Mockito.when(mockedUserRoleController.showUserRoles(model, request)).thenCallRealMethod();
        String view = mockedUserRoleController.showUserRoles(model, request);
        assertNotNull(view);
        assertEquals("searchRecords",view);
    }

    @Test
    public void searchUserRole() throws Exception{
        UserRoleForm userRoleForm = new UserRoleForm();

        usersSessionAttributes();
        Page<UsersEntity> usersEntityPage = getPage();
        boolean superAdmin = true;
        Mockito.when(mockedUserRoleController.getUserAuthUtil()).thenReturn(userAuthUtil);
        Mockito.when(mockedUserRoleController.getUserRoleService()).thenReturn(userRoleService);
        Mockito.when(mockedUserRoleController.getLogger()).thenReturn(logger);
        Mockito.when(mockedUserRoleController.getUserRoleService().searchUsers(userRoleForm, superAdmin)).thenReturn(usersEntityPage);
        Mockito.when(mockedUserRoleController.getUserAuthUtil().getUserDetails(session, RecapConstants.BARCODE_RESTRICTED_PRIVILEGE)).thenCallRealMethod();
        Mockito.when(mockedUserRoleController.getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_USER_ROLE_URL, (UsernamePasswordToken) session.getAttribute(RecapConstants.USER_TOKEN))).thenReturn(true);
        Mockito.when(mockedUserRoleController.searchUserRole(userRoleForm, model, request)).thenCallRealMethod();
        ModelAndView modelAndView = mockedUserRoleController.searchUserRole(userRoleForm, model, request);
        assertNotNull(modelAndView);
        assertEquals("userRolesSearch :: #request-result-table",modelAndView.getViewName());
    }

    @Test
    public void deleteUserRole() throws Exception{
        UserRoleForm userRoleForm = new UserRoleForm();
        userRoleForm.setSearchNetworkId("smith");
        usersSessionAttributes();
        UserDetailsForm userDetailsForm = new UserDetailsForm();
        userDetailsForm.setSuperAdmin(true);
        userDetailsForm.setLoginInstitutionId(1);
        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setLoginId("1");
        usersEntity.setUserDescription("test");
        usersEntity.setEmailId("hemalatha.s@htcindia.com");
        usersEntity.setUserRole(Arrays.asList(new RoleEntity()));
        usersEntity.setInstitutionId(1);
        Integer userId = 3;
        Mockito.when(mockedUserRoleController.getUserAuthUtil()).thenReturn(userAuthUtil);
        Mockito.when(mockedUserRoleController.getUserRoleService()).thenReturn(userRoleService);
        Mockito.when(mockedUserRoleController.getLogger()).thenReturn(logger);
        Mockito.when(mockedUserRoleController.getUserDetailsRepository()).thenReturn(userDetailsRepository);
        Mockito.when(mockedUserRoleController.getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_USER_ROLE_URL, (UsernamePasswordToken) session.getAttribute(RecapConstants.USER_TOKEN))).thenReturn(true);
        Mockito.when(mockedUserRoleController.getUserDetailsRepository().findByUserId(userId)).thenReturn(usersEntity);
        Mockito.when(mockedUserRoleController.getUserAuthUtil().getUserDetails(request.getSession(), RecapConstants.BARCODE_RESTRICTED_PRIVILEGE)).thenReturn(userDetailsForm);
        Mockito.when(mockedUserRoleController.deleteUserRole(userRoleForm.getSearchNetworkId(),3,request,10,1,2)).thenCallRealMethod();
        ModelAndView modelAndView = mockedUserRoleController.deleteUserRole(userRoleForm.getSearchNetworkId(),3,request,10,1,2);
        assertNotNull(modelAndView);
        assertEquals("userRolesSearch",modelAndView.getViewName());
    }

    @Test
    public void deleteUser()throws Exception{
        UserRoleForm userRoleForm = new UserRoleForm();
        userRoleForm.setSearchNetworkId("smith");
        usersSessionAttributes();
        Mockito.when(mockedUserRoleController.getUserAuthUtil()).thenReturn(userAuthUtil);
        Mockito.when(mockedUserRoleController.getUserRoleService()).thenReturn(userRoleService);
        Mockito.when(mockedUserRoleController.getLogger()).thenReturn(logger);
        Mockito.when(mockedUserRoleController.getUserDetailsRepository()).thenReturn(userDetailsRepository);
        Mockito.when(mockedUserRoleController.getUserAuthUtil().getUserDetails(session, RecapConstants.BARCODE_RESTRICTED_PRIVILEGE)).thenCallRealMethod();
        Mockito.when(mockedUserRoleController.getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_USER_ROLE_URL, (UsernamePasswordToken) session.getAttribute(RecapConstants.USER_TOKEN))).thenReturn(true);
        Mockito.when(mockedUserRoleController.deleteUser(userRoleForm,model,3,userRoleForm.getSearchNetworkId(),10,1,2,request)).thenCallRealMethod();
        ModelAndView modelAndView = mockedUserRoleController.deleteUser(userRoleForm,model,3,userRoleForm.getSearchNetworkId(),10,1,2,request);
        assertNotNull(modelAndView);
        assertEquals("userRolesSearch",modelAndView.getViewName());
    }

    @Test
    public void searchFirstPage()throws Exception{
        usersSessionAttributes();
        UserRoleForm userRoleForm = new UserRoleForm();
        userRoleForm.setPageSize(10);
        userRoleForm.setPageNumber(1);
        Page<UsersEntity> usersEntityPage = getPage();
        boolean superAdmin = true;
        Mockito.when(mockedUserRoleController.getUserAuthUtil()).thenReturn(userAuthUtil);
        Mockito.when(mockedUserRoleController.getUserRoleService()).thenReturn(userRoleService);
        Mockito.when(mockedUserRoleController.getLogger()).thenReturn(logger);
        Mockito.when(mockedUserRoleController.getUserDetailsRepository()).thenReturn(userDetailsRepository);
        Mockito.when(mockedUserRoleController.getUserAuthUtil().getUserDetails(session, RecapConstants.BARCODE_RESTRICTED_PRIVILEGE)).thenCallRealMethod();
        Mockito.when(mockedUserRoleController.getUserRoleService().searchUsers(userRoleForm, superAdmin)).thenReturn(usersEntityPage);
        Mockito.when(mockedUserRoleController.getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_USER_ROLE_URL, (UsernamePasswordToken) session.getAttribute(RecapConstants.USER_TOKEN))).thenReturn(false);
        Mockito.when(mockedUserRoleController.searchFirstPage(userRoleForm, model, request)).thenCallRealMethod();
        ModelAndView modelAndView = mockedUserRoleController.searchFirstPage(userRoleForm, model, request);
        assertNotNull(modelAndView);
        assertEquals("login",modelAndView.getViewName());
    }

    @Test
    public void searchNextPage()throws Exception{
        usersSessionAttributes();
        UserRoleForm userRoleForm = new UserRoleForm();
        userRoleForm.setSearchNetworkId("smith");
        userRoleForm.setPageSize(10);
        userRoleForm.setPageNumber(1);
        Mockito.when(mockedUserRoleController.getUserAuthUtil()).thenReturn(userAuthUtil);
        Mockito.when(mockedUserRoleController.getUserRoleService()).thenReturn(userRoleService);
        Mockito.when(mockedUserRoleController.getLogger()).thenReturn(logger);
        Mockito.when(mockedUserRoleController.getUserDetailsRepository()).thenReturn(userDetailsRepository);
        Mockito.when(mockedUserRoleController.getUserAuthUtil().getUserDetails(session, RecapConstants.BARCODE_RESTRICTED_PRIVILEGE)).thenCallRealMethod();
        Mockito.when(mockedUserRoleController.getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_USER_ROLE_URL, (UsernamePasswordToken) session.getAttribute(RecapConstants.USER_TOKEN))).thenReturn(false);
        Mockito.when(mockedUserRoleController.searchNextPage(userRoleForm, model, request)).thenCallRealMethod();
        ModelAndView modelAndView = mockedUserRoleController.searchNextPage(userRoleForm, model, request);
        assertNotNull(modelAndView);
        assertEquals("login",modelAndView.getViewName());
    }


    @Test
    public void searchPreviousPage()throws Exception{
        usersSessionAttributes();
        UserRoleForm userRoleForm = new UserRoleForm();
        userRoleForm.setSearchNetworkId("smith");
        userRoleForm.setPageSize(10);
        userRoleForm.setPageNumber(1);
        Mockito.when(mockedUserRoleController.getUserAuthUtil()).thenReturn(userAuthUtil);
        Mockito.when(mockedUserRoleController.getUserRoleService()).thenReturn(userRoleService);
        Mockito.when(mockedUserRoleController.getLogger()).thenReturn(logger);
        Mockito.when(mockedUserRoleController.getUserDetailsRepository()).thenReturn(userDetailsRepository);
        Mockito.when(mockedUserRoleController.getUserAuthUtil().getUserDetails(session, RecapConstants.BARCODE_RESTRICTED_PRIVILEGE)).thenCallRealMethod();
        Mockito.when(mockedUserRoleController.getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_USER_ROLE_URL, (UsernamePasswordToken) session.getAttribute(RecapConstants.USER_TOKEN))).thenReturn(false);
        Mockito.when(mockedUserRoleController.searchPreviousPage(userRoleForm, model, request)).thenCallRealMethod();
        ModelAndView modelAndView = mockedUserRoleController.searchPreviousPage(userRoleForm,model, request);
        assertNotNull(modelAndView);
        assertEquals("login",modelAndView.getViewName());
    }

    @Test
    public void searchLastPage()throws Exception{
        usersSessionAttributes();
        UserRoleForm userRoleForm = new UserRoleForm();
        userRoleForm.setSearchNetworkId("smith");
        userRoleForm.setPageSize(10);
        userRoleForm.setPageNumber(1);
        Mockito.when(mockedUserRoleController.getUserAuthUtil()).thenReturn(userAuthUtil);
        Mockito.when(mockedUserRoleController.getUserRoleService()).thenReturn(userRoleService);
        Mockito.when(mockedUserRoleController.getLogger()).thenReturn(logger);
        Mockito.when(mockedUserRoleController.getUserDetailsRepository()).thenReturn(userDetailsRepository);
        Mockito.when(mockedUserRoleController.getUserAuthUtil().getUserDetails(session, RecapConstants.BARCODE_RESTRICTED_PRIVILEGE)).thenCallRealMethod();
        Mockito.when(mockedUserRoleController.getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_USER_ROLE_URL, (UsernamePasswordToken) session.getAttribute(RecapConstants.USER_TOKEN))).thenReturn(false);
        Mockito.when(mockedUserRoleController.searchLastPage(userRoleForm, model, request)).thenCallRealMethod();
        ModelAndView modelAndView = mockedUserRoleController.searchLastPage(userRoleForm, model, request);
        assertNotNull(modelAndView);
        assertEquals("login",modelAndView.getViewName());
    }

    @Test
    public void createUserRequest()throws Exception{
        usersSessionAttributes();
        UserRoleForm userRoleForm = new UserRoleForm();
        userRoleForm.setNetworkLoginId("test");
        userRoleForm.setEmailId("test@gmail.com");
        userRoleForm.setInstitutionId(2);
        userRoleForm.setUserDescription("testdescription");
        List<Integer> role = new ArrayList<>();
        role.add(2);
        userRoleForm.setSelectedForCreate(role);
        ModelAndView modelAndView1 = new ModelAndView();
        modelAndView1.setViewName("userRolesSearch");
        Mockito.when(mockedUserRoleController.createUserRequest(userRoleForm,request)).thenReturn(modelAndView1);
        ModelAndView modelAndView = mockedUserRoleController.createUserRequest(userRoleForm,request);
        assertNotNull(modelAndView);
        assertEquals("userRolesSearch",modelAndView.getViewName());
    }

    @Test
    public void editUser()throws Exception{
        usersSessionAttributes();
        ModelAndView modelAndView1 = new ModelAndView();
        modelAndView1.setViewName("userRolesSearch");
        Mockito.when(mockedUserRoleController.editUser(3, "smith", request)).thenReturn(modelAndView1);
        ModelAndView modelAndView = mockedUserRoleController.editUser(3, "smith", request);
        assertNotNull(modelAndView);
        assertEquals("userRolesSearch",modelAndView.getViewName());
    }

    @Test
    public void saveEditUserDetails()throws Exception{
        usersSessionAttributes();
        String[] roleIds = {"2","3"};
        when(request.getParameterValues("roleIds[]")).thenReturn(roleIds);
        ModelAndView modelAndView1 = new ModelAndView();
        modelAndView1.setViewName("userRolesSearch");
        Mockito.when(mockedUserRoleController.saveEditUserDetails(3, "test", "test description", 2, "test@mail.com", request)).thenReturn(modelAndView1);
        ModelAndView modelAndView = mockedUserRoleController.saveEditUserDetails(3, "test", "test description", 2, "test@mail.com", request);
        assertNotNull(modelAndView);
        assertEquals("userRolesSearch",modelAndView.getViewName());

    }

    private void usersSessionAttributes() throws Exception {
        when(request.getSession()).thenReturn(session);
        UserForm userForm = new UserForm();
        userForm.setUsername("kholi");
        userForm.setInstitution("3");
        userForm.setPassword("12345");
        UsernamePasswordToken token=new UsernamePasswordToken(userForm.getUsername()+ RecapConstants.TOKEN_SPLITER +userForm.getInstitution(),userForm.getPassword(),true);
        userAuthUtil.doAuthentication(token);
        when(session.getAttribute(RecapConstants.USER_TOKEN)).thenReturn(token);
        when(session.getAttribute(RecapConstants.USER_ID)).thenReturn(3);
        when(session.getAttribute(RecapConstants.SUPER_ADMIN_USER)).thenReturn(false);
        when(session.getAttribute(RecapConstants.BARCODE_RESTRICTED_PRIVILEGE)).thenReturn(false);
        when(session.getAttribute(RecapConstants.USER_INSTITUTION)).thenReturn(1);
        when(session.getAttribute(RecapConstants.REQUEST_ALL_PRIVILEGE)).thenReturn(false);
        userAuthUtil.getUserDetails(session,RecapConstants.BARCODE_RESTRICTED_PRIVILEGE);
    }

    public List<UsersEntity> getUsersEntity(){
        UsersEntity usersEntity = new UsersEntity();
        List<UsersEntity> usersEntityList = new ArrayList<>();
        usersEntity.setUserId(2);
        usersEntity.setLoginId("1");
        usersEntity.setUserRole(Arrays.asList(new RoleEntity()));
        usersEntityList.add(usersEntity);
        return usersEntityList;
    }

    public Page getPage(){
        Page<UsersEntity> page = new Page<UsersEntity>() {
            @Override
            public int getTotalPages() {
                return 0;
            }

            @Override
            public long getTotalElements() {
                return 2;
            }

            @Override
            public <S> Page<S> map(Converter<? super UsersEntity, ? extends S> converter) {
                return null;
            }

            @Override
            public int getNumber() {
                return 0;
            }

            @Override
            public int getSize() {
                return 0;
            }

            @Override
            public int getNumberOfElements() {
                return 0;
            }

            @Override
            public List<UsersEntity> getContent() {
                return getUsersEntity();
            }

            @Override
            public boolean hasContent() {
                return false;
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public Pageable nextPageable() {
                return null;
            }

            @Override
            public Pageable previousPageable() {
                return null;
            }

            @Override
            public Iterator<UsersEntity> iterator() {
                return null;
            }
        };
        return page;
    }

}
