angular.module('bpm-common.directives')
.directive("sdConfirmDialog",['ngDialog',function(ngDialog){
	
	return {
		restrict: 'A',
		scope : {
			ngDialogScope : '=',
			ngOnOpen : '&sdOnOpen'
		},
		link: function (scope, elem, attrs) {
			elem.on('click', function (e) {
				e.preventDefault();

				var ngDialogScope = angular.isDefined(scope.ngDialogScope) ? scope.ngDialogScope : 'noScope';
				angular.isDefined(attrs.ngDialogClosePrevious) && ngDialog.close(attrs.ngDialogClosePrevious);

				ngDialog.openConfirm({
					template: "plugins/html5-common/scripts/directives/dialogs/templates/confirm.html",
					className: "ngdialog-theme-default",
					controller: attrs.ngDialogController,
					scope: ngDialogScope ,
					onOpen: scope.ngOnOpen,
					data: attrs.ngDialogData,
					showClose: attrs.ngDialogShowClose === 'false' ? false : true,
					closeByDocument: attrs.ngDialogCloseByDocument === 'false' ? false : true,
					closeByEscape: attrs.ngDialogCloseByEscape === 'false' ? false : true
				});
			});
		}
	};
	
}]);