package org.recap.controller;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.search.DeaccessionItemResultsRow;
import org.recap.model.search.IncompleteReportResultsRow;
import org.recap.model.search.ReportsForm;
import org.recap.repository.jpa.InstitutionDetailsRepository;
import org.recap.security.UserManagementService;
import org.recap.util.ReportsUtil;
import org.recap.util.UserAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by rajeshbabuk on 13/10/16.
 */

@Controller
public class ReportsController {

    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);

    @Autowired
    ReportsUtil reportsUtil;

    @Autowired
    private UserAuthUtil userAuthUtil;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    public UserAuthUtil getUserAuthUtil() {
        return userAuthUtil;
    }

    public void setUserAuthUtil(UserAuthUtil userAuthUtil) {
        this.userAuthUtil = userAuthUtil;
    }

    public ReportsUtil getReportsUtil() {
        return reportsUtil;
    }

    @RequestMapping("/reports")
    public String collection(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        boolean authenticated = getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_REPORT_URL, (UsernamePasswordToken) session.getAttribute(RecapConstants.USER_TOKEN));
        if (authenticated) {
            ReportsForm reportsForm = new ReportsForm();
            model.addAttribute(RecapConstants.REPORTS_FORM, reportsForm);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
            return RecapConstants.VIEW_SEARCH_RECORDS;
        } else {
            return UserManagementService.unAuthorizedUser(session, "Reports", logger);
        }

    }

    @ResponseBody
    @RequestMapping(value = "/reports/submit", method = RequestMethod.POST)
    public ModelAndView reportCounts(@Valid @ModelAttribute("reportsForm") ReportsForm reportsForm,
                                     Model model) throws Exception {
        if (reportsForm.getRequestType().equalsIgnoreCase(RecapConstants.REPORTS_REQUEST)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(RecapConstants.SIMPLE_DATE_FORMAT_REPORTS);
            Date requestFromDate = simpleDateFormat.parse(reportsForm.getRequestFromDate());
            Date requestToDate = simpleDateFormat.parse(reportsForm.getRequestToDate());
            if (reportsForm.getShowBy().equalsIgnoreCase(RecapConstants.REPORTS_IL_BD)) {
                reportsUtil.populateILBDCountsForRequest(reportsForm, requestFromDate, requestToDate);
            } else if (reportsForm.getShowBy().equalsIgnoreCase(RecapConstants.REPORTS_PARTNERS)) {
                reportsUtil.populatePartnersCountForRequest(reportsForm, requestFromDate, requestToDate);
            } else if (reportsForm.getShowBy().equalsIgnoreCase(RecapConstants.REPORTS_REQUEST_TYPE)) {
                reportsUtil.populateRequestTypeInformation(reportsForm, requestFromDate, requestToDate);
            }
        } else if (reportsForm.getRequestType().equalsIgnoreCase(RecapConstants.REPORTS_ACCESSION_DEACCESSION)) {
            reportsUtil.populateAccessionDeaccessionItemCounts(reportsForm);

        } else if ("CollectionGroupDesignation".equalsIgnoreCase(reportsForm.getRequestType())) {
            reportsUtil.populateCGDItemCounts(reportsForm);
        }
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
        return new ModelAndView("reports", "reportsForm", reportsForm);
    }

    @ResponseBody
    @RequestMapping(value = "/reports/collectionGroupDesignation", method = RequestMethod.GET)
    public ModelAndView cgdCounts(@Valid @ModelAttribute("reportsForm") ReportsForm reportsForm,
                                  Model model) throws Exception {
        reportsUtil.populateCGDItemCounts(reportsForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
        return new ModelAndView(RecapConstants.REPORTS_VIEW_CGD_TABLE, RecapConstants.REPORTS_FORM, reportsForm);

    }

    @ResponseBody
    @RequestMapping(value = "/reports/deaccessionInformation", method = RequestMethod.GET)
    public ModelAndView deaccessionInformation(ReportsForm reportsForm,
                                               Model model) throws Exception {
        List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = getReportsUtil().deaccessionReportFieldsInformation(reportsForm);
        reportsForm.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
        return new ModelAndView(RecapConstants.REPORTS_VIEW_DEACCESSION_INFORMARION, RecapConstants.REPORTS_FORM, reportsForm);

    }


    @ResponseBody
    @RequestMapping(value = "/reports/first", method = RequestMethod.POST)
    public ModelAndView searchFirst(@Valid ReportsForm reportsForm,
                                    Model model) throws Exception {
        if ((RecapConstants.REPORTS_INCOMPLETE_RECORDS).equals(reportsForm.getRequestType())) {
            reportsForm.setIncompletePageNumber(0);
            return getIncompleteRecords(reportsForm, model);
        } else {
            reportsForm.setPageNumber(0);
            List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = reportsUtil.deaccessionReportFieldsInformation(reportsForm);
            reportsForm.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
            return new ModelAndView(RecapConstants.REPORTS_VIEW_DEACCESSION_INFORMARION, RecapConstants.REPORTS_FORM, reportsForm);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/reports/previous", method = RequestMethod.POST)
    public ModelAndView searchPrevious(@Valid ReportsForm reportsForm,
                                       Model model) throws Exception {
        if ((RecapConstants.REPORTS_INCOMPLETE_RECORDS).equals(reportsForm.getRequestType())) {
            return getIncompleteRecords(reportsForm, model);
        } else {
            List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = reportsUtil.deaccessionReportFieldsInformation(reportsForm);
            reportsForm.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
            return new ModelAndView(RecapConstants.REPORTS_VIEW_DEACCESSION_INFORMARION, RecapConstants.REPORTS_FORM, reportsForm);
        }
    }


    @ResponseBody
    @RequestMapping(value = "/reports/next", method = RequestMethod.POST)
    public ModelAndView searchNext(@Valid ReportsForm reportsForm,
                                   Model model) throws Exception {
        if ((RecapConstants.REPORTS_INCOMPLETE_RECORDS).equals(reportsForm.getRequestType())) {
            return getIncompleteRecords(reportsForm, model);
        } else {
            List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = reportsUtil.deaccessionReportFieldsInformation(reportsForm);
            reportsForm.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
            return new ModelAndView(RecapConstants.REPORTS_VIEW_DEACCESSION_INFORMARION, RecapConstants.REPORTS_FORM, reportsForm);
        }
    }


    @ResponseBody
    @RequestMapping(value = "/reports/last", method = RequestMethod.POST)
    public ModelAndView searchLast(@Valid ReportsForm reportsForm,
                                   Model model) throws Exception {
        if ((RecapConstants.REPORTS_INCOMPLETE_RECORDS).equals(reportsForm.getRequestType())) {
            reportsForm.setIncompletePageNumber(reportsForm.getIncompleteTotalPageCount() - 1);
            return getIncompleteRecords(reportsForm, model);
        } else {
            reportsForm.setPageNumber(reportsForm.getTotalPageCount() - 1);
            List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = reportsUtil.deaccessionReportFieldsInformation(reportsForm);
            reportsForm.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
            return new ModelAndView(RecapConstants.REPORTS_VIEW_DEACCESSION_INFORMARION, RecapConstants.REPORTS_FORM, reportsForm);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/reports/incompleteRecords", method = RequestMethod.POST)
    public ModelAndView incompleteRecordsReport(ReportsForm reportsForm,
                                                Model model) throws Exception {
        reportsForm.setIncompletePageNumber(0);
        return getIncompleteRecords(reportsForm, model);

    }

    @RequestMapping(value = "/reports/getInstitutions", method = RequestMethod.GET)
    public ModelAndView getInstitutionForIncompletereport(HttpServletRequest request, ReportsForm reportsForm) {
            List<String> instList = new ArrayList<>();
            List<InstitutionEntity> institutionCodeForSuperAdmin = institutionDetailsRepository.getInstitutionCodeForSuperAdmin();
            for (InstitutionEntity institutionEntity : institutionCodeForSuperAdmin) {
                instList.add(institutionEntity.getInstitutionCode());
            }
            reportsForm.setIncompleteShowByInst(instList);
        return new ModelAndView(RecapConstants.REPORTS_INCOMPLETE_SHOW_BY_VIEW, RecapConstants.REPORTS_FORM, reportsForm);
    }


    @ResponseBody
    @RequestMapping(value = "/reports/export", method = RequestMethod.POST)
    public byte[] exportIncompleteRecords(ReportsForm reportsForm, HttpServletResponse response, Model model) throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileNameWithExtension = RecapConstants.REPORTS_INCOMPLETE_EXPORT_FILE_NAME + dateFormat.format(new Date()) + ".csv";
        reportsForm.setExport(true);
        List<IncompleteReportResultsRow> incompleteReportResultsRows = reportsUtil.incompleteRecordsReportFieldsInformation(reportsForm);
        File csvFile = reportsUtil.exportIncompleteRecords(incompleteReportResultsRows, fileNameWithExtension);
        byte[] fileContent = IOUtils.toByteArray(new FileInputStream(csvFile));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileNameWithExtension + "\"");
        response.setContentLength(fileContent.length);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
        return fileContent;
    }

    @ResponseBody
    @RequestMapping(value = "/reports/incompleteReportPageSizeChange", method = RequestMethod.POST)
    public ModelAndView incompleteReportPageSizeChange(ReportsForm reportsForm,
                                                       Model model) throws Exception {
        reportsForm.setIncompletePageNumber(0);
        return getIncompleteRecords(reportsForm, model);
    }

    private ModelAndView getIncompleteRecords(ReportsForm reportsForm, Model model) throws Exception {
        List<IncompleteReportResultsRow> incompleteReportResultsRows = getReportsUtil().incompleteRecordsReportFieldsInformation(reportsForm);
        reportsForm.setIncompleteReportResultsRows(incompleteReportResultsRows);
        if (incompleteReportResultsRows.isEmpty()) {
            reportsForm.setShowIncompleteResults(false);
            reportsForm.setErrorMessage(RecapConstants.REPORTS_INCOMPLETE_RECORDS_NOT_FOUND);
        } else {
            reportsForm.setShowIncompleteResults(true);
            reportsForm.setShowIncompletePagination(true);
        }
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
        return new ModelAndView(RecapConstants.REPORTS_INCOMPLETE_RECORDS_VIEW, RecapConstants.REPORTS_FORM, reportsForm);
    }

}
