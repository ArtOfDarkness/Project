messageResource.init({ filePath: '../' });
var currentLocale = localStorage.getItem('locales');
messageResource.load('message', function() {}, currentLocale);

function viewSubjectsBySpeciality() {
	var speciality = $("select[name='speciality']").val();
	var subjects = null;

	if (speciality != '') {	
		jQuery.get('../subjectBySpeciality', {id: speciality}, function(data) {
			if (data !== '') {
				subjects = data;
			}
		}).done(
			function() {
				var formContent = '<h5>' + messageResource.get('aplication.zno_marks', 'message', currentLocale) + ':</h5>';
				jQuery.each(subjects, function(i, value) {
					formContent +=
						"<div class='form-group row'>"
							+ "<label class='col-sm-2 col-form-label'>" + value.title + ": </label>"
							+ "<div class='col-sm-6'>"
								+ "<input class='subject form-control'"
									+ "type='number' step='1' min='100' max='200' name='subject" + value.id + "' placeholder='" + value.title +"'/>"
								+ "<div id='subject" + value.id + "Error'></div>"
							+ "</div>"
						+ "</div>"					
				});
					$('#subjects').html(formContent);
			});
	} else {
		$('#subjects').html('');
	}
}

function handleFileMultipartInputPlaceholder() {
	var filesInput = $("input[name='supportingDocument']");

	filesInput.next('.custom-file-label').addClass('selected').html(
		filesInput[0].files.length == 0 ? messageResource.get('aplication.supporting_documents_placeholder', 'message', currentLocale) :
			( filesInput[0].files.length == 1 ? filesInput.val().split('\\').pop() : 
				messageResource.get('aplication.supporting_documents_files_uploaded', 'message', currentLocale) + ': ' + filesInput[0].files.length ) )
}

function handleFileSelect(event) {
	var files = event.target.files;

	var output = [];
	for (var i = 0, file; file = files[i]; i++) {
		output.push('<li><strong>', file.name, '</strong> (', file.type || 'n/a', ') - ', file.size, ' ',
				messageResource.get('aplication.supporting_documents_bytes', 'message', currentLocale), '</li>');
	}

	document.getElementById('filesList').innerHTML = '<ul>' + output.join('') + '</ul>';
}

document.addEventListener('DOMContentLoaded', function() {
	if (document.getElementById('files') != null) {
		document.getElementById('files').addEventListener('change',	handleFileSelect, false);
	}
});

document.addEventListener('DOMContentLoaded', function() {
	$('#submit').click(function() {
		var subjects = $('.subject');
		var supportingDocuments = $('#files');
		var errors = 0;

		errors += validateSelectOnEmptiness('speciality', 'specialityError');
		
		if (subjects != null) {
			for (var i = 0; i < subjects.length; i++) {				
				var subjectMarkError = validateOnEmptiness(subjects[i].name, 'subjectMarkError');
				errors += subjectMarkError;
						
				if (subjectMarkError == 0) {
					var subjectMarkGreaterError = validateOnGreater(subjects[i].name, 'subjectMarkGreaterError', 200);
					errors += subjectMarkGreaterError;
					
					if (subjectMarkGreaterError == 0) {
						var subjectMarkLowerError = validateOnLower(subjects[i].name, 'subjectMarkLowerError', 100);
						errors += subjectMarkLowerError;
					}
				}
			}
		}
		
		var attMarkError = validateOnEmptiness('attMark', 'attMarkError');
		errors += attMarkError;
				
		if (attMarkError == 0) {
			var attMarkGreaterError = validateOnGreater('attMark', 'attMarkGreaterError', 200);
			errors += attMarkGreaterError;
			
			if (attMarkGreaterError == 0) {
				var attMarkLowerError = validateOnLower('attMark', 'attMarkLowerError', 100);
				errors += attMarkLowerError;
			}
		}
		
		if (supportingDocuments != null) {
			for (var i = 0; i < supportingDocuments[0].files.length; i++) {
				var supportingDocumentError = validateFileSize('supportingDocument', i, 'supportingDocumentError', 8388608);
				errors += supportingDocumentError;
				
				if (supportingDocumentError == 1)
					break;
			}
		}
		
		if (errors != 0) {
			return false;
		} else {
			$('form').submit();
		}
	});
});

document.addEventListener('DOMContentLoaded', function() {
	var checkbox = document.getElementById('accept');

	if (checkbox != null) {
		checkbox.onclick = function() {
			handleRejectionMessageVisibility(checkbox);
			handleSubmitButtonStyleOnCheckboxChange(checkbox);
		}
	}
});

function handleRejectionMessageVisibility(checkbox) {
	var rejectionMessage = document.getElementById('rejectionMessage');
	var state = (checkbox.checked) ? 'none' : 'block';

	rejectionMessage.style.display = state;
	rejectionMessage.value = '';		
}
	
function handleSubmitButtonStyleOnCheckboxChange(checkbox) {
	var button = document.getElementById('submit');

	if (checkbox.checked) {
		button.classList.remove('btn-primary');
		button.classList.remove('btn-danger');
		button.classList.add('btn-success');
		button.innerHTML = messageResource.get('aplication.accept', 'message', currentLocale);
	} else {
		button.classList.remove('btn-success');
		button.classList.remove('btn-danger');
		button.classList.add('btn-primary');
		button.innerHTML = messageResource.get('aplication.save', 'message', currentLocale);
	}
}

document.addEventListener('DOMContentLoaded', function handleSubmitButtonStyleOnRejectionMessageChange() {
	var rejectionMessage = document.getElementById('rejectionMessage');
	var button = document.getElementById('submit');

	if (rejectionMessage != null) {
		rejectionMessage.oninput = function() {
			if (rejectionMessage.value != '') {
				button.classList.remove('btn-primary');
				button.classList.remove('btn-danger');
				button.classList.add('btn-danger');
				button.innerHTML = messageResource.get('aplication.reject', 'message', currentLocale);		
			} else {
				button.classList.remove('btn-success');
				button.classList.remove('btn-danger');
				button.classList.add('btn-primary');
				button.innerHTML = messageResource.get('aplication.save', 'message', currentLocale);
			}
		}
	}
});