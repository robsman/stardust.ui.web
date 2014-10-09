angular.module('bpm-common.directives')
.directive("sdDialog",['ngDialog',function(ngDialog){
	
	return {
		
		restrict: 'A',
		
		scope : {
			ngDialogScope : '=sdDialogScope',
			ngOnOpen : '&sdOnOpen',
			userTemplate : '@sdTemplate'
		},
		
		link: function (scope, elem, attrs) {
			elem.on('click', function (e) {
				
				//INFO||CONFIRM
				var dialogType = (attrs.sdDialogType || 'INFO').toUpperCase(),
					templateUrlBase = "plugins/html5-common/scripts/directives/dialogs/templates/",
					templateUrl,
					invokeOpenConfirm=false,
					ngDialogScope,
					options;

				e.preventDefault();
				
				switch(dialogType){
					case "INFO":
						templatePage="info.html";
						break;
					case "CONFIRM":
						templatePage="confirm.html";
						invokeOpenConfirm=true;
						break;
					default:
						templateUrl="info.html";
				}

				ngDialogScope = angular.isDefined(scope.ngDialogScope) ? scope.ngDialogScope : 'noScope';
				options = { template: templateUrlBase + templatePage,
							userTemplate : scope.userTemplate,
							className: "ngdialog-theme-default",
							controller: attrs.ngDialogController,
							title: attrs.sdTitle,
							scope: ngDialogScope ,
							onOpen: scope.ngOnOpen,
							data: attrs.ngDialogData,
							showClose: attrs.ngDialogShowClose === 'false' ? false : true,
							closeByDocument: attrs.ngDialogCloseByDocument === 'false' ? false : true,
							closeByEscape: attrs.ngDialogCloseByEscape === 'false' ? false : true };
				
				angular.isDefined(attrs.ngDialogClosePrevious) && ngDialog.close(attrs.ngDialogClosePrevious);
				
				if(dialogType==="CONFIRM"){
					ngDialog.openConfirm(options);
				}
				else{
					ngDialog.open(options);
				}
				
			});
		}
	};
	
}]);