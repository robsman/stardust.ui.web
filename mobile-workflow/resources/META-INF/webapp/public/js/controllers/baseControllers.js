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
	
	var loginModel=function(acc,pw,part){
		this.account = acc;
		this.password = pw;
		this.partition = part;
	};
	
	var baseCtrl = {
			
			/* Handle login submission, on success load our rootScope level data and 
			 * trigger a navigateTo event on rootScope.*/
			"loginCtrl" : function($scope,$rootScope,workflowService,utilService){
				
				//TODO: ZZM - remove default values
				$scope.loginModel=new loginModel("motu","motu","default");
				
				$scope.login = function(account,password,partition){
					var loginPromise = workflowService.login(account,password,partition),
						success=function(user){
							$rootScope.appData.user=user;
							$rootScope.appData.isAuthorized=true;
							utilService.navigateTo($rootScope,"#mainPage",{});
						},
						fail = function(status){
							//TODO: handle fail case
							console.log("Login Failed");
						};
						
					loginPromise.then(success,fail);
				};

				$scope.reset = function(){
					$scope.loginModel=new loginModel("","","");
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