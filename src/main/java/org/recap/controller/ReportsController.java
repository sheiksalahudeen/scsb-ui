package org.recap.controller;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.recap.model.search.DeaccessionItemResultsRow;
import org.recap.model.search.ReportsForm;
import org.recap.security.UserManagement;
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
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
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
        HttpSession session=request.getSession();
        boolean authenticated=getUserAuthUtil().authorizedUser(RecapConstants.SCSB_SHIRO_REPORT_URL,(UsernamePasswordToken)session.getAttribute(UserManagement.USER_TOKEN));
        if(authenticated)
        {
            ReportsForm reportsForm = new ReportsForm();
            model.addAttribute(RecapConstants.REPORTS_FORM, reportsForm);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
            return RecapConstants.VIEW_SEARCH_RECORDS;
        }else{
            return UserManagement.unAuthorizedUser(session,"Reports",logger);
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
    @RequestMapping(value = "/reports/first", method = RequestMethod.GET)
    public ModelAndView searchFirst(@Valid ReportsForm reportsForm,
                                    Model model) throws Exception {
        reportsForm.setPageNumber(0);
        List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = reportsUtil.deaccessionReportFieldsInformation(reportsForm);
        reportsForm.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
        return new ModelAndView(RecapConstants.REPORTS_VIEW_DEACCESSION_INFORMARION, RecapConstants.REPORTS_FORM, reportsForm);
    }

    @ResponseBody
    @RequestMapping(value = "/reports/previous", method = RequestMethod.GET)
    public ModelAndView searchPrevious(@Valid ReportsForm reportsForm,
                                       Model model) throws Exception {
        List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = reportsUtil.deaccessionReportFieldsInformation(reportsForm);
        reportsForm.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
        return new ModelAndView(RecapConstants.REPORTS_VIEW_DEACCESSION_INFORMARION, RecapConstants.REPORTS_FORM, reportsForm);
    }


    @ResponseBody
    @RequestMapping(value = "/reports/next", method = RequestMethod.GET)
    public ModelAndView searchNext(@Valid ReportsForm reportsForm,
                                   Model model) throws Exception {
        List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = reportsUtil.deaccessionReportFieldsInformation(reportsForm);
        reportsForm.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
        return new ModelAndView(RecapConstants.REPORTS_VIEW_DEACCESSION_INFORMARION, RecapConstants.REPORTS_FORM, reportsForm);
    }


    @ResponseBody
    @RequestMapping(value = "/reports/last", method = RequestMethod.GET)
    public ModelAndView searchLast(@Valid ReportsForm reportsForm,
                                   Model model) throws Exception {
        reportsForm.setPageNumber(reportsForm.getTotalPageCount() - 1);
        List<DeaccessionItemResultsRow> deaccessionItemResultsRowList = reportsUtil.deaccessionReportFieldsInformation(reportsForm);
        reportsForm.setDeaccessionItemResultsRows(deaccessionItemResultsRowList);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.REPORTS);
        return new ModelAndView(RecapConstants.REPORTS_VIEW_DEACCESSION_INFORMARION, RecapConstants.REPORTS_FORM, reportsForm);
    }
}
