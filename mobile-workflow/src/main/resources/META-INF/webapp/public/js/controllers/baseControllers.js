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
		this.showPartition=true;
		this.hasError=false;
	};
	
	var errorModel=function(){
		this.errorMessage="";
		this.hasError =false;
		this.showExtended =false;
	};
	
	var headerModel = function(title){
		this.title= title || "Process Portal";
		this.activityInstance= {
				"oid"  : "",
				"name" : ""
			};
		this.showActivityNavBar =true;
	};
	
	var baseCtrl = {
			
			/* Handle login submission, on success load our rootScope level data and 
			 * trigger a navigateTo event on rootScope.*/
			"loginCtrl" : function($scope,$rootScope,workflowService,$timeout,utilService,$window,il18nService){
				
				var urlArray=$window.location.href.split("/"),
					index=urlArray.indexOf('a'),
					partition="default",
					showPartition=true;
				
				if(index>0){
					partition=(urlArray[index+1]);
					showPartition=false;
				}
				
				$scope.uiText={
						"username"  : il18nService.getProperty('mobile.login.username.placeholder'),
						"password"  : il18nService.getProperty('mobile.login.password.placeholder'),
						"partition" : il18nService.getProperty('mobile.login.partition.placeholder'),
						"login"     : il18nService.getProperty('mobile.login.submit.text'),
						"fail"      : il18nService.getProperty('mobile.login.fail')
				};
				
				$scope.loginModel=new loginModel("motu","motu",partition);
				$scope.loginModel.showPartition=showPartition;
				$scope.errorModel=new errorModel();
				
				$scope.login = function(account,password,partition){
					var loginPromise = workflowService.login(account,password,partition);
						
					loginPromise
						.then(function(user){
							$rootScope.$apply(function(){
								$rootScope.appData.user=user;
								$rootScope.appData.isAuthorized=true;
								$rootScope.appData.isActivityHot = false;
								$rootScope.appData.hotActivityInstance = {};
								$rootScope.appData.settings={"pageSize" : 5};
							});
							$rootScope.$broadcast("login",user);
							utilService.navigateTo($rootScope,"#mainPage",{});
						})
						.catch(function(){
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage= $scope.uiText.fail + " " + status;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						});
				};

				$scope.reset = function(){
					$scope.loginModel=new loginModel("","","");
				};
			},
			
			/*simple binding for our persistent header*/
			"headerCtrl" : function($scope,$rootScope,il18nService){
				$scope.headerModel = new headerModel();
				$scope.uiText = {
						"title" : il18nService.getProperty("mobile.extheader.title"),
						"tapto" : il18nService.getProperty("mobile.extheader.infobar.text")
				};
				
				$scope.$on("activityActivation",function(e){
					console.log("Activity Activation event received!");
					$scope.$apply(function(){
						$scope.headerModel.showActivityNavBar = true;
					});
				});
				
			},
			
			/*simple binding for our persistent footer*/
			"footerCtrl" : function($scope,$rootScope, $sce, workflowService){
				workflowService.getVersionAndCopyrightInfo().then(function(data) {
					$scope.title = $sce.trustAsHtml(data.copyrightInfo);
					$scope.version = data.version;
				}).catch(function() {
					$scope.title = $sce.trustAsHtml("SunGard &copy; " + (new Date()).getFullYear());
					$scope.version = 0.1;
				});
			}
	};
	return baseCtrl;
});