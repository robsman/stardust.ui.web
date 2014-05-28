/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Yogesh.Manware
 * 
 */
define(
		[ "bpm-reporting/public/js/report/I18NUtils" ],
		function(I18NUtils) {
			return {
				create : function(report, filters, reportingService,
						reportHelper, paremetersDisplay) {
					var controller = new ReportFilterController();
					controller.initialize(report, filters, reportingService,
							reportHelper, paremetersDisplay);
					return controller;
				}
			};

			/**
			 * 
			 */
			function ReportFilterController() {
				/**
				 * 
				 * @param key
				 * @returns
				 */
				ReportFilterController.prototype.getI18N = function(key) {
					return I18NUtils.getProperty(key);
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.resetReport = function(report,
						filters) {
					this.filters = filters;
					this.report = report;
				}
				
				/**
				 * 
				 */
				ReportFilterController.prototype.initialize = function(report,
						filters, reportingService, reportHelper,
						paremetersDisplay) {
					this.constants = {
						ALL_PROCESSES : {
							id : "allProcesses",
							name : this
									.getI18N("reporting.definitionView.additionalFiltering.allprocesses")
						},
						ALL_ACTIVITIES : {
							id : "allActivities",
							name : this
									.getI18N("reporting.definitionView.additionalFiltering.allactivities")
						}
					};

					this.reportingService = reportingService;
					this.reportHelper = reportHelper;
					this.filters = filters;
					this.report = report;
					this.parameterDisplay = paremetersDisplay; 
					
					this.filterSelected = [];
				}

				/**
				 * 
				 */
				ReportFilterController.prototype.toggleFilter = function(
						filter, property) {
					filter.metadata[property] = !filter.metadata[property];
					// this.updateView();
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.selectedProcessChanged = function(
						filter) {
					var self = this;
					if (filter.metadata.selectedProcesses.some(function(id) {
						return self.constants.ALL_PROCESSES.id == id;
					})) {
						filter.metadata.selectedProcesses = [ this.constants.ALL_PROCESSES.id ];
					}
				};

				/**
				 * This function will populte the filter variables by loading
				 * previously saved filters
				 */
				ReportFilterController.prototype.loadFilters = function() {
					if (this.filterSelected.length != this.filters.length) {
						for ( var item in this.filters) {
							this.filterSelected.push({
								index : this.filters.length,
								value : []
							});
						}
					}
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.deleteFilter = function(index) {
					var newFilters = [];

					for (var n = 0; n < this.filters.length; ++n) {
						if (n == index) {
							continue;
						}

						newFilters.push(this.filters[n]);
					}

					this.filters = newFilters;

					// Remove parameters from parameter
					this.removeParametersFromParameterList(index);

				};
				/**
				 * This function will remove parameters from parameter list
				 */
				ReportFilterController.prototype.removeParametersFromParameterList = function(
						index) {
					if (this.filterSelected[index].value != null) {
						var params = this.filterSelected[index].value;
						for ( var param in params) {
							this
									.removeParameter(this.filterSelected[index].value[param].id);
						}
					}
				};

				/**
				 * Reinitializes filter values and operator.
				 */
				ReportFilterController.prototype.onFilterDimensionChange = function(
						index) {
					this.filters[index].value = null;
					this.filters[index].metadata = null;
					this.filters[index].operator = null;

					if (this.filters[index].dimension == 'processName') {
						this.filters[index].metadata = {
							process_filter_auxiliary : true
						};
						this.filters[index].value = [ this.constants.ALL_PROCESSES.id ];
					} else if (this.filters[index].dimension == 'activityName') {
						this.filters[index].metadata = activityFilterTemplate();
						this.filters[index].metadata.selectedProcesses
								.push(this.constants.ALL_PROCESSES.id);
						this.filters[index].value = [ this.constants.ALL_ACTIVITIES.id ];
					} else {
						var dimenison = this
								.getDimension(this.filters[index].dimension);

						if (dimenison && dimenison.metadata
								&& dimenison.metadata.isDescriptor) {
							this.filters[index].metadata = dimenison.metadata;
						}

						if (dimenison
								&& (dimenison.type == this.reportingService.metadata.autocompleteType)) {
							this.filters[index].value = [];
						}

						if (dimenison
								&& (dimenison.type == this.reportingService.metadata.timestampType)) {
							this.filters[index].value = {
								from : "",
								to : "",
								duration : "",
								durationUnit : ""
							};
							if (!this.filters[index].metadata) {
								this.filters[index].metadata = {};
							}
							this.filters[index].metadata.fromTo = true;
						}
					}

					// TODO: Operator only for respective types
					this.filters[index].operator = "equal";

					this.removeParametersFromParameterList(index);
				};

				/**
				 * Adding order parameter to dimension object used in Filtering
				 * for displaying it on UI in specific order
				 */
				ReportFilterController.prototype.getPrimaryObjectEnumByGroup = function() {
					var dimensions = this
							.filterPrimaryObjectEnum(this.report);
					for ( var dimension in dimensions) {
						var group = this.reportHelper.primaryObjectEnumGroup(
								dimensions[dimension].id,
								this.report);
						dimensions[dimension].order = this.reportHelper
								.getDimensionsDisplayOrder(
										dimensions[dimension].id,
										this.report);
					}
					return dimensions;
				};

				/**
				 * 
				 * @param report
				 * @returns
				 */
				ReportFilterController.prototype.getPrimaryObjectEnum = function(
						report) {
					var dimensionsObj = this.reportingService.metadata.objects[report.dataSet.primaryObject].dimensions;
					var enumerators = [];

					for ( var n in dimensionsObj) {
						enumerators.push(dimensionsObj[n]);
					}

					dimensionsObj = this.reportHelper
							.getComputedColumnAsDimensions(report);

					for ( var n in dimensionsObj) {
						enumerators.push(dimensionsObj[n]);
					}

					enumerators.sort(function(object1, object2) {
						return object1.name.localeCompare(object2.name);
					});

					return enumerators;
				};
				/**
				 * 
				 */
				ReportFilterController.prototype.getOperatorsEnum = function(
						dimension) {
					var operators = [];

					if (!dimension) {
						return operators;
					}

					// operators can be data type specific or filter specific
					var dimensionOperator = dimension.operators;
					if (!dimensionOperator && dimension.type
							&& dimension.type.operators) {
						dimensionOperator = dimension.type.operators;
					}

					if (dimensionOperator) {
						for ( var i in dimensionOperator) {
							operators
									.push({
										"id" : dimensionOperator[i],
										"label" : this
												.getI18N("reporting.definitionView.additionalFiltering.operator."
														+ dimensionOperator[i])
									});
						}
					} else {
						// return default list
						operators
								.push({
									"id" : "E",
									"label" : this
											.getI18N("reporting.definitionView.additionalFiltering.operator.E")
								});
						operators
								.push({
									"id" : "LE",
									"label" : this
											.getI18N("reporting.definitionView.additionalFiltering.operator.LE")
								});
						operators
								.push({
									"id" : "GE",
									"label" : this
											.getI18N("reporting.definitionView.additionalFiltering.operator.GE")
								});
						operators
								.push({
									"id" : "NE",
									"label" : this
											.getI18N("reporting.definitionView.additionalFiltering.operator.NE")
								});
					}
					return operators;
				}

				/**
				 * 
				 */
				ReportFilterController.prototype.removeParameter = function(id) {
					delete this.report.parameters[id];
					for (var int = 0; int < this.filters.length; int++) {
						if (id.indexOf(this.filters[int].dimension) != -1) {
							delete this.filters[int].metadata.parameterizable;
						}
					}
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.existsParameter = function(id) {
					return this.report.parameters[id] != null;
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.addParameter = function(id,
						name, type, value, operator) {

					var currentFilter = this.filters;

					for (var int = 0; int < this.filters.length; int++) {
						if (id.indexOf(this.filters[int].dimension) != -1) {
							this.filterSelected[int].value.push({
								id : id,
								name : name,
								type : type,
								value : value,
								operator : operator
							});
							if (this.filters[int].metadata == null) {
								this.filters[int].metadata = {};
							}
							this.filters[int].metadata.parameterizable = true;
						}
					}

					if (id != null && id.length != 0) {
						this.report.parameters[id] = {
							id : id,
							name : name,
							type : type,
							value : value,
							operator : operator
						};
					}
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.toggleToAndDuration = function(
						param) {
					if (this.existsParameter(param)) {
						this.removeParameter(param);
					}
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.toggleFilter = function(
						filter, property) {
					filter.metadata[property] = !filter.metadata[property];
					// this.updateView();
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.selectedProcessChanged = function(
						filter) {
					var self = this;
					if (filter.metadata.selectedProcesses.some(function(id) {
						return self.constants.ALL_PROCESSES.id == id;
					})) {
						filter.metadata.selectedProcesses = [ this.constants.ALL_PROCESSES.id ];
					}
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.getEnumerators2 = function(
						dimension, filter) {
					if (!dimension || !dimension.enumerationType) {
						return null;
					}

					var qualifier = dimension.enumerationType.split(":");

					var enumItems = this.reportingService.getEnumerators2(
							qualifier[0], qualifier[1]);

					var filteredEnumItems = enumItems;

					if (filter
							&& (filter.dimension == "processName" || filter.dimension == "activityName")) {
						self = this;
						// processes
						if ((dimension.id == "processName" || dimension.id == "activityName")) {
							filteredEnumItems = [];
							filteredEnumItems
									.push(this.constants.ALL_PROCESSES);
							for (var i = 0; i < enumItems.length; i++) {
								var process = enumItems[i];
								if (!filter.metadata.process_filter_auxiliary
										|| !process.auxiliary) {
									filteredEnumItems.push(process);
								}
							}
						}

						// activities
						if (dimension.id == "activityName") {
							var selectedProcesses = [];
							self = this;

							if (!filter.metadata.selectedProcesses
									|| filter.metadata.selectedProcesses.length < 1) {
								selectedProcesses = selectedProcesses
										.concat(filteredEnumItems);
							} else if (filter.metadata.selectedProcesses
									.some(function(id) {
										return self.constants.ALL_PROCESSES.id == id;
									})) {
								selectedProcesses = selectedProcesses
										.concat(filteredEnumItems);
							} else {
								filteredEnumItems.forEach(function(item) {
									if (filter.metadata.selectedProcesses
											.some(function(id) {
												return item.id == id;
											})) {
										selectedProcesses.push(item);
									}
								});
							}

							filteredEnumItems = [];
							filteredEnumItems
									.push(this.constants.ALL_ACTIVITIES);

							for (var i = 0; i < selectedProcesses.length; i++) {
								var process = selectedProcesses[i];
								if (process == this.constants.ALL_PROCESSES) {
									continue;
								}
								for (var j = 0; j < process.activities.length; j++) {
									var activity = process.activities[j];
									if (!filter.metadata.activity_filter_auxiliary
											|| !activity.auxiliary) {
										if (!filter.metadata.activity_filter_interactive
												|| !activity.interactive) {
											if (!filter.metadata.activity_filter_nonInteractive
													|| activity.interactive) {
												filteredEnumItems
														.push(activity);
											}
										}
									}
								}
							}

						}
						
						//persist all processes or all activities
						var selectedAll = false;
						for ( var valueInd in filter.value) {
							if (filter.value[valueInd] == self.constants.ALL_PROCESSES.id
									|| filter.value[valueInd] == self.constants.ALL_ACTIVITIES.id) {
								selectedAll = true;
							}
						}

						if (selectedAll) {
							filter.allValues = [];
							for ( var itemInd in filteredEnumItems) {
								var itemId = filteredEnumItems[itemInd].id;
								if (itemId != self.constants.ALL_PROCESSES.id && itemId != self.constants.ALL_ACTIVITIES.id) {
									filter.allValues.push(itemId);
								}
							}
						} else {
							delete filter.allValues;
						}
					}

					return filteredEnumItems;
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.selectionChanged = function(
						dimension, filter) {
					var self = this;
					if (dimension.id == "processName"
							&& filter.value.some(function(id) {
								return self.constants.ALL_PROCESSES.id == id;
							})) {
						filter.value = [ this.constants.ALL_PROCESSES.id ];
					}

					if (dimension.id == "activityName"
							&& filter.value.some(function(id) {
								return self.constants.ALL_ACTIVITIES.id == id;
							})) {
						filter.value = [ this.constants.ALL_ACTIVITIES.id ];
					}

					if (dimension.id == "criticality") {
						filter.metadata = this
								.getCriticalityForName(filter.value);
					}
				};
				/**
				 * 
				 */
				ReportFilterController.prototype.getCriticalityForName = function(
						name) {
					var criticality;
					this.reportingService.preferenceData.criticality
							.forEach(function(item) {
								if (item.name == name) {
									criticality = item;
								}
							});

					return criticality;
				};

				/**
				 * Filters are stored in an array. Retrieves a filter by ID from
				 * that array.
				 */
				ReportFilterController.prototype.getFilterByDimension = function(
						dimension) {
					for (var n = 0; n < this.filters.length; ++n) {
						if (this.filters[n].dimension == dimension) {
							return this.filters[n];
						}
						// TODO: this is always thrown commenting temporarily
						/*
						 * throw "No filter found with dimension " + dimension +
						 * ".";
						 */
					}
				};

				/**
				 * This function will return UI Displayable param name
				 * 
				 */
				ReportFilterController.prototype.getParamDisplayName = function(
						id) {
					var pattern = "Filter for";
					if (id.indexOf(pattern) >= 0) {
						return id.substr(pattern.length, id.length);
					}
					return id;
				};

				/**
				 * Dimensions of type durationType are not filterable and
				 * Groupable so filtering them out.
				 */
				ReportFilterController.prototype.filterPrimaryObjectEnum = function(
						report) {
					var dimensions = this.getPrimaryObjectEnum(report);

					for (var i = dimensions.length - 1; i >= 0; i--) {
						if (this.reportingService.metadata.durationType.id == dimensions[i].type.id) {
							dimensions.splice(i, 1);
						}
					}

					return dimensions;
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.getParameterDefaultValue = function(
						id) {
					if (id.indexOf("firstDimension") >= 0) {
						return this.report.dataSet[id];
					} else if (id.indexOf("firstDimension") >= 0) {
						return this.report.dataSet[id];
					} else if (id.indexOf("filters.") >= 0) {
						var path = id.split(".");

						var dimension = this.getDimension(this
								.getFilterByDimension(path[1]).dimension);
						if (dimension.type.id == this.reportingService.metadata.timestampType.id) {
							if (path[2] === "from") {
								return this.getFilterByDimension(path[1]).value.from;
							} else if (path[2] === "to") {
								return this.getFilterByDimension(path[1]).value.to;
							} else if (path[2] === "duration") {
								return this.getFilterByDimension(path[1]).value.duration;
							} else if (path[2] === "durationUnit") {
								return this.getFilterByDimension(path[1]).value.durationUnit;
							}
							return this.getFilterByDimension(path[1]).value.path[2];
						} else {
							return this.getFilterByDimension(path[1]).value;
						}
						return null;
					}
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.addFilter = function() {
					var index = this.filters.length;

					this.filters.push({
						index : index,
						value : null,

						// TODO: Operator only for respective types

						operator : "equal"
					});

					this.filterSelected.push({
						index : index,
						value : []
					});
				};

				ReportFilterController.prototype.getDimension = function(id) {
					return this.getPrimaryObject().dimensions[id];
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.getPrimaryObject = function() {
					return this.reportingService.metadata.objects[this.report.dataSet.primaryObject];
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.getMetadata = function() {
					return this.reportingService.metadata;
				};

			}
			function activityFilterTemplate() {
				return {
					// Processes
					process_filter_auxiliary : true,
					selectedProcesses : [],

					// Activities
					activity_filter_auxiliary : true,
					activity_filter_interactive : false,
					activity_filter_nonInteractive : true
				};
			}
		});