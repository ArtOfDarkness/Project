messageResource.init({ filePath: '../' });
var currentLocale = localStorage.getItem('locales');
messageResource.load('message', function() {}, currentLocale);

document.addEventListener('DOMContentLoaded', function() {
	$('#submit').click(function() {
		var errors = 0;

		errors += validateOnEmptiness('firstName', 'firstNameError');
		errors += validateOnEmptiness('lastName', 'lastNameError');
		
		if (errors != 0) {
			return false;
		} else {
			$('form').submit();
		}
	});
});