messageResource.init({ filePath: '../' });
var currentLocale = localStorage.getItem('locales');
messageResource.load('message', function() {}, currentLocale);

document.addEventListener('DOMContentLoaded', function() {
	$('#submit').click(function() {
		var errors = 0;

		errors += validateOnEmptiness('firstName', 'firstNameError');
		errors += validateOnEmptiness('lastName', 'lastNameError');
		errors += validateOnEmptiness('email', 'emailError');
		
		var passwordError = validateOnEmptiness('password', 'passwordError');
		errors += passwordError;
		
		if (passwordError == 0) {
			var passwordLengthError = validateLengthOnLower('password', 'passwordLengthError', 6);
			errors += passwordLengthError;
		}
		
		var confirmPasswordError = validateOnEmptiness('confirmPassword', 'passwordError');
		errors += confirmPasswordError;
		
		if (confirmPasswordError == 0) {
			var confirmPasswordLengthError = validateLengthOnLower('confirmPassword', 'confirmPasswordError', 6);
			errors += confirmPasswordLengthError;
		}
		
		if (passwordLengthError == 0 && confirmPasswordLengthError == 0) {
			var passwordsMatchError = validateOnMatch('password', 'confirmPassword', 'confirmPasswordError2');
			errors += passwordsMatchError;
		}
		
		if ($('#customFile')[0].files.length != 0) {
			errors += validateOnImageType('photo', 'photoError');
		}
		
		if (errors != 0) {
			return false;
		} else {
			$('form').submit();
		}
	});
});