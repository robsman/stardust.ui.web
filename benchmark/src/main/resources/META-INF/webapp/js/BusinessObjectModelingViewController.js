/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "benchmark/js/Utils" ],
		function(Utils) {
			return {
				create : function() {
					var controller = new BusinessObjectModelingViewController();

					return controller;
				}
			};

			/**
			 * 
			 */
			function BusinessObjectModelingViewController() {
				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.initialize = function() {
					this.cardinalities = [ {
						id : "exactlyOne",
						label : "Exactly One"
					}, {
						id : "zeroOrMore",
						label : "Zero or More"
					} ];
					this.primitives = [ {
						id : "string",
						name : "String",
						description : "e.g. \"Joe\""
					}, {
						id : "number",
						name : "Number",
						description : "e.g. 156789"
					}, {
						id : "date",
						name : "Date",
						description : "e.g. 12/12/2014"
					}, {
						id : "decimal",
						name : "Decimal",
						description : "e.g. 134.30"
					}, {
						id : "scientific",
						name : "Scientific",
						description : "e.g. 3.1415"
					} ];
					this.structures = [ {
						id : "Address",
						name : "Address",
						description : "Address data",
						type : "structure",
						fields : [ {
							name : "street",
							typeClass : "primitive",
							type : "string",
							cardinality : "exactlyOne"
						}, {
							name : "city",
							typeClass : "primitive",
							type : "string",
							cardinality : "exactlyOne"
						}, {
							name : "country",
							typeClass : "structure",
							type : "Country",
							cardinality : "exactlyOne"
						} ]
					}, {
						id : "Country",
						name : "Country",
						description : "List of all countries",
						type : "enumeration",
						fields : [ {
							name : "USA",
							typeClass : "primitive",
							type : "string",
							cardinality : "exactlyOne"
						}, {
							name : "Germany",
							typeClass : "primitive",
							type : "string",
							cardinality : "exactlyOne"
						} ]
					} ];
					this.businessObject = {
						id : "FundGroup",
						name : "Fund Group",
						description : "Grouping of funds for processing."
					};
					this.businessObjects = [ {
						id : "Fund",
						name : "Fund",
						description : "Fund data"
					}, {
						id : "Custodian",
						name : "Custodian",
						description : "Custodian for fund assets"
					}, this.businessObject ];

					var self = this;

					this.businessObjects.forEach(function(data) {
						self.structures.push(data);
					});

					this.businessObject.fields = [ {
						name : "firstName",
						typeClass : "primitive",
						type : "string",
						cardinality : "exactlyOne"
					}, {
						name : "address",
						typeClass : "structure",
						type : "Address",
						cardinality : "exactlyOne"
					} ];
					this.businessObject.relationships = [ {
						otherObject : "Fund Group",
						otherRole : "Fund Groups",
						otherCardinality : "zeroOrMore",
						thisCardinality : "zeroOrMore"
					}, {
						otherObject : "Custodian",
						otherRole : "Custodian",
						otherCardinality : "exactlyOne",
						thisCardinality : "exactlyOne"
					} ];

					var self = this;

					window
							.setTimeout(
									function() {
										for (var n = 0; n < self.primitives.length; n++) {
											var primitive = self.primitives[n];

											jQuery(
													"#fieldPalette #"
															+ primitive.id)
													.data("primitive",
															primitive);
											jQuery(
													"#fieldPalette #"
															+ primitive.id)
													.draggable({
														opacity : 0.7,
														helper : "clone",
														cursorAt : {
															top : 0,
															left : 0
														}
													});
										}

										for (var n = 0; n < self.structures.length; n++) {
											var structure = self.structures[n];

											jQuery(
													"#fieldPalette #"
															+ structure.id)
													.data("structure",
															structure);
											jQuery(
													"#fieldPalette #"
															+ structure.id)
													.draggable({
														opacity : 0.7,
														helper : "clone",
														cursorAt : {
															top : 0,
															left : 0
														}
													});
										}

										for (var n = 0; n < self.businessObjects.length; n++) {
											var businessObject = self.businessObjects[n];

											jQuery(
													"#businessObjectPalette #"
															+ businessObject.id)
													.data("businessObject",
															businessObject);
											jQuery(
													"#businessObjectPalette #"
															+ businessObject.id)
													.draggable({
														opacity : 0.7,
														helper : "clone",
														cursorAt : {
															top : 0,
															left : 0
														}
													});
										}

										self.decorateFields();

										jQuery("#fieldList")
												.droppable(
														{
															greedy : true,
															accept : ".paletteEntry",
															drop : function(
																	event, ui) {
																var primitive = jQuery
																		.data(
																				ui.draggable[0],
																				"primitive");
																var structure = jQuery
																		.data(
																				ui.draggable[0],
																				"structure");
																var field;

																if (primitive) {
																	field = {
																		name : self
																				.getNewFieldName(primitive.name),
																		typeClass : "primitive",
																		type : primitive.id,
																		cardinality : "exactlyOne"
																	};
																} else {
																	field = {
																		name : self
																				.getNewFieldName(structure.name),
																		typeClass : "structure",
																		type : structure.id,
																		cardinality : "exactlyOne"
																	};
																}

																self.businessObject.fields
																		.push(field);
																self
																		.safeApply();

																window
																		.setTimeout(
																				function() {
																					self
																							.decorateFields(field);

																					jQuery(
																							"#fieldList #field"
																									+ (self.businessObject.fields.length - 1))
																							.find(
																									"input")
																							.select();
																				},
																				500)
															}
														});
										jQuery("#relationshipList")
												.droppable(
														{
															greedy : true,
															drop : function(
																	event, ui) {
																console
																		.log("Dropping");

																var businessObject = jQuery
																		.data(
																				ui.draggable[0],
																				"businessObject");

																if (structure) {
																	self.businessObject.relationships
																			.push({
																				otherObject : businessObject.name,
																				otherRole : businessObject.name,
																				otherCardinality : "zeroOrMore",
																				thisRole : self.businessObject.name,
																				thisCardinality : "zeroOrMore"
																			});

																	self
																			.safeApply();
																}
															}
														});
										jQuery("#businessObjectTabs").tabs();
										jQuery("body").css("visibility",
												"visible")
									}, 700);
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.getPrimitive = function(
						id) {
					for (var n = 0; n < this.primitives.length; n++) {
						if (this.primitives[n].id == id) {
							return this.primitives[n];
						}
					}
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.getStructure = function(
						id) {
					for (var n = 0; n < this.structures.length; n++) {
						if (this.structures[n].id == id) {
							return this.structures[n];
						}
					}
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.getTypeName = function(
						field) {
					if (field.typeClass == "primitive") {
						return this.getPrimitive(field.type).name;
					} else {
						return this.getStructure(field.type).name;
					}
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.getNewFieldName = function(
						name, index) {
					var test = name;

					if (index) {
						test += " ";
						test += index;
					} else {
						index = 0;
					}

					for (var n = 0; n < this.businessObject.fields.length; n++) {
						if (this.businessObject.fields[n].name == test) {
							++index;

							return this.getNewFieldName(name, index);
						}
					}

					return test;
				}

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.decorateFields = function() {
					for (var n = 0; n < this.businessObject.fields.length; n++) {
						var field = this.businessObject.fields[n];
						var self = this;

						jQuery("#fieldList #field" + n).data("field", field);
						jQuery("#fieldList #field" + n).draggable(
								{
									opacity : 0.7,
									helper : "clone",
									axis : "y",
									stop : function(event, ui) {
										self.findFieldEntryAbove(
												ui.position.top, jQuery.data(
														this, "field"));
									}
								});
					}
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.removeField = function(
						index) {
					this.businessObject.fields.splice(index, 1);

					this.safeApply();

					var self = this;

					window.setTimeout(function() {
						self.decorateFields();
					}, 500)
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.removeRelationship = function(
						index) {
					this.businessObject.relationships.splice(index, 1);
					this.safeApply();
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.findFieldEntryAbove = function(
						top, currentField) {
					var fields = [];

					for (var n = 0; n < this.businessObject.fields.length; n++) {
						var field = this.businessObject.fields[n];

						if (currentField) {
							if (field.name == currentField.name) {
								continue;
							}

							var fieldEntry = jQuery("#fieldList #field" + n);

							if (fieldEntry.position().top + fieldEntry.height() > top) {
								fields.push(currentField);

								currentField = null;
							}
						}

						fields.push(field);
					}

					// Field is below all others

					if (currentField) {
						fields.push(currentField);
					}

					this.businessObject.fields = fields;

					this.safeApply();

					var self = this;

					window.setTimeout(function() {
						self.decorateFields();
					}, 500)
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.safeApply = function(
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
