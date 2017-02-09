package org.recap.util;

import org.apache.commons.lang3.StringUtils;
import org.recap.RecapConstants;
import org.recap.model.jpa.RequestItemEntity;
import org.recap.model.search.RequestForm;
import org.recap.repository.jpa.RequestItemDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Created by rajeshbabuk on 29/10/16.
 */
@Service
public class RequestServiceUtil {

    Logger logger = LoggerFactory.getLogger(RequestServiceUtil.class);

    @Autowired
    RequestItemDetailsRepository requestItemDetailsRepository;

    public Page<RequestItemEntity> searchRequests(RequestForm requestForm) {
        String patronBarcode = StringUtils.isNotBlank(requestForm.getPatronBarcode()) ? requestForm.getPatronBarcode().trim() : requestForm.getPatronBarcode();
        String itemBarcode = StringUtils.isNotBlank(requestForm.getItemBarcode()) ? requestForm.getItemBarcode().trim() : requestForm.getItemBarcode();
        String status = StringUtils.isNotBlank(requestForm.getStatus()) ? requestForm.getStatus().trim() : requestForm.getStatus();
        Pageable pageable = new PageRequest(requestForm.getPageNumber(), requestForm.getPageSize(), Sort.Direction.DESC, RecapConstants.REQUEST_ID);

        Page<RequestItemEntity> requestItemEntities;
        if (StringUtils.isNotBlank(patronBarcode) && StringUtils.isNotBlank(itemBarcode) && StringUtils.isNotBlank(status)) {
            requestItemEntities = requestItemDetailsRepository.findByPatronBarcodeAndItemBarcodeAndStatus(pageable, patronBarcode, itemBarcode, status);
        } else if (StringUtils.isNotBlank(patronBarcode) && StringUtils.isNotBlank(itemBarcode) && StringUtils.isBlank(status)) {
            requestItemEntities = requestItemDetailsRepository.findByPatronBarcodeAndItemBarcodeAndActive(pageable, patronBarcode, itemBarcode);
        } else if (StringUtils.isNotBlank(patronBarcode) && StringUtils.isBlank(itemBarcode) && StringUtils.isNotBlank(status)) {
            requestItemEntities = requestItemDetailsRepository.findByPatronBarcodeAndStatus(pageable, patronBarcode, status);
        } else if (StringUtils.isBlank(patronBarcode) && StringUtils.isNotBlank(itemBarcode) && StringUtils.isNotBlank(status)) {
            requestItemEntities = requestItemDetailsRepository.findByItemBarcodeAndStatus(pageable, itemBarcode, status);
        } else if (StringUtils.isNotBlank(patronBarcode) && StringUtils.isBlank(itemBarcode) && StringUtils.isBlank(status)) {
            requestItemEntities = requestItemDetailsRepository.findByPatronBarcodeAndActive(pageable, patronBarcode);
        } else if (StringUtils.isBlank(patronBarcode) && StringUtils.isNotBlank(itemBarcode) && StringUtils.isBlank(status)) {
            requestItemEntities = requestItemDetailsRepository.findByItemBarcodeAndActive(pageable, itemBarcode);
        } else if (StringUtils.isBlank(patronBarcode) && StringUtils.isBlank(itemBarcode) && StringUtils.isNotBlank(status)) {
            requestItemEntities = requestItemDetailsRepository.findByStatus(pageable, status);
        } else {
            requestItemEntities = requestItemDetailsRepository.findAllActive(pageable);
        }
        return requestItemEntities;
    }
}
