package org.recap.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.search.SearchItemResultRow;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.model.search.SearchRecordsResponse;
import org.recap.model.search.SearchResultRow;
import org.recap.model.userManagement.UserDetailsForm;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.security.UserManagement;
import org.recap.util.CsvUtil;
import org.recap.util.SearchUtil;
import org.recap.util.UserAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by rajeshbabuk on 6/7/16.
 */

@Controller
public class SearchRecordsController {

    Logger logger = LoggerFactory.getLogger(SearchRecordsController.class);

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.shiro}")
    String scsbShiro;


    @Autowired
    SearchUtil searchUtil;

    @Autowired
    private CsvUtil csvUtil;

    @Autowired
    private UserAuthUtil userAuthUtil;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    public CsvUtil getCsvUtil() {
        return csvUtil;
    }

    public void setCsvUtil(CsvUtil csvUtil) {
        this.csvUtil = csvUtil;
    }

    public SearchUtil getSearchUtil() {
        return searchUtil;
    }

    public void setSearchUtil(SearchUtil searchUtil) {
        this.searchUtil = searchUtil;
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

    @RequestMapping("/search")
    public String searchRecords(Model model, HttpServletRequest request) {
        HttpSession session=request.getSession();
        boolean authenticated=getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_SEARCH_URL,(UsernamePasswordToken)session.getAttribute(UserManagement.USER_TOKEN));
        if(authenticated)
        {
            SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
            model.addAttribute(RecapConstants.VIEW_SEARCH_RECORDS_REQUEST, searchRecordsRequest);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SEARCH);
            return RecapConstants.VIEW_SEARCH_RECORDS;
        }else{
            return UserManagement.unAuthorizedUser(session,"Search",logger);
        }

    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=search")
    public ModelAndView search(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                  BindingResult result,
                                  Model model) {
        searchRecordsRequest.resetPageNumber();
        searchAndSetResults(searchRecordsRequest);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SEARCH);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.VIEW_SEARCH_RECORDS_REQUEST, searchRecordsRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=previous")
    public ModelAndView searchPrevious(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                               BindingResult result,
                               Model model) {
        searchAndSetResults(searchRecordsRequest);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SEARCH);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.VIEW_SEARCH_RECORDS_REQUEST, searchRecordsRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=next")
    public ModelAndView searchNext(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                   BindingResult result,
                                   Model model) {
        searchAndSetResults(searchRecordsRequest);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SEARCH);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.VIEW_SEARCH_RECORDS_REQUEST, searchRecordsRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=first")
    public ModelAndView searchFirst(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                       BindingResult result,
                                       Model model) {
        searchRecordsRequest.resetPageNumber();
        searchAndSetResults(searchRecordsRequest);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SEARCH);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.VIEW_SEARCH_RECORDS_REQUEST, searchRecordsRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=last")
    public ModelAndView searchLast(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                       BindingResult result,
                                       Model model) {
        searchRecordsRequest.setPageNumber(searchRecordsRequest.getTotalPageCount() - 1);
        searchAndSetResults(searchRecordsRequest);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SEARCH);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.VIEW_SEARCH_RECORDS_REQUEST, searchRecordsRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=clear")
    public ModelAndView clear(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                              BindingResult result,
                              Model model) {

        searchRecordsRequest.setFieldValue("");
        searchRecordsRequest.setOwningInstitutions(new ArrayList<>());
        searchRecordsRequest.setCollectionGroupDesignations(new ArrayList<>());
        searchRecordsRequest.setAvailability(new ArrayList<>());
        searchRecordsRequest.setMaterialTypes(new ArrayList<>());
        searchRecordsRequest.setUseRestrictions(new ArrayList<>());
        searchRecordsRequest.setShowResults(false);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SEARCH);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS);
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=newSearch")
    public ModelAndView newSearch(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                  BindingResult result,
                                  Model model) {
        searchRecordsRequest = new SearchRecordsRequest();
        model.addAttribute(RecapConstants.VIEW_SEARCH_RECORDS_REQUEST, searchRecordsRequest);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SEARCH);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS);
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=request")
    public ModelAndView requestRecords(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                       BindingResult result,
                                       Model model,
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        UserDetailsForm userDetailsForm = getUserAuthUtil().getUserDetails(request.getSession(),UserManagement.REQUEST_PRIVILEGE);
        processRequest(searchRecordsRequest, userDetailsForm, redirectAttributes);
        if (StringUtils.isNotBlank(searchRecordsRequest.getErrorMessage())) {
            searchRecordsRequest.setShowResults(true);
            model.addAttribute("searchRecordsRequest", searchRecordsRequest);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SEARCH);
            return new ModelAndView("searchRecords");
        }
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REQUEST);
        return new ModelAndView(new RedirectView("/request",true));
    }


    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=export")
    public byte[] exportRecords(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest, HttpServletResponse response,
                                  BindingResult result,
                                  Model model) throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileNameWithExtension = "ExportRecords_" + dateFormat.format(new Date()) + ".csv";
        File csvFile = csvUtil.writeSearchResultsToCsv(searchRecordsRequest.getSearchResultRows(), fileNameWithExtension);
        byte[] fileContent = IOUtils.toByteArray(new FileInputStream(csvFile));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameWithExtension + "\"");
        response.setContentLength(fileContent.length);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SEARCH);
        return fileContent;
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST, params = "action=pageSizeChange")
    public ModelAndView onPageSizeChange(@Valid @ModelAttribute("searchRecordsRequest") SearchRecordsRequest searchRecordsRequest,
                                         BindingResult result,
                                         Model model) throws Exception {
        searchRecordsRequest.setPageNumber(getPageNumberOnPageSizeChange(searchRecordsRequest));
        searchAndSetResults(searchRecordsRequest);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SEARCH);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.VIEW_SEARCH_RECORDS_REQUEST, searchRecordsRequest);
    }

    public Integer getPageNumberOnPageSizeChange(SearchRecordsRequest searchRecordsRequest) {
        int totalRecordsCount;
        Integer pageNumber = searchRecordsRequest.getPageNumber();
        try {
            if (isEmptyField(searchRecordsRequest)) {
                totalRecordsCount = NumberFormat.getNumberInstance().parse(searchRecordsRequest.getTotalRecordsCount()).intValue();
            } else if (isItemField(searchRecordsRequest)) {
                totalRecordsCount = NumberFormat.getNumberInstance().parse(searchRecordsRequest.getTotalItemRecordsCount()).intValue();
            } else {
                totalRecordsCount = NumberFormat.getNumberInstance().parse(searchRecordsRequest.getTotalBibRecordsCount()).intValue();
            }
            int totalPagesCount = (int) Math.ceil((double) totalRecordsCount / (double) searchRecordsRequest.getPageSize());
            if (totalPagesCount > 0 && pageNumber >= totalPagesCount) {
                pageNumber = totalPagesCount - 1;
            }
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return pageNumber;
    }

    private boolean isEmptyField(SearchRecordsRequest searchRecordsRequest) {
        if (StringUtils.isBlank(searchRecordsRequest.getFieldName()) && StringUtils.isNotBlank(searchRecordsRequest.getFieldValue())) {
            return true;
        }
        return false;
    }

    private boolean isItemField(SearchRecordsRequest searchRecordsRequest) {
        if (StringUtils.isNotBlank(searchRecordsRequest.getFieldName())
                && (searchRecordsRequest.getFieldName().equalsIgnoreCase(RecapConstants.BARCODE) || searchRecordsRequest.getFieldName().equalsIgnoreCase(RecapConstants.CALL_NUMBER))) {
            return true;
        }
        return false;
    }

    private void searchAndSetResults(SearchRecordsRequest searchRecordsRequest) {
        boolean errorStatus=false;
        searchRecordsRequest.reset();
        searchRecordsRequest.setSearchResultRows(null);
        searchRecordsRequest.setShowResults(true);
        searchRecordsRequest.setSelectAll(false);

        SearchRecordsResponse searchRecordsResponse = getSearchUtil().requestSearchResults(searchRecordsRequest);
        searchRecordsRequest.setSearchResultRows(searchRecordsResponse.getSearchResultRows());
        searchRecordsRequest.setTotalRecordsCount(searchRecordsResponse.getTotalRecordsCount());
        searchRecordsRequest.setTotalBibRecordsCount(searchRecordsResponse.getTotalBibRecordsCount());
        searchRecordsRequest.setTotalItemRecordsCount(searchRecordsResponse.getTotalItemRecordsCount());
        searchRecordsRequest.setTotalPageCount(searchRecordsResponse.getTotalPageCount());
        searchRecordsRequest.setShowTotalCount(searchRecordsResponse.isShowTotalCount());
        searchRecordsRequest.setErrorMessage(searchRecordsResponse.getErrorMessage());

        if(CollectionUtils.isEmpty(searchRecordsRequest.getSearchResultRows())) {
            searchRecordsRequest.setTotalRecordsCount(String.valueOf(0));
            searchRecordsRequest.setTotalBibRecordsCount(String.valueOf(0));
            searchRecordsRequest.setTotalItemRecordsCount(String.valueOf(0));
            if (errorStatus){
                searchRecordsRequest.setErrorMessage(RecapConstants.SEARCH_RESULT_ERROR_INVALID_CHARACTERS);
            }else{
                if(searchRecordsRequest.getErrorMessage() == null ) {
                    searchRecordsRequest.setErrorMessage(RecapConstants.SEARCH_RESULT_ERROR_NO_RECORDS_FOUND);
                }
            }
        }
    }

    private void processRequest(SearchRecordsRequest searchRecordsRequest, UserDetailsForm userDetailsForm, RedirectAttributes redirectAttributes) {
        String userInstitution = null;
        InstitutionEntity institutionEntity = getInstitutionDetailsRepository().findByInstitutionId(userDetailsForm.getLoginInstitutionId());
        if (null != institutionEntity) {
            userInstitution = institutionEntity.getInstitutionCode();
        }
        List<SearchResultRow> searchResultRows = searchRecordsRequest.getSearchResultRows();
        Set<String> barcodes = new HashSet<>();
        Set<String> itemTitles = new HashSet<>();
        Set<String> itemOwningInstitutions = new HashSet<>();
        for (SearchResultRow searchResultRow : searchResultRows) {
            if (searchResultRow.isSelected()) {
                if (RecapConstants.PRIVATE.equals(searchResultRow.getCollectionGroupDesignation()) && !userDetailsForm.isSuperAdmin() && !userDetailsForm.isRecapUser() && StringUtils.isNotBlank(userInstitution) && !userInstitution.equals(searchResultRow.getOwningInstitution())) {
                    searchRecordsRequest.setErrorMessage(RecapConstants.REQUEST_PRIVATE_ERROR_USER_NOT_PERMITTED);
                    return;
                } else if (!userDetailsForm.isRecapPermissionAllowed()) {
                    searchRecordsRequest.setErrorMessage(RecapConstants.REQUEST_ERROR_USER_NOT_PERMITTED);
                    return;
                } else {
                    processBarcodesForSearchResultRow(barcodes, itemTitles, itemOwningInstitutions, searchResultRow);
                }
            } else if (!CollectionUtils.isEmpty(searchResultRow.getSearchItemResultRows())) {
                for (SearchItemResultRow searchItemResultRow : searchResultRow.getSearchItemResultRows()) {
                    if (searchItemResultRow.isSelectedItem()) {
                        if (RecapConstants.PRIVATE.equals(searchItemResultRow.getCollectionGroupDesignation()) && !userDetailsForm.isSuperAdmin() && !userDetailsForm.isRecapUser() && StringUtils.isNotBlank(userInstitution) && !userInstitution.equals(searchResultRow.getOwningInstitution())) {
                            searchRecordsRequest.setErrorMessage(RecapConstants.REQUEST_PRIVATE_ERROR_USER_NOT_PERMITTED);
                            return;
                        } else if (!userDetailsForm.isRecapPermissionAllowed()) {
                            searchRecordsRequest.setErrorMessage(RecapConstants.REQUEST_ERROR_USER_NOT_PERMITTED);
                            return;
                        } else {
                            processBarcodeForSearchItemResultRow(barcodes, itemTitles, itemOwningInstitutions, searchItemResultRow, searchResultRow);
                        }
                    }
                }
            }
        }
        redirectAttributes.addFlashAttribute(RecapConstants.REQUESTED_BARCODE, StringUtils.join(barcodes, ","));
        redirectAttributes.addFlashAttribute(RecapConstants.REQUESTED_ITEM_TITLE, StringUtils.join(itemTitles, " || "));
        redirectAttributes.addFlashAttribute(RecapConstants.REQUESTED_ITEM_OWNING_INSTITUTION, StringUtils.join(itemOwningInstitutions, ","));
    }

    private void processBarcodeForSearchItemResultRow(Set<String> barcodes, Set<String> titles, Set<String> itemInstitutions, SearchItemResultRow searchItemResultRow, SearchResultRow searchResultRow) {
        String barcode = searchItemResultRow.getBarcode();
        processTitleAndItemInstitution(barcodes, titles, itemInstitutions, searchResultRow, barcode);
    }

    private void processBarcodesForSearchResultRow(Set<String> barcodes, Set<String> titles, Set<String> itemInstitutions, SearchResultRow searchResultRow) {
        String barcode = searchResultRow.getBarcode();
        processTitleAndItemInstitution(barcodes, titles, itemInstitutions, searchResultRow, barcode);
    }

    private void processTitleAndItemInstitution(Set<String> barcodes, Set<String> titles, Set<String> itemInstitutions, SearchResultRow searchResultRow, String barcode) {
        String title = searchResultRow.getTitle();
        String owningInstitution = searchResultRow.getOwningInstitution();
        if (StringUtils.isNotBlank(barcode)) {
            barcodes.add(barcode);
        }
        if (StringUtils.isNotBlank(title)) {
            titles.add(title);
        }
        if (StringUtils.isNotBlank(owningInstitution)) {
            itemInstitutions.add(owningInstitution);
        }
    }

    private boolean isAnyItemSelected(List<SearchItemResultRow> searchItemResultRows) {
        for (SearchItemResultRow searchItemResultRow :  searchItemResultRows) {
            if (searchItemResultRow.isSelectedItem()) {
                return true;
            }
        }
        return false;
    }


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAutoGrowCollectionLimit(1048576);
    }


}
