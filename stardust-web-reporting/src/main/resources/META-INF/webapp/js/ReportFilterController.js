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
				 * 
				 */
				ReportFilterController.prototype.deleteFilter = function(filter) {
					if(!filter.dimension){
						this.filters.splice(this.filters.length-1, 1);
					}else{
						this.filters.splice(this.getIndex(filter.dimension), 1);	
					}
				};

				/**
				 * Reinitializes filter values and operator.
				 */
				ReportFilterController.prototype.onFilterDimensionChange = function(
						filter) {
					var index = this.getIndex(filter.dimension);
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
								&& (dimenison.metadata.isDescriptor || dimenison.metadata.isComputedType)) {
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
								to : ""
							};
							if (!this.filters[index].metadata) {
								this.filters[index].metadata = {};
							}
							this.filters[index].metadata.fromTo = true;
						}
					}

					// TODO: Operator only for respective types
					this.filters[index].operator = "equal";
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.getIndex = function(id) {
					for (var int = 0; int < this.filters.length; int++) {
						if (id.indexOf(this.filters[int].dimension) != -1) {
							return int;
						}
					}
				};

				/**
				 * Adding order parameter to dimension object used in Filtering
				 * for displaying it on UI in specific order
				 */
				ReportFilterController.prototype.getDimensions = function() {
					var dimensions = this.reportingService.getCumulatedDimensions(this.report);

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
					for (var int = 0; int < this.filters.length; int++) {
						if (id.indexOf(this.filters[int].dimension) != -1) {
							return this.filters[int].metadata
									&& this.filters[int].metadata.parameterizable;
						}
					}
					return false;
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.addParameter = function(id,
						name, type, value, operator) {
					for (var int = 0; int < this.filters.length; int++) {
						if (this.filters[int].metadata == null) {
							this.filters[int].metadata = {};
						}
						this.filters[int].metadata.parameterizable = true;
					}
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.toggleToAndDuration = function(
						filter) {
					if (filter.metadata.fromTo) {
						delete filter.value.duration;
						delete filter.value.durationUnit;
					} else {
						delete filter.value.to;
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

						// persist all processes or all activities
						var selectedAll = false;
						for ( var valueInd in filter.value) {
							if (filter.value[valueInd] == self.constants.ALL_PROCESSES.id
									|| filter.value[valueInd] == self.constants.ALL_ACTIVITIES.id) {
								selectedAll = true;
							}
						}

						if (selectedAll) {
							filter.uiValue = [];
							for ( var itemInd in filteredEnumItems) {
								var itemId = filteredEnumItems[itemInd].id;
								if (itemId != self.constants.ALL_PROCESSES.id
										&& itemId != self.constants.ALL_ACTIVITIES.id) {
									filter.uiValue.push(itemId);
								}
							}
						} else {
							delete filter.uiValue;
						}
					}else {
						if(filter.dimension != "criticality"){
							delete filter.uiValue;	
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
						filter.uiValue = [filter.metadata.rangeFrom/1000, filter.metadata.rangeTo/1000];
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
					this.filters.push({
						value : null,
					});
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.getDimension = function(id) {
					var dimensions = this.getDimensions();
					for(var i = 0; i < dimensions.length; i++){
						if(id == dimensions[i].id){
							return dimensions[i]
						}
					}
				};

				/**
				 * 
				 */
				ReportFilterController.prototype.getMetadata = function() {
					return this.reportingService.metadata;
				};
				
				/**
             * 
             */
            ReportFilterController.prototype.isCompColNumeric = function(id) {
               var dimensions = this.getDimensions();
               for(var i = 0; i < dimensions.length; i++){
                  if(id == dimensions[i].id){
                     var dimension = dimensions[i];
                     if (dimension.type.id === this.getMetadata().integerType.id || 
                              dimension.type.id === this.getMetadata().decimalType.id ||
                              dimension.type.id === this.getMetadata().countType.id)
                     {
                        return true;
                     }
                  }
               }
               return false;
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