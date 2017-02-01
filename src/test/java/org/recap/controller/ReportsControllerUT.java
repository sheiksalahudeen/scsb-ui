package org.recap.controller;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.recap.RecapConstants;
import org.recap.model.search.DeaccessionItemResultsRow;
import org.recap.model.search.ReportsForm;
import org.recap.model.userManagement.UserForm;
import org.recap.repository.jpa.RequestItemDetailsRepository;
import org.recap.security.UserManagement;
import org.recap.util.ReportsUtil;
import org.recap.util.UserAuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by rajeshbabuk on 21/10/16.
 */
public class ReportsControllerUT extends BaseControllerUT {

    @Mock
    Model model;

    @Mock
    BindingResult bindingResult;

    @InjectMocks
    ReportsController reportsController;

    @Autowired
    ReportsController getReportsController;

    @Mock
    RequestItemDetailsRepository requestItemDetailsRepository;

    @Mock
    ReportsUtil reportsUtil;

    @Mock
    HttpSession session;

    @Mock
    HttpServletRequest request;

    @Autowired
    private UserAuthUtil userAuthUtil;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(getReportsController).build();
    }


    @Test
    public void reports() throws Exception{
        when(request.getSession()).thenReturn(session);
        usersSessionAttributes();
        String response = getReportsController.collection(model,request);
        assertNotNull(response);
        assertEquals("searchRecords",response);
    }

    @Test
    public void ilBdRequest() throws Exception {
        ReportsForm reportsForm = new ReportsForm();
        reportsForm.setRequestType(RecapConstants.REPORTS_REQUEST);
        reportsForm.setRequestFromDate("11/01/2016");
        reportsForm.setRequestToDate("12/01/2016");
        reportsForm.setShowBy(RecapConstants.REPORTS_IL_BD);
        ModelAndView modelAndView = reportsController.reportCounts(reportsForm,model);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(RecapConstants.SIMPLE_DATE_FORMAT_REPORTS);
        String fromDate = reportsForm.getRequestFromDate();
        String toDate = reportsForm.getRequestToDate();
        Date requestFromDate = new Date();
        Date requestToDate = new Date();
        reportsUtil.populateILBDCountsForRequest(reportsForm, requestFromDate, requestToDate);
        assertNotNull(modelAndView);
        assertEquals("searchRecords", modelAndView.getViewName());
    }

    @Test
    public void partnersRequest() throws Exception {
        ReportsForm reportsForm = new ReportsForm();
        reportsForm.setRequestType(RecapConstants.REPORTS_REQUEST);
        reportsForm.setRequestFromDate("11/01/2016");
        reportsForm.setRequestToDate("12/01/2016");
        reportsForm.setShowBy(RecapConstants.REPORTS_PARTNERS);
        ModelAndView modelAndView = reportsController.reportCounts(reportsForm,model);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(RecapConstants.SIMPLE_DATE_FORMAT_REPORTS);
        String fromDate = reportsForm.getRequestFromDate();
        String toDate = reportsForm.getRequestToDate();
        Date requestFromDate = new Date();
        Date requestToDate =new Date();
        reportsUtil.populatePartnersCountForRequest(reportsForm, requestFromDate, requestToDate);
        assertNotNull(modelAndView);
        assertEquals("searchRecords", modelAndView.getViewName());
    }

    @Test
    public void requestType() throws Exception {
        ReportsForm reportsForm = new ReportsForm();
        reportsForm.setRequestType(RecapConstants.REPORTS_REQUEST);
        reportsForm.setRequestFromDate("11/01/2016");
        reportsForm.setRequestToDate("12/01/2016");
        reportsForm.setShowBy(RecapConstants.REPORTS_REQUEST_TYPE);
        ModelAndView modelAndView = reportsController.reportCounts(reportsForm,model);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(RecapConstants.SIMPLE_DATE_FORMAT_REPORTS);
        String fromDate = reportsForm.getRequestFromDate();
        String toDate = reportsForm.getRequestToDate();
        Date requestFromDate = new Date();
        Date requestToDate = new Date();
        reportsUtil.populateRequestTypeInformation(reportsForm, requestFromDate, requestToDate);
        assertNotNull(modelAndView);
        assertEquals("searchRecords", modelAndView.getViewName());
    }

    @Test
    public void accessionDeaccessionReports() throws Exception {
        ReportsForm reportsForm = new ReportsForm();
        reportsForm.setRequestType(RecapConstants.REPORTS_REQUEST);
        reportsForm.setRequestFromDate("11/01/2016");
        reportsForm.setRequestToDate("12/01/2016");
        reportsForm.setShowBy(RecapConstants.REPORTS_ACCESSION_DEACCESSION);
        String fromDate = reportsForm.getAccessionDeaccessionFromDate();
        String toDate = reportsForm.getAccessionDeaccessionToDate();
        ModelAndView modelAndView = reportsController.reportCounts(reportsForm,model);
        reportsUtil.populateAccessionDeaccessionItemCounts(reportsForm);
        assertNotNull(modelAndView);
        assertEquals("searchRecords", modelAndView.getViewName());
    }


    @Test
    public void cgdCounts() throws Exception {
        ReportsForm reportsForm = new ReportsForm();
        ModelAndView modelAndView = reportsController.cgdCounts(reportsForm,model);
        reportsUtil.populateCGDItemCounts(reportsForm);
        assertNotNull(modelAndView);
        assertEquals("reports :: #cgdTable",modelAndView.getViewName());
    }

    @Test
    public void deaccessionInformation() throws Exception {
        ReportsForm reportsForm = new ReportsForm();
        List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = new ArrayList<>();
        ModelAndView modelAndView = reportsController.deaccessionInformation(reportsForm,model);
        when((reportsUtil.deaccessionReportFieldsInformation(reportsForm))).thenReturn(deaccessionItemResultsRowList);
        assertNotNull(deaccessionItemResultsRowList);
        assertNotNull(modelAndView);
        assertEquals("reports :: #deaccessionInformation",modelAndView.getViewName());

    }

    @Test
    public void searchPrevious() throws Exception{
        ReportsForm reportsForm = new ReportsForm();
        ModelAndView modelAndView = reportsController.searchPrevious(reportsForm,model);
        assertNotNull(modelAndView);
        assertEquals("reports :: #deaccessionInformation",modelAndView.getViewName());
    }

    @Test
    public void searchNext() throws Exception{
        ReportsForm reportsForm = new ReportsForm();
        ModelAndView modelAndView = reportsController.searchNext(reportsForm,model);
        assertNotNull(modelAndView);
        assertEquals("reports :: #deaccessionInformation",modelAndView.getViewName());
    }

    @Test
    public void searchFirst() throws Exception{
        ReportsForm reportsForm = new ReportsForm();
        ModelAndView modelAndView = reportsController.searchFirst(reportsForm,model);
        assertNotNull(modelAndView);
        assertEquals("reports :: #deaccessionInformation",modelAndView.getViewName());
    }

    @Test
    public void searchLast() throws Exception{
        ReportsForm reportsForm = new ReportsForm();
        ModelAndView modelAndView = reportsController.searchLast(reportsForm,model);
        assertNotNull(modelAndView);
        assertEquals("reports :: #deaccessionInformation",modelAndView.getViewName());
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
    }
}
