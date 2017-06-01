package org.recap.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.recap.RecapConstants;
import org.recap.model.jpa.CustomerCodeEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.jpa.RequestItemEntity;
import org.recap.model.request.CancelRequestResponse;
import org.recap.model.request.ItemRequestInformation;
import org.recap.model.request.ItemResponseInformation;
import org.recap.model.search.RequestForm;
import org.recap.model.search.SearchResultRow;
import org.recap.model.usermanagement.UserDetailsForm;
import org.recap.repository.jpa.*;
import org.recap.security.UserManagementService;
import org.recap.service.RequestService;
import org.recap.util.RequestServiceUtil;
import org.recap.util.UserAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by rajeshbabuk on 13/10/16.
 */

@Controller
public class RequestController {

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    @Value("${scsb.url}")
    String scsbUrl;

    @Value("${scsb.shiro}")
    String scsbShiro;

    @Autowired
    RequestServiceUtil requestServiceUtil;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    RequestTypeDetailsRepository requestTypeDetailsRepository;

    @Autowired
    CustomerCodeDetailsRepository customerCodeDetailsRepository;

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    @Autowired
    RequestStatusDetailsRepository requestStatusDetailsRepository;

    @Autowired
    RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    private UserAuthUtil userAuthUtil;

    @Autowired
    RequestService requestService;

    public RequestServiceUtil getRequestServiceUtil() {
        return requestServiceUtil;
    }

    public UserAuthUtil getUserAuthUtil() {
        return userAuthUtil;
    }

    public InstitutionDetailsRepository getInstitutionDetailsRepository() {
        return institutionDetailsRepository;
    }

    public RequestTypeDetailsRepository getRequestTypeDetailsRepository() {
        return requestTypeDetailsRepository;
    }

    public CustomerCodeDetailsRepository getCustomerCodeDetailsRepository() {
        return customerCodeDetailsRepository;
    }

    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    public String getScsbShiro() {
        return scsbShiro;
    }

    public String getScsbUrl() {
        return scsbUrl;
    }

    public RequestItemDetailsRepository getRequestItemDetailsRepository() {
        return requestItemDetailsRepository;
    }

    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    public RequestStatusDetailsRepository getRequestStatusDetailsRepository() {
        return requestStatusDetailsRepository;
    }

    public RequestService getRequestService() {
        return requestService;
    }

    @RequestMapping("/request")
    public String request(Model model, HttpServletRequest request) throws JSONException {
        HttpSession session = request.getSession(false);
        boolean authenticated = getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_REQUEST_URL, (UsernamePasswordToken) session.getAttribute(RecapConstants.USER_TOKEN));
        if (authenticated) {
            UserDetailsForm userDetailsForm = getUserAuthUtil().getUserDetails(session, RecapConstants.REQUEST_PRIVILEGE);
            RequestForm requestForm = getRequestService().setFormDetailsForRequest(model, request, userDetailsForm);
            model.addAttribute(RecapConstants.REQUEST_FORM, requestForm);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
            return RecapConstants.VIEW_SEARCH_RECORDS;
        } else {
            return UserManagementService.unAuthorizedUser(session, "Request", logger);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=searchRequests")
    public ModelAndView searchRequests(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                       BindingResult result,
                                       Model model) {
        try {
            requestForm.resetPageNumber();
            searchAndSetResults(requestForm);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        } catch (Exception exception) {
            logger.error(RecapConstants.LOG_ERROR, exception);
            logger.debug(exception.getMessage());
        }
        return new ModelAndView(RecapConstants.VIEW_SEARCH_REQUESTS_SECTION, RecapConstants.REQUEST_FORM, requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request/goToSearchRequest", method = RequestMethod.GET)
    public ModelAndView goToSearchRequest(@Valid @ModelAttribute("requestForm") RequestForm requestForm,String patronBarcodeInRequest,
                                       BindingResult result,
                                       Model model) {
        try {
            requestForm.resetPageNumber();
            requestForm.setPatronBarcode(patronBarcodeInRequest);
            List<String> requestStatuses = new ArrayList<>();
            List<String> institutionList = new ArrayList<>();
            getRequestService().findAllRequestStatusExceptProcessing(requestStatuses);
            requestForm.setRequestStatuses(requestStatuses);
            getRequestService().getInstitutionForSuperAdmin(institutionList);
            requestForm.setInstitutionList(institutionList);
            requestForm.setStatus("");
            searchAndSetResults(requestForm);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        } catch (Exception exception) {
            logger.error(RecapConstants.LOG_ERROR, exception);
            logger.debug(exception.getMessage());
        }
        return new ModelAndView("request :: #requestContentId", RecapConstants.REQUEST_FORM, requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=first")
    public ModelAndView searchFirst(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                    BindingResult result,
                                    Model model) {
        requestForm.resetPageNumber();
        searchAndSetResults(requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_REQUESTS_SECTION, RecapConstants.REQUEST_FORM, requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=last")
    public ModelAndView searchLast(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                   BindingResult result,
                                   Model model) {
        requestForm.setPageNumber(requestForm.getTotalPageCount() - 1);
        searchAndSetResults(requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_REQUESTS_SECTION, RecapConstants.REQUEST_FORM, requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=previous")
    public ModelAndView searchPrevious(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                       BindingResult result,
                                       Model model) {
        searchAndSetResults(requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_REQUESTS_SECTION, RecapConstants.REQUEST_FORM, requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=next")
    public ModelAndView searchNext(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                   BindingResult result,
                                   Model model) {
        searchAndSetResults(requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_REQUESTS_SECTION, RecapConstants.REQUEST_FORM, requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=requestPageSizeChange")
    public ModelAndView onRequestPageSizeChange(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                                BindingResult result,
                                                Model model) {
        requestForm.setPageNumber(getPageNumberOnPageSizeChange(requestForm));
        searchAndSetResults(requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_REQUESTS_SECTION, RecapConstants.REQUEST_FORM, requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=loadCreateRequest")
    public ModelAndView loadCreateRequest(Model model, HttpServletRequest request) {
        UserDetailsForm userDetailsForm = getUserAuthUtil().getUserDetails(request.getSession(false), RecapConstants.REQUEST_PRIVILEGE);
        RequestForm requestForm = getRequestService().setDefaultsToCreateRequest(userDetailsForm,model);
        model.addAttribute(RecapConstants.REQUEST_FORM, requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView(RecapConstants.REQUEST, RecapConstants.REQUEST_FORM, requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=loadCreateRequestForSamePatron")
    public ModelAndView loadCreateRequestForSamePatron(Model model, HttpServletRequest request) {
        UserDetailsForm userDetailsForm = getUserAuthUtil().getUserDetails(request.getSession(false), RecapConstants.REQUEST_PRIVILEGE);
        RequestForm requestForm = getRequestService().setDefaultsToCreateRequest(userDetailsForm,model);
        requestForm.setOnChange("true");
        model.addAttribute(RecapConstants.REQUEST_FORM, requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView(RecapConstants.REQUEST, RecapConstants.REQUEST_FORM, requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=loadSearchRequest")
    public ModelAndView loadSearchRequest(Model model, HttpServletRequest request) {
        RequestForm requestForm = new RequestForm();
        List<String> requestStatuses = new ArrayList<>();
        List<String> institutionList = new ArrayList<>();
        getRequestService().findAllRequestStatusExceptProcessing(requestStatuses);
        requestForm.setRequestStatuses(requestStatuses);
        getRequestService().getInstitutionForSuperAdmin(institutionList);
        requestForm.setInstitutionList(institutionList);
        model.addAttribute(RecapConstants.REQUEST_FORM, requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView(RecapConstants.REQUEST, RecapConstants.REQUEST_FORM, requestForm);
    }



    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=populateItem")
    public String populateItem(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                               BindingResult result,
                               Model model, HttpServletRequest request) throws JSONException {
        return getRequestService().populateItemForRequest(requestForm, request);
    }



    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=createRequest")
    public ModelAndView createRequest(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                BindingResult result,
                                Model model, HttpServletRequest request) throws JSONException {

        try {
            HttpSession session = request.getSession(false);
            String username = (String) session.getAttribute(RecapConstants.USER_NAME);
            String stringJson = populateItem(requestForm, null, model, request);
            if (stringJson != null) {
                JSONObject responseJsonObject = new JSONObject(stringJson);
                Object errorMessage = responseJsonObject.has(RecapConstants.ERROR_MESSAGE) ? responseJsonObject.get(RecapConstants.ERROR_MESSAGE) : null;
                Object noPermissionErrorMessage = responseJsonObject.has(RecapConstants.NO_PERMISSION_ERROR_MESSAGE) ? responseJsonObject.get(RecapConstants.NO_PERMISSION_ERROR_MESSAGE) : null;
                Object itemTitle = responseJsonObject.has(RecapConstants.REQUESTED_ITEM_TITLE) ? responseJsonObject.get(RecapConstants.REQUESTED_ITEM_TITLE) : null;
                Object itemOwningInstitution = responseJsonObject.has(RecapConstants.REQUESTED_ITEM_OWNING_INSTITUTION) ? responseJsonObject.get(RecapConstants.REQUESTED_ITEM_OWNING_INSTITUTION) : null;
                Object deliveryLocations = responseJsonObject.has(RecapConstants.DELIVERY_LOCATION) ? responseJsonObject.get(RecapConstants.DELIVERY_LOCATION) : null;
                Object requestTypes = responseJsonObject.has(RecapConstants.REQUEST_TYPES) ? responseJsonObject.get(RecapConstants.REQUEST_TYPES) : null;
                List<CustomerCodeEntity> customerCodeEntities = new ArrayList<>();
                List<String> requestTypeList=new ArrayList<>();
                if (itemTitle != null && itemOwningInstitution != null && deliveryLocations != null) {
                    requestForm.setItemTitle((String) itemTitle);
                    requestForm.setItemOwningInstitution((String) itemOwningInstitution);
                    JSONObject deliveryLocationsJson = (JSONObject) deliveryLocations;
                    Iterator iterator = deliveryLocationsJson.keys();
                    while (iterator.hasNext()) {
                        String customerCode = (String) iterator.next();
                        String description = (String) deliveryLocationsJson.get(customerCode);
                        CustomerCodeEntity customerCodeEntity = new CustomerCodeEntity();
                        customerCodeEntity.setCustomerCode(customerCode);
                        customerCodeEntity.setDescription(description);
                        customerCodeEntities.add(customerCodeEntity);
                    }
                    requestForm.setDeliveryLocations(customerCodeEntities);
                }
                if(!(RecapConstants.RECALL.equals(requestForm.getRequestType())) && requestTypes!=null) {
                    JSONArray requestTypeArray = (JSONArray) requestTypes;
                    for (int i = 0; i < requestTypeArray.length(); i++) {
                        requestTypeList.add(requestTypeArray.getString(i));
                    }
                    requestForm.setRequestTypes(requestTypeList);
                }
                if (noPermissionErrorMessage != null) {
                    requestForm.setErrorMessage((String) noPermissionErrorMessage);
                    requestForm.setShowRequestErrorMsg(true);
                    return new ModelAndView(RecapConstants.CREATE_REQUEST_SECTION, RecapConstants.REQUEST_FORM, requestForm);
                } else if (errorMessage != null) {
                    requestForm.setErrorMessage((String) errorMessage);
                    requestForm.setShowRequestErrorMsg(true);
                    return new ModelAndView(RecapConstants.CREATE_REQUEST_SECTION, RecapConstants.REQUEST_FORM, requestForm);
                }
            }

            String requestItemUrl = getScsbUrl() + RecapConstants.REQUEST_ITEM_URL;

            ItemRequestInformation itemRequestInformation = getItemRequestInformation();
            itemRequestInformation.setUsername(username);
            itemRequestInformation.setItemBarcodes(Arrays.asList(requestForm.getItemBarcodeInRequest().split(",")));
            itemRequestInformation.setPatronBarcode(requestForm.getPatronBarcodeInRequest());
            itemRequestInformation.setRequestingInstitution(requestForm.getRequestingInstitution());
            itemRequestInformation.setEmailAddress(requestForm.getPatronEmailAddress());
            itemRequestInformation.setTitle(requestForm.getItemTitle());
            itemRequestInformation.setTitleIdentifier(requestForm.getItemTitle());
            itemRequestInformation.setItemOwningInstitution(requestForm.getItemOwningInstitution());
            itemRequestInformation.setRequestType(requestForm.getRequestType());
            itemRequestInformation.setRequestNotes(requestForm.getRequestNotes());
            itemRequestInformation.setStartPage(requestForm.getStartPage());
            itemRequestInformation.setEndPage(requestForm.getEndPage());
            itemRequestInformation.setAuthor(requestForm.getArticleAuthor());
            itemRequestInformation.setChapterTitle(requestForm.getArticleTitle());
            itemRequestInformation.setIssue(requestForm.getIssue());
            if (requestForm.getVolumeNumber() != null) {
                itemRequestInformation.setVolume(requestForm.getVolumeNumber().toString());
            } else {
                itemRequestInformation.setVolume("");
            }

            if (StringUtils.isNotBlank(requestForm.getDeliveryLocationInRequest())) {
                CustomerCodeEntity customerCodeEntity = getCustomerCodeDetailsRepository().findByCustomerCode(requestForm.getDeliveryLocationInRequest());
                if (null != customerCodeEntity) {
                    itemRequestInformation.setDeliveryLocation(customerCodeEntity.getCustomerCode());
                }
            }

            HttpEntity<ItemRequestInformation> requestEntity = new HttpEntity<>(itemRequestInformation, getHttpHeaders());
            ResponseEntity<ItemResponseInformation> itemResponseEntity = getRestTemplate().exchange(requestItemUrl, HttpMethod.POST, requestEntity, ItemResponseInformation.class);
            ItemResponseInformation itemResponseInformation = itemResponseEntity.getBody();
            if (null != itemResponseInformation && !itemResponseInformation.isSuccess()) {
                requestForm.setErrorMessage(itemResponseInformation.getScreenMessage());
                requestForm.setDisableRequestingInstitution(false);
                requestForm.setShowRequestErrorMsg(true);
            }
        } catch (HttpClientErrorException httpException) {
            logger.error(RecapConstants.LOG_ERROR, httpException);
            String responseBodyAsString = httpException.getResponseBodyAsString();
            requestForm.setErrorMessage(responseBodyAsString);
            requestForm.setShowRequestErrorMsg(true);
        } catch (Exception exception) {
            logger.error(RecapConstants.LOG_ERROR, exception);
            requestForm.setErrorMessage(exception.getMessage());
            requestForm.setShowRequestErrorMsg(true);
        }

        requestForm.setRequestingInstitutions(requestForm.getInstitutionList());

        if(requestForm.getErrorMessage()==null){
        requestForm.setSubmitted(true);
        requestForm.setDisableRequestingInstitution(true);
        }
        return new ModelAndView(RecapConstants.CREATE_REQUEST_SECTION, RecapConstants.REQUEST_FORM, requestForm);
    }
    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=cancelRequest")
    public String cancelRequest(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                BindingResult result,
                                Model model) {
        JSONObject jsonObject = new JSONObject();
        String requestStatus = null;
        try {
            HttpEntity requestEntity = new HttpEntity<>(getHttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getScsbUrl() + RecapConstants.URL_REQUEST_CANCEL).queryParam(RecapConstants.REQUEST_ID, requestForm.getRequestId());
            HttpEntity<CancelRequestResponse> responseEntity = getRestTemplate().exchange(builder.build().encode().toUri(), HttpMethod.POST, requestEntity, CancelRequestResponse.class);
            CancelRequestResponse cancelRequestResponse = responseEntity.getBody();
            jsonObject.put(RecapConstants.MESSAGE, cancelRequestResponse.getScreenMessage());
            jsonObject.put(RecapConstants.STATUS, cancelRequestResponse.isSuccess());
            RequestItemEntity requestItemEntity = getRequestItemDetailsRepository().findByRequestId(requestForm.getRequestId());
            if (null != requestItemEntity) {
                requestStatus = requestItemEntity.getRequestStatusEntity().getRequestStatusDescription();
            }
            jsonObject.put(RecapConstants.REQUEST_STATUS, requestStatus);
        } catch (Exception exception) {
            logger.error(RecapConstants.LOG_ERROR, exception);
            logger.debug(exception.getMessage());
        }
        return jsonObject.toString();
    }

    private void searchAndSetResults(RequestForm requestForm) {
        Page<RequestItemEntity> requestItemEntities = getRequestServiceUtil().searchRequests(requestForm);
        List<SearchResultRow> searchResultRows = buildSearchResultRows(requestItemEntities.getContent());
        if (CollectionUtils.isNotEmpty(searchResultRows)) {
            requestForm.setSearchResultRows(searchResultRows);
            requestForm.setTotalRecordsCount(NumberFormat.getNumberInstance().format(requestItemEntities.getTotalElements()));
            requestForm.setTotalPageCount(requestItemEntities.getTotalPages());
        } else {
            requestForm.setSearchResultRows(Collections.emptyList());
            requestForm.setMessage(RecapConstants.SEARCH_RESULT_ERROR_NO_RECORDS_FOUND);
        }
        requestForm.setShowResults(true);
    }

    private List<SearchResultRow> buildSearchResultRows(List<RequestItemEntity> requestItemEntities) {
        if (CollectionUtils.isNotEmpty(requestItemEntities)) {
            List<SearchResultRow> searchResultRows = new ArrayList<>();
            for (RequestItemEntity requestItemEntity : requestItemEntities) {
                SearchResultRow searchResultRow = new SearchResultRow();
                searchResultRow.setRequestId(requestItemEntity.getRequestId());
                searchResultRow.setPatronBarcode(requestItemEntity.getPatronId());
                searchResultRow.setRequestingInstitution(requestItemEntity.getInstitutionEntity().getInstitutionCode());
                searchResultRow.setBarcode(requestItemEntity.getItemEntity().getBarcode());
                searchResultRow.setOwningInstitution(requestItemEntity.getItemEntity().getInstitutionEntity().getInstitutionCode());
                searchResultRow.setDeliveryLocation(requestItemEntity.getStopCode());
                searchResultRow.setRequestType(requestItemEntity.getRequestTypeEntity().getRequestTypeCode());
                searchResultRow.setRequestCreatedBy(requestItemEntity.getCreatedBy());
                searchResultRow.setPatronEmailId(requestItemEntity.getEmailId());
                searchResultRow.setRequestNotes(requestItemEntity.getNotes());
                searchResultRow.setCreatedDate(requestItemEntity.getCreatedDate());
                searchResultRow.setStatus(requestItemEntity.getRequestStatusEntity().getRequestStatusDescription());

                ItemEntity itemEntity = requestItemEntity.getItemEntity();
                if (null != itemEntity && CollectionUtils.isNotEmpty(itemEntity.getBibliographicEntities())) {
                    searchResultRow.setBibId(itemEntity.getBibliographicEntities().get(0).getBibliographicId());
                }
                searchResultRows.add(searchResultRow);
            }
            return searchResultRows;
        }
        return Collections.emptyList();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(RecapConstants.API_KEY, RecapConstants.RECAP);
        return headers;
    }

    private Integer getPageNumberOnPageSizeChange(RequestForm requestForm) {
        int totalRecordsCount;
        Integer pageNumber = requestForm.getPageNumber();
        try {
            totalRecordsCount = NumberFormat.getNumberInstance().parse(requestForm.getTotalRecordsCount()).intValue();
            int totalPagesCount = (int) Math.ceil((double) totalRecordsCount / (double) requestForm.getPageSize());
            if (totalPagesCount > 0 && pageNumber >= totalPagesCount) {
                pageNumber = totalPagesCount - 1;
            }
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR, e);
        }
        return pageNumber;
    }

    public ItemRequestInformation getItemRequestInformation() {
        return new ItemRequestInformation();
    }

    @ResponseBody
    @RequestMapping(value = "/request/refreshStatus", method = RequestMethod.GET)
    public String refreshStatus(HttpServletRequest request) {
        return getRequestService().getRefreshedStatus(request);
    }

}
