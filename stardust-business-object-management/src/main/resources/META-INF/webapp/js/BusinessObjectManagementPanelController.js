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
					console.log("Initialize Search");

					this.rootController = rootController;
					this.businessObjectFilter = {};
					this.selectedBusinessObjectInstances = [];

					var deferred = jQuery.Deferred();
					var self = this;

					BusinessObjectManagementService
							.instance()
							.getBusinessObjects()
							.done(
									function(businessObjectModels) {
										self.businessObjectModels = businessObjectModels;

										console.log(self.businessObjectModels);

										self.refreshBusinessObjects();

										deferred.resolve();
									}).fail(function() {
								deferred.reject();
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.refreshBusinessObjects = function() {
					this.businessObjects = [];

					for (var n = 0; n < this.businessObjectModels.length; ++n) {
						for (var m = 0; m < this.businessObjectModels[n].businessObjects.length; ++m) {
							if (!this.businessObjectModels[n].businessObjects[m].types) {
								this.businessObjectModels[n].businessObjects[m].types = {};
							}

							this.businessObjects
									.push({
										label : this.businessObjectModels[n].name
												+ "/"
												+ this.businessObjectModels[n].businessObjects[m].name,
										model : this.businessObjectModels[n],
										businessObject : this.businessObjectModels[n].businessObjects[m]
									});
						}
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.onBusinessObjectChanged = function() {
					this.businessObjectInstances = [];
					this.keyFields = [];
					this.topLevelFields = [];

					if (!this.businessObject) {
						return;
					}

					for (var n = 0; n < this.businessObject.businessObject.fields.length; ++n) {
						if (this.businessObject.businessObject.types[this.businessObject.businessObject.fields[n].type]) {
							continue;
						}

						if (this.businessObject.businessObject.fields[n].primaryKey) {
							this.primaryKeyField = this.businessObject.businessObject.fields[n];
						} else if (this.businessObject.businessObject.fields[n].key) {
							this.keyFields
									.push(this.businessObject.businessObject.fields[n]);
						}

						this.topLevelFields
								.push(this.businessObject.businessObject.fields[n]);
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementPanelController.prototype.filterBusinessObjectInstances = function() {
					var self = this;

					BusinessObjectManagementService
							.instance()
							.getBusinessObjectInstances(
									this.businessObject.modelOid,
									this.businessObject.businessObject.id,
									this.primaryKeyField, this.keyFields)
							.done(
									function(businessObjectInstances) {
										self.businessObjectInstances = businessObjectInstances;

										self.rootController.safeApply();
									}).fail();
				};
			}
		});
