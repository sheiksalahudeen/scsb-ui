
jQuery(document).ready(function ($) {
    var showResult=false;
    var showCreateRequest=false;

    if(showResult==false&&showCreateRequest==false){
        $('.showEntriesOption').hide();
        $('#request-result-table').hide();
        $('#users-createview').hide();
    }
    else if(showResult==true){
        $('.showEntriesOption').hide();
        $('#request-result-table').show();
    }
    else if(showCreateRequest==true){
        $('#users-createview').show();
    }

    $('#roleId').multiselect();



    // $('#showUserResult').hide();

    /***Role Tab Form Show/Hide ***/
    $("#recaprole a.role-detailtable").click(function (e) {
        $("#role-searchview").hide();
        $("#role-detailtableview").show();
        e.preventDefault();
    });
    $("#recaprole .rolebacklink a").click(function (e) {
        $("#role-detailtableview").hide();
        $("#role-searchview").show();
        e.preventDefault();
    });
    /***Users Tab Form Show/Hide ***/
    $("#recapusers .userscreaterequestlink a").click(function (e) {
        $("#users-searchview").hide();
        $("#users-createview").show();
        e.preventDefault();
    });
    $("#recapusers .usersbacklink a").click(function (e) {
        $("#users-createview").hide();
        $("#users-searchview").show();
        e.preventDefault();
    });
    $('#recapusers #userstabaddrole').multiselect();


    //\'' + ${userRoleForm.userId} + '\
    /**/


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
});

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




function userRolesFirstPage() {
    searchUserRoles('first','#userRoles .users-main-section','#userRoles .user-create-section');
}

function userRolesLastPage() {
    searchUserRoles('last','#userRoles .users-main-section','#userRoles .user-create-section');
}

function userRolesPreviousPage() {
    $('#pageNumber').val(parseInt($('#pageNumber').val()) - 1);
    searchUserRoles('previous','#userRoles .users-main-section','#userRoles .user-create-section');
}

function userRolesNextPage() {
    $('#pageNumber').val(parseInt($('#pageNumber').val()) + 1);
    searchUserRoles('next','#userRoles .users-main-section','#userRoles .user-create-section');
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
            $('#userRolesFormDivId').html(response);

        }
    });
}

function searchUserRoles() {
    $('.showEntriesOption').show();
    var $form = $('#userRole-form');
    var url = "/userRoles/searchUsers";
    var request = $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('#request-result-table').html(response);
            $('#request-result-table').show();

        }
    });
}

function submitForm(){
    validateForm();
        var $form = $('#userRole-form');
        var url = "/userRoles/createUser";
        var request = $.ajax({
            url: url,
            type: 'post',
            data: $form.serialize(),
            success: function (response) {
                $('#users-createview').html(response);
                $('#users-createview').show();
                $('#successMsg').show();

                $('#networkLoginIdErrMsg').hide();
                $('#institutionIdErrMsg').hide();
                $('#roleIdErrMsg').hide();
                $('#userDescriptionErrMsg').hide();
            }
        });

}
function resetDefaults(){
    $('#networkLoginIdErrMsg').hide();
    $('#institutionIdErrMsg').hide();
    $('#roleIdErrMsg').hide();
}

function validateForm(){
    /*resetDefaults();*/
    var newNetworkLoginId=$('#networkloginid');
    var institutionId=$('#institutionId');
    var roleId=$('#roleId');
    var userDescription=$('#userDescription')
    if(newNetworkLoginId == null)
    {
        $('#networkLoginIdErrMsg').show();
        return false;
    }

    if(institutionId == null)
    {
        $('#institutionIdErrMsg').show();
        return false;
    }

    if(roleId == null )
    {
        $('#roleIdErrMsg').show();
        return false;
    }
    if(userDescription == null)
    {
        $('#userDescriptionErrMsg').show();
        return false;
    }

}





