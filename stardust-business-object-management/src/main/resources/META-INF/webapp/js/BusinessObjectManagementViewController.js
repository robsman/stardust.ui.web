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
					this.formColumns = [ {
						min : 0,
						max : 3
					}, {
						min : 4,
						max : 7
					}, {
						min : 8,
						max : 11
					}, {
						min : 12,
						max : 15
					}, {
						min : 16,
						max : 19
					} ]; // TODO
					// Calculate

					this.messages = [];
					this.businessObjectManagementPanelController = BusinessObjectManagementPanelController
							.create();

					var self = this;

					this.businessObjectManagementPanelController.initialize(
							this).done(function() {
						self.safeApply();
						jQuery("#businessObjectTabs").tabs();
					});
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.onBusinessObjectInstanceSelectionChange = function() {
					console.log("===> Event fired");
					console
							.log(this.businessObjectManagementPanelController.selectedBusinessObjectInstances);

					this.currentBusinessObjectInstance = this.businessObjectManagementPanelController.selectedBusinessObjectInstances[0];

					this.createMissingComplexFieldInstances(
							this.currentBusinessObjectInstance, this
									.getBusinessObject());

					this.messages = [];

					this.safeApply();
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.createBusinessObjectInstance = function() {
					this.messages = [];
					this.currentBusinessObjectInstance = {};
					this.newBusinessObjectInstance = this.currentBusinessObjectInstance;

					this.createMissingComplexFieldInstances(
							this.currentBusinessObjectInstance, this
									.getBusinessObject());
					this.searchCollapsed = true;
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.saveBusinessObjectInstance = function() {
					var self = this;

					this.messages = [];

					if (this.currentBusinessObjectInstance[this.businessObjectManagementPanelController.primaryKeyField.id] == null
							|| this.currentBusinessObjectInstance[this.businessObjectManagementPanelController.primaryKeyField.id].length == 0) {
						this.messages = [ "Primary Key "
								+ this.businessObjectManagementPanelController.primaryKeyField.name
								+ " not set." ];

						return;
					}

					if (this.newBusinessObjectInstance) {
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
				BusinessObjectManagementViewController.prototype.clearBusinessObjectInstance = function() {
					this.messages = [];
					this.currentBusinessObjectInstance = null;
					this.newBusinessObjectInstance = null;
					this.searchCollapsed = false;
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.getBusinessObjectInstance = function() {
					return this.currentBusinessObjectInstance;
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.getBusinessObject = function() {
					if (this.businessObjectManagementPanelController.businessObject) {
						return this.businessObjectManagementPanelController.businessObject.businessObject;
					}

					return null;
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.getPrimitiveFieldColumns = function(
						type) {
					var fieldColumns = [ [] ];

					if (!type || !this.getBusinessObject()) {
						return fieldColumns;
					}

					for (var n = 0; n < type.fields.length; ++n) {
						// Skip non primitive fields

						if (type.fields[n].list
								|| this.getBusinessObject().types[type.fields[n].type]) {
							continue;
						}

						// TODO Make 5 configurable

						if (fieldColumns[fieldColumns.length - 1].length == 5) {
							fieldColumns.push([]);
						}

						fieldColumns[fieldColumns.length - 1]
								.push(type.fields[n]);
					}

					return fieldColumns;
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.getPrimitiveFields = function(
						type) {
					var fields = [];

					if (!type || !this.getBusinessObject()) {
						return fields;
					}

					for (var n = 0; n < type.fields.length; ++n) {
						// Skip complex fields

						if (type.fields[n].list
								|| this.getBusinessObject().types[type.fields[n].type]) {
							continue;
						}

						fields.push(type.fields[n]);
					}

					return fields;
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.getComplexFields = function(
						type) {
					var fields = [];

					if (!type || !this.getBusinessObject()) {
						return fields;
					}

					for (var n = 0; n < type.fields.length; ++n) {
						// Skip primitive fields

						if (!type.fields[n].list
								&& !this.getBusinessObject().types[type.fields[n].type]) {
							continue;
						}

						fields.push(type.fields[n]);
					}

					return fields;
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.createMissingComplexFieldInstances = function(
						instance, type) {
					for (var n = 0; n < type.fields.length; ++n) {
						var fieldType = this.getBusinessObject().types[type.fields[n].type];

						// Skip primitive fields

						if (!type.fields[n].list || !fieldType) {
							continue;
						}

						if (!instance[type.fields[n].id]) {
							if (!type.fields[n].list) {
								instance[type.fields[n].id] = {};

								this.createMissingComplexFieldInstances(
										instance[type.fields[n].id], fieldType);
							} else {
								instance[type.fields[n].id] = [];
							}
						}
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
