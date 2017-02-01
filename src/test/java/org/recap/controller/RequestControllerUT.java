package org.recap.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.recap.model.jpa.RequestItemEntity;
import org.recap.model.search.RequestForm;
import org.recap.model.userManagement.UserForm;
import org.recap.repository.jpa.CustomerCodeDetailsRepository;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.repository.jpa.RequestTypeDetailsRepository;
import org.recap.security.UserManagement;
import org.recap.util.RequestServiceUtil;
import org.recap.util.UserAuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Created by rajeshbabuk on 21/10/16.
 */
public class RequestControllerUT extends BaseControllerUT {
    @Mock
    Model model;

    @Mock
    BindingResult bindingResult;

    @InjectMocks
    RequestController requestController;

    @Autowired
    RequestController getRequestController;

    @Mock
    RequestServiceUtil requestServiceUtil;

    @Mock
    InstitutionDetailsRepository institutionDetailsRepository;

    @Mock
    RequestTypeDetailsRepository requestTypeDetailsRepository;

    @Mock
    CustomerCodeDetailsRepository customerCodeDetailsRepository;

    @Mock
    HttpSession session;

    @Mock
    javax.servlet.http.HttpServletRequest request;

    @Autowired
    private UserAuthUtil userAuthUtil;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(getRequestController).build();
    }


    @Test
    public void request() throws Exception{
        when(request.getSession()).thenReturn(session);
        usersSessionAttributes();
        String response = getRequestController.collection(model,request);
        assertNotNull(response);
        assertEquals("searchRecords",response);
    }

    @Test
    public void searchRequests() throws Exception {
        RequestForm requestForm = new RequestForm();
        Page<RequestItemEntity> requestItemEntities = new PageImpl<RequestItemEntity>(new ArrayList<>());
        when(requestServiceUtil.searchRequests(requestForm)).thenReturn(requestItemEntities);
        ModelAndView modelAndView = requestController.searchRequests(requestForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("request", modelAndView.getViewName());
    }

    @Test
    public void searchPrevious() throws Exception {
        RequestForm requestForm = new RequestForm();
        Page<RequestItemEntity> requestItemEntities = new PageImpl<RequestItemEntity>(new ArrayList<>());
        when(requestServiceUtil.searchRequests(requestForm)).thenReturn(requestItemEntities);
        ModelAndView modelAndView = requestController.searchPrevious(requestForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("request", modelAndView.getViewName());
    }

    @Test
    public void searchNext() throws Exception {
        RequestForm requestForm = new RequestForm();
        Page<RequestItemEntity> requestItemEntities = new PageImpl<RequestItemEntity>(new ArrayList<>());
        when(requestServiceUtil.searchRequests(requestForm)).thenReturn(requestItemEntities);
        ModelAndView modelAndView = requestController.searchNext(requestForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("request", modelAndView.getViewName());
    }

    @Test
    public void searchFirst() throws Exception {
        RequestForm requestForm = new RequestForm();
        Page<RequestItemEntity> requestItemEntities = new PageImpl<RequestItemEntity>(new ArrayList<>());
        when(requestServiceUtil.searchRequests(requestForm)).thenReturn(requestItemEntities);
        ModelAndView modelAndView = requestController.searchFirst(requestForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("request", modelAndView.getViewName());
    }

    @Test
    public void searchLast() throws Exception {
        RequestForm requestForm = new RequestForm();
        Page<RequestItemEntity> requestItemEntities = new PageImpl<RequestItemEntity>(new ArrayList<>());
        when(requestServiceUtil.searchRequests(requestForm)).thenReturn(requestItemEntities);
        ModelAndView modelAndView = requestController.searchLast(requestForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("request", modelAndView.getViewName());
    }

    @Test
    public void loadCreateRequest() throws Exception {

        when(institutionDetailsRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        when(requestTypeDetailsRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        when(customerCodeDetailsRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        when(request.getSession()).thenReturn(session);
        usersSessionAttributes();
        ModelAndView modelAndView = getRequestController.loadCreateRequest(model,request);
        assertNotNull(modelAndView);
        assertEquals("request", modelAndView.getViewName());
    }

    @Test
    public void populateItem() throws Exception {
        RequestForm requestForm = new RequestForm();
        String response = requestController.populateItem(requestForm, bindingResult, model,null);
        assertNotNull(response);
    }

    private void usersSessionAttributes() throws Exception {
        when(request.getSession()).thenReturn(session);
        UserForm userForm = new UserForm();
        userForm.setUsername("SuperAdmin");
        userForm.setInstitution(1);
        userForm.setPassword("12345");
        UsernamePasswordToken token=new UsernamePasswordToken(userForm.getUsername()+ UserManagement.TOKEN_SPLITER.getValue()+userForm.getInstitution(),userForm.getPassword(),true);
        userAuthUtil.doAuthentication(token);
        when(session.getAttribute(UserManagement.USER_TOKEN)).thenReturn(token);
        when(session.getAttribute(UserManagement.USER_ID)).thenReturn(3);
        when(session.getAttribute(UserManagement.SUPER_ADMIN_USER)).thenReturn(false);
        when(session.getAttribute(UserManagement.BARCODE_RESTRICTED_PRIVILEGE)).thenReturn(false);
        when(session.getAttribute(UserManagement.REQUEST_ITEM_PRIVILEGE)).thenReturn(false);
        when(session.getAttribute(UserManagement.USER_INSTITUTION)).thenReturn(1);
        when(session.getAttribute(UserManagement.REQUEST_ALL_PRIVILEGE)).thenReturn(false);
        userAuthUtil.getUserDetails(session,UserManagement.BARCODE_RESTRICTED_PRIVILEGE);
    }
}
