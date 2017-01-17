
jQuery(document).ready(function ($) {
    $('#createRequest').click(function(){
       $('#networkLoginId').val('');
       $('#institutionId').val(1);
        $('#roleId').multiselect('deselectAll', false);
        $('#roleId').multiselect('refresh');
        $('#userDescription').val('');
        $('#successMsg').hide();
        $('#errormsg').hide();
    });


    $('#editusers-createview').hide();
    $('#users-createview').hide();

    $('#backLink').click(function(){
        $('#editusers-createview').hide();
       $('#request-result-table').hide();
        $('#userRolePaginationDivId').hide();
        $('#showEntries').hide();
        $('#numOfRecordsId').val(10);
        $('#pageNumber').val(0);
    });

    $('#editbackLink').click(function(){
        $('#editusers-createview').hide();
        $('#request-result-table').hide();
        $('#userRolePaginationDivId').hide();
        $('#showEntries').hide();
        $('#numOfRecordsId').val(10);
        $('#pageNumber').val(0);
    });

    /***Role Tab Form Show/Hide ***/
    $("#recaprole a.role-detailtable").click(function (e) {
        $("#role-searchview").hide();
        $("#role-detailtableview").show();
    });
    $("#recaprole .rolebacklink a").click(function (e) {
        $("#role-detailtableview").hide();
        $("#role-searchview").show();
    });
    /***Users Tab Form Show/Hide ***/
    $("#recapusers .userscreaterequestlink a").click(function (e) {
        $("#users-searchview").hide();
        $("#users-createview").show();
    });
    $("#recapusers .usersbacklink a").click(function (e) {
        $("#users-createview").hide();
        $("#users-searchview").show();
    });

    $("#userRoles .users-main-section").show();
    resetDefaults();
    /***Request Tab Create Request Form Show/Hide ***/
    $("#userRoles .usersbacklink a").click(function (e) {
        $("#userRoles .users-main-section").show();
        $("#userRoles .user-create-section").hide();
    });
    $("#userRoles .user-create a").click(function () {
        $("#userRoles .users-main-section").hide();
        $("#userRoles .user-create-section").show();
    });


    /**Multi Select **/
    $('#roleId').multiselect();
    $('#recapusers #userstabaddrole').multiselect();
    $('#editroleId').multiselect();

});
function userRolesFirstPage() {
    //searchUserRoles('first','#userRoles .users-main-section','#userRoles .user-create-section');
    var $form = $('#userRole-form');
    var url = "/userRoles/first";
    var request = $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('.showEntriesOption').show();
            $('#request-result-table').html(response);
            $('#request-result-table').show();
            $('#userRolePaginationDivId').show();

        }
    });
}

function userRolesLastPage() {
    //searchUserRoles('last','#userRoles .users-main-section','#userRoles .user-create-section');
    $('#pageNumber').val(parseInt($('#totalPageCount').val()) - 1);
    var $form = $('#userRole-form');
    var url = "/userRoles/last";
    var request = $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('#request-result-table').html(response);
            $('.showEntriesOption').show();
            $('#request-result-table').show();
            $('#userRolePaginationDivId').show();

        }
    });
}

function userRolesPreviousPage() {
    $('#pageNumber').val(parseInt($('#pageNumber').val()) - 1);
    //searchUserRoles('previous','#userRoles .users-main-section','#userRoles .user-create-section');
    var $form = $('#userRole-form');
    var url = "/userRoles/previous";
    var request = $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('#request-result-table').html(response);
            $('.showEntriesOption').show();
            $('#request-result-table').show();
            $('#userRolePaginationDivId').show();

        }
    });
}

function userRolesNextPage() {
    $('#pageNumber').val(parseInt($('#pageNumber').val()) + 1);
    //searchUserRoles('next','#userRoles .users-main-section','#userRoles .user-create-section');
    var $form = $('#userRole-form');
    var url = "/userRoles/next";
    var request = $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('#request-result-table').html(response);
            $('#showEntries').show();
            $('#request-result-table').show();
            $('#userRolePaginationDivId').show();

        }
    });
}

function deleteUserRole(networkLoginId,userId) {
    console.log('DeleteUserRole');
    console.log('NetWorkLoginId : ' + networkLoginId);
    console.log('UserId  : ' + userId);
    var $form = $('#userRole-form');
    var url = "/userRoles/deleteUser";
    var role = $.ajax({
        url: url,
        type: 'post',
        data: {networkLoginId:networkLoginId,userId:userId},
        success: function (response) {
            console.log("completed");
            $('#popUpBox').html(response);

        }
    });
}

//Delete User Id
function deleteUser(userId){
    console.log('Delete');
    console.log('UserId  : ' + userId);
    var $form = $('#userRole-form');
    var url = "/userRoles/delete";
    var role = $.ajax({
        url: url,
        type: 'post',
        data: {userId:userId},
        success: function (response) {
            console.log("completed");
            $('#request-result-table').html(response);

        }
    });
}

function searchUserRoles() {
    var $form = $('#userRole-form');
    var url = "/userRoles/searchUsers";
    var request = $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('#request-result-table').html(response);
            $('#showEntries').show();
            $('#request-result-table').show();
            $('#userRolePaginationDivId').show();
        }
    });
}



function showEntriesChange(){
    $('#pageNumber').val(0);
    var $form = $('#userRole-form');
    var url = "/userRoles/searchUsers";
    var request = $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('#request-result-table').html(response);
            $('#request-result-table').show();
            $('#userRolePaginationDivId').show();

        }
    });
}

function submitForm(){
    var formValidation = validateForm();
    console.log(formValidation);
    if(formValidation) {
        var $form = $('#userRole-form');
        var url = "/userRoles/createUser";
        var request = $.ajax({
            url: url,
            type: 'post',
            data: $form.serialize(),
            success: function (response) {
                $('#userRolesContentId').html(response);
                $('#users-searchview').hide();
                $('#users-createview').show();
                $('#successMsg').show();
                $('#roleId').multiselect();
                $('#networkLoginIdErrMsg').hide();
                $('#institutionIdErrMsg').hide();
                $('#roleIdErrMsg').hide();
                $('#userDescriptionErrMsg').hide();
            }
        });
    }

}
function resetDefaults(){
    $('#networkLoginIdErrMsg').hide();
    $('#institutionIdErrMsg').hide();
    $('#roleIdErrMsg').hide();
}

function validateForm(){
    var isValid = true;
    var newNetworkLoginId=$('#networkLoginId').val();
    var institutionId=$('#institutionId').val();
    var roleId=$('#roleId').val();
    var userDescription=$('#userDescription').val();
    if(isBlankValue(newNetworkLoginId)) {
        $('#networkLoginIdErrMsg').show();
        isValid=false;
    }else {
        $('#networkLoginIdErrMsg').hide();
    }
    if(isBlankValue(institutionId)) {
        $('#institutionIdErrMsg').show();
        isValid= false;
    }else{
        $('#institutionIdErrMsg').hide();
    }
    if(isBlankValue(roleId)) {
        $('#roleIdErrMsg').show();
        isValid= false;
    }else{
        $('#roleIdErrMsg').hide();
    }
    if(isBlankValue(userDescription)){
        $('#userDescriptionErrMsg').show();
        isValid= false;
    }else {
        $('#userDescriptionErrMsg').hide();
    }
    return isValid;
}


function isBlankValue(value) {
    if (value == null || value == '') {
        return true;
    }
    return false;
}

function editUser(userId,networkLoginId){
    console.log("userId"+userId);
    console.log("log id"+networkLoginId);
    $('#editsuccessMsg').hide();
    $('#editerrormsg').hide();
    $('#editusers-createview').show();
    $('#users-createview').hide();
    $('#users-searchview').hide();
    var url = "/userRoles/editUser";
    var request = $.ajax({
        url: url,
        type: 'post',
        data: {userId:userId,networkLoginId:networkLoginId},
        success: function (response) {
            $('#userRolesContentId').html(response);
            $('#editusers-createview').show();
            $('#users-searchview').hide();
            $('#users-createview').hide();
            $('#successMsg').show();
            $('#editroleId').multiselect();
        }
    });
}

function editclear(){
    editUser(userId,networkLoginId);
}


function editUserDetails(){
    var userId = $('#editUserIdHidden').val();
    var networkLoginId = $('#editnetworkLoginId').val();
    var institutionId = $('#editinstitutionId').val();
    var roleIds = $('#editroleId').val();
    var userDescription = $('#edituserDescription').val();
    $('#editusers-createview').show();
    $('#users-createview').hide();
    $('#users-searchview').hide();
    var $form = $('#userRole-form');
    var url = "/userRoles/saveEditUserDetails";
    var request = $.ajax({
        url: url,
        type: 'post',
        data: {userId:userId,networkLoginId:networkLoginId,roleIds:roleIds,userDescription:userDescription,institutionId:institutionId},
        success: function (response) {
            $('#userRolesContentId').html(response);
            $('#editusers-createview').show();
            $('#users-searchview').hide();
            $('#users-createview').hide();
            $('#successMsg').show();
            $('#editroleId').multiselect();
        }
    });
}






