/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[
				"business-object-management/js/Utils",
				"business-object-management/js/BusinessObjectManagementPanelController",
				"business-object-management/js/BusinessObjectManagementService" ],
		function(Utils, BusinessObjectManagementPanelController,
				BusinessObjectManagementService) {
			return {
				create : function() {
					var controller = new BusinessObjectManagementViewController();

					return controller;
				}
			};

			/**
			 * 
			 */
			function BusinessObjectManagementViewController() {
				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.initialize = function() {
					this.messages = [];
					this.businessObjectManagementPanelController = BusinessObjectManagementPanelController
							.create();

					var self = this;

					this.businessObjectManagementPanelController.initialize(
							this).done(function() {
						self.safeApply();
					});
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.onBusinessObjectInstanceSelectionChange = function() {
					this.currentBusinessObjectInstance = this.businessObjectManagementPanelController.selectedBusinessObjectInstances[0];

					console.log("BOI");
					console.log(this.currentBusinessObjectInstance);

					this.safeApply();
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.createBusinessObjectInstance = function() {
					this.messages = [];
					this.currentBusinessObjectInstance = {};
					this.newBusinessObjectInstance = this.currentBusinessObjectInstance;
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.saveBusinessObjectInstance = function() {
					var self = this;

					this.messages = [];

					if (this.newBusinessObjectInstance) {
						if (this.currentBusinessObjectInstance[this.businessObjectManagementPanelController.primaryKeyField.id] == null
								|| this.currentBusinessObjectInstance[this.businessObjectManagementPanelController.primaryKeyField.id].length == 0) {
							this.messages = [ "Primary Key "
									+ this.businessObjectManagementPanelController.primaryKeyField.name
									+ " not set." ];
						}

						BusinessObjectManagementService
								.instance()
								.createBusinessObjectInstance(
										this.businessObjectManagementPanelController.businessObject.model.oid,
										this.businessObjectManagementPanelController.businessObject.businessObject.id,
										this.currentBusinessObjectInstance[this.businessObjectManagementPanelController.primaryKeyField.id],
										this.currentBusinessObjectInstance)
								.done(function() {
									self.safeApply();
								}).fail();

						this.newBusinessObjectInstance = null;
					} else {
						BusinessObjectManagementService
								.instance()
								.updateBusinessObjectInstance(
										this.businessObjectManagementPanelController.businessObject.model.oid,
										this.businessObjectManagementPanelController.businessObject.businessObject.id,
										this.currentBusinessObjectInstance[this.businessObjectManagementPanelController.primaryKeyField.id],
										this.currentBusinessObjectInstance)
								.done(function() {
									self.safeApply();
								}).fail();
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.safeApply = function(
						fn) {
					var phase = this.$root.$$phase;

					if (phase == '$apply' || phase == '$digest') {
						if (fn && (typeof (fn) === 'function')) {
							fn();
						}
					} else {
						this.$apply(fn);
					}
				};
			}
		});
