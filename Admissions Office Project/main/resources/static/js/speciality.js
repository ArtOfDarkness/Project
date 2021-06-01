messageResource.init({ filePath: '../' });
var currentLocale = localStorage.getItem('locales');
messageResource.load('message', function() {}, currentLocale);

document.addEventListener('DOMContentLoaded', function() {
	$('#submit').click(function() {
		var errors = 0;

		errors += validateOnEmptiness('title', 'specialityTitleError');
		errors += validateSelectOnEmptiness('faculty', 'facultyError');
		
		var enrollmentPlanError = validateOnEmptiness('enrollmentPlan', 'enrollmentPlanError');
		errors += enrollmentPlanError;
		
		if (enrollmentPlanError == 0) {
			errors += validateOnZero('enrollmentPlan', 'enrollmentPlanZeroError');
		}
		
		if (errors != 0) {
			return false;
		} else {
			$('form').submit();
		}
	});
});