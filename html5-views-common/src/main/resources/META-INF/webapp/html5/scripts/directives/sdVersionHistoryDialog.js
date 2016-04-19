/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * Impelemnts a dialog which given a document ID in the document repository, will show
 * that documents corresponding file version history.
 *
 * @ATTRIBUTES
 * ---------------------------------------
 * SYNCHRONICITY WARNING: Always set sda-document-id before you change the sda-show value. Same rule if you call api.open without passing
 * 						  in the optional documentID variable. If passing in the documentID then the dialog will update correctly otherwise
 * 						  it relies on the value of the sda-document-id attribute at the time it detects that the sda-show value has changed
 * 						  or at the time of the api.open invocation.
 * 						  Doing so after the dialog is open will not result in a data call to the server.
 * 
 * sd-version-history-dialog : [@] Name of property on parent controller we will assign the dialog api to. This will be the api
 * 								   leveraged to open or close the dialog programatically (attr.open(docid) | attr.close()).
 * 								   
 * sda-document-id: [=] The doucment ID we will retrieve file history for, and display in our dialog.
 * 
 * sda-show: [=]{bool} ALternate method of opening the dialog. When true the dialog will open. Upon closing the dialog will
 * 			 update this value to false so there is no real need to set this to false by the directive user.
 * 
 */
(function(){

	/*CONTROLLER IMPLEMENTATION*/
	sdVersionHistoryDialogCtrl.$inject = ["$timeout", "documentRepositoryService", "$scope", "sdI18nService","$parse", "$q"];

	/**
	 * Controller for the version histroy directive.
	 * 
	 * @param  {[type]} $timeout                  [description]
	 * @param  {[type]} documentRepositoryService [description]
	 * @param  {[type]} $scope                    [description]
	 * @param  {[type]} sdI18nService             [description]
	 * @param  {[type]} $parse                    [description]
	 * @return {[type]}                           [description]
	 */
	function sdVersionHistoryDialogCtrl($timeout, documentRepositoryService, $scope, sdI18nService, $parse, $q){

		var that = this;

		//expose dependencies
		this.$parse = $parse;
		this.$scope = $scope;
		this.$timeout = $timeout;
		this.$q = $q;
		this.documentRepositoryService = documentRepositoryService;

		//create i18n based text map
		this.i18n = sdI18nService.getInstance('views-common-messages');
		this.textMap = this.getTextMap(this.i18n);

		//Assign our api to the attribute provided by the directive user. This will
		//allow them to programatically open and close the dialog.
		$timeout(function(){
			that.assignApi($scope.assignableAPI,$scope.$parent);
		},0);

	};

	/**
	 * Compute our internationalized text map.
	 * @param  {[type]} i18n [instance of our sdI18nService]
	 * @return {[type]}      [description]
	 */
	sdVersionHistoryDialogCtrl.prototype.getTextMap = function(i18n){

			var textMap = {};

			textMap.confirm = i18n.translate("common.confirm","!confirm");
			textMap.close = i18n.translate("common.close","!close");
			textMap.versionNo = i18n.translate("views.documentView.documentVersion.versionNo");
			textMap.documentName = i18n.translate("views.documentPanelView.documentPropertiesPanel.DocumentName");
			textMap.author = i18n.translate("views.documentSearchView.author.label");
			textMap.modificationDate = i18n.translate("views.documentView.documentVersion.modifiedDate");
			textMap.comments = i18n.translate("views.common.comments.label");
			textMap.header = i18n.translate("views.documentView.documentVersion.versionHistoryHeader");

			return textMap;		
	};

	/**
	 * Wrapper function for our services getFileVersionHistory method.
	 * 
	 * @param  {[type]} docId [Document UUID of the form {urn:repositoryId:*****}{jcrUuid}*****]
	 * @return {[type]}       [description]
	 */
	sdVersionHistoryDialogCtrl.prototype.getFileVersionHistory = function(docId){
		return this.documentRepositoryService.getFileVersionHistory(docId);
	}

	/**
	 * given the documentID of interest this function will retrieve that documents
	 * version history from the server, assign it to our data table, and invoke a refresh
	 * on that table. Returns a promise, if opening in concert with this invocation then open
	 * when the promise resolves.
	 * @param  {string} docId [description]
	 * @return {promise}       [description]
	 */
	sdVersionHistoryDialogCtrl.prototype.init = function(docId){

		var that = this;
		var deferred = this.$q.defer();

		this.getFileVersionHistory(docId)
		.then(function(res){

			//guard against refreshing when initializing the first time
			var doRefresh = that.currentFileVersionHistory !== undefined;

			if(res.data.length >0){
				//Assign data to our controller, this exposes to our dataTable
				that.currentFileVersionHistory = res.data;
				//Refresh the data table otherwise we will be stuck viewing the old data. Guard
				//against refreshing before the API is ready, this can occur on the initial change
				//of the table from empty to having data.
				if(doRefresh && that.fvhTableApi && that.fvhTableApi.refresh){
					that.fvhTableApi.refresh(false);
				}
				//sort of hacky but we need to display the document name on the dialog so we 
				//assume the correct name to display is the name of the document in its current revision.
				that.title = that.textMap.header.replace("{0}",res.data[0].name);
			}
			//always resolve, even if no version history length = 0
			deferred.resolve(res);
		})
		["catch"](function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	}

	/**
	 * Handle the assignment of the base sdDialog api to the attribute specified by the 
	 * directive user. This is how we expose our open and close functions to the user.
	 * @param  {[type]} attr  [description]
	 * @param  {[type]} scope [description]
	 * @return {[type]}       [description]
	 */
	sdVersionHistoryDialogCtrl.prototype.assignApi = function(attr,scope){

		var targetName = attr;
		var dialogAttrAssignable;
		var api ={};
		var that = this;

		api.close = this.fileVersionHistoryDialog.close;
		api.open = function(newId){
			//api call allows ptional documentID variable
			that.targetDocument = (newId)?newId:that.targetDocument;
			that.init(that.targetDocument).then(function(){
				that.fileVersionHistoryDialog.open();
			});
		}

		if (angular.isDefined(targetName) && targetName != '') {

          dialogAttrAssignable = this.$parse(targetName).assign;

          if (dialogAttrAssignable) {
            dialogAttrAssignable(scope, api);
          }

        }

	};

	/**
	 * Callback we handle when the dialog closes. Without fail we will always set our
	 * show value to false. This is the same scoped value that is tied to the sda-show
	 * attribute which may be optionally supplied by our directive user. If we did not do this the
	 * user would be tasked with maintaining this state.
	 * @param  {[type]} res [description]
	 * @return {[type]}     [description]
	 */
	sdVersionHistoryDialogCtrl.prototype.onClose = function(res){
		this.$scope.show = false;
	};

	/*DIRECTIVE IMPLEMENTATION*/
	sdVersionHistoryDialog.$inject = ["sdUtilService","$q"];

	function sdVersionHistoryDialog(sdUtilService,$q){
		
		var templateUrl = sdUtilService.getBaseUrl() + 'plugins/html5-views-common/html5/scripts/directives/partials/sdVersionHistoryDialog.html',
			linkfx,
			that = this;

		/**
		 * Linking function where we will set up our watch on the targetDocument and 
		 * attempt to assign our api.
		 * @param  {[type]} scope   [description]
		 * @param  {[type]} element [description]
		 * @param  {[type]} attrs   [description]
		 * @return {[type]}         [description]
		 */
		linkfx = function(scope, element, attrs){

			//Watch our targetDocument ID and anytime it changes update property
			
			scope.$watch('targetDocument',function(newValue, oldValue, scope){
				if(!newValue){return;}
				scope.verHistoryCtrl.targetDocument=newValue;
			});

			//Watch our show value and anytime it changes to true then reinitalize our data
			//and then show the dialog. Note that everytime the dialog closes we set this
			//value back to false as part of the show invocation.
			scope.$watch('show', function(newValue, oldValue, scope){
				if(newValue===true){
					//always refresh on show
					scope.verHistoryCtrl.init(scope.verHistoryCtrl.targetDocument)
					.then(function(){
						scope.verHistoryCtrl.fileVersionHistoryDialog.open();
					});
				}
				else{
					scope.verHistoryCtrl.fileVersionHistoryDialog.close();
				}
			});

		};
		
		return {
			"controller" : sdVersionHistoryDialogCtrl,
			"controllerAs" : "verHistoryCtrl",
			"link" : linkfx,
			"scope": {
				"targetDocument" : "=sdaDocumentId", //document ID 
				"assignableAPI"		: "@sdVersionHistoryDialog", //attribute we will assign the dialog api to.
				"show" : "=sdaShow" //alternate method to open dialog using bool attribute
			},
			"transclude" : true,
			"templateUrl" : templateUrl
		}
		
	};

	sdVersionHistoryDialog.$inject = ["sdUtilService","$q"];

	angular.module("bpm-common.directives")
	.directive("sdVersionHistoryDialog",sdVersionHistoryDialog);

})();