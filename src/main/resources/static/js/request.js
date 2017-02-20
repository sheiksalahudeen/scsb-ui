/**
 * Created by rajeshbabuk on 25/10/16.
 */

jQuery(document).ready(function ($) {
    /***Request Tab Create Request Form Show/Hide ***/
    $("#request .search-request a").click(function () {
        loadSearchRequest();
    });
    $("#request .backtext a").click(function () {
        loadCreateRequest();
    });

    $('#goBackLink').click(function(){
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
    });

    $("a[href='https://htcrecap.atlassian.net/wiki/display/RTG/Search']").attr('href',
        'https://htcrecap.atlassian.net/wiki/display/RTG/Request');

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
});

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
            $("#request .create-request-section").show();
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
            $("#request .create-request-section").hide();
        }
    });
}

function searchRequests(action) {
    var $form = $('#request-form');
    var url = $form.attr('action') + "?action=" + action;
    $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $('#requestContentId').html(response);
            $("#request .request-main-section").show();
            $("#request .create-request-section").hide();
        }
    });
}

function clearRequests() {
    $("#patronBarcode").val('');
    $("#itemBarcode").val('');
    $("#requestStatus").val('');
    $(".search-results-container").css('display', 'none');
}

function requestsFirstPage() {
    searchRequests('first');
}

function requestsLastPage() {
    searchRequests('last');
}

function requestsPreviousPage() {
    $('#pageNumber').val(parseInt($('#pageNumber').val()) - 1);
    searchRequests('previous');
}

function requestsNextPage() {
    $('#pageNumber').val(parseInt($('#pageNumber').val()) + 1);
    searchRequests('next');
}

function requestsOnPageSizeChange() {
    searchRequests('requestPageSizeChange');
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
                $('#itemBarcodeErrorMessage').hide();
                if (errorMessage != null && errorMessage != '') {
                    $('#itemBarcodeNotFoundErrorMessage').html(errorMessage);
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
        if ($(this).find(':selected').val() === 'EDD' || $(this).find(':selected').val() === 'BORROW DIRECT') {
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
        }
        $('#requestTypeErrorMessage').hide();
    }
    if (isBlankValue(deliveryLocation)) {
        if (!(requestType == 'EDD' || requestType == 'BORROW DIRECT')) {
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
                var jsonResponse = JSON.parse(response);
                var errorMessage = jsonResponse['errorMessage'];
                $('#errorMessageSpanId').hide();
                if (errorMessage != null && errorMessage != '') {
                    $('#errorMessageSpanId').html(errorMessage);
                    $('#errorMessageId').show();
                    $('#errorMessageSpanId').show();
                } else {
                    $('#errorMessageSpanId').html('');
                    $('#errorMessageId').hide();
                    $('#itemTitleHead').html(jsonResponse['itemTitle']);
                    $('#itemBarcodeInRequest').html(jsonResponse['ItemBarcode']);
                    $('#itemTitle').html(jsonResponse['itemTitle']);
                    $('#itemOwningInstitution').html(jsonResponse['ItemOwningInstitution']);
                    $('#patronBarcodeInRequest').html(jsonResponse['patronBarcode']);
                    $('#patronEmailAddress').html(jsonResponse['patronEmailAddress']);
                    $('#requestingInstitution').html(jsonResponse['requestingInstitution']);
                    $('#requestType').html(jsonResponse['requestType']);
                    $('#deliveryLocationInRequest').html(jsonResponse['deliveryLocation']);
                    $('#requestNotes').html(jsonResponse['requestNotes']);
                    $('#patronBarcodeInRequestHdn').html(jsonResponse['patronBarcode']);
                    $('#patronEmailIdHdn').html(jsonResponse['patronEmailAddress']);
                    $('#requestingInstitutionHdn').html(jsonResponse['requestingInstitution']);
                    $('#createrequestclear').click();
                    $('#createRequestModal').modal('show');
                }
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
    //EDD
    $('#EDD').hide();
    $('#StartPage').val('');
    $('#EndPage').val('');
    $('#VolumeNumber').val('');
    $('#Issue').val('');
    $('#ArticleAuthor').val('');
    $('#ArticleChapterTitle').val('');

}

function toggleItemBarcodeValidation() {
    var itemBarcode = $('#itemBarcodeId').val();
    if (isBlankValue(itemBarcode)) {
        $('#itemBarcodeErrorMessage').show();
        $('#itemBarcodeNotFoundErrorMessage').hide();
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
    $('#patronBarcodeId').val($('#patronBarcodeInRequest').html());
    $('#patronEmailId').val($('#patronEmailAddress').html());
    $('#requestingInstitutionId').val($('#requestingInstitution').html());
    $('.EDDdetails-section').hide();
    $('#deliverylocation_request').show();
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