angular.module('bpm-common.directives')
.directive("sdDialog",['ngDialog',function(ngDialog){
	
	return {
		
		restrict: 'A',
		
		scope : {
			ngDialogScope : '=sdDialogScope',
			ngOnOpen : '&sdOnOpen',
			userTemplate : '@sdTemplate',
			showDialog : '=sdShowDialog'
		},
		
		link: function (scope, elem, attrs) {
			
			attrs.sdTriggerOn = attrs.sdTriggerOn || 'click';
			
			if( angular.isDefined(attrs.sdShowDialog)){
				attrs.sdTriggerOn='SCOPE';
			};
			
			var showDialog = function(e){
				var dialogType = (attrs.sdDialogType || 'INFO').toUpperCase(),
					templateUrlBase = "plugins/html5-common/scripts/directives/dialogs/templates/",
					templateUrl,
					invokeOpenConfirm=false,
					ngDialogScope,
					options,
					className = 'ngdialog-theme-default',
					isPopover;
				
				if(attrs.sdPosition && angular.isString(attrs.sdPosition)){
					attrs.sdPosition=attrs.sdPosition.toUpperCase();
				}
				
				if(attrs.sdPosition === 'POPOVER'){
					isPopover = true;
					className = 'ngdialog-theme-popup';
				}
				
				if(e && e.preventDefault){
					e.preventDefault();
				}
				else{
					e={clientX:0,clientY:0 };
				}
				
				switch(dialogType){
					case "INFO":
						templatePage="info.html";
						break;
					case "ALERT":
						templatePage="alert.html";
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
							className: className,
							controller: attrs.ngDialogController,
							data: attrs.ngDialogData,
							scope: ngDialogScope ,
							onOpen: scope.ngOnOpen,
							position: [e.clientX,e.clientY],
							isCentered : isPopover ? false : true,
							isMoveable : (attrs.sdIsMoveable === 'true') ? true : false,
							showOverlay: attrs.sdShowOverlay === 'false' ? false : true,
							showClose: attrs.ngDialogShowClose === 'false' ? false : true,
							title: attrs.sdTitle,
							closeByDocument: attrs.ngDialogCloseByDocument === 'false' ? false : true,
							closeByEscape: attrs.ngDialogCloseByEscape === 'false' ? false : true };
				
				angular.isDefined(attrs.ngDialogClosePrevious) && ngDialog.close(attrs.ngDialogClosePrevious);
				
				if(dialogType==="CONFIRM"){
					ngDialog.openConfirm(options);
				}
				else{
					ngDialog.open(options);
				}
				
			};
			
			if(attrs.sdTriggerOn==="SCOPE"){
				scope.$watch("showDialog",function(v){
					if(v === true){
						showDialog();
						scope.showDialog=false;
					}
				});
			}
			else{
				elem.on(attrs.sdTriggerOn, showDialog);
			}
		}
	};
	
}]);