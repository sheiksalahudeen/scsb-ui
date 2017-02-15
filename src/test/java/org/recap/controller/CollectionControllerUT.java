package org.recap.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.RecapConstants;
import org.recap.model.search.BibliographicMarcForm;
import org.recap.model.search.CollectionForm;
import org.recap.model.userManagement.UserDetailsForm;
import org.recap.model.userManagement.UserForm;
import org.recap.security.UserManagement;
import org.recap.util.CollectionServiceUtil;
import org.recap.util.MarcRecordViewUtil;
import org.recap.util.UserAuthUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Created by rajeshbabuk on 12/10/16.
 */
public class CollectionControllerUT extends BaseControllerUT {

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.shiro}")
    String scsbShiro;

    public String getServerProtocol() {
        return serverProtocol;
    }

    public void setServerProtocol(String serverProtocol) {
        this.serverProtocol = serverProtocol;
    }

    public String getScsbShiro() {
        return scsbShiro;
    }

    public void setScsbShiro(String scsbShiro) {
        this.scsbShiro = scsbShiro;
    }

    @Mock
    Model model;

    @Mock
    BindingResult bindingResult;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    CollectionController getCollectionController;

    @Mock
    MarcRecordViewUtil marcRecordViewUtil;

    @Mock
    HttpSession session;

    @Mock
    CollectionServiceUtil collectionServiceUtil;

    @Mock
    javax.servlet.http.HttpServletRequest request;

    @Mock
    private UserAuthUtil userAuthUtil;

    public UserAuthUtil getUserAuthUtil() {
        return userAuthUtil;
    }

    public void setUserAuthUtil(UserAuthUtil userAuthUtil) {
        this.userAuthUtil = userAuthUtil;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(getCollectionController).build();
    }



    @Test
    public void collection() throws Exception{
        when(request.getSession()).thenReturn(session);
        Mockito.when(getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_COLLECTION_URL,(UsernamePasswordToken)session.getAttribute(UserManagement.USER_TOKEN))).thenReturn(true);
        Mockito.when(getCollectionController.getUserAuthUtil()).thenReturn(userAuthUtil);
        Mockito.when(getCollectionController.collection(model,request)).thenCallRealMethod();
        String response = getCollectionController.collection(model,request);
        assertNotNull(response);
        assertEquals("searchRecords",response);
    }

    @Test
    public void displayRecords() throws Exception {
        CollectionForm collectionForm = new CollectionForm();
        collectionForm.setItemBarcodes("");
        Mockito.when(getCollectionController.displayRecords(collectionForm, bindingResult, model)).thenCallRealMethod();
        ModelAndView modelAndView = getCollectionController.displayRecords(collectionForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords", modelAndView.getViewName());
    }

    @Test
    public void openMarcRecord() throws Exception {
        CollectionForm collectionForm = new CollectionForm();
        UserDetailsForm userDetailsForm= new UserDetailsForm();
        userDetailsForm.setSuperAdmin(false);
        userDetailsForm.setLoginInstitutionId(2);
        userDetailsForm.setRecapUser(false);
        when(request.getSession()).thenReturn(session);
        usersSessionAttributes();
        Mockito.when(getUserAuthUtil().getUserDetails(request.getSession(),UserManagement.BARCODE_RESTRICTED_PRIVILEGE)).thenReturn(userDetailsForm);
        Mockito.when(getCollectionController.getUserAuthUtil()).thenReturn(userAuthUtil);
        Mockito.when(getCollectionController.getMarcRecordViewUtil()).thenReturn(marcRecordViewUtil);
        Mockito.when(getCollectionController.openMarcView(collectionForm, bindingResult, model,request)).thenCallRealMethod();
        Mockito.when(marcRecordViewUtil.buildBibliographicMarcForm(collectionForm.getBibId(), collectionForm.getItemId(),userDetailsForm)).thenReturn(new BibliographicMarcForm());
        ModelAndView modelAndView = getCollectionController.openMarcView(collectionForm, bindingResult, model,request);
        assertNotNull(modelAndView);
        assertEquals("collection :: #collectionUpdateModal", modelAndView.getViewName());
    }

    @Test
    public void collectionUpdate() throws Exception {
        CollectionForm collectionForm = new CollectionForm();
        Mockito.when(getCollectionController.getCollectionServiceUtil()).thenReturn(collectionServiceUtil);
        Mockito.when(getCollectionController.collectionUpdate(collectionForm, bindingResult, model)).thenCallRealMethod();
        ModelAndView modelAndView = getCollectionController.collectionUpdate(collectionForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("collection :: #itemDetailsSection", modelAndView.getViewName());
    }

    private void usersSessionAttributes() throws Exception {
        when(request.getSession()).thenReturn(session);
        UserForm userForm = new UserForm();
        userForm.setUsername("SuperAdmin");
        userForm.setInstitution(1);
        userForm.setPassword("12345");
        UsernamePasswordToken token=new UsernamePasswordToken(userForm.getUsername()+ UserManagement.TOKEN_SPLITER.getValue()+userForm.getInstitution(),userForm.getPassword(),true);
        when(session.getAttribute(UserManagement.USER_TOKEN)).thenReturn(token);
        when(session.getAttribute(UserManagement.USER_ID)).thenReturn(3);
        when(session.getAttribute(UserManagement.SUPER_ADMIN_USER)).thenReturn(false);
        when(session.getAttribute(UserManagement.BARCODE_RESTRICTED_PRIVILEGE)).thenReturn(false);
        when(session.getAttribute(UserManagement.REQUEST_ITEM_PRIVILEGE)).thenReturn(false);
        when(session.getAttribute(UserManagement.USER_INSTITUTION)).thenReturn(1);
        when(session.getAttribute(UserManagement.REQUEST_ALL_PRIVILEGE)).thenReturn(false);
    }
}
