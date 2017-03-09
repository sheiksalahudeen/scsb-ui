package org.recap.controller;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.RecapConstants;
import org.recap.model.jpa.*;
import org.recap.model.request.CancelRequestResponse;
import org.recap.model.request.ItemRequestInformation;
import org.recap.model.request.ItemResponseInformation;
import org.recap.model.search.RequestForm;
import org.recap.model.userManagement.UserDetailsForm;
import org.recap.repository.jpa.*;
import org.recap.security.UserManagement;
import org.recap.util.RequestServiceUtil;
import org.recap.util.UserAuthUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.Thread;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by rajeshbabuk on 21/10/16.
 */
public class RequestControllerUT extends BaseControllerUT {


    @Mock
    BindingAwareModelMap model;

    @Mock
    BindingResult bindingResult;

    @Mock
    RequestController requestController;

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

    @Mock
    private UserAuthUtil userAuthUtil;

    @Mock
    ItemDetailsRepository itemDetailsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Mock
    RestTemplate restTemplate;

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.url}")
    String scsbUrl;

    @Value("${scsb.shiro}")
    String scsbShiro;

    @Mock
    RequestItemDetailsRepository requestItemDetailsRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(requestController).build();
    }

    public BindingAwareModelMap getModel() {
        return model;
    }

    public String getServerProtocol() {
        return serverProtocol;
    }

    public String getScsbUrl() {
        return scsbUrl;
    }

    public String getScsbShiro() {
        return scsbShiro;
    }

    @Test
    public void request() throws Exception{
        when(request.getSession()).thenReturn(session);
        Mockito.when(userAuthUtil.authorizedUser(RecapConstants.SCSB_SHIRO_REQUEST_URL,(UsernamePasswordToken)session.getAttribute(UserManagement.USER_TOKEN))).thenReturn(true);
        Mockito.when(requestController.getUserAuthUtil()).thenReturn(userAuthUtil);
        Mockito.when(requestController.getInstitutionDetailsRepository()).thenReturn(institutionDetailsRepository);
        Mockito.when(requestController.getCustomerCodeDetailsRepository()).thenReturn(customerCodeDetailsRepository);
        Mockito.when(requestController.getRequestTypeDetailsRepository()).thenReturn(requestTypeDetailsRepository);
        when(institutionDetailsRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        when(requestTypeDetailsRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        when(customerCodeDetailsRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        when(((BindingAwareModelMap) model).get("requestedBarcode")).thenReturn("test");
        when(requestController.populateItem(new RequestForm(), null, model,request)).thenReturn("");
        when(requestController.request(model,request)).thenCallRealMethod();
        String response = requestController.request(model,request);
        assertNotNull(response);
        assertEquals("searchRecords",response);
    }

    @Test
    public void searchRequests() throws Exception {
        RequestForm requestForm = new RequestForm();
        Page<RequestItemEntity> requestItemEntities = new PageImpl<RequestItemEntity>(new ArrayList<>());
        when(requestController.getRequestServiceUtil()).thenReturn(requestServiceUtil);
        when(requestServiceUtil.searchRequests(requestForm)).thenReturn(requestItemEntities);
        when(requestController.searchRequests(requestForm, bindingResult, model)).thenCallRealMethod();
        ModelAndView modelAndView = requestController.searchRequests(requestForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("request :: #searchRequestsSection", modelAndView.getViewName());
    }

    @Test
    public void searchPrevious() throws Exception {
        RequestForm requestForm = new RequestForm();
        Page<RequestItemEntity> requestItemEntities = new PageImpl<RequestItemEntity>(new ArrayList<>());
        when(requestController.getRequestServiceUtil()).thenReturn(requestServiceUtil);
        when(requestServiceUtil.searchRequests(requestForm)).thenReturn(requestItemEntities);
        when(requestController.searchPrevious(requestForm, bindingResult, model)).thenCallRealMethod();
        ModelAndView modelAndView = requestController.searchPrevious(requestForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("request :: #searchRequestsSection", modelAndView.getViewName());
    }

    @Test
    public void searchNext() throws Exception {
        RequestForm requestForm = new RequestForm();
        Page<RequestItemEntity> requestItemEntities = new PageImpl<RequestItemEntity>(new ArrayList<>());
        when(requestController.getRequestServiceUtil()).thenReturn(requestServiceUtil);
        when(requestServiceUtil.searchRequests(requestForm)).thenReturn(requestItemEntities);
        when(requestController.searchNext(requestForm, bindingResult, model)).thenCallRealMethod();
        ModelAndView modelAndView = requestController.searchNext(requestForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("request :: #searchRequestsSection", modelAndView.getViewName());
    }

    @Test
    public void searchFirst() throws Exception {
        RequestForm requestForm = new RequestForm();
        Page<RequestItemEntity> requestItemEntities = new PageImpl<RequestItemEntity>(new ArrayList<>());
        when(requestController.getRequestServiceUtil()).thenReturn(requestServiceUtil);
        when(requestServiceUtil.searchRequests(requestForm)).thenReturn(requestItemEntities);
        when(requestController.searchFirst(requestForm, bindingResult, model)).thenCallRealMethod();
        ModelAndView modelAndView = requestController.searchFirst(requestForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("request :: #searchRequestsSection", modelAndView.getViewName());
    }

    @Test
    public void searchLast() throws Exception {
        RequestForm requestForm = new RequestForm();
        Page<RequestItemEntity> requestItemEntities = new PageImpl<RequestItemEntity>(new ArrayList<>());
        when(requestController.getRequestServiceUtil()).thenReturn(requestServiceUtil);
        when(requestServiceUtil.searchRequests(requestForm)).thenReturn(requestItemEntities);
        when(requestController.searchLast(requestForm, bindingResult, model)).thenCallRealMethod();
        ModelAndView modelAndView = requestController.searchLast(requestForm, bindingResult, model);
        assertNotNull(modelAndView);
        assertEquals("request :: #searchRequestsSection", modelAndView.getViewName());
    }

    @Test
    public void loadCreateRequest() throws Exception {
        Mockito.when(requestController.getInstitutionDetailsRepository()).thenReturn(institutionDetailsRepository);
        Mockito.when(requestController.getCustomerCodeDetailsRepository()).thenReturn(customerCodeDetailsRepository);
        Mockito.when(requestController.getRequestTypeDetailsRepository()).thenReturn(requestTypeDetailsRepository);
        Mockito.when(requestController.getUserAuthUtil()).thenReturn(userAuthUtil);

        when(institutionDetailsRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        when(requestTypeDetailsRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        when(customerCodeDetailsRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        when(userAuthUtil.getUserDetails(request.getSession(),UserManagement.REQUEST_ITEM_PRIVILEGE)).thenReturn(getUserDetails());

        when(requestController.loadCreateRequest(model,request)).thenCallRealMethod();
        when(request.getSession()).thenReturn(session);
        ModelAndView modelAndView = requestController.loadCreateRequest(model,request);
        assertNotNull(modelAndView);
        assertEquals("request", modelAndView.getViewName());
    }

    @Test
    public void populateItem() throws Exception {
        RequestForm requestForm = new RequestForm();
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsSingleItem();
        String barcode = bibliographicEntity.getItemEntities().get(0).getBarcode();
        requestForm.setItemBarcodeInRequest(barcode);
        Mockito.when(requestController.getItemDetailsRepository()).thenReturn(itemDetailsRepository);
        Mockito.when(requestController.getUserAuthUtil()).thenReturn(userAuthUtil);
        when(request.getSession()).thenReturn(session);
        Mockito.when(requestController.getUserAuthUtil().getUserDetails(request.getSession(),UserManagement.REQUEST_PRIVILEGE)).thenReturn(getUserDetails());
        Mockito.when(requestController.getItemDetailsRepository().findByBarcodeAndIsDeletedFalse(barcode)).thenReturn(Arrays.asList(bibliographicEntity.getItemEntities().get(0)));
        when(requestController.populateItem(requestForm, bindingResult, model,request)).thenCallRealMethod();
        String response = requestController.populateItem(requestForm, bindingResult, model,request);
        assertNotNull(response);
        assertTrue(response.contains("User is not permitted to request private item(s)"));
    }

    @Test
    public void testCreateRequest() throws Exception {
        RequestForm requestForm = getRequestForm();
        ResponseEntity responseEntity = new ResponseEntity(RecapConstants.VALID_REQUEST,HttpStatus.OK);
        ResponseEntity responseEntity1 = new ResponseEntity<ItemResponseInformation>(getItemResponseInformation(),HttpStatus.OK);
        when(request.getSession()).thenReturn(session);
        ItemRequestInformation itemRequestInformation = getItemRequestInformation();
        HttpEntity<ItemRequestInformation> requestEntity = new HttpEntity<>(itemRequestInformation, getHttpHeaders());
        String validateRequestItemUrl = getServerProtocol() + getScsbUrl() + RecapConstants.VALIDATE_REQUEST_ITEM_URL;
        String requestItemUrl = serverProtocol + scsbUrl + RecapConstants.REQUEST_ITEM_URL;
        CustomerCodeEntity customerCodeEntity = new CustomerCodeEntity();
        customerCodeEntity.setCustomerCode("PB");
        Mockito.when(requestController.getItemRequestInformation()).thenReturn(itemRequestInformation);
        Mockito.when((String) session.getAttribute(UserManagement.USER_NAME)).thenReturn("Admin");
        Mockito.when(requestController.getRestTemplate()).thenReturn(restTemplate);
        Mockito.when(requestController.getServerProtocol()).thenReturn(serverProtocol);
        Mockito.when(requestController.getScsbShiro()).thenReturn(scsbShiro);
        Mockito.when(requestController.getScsbUrl()).thenReturn(scsbUrl);
        Mockito.when(requestController.getCustomerCodeDetailsRepository()).thenReturn(customerCodeDetailsRepository);
        Mockito.when(requestController.getCustomerCodeDetailsRepository().findByDescription(requestForm.getDeliveryLocationInRequest())).thenReturn(customerCodeEntity);
        Mockito.when(requestController.getRestTemplate().exchange(requestItemUrl, HttpMethod.POST, requestEntity, ItemResponseInformation.class)).thenReturn(responseEntity1);
        Mockito.when(requestController.getRestTemplate().exchange(validateRequestItemUrl, HttpMethod.POST, requestEntity, String.class)).thenReturn(responseEntity);
        Mockito.when(requestController.createRequest(requestForm,bindingResult,model,request)).thenCallRealMethod();
        String response = requestController.createRequest(requestForm,bindingResult,model,request);
        assertNotNull(response);
    }

    @Test
    public void testCancelRequest() throws Exception {
        RequestForm requestForm = getRequestForm();
        HttpEntity requestEntity = new HttpEntity<>(getHttpHeaders());
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverProtocol + scsbUrl + RecapConstants.URL_REQUEST_CANCEL).queryParam(RecapConstants.REQUEST_ID, requestForm.getRequestId());
        CancelRequestResponse cancelRequestResponse = new CancelRequestResponse();
        cancelRequestResponse.setSuccess(true);
        cancelRequestResponse.setScreenMessage("Request cancelled.");
        ResponseEntity<CancelRequestResponse> responseEntity = new ResponseEntity<CancelRequestResponse>(cancelRequestResponse,HttpStatus.OK);
        RequestItemEntity requestItemEntity = new RequestItemEntity();
        RequestStatusEntity requestStatusEntity = new RequestStatusEntity();
        requestStatusEntity.setRequestStatusDescription("Cancelled");
        requestItemEntity.setRequestStatusEntity(requestStatusEntity);
        Mockito.when(requestController.getRestTemplate()).thenReturn(restTemplate);
        Mockito.when(requestController.getServerProtocol()).thenReturn(serverProtocol);
        Mockito.when(requestController.getScsbShiro()).thenReturn(scsbShiro);
        Mockito.when(requestController.getScsbUrl()).thenReturn(scsbUrl);
        Mockito.when(requestController.getRequestItemDetailsRepository()).thenReturn(requestItemDetailsRepository);
        Mockito.when(requestController.getRequestItemDetailsRepository().findByRequestId(requestForm.getRequestId())).thenReturn(requestItemEntity);
        Mockito.when(requestController.getRestTemplate().exchange(builder.build().encode().toUri(), HttpMethod.POST, requestEntity, CancelRequestResponse.class)).thenReturn(responseEntity);
        Mockito.when(requestController.cancelRequest(requestForm,bindingResult,model)).thenCallRealMethod();
        String response = requestController.cancelRequest(requestForm,bindingResult,model);
        assertNotNull(response);
        assertTrue(response.contains("Request cancelled."));
    }

    private RequestForm getRequestForm(){
        RequestForm requestForm = new RequestForm();
        requestForm.setItemBarcodeInRequest("123");
        requestForm.setPatronBarcodeInRequest("46259871");
        requestForm.setRequestingInstitution("PUL");
        requestForm.setPatronEmailAddress("hemalatha.s@htcindia.com");
        requestForm.setItemTitle("test");
        requestForm.setItemOwningInstitution("PUL");
        requestForm.setRequestType("recall");
        requestForm.setRequestNotes("test");
        requestForm.setStartPage(2);
        requestForm.setEndPage(5);
        requestForm.setArticleAuthor("john");
        requestForm.setArticleTitle("test");
        requestForm.setDeliveryLocationInRequest("PB");
        return requestForm;
    }

    private ItemRequestInformation getItemRequestInformation(){
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setUsername("Admin");
        itemRequestInformation.setItemBarcodes(Arrays.asList("123"));
        itemRequestInformation.setPatronBarcode("46259871");
        itemRequestInformation.setRequestingInstitution("PUL");
        itemRequestInformation.setEmailAddress("hemalatha.s@htcindia.com");
        itemRequestInformation.setTitle("test");
        itemRequestInformation.setTitleIdentifier("test");
        itemRequestInformation.setItemOwningInstitution("PUL");
        itemRequestInformation.setRequestType("recall");
        itemRequestInformation.setRequestNotes("test");
        itemRequestInformation.setStartPage(2);
        itemRequestInformation.setEndPage(5);
        itemRequestInformation.setAuthor("john");
        itemRequestInformation.setChapterTitle("test");
        itemRequestInformation.setDeliveryLocation("PB");
        return itemRequestInformation;
    }

    private ItemResponseInformation getItemResponseInformation(){
        ItemResponseInformation itemResponseInformation = new ItemResponseInformation();
        itemResponseInformation.setPatronBarcode("46259871");
        itemResponseInformation.setItemBarcode("123");
        itemResponseInformation.setSuccess(true);
        return itemResponseInformation;
    }

    private UserDetailsForm getUserDetails(){
        UserDetailsForm userDetailsForm=new UserDetailsForm();
        userDetailsForm.setLoginInstitutionId(2);
        userDetailsForm.setSuperAdmin(false);
        userDetailsForm.setRecapPermissionAllowed(false);
        userDetailsForm.setRecapUser(false);
        return userDetailsForm;
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
        itemEntity.setCollectionGroupId(3);
        itemEntity.setCallNumberType("1");
        itemEntity.setCustomerCode("123");
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("tst");
        itemEntity.setLastUpdatedBy("tst");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        itemEntity.setBibliographicEntities(Arrays.asList(bibliographicEntity));
        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        return savedBibliographicEntity;

    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(RecapConstants.API_KEY, RecapConstants.RECAP);
        return headers;
    }


}
