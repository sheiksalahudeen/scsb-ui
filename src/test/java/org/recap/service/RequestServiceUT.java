package org.recap.service;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.CustomerCodeEntity;
import org.recap.model.jpa.DeliveryRestrictionEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.model.search.RequestForm;
import org.recap.model.usermanagement.UserDetailsForm;
import org.recap.repository.jpa.CustomerCodeDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by akulak on 24/4/17.
 */
public class RequestServiceUT extends BaseTestCase {

    @Autowired
    RequestService requestService;

    @Autowired
    CustomerCodeDetailsRepository customerCodeDetailsRepository;

    @Test
    public void testDeliveryLocations() throws Exception{
        RequestForm requestForm = getRequestForm();
        ItemEntity itemEntity = getItemEntity();
        UserDetailsForm userDetailsForm = getUserDetailsForm(false);
        Map<String, String> deliveryLocationsMap = new HashMap<>();
        requestService.processCustomerAndDeliveryCodes(requestForm,deliveryLocationsMap,userDetailsForm,itemEntity,1);
        List<String> deliveryLocationList = new ArrayList<>();
        for(String deliveryLocation : deliveryLocationsMap.keySet()){
            deliveryLocationList.add(deliveryLocation);
        }
        CustomerCodeEntity CustomerCode = customerCodeDetailsRepository.findByCustomerCode(itemEntity.getCustomerCode());
        String deliveryRestrictions = CustomerCode.getDeliveryRestrictions();
        String[] splitDeliveryLocation = StringUtils.split(deliveryRestrictions, ",");
        String[] deliveryRestrictionsArray = Arrays.stream(splitDeliveryLocation).map(String::trim).toArray(String[]::new);
        assertTrue(deliveryLocationList.containsAll(Arrays.asList(deliveryRestrictionsArray)) && Arrays.asList(deliveryRestrictionsArray).containsAll(deliveryLocationList));
    }

    @Test
    public void testDeliveryLocationsForRecapUser() throws Exception{
        RequestForm requestForm = getRequestForm();
        ItemEntity itemEntity = getItemEntity();
        UserDetailsForm userDetailsForm = getUserDetailsForm(true);
        Map<String, String> deliveryLocationsMap = new HashMap<>();
        requestService.processCustomerAndDeliveryCodes(requestForm,deliveryLocationsMap,userDetailsForm,itemEntity,1);
        List<String> deliveryLocationList = new ArrayList<>();
        for(String deliveryLocation : deliveryLocationsMap.keySet()){
            deliveryLocationList.add(deliveryLocation);
        }
        CustomerCodeEntity customerCode = customerCodeDetailsRepository.findByCustomerCode(itemEntity.getCustomerCode());
        String deliveryRestrictions = customerCode.getDeliveryRestrictions();
        String recapDeliveryRestrictions = customerCode.getRecapDeliveryRestrictions();
        String[] deliveryRestrictionSplit = StringUtils.split(deliveryRestrictions, ",");
        String[] recapDeliveryLocationSplit = StringUtils.split(recapDeliveryRestrictions, ",");
        String[] deliveryRestrictionsArray = Arrays.stream(deliveryRestrictionSplit).map(String::trim).toArray(String[]::new);
        String[] recapDeliveryRestrictionsArray = Arrays.stream(recapDeliveryLocationSplit).map(String::trim).toArray(String[]::new);
        List<String> deliveryLocationsList= new ArrayList<>(Arrays.asList(deliveryRestrictionsArray));
        deliveryLocationsList.addAll(Arrays.asList(recapDeliveryRestrictionsArray));
        assertTrue(deliveryLocationList.containsAll(deliveryLocationsList) && deliveryLocationsList.containsAll(deliveryLocationList));
    }

    private ItemEntity getItemEntity() {
        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setCustomerCode("PA");
        return itemEntity;
    }

    private RequestForm getRequestForm() {
        RequestForm requestForm = new RequestForm();
        requestForm.setItemOwningInstitution("PUL");
        requestForm.setRequestingInstitution("PUL");
        return requestForm;
    }

    private UserDetailsForm getUserDetailsForm(boolean recapUser) {
        UserDetailsForm userDetailsForm = new UserDetailsForm();
        userDetailsForm.setRecapUser(recapUser);
        return userDetailsForm;
    }



}
