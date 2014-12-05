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
					this.parameters = Utils.getQueryParameters();
					this.relationshipDialog = {};
					this.formColumns = [];
					this.messages = [];

					this.businessObjectManagementPanelController = BusinessObjectManagementPanelController
							.create();

					this.businessObjectManagementPanelController
							.initialize(this);

					this.relationshipPanelController = BusinessObjectManagementPanelController
							.create();

					this.relationshipPanelController.initialize(this);

					var self = this;

					BusinessObjectManagementService
							.instance()
							.getBusinessObjects()
							.done(
									function(businessObjectModels) {
										self.businessObjectModels = businessObjectModels;

										self.refreshBusinessObjects();

										// TODO This code will go

										if (self.parameters.modelOid
												&& self.parameters.businessObjectId) {
											BusinessObjectManagementService
													.instance()
													.getBusinessObject(
															self.parameters.modelOid,
															self.parameters.businessObjectId)
													.done(
															function(
																	businessObject) {
																self.businessObject = businessObject;

																self
																		.initializeForm();

																self
																		.safeApply();
															}).fail(function() {
													});
										} else {
											self.safeApply();
										}
									}).fail(function() {
							});
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.refreshBusinessObjects = function() {
					this.businessObjects = [];

					for (var n = 0; n < this.businessObjectModels.length; ++n) {
						for (var m = 0; m < this.businessObjectModels[n].businessObjects.length; ++m) {
							if (!this.businessObjectModels[n].businessObjects[m].types) {
								this.businessObjectModels[n].businessObjects[m].types = {};
							}

							this.businessObjectModels[n].businessObjects[m].modelOid = this.businessObjectModels[n].oid;
							this.businessObjectModels[n].businessObjects[m].label = this.businessObjectModels[n].name
									+ "/"
									+ this.businessObjectModels[n].businessObjects[m].name;
							this.businessObjects
									.push(this.businessObjectModels[n].businessObjects[m]);
						}
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.changeBusinessObject = function() {
					console.log("Business Object");
					console.log(this.businessObject.modelOid);

					BusinessObjectManagementService.instance()
							.calculateBusinessObjectFields(this.businessObject);
					console.log("Before form");
					console.log(this.businessObject.modelOid);
					this.initializeForm();

					console.log("Changed Business Object");
					console.log(this.businessObject.modelOid);
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.initializeForm = function() {
					this.businessObjectManagementPanelController
							.changeBusinessObject(this.businessObject);

					this.formColumns = [];
					this.foreignKeyFields = {};

					var primitiveFields = this
							.getPrimitiveFields(this.businessObject);
					var complexFields = this
							.getComplexFields(this.businessObject);

					if (this.businessObject.relationships) {
						for (var n = 0; n < primitiveFields.length; ++n) {
							for (var m = 0; m < this.businessObject.relationships.length; ++m) {
								if (this.businessObject.relationships[m].otherForeignKeyField == primitiveFields[n].id) {
									this.foreignKeyFields[this.businessObject.relationships[m].otherForeignKeyField] = primitiveFields[n];

									break;
								}
							}
						}

						for (var n = 0; n < complexFields.length; ++n) {
							for (var m = 0; m < this.businessObject.relationships.length; ++m) {
								// TODO

								if (!this.businessObject.relationships[m].otherForeignKeyField) {
									this.businessObject.relationships[m].otherForeignKeyField = "FundGroupIds";
								}

								if (this.businessObject.relationships[m].otherForeignKeyField == complexFields[n].id) {
									this.foreignKeyFields[this.businessObject.relationships[m].otherForeignKeyField] = complexFields[n];

									break;
								}
							}
						}

						console.log("Foreign Key Fields");
						console.log(this.foreignKeyFields);
					}

					var fieldsPerColumn = Math.ceil(primitiveFields.length / 3);

					for (var n = 0; n < primitiveFields.length; n += fieldsPerColumn) {
						this.formColumns.push({
							min : n,
							max : n + fieldsPerColumn - 1
						});
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.onBusinessObjectInstanceSelectionChange = function() {
					this.currentBusinessObjectInstance = this.businessObjectManagementPanelController.selectedBusinessObjectInstances[0];
					this.messages = [];

					var self = this;

					// Retrieve Process Instances the BOI is used in

					BusinessObjectManagementService
							.instance()
							.getProcessInstances(this.businessObject,
									this.currentBusinessObjectInstance)
							.done(
									function(processInstances) {
										self.processInstances = processInstances;
										self.versionPanelExpanded = true;

										// Prepare BOI for display

										self
												.createMissingComplexFieldInstances(
														self.currentBusinessObjectInstance,
														self
																.getBusinessObject());

										self.safeApply();
									}).fail(function() {
							});
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.createBusinessObjectInstance = function() {
					console.log("Create BO");
					this.messages = [];
					this.currentBusinessObjectInstance = {};
					this.newBusinessObjectInstance = this.currentBusinessObjectInstance;

					this.createMissingComplexFieldInstances(
							this.currentBusinessObjectInstance,
							this.businessObject);
					this.searchCollapsed = true;
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.saveBusinessObjectInstance = function() {
					var self = this;

					this.messages = [];

					if (this.currentBusinessObjectInstance[this.businessObjectManagementPanelController.businessObject.primaryKeyField.id] == null
							|| this.currentBusinessObjectInstance[this.businessObjectManagementPanelController.businessObject.primaryKeyField.id].length == 0) {
						this.messages = [ "Primary Key "
								+ this.businessObjectManagementPanelController.businessObject.primaryKeyField.name
								+ " not set." ];

						return;
					}

					if (this.newBusinessObjectInstance) {
						this
								.cleanUpAngularFields(this.newBusinessObjectInstance);

						BusinessObjectManagementService
								.instance()
								.createBusinessObjectInstance(
										this.businessObject.modelId,
										this.businessObject.id,
										this.currentBusinessObjectInstance[this.businessObjectManagementPanelController.businessObject.primaryKeyField.id],
										this.currentBusinessObjectInstance)
								.done(
										function() {
											self.currentBusinessObjectInstance = null;
											self.newBusinessObjectInstance = null;
											self.searchCollapsed = false;
											self.businessObjectManagementPanelController
													.filterBusinessObjectInstances();
										}).fail();

					} else {
						this
								.cleanUpAngularFields(this.currentBusinessObjectInstance);

						BusinessObjectManagementService
								.instance()
								.updateBusinessObjectInstance(
										this.businessObjectManagementPanelController.businessObject,
										this.currentBusinessObjectInstance)
								.done(
										function() {
											self.currentBusinessObjectInstance = null;
											self.searchCollapsed = false;
											self.businessObjectManagementPanelController
													.filterBusinessObjectInstances();
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
					return this.businessObjectManagementPanelController.businessObject;
				};

				/**
				 * Include arrays of Primitives.
				 */
				BusinessObjectManagementViewController.prototype.getPrimitiveFields = function(
						type) {
					var fields = [];

					if (!type || !this.businessObject) {
						return fields;
					}

					for (var n = 0; type.fields && n < type.fields.length; ++n) {
						// Skip complex fields

						if (type.fields[n].list
								|| (this.businessObject.types
										&& this.businessObject.types[type.fields[n].type] && !this.businessObject.types[type.fields[n].type].values)) {
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

					for (var n = 0; type.fields && n < type.fields.length; ++n) {
						// Skip primitive fields

						if (!type.fields[n].list
								&& (!this.getBusinessObject().types
										|| !this.getBusinessObject().types[type.fields[n].type] || this
										.getBusinessObject().types[type.fields[n].type].values)) {
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
					// Nothing to be done for Enumerations

					if (!type.fields) {
						return;
					}

					for (var n = 0; n < type.fields.length; ++n) {
						var fieldType = this.businessObject.types[type.fields[n].type];

						// Skip primitive fields

						if (!type.fields[n].list && !fieldType) {
							continue;
						}

						if (!instance[type.fields[n].id]) {
							if (!type.fields[n].list) {
								instance[type.fields[n].id] = {};
							} else {
								instance[type.fields[n].id] = [];
							}
						}

						if (fieldType) {
							this.createMissingComplexFieldInstances(
									instance[type.fields[n].id], fieldType);
						}
					}
				};

				/**
				 * Strips all "$" and "$$" fields injected by AngularJS.
				 */
				BusinessObjectManagementViewController.prototype.cleanUpAngularFields = function(
						instance) {
					if (!instance
							|| (typeof instance !== 'object' && !jQuery
									.isArray(instance))) {
						return;
					}

					for ( var key in instance) {
						if (key.indexOf("$") == 0) {
							delete instance[key];

							continue;
						}

						this.cleanUpAngularFields(instance[key]);
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.getAssociatedObjects = function(
						businessObjectInstance, relationship) {
					if (this.currentBusinessObjectInstance[this.relationship.otherForeignKeyField]) {
						return this.currentBusinessObjectInstance[this.relationship.otherForeignKeyField].length;
					}

					return 0;
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.openRelationshipDialog = function(
						relationship) {
					var self = this;
					//TODO : Does this.paramaters works when view opened from BCC perspective
					var modelOid = this.parameters.modelOid ?  this.parameters.modelOid : this.businessObject.modelOid;
					
					console.log("Open with relationship");
					console.log(relationship);

					BusinessObjectManagementService
							.instance()
							.getBusinessObject(modelOid, // TODO
							// possibly
							// not
							// same
							// model
							relationship.otherBusinessObject.id)
							// Remove Hard-coding
							.done(
									function(businessObject) {
										// TODO : modelOid not fetched from
										// server, need a way to set on
										// businessObjects
										if(!businessObject.modelOid){
											businessObject.modelOid = modelOid;
										}
										self.relationshipPanelController
												.changeBusinessObject(businessObject);
										self.relationshipPanelController
												.setRelationshipContext(
														self.currentBusinessObjectInstance,
														relationship);

										self.safeApply();
									}).fail(function() {
							});

					this.relationshipDialog.dialog("open");
					this.relationshipDialog.errors = [];
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.saveRelationshipChanges = function() {
					console.log("Save relationship changed");
					console.log(this);
					console.log(this.relationshipPanelController);

					// TODO Cleanup removed Relationships!!!
					// TODO All code into Panel Controller
					// TODO Remove long dereferentiations by introducing a few
					// local variables

					this.relationshipPanelController.rootBusinessObjectInstance[this.relationshipPanelController.relationship.otherForeignKeyField] = [];

					for (var n = 0; n < this.relationshipPanelController.selectedBusinessObjectInstances.length; ++n) {
						this.relationshipPanelController.rootBusinessObjectInstance[this.relationshipPanelController.relationship.otherForeignKeyField]
								.push(this.relationshipPanelController.selectedBusinessObjectInstances[n][this.relationshipPanelController.businessObject.primaryKeyField.id]);
					}

					for (var n = 0; n < this.relationshipPanelController.selectedBusinessObjectInstances.length; ++n) {
						var found = false;

						if (this.relationshipPanelController.selectedBusinessObjectInstances[n][this.relationshipPanelController.relationship.thisForeignKeyField]) {
							for (var m = 0; m < this.relationshipPanelController.selectedBusinessObjectInstances[n][this.relationshipPanelController.relationship.thisForeignKeyField].length; ++m) {
								if (this.relationshipPanelController.rootBusinessObjectInstance[this.businessObjectManagementPanelController.businessObject.primaryKeyField] == this.relationshipPanelController.selectedBusinessObjectInstances[m][this.relationshipPanelController.relationship.thisForeignKeyField][m]) {
									found = true;

									break;
								}
							}

							if (!found) {
								this.relationshipPanelController.selectedBusinessObjectInstances[n][this.relationshipPanelController.relationship.thisForeignKeyField]
										.push(this.relationshipPanelController.rootBusinessObjectInstance[this.businessObject.primaryKeyField.id]);
							}
						} else {
							this.relationshipPanelController.selectedBusinessObjectInstances[n][this.relationshipPanelController.relationship.thisForeignKeyField] = [ this.relationshipPanelController.rootBusinessObjectInstance[this.businessObject.primaryKeyField.id] ];
						}
					}

					console.log("Resulting object changes");
					console
							.log(this.relationshipPanelController.rootBusinessObjectInstance);
					console
							.log(this.relationshipPanelController.selectedBusinessObjectInstances);

					// Write changes to the server

					var self = this;

					 BusinessObjectManagementService
							.instance()
							.updateBusinessObjectInstance(
									this.businessObjectManagementPanelController.businessObject,
									this.relationshipPanelController.rootBusinessObjectInstance)
							.done(
									function() {
										self
												.updateBusinessObjectInstancesRecursively(
														0,
														self.relationshipPanelController.selectedBusinessObjectInstances)
												.done(
														function() {
															self
																	.closeRelationshipDialog();
														}).fail(function() {
													// TODO Error message
												});
									}).fail(function() {
								// TODO Error message
							});
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.updateBusinessObjectInstancesRecursively = function(
						index, businessObjectInstances) {
					var deferred = jQuery.Deferred();
					var self = this;

					console.log("index = " + index);

					if (index == businessObjectInstances.length) {
						deferred.resolve();
					} else {
						BusinessObjectManagementService
								.instance()
								.updateBusinessObjectInstance(
										this.relationshipPanelController.businessObject,
										businessObjectInstances[index])
								.done(
										function() {
											self
													.updateBusinessObjectInstancesRecursively(
															++index,
															businessObjectInstances)
													.done(function() {
														deferred.resolve();
													}).fail(function() {
														deferred.reject();
													});
										}).fail(function() {
									deferred.reject();
								});
					}

					return deferred.promise();
				};

				/**
				 * 
				 */
				BusinessObjectManagementViewController.prototype.closeRelationshipDialog = function() {
					this.relationshipDialog.dialog("close");
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
