package org.recap.controller;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.recap.RecapConstants;
import org.recap.model.jpa.PermissionEntity;
import org.recap.model.jpa.RoleEntity;
import org.recap.model.search.RolesForm;
import org.recap.model.search.RolesSearchResult;
import org.recap.repository.jpa.PermissionsDetailsRepository;
import org.recap.repository.jpa.RolesDetailsRepositorty;
import org.recap.security.UserManagement;
import org.recap.util.UserAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hemalathas on 22/12/16.
 */
@Controller
public class RolesController {

    private static final Logger logger = LoggerFactory.getLogger(RolesController.class);

    @Autowired
    RolesDetailsRepositorty rolesDetailsRepositorty;

    @Autowired
    PermissionsDetailsRepository permissionsRepository;

    @Autowired
    private UserAuthUtil userAuthUtil;

    @Autowired
    HttpServletRequest request;

    @RequestMapping("/roles")
    public String collection(Model model, HttpServletRequest request) {
        HttpSession session=request.getSession();
        boolean authenticated=userAuthUtil.authorizedUser(RecapConstants.SCSB_SHIRO_ROLE_URL,(UsernamePasswordToken)session.getAttribute(UserManagement.USER_TOKEN));
        if(authenticated)
        {
            RolesForm rolesForm = new RolesForm();
            model.addAttribute(RecapConstants.ROLES_FORM, rolesForm);
            model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.ROLES);
            return RecapConstants.VIEW_SEARCH_RECORDS;
        }else{
            return UserManagement.unAuthorizedUser(session,"Roles",logger);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.POST, params = "action=searchRoles")
    public ModelAndView search(@Valid @ModelAttribute("rolesForm") RolesForm rolesForm,
                               Model model) {
        rolesForm.setShowResults(true);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.ROLES);
        setRolesFormSearchResults(rolesForm);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.POST, params = "action=populatePermissionName")
    public ModelAndView populatePermissionNames(Model model) {
        RolesForm rolesForm = getAllPermissionNames();
        model.addAttribute(RecapConstants.ROLES_FORM, rolesForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.ROLES);
        return new ModelAndView(RecapConstants.ROLES, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.POST, params = "action=loadCreateRole")
    public ModelAndView newRole(@Valid @ModelAttribute("rolesForm") RolesForm rolesForm,
                                Model model) {
        boolean specialCharacterCheck = isSpecialCharacterCheck(rolesForm.getNewRoleName());
        if(specialCharacterCheck == false){
            rolesForm.setErrorMessage(RecapConstants.SPECIAL_CHARACTERS_NOT_ALLOWED_CREATE);
            rolesForm.setSelectedPermissionNames(getSeletedPermissionNames(rolesForm.getNewPermissionNames()));
        }
        else{
            RoleEntity roleEntity = saveNewRoleToDB(rolesForm);
            if(null != roleEntity){
                rolesForm.setMessage(rolesForm.getNewRoleName()+RecapConstants.ROLES_ADD_SUCCESS_MESSAGE);
            }else{
                rolesForm.setErrorMessage(rolesForm.getNewRoleName()+RecapConstants.ROLES_DUPLICATE_MESSAGE);
            }
            rolesForm.setNewRoleName("");
            rolesForm.setNewRoleDescription("");
        }
        rolesForm.setPermissionNameList(getAllPermissionNames().getPermissionNameList());
        rolesForm.setShowIntial(false);
        return new ModelAndView(RecapConstants.ROLES, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.GET, params = "action=editRole")
    public ModelAndView editRole(Integer roleId, String roleName, String roleDescription, String permissionName) {
        permissionName = HtmlUtils.htmlUnescape(permissionName);
        RolesForm rolesForm = getAllPermissionNames();
        rolesForm.setRoleId(roleId);
        rolesForm.setEditRoleName(roleName);
        rolesForm.setEditRoleDescription(roleDescription);
        rolesForm.setEditPermissionNames(permissionName);
        rolesForm.setSelectedPermissionNames(getSeletedPermissionNames(permissionName));
        rolesForm.setShowIntial(false);
        return new ModelAndView(RecapConstants.ROLES, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.GET, params = "action=saveEditedRole")
    public ModelAndView saveEditedRole(@ModelAttribute("roleId") Integer roleId,
                                       @ModelAttribute("roleName") String roleName,
                                       @ModelAttribute("roleDescription") String roleDescription,
                                      HttpServletRequest request) {
        RolesForm rolesForm = new RolesForm();
        HttpSession session = request.getSession();
        rolesForm.setRoleId(roleId);
        rolesForm.setEditRoleName(roleName);
        rolesForm.setEditRoleDescription(roleDescription);
        String[] editPermissionNames = request.getParameterValues("permissionNames[]");
        rolesForm.setEditPermissionName(Arrays.asList(editPermissionNames));
        RoleEntity roleEntityByRoleId = rolesDetailsRepositorty.findByRoleId(roleId);
        roleEntityByRoleId.setRoleId(roleId);
        roleEntityByRoleId.setRoleName(roleName);
        roleEntityByRoleId.setRoleDescription(roleDescription);
        roleEntityByRoleId.setLastUpdatedDate(new Date());
        roleEntityByRoleId.setLastUpdatedBy(String.valueOf(session.getAttribute(UserManagement.USER_NAME)));
        RoleEntity roleEntity = saveRoleEntity(roleEntityByRoleId, Arrays.asList(editPermissionNames));
            if(null != roleEntity){
                rolesForm.setMessage(rolesForm.getEditRoleName()+RecapConstants.ROLES_EDIT_SAVE_SUCCESS_MESSAGE);
            }
            else{
            rolesForm.setErrorMessage(rolesForm.getEditRoleName()+RecapConstants.ROLES_DUPLICATE_MESSAGE);
        }
        rolesForm.setPermissionNameList(getAllPermissionNames().getPermissionNameList());
        rolesForm.setSelectedPermissionNames(Arrays.asList(editPermissionNames));
        rolesForm.setShowIntial(false);
        return new ModelAndView(RecapConstants.ROLES, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.GET, params = "action=deleteRole")
    public ModelAndView deleteRole(Integer roleId, String roleName, String roleDescription, String permissionName,
                                   Integer pageSize,Integer pageNumber,Integer totalPageCount) {
        permissionName = HtmlUtils.htmlUnescape(permissionName);
        RolesForm rolesForm = getAllPermissionNames();
        rolesForm.setAfterDelPageSize(pageSize);
        rolesForm.setAfterDelPageNumber(pageNumber);
        rolesForm.setAfterDelTotalPageCount(totalPageCount);
        rolesForm.setRoleId(roleId);
        rolesForm.setRoleNameForDelete(roleName);
        rolesForm.setRoleDescriptionForDelete(roleDescription);
        rolesForm.setPermissionNamesForDelete(permissionName);
        rolesForm.setSelectedPermissionNames(getSeletedPermissionNames(permissionName));
        rolesForm.setPageSize(pageSize);
        rolesForm.setPageNumber(pageNumber);
        rolesForm.setTotalPageCount(totalPageCount);
        rolesForm.setShowIntial(false);
        return new ModelAndView(RecapConstants.ROLES, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.GET, params = "action=delete")
    public ModelAndView delete(@Valid @ModelAttribute("rolesForm") RolesForm rolesForm,
                               Model model) {
        RoleEntity roleEntity = rolesDetailsRepositorty.findByRoleId(rolesForm.getRoleId());
        try {
            rolesDetailsRepositorty.delete(roleEntity);
            rolesForm.setShowResults(true);
            rolesForm.setPageNumber(rolesForm.getAfterDelPageNumber());
            rolesForm.setPageSize(rolesForm.getAfterDelPageSize());
            rolesForm.setTotalPageCount(rolesForm.getAfterDelTotalPageCount());
            rolesForm.setRoleName("");
            rolesForm.setPermissionNames("");
            setRolesFormSearchResults(rolesForm);
            rolesForm.setMessage(rolesForm.getRoleNameForDelete()+RecapConstants.ROLES_DELETED_SUCCESS_MESSAGE);
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR,e);
        }
        rolesForm.setShowResults(true);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.ROLES);
        return new ModelAndView(RecapConstants.ROLES, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.POST, params = "action=previous")
    public ModelAndView searchPrevious(@Valid @ModelAttribute("rolesForm") RolesForm rolesForm,
                                       Model model) {
        rolesForm.setShowResults(true);
        findByPagination(rolesForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.ROLES);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.POST, params = "action=next")
    public ModelAndView searchNext(@Valid @ModelAttribute("rolesForm") RolesForm rolesForm,
                                   Model model) {
        rolesForm.setShowResults(true);
        findByPagination(rolesForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.ROLES);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.POST, params = "action=first")
    public ModelAndView searchFirst(@Valid @ModelAttribute("rolesForm") RolesForm rolesForm,
                                    Model model) {
        rolesForm.setShowResults(true);
        rolesForm.resetPageNumber();
        findByPagination(rolesForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.ROLES);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.POST, params = "action=last")
    public ModelAndView searchLast(@Valid @ModelAttribute("rolesForm") RolesForm rolesForm,
                                   Model model) {
        rolesForm.setShowResults(true);
        rolesForm.setPageNumber(rolesForm.getTotalPageCount() - 1);
        findByPagination(rolesForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.ROLES);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.POST, params = "action=clearPage")
    public ModelAndView clearPage(@Valid @ModelAttribute("rolesForm") RolesForm rolesForm,
                                  Model model) {
        rolesForm.setNewRoleName("");
        rolesForm.setNewRoleDescription("");
        rolesForm.setNewPermissionNames("");
        rolesForm.setEditRoleName("");
        rolesForm.setEditRoleDescription("");
        rolesForm.setEditPermissionNames("");
        rolesForm.setPermissionNameList(getAllPermissionNames().getPermissionNameList());
        rolesForm.setErrorMessage("");
        rolesForm.setMessage("");
        rolesForm.setSelectedPermissionNames(new ArrayList<String>());
        rolesForm.setShowIntial(false);
        return new ModelAndView(RecapConstants.ROLES, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.POST, params = "action=editClearPage")
    public ModelAndView editClearPage(@Valid @ModelAttribute("rolesForm") RolesForm rolesForm,
                                      Model model) {
        RoleEntity roleEntity = rolesDetailsRepositorty.findByRoleId(rolesForm.getRoleId());
        rolesForm.setEditRoleName(roleEntity.getRoleName());
        rolesForm.setEditRoleDescription(roleEntity.getRoleDescription());
        rolesForm.setEditPermissionNames("");
        rolesForm.setPermissionNameList(getAllPermissionNames().getPermissionNameList());
        rolesForm.setErrorMessage("");
        rolesForm.setMessage("");
        List<String> permissionEntityList = new ArrayList<>();
        Set<PermissionEntity> permissions = roleEntity.getPermissions();
        for(PermissionEntity permission : permissions){
            permissionEntityList.add(permission.getPermissionName());
        }
        rolesForm.setSelectedPermissionNames(permissionEntityList);
        rolesForm.setShowIntial(false);
        return new ModelAndView(RecapConstants.ROLES, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.POST, params = "action=pageSizeChange")
    public ModelAndView onPageSizeChange(@Valid @ModelAttribute("rolesForm") RolesForm rolesForm,
                                         Model model) throws Exception {
        rolesForm.setShowResults(true);
        rolesForm.setPageNumber(0);
        findByPagination(rolesForm);
        model.addAttribute(RecapConstants.TEMPLATE, RecapConstants.ROLES);
        return new ModelAndView(RecapConstants.VIEW_SEARCH_RECORDS, RecapConstants.ROLES_FORM, rolesForm);
    }

    @ResponseBody
    @RequestMapping(value = "/roles", method = RequestMethod.POST, params = "action=goBack")
    public ModelAndView goBack(RolesForm rolesForm,Model model){
        rolesForm.setShowIntial(true);
        return new ModelAndView("roles", "rolesForm", rolesForm);
    }

    public void setRolesFormSearchResults(RolesForm rolesForm) {
        List<RolesSearchResult> rolesSearchResults = new ArrayList<>();
        rolesForm.reset();
        if (rolesForm.getRoleName().equalsIgnoreCase(RecapConstants.ROLES_SUPER_ADMIN) || rolesForm.getPermissionNames().equalsIgnoreCase(RecapConstants.ROLES_SUPER_ADMIN)) {
            if (rolesForm.getRoleName().equalsIgnoreCase(RecapConstants.ROLES_SUPER_ADMIN)){
                rolesForm.setErrorMessage(RecapConstants.INVALID_ROLE_NAME);
            }
            else{
                rolesForm.setErrorMessage(RecapConstants.INVALID_PERMISSION);
            }

        } else if (!StringUtils.isEmpty(rolesForm.getRoleName()) && StringUtils.isEmpty(rolesForm.getPermissionNames())) {
            if (isSpecialCharacterCheck(rolesForm.getRoleName())) {
                Pageable pageable = new PageRequest(rolesForm.getPageNumber(), rolesForm.getPageSize());
                Page<RoleEntity> rolesEntityListByPagination = rolesDetailsRepositorty.findByRoleName(pageable, rolesForm.getRoleName());
                List<RoleEntity> roleEntityList = rolesEntityListByPagination.getContent();
                rolesForm.setTotalRecordCount(NumberFormat.getNumberInstance().format(rolesEntityListByPagination.getTotalElements()));
                rolesForm.setTotalPageCount(rolesEntityListByPagination.getTotalPages());
                if (null != roleEntityList && !roleEntityList.isEmpty()) {
                    for (RoleEntity roleEntity : roleEntityList) {
                        rolesForm.setTotalRecordCount(String.valueOf(1));
                        RolesSearchResult rolesSearchResult = getRolesSearchResult(roleEntity);
                        rolesSearchResults.add(rolesSearchResult);
                        rolesForm.setRolesSearchResults(rolesSearchResults);
                    }
                } else {
                    rolesForm.setErrorMessage(RecapConstants.INVALID_ROLE_NAME);
                }
            } else {
                rolesForm.setErrorMessage(RecapConstants.SPECIAL_CHARACTERS_NOT_ALLOWED);
            }
            rolesForm.setPageSize(10);
        } else if (!StringUtils.isEmpty(rolesForm.getPermissionNames()) && StringUtils.isEmpty(rolesForm.getRoleName())) {
                if (isSpecialCharacterCheck(rolesForm.getPermissionNames())) {
                    Page<RoleEntity> roleEntity=null;
                    Pageable pageable = new PageRequest(rolesForm.getPageNumber(), rolesForm.getPageSize());
                    PermissionEntity pemissionEntity = permissionsRepository.findByPermissionName(rolesForm.getPermissionNames());
                    getResultsForNonEmptyRolePermissionName(rolesForm, rolesSearchResults, roleEntity, pageable, pemissionEntity);
                }
            } else if (!StringUtils.isEmpty(rolesForm.getRoleName()) && !StringUtils.isEmpty(rolesForm.getPermissionNames())) {
                if (isSpecialCharacterCheck(rolesForm.getPermissionNames())) {
                    RoleEntity roleEntity = rolesDetailsRepositorty.findByRoleName(rolesForm.getRoleName());
                    if (null != roleEntity) {
                        boolean isExist = false;
                        RolesSearchResult rolesSearchResult = new RolesSearchResult();
                        for (PermissionEntity permissionEnt : roleEntity.getPermissions()) {
                            if (rolesForm.getPermissionNames().equalsIgnoreCase(permissionEnt.getPermissionName())) {
                                isExist = true;
                            }
                        }
                        if (isExist) {
                            StringBuilder allPermissions = new StringBuilder();
                            for (PermissionEntity permissionEnt : roleEntity.getPermissions()) {
                                allPermissions.append(permissionEnt.getPermissionName());
                                allPermissions.append(", ");
                            }
                            rolesForm.setTotalPageCount(1);
                            rolesForm.setTotalRecordCount(String.valueOf(1));
                            rolesForm.setPageSize(10);
                            rolesSearchResult.setRoleId(roleEntity.getRoleId());
                            rolesSearchResult.setPermissionName(allPermissions.toString());
                            rolesSearchResult.setRolesName(roleEntity.getRoleName());
                            rolesSearchResult.setRolesDescription(roleEntity.getRoleDescription());
                            rolesSearchResults.add(rolesSearchResult);
                            rolesForm.setRolesSearchResults(rolesSearchResults);
                        } else {
                            rolesForm.setErrorMessage(RecapConstants.WRONG_PERMISSION);
                        }
                    } else {
                        rolesForm.setErrorMessage(RecapConstants.INVALID_ROLE_NAME);
                    }
                } else {
                    rolesForm.setErrorMessage(RecapConstants.INVALID_ROLE_NAME);
                }
            rolesForm.setPageSize(10);
            } else if (StringUtils.isEmpty(rolesForm.getRoleName()) && StringUtils.isEmpty(rolesForm.getPermissionNames())) {
                Pageable pageable = new PageRequest(rolesForm.getPageNumber(), rolesForm.getPageSize());
                Page<RoleEntity> rolesEntityListByPagination = rolesDetailsRepositorty.getRolesWithoutSuperAdmin(pageable);
                List<RoleEntity> rolesEntityList = rolesEntityListByPagination.getContent();
                rolesForm.setTotalRecordCount(NumberFormat.getNumberInstance().format(rolesEntityListByPagination.getTotalElements()));
                rolesForm.setTotalPageCount(rolesEntityListByPagination.getTotalPages());
                for (RoleEntity roleEntity : rolesEntityList) {
                    RolesSearchResult rolesSearchResult = getRolesSearchResult(roleEntity);
                    rolesSearchResults.add(rolesSearchResult);
                }
                rolesForm.setRolesSearchResults(rolesSearchResults);
            }

        }

    private void getResultsForNonEmptyRolePermissionName(RolesForm rolesForm, List<RolesSearchResult> rolesSearchResults, Page<RoleEntity> roleEntity, Pageable pageable, PermissionEntity pemissionEntity) {
        if(pemissionEntity != null){
            List<Integer> roleIdList = rolesDetailsRepositorty.getRoleIDforPermissionName(pemissionEntity.getPermissionId());
            if(roleIdList != null){
                    roleEntity = rolesDetailsRepositorty.findByRoleIDs(pageable, roleIdList);
                    List<RoleEntity> roleEntityList = roleEntity.getContent();
                for (RoleEntity entity : roleEntityList) {
                    RolesSearchResult rolesSearchResult = getRolesSearchResult(entity);
                    rolesSearchResults.add(rolesSearchResult);
                }
            }
                rolesForm.setRolesSearchResults(rolesSearchResults);
                rolesForm.setTotalRecordCount(String.valueOf(roleEntity.getTotalElements()));
                rolesForm.setTotalPageCount(roleEntity.getTotalPages());
            }
            else{
                rolesForm.setErrorMessage(RecapConstants.INVALID_PERMISSION);
            }
    }

    private List<String> getSeletedPermissionNames(String permissionName) {
        List<String> permissionNames = new ArrayList<>();
        if(!StringUtils.isEmpty(permissionName)){
            StringTokenizer stringTokenizer = new StringTokenizer(permissionName, ",");
            while(stringTokenizer.hasMoreTokens()){
                String token = stringTokenizer.nextToken();
                String trim = token.trim();
                if(!StringUtils.isEmpty(trim)){
                    permissionNames.add(trim);
                }
            }
        }
        return permissionNames;
    }


    public RolesSearchResult getRolesSearchResult(RoleEntity roleEntity){
        StringBuilder permission = new StringBuilder();
        RolesSearchResult rolesSearchResult = new RolesSearchResult();
        for(PermissionEntity permissionEntity : roleEntity.getPermissions()){
            permission.append(permissionEntity.getPermissionName());
            permission.append(", ");
        }
        String permissionName = getSeletedPermissionNames(permission.toString()).toString().replaceAll("\\[", "").replaceAll("\\]", "");
        rolesSearchResult.setPermissionName(permissionName);
        rolesSearchResult.setRolesName(roleEntity.getRoleName());
        rolesSearchResult.setRolesDescription(roleEntity.getRoleDescription());
        rolesSearchResult.setRoleId(roleEntity.getRoleId());
        return rolesSearchResult;
    }


    private boolean isSpecialCharacterCheck(String inputString) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9_ ]*");
        Matcher matcher = pattern.matcher(inputString);
        if(!matcher.matches()){
            return false;
        }else{
            return true;
        }
    }

    public void findByPagination(RolesForm rolesForm){
        List<RolesSearchResult> rolesSearchResults = new ArrayList<>();
        Pageable pageable = new PageRequest(rolesForm.getPageNumber(), rolesForm.getPageSize());
        List<RoleEntity> rolesEntityList;
        if(!StringUtils.isEmpty(rolesForm.getRoleName()) && StringUtils.isEmpty(rolesForm.getPermissionNames())){
            Page<RoleEntity> rolesEntityListByPagination = rolesDetailsRepositorty.findByRoleName(pageable, rolesForm.getRoleName());
            rolesEntityList = rolesEntityListByPagination.getContent();
            rolesForm.setTotalRecordCount(NumberFormat.getNumberInstance().format(rolesEntityListByPagination.getTotalElements()));
            rolesForm.setTotalPageCount(rolesEntityListByPagination.getTotalPages());
            for(RoleEntity roleEntity : rolesEntityList){
                RolesSearchResult rolesSearchResult = getRolesSearchResult(roleEntity);
                rolesSearchResults.add(rolesSearchResult);
            }
            rolesForm.setRolesSearchResults(rolesSearchResults);
        }
        else if(StringUtils.isEmpty(rolesForm.getRoleName()) && !StringUtils.isEmpty(rolesForm.getPermissionNames())){
            Page<RoleEntity> roleEntity=null;
            Pageable pageable1 = new PageRequest(rolesForm.getPageNumber(), rolesForm.getPageSize());
            PermissionEntity pemissionEntity = permissionsRepository.findByPermissionName(rolesForm.getPermissionNames());
            getResultsForNonEmptyRolePermissionName(rolesForm, rolesSearchResults, roleEntity, pageable1, pemissionEntity);

        }

        else if(StringUtils.isEmpty(rolesForm.getRoleName()) && StringUtils.isEmpty(rolesForm.getPermissionNames())) {
            Page<RoleEntity> rolesEntityListByPagination = rolesDetailsRepositorty.getRolesWithoutSuperAdmin(pageable);
            rolesEntityList = rolesEntityListByPagination.getContent();
            rolesForm.setTotalRecordCount(NumberFormat.getNumberInstance().format(rolesEntityListByPagination.getTotalElements()));
            rolesForm.setTotalPageCount(rolesEntityListByPagination.getTotalPages());
            for(RoleEntity roleEntity : rolesEntityList){
                RolesSearchResult rolesSearchResult = getRolesSearchResult(roleEntity);
                rolesSearchResults.add(rolesSearchResult);
            }
            rolesForm.setRolesSearchResults(rolesSearchResults);
        }

    }

    public RoleEntity saveNewRoleToDB(RolesForm rolesForm){
        RoleEntity roleEntity = new RoleEntity();
        HttpSession session = request.getSession();
        roleEntity.setRoleName(rolesForm.getNewRoleName().trim());
        roleEntity.setRoleDescription(rolesForm.getNewRoleDescription());
        roleEntity.setCreatedDate(new Date());
        roleEntity.setCreatedBy(String.valueOf(session.getAttribute(UserManagement.USER_NAME)));
        roleEntity.setLastUpdatedDate(new Date());
        roleEntity.setLastUpdatedBy(String.valueOf(session.getAttribute(UserManagement.USER_NAME)));
        List<String> permissionNameList = splitStringAndGetList(rolesForm.getNewPermissionNames());
        return saveRoleEntity(roleEntity, permissionNameList);
    }

    private RoleEntity saveRoleEntity(RoleEntity roleEntity1, List<String> permissionNameList) {
        PermissionEntity permissionEntity;
        RoleEntity roleEntity = null;
        Set rolesSet = new HashSet();
        try {
            for(String permissionName : permissionNameList){
                permissionEntity = permissionsRepository.findByPermissionName(permissionName);
                rolesSet.add(permissionEntity);
            }
            roleEntity1.setPermissions(rolesSet);
            roleEntity = rolesDetailsRepositorty.save(roleEntity1);
        } catch (Exception e) {
            logger.error(RecapConstants.LOG_ERROR,e);
        }
        return roleEntity;
    }

    private List<String> splitStringAndGetList(String inputString) {
        String[] splittedString = inputString.split(",");
        return Arrays.asList(splittedString);
    }

    private RolesForm getAllPermissionNames(){
        RolesForm rolesForm = new RolesForm();
        List<String> permissionNameList = new ArrayList<>();
        List<PermissionEntity> permissionEntityList = permissionsRepository.findAll();
        for(PermissionEntity permissionEntity : permissionEntityList){
            String permissionName = permissionEntity.getPermissionName();
            permissionNameList.add(permissionName);
        }
        rolesForm.setPermissionNameList(permissionNameList);
        return rolesForm;
    }


}
