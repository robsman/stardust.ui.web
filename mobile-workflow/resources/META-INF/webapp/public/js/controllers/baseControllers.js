/**
 * Controllers Not directly related to Worklist/Process/Activity. 
 * CONTROLLERS:
 * --------------------------------------------------------------
 * 1. loginCtrl : loginPage control, $rootScope tracks global authorization.
 * 				  REF app.js "ui.inptLogin.on("click"..."
 * 2. footerCtrl : controller for our persistent footer.
 * 3. headerCtrl : controller for our persistent header.
 */

define([],function(){
	var baseCtrl = {
			
			/*handle login binding to our DOM, actual global level authorization
			 *is handled by app.js which handles login attempts and sets the resulting
			 *value on our applications $rootScope*/
			"loginCtrl" : function($scope,$rootScope){
				$scope.username="motu";
				$scope.password="motu";
				$scope.partition = "default";
				$scope.reset = function(){
					$scope.username="";
					$scope.password="";
				};
			},
			
			/*simple binding for our persistent header*/
			"headerCtrl" : function($scope,$rootScope){
				$scope.title = "Process Portal";
			},
			
			/*simple binding for our persistent footer*/
			"footerCtrl" : function($scope,$rootScope, $sce){
				var title="&copy; " + (new Date()).getFullYear() + " SunGard";
				$scope.title = $sce.trustAsHtml(title);
				$scope.version = "0.1";
			}
	};
	return baseCtrl;
});