controller.customValidation = function() {

	if (this.Customer.firstName == "Lara" && this.Customer.firstName == "Soft") {

		this.errors.push({
			path : "Customer.firstName",

			type : "Love",

			message : "Lara, I love you."
		});
	}
};

var POSTAL_CODE_REGEXP = /^\-?\d+((\.|\,)\d+)?$/;

controller.angularModule.directive('sdZip', function() {
	return {
		require : 'ngModel',
		link : function(scope, elm, attrs, ctrl) {
			ctrl.$parsers.unshift(function(viewValue) {
				controller.removeError(attrs.ngModel, "sdZip");
				if (POSTAL_CODE_REGEXP.test(viewValue)) {
					ctrl.$setValidity('sdZip', true);

					return viewValue;
				} else {
					ctrl.$setValidity('sdZip', false);

					controller.errors.push({
						path : attrs.ngModel,
						type : "sdZip",
						message : "Invalid Postal Code Format ("
								+ controller
										.generateLabelForPath(attrs.ngModel)
								+ ")."
					});
					return undefined;
				}
			});
		}
	};
});