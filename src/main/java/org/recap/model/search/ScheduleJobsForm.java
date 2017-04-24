package org.recap.model.search;

import org.recap.model.jpa.JobEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajeshbabuk on 4/4/17.
 */
public class ScheduleJobsForm {

    private Integer jobId;
    private String jobName;
    private String jobDescription;
    private String cronExpression;
    private String scheduleType;
    private String message;
    private String errorMessage;
    private List<JobEntity> jobEntities = new ArrayList<>();

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<JobEntity> getJobEntities() {
        return jobEntities;
    }

    public void setJobEntities(List<JobEntity> jobEntities) {
        this.jobEntities = jobEntities;
    }
}
