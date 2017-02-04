package org.recap.controller;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.recap.model.search.SearchItemResultRow;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.model.search.SearchResultRow;
import org.recap.model.userManagement.UserForm;
import org.recap.security.UserManagement;
import org.recap.util.CsvUtil;
import org.recap.util.UserAuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Created by premkb on 2/8/16.
 */
public class SearchRecordsControllerUT extends BaseControllerUT{

    @Mock
    Model model;

    @Mock
    BindingResult bindingResult;

    @Mock
    HttpServletResponse httpServletResponse;

    @InjectMocks
    SearchRecordsController searchRecordsController = new SearchRecordsController();

    @Autowired
    private SearchRecordsController recordsController;

    @Mock
    private CsvUtil csvUtil;

    @Mock
    HttpSession session;

    @Mock
    HttpServletRequest request;

    @Autowired
    private UserAuthUtil userAuthUtil;

    SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
    @Before
    public void setUp() throws Exception {
        when(request.getSession()).thenReturn(session);
        usersSessionAttributes();
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(recordsController).build();
        Map searchRecordsMap = new HashMap();
    }


    @Test
    public void searchRecords() throws Exception{
        when(request.getSession()).thenReturn(session);
        usersSessionAttributes();
        String response = recordsController.searchRecords(model,request);
        assertNotNull(response);
        assertEquals("searchRecords",response);
    }

    @Test
    public void search() throws Exception{
        ModelAndView modelAndView = recordsController.search(getSearchRecordsRequest(),bindingResult,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void searchPrevious() throws Exception{
        ModelAndView modelAndView = recordsController.searchPrevious(getSearchRecordsRequest(),bindingResult,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void searchNext() throws Exception{
        ModelAndView modelAndView = recordsController.searchNext(getSearchRecordsRequest(),bindingResult,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void searchFirst() throws Exception{
        ModelAndView modelAndView = recordsController.searchFirst(getSearchRecordsRequest(),bindingResult,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void searchLast() throws Exception{
        ModelAndView modelAndView = recordsController.searchLast(getSearchRecordsRequest(),bindingResult,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void clear() throws Exception{
        ModelAndView modelAndView = searchRecordsController.clear(getSearchRecordsRequest(),bindingResult,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void newSearch() throws Exception{
        ModelAndView modelAndView = searchRecordsController.newSearch(getSearchRecordsRequest(),bindingResult,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void requestRecords() throws Exception{
        ModelAndView modelAndView = searchRecordsController.requestRecords(getSearchRecordsRequest(),bindingResult,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    @Test
    public void exportRecords() throws Exception {
        byte[] fileContent = recordsController.exportRecords(buildRequestWithResultRows(), httpServletResponse, bindingResult, model);
        assertNotNull(fileContent);
    }

    @Test
    public void onPageSizeChange() throws Exception{
        ModelAndView modelAndView = recordsController.onPageSizeChange(getSearchRecordsRequest(),bindingResult,model);
        assertNotNull(modelAndView);
        assertEquals("searchRecords",modelAndView.getViewName());
    }

    private SearchRecordsRequest getSearchRecordsRequest(){
        searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setShowResults(true);
        List<SearchResultRow> searchResultRows = new ArrayList<>();
        SearchResultRow searchResultRow = new SearchResultRow();
        searchResultRow.setTitle("Title1");
        searchResultRow.setBibId(1);
        searchResultRows.add(searchResultRow);
        searchRecordsRequest.setSearchResultRows(searchResultRows);
        List<String> availability = new ArrayList<>();
        availability.add("Available");
        searchRecordsRequest.setAvailability(availability);
        searchRecordsRequest.setFieldName("Title_search");
        searchRecordsRequest.setFieldValue("Stay Hungry");
        List<String> owningInstitutions = new ArrayList<>();
        owningInstitutions.add("NYPL");
        searchRecordsRequest.setOwningInstitutions(owningInstitutions);
        List<String> collectionGroupDesignations = new ArrayList<>();
        collectionGroupDesignations.add("Shared");
        searchRecordsRequest.setCollectionGroupDesignations(collectionGroupDesignations);
        List<String> materialTypes = new ArrayList<>();
        materialTypes.add("Monograph");
        searchRecordsRequest.setMaterialTypes(materialTypes);
        searchRecordsRequest.setTotalPageCount(1);
        searchRecordsRequest.setTotalBibRecordsCount("1");
        searchRecordsRequest.setTotalItemRecordsCount("1");
        searchRecordsRequest.setSelectAll(false);
        searchRecordsRequest.setIndex(1);
        searchRecordsRequest.setPageNumber(1);
        searchRecordsRequest.setPageSize(1);
        return searchRecordsRequest;
    }

    private SearchRecordsRequest buildRequestWithResultRows() {
        searchRecordsRequest = new SearchRecordsRequest();
        List<SearchResultRow> searchResultRows = new ArrayList<>();
        SearchResultRow searchResultRow1 = new SearchResultRow();
        searchResultRow1.setTitle("Title1");
        searchResultRow1.setAuthor("Author1");
        searchResultRow1.setPublisher("publisher1");
        searchResultRow1.setOwningInstitution("NYPL");
        searchResultRow1.setCollectionGroupDesignation("Shared");
        searchResultRow1.setSelected(true);

        SearchResultRow searchResultRow2 = new SearchResultRow();
        searchResultRow2.setTitle("Title2");
        searchResultRow2.setAuthor("Author2");
        searchResultRow2.setPublisher("publisher2");
        searchResultRow2.setOwningInstitution("NYPL");
        searchResultRow2.setCollectionGroupDesignation("Shared");
        searchResultRow2.setSelectAllItems(true);

        List<SearchItemResultRow> searchItemResultRows = new ArrayList<>();
        SearchItemResultRow searchItemResultRow1 = new SearchItemResultRow();
        searchItemResultRow1.setCallNumber("call number1");
        searchItemResultRow1.setChronologyAndEnum("Chn Enum1");
        searchItemResultRow1.setCustomerCode("Code 1");
        searchItemResultRow1.setBarcode("1");
        searchItemResultRow1.setUseRestriction("In Library use");
        searchItemResultRow1.setCollectionGroupDesignation("Shared");
        searchItemResultRow1.setAvailability("Available");
        searchItemResultRows.add(searchItemResultRow1);
        searchResultRow2.setSearchItemResultRows(searchItemResultRows);

        searchResultRows.add(searchResultRow1);
        searchResultRows.add(searchResultRow2);
        searchRecordsRequest.setSearchResultRows(searchResultRows);
        return searchRecordsRequest;
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