messageResource.init({ filePath: '../' });
var currentLocale = localStorage.getItem('locales');
messageResource.load('message', function() {}, currentLocale);

document.addEventListener('DOMContentLoaded', function() {
	var checkbox = $("input[type='checkbox']");

	for (var i = 0; i < checkbox.length; i++) {
		if (!checkbox[i].checked) {
			$("input[name='coeff" + checkbox[i].name + "']")[0].style.display = 'none';
		}
	}
});

document.addEventListener('DOMContentLoaded', function() {
	$("input[type='checkbox']").click(function() {
		var state = ($(this)[0].checked) ? 'block' : 'none';
		var field = 'coeff' + $(this).attr('name');

		$("input[name='" + field + "']")[0].style.display = state;
		$("input[name='" + field + "']").val('');
		$("div[id='" + field + "Error']").html('');
		$("div[id='" + field + "Error']").removeClass('invalid-feedback');
		$("input[name='" + field + "']").removeClass('is-invalid');
	});
});

document.addEventListener('DOMContentLoaded', function() {
	$('#submit').click(function() {
		var checkbox = $("input[type='checkbox']");
		var errors = 0;

		errors += validateOnEmptiness('title', 'facultyTitleError');

		for (var i = 0; i < checkbox.length; i++) {
			if (checkbox[i].checked) {
				errors += validateOnEmptiness('coeff' + checkbox[i].name, 'facultyCoefficientError');
			}
		}

		var checkedSubjects = 0;
		var totalCoeff = 0;
		if (errors == 0) {
			for (var i = 0; i < checkbox.length; i++) {
				if (checkbox[i].checked) {
					checkedSubjects += 1;
					totalCoeff += parseFloat($("input[name='coeff" + checkbox[i].name + "']").val());
				}
			}				
		}
		
		if (errors != 0) {
			$('#facultyTotalCoeffMessage').html('');
			$('#facultyTotalCoeffMessage').removeClass('alert alert-danger');
			return false;
		} else if (checkedSubjects > 0 && totalCoeff != 1) {
			$('#facultyTotalCoeffMessage').addClass('alert alert-danger');
			$('#facultyTotalCoeffMessage').html(messageResource.get('facultyTotalCoeffMessage', 'message', currentLocale));
			return false;
		} else {
			$('#facultyTotalCoeffMessage').html('');
			$('#facultyTotalCoeffMessage').removeClass('alert alert-danger');
			$('form').submit();
		}
	});
});