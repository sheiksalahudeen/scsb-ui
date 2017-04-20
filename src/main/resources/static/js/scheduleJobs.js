/**
 * Created by rajeshbabuk on 4/4/17.
 */

function showScheduleJob(index) {
    showScheduleJobView(index, 'Schedule');
    $("#scheduleJobButtonId").show();
    $("#rescheduleJobButtonId").hide();
    $("#unscheduleJobButtonId").hide();
}

function showRescheduleJob(index) {
    showScheduleJobView(index, 'Reschedule');
    $("#scheduleJobButtonId").hide();
    $("#rescheduleJobButtonId").show();
    $("#unscheduleJobButtonId").hide();
}

function showUnscheduleJob(index) {
    showScheduleJobView(index, 'Unschedule');
    $("#cronExpressionId").css('readonly', true);
    $('#cronExpressionId').attr('readonly', true);
    $('#cronExpressionId').prop('readonly', true);
    $("#scheduleJobButtonId").hide();
    $("#rescheduleJobButtonId").hide();
    $("#unscheduleJobButtonId").show();
}

function showScheduleJobView(index, scheduleType) {
    var jobId = $("#scheduleJobsRowJobId-" + index).val();
    var jobName = $("#scheduleJobsRowJobName-" + index).val();
    var jobDescription = $("#scheduleJobsRowJobDescription-" + index).val();
    var cronExpression = $("#scheduleJobsRowCronExpression-" + index).val();

    $("#cronExpressionId").val(cronExpression);
    $("#jobId").val(jobId);
    $("#jobNameId").val(jobName);
    $("#jobDescriptionId").val(jobDescription);
    $("#scheduleType").val(scheduleType);
    $("#scheduleJobDetailsSection").hide();
    $("#scheduleJobSection").show();
}


function invokeScheduleJob(scheduleType) {
    var $form = $('#scheduleJobs-form');
    var url = $form.attr('action') + "?action=scheduleJob";
    $.ajax({
        url: url,
        type: 'post',
        data: $form.serialize(),
        success: function (response) {
            $("#scheduleJobs").html(response);
            $("#scheduleJobSection").show();
            $("#scheduleJobDetailsSection").hide();
            if (scheduleType == 'Schedule') {
                $("#scheduleJobButtonId").show();
                $("#rescheduleJobButtonId").hide();
                $("#unscheduleJobButtonId").hide();
            } else if (scheduleType == 'Reschedule') {
                $("#scheduleJobButtonId").hide();
                $("#rescheduleJobButtonId").show();
                $("#unscheduleJobButtonId").hide();
            } else if (scheduleType == 'Unschedule') {
                $("#scheduleJobButtonId").hide();
                $("#rescheduleJobButtonId").hide();
                $("#unscheduleJobButtonId").show();
            }
        }
    });
}