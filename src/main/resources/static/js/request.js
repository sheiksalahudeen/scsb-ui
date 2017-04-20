/**
 * Created by rajeshbabuk on 25/10/16.
 */

$("a[href='https://htcrecap.atlassian.net/wiki/display/RTG/Search']").attr('href',
    'https://htcrecap.atlassian.net/wiki/display/RTG/Request');

function onChangeRequestStatus() {
    var status = $('#requestStatus').val();
    if (status == 'active'){
        $('#noteActive').show();
        $('#noteAll').hide();
    }
    else if (isBlankValue(status)){
        $('#noteAll').show();
        $('#noteActive').hide();
    }
    else {
        $('#noteAll').hide();
        $('#noteActive').hide();
    }
    $('#patronBarcodeSearchError').hide();
    $('#itemBarcodeSearchError').hide();
}

$(function() {
    $("#searchRequestsSection input").keypress(function (e) {
        if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
            $("#searchRequestsButton").click();
            $("#request .request-main-section").show();
            $("#request .create-request-section").hide();
            return false;
        } else {
            return true;
        }
    });
});

function loadCreateRequest() {
    var $form = $('#request-form');
    var url = $form.attr('action') + "?action=loadCreateRequest";
    $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('#requestContentId').html(response);
            $("#request .request-main-section").hide();
            $("#goBackLink").hide();
            $("#request .create-request-section").show();
        }
    });
}

function loadCreateRequestForSamePatron() {
    var patronBarcode = $("#patronBarcodeId").val();
    var patronEmailId = $("#patronEmailId").val();
    var requestingInstitutionId = $("#requestingInstitutionId").val();
    var $form = $('#request-form');
    var url = $form.attr('action') + "?action=loadCreateRequestForSamePatron";
    $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('#requestContentId').html(response);
            $("#request .request-main-section").hide();
            $("#goBackLink").hide();
            $("#request .create-request-section").show();
            $("#requestingInstitutionId option").prop("disabled", false);
            $('#patronBarcodeId').val(patronBarcode);
            $('#patronEmailId').val(patronEmailId);
            $('#requestingInstitutionId').val(requestingInstitutionId);
            $('.EDDdetails-section').hide();
            $('#deliverylocation_request').show();
            $('#deliveryLocationId').empty();
        }
    });
}

function loadSearchRequest() {
    var $form = $('#request-form');
    var url = $form.attr('action') + "?action=loadSearchRequest";
    $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('#requestContentId').html(response);
            $("#request .request-main-section").show();
            $("#goBackLink").show();
            $("#request .create-request-section").hide();
        }
    });
}

function goToSearchRequest(patronBarcodeInRequest){
    var $form = $('#request-form');
    var url = "/request/goToSearchRequest";
    $.ajax({
        url: url,
        type: 'GET',
        data: {patronBarcodeInRequest: patronBarcodeInRequest},
        success: function (response) {
            $('#requestContentId').html(response);
            $("#goBackLink").show();
            $("#request .request-main-section").show();
            $("#request .create-request-section").hide();
            var status = $('#requestStatus').val();
            if (status == 'active'){
                $('#noteActive').show();
                $('#noteAll').hide();
            }
            else if (isBlankValue(status)){
                $('#noteAll').show();
                $('#noteActive').hide();
            }
            else {
                $('#noteAll').hide();
                $('#noteActive').hide();
            }
        }
    });

}

function searchRequests(action) {
    var isValidSearch = validSearch();
    if (isValidSearch) {
        searchRequestsByAction(action);
    }
    else {
        $(".search-results-container").css('display', 'none');
    }
}

function validSearch() {
    var patronBarcode = $("#patronBarcode").val();
    var itemBarcode = $("#itemBarcode").val();
    var requestStatus = $("#requestStatus").val();
    var isValidSearch = true;
    if(isBlankValue(requestStatus)){
        if (isBlankValue(patronBarcode) && isBlankValue(itemBarcode)){
            isValidSearch = false;
            $('#patronBarcodeSearchError').show();
            $('#itemBarcodeSearchError').show();
        }
    }
    return isValidSearch;
}

function clearRequests() {
    $("#patronBarcode").val('');
    $("#itemBarcode").val('');
    $("#requestStatus").val('');
    $(".search-results-container").css('display', 'none');
    $('#patronBarcodeSearchError').hide();
    $('#itemBarcodeSearchError').hide();
    $('#noteAll').show();
    $('#noteActive').hide();
    $('#notesLengthErrMsg').hide();
}


function searchRequestsByAction(action) {
    var $form = $('#request-form');
    var url = $form.attr('action') + "?action=" + action;
    $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('#searchRequestsSection').html(response);
            $("#request .request-main-section").show();
            $("#request .create-request-section").hide();
            var status = $('#requestStatus').val();
            if (status == 'active'){
                $('#noteActive').show();
                $('#noteAll').hide();
            }
            else if (isBlankValue(status)){
                $('#noteAll').show();
                $('#noteActive').hide();
            }
            else {
                $('#noteAll').hide();
                $('#noteActive').hide();
            }
        }
    });
}

function requestsFirstPage() {
    searchRequestsByAction('first');
}

function requestsLastPage() {
    searchRequestsByAction('last');
}

function requestsPreviousPage() {
    $('#pageNumber').val(parseInt($('#pageNumber').val()) - 1);
    searchRequestsByAction('previous');
}

function requestsNextPage() {
    $('#pageNumber').val(parseInt($('#pageNumber').val()) + 1);
    searchRequestsByAction('next');
}

function requestsOnPageSizeChange() {
    searchRequestsByAction('requestPageSizeChange');
}

function populateItemDetails() {
    var itemBarcode = $('#itemBarcodeId').val();
    if (!isBlankValue(itemBarcode)) {
        var $form = $('#request-form');
        var url = $form.attr('action') + "?action=populateItem";
        $.ajax({
            url: url,
            type: 'post',
            data: $form.serialize(),
            success: function (response) {
                var jsonResponse = JSON.parse(response);
                $('#itemTitleId').val(jsonResponse['itemTitle']);
                $('#itemOwningInstitutionId').val(jsonResponse['itemOwningInstitution']);
                var errorMessage = jsonResponse['errorMessage'];
                var notAvailableErrorMessage = jsonResponse['notAvailableErrorMessage'];
                var noPermissionErrorMessage = jsonResponse['noPermissionErrorMessage'];
                var deliveryLocation = jsonResponse['deliveryLocation'];
                if (deliveryLocation != null && deliveryLocation != '') {
                    $('#deliveryLocationId').empty();
                    $('#deliveryLocationId').append($("<option/>", {
                        value: "",
                        text: ""
                    }));
                    $.each(deliveryLocation, function (key, value) {
                        $('#deliveryLocationId').append($("<option/>", {
                            value: key,
                            text: value
                        }));
                    });
                }
                $('#itemBarcodeErrorMessage').hide();
                if (errorMessage != null && errorMessage != '' && notAvailableErrorMessage != null && notAvailableErrorMessage != '') {
                    $('#itemBarcodeNotFoundErrorMessage').html(errorMessage + "<br>" + notAvailableErrorMessage);
                    $('#itemBarcodeNotFoundErrorMessage').show();
                } else if ((errorMessage != null && errorMessage != '')) {
                    $('#itemBarcodeNotFoundErrorMessage').html(errorMessage);
                    $('#itemBarcodeNotFoundErrorMessage').show();
                } else if ((notAvailableErrorMessage != null && notAvailableErrorMessage != '')) {
                    $('#itemBarcodeNotFoundErrorMessage').html(notAvailableErrorMessage);
                    $('#itemBarcodeNotFoundErrorMessage').show();
                } else if (noPermissionErrorMessage != null && noPermissionErrorMessage != '') {
                    $('#itemBarcodeNotFoundErrorMessage').html(noPermissionErrorMessage);
                    $('#itemBarcodeNotFoundErrorMessage').show();
                } else {
                    $('#itemBarcodeNotFoundErrorMessage').html('');
                }
            }
        });
    }
}

/***Request Tab Create Request Form Selecrt EDD Section Show/Hide ***/
$(function() {
    $('#requestTypeId').change(function(){
        $('.EDDdetails-section').hide();
        $('#' + $(this).val()).show();
        if ($(this).find(':selected').val() === 'EDD') {
            $('#deliverylocation_request').hide();
        } else {
            $('#deliverylocation_request').show();
        }
    });
});

function isValidInputs() {
    var isValid = true;

    var itemBarcode = $('#itemBarcodeId').val();
    var patronBarcode = $('#patronBarcodeId').val();
    var requestType = $('#requestTypeId').val();
    var deliveryLocation = $('#deliveryLocationId').val();
    var requestingInstitution = $('#requestingInstitutionId').val();
    var notesLength = $('#requestNotesId').val().length;
    if (notesLength  > 2000){
        $('#notesLengthErrMsg').show();
        isValid = false;
    }else {
        $('#notesLengthErrMsg').hide();
    }
    validateEmailAddress();

    if (isBlankValue(itemBarcode)) {
        $('#itemBarcodeErrorMessage').show();
        isValid = false;
    } else {
        $('#itemBarcodeErrorMessage').hide();
    }
    if (isBlankValue(patronBarcode)) {
        $('#patronBarcodeErrorMessage').show();
        isValid = false;
    } else {
        $('#patronBarcodeErrorMessage').hide();
    }
    if (isBlankValue(requestType)) {
        $('#requestTypeErrorMessage').show();
        isValid = false;
    } else {
        if (requestType == 'EDD') {
            var startPage = $('#StartPage').val();
            var endPage = $('#EndPage').val();
            var articleTitle = $('#ArticleChapterTitle').val();
            var patronEmailId = $('#patronEmailId').val();

            if (isBlankValue(startPage)) {
                $('#startPageErrorMessage').show();
                isValid = false;
            } else {
                $('#startPageErrorMessage').hide();
            }
            if (isBlankValue(endPage)) {
                $('#endPageErrorMessage').show();
                isValid = false;
            } else {
                $('#endPageErrorMessage').hide();
            }
            if (isBlankValue(articleTitle)) {
                $('#articleTitleErrorMessage').show();
                isValid = false;
            } else {
                $('#articleTitleErrorMessage').hide();
            }
            if(isBlankValue(patronEmailId)){
                $('#EmailMandatoryErrorMessage').show();
                isValid = false;
            }
            else {
                $('#EmailMandatoryErrorMessage').hide();
            }
        }
        $('#requestTypeErrorMessage').hide();
    }
    if (isBlankValue(deliveryLocation)) {
        if (!(requestType == 'EDD')) {
            $('#deliveryLocationErrorMessage').show();
            isValid = false;
        }
    } else {
        $('#deliveryLocationErrorMessage').hide();
    }
    if (isBlankValue(requestingInstitution)) {
        $('#requestingInstitutionErrorMessage').show();
        isValid = false;
    } else {
        $('#requestingInstitutionErrorMessage').hide();
    }
    return isValid;
}

function createRequest() {
    if (isValidInputs()) {
        var $form = $('#request-form');
        var url = $form.attr('action') + "?action=createRequest";
        $.ajax({
            url: url,
            type: 'post',
            data: $form.serialize(),
            beforeSend: function () {
                $('#createRequestSection').block({
                    message: '<h1>Processing...</h1>'
                });
            },
            success: function (response) {
                $('#createRequestSection').unblock();
                $('#createRequestSection').html(response);
                $("#textField").hide();
            }
        });
    }
}

function isBlankValue(value) {
    if (value == null || value == '') {
        return true;
    }
    return false;
}

function resetDefaults() {
    $('#errorMessageId').hide();
    $('#itemBarcodeErrorMessage').hide();
    $('#patronBarcodeErrorMessage').hide();
    $('#requestTypeErrorMessage').hide();
    $('#deliveryLocationErrorMessage').hide();
    $('#requestingInstitutionErrorMessage').hide();
    $('#startPageErrorMessage').hide();
    $('#endPageErrorMessage').hide();
    $('#articleTitleErrorMessage').hide();
    $('#patronEmailIdErrorMessage').hide();
    $('#itemBarcodeNotFoundErrorMessage').hide();
    $('#itemBarcodeId').val('');
    $('#itemTitleId').val('');
    $('#itemOwningInstitutionId').val('');
    $('#patronBarcodeId').val('');
    $('#patronEmailId').val('');
    $('#requestTypeId').val('RETRIEVAL');
    $('#deliveryLocationId').val('');
    $('#requestingInstitutionId').val('');
    $('#requestNotesId').val('');
    $('#deliverylocation_request').show();
    $('#deliveryLocationId').empty();
    //EDD
    $('#EDD').hide();
    $('#StartPage').val('');
    $('#EndPage').val('');
    $('#VolumeNumber').val('');
    $('#Issue').val('');
    $('#ArticleAuthor').val('');
    $('#ArticleChapterTitle').val('');
    $('#EmailMandatoryErrorMessage').hide();
    $('#emailMandatory').hide();
    $('#notesLengthErrMsg').hide();


}

function toggleItemBarcodeValidation() {
    var itemBarcode = $('#itemBarcodeId').val();
    if (isBlankValue(itemBarcode)) {
        $('#itemBarcodeErrorMessage').show();
        $('#itemBarcodeNotFoundErrorMessage').hide();
        $('#itemTitleId').val('');
        $('#itemOwningInstitutionId').val('');
        $('#patronBarcodeId').val('');
        $('#patronEmailId').val('');
        $('#deliveryLocationId').val('');
        $('#deliveryLocationId').empty();
    } else {
        $('#itemBarcodeErrorMessage').hide();
    }
}

function toggleRequestingInstitutionValidation() {
    var requestingInstitution = $('#requestingInstitutionId').val();
    if (isBlankValue(requestingInstitution)) {
        $('#requestingInstitutionErrorMessage').show();
    } else {
        $('#requestingInstitutionErrorMessage').hide();
    }
}

function togglePatronBarcodeValidation() {
    var patronBarcode = $('#patronBarcodeId').val();
    if (isBlankValue(patronBarcode)) {
        $('#patronBarcodeErrorMessage').show();
    } else {
        $('#patronBarcodeErrorMessage').hide();
    }
}

function toggleDeliveryLocationValidation() {
    var deliveryLocation = $('#deliveryLocationId').val();
    if (isBlankValue(deliveryLocation)) {
        $('#deliveryLocationErrorMessage').show();
    } else {
        $('#deliveryLocationErrorMessage').hide();
    }
}

function toggleStartPageValidation() {
    var startPage = $('#StartPage').val();
    if (isBlankValue(startPage)) {
        $('#startPageErrorMessage').show();
    } else {
        $('#startPageErrorMessage').hide();
    }
}

function toggleEndPageValidation() {
    var endPage = $('#EndPage').val();
    if (isBlankValue(endPage)) {
        $('#endPageErrorMessage').show();
    } else {
        $('#endPageErrorMessage').hide();
    }
}

function toggleArticleTitleValidation() {
    var articleTitle = $('#ArticleChapterTitle').val();
    if (isBlankValue(articleTitle)) {
        $('#articleTitleErrorMessage').show();
    } else {
        $('#articleTitleErrorMessage').hide();
    }
}

function validateEmailAddress() {
    var isValidEmailAddress = $('#patronEmailId').is(':valid');
    if (!isValidEmailAddress) {
        $('#patronEmailIdErrorMessage').show();
    } else {
        $('#patronEmailIdErrorMessage').hide();
    }
}

function createRequestSamePatron() {
    $('#createRequestModal').modal('hide');
    $("#requestingInstitutionId option").prop("disabled", false);
    $('#patronBarcodeId').val($('#patronBarcodeInRequest').html());
    $('#patronEmailId').val($('#patronEmailAddress').html());
    $('#requestingInstitutionId').val($('#requestingInstitution').html());
    $('.EDDdetails-section').hide();
    $('#deliverylocation_request').show();
    $('#deliveryLocationId').empty();
}

function cancelRequest(index) {
    var requestId = $("#requestRowRequestId-" + index).val();
    $("#requestIdHdn").val(requestId);
    cancelRequestItem(index);
}

function cancelRequestItem(index) {
    var $form = $('#request-form');
    var url = $form.attr('action') + "?action=cancelRequest";
    $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        beforeSend: function () {
            $('#searchRequestsSection').block({
                message: '<h1>Processing...</h1>'
            });
        },
        success: function (response) {
            $('#searchRequestsSection').unblock();
            var jsonResponse = JSON.parse(response);
            var message = jsonResponse['Message'];
            var status = jsonResponse['Status'];
            var requestStatus = jsonResponse['RequestStatus'];
            if (status) {
                $("#cancelStatus").html("Request cancelled successfully");
                $("#status-" + index).html(requestStatus);
                $("#cancelButton-" + index).hide();
            } else {
                $("#cancelStatus").html("Request cancellation failed. " + message);
            }
            $('#cancelRequestModal').modal('show');
        }
    });
}

function showNotesPopup(index) {
    var notes = $("#notes-" + index).val();
    $("#requestNotesData").html(notes);
    $('#requestNotesModal').modal('show');
}

function togglePatronBarcodeSearch(){
    var patronBarcode = $('#patronBarcode').val();
    if(isBlankValue(patronBarcode)){
        $('#patronBarcodeSearchError').show();
    }
    else{
        $('#patronBarcodeSearchError').hide();
    }
}

function toggleItemBarcodeSearch(){
    var itemBarcode = $('#itemBarcode').val();
    if(isBlankValue(itemBarcode)){
        $('#itemBarcodeSearchError').show();
    }
    else{
        $('#itemBarcodeSearchError').hide();
    }
}

function toggleEmailAddress(){
    var requestType = $('#requestTypeId').val();
    if(requestType == 'EDD') {
        var patronEmailId = $('#patronEmailId').val();
        if (isBlankValue(patronEmailId)) {
            $('#patronEmailIdErrorMessage').hide();
            $('#EmailMandatoryErrorMessage').show();
        }
        else {
            $('#EmailMandatoryErrorMessage').hide();
        }
    }
}

function emailMandatory(){
    var requestType = $('#requestTypeId').val();
    if(requestType == 'EDD'){
        $('#emailMandatory').show();
    }
    else {
        $('#emailMandatory').hide();
        $('#EmailMandatoryErrorMessage').hide();
    }
}


function populateDeliveryLocations(){
    var requestingInstitutionId = $('#requestingInstitutionId').val();
    if(!isBlankValue(requestingInstitutionId)){
        toggleRequestingInstitutionValidation();
    }
    $('#onChangeOwnInst').val('true');
    var $form = $('#request-form');
    var url = $form.attr('action') + "?action=populateItem";
    $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            var jsonResponse = JSON.parse(response);
            $('#itemTitleId').val(jsonResponse['itemTitle']);
            $('#itemOwningInstitutionId').val(jsonResponse['itemOwningInstitution']);
            var errorMessage = jsonResponse['errorMessage'];
            var noPermissionErrorMessage = jsonResponse['noPermissionErrorMessage'];
            var deliveryLocation = jsonResponse['deliveryLocation'];
            if (deliveryLocation != null && deliveryLocation != '') {
                $('#deliveryLocationId').empty();
                $('#deliveryLocationId').append($("<option/>", {
                    value: "",
                    text: ""
                }));
                $.each(deliveryLocation, function (key, value) {
                    $('#deliveryLocationId').append($("<option/>", {
                        value: key,
                        text: value + "-" +key
                    }));
                });
            }
            $('#itemBarcodeErrorMessage').hide();
            if (errorMessage != null && errorMessage != '') {
                $('#itemBarcodeNotFoundErrorMessage').html(errorMessage);
                $('#itemBarcodeNotFoundErrorMessage').show();
            } else if (noPermissionErrorMessage != null && noPermissionErrorMessage != '') {
                $('#itemBarcodeNotFoundErrorMessage').html(noPermissionErrorMessage);
                $('#itemBarcodeNotFoundErrorMessage').show();
            } else {
                $('#itemBarcodeNotFoundErrorMessage').html('');
            }
        }
    });
}

function NotesLengthValidation(){
    var notesLength = $('#requestNotesId').val().length;
    if (notesLength > 2000){
        $('#notesLengthErrMsg').show();
    }else {
        $('#notesLengthErrMsg').hide();
    }
}