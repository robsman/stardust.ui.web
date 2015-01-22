/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "business-object-management/js/Utils",
				"business-object-management/js/BusinessObjectManagementService" ],
		function(Utils, BusinessObjectManagementService) {
			return {
				create : function() {
					var controller = new BusinessObjectManagementPanelController();

					return controller;
				}
			};

			/**
			 * 
			 */
			function BusinessObjectManagementPanelController() {
				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.initialize = function(
						rootController) {
					this.importCSVDialog = {
						errors : []
					};
					this.rootController = rootController;
					this.businessObjectFilter = {};
					this.selectedBusinessObjectInstances = [];
					this.relatedBusinessObjInstancesToRemove = [];
					this.relatedBusinessObjectInstances = [];
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.changeBusinessObject = function(
						businessObject) {
					this.businessObject = businessObject;
					this.businessObjectInstances = [];

					if (!this.businessObject) {
						return;
					}
				};
				
				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.addInstanceToRelationship = function () {
					var tempArray = [];
					for(var n=0; n <  this.selectedBusinessObjectInstances.length; ++n){
						if(jQuery.inArray(this.selectedBusinessObjectInstances[n], this.relatedBusinessObjectInstances) == -1){
							tempArray.push(this.selectedBusinessObjectInstances[n]);
						}
					}
					this.relatedBusinessObjectInstances = this.relatedBusinessObjectInstances.concat(tempArray);
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.removeInstanceFromRelationship = function() {
					for ( var n = 0; n < this.relatedBusinessObjInstancesToRemove.length; ++n) {
						var idx = jQuery.inArray(
								this.relatedBusinessObjInstancesToRemove[n],
								this.relatedBusinessObjectInstances);
						if (idx != -1) {
							this.relatedBusinessObjectInstances.splice(idx, 1);
						}
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.setRelationshipContext = function(
						rootBusinessObjectInstance, relationship) {
					this.rootBusinessObjectInstance = rootBusinessObjectInstance;
					this.relationship = relationship;
					this.relatedBusinessObjInstancesToRemove = [];
					var self = this;

					BusinessObjectManagementService
							.instance()
							.getRelatedBusinessObjectInstances(
									this.businessObject,
									this.rootBusinessObjectInstance, this.rootBusinessObjectInstance[this.relationship.otherForeignKeyField])
							.done(
									function(businessObjectInstances) {
										console.log("Result");
										console.log(businessObjectInstances);

										self.businessObjectInstances = businessObjectInstances;

										self.selectAllInRelationship();
										self.rootController.safeApply();
									}).fail();
				};

				/**
				 * This is required because selected objects go by identity.
				 */
				BusinessObjectManagementPanelController.prototype.selectAllInRelationship = function() {
					var foreignKeys = this.rootBusinessObjectInstance[this.relationship.otherForeignKeyField];

					console.log("FK");
					console.log(foreignKeys);
					console.log(this.businessObject);

					this.selectedBusinessObjectInstances = [];
					this.relatedBusinessObjectInstances = [];
					// To keep track of old relationships, on table selection change
					if (foreignKeys && foreignKeys.length) {
						for (var n = 0; n < this.businessObjectInstances.length; ++n) {
							for (var m = 0; m < foreignKeys.length; ++m) {
								if (foreignKeys[m] == this.businessObjectInstances[n][this.businessObject.primaryKeyField.id]) {
									
									this.relatedBusinessObjectInstances.push(this.businessObjectInstances[n]);
									break;
								}
							}
						}
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.setRelationship = function(
						relationship) {
					this.relationship = relationship;
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.clearFilters = function() {
					this.businessObject.primaryKeyField.filterValue = null;

					for (var n = 0; n < this.businessObject.keyFields.length; ++n) {
						this.businessObject.keyFields[n].filterValue = null;
					}

					this.filterBusinessObjectInstances();
				};
				
				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.fetchKeyFields = function() {
					var keyFields = [];

					for (var n = 0; this.businessObject && n < this.businessObject.keyFields.length; n++) {
						// Skip complex fields

						if (this.businessObject.keyFields[n].list){
							continue;
						}

						keyFields.push(this.businessObject.keyFields[n]);
					}
					return keyFields;
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.filterBusinessObjectInstances = function() {
					var self = this;

					BusinessObjectManagementService
							.instance()
							.getBusinessObjectInstances(this.businessObject)
							.done(
									function(businessObjectInstances) {
										console.log("Result");
										console.log(businessObjectInstances);

										self.businessObjectInstances = businessObjectInstances;

										self.rootController.safeApply();
									}).fail();
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.openImportCSVDialog = function() {
					console.log("Dialog");
					console.log(this.importCSVDialog);

					this.importCSVDialog.dialog("open");
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.openImportCSVDialog = function() {
					jQuery("#uploadButton").prop('disabled', true);

					this.importCSVDialog.dialog("option", "modal", true);
					this.importCSVDialog.dialog("open");
					this.importCSVDialog.errors = [];
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.closeImportCSVDialog = function() {
					this.importCSVDialog.dialog("close");
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.onCSVFileSelect = function() {
					this.importCSVDialog.progressVisible = false;
					this.importCSVDialog.errors = [];

					this.rootController.safeApply();

					var file = document.getElementById('csvFile').files[0];

					if (!file) {
						jQuery("#uploadButton").prop('disabled', true);
					} else {
						jQuery("#uploadButton").prop('disabled', false);
					}
				}
				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.uploadCSVFile = function() {
					var formData = new FormData();
					var file = document.getElementById('csvFile').files[0];

					formData.append("file", file);

					var client = new XMLHttpRequest();
					var self = this;

					client.addEventListener("progress", function(event) {
						self.uploadProgress(event);
					}, false);
					client.addEventListener("load", function(event) {
						self.uploadComplete(event);
					}, false);
					client.addEventListener("error", function(event) {
						self.uploadFailed(event);
					}, false);
					client.addEventListener("abort", function(event) {
						self.uploadCanceled(event);
					}, false);

					client
							.open(
									"POST",
									this.getRootUrl()
											+ "/services/rest/business-object-management/businessObject/"
											+ self.businessObject.modelOid
											+ "/" + self.businessObject.id
											+ ".form-data");

					client.setRequestHeader("Content-Type",
							"multipart/form-data");

					this.importCSVDialog.progressVisible = true;

					try {
						var response = client.send(formData);

						if (!response) {
							throw "Upload failed.";
						}
					} catch (x) {
						this.importCSVDialog.progressVisible = false;
						this.importCSVDialog.errors.push({
							message : "Upload failed."
						});

						this.rootController.safeApply();
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.uploadProgress = function(
						event) {
					if (event.lengthComputable) {
						this.importCSVDialog.progress = Math.round(event.loaded
								* 100 / event.total)
					} else {
						this.importCSVDialog.progress = '?';
					}

					this.rootController.safeApply();
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.uploadComplete = function(
						event) {
					// TODO Load new BOIs
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.uploadFailed = function(
						event) {
					this.importCSVDialog.progressVisible = false;
					this.importCSVDialog.errors.push({
						message : "Upload failed."
					});

					this.rootController.safeApply();
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.uploadCanceled = function(
						event) {
					this.importCSVDialog.errors.push({
						message : "Upload cancelled."
					});

					this.rootController.safeApply();
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.getRootUrl = function() {
					return location.href.substring(0, location.href
							.indexOf("/plugins"));
				};
			}
		});
