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
						id : "number",
						name : "Number",
						description : "e.g. 156789"
					}, {
						id : "date",
						name : "Date",
						description : "e.g. 12/12/2014"
					} ];
					this.structures = [ {
						id : "Address",
						name : "Address",
						description : "Address data",
						type : "structure",
						fields : [ {
							name : "street",
							typeClass : "primitive",
							type : "string"
						}, {
							name : "city",
							typeClass : "primitive",
							type : "string"
						}, {
							name : "country",
							typeClass : "structure",
							type : "Country"
						} ]
					}, {
						id : "Country",
						name : "Country",
						description : "List of all countries",
						type : "enumeration",
						fields : [ {
							name : "USA"
						}, {
							name : "Germany"
						} ]
					} ];
					this.fields = [ {
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
					this.relationships = [ {
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

											jQuery("#palette #" + primitive.id)
													.data("primitive",
															primitive);
											jQuery("#palette #" + primitive.id)
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

											jQuery("#palette #" + structure.id)
													.data("structure",
															structure);
											jQuery("#palette #" + structure.id)
													.draggable({
														opacity : 0.7,
														helper : "clone",
														cursorAt : {
															top : 0,
															left : 0
														}
													});
										}

										for (var n = 0; n < self.fields.length; n++) {
											self.decorateField(self.fields[n]);
										}

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
																		name : primitive.name,
																		typeClass : "primitive",
																		type : primitive.id,
																		cardinality : "exactlyOne"
																	};
																} else {
																	field = {
																		name : structure.name,
																		typeClass : "structure",
																		type : structure.id,
																		cardinality : "exactlyOne"
																	};
																}

																self.fields
																		.push(field);
																self
																		.safeApply();

																window
																		.setTimeout(
																				function() {
																					self
																							.decorateField(field);
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

																var structure = jQuery
																		.data(
																				ui.draggable[0],
																				"structure");

																if (structure) {
																	self.relationships
																			.push({
																				otherObject : structure.name,
																				otherRole : structure.name,
																				otherCardinality : "zeroOrMore",
																				thisRole : "bla",
																				thisCardinality : "zeroOrMore"
																			});

																	self
																			.safeApply();
																}
															}
														});
										jQuery("#businessObjectTabs").tabs();
									}, 500);
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

					throw "Cannot find structure " + id;
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.decorateField = function(
						field) {
					var self = this;

					window.setTimeout(function() {
						jQuery("#fieldList #" + field.name)
								.data("field", field);
						jQuery("#fieldList #" + field.name).draggable(
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
					}, 500);
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.removeField = function(
						index) {
					this.fields = this.fields.splice(index, 1);

					this.safeApply();

					var self = this;

					window.setTimeout(function() {
						for (var n = 0; n < self.fields.length; n++) {
							self.decorateField(self.fields[n]);
						}
					}, 500)
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.removeRelationship = function(
						index) {
					this.relationships = this.relationships.splice(index, 1);

					this.safeApply();
				};

				/**
				 * 
				 */
				BusinessObjectModelingViewController.prototype.findFieldEntryAbove = function(
						top, currentField) {
					console.log(top);
					console.log(currentField);

					var fields = [];

					for (var n = 0; n < this.fields.length; n++) {
						var field = this.fields[n];

						if (currentField) {
							if (field.name == currentField.name) {
								continue;
							}

							var fieldEntry = jQuery("#fieldList #" + field.name);

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

					this.fields = fields;

					this.safeApply();

					var self = this;

					window.setTimeout(function() {
						for (var n = 0; n < self.fields.length; n++) {
							self.decorateField(self.fields[n]);
						}
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
