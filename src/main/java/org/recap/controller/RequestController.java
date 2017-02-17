package org.recap.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.codehaus.jettison.json.JSONObject;
import org.marc4j.marc.Record;
import org.recap.RecapConstants;
import org.recap.model.jpa.*;
import org.recap.model.request.CancelRequestResponse;
import org.recap.model.request.ItemRequestInformation;
import org.recap.model.request.ItemResponseInformation;
import org.recap.model.search.RequestForm;
import org.recap.model.search.SearchResultRow;
import org.recap.model.userManagement.UserDetailsForm;
import org.recap.repository.jpa.*;
import org.recap.security.UserManagement;
import org.recap.util.BibJSONUtil;
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
import org.springframework.validation.support.BindingAwareModelMap;
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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by rajeshbabuk on 13/10/16.
 */

@Controller
public class RequestController {

    Logger logger = LoggerFactory.getLogger(RequestController.class);

    @Value("${server.protocol}")
    String serverProtocol;

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

    public RequestServiceUtil getRequestServiceUtil() {
        return requestServiceUtil;
    }

    public void setRequestServiceUtil(RequestServiceUtil requestServiceUtil) {
        this.requestServiceUtil = requestServiceUtil;
    }

    public UserAuthUtil getUserAuthUtil() {
        return userAuthUtil;
    }

    public void setUserAuthUtil(UserAuthUtil userAuthUtil) {
        this.userAuthUtil = userAuthUtil;
    }

    public InstitutionDetailsRepository getInstitutionDetailsRepository() {
        return institutionDetailsRepository;
    }

    public void setInstitutionDetailsRepository(InstitutionDetailsRepository institutionDetailsRepository) {
        this.institutionDetailsRepository = institutionDetailsRepository;
    }

    public RequestTypeDetailsRepository getRequestTypeDetailsRepository() {
        return requestTypeDetailsRepository;
    }

    public void setRequestTypeDetailsRepository(RequestTypeDetailsRepository requestTypeDetailsRepository) {
        this.requestTypeDetailsRepository = requestTypeDetailsRepository;
    }

    public CustomerCodeDetailsRepository getCustomerCodeDetailsRepository() {
        return customerCodeDetailsRepository;
    }

    public void setCustomerCodeDetailsRepository(CustomerCodeDetailsRepository customerCodeDetailsRepository) {
        this.customerCodeDetailsRepository = customerCodeDetailsRepository;
    }

    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    public void setItemDetailsRepository(ItemDetailsRepository itemDetailsRepository) {
        this.itemDetailsRepository = itemDetailsRepository;
    }

    @RequestMapping("/request")
    public String collection(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        boolean authenticated = getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_REQUEST_URL, (UsernamePasswordToken) session.getAttribute(UserManagement.USER_TOKEN));
        if (authenticated) {
            UserDetailsForm userDetailsForm = getUserAuthUtil().getUserDetails(session, UserManagement.REQUEST_ITEM_PRIVILEGE);
            RequestForm requestForm = setDefaultsToCreateRequest(userDetailsForm);
            Object requestedBarcode = ((BindingAwareModelMap) model).get(RecapConstants.REQUESTED_BARCODE);
            Object itemTitle = ((BindingAwareModelMap) model).get(RecapConstants.REQUESTED_ITEM_TITLE);
            Object itemOwningInstitution = ((BindingAwareModelMap) model).get(RecapConstants.REQUESTED_ITEM_OWNING_INSTITUTION);
            if(requestedBarcode  != null && itemTitle != null && itemOwningInstitution != null){
                requestForm.setItemBarcodeInRequest((String) requestedBarcode);
                requestForm.setItemTitle((String) itemTitle);
                requestForm.setItemOwningInstitution((String) itemOwningInstitution);
            }
            model.addAttribute("requestForm", requestForm);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
            return "searchRecords";
        } else {
            return UserManagement.unAuthorizedUser(session, "Request", logger);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=searchRequests")
    public ModelAndView searchRequests(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                       BindingResult result,
                                       Model model) throws Exception {
        try {
            requestForm.resetPageNumber();
            searchAndSetResults(requestForm);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error("" + exception);
            logger.debug(exception.getMessage());
        }
        return new ModelAndView("request", "requestForm", requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=first")
    public ModelAndView searchFirst(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                    BindingResult result,
                                    Model model) {
        requestForm.resetPageNumber();
        searchAndSetResults(requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView("request", "requestForm", requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=last")
    public ModelAndView searchLast(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                   BindingResult result,
                                   Model model) {
        requestForm.setPageNumber(requestForm.getTotalPageCount() - 1);
        searchAndSetResults(requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView("request", "requestForm", requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=previous")
    public ModelAndView searchPrevious(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                       BindingResult result,
                                       Model model) {
        searchAndSetResults(requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView("request", "requestForm", requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=next")
    public ModelAndView searchNext(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                   BindingResult result,
                                   Model model) {
        searchAndSetResults(requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView("request", "requestForm", requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=requestPageSizeChange")
    public ModelAndView onRequestPageSizeChange(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                         BindingResult result,
                                         Model model) {
        requestForm.setPageNumber(getPageNumberOnPageSizeChange(requestForm));
        searchAndSetResults(requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView("request", "requestForm", requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=loadCreateRequest")
    public ModelAndView loadCreateRequest(Model model,HttpServletRequest request) {
        UserDetailsForm userDetailsForm=getUserAuthUtil().getUserDetails(request.getSession(),UserManagement.REQUEST_ITEM_PRIVILEGE);
        RequestForm requestForm = setDefaultsToCreateRequest(userDetailsForm);
        model.addAttribute("requestForm", requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView("request", "requestForm", requestForm);
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=loadSearchRequest")
    public ModelAndView loadSearchRequest(Model model, HttpServletRequest request) {
        RequestForm requestForm = new RequestForm();
        List<String> requestStatuses = new ArrayList<>();
        Iterable<RequestStatusEntity> requestStatusEntities = requestStatusDetailsRepository.findAll();
        for (Iterator iterator = requestStatusEntities.iterator(); iterator.hasNext(); ) {
            RequestStatusEntity requestStatusEntity = (RequestStatusEntity) iterator.next();
            requestStatuses.add(requestStatusEntity.getRequestStatusDescription());
        }
        requestForm.setRequestStatuses(requestStatuses);
        model.addAttribute("requestForm", requestForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView("request", "requestForm", requestForm);
    }

    private RequestForm setDefaultsToCreateRequest(UserDetailsForm userDetailsForm) {
        RequestForm requestForm = new RequestForm();

        List<String> requestingInstitutions = new ArrayList<>();
        List<String> requestTypes = new ArrayList<>();
        List<String> deliveryLocations = new ArrayList<>();

        Iterable<InstitutionEntity> institutionEntities = getInstitutionDetailsRepository().findAll();
        for (Iterator iterator = institutionEntities.iterator(); iterator.hasNext(); ) {
            InstitutionEntity institutionEntity = (InstitutionEntity) iterator.next();
            if (userDetailsForm.getLoginInstitutionId() == institutionEntity.getInstitutionId() || userDetailsForm.isRecapUser() || userDetailsForm.isSuperAdmin()) {
                requestingInstitutions.add(institutionEntity.getInstitutionCode());
            }
        }

        Iterable<RequestTypeEntity> requestTypeEntities = getRequestTypeDetailsRepository().findAll();
        for (Iterator iterator = requestTypeEntities.iterator(); iterator.hasNext(); ) {
            RequestTypeEntity requestTypeEntity = (RequestTypeEntity) iterator.next();
            requestTypes.add(requestTypeEntity.getRequestTypeCode());
        }

        Iterable<CustomerCodeEntity> customerCodeEntities = getCustomerCodeDetailsRepository().findAll();
        for (Iterator iterator = customerCodeEntities.iterator(); iterator.hasNext(); ) {
            CustomerCodeEntity customerCodeEntity = (CustomerCodeEntity) iterator.next();
            if (userDetailsForm.getLoginInstitutionId() == customerCodeEntity.getOwningInstitutionId() || userDetailsForm.isRecapUser() || userDetailsForm.isSuperAdmin()) {
                deliveryLocations.add(customerCodeEntity.getDescription());
            }
        }

        requestForm.setRequestingInstitutions(requestingInstitutions);
        requestForm.setRequestTypes(requestTypes);
        requestForm.setDeliveryLocations(deliveryLocations);
        requestForm.setRequestType("RETRIEVAL");
        return requestForm;
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=populateItem")
    public String populateItem(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                               BindingResult result,
                               Model model,HttpServletRequest request) throws Exception {
        JSONObject jsonObject = new JSONObject();

        if (StringUtils.isNotBlank(requestForm.getItemBarcodeInRequest())) {
            List<String> itemBarcodes = Arrays.asList(requestForm.getItemBarcodeInRequest().split(","));
            List<String> invalidBarcodes = new ArrayList<>();
            Set<String> itemTitles = new HashSet<>();
            Set<String> itemOwningInstitutions = new HashSet<>();
            UserDetailsForm userDetailsForm=null;
            for (String itemBarcode : itemBarcodes) {
                String barcode = itemBarcode.trim();
                if (StringUtils.isNotBlank(barcode)) {
                    List<ItemEntity> itemEntities = getItemDetailsRepository().findByBarcodeAndIsDeletedFalse(barcode);
                    if (CollectionUtils.isNotEmpty(itemEntities)) {
                        for (ItemEntity itemEntity : itemEntities) {
                            if (null != itemEntity && CollectionUtils.isNotEmpty(itemEntity.getBibliographicEntities())) {
                                userDetailsForm = getUserAuthUtil().getUserDetails(request.getSession(),UserManagement.REQUEST_ITEM_PRIVILEGE);
                                if (itemEntity.getCollectionGroupId()==RecapConstants.CGD_PRIVATE && !userDetailsForm.isSuperAdmin() && !userDetailsForm.isRecapUser() && !userDetailsForm.getLoginInstitutionId().equals(itemEntity.getOwningInstitutionId())) {
                                    jsonObject.put("errorMessage", "User is not permitted to request private item(s)");
                                } else {
                                    for (BibliographicEntity bibliographicEntity : itemEntity.getBibliographicEntities()) {
                                        String bibContent = new String(bibliographicEntity.getContent());
                                        BibJSONUtil bibJSONUtil = new BibJSONUtil();
                                        List<Record> records = bibJSONUtil.convertMarcXmlToRecord(bibContent);
                                        Record marcRecord = records.get(0);
                                        itemTitles.add(bibJSONUtil.getTitle(marcRecord));
                                        itemOwningInstitutions.add(itemEntity.getInstitutionEntity().getInstitutionCode());
                                    }
                                }
                            }
                        }
                    } else {
                        invalidBarcodes.add(barcode);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(itemTitles)) {
                jsonObject.put("itemTitle", StringUtils.join(itemTitles, " || "));
            }
            if (CollectionUtils.isNotEmpty(itemOwningInstitutions)) {
                jsonObject.put("itemOwningInstitution", StringUtils.join(itemOwningInstitutions, ","));
            }
            if (CollectionUtils.isNotEmpty(invalidBarcodes)) {
                jsonObject.put("errorMessage", RecapConstants.BARCODES_NOT_FOUND + " - " + StringUtils.join(invalidBarcodes, ","));
            }
        }
        return jsonObject.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=createRequest")
    public String createRequest(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                BindingResult result,
                                Model model, HttpServletRequest request) throws Exception {
        try {
            HttpSession session = request.getSession();
            String username = (String) session.getAttribute(UserManagement.USER_NAME);

            RestTemplate restTemplate = new RestTemplate();

            String validateRequestItemUrl = serverProtocol + scsbUrl + RecapConstants.VALIDATE_REQUEST_ITEM_URL;
            String requestItemUrl = serverProtocol + scsbUrl + RecapConstants.REQUEST_ITEM_URL;

            ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
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
            if (StringUtils.isNotBlank(requestForm.getDeliveryLocationInRequest())) {
                CustomerCodeEntity customerCodeEntity = customerCodeDetailsRepository.findByDescription(requestForm.getDeliveryLocationInRequest());
                if (null != customerCodeEntity) {
                    itemRequestInformation.setDeliveryLocation(customerCodeEntity.getCustomerCode());
                }
            }

            HttpEntity<ItemRequestInformation> requestEntity = new HttpEntity<>(itemRequestInformation, getHttpHeaders());

            ResponseEntity<String> responseEntity = restTemplate.exchange(validateRequestItemUrl, HttpMethod.POST, requestEntity, String.class);
            if (RecapConstants.VALID_REQUEST.equalsIgnoreCase(responseEntity.getBody())) {
                ResponseEntity<ItemResponseInformation> itemResponseEntity = restTemplate.exchange(requestItemUrl, HttpMethod.POST, requestEntity, ItemResponseInformation.class);
                ItemResponseInformation itemResponseInformation = itemResponseEntity.getBody();
                if (null != itemResponseInformation && !itemResponseInformation.isSuccess()) {
                    requestForm.setErrorMessage(itemResponseInformation.getScreenMessage());
                }
            }

        } catch (HttpClientErrorException httpException) {
            String responseBodyAsString = httpException.getResponseBodyAsString();
            requestForm.setErrorMessage(responseBodyAsString);
        } catch (Exception exception) {
            exception.printStackTrace();
            requestForm.setErrorMessage(exception.getMessage());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(RecapConstants.ITEM_TITLE, requestForm.getItemTitle());
        jsonObject.put(RecapConstants.ITEM_BARCODE, requestForm.getItemBarcodeInRequest());
        jsonObject.put(RecapConstants.ITEM_OWNING_INSTITUTION, requestForm.getItemOwningInstitution());
        jsonObject.put(RecapConstants.PATRON_BARCODE, requestForm.getPatronBarcodeInRequest());
        jsonObject.put(RecapConstants.PATRON_EMAIL_ADDRESS, requestForm.getPatronEmailAddress());
        jsonObject.put(RecapConstants.REQUESTING_INSTITUTION, requestForm.getRequestingInstitution());
        jsonObject.put(RecapConstants.REQUEST_TYPE, requestForm.getRequestType());
        jsonObject.put(RecapConstants.DELIVERY_LOCATION, requestForm.getDeliveryLocationInRequest());
        jsonObject.put(RecapConstants.REQUEST_NOTES, requestForm.getRequestNotes());
        jsonObject.put(RecapConstants.ERROR_MESSAGE, requestForm.getErrorMessage());

        return jsonObject.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST, params = "action=cancelRequest")
    public String cancelRequest(@Valid @ModelAttribute("requestForm") RequestForm requestForm,
                                      BindingResult result,
                                      Model model) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        JSONObject jsonObject = new JSONObject();
        String requestStatus = null;
        try {
            HttpEntity requestEntity = new HttpEntity<>(getHttpHeaders());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverProtocol + scsbUrl + RecapConstants.URL_REQUEST_CANCEL).queryParam(RecapConstants.REQUEST_ID, requestForm.getRequestId());
            HttpEntity<CancelRequestResponse> responseEntity  = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, requestEntity, CancelRequestResponse.class);
            CancelRequestResponse cancelRequestResponse = responseEntity.getBody();
            jsonObject.put(RecapConstants.MESSAGE, cancelRequestResponse.getScreenMessage());
            jsonObject.put(RecapConstants.STATUS, cancelRequestResponse.isSuccess());
            RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByRequestId(requestForm.getRequestId());
            if (null != requestItemEntity) {
                requestStatus = requestItemEntity.getRequestStatusEntity().getRequestStatusDescription();
            }
            jsonObject.put(RecapConstants.REQUEST_STATUS, requestStatus);
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error("" + exception);
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
            requestForm.setSearchResultRows(Collections.EMPTY_LIST);
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
                if (null != itemEntity) {
                    if (CollectionUtils.isNotEmpty(itemEntity.getBibliographicEntities())) {
                        searchResultRow.setBibId(itemEntity.getBibliographicEntities().get(0).getBibliographicId());
                    }
                }
                searchResultRows.add(searchResultRow);
            }
            return searchResultRows;
        }
        return Collections.EMPTY_LIST;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(RecapConstants.API_KEY, RecapConstants.RECAP);
        return headers;
    }

    public Integer getPageNumberOnPageSizeChange(RequestForm requestForm) {
        int totalRecordsCount;
        Integer pageNumber = requestForm.getPageNumber();
        try {
            totalRecordsCount = NumberFormat.getNumberInstance().parse(requestForm.getTotalRecordsCount()).intValue();
            int totalPagesCount = (int) Math.ceil((double) totalRecordsCount / (double) requestForm.getPageSize());
            if (totalPagesCount > 0 && pageNumber >= totalPagesCount) {
                pageNumber = totalPagesCount - 1;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return pageNumber;
    }

}
