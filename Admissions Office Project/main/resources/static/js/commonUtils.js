messageResource.init({ filePath: '../' });
var currentLocale = localStorage.getItem('locales');
messageResource.load('message', function() {}, currentLocale);

function commonValidate(field, errorMessage, inputType, condition) {
	var errors = 0;
	
	if (condition) {
		$(inputType + "[name='" + field + "']").addClass('is-invalid');
		$("div[id='" + field + "Error']").addClass('invalid-feedback');
		$("div[id='" + field + "Error']").html(messageResource.get(errorMessage, 'message', currentLocale));
		errors += 1;
	} else {
		$("div[id='" + field + "Error']").html('');				
		$("div[id='" + field + "Error']").removeClass('invalid-feedback');
		$(inputType + "[name='" + field + "']").removeClass('is-invalid');
	}
	return errors;
}

function validateOnEmptiness(field, errorMessage) {
	var condition = $("input[name='" + field + "']").val() == '';
	return commonValidate(field, errorMessage, 'input', condition);
}

function validateSelectOnEmptiness(field, errorMessage) {
	var condition = $("select[name='" + field + "']").val() == '';
	return commonValidate(field, errorMessage, 'select', condition);
}

function validateOnZero(field, errorMessage) {
	var condition = $("input[name='" + field + "']").val() == 0;
	return commonValidate(field, errorMessage, 'input', condition);
}

function validateOnGreater(field, errorMessage, number) {
	var condition = $("input[name='" + field + "']").val() > number;
	return commonValidate(field, errorMessage, 'input', condition);
}

function validateOnLower(field, errorMessage, number) {
	var condition = $("input[name='" + field + "']").val() < number;
	return commonValidate(field, errorMessage, 'input', condition);
}

function validateLengthOnLower(field, errorMessage, number) {
	var condition = $("input[name='" + field + "']").val().length < number;
	return commonValidate(field, errorMessage, 'input', condition);
}

function validateOnMatch(field1, field2, errorMessage) {
	var condition = $("input[name='" + field1 + "']").val() != $("input[name='" + field2 + "']").val();
	commonValidate(field1, errorMessage, 'input', condition);
	return commonValidate(field2, errorMessage, 'input', condition);
}

function validateOnImageType(field, errorMessage) {
	var condition = !($("input[name='" + field + "']")[0].files[0].type.includes('image'));
	return commonValidate(field, errorMessage, 'input', condition);
}

function validateFileSize(field, file, errorMessage, bytesSize) {
	var condition = $("input[name='" + field + "']")[0].files[file].size > bytesSize;
	return commonValidate(field, errorMessage, 'input', condition);
}