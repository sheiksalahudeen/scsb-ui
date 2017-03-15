package org.recap.util;

import org.recap.RecapConstants;
import org.recap.model.reports.ReportsRequest;
import org.recap.model.reports.ReportsResponse;
import org.recap.model.search.ReportsForm;
import org.recap.model.search.SearchRecordsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Created by rajeshbabuk on 13/1/17.
 */
@Service
public class ReportsServiceUtil {

    private static final Logger logger = LoggerFactory.getLogger(SearchUtil.class);

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.url}")
    String scsbUrl;

    public ReportsResponse requestAccessionDeaccessionCounts(ReportsForm reportsForm) {
        ReportsRequest reportsRequest = new ReportsRequest();
        reportsRequest.setAccessionDeaccessionFromDate(reportsForm.getAccessionDeaccessionFromDate());
        reportsRequest.setAccessionDeaccessionToDate(reportsForm.getAccessionDeaccessionToDate());
        reportsRequest.setOwningInstitutions(reportsForm.getOwningInstitutions());
        reportsRequest.setCollectionGroupDesignations(reportsForm.getCollectionGroupDesignations());
        ReportsResponse reportsResponse = new ReportsResponse();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(RecapConstants.API_KEY, RecapConstants.RECAP);
            HttpEntity<ReportsRequest> httpEntity = new HttpEntity<>(reportsRequest, headers);

            ResponseEntity<ReportsResponse> responseEntity = restTemplate.exchange(serverProtocol + scsbUrl + RecapConstants.SCSB_REPORTS_ACCESSION_DEACCESSION_COUNTS_URL, HttpMethod.POST, httpEntity, ReportsResponse.class);
            reportsResponse = responseEntity.getBody();
            return reportsResponse;
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR,e);
            reportsResponse.setMessage(e.getMessage());
            return reportsResponse;
        }
    }

    public ReportsResponse requestCgdItemCounts(ReportsForm reportsForm) {
        ReportsRequest reportsRequest = new ReportsRequest();
        reportsRequest.setOwningInstitutions(reportsForm.getOwningInstitutions());
        reportsRequest.setCollectionGroupDesignations(reportsForm.getCollectionGroupDesignations());
        ReportsResponse reportsResponse = new ReportsResponse();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(RecapConstants.API_KEY, RecapConstants.RECAP);
            HttpEntity<ReportsRequest> httpEntity = new HttpEntity<>(reportsRequest, headers);

            ResponseEntity<ReportsResponse> responseEntity = restTemplate.exchange(serverProtocol + scsbUrl + RecapConstants.SCSB_REPORTS_CGD_ITEM_COUNTS_URL, HttpMethod.POST, httpEntity, ReportsResponse.class);
            reportsResponse = responseEntity.getBody();
            return reportsResponse;
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR,e);
            reportsResponse.setMessage(e.getMessage());
            return reportsResponse;
        }
    }

    public ReportsResponse requestDeaccessionResults(ReportsForm reportsForm) {
        ReportsRequest reportsRequest = new ReportsRequest();
        reportsRequest.setAccessionDeaccessionFromDate(reportsForm.getAccessionDeaccessionFromDate());
        reportsRequest.setAccessionDeaccessionToDate(reportsForm.getAccessionDeaccessionToDate());
        reportsRequest.setDeaccessionOwningInstitution(reportsForm.getDeaccessionOwnInst());
        reportsRequest.setPageNumber(reportsForm.getPageNumber());
        reportsRequest.setPageSize(reportsForm.getPageSize());

        ReportsResponse reportsResponse = new ReportsResponse();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(RecapConstants.API_KEY, RecapConstants.RECAP);
            HttpEntity<ReportsRequest> httpEntity = new HttpEntity<>(reportsRequest, headers);

            ResponseEntity<ReportsResponse> responseEntity = restTemplate.exchange(serverProtocol + scsbUrl + RecapConstants.SCSB_REPORTS_DEACCESSION_RESULTS_URL, HttpMethod.POST, httpEntity, ReportsResponse.class);
            reportsResponse = responseEntity.getBody();
            return reportsResponse;
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR,e);
            reportsResponse.setMessage(e.getMessage());
            return reportsResponse;
        }
    }

    public ReportsResponse requestIncompleteRecords(ReportsForm reportsForm) {
        ReportsRequest reportsRequest = new ReportsRequest();
        reportsRequest.setIncompleteRequestingInstitution(reportsForm.getIncompleteRequestingInstitution());
        reportsRequest.setIncompletePageNumber(reportsForm.getIncompletePageNumber());
        reportsRequest.setIncompletePageSize(reportsForm.getIncompletePageSize());
        reportsRequest.setExport(reportsForm.isExport());
        ReportsResponse reportsResponse = new ReportsResponse();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(RecapConstants.API_KEY, RecapConstants.RECAP);
            HttpEntity<ReportsRequest> httpEntity = new HttpEntity<>(reportsRequest, headers);
            ResponseEntity<ReportsResponse> responseEntity = restTemplate.exchange(serverProtocol + scsbUrl + RecapConstants.SCSB_REPORTS_INCOMPLETE_RESULTS_URL, HttpMethod.POST, httpEntity, ReportsResponse.class);
            reportsResponse = responseEntity.getBody();
            return reportsResponse;
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR,e);
            reportsResponse.setMessage(e.getMessage());
            return reportsResponse;
        }
    }



}
