package org.recap.controller;

import org.apache.commons.lang.StringUtils;
import org.recap.RecapConstants;
import org.recap.model.jpa.JobEntity;
import org.recap.model.schedule.ScheduleJobRequest;
import org.recap.model.schedule.ScheduleJobResponse;
import org.recap.model.search.ScheduleJobsForm;
import org.recap.model.usermanagement.UserDetailsForm;
import org.recap.repository.jpa.JobDetailsRepository;
import org.recap.security.UserManagementService;
import org.recap.util.UserAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

/**
 * Created by rajeshbabuk on 4/4/17.
 */

@Controller
public class ScheduleJobsController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleJobsController.class);

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.url}")
    String scsbUrl;

    @Autowired
    private UserAuthUtil userAuthUtil;

    @Autowired
    private JobDetailsRepository jobDetailsRepository;

    public UserAuthUtil getUserAuthUtil() {
        return userAuthUtil;
    }

    public void setUserAuthUtil(UserAuthUtil userAuthUtil) {
        this.userAuthUtil = userAuthUtil;
    }

    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @RequestMapping("/jobs")
    public String displayJobs(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        ScheduleJobsForm scheduleJobsForm = new ScheduleJobsForm();
        UserDetailsForm userDetailsForm = getUserAuthUtil().getUserDetails(session, RecapConstants.BARCODE_RESTRICTED_PRIVILEGE);
        if (userDetailsForm.isSuperAdmin()) {
            List<JobEntity> jobEntities = jobDetailsRepository.findAll();
            scheduleJobsForm.setJobEntities(jobEntities);
        } else {
            return UserManagementService.unAuthorizedUser(session, RecapConstants.SEARCH, logger);
        }
        model.addAttribute(RecapConstants.SCHEDULE_JOBS_FORM, scheduleJobsForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SCHEDULE_JOBS);
        return RecapConstants.VIEW_SEARCH_RECORDS;
    }

    @ResponseBody
    @RequestMapping(value = "/jobs", method = RequestMethod.POST, params = "action=scheduleJob")
    public ModelAndView scheduleJob(@Valid @ModelAttribute("scheduleJobsForm") ScheduleJobsForm scheduleJobsForm,
                                    BindingResult result,
                                    Model model) {

        ScheduleJobResponse scheduleJobResponse = null;
        try {
            ScheduleJobRequest scheduleJobRequest = new ScheduleJobRequest();
            scheduleJobRequest.setJobId(scheduleJobsForm.getJobId());
            scheduleJobRequest.setJobName(scheduleJobsForm.getJobName());
            scheduleJobRequest.setCronExpression(scheduleJobsForm.getCronExpression());
            scheduleJobRequest.setScheduleType(scheduleJobsForm.getScheduleType());
            HttpEntity<ScheduleJobRequest> httpEntity = new HttpEntity<>(scheduleJobRequest, RecapConstants.getHttpHeaders());

            ResponseEntity<ScheduleJobResponse> responseEntity = getRestTemplate().exchange(serverProtocol + scsbUrl + RecapConstants.URL_SCHEDULE_JOBS, HttpMethod.POST, httpEntity, ScheduleJobResponse.class);
            scheduleJobResponse = responseEntity.getBody();
            String message = scheduleJobResponse.getMessage();
            if (StringUtils.containsIgnoreCase(message, RecapConstants.SUCCESS)) {
                JobEntity jobEntity = jobDetailsRepository.findByJobName(scheduleJobsForm.getJobName());
                if (null != jobEntity) {
                    if (RecapConstants.UNSCHEDULE.equals(scheduleJobsForm.getScheduleType())) {
                        jobEntity.setStatus(RecapConstants.UNSCHEDULED);
                        jobEntity.setCronExpression(null);
                    } else {
                        jobEntity.setStatus(RecapConstants.SCHEDULED);
                        jobEntity.setCronExpression(scheduleJobsForm.getCronExpression());
                    }
                    jobDetailsRepository.save(jobEntity);
                }
                scheduleJobsForm.setMessage(scheduleJobResponse.getMessage());
            } else {
                scheduleJobsForm.setErrorMessage(scheduleJobResponse.getMessage());
            }
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR, e);
            scheduleJobsForm.setErrorMessage(e.getMessage());
        }

        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.SCHEDULE_JOBS);
        return new ModelAndView(RecapConstants.VIEW_SCHEDULE_JOB_SECTION, RecapConstants.SCHEDULE_JOBS_FORM, scheduleJobsForm);
    }
}
