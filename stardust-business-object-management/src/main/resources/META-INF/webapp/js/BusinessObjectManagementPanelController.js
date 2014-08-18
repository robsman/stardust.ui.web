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
			}
		});
