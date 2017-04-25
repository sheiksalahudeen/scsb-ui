package org.recap.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.model.jpa.CustomerCodeEntity;
import org.recap.model.jpa.DeliveryRestrictionEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.search.RequestForm;
import org.recap.model.usermanagement.UserDetailsForm;
import org.recap.repository.jpa.CustomerCodeDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by akulak on 20/4/17.
 */
@Service
public class RequestService {

    @Autowired
    CustomerCodeDetailsRepository customerCodeDetailsRepository;


    public void processCustomerAndDeliveryCodes(RequestForm requestForm, Map<String, String> deliveryLocationsMap, UserDetailsForm userDetailsForm, ItemEntity itemEntity, Integer institutionId) {
        String customerCode = itemEntity.getCustomerCode();
        CustomerCodeEntity customerCodeEntity = customerCodeDetailsRepository.findByCustomerCodeAndOwningInstitutionId(customerCode, institutionId);
        if(requestForm.getItemOwningInstitution().equals(requestForm.getRequestingInstitution())){
            if (customerCodeEntity != null) {
                String deliveryRestrictions = customerCodeEntity.getDeliveryRestrictions();
                if (StringUtils.isNotBlank(deliveryRestrictions)) {
                    String[] deliverLocationsArray = deliveryRestrictions.split(",");
                    addDeliveryLocations(deliveryLocationsMap,deliverLocationsArray);
                }
            }
        }
        else{
            addDeliveryLocationsForCrossPartner(requestForm, deliveryLocationsMap,customerCodeEntity);
        }
        addRecapDeliveryRestrictions(deliveryLocationsMap, userDetailsForm, customerCodeEntity);
    }

    private void addDeliveryLocationsForCrossPartner(RequestForm requestForm, Map<String, String> deliveryLocationsMap,CustomerCodeEntity customerCodeEntity) {
        if (customerCodeEntity != null) {
            List<DeliveryRestrictionEntity> deliveryRestrictionEntityList = customerCodeEntity.getDeliveryRestrictionEntityList();
            if(CollectionUtils.isNotEmpty(deliveryRestrictionEntityList)){
                for (DeliveryRestrictionEntity deliveryRestrictionEntity : deliveryRestrictionEntityList) {
                    if(requestForm.getRequestingInstitution().equals(deliveryRestrictionEntity.getInstitutionEntity().getInstitutionCode())){
                        String deliveryRestriction = deliveryRestrictionEntity.getDeliveryRestriction();
                        String[] splitDeliveryLocation = StringUtils.split(deliveryRestriction, ",");
                        addDeliveryLocations(deliveryLocationsMap,splitDeliveryLocation);
                    }
                }
            }
        }
    }

    private void addDeliveryLocations(Map<String, String> deliveryLocationsMap, String[] deliveryRestrictions) {
        String[] deliveryRestrictionsArray = Arrays.stream(deliveryRestrictions).map(String::trim).toArray(String[]::new);
        List<CustomerCodeEntity> deliveryRestrictionsList = customerCodeDetailsRepository.findByCustomerCodeIn(Arrays.asList(deliveryRestrictionsArray));
        if (CollectionUtils.isNotEmpty(deliveryRestrictionsList)) {
            Collections.sort(deliveryRestrictionsList);
            for (CustomerCodeEntity customerCodeEntity : deliveryRestrictionsList) {
                if (customerCodeEntity != null){
                    deliveryLocationsMap.put(customerCodeEntity.getCustomerCode(), customerCodeEntity.getDescription());
                }
            }
        }
    }

    private void addRecapDeliveryRestrictions(Map<String, String> deliveryLocationsMap, UserDetailsForm userDetailsForm, CustomerCodeEntity customerCodeEntity) {
        if(userDetailsForm.isRecapUser()){
            String recapDeliveryRestrictions = customerCodeEntity.getRecapDeliveryRestrictions();
            String[] recapDeliveryRestrictionsArray = recapDeliveryRestrictions.split(",");
            addDeliveryLocations(deliveryLocationsMap, recapDeliveryRestrictionsArray);
        }
    }

}
