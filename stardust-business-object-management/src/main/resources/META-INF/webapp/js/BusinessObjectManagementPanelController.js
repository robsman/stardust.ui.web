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
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.changeBusinessObject = function(
						businessObject) {
					this.businessObject = businessObject;
					this.businessObjectInstances = [];
					this.keyFields = [];
					this.topLevelFields = [];

					if (!this.businessObject) {
						return;
					}

					// Create labels for all used types

					if (this.businessObject.types) {
						for ( var type in this.businessObject.types) {
							for (var n = 0; n < this.businessObject.types[type].fields.length; ++n) {
								this.businessObject.types[type].fields[n].label = this
										.createLabel(this.businessObject.types[type].fields[n].name);
							}
						}
					}

					for (var n = 0; n < this.businessObject.fields.length; ++n) {
						// TODO Retrieve label from annotations

						this.businessObject.fields[n].label = this
								.createLabel(this.businessObject.fields[n].name);

						if (!this.businessObject.types) {
							this.businessObject.types = {};
						}

						if (this.businessObject.types[this.businessObject.fields[n].type]) {
							continue;
						}

						if (this.businessObject.fields[n].primaryKey) {
							this.primaryKeyField = this.businessObject.fields[n];
						} else if (this.businessObject.fields[n].key) {
							this.keyFields.push(this.businessObject.fields[n]);
						}

						this.topLevelFields.push(this.businessObject.fields[n]);
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.createLabel = function(
						str) {
					return str
					// insert a space between lower & upper
					.replace(/([a-z])([A-Z])/g, '$1 $2')
					// space before last upper in a sequence followed by lower
					.replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3')
					// uppercase the first character
					.replace(/^./, function(str) {
						return str.toUpperCase();
					})
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.filterBusinessObjectInstances = function() {
					var self = this;

					BusinessObjectManagementService
							.instance()
							.getBusinessObjectInstances(this.businessObject,
									this.primaryKeyField, this.keyFields)
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
