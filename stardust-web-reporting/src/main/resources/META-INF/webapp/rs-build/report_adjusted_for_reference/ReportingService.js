/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		["../report/I18NUtils"],
		function(I18NUtils) {
			return {
				instance : function() {
					if (!window.top.reportingService) {
						window.top.reportingService = new ReportingService();
					}

					return window.top.reportingService;
				}
			};

			/**
			 * Singleton to access server data. Communication is stateless, but
			 * loaded reports are kept in a cache.
			 */
			function ReportingService() {
				
				ReportingService.prototype.getI18N = function(key) {
					return I18NUtils.getProperty(key);
				};
				
				this.mode = "server";
				this.metadata = {};
				
				this.preferenceData = {};

				this.metadata.layoutSubTypes = {
						chart : {
							id : "chart",
							name : this.getI18N("reporting.definitionView.layout.chart.label"),
						},
						table : {
							id : "table",
							name : this.getI18N("reporting.definitionView.layout.table.label"),
						}
				};
				
				this.metadata.cumulantsDisplay = {
						rows : {
							id : "rows",
							name : this.getI18N("reporting.definitionView.layout.table.cumulant.row"),
						},
						columns : {
							id : "columns",
							name : this.getI18N("reporting.definitionView.layout.table.cumulant.column"),
						}
				};
				
				this.metadata.cumulants = {
						count : {
							id : "count",
							name : this.getI18N("reporting.definitionView.layout.table.cumulant.count"),
						},
						average : {
							id : "average",
							name : this.getI18N("reporting.definitionView.layout.table.cumulant.average"),
						},
						stdDeviation : {
							id : "stdDeviation",
							name : this.getI18N("reporting.definitionView.layout.table.cumulant.stdDeviation"),
						},
						minimum : {
							id : "minimum",
							name : this.getI18N("reporting.definitionView.layout.table.cumulant.minimum"),
						},
						maximum : {
							id : "maximum",
							name : this.getI18N("reporting.definitionView.layout.table.cumulant.maximum"),
						}
				};
				
				this.metadata.recordSetAggregationFunctions  = {
	                  average : {
	                     id : "average",
	                     name : this.getI18N("reporting.definitionView.layout.table.recordSet.aggregation.average"),
	                  },
	                  maximum : {
                        id : "maximum",
                        name : this.getI18N("reporting.definitionView.layout.table.recordSet.aggregation.maximum"),
                     },
                     minimum : {
                        id : "minimum",
                        name : this.getI18N("reporting.definitionView.layout.table.recordSet.aggregation.minimum"),
                     },
	                  stdDeviation : {
	                     id : "stdDeviation",
	                     name : this.getI18N("reporting.definitionView.layout.table.recordSet.aggregation.stdDeviation"),
	                  }
	            };
				
				this.metadata.chartTypes = {
					xyPlot : {
						id : "xyPlot",
						name : this.getI18N("reporting.definitionView.layout.subType.xyPlot.label"),
						hasDefaultSeries : true,
						hasAxes : true
					},
					candlestickChart : {
						id : "candlestickChart",
						name : this.getI18N("reporting.definitionView.layout.subType.candlestickChart.label"),
						hasDefaultSeries : true,
						hasAxes : true
					},
					pieChart : {
						id : "pieChart",
						name : this.getI18N("reporting.definitionView.layout.subType.pieChart.label"),
						hasDefaultSeries : false,
						hasAxes : false
					},
					barChart : {
						id : "barChart",
						name : this.getI18N("reporting.definitionView.layout.subType.barChart.label"),
						hasDefaultSeries : true,
						hasAxes : true
					},
					bubbleChart : {
						id : "bubbleChart",
						name : this.getI18N("reporting.definitionView.layout.subType.bubbleChart.label"),
						hasDefaultSeries : false,
						hasAxes : true
					},
					donutChart : {
						id : "donutChart",
						name : this.getI18N("reporting.definitionView.layout.subType.donutChart.label"),
						hasDefaultSeries : false,
						hasAxes : false
					}
				};

				this.metadata.stringType = {
					id : "stringType",
					name : this.getI18N("reporting.definitionView.metadata.stringType.label"),
					operators : ["E", "NE", "I", "NI", "L"],
				};
				this.metadata.integerType = {
					id : "integerType",
					name : this.getI18N("reporting.definitionView.metadata.integerType.label"),
					operators : ["E", "LE", "GE", "NE", "I", "NI"]
				};
				this.metadata.decimalType = {
					id : "decimalType",
					name : this.getI18N("reporting.definitionView.metadata.decimalType.label"),
					operators : ["E", "LE", "GE", "NE", "I", "NI"]
				};
				this.metadata.booleanType = {
					id : "booleanType",
					name : this.getI18N("reporting.definitionView.metadata.booleanType.label")
				};
				this.metadata.countType = {
					id : "countType",
					name : this.getI18N("reporting.definitionView.metadata.countType.label")
				};
				this.metadata.timestampType = {
					id : "timestampType",
					name : this.getI18N("reporting.definitionView.metadata.timestampType.label")
				};
				this.metadata.durationType = {
					id : "durationType",
					name : this.getI18N("reporting.definitionView.metadata.durationType.label")
				};
				this.metadata.enumerationType = {
					id : "enumerationType",
					name : this.getI18N("reporting.definitionView.metadata.enumerationType.label")
				};
				this.metadata.autocompleteType = {
					id : "autocompleteType",
					name : this.getI18N("reporting.definitionView.metadata.autocompleteType.label")
				};
				
				this.metadata.objects = {
					processInstance : {
						id : "processInstance",
						name : this.getI18N("reporting.definitionView.processInstances.label"),
						supportsDescriptors : true,
						facts : {
							count : {
								id : "count",
								name : this.getI18N("reporting.definitionView.count"),
								type : this.metadata.countType,
							},
							processInstanceDuration : {
                        id : "processInstanceDuration",
                        name : this.getI18N("reporting.definitionView.processInstanceDuration"),
                        type : this.metadata.durationType
                     },
                     rootProcessInstanceDuration : {
                        id : "rootProcessInstanceDuration",
                        name : this.getI18N("reporting.definitionView.rootProcessInstanceDuration"),
                        type : this.metadata.durationType
                     }
						},
						dimensions : {
						   processInstanceStartTimestamp : {
								id : "processInstanceStartTimestamp",
								name : this.getI18N("reporting.definitionView.additionalFiltering.processInstanceStartTimestamp"),
								type : this.metadata.timestampType
							},
							rootProcessInstanceStartTimestamp : {
                        id : "rootProcessInstanceStartTimestamp",
                        name : this.getI18N("reporting.definitionView.additionalFiltering.rootProcessInstanceStartTimestamp"),
                        type : this.metadata.timestampType
                     },
							terminationTimestamp : {
								id : "terminationTimestamp",
								name : this.getI18N("reporting.definitionView.additionalFiltering.timestamp.termination"),
								type : this.metadata.timestampType
							},
                     processInstanceDuration : {
                        id : "processInstanceDuration",
                        name : this.getI18N("reporting.definitionView.processInstanceDuration"),
                        type : this.metadata.durationType
                     },
                     rootProcessInstanceDuration : {
                        id : "rootProcessInstanceDuration",
                        name : this.getI18N("reporting.definitionView.rootProcessInstanceDuration"),
                        type : this.metadata.durationType
                     },
					processOID : {
                        id : "processOID",
                        name : this.getI18N("reporting.definitionView.additionalFiltering.processOID"),
                        type : this.metadata.integerType
                     },
                     /*processID : {
                        id : "processID",
                        name : this.getI18N("reporting.definitionView.additionalFiltering.processID"),
                        type : this.metadata.integerType
                     },*/
							processName : {
								id : "processName",
								name : this.getI18N("reporting.definitionView.additionalFiltering.processName"),
								type : this.metadata.enumerationType,
								enumerationType : "modelData:processDefinitions:name"
							},
							startingUserName : {
								id : "startingUserName",
								name : this.getI18N("reporting.definitionView.additionalFiltering.startingUserName"),
								type : this.metadata.autocompleteType,
								service : "userService" //TODO rest end point
							},
							state : {
								id : "state",
								name : this.getI18N("reporting.definitionView.additionalFiltering.processState"),
								type : this.metadata.enumerationType,
								enumerationType : "staticData:processStates:name"
							},
							priority : {
								id : "priority",
								name : this.getI18N("reporting.definitionView.additionalFiltering.priority"),
								type : this.metadata.enumerationType,
								display : "singleSelect",
								enumerationType : "staticData:priorityLevel:name",
								operators : ["E", "LE", "GE", "NE"]
							}
						}
					},
					activityInstance : {
						id : "activityInstance",
						name : this.getI18N("reporting.definitionView.activityInstances.label"),
						supportsDescriptors : true,
						facts : {
							count : {
								id : "count",
								name : this.getI18N("reporting.definitionView.count"),
								type : this.metadata.countType,
								cumulated : true
							},
							activityInstanceDuration : {
								id : "activityInstanceDuration",
								name : this.getI18N("reporting.definitionView.activityInstanceDuration"),
								type : this.metadata.durationType,
								cumulated : true
							},
                     processInstanceDuration : {
                        id : "processInstanceDuration",
                        name : this.getI18N("reporting.definitionView.processInstanceDuration"),
                        type : this.metadata.durationType
                     },
                     rootProcessInstanceDuration : {
                        id : "rootProcessInstanceDuration",
                        name : this.getI18N("reporting.definitionView.rootProcessInstanceDuration"),
                        type : this.metadata.durationType
                     }
						},
						dimensions : {
							startTimestamp : {
								id : "startTimestamp",
								name : this.getI18N("reporting.definitionView.additionalFiltering.activityStartTimestamp"),
								type : this.metadata.timestampType
							},
							processInstanceStartTimestamp : {
                        id : "processInstanceStartTimestamp",
                        name : this.getI18N("reporting.definitionView.additionalFiltering.processInstanceStartTimestamp"),
                        type : this.metadata.timestampType
                     },
                     rootProcessInstanceStartTimestamp : {
                        id : "rootProcessInstanceStartTimestamp",
                        name : this.getI18N("reporting.definitionView.additionalFiltering.rootProcessInstanceStartTimestamp"),
                        type : this.metadata.timestampType
                     },
                     activityInstanceDuration : {
                        id : "activityInstanceDuration",
                        name : this.getI18N("reporting.definitionView.activityInstanceDuration"),
                        type : this.metadata.durationType,
                        cumulated : true
                     },
                     processInstanceDuration : {
                        id : "processInstanceDuration",
                        name : this.getI18N("reporting.definitionView.processInstanceDuration"),
                        type : this.metadata.durationType
                     },
                     rootProcessInstanceDuration : {
                        id : "rootProcessInstanceDuration",
                        name : this.getI18N("reporting.definitionView.rootProcessInstanceDuration"),
                        type : this.metadata.durationType
                     },
							lastModificationTimestamp : {
								id : "lastModificationTimestamp",
								name : this.getI18N("reporting.definitionView.additionalFiltering.timestamp.last"),
								type : this.metadata.timestampType
							},
                     processOID : {
                        id : "processOID",
                        name : this.getI18N("reporting.definitionView.additionalFiltering.processOID"),
                        type : this.metadata.integerType
                     },
                     /*processID : {
                        id : "processID",
                        name : this.getI18N("reporting.definitionView.additionalFiltering.processID"),
                        type : this.metadata.integerType
                     },*/
                     activityOID : {
                        id : "activityOID",
                        name : this.getI18N("reporting.definitionView.additionalFiltering.activityOID"),
                        type : this.metadata.integerType
                     },
                     /*activityID : {
                        id : "activityID",
                        name : this.getI18N("reporting.definitionView.additionalFiltering.activityID"),
                        type : this.metadata.integerType
                     },*/                     
							activityName : {
								id : "activityName",
								name : this.getI18N("reporting.definitionView.additionalFiltering.activityName"),
								type : this.metadata.enumerationType,
								enumerationType : "modelData:processDefinitions:name"
							},
							processName : {
								id : "processName",
								name : this.getI18N("reporting.definitionView.additionalFiltering.processName"),
								type : this.metadata.enumerationType,
								enumerationType : "modelData:processDefinitions:name"
							},
							userPerformerName : {
								id : "userPerformerName",
								name : this.getI18N("reporting.definitionView.additionalFiltering.userPerformer"),
								type : this.metadata.autocompleteType,
								service : "userService"
							},
							participantPerformerName : {
								id : "participantPerformerName",
								name : this.getI18N("reporting.definitionView.additionalFiltering.performer"),
								type : this.metadata.enumerationType,
								enumerationType : "modelData:participants:name"
							},
							state : {
								id : "state",
								name : this.getI18N("reporting.definitionView.additionalFiltering.activityState"),
								type : this.metadata.enumerationType,
								enumerationType : "staticData:activityStates:name"
							},
							criticality : {
								id : "criticality",
								name : this.getI18N("reporting.definitionView.additionalFiltering.criticality"),
								type : this.metadata.enumerationType,
								display : "singleSelect",
								enumerationType : "preferenceData:criticality:label",
								operators : ["E", "LE", "GE", "NE"]
							}
						}
					}/*,
					role : {
						id : "role",
						name : "Roles",
						supportsDescriptors : false,
						facts : {
							userCountInRole : {
								id : "userCountInRole",
								name : "User Count in Role",
								type : this.metadata.countType
							}
						},
						dimensions : {
							id : {
								id : "id",
								name : "Role ID",
								type : this.metadata.enumerationType,
								enumerationType : "modelData:participants:name"
							},
							name : {
								id : "name",
								name : "Role Name",
								type : this.metadata.enumerationType,
								enumerationType : "modelData:participants:name"
							}
						}
					}*/
				};

				this.staticData = {
					processStates : {
						alive : {
							id : "Alive", 
							name : this.getI18N("reporting.definitionView.additionalFiltering.processState.alive"),
						},
						aborted : {
							id : "Aborted",
							name : this.getI18N("reporting.definitionView.additionalFiltering.processState.aborted")
						},
						completed : {
							id : "Completed",
							name : this.getI18N("reporting.definitionView.additionalFiltering.processState.completed")
						},
						interrupted : {
							id : "Interrupted",
							name : this.getI18N("reporting.definitionView.additionalFiltering.processState.interrupted")
						}
					},
					activityStates : {
						alive : {
							id : "Alive",
							name : this.getI18N("reporting.definitionView.additionalFiltering.activityState.alive")
						},
						suspended : {
							id : "Suspended",
							name : this.getI18N("reporting.definitionView.additionalFiltering.activityState.suspended")
						},
						hibernated : {
							id : "Hibernated", 
							name : this.getI18N("reporting.definitionView.additionalFiltering.activityState.hibernated")
						},
						completed : {
							id : "Completed",
							name : this.getI18N("reporting.definitionView.additionalFiltering.activityState.completed")
						},
						aborted : {
							id : "Aborted",
							name : this.getI18N("reporting.definitionView.additionalFiltering.activityState.aborted")
						},
						aborting : {
							id : "Aborting",
							name : this.getI18N("reporting.definitionView.additionalFiltering.activityState.aborting")
						},
						interrupted : {
							id : "Interrupted",
							name : this.getI18N("reporting.definitionView.additionalFiltering.activityState.interrupted")
						}						
					},
					priorityLevel : {
						low : {
							id : "-1",
							name : this.getI18N("reporting.definitionView.additionalFiltering.priority.low")
						},
						medium : {
							id : "0",
							name : this.getI18N("reporting.definitionView.additionalFiltering.priority.medium")
						},
						high : {
							id : "1",
							name : this.getI18N("reporting.definitionView.additionalFiltering.priority.high")
						}
					}
				};

				this.userData = {
					users : {
						mayIkenn : {
							name : "May Ikenn"
						},
						rettChinaski : {
							name : "Rett Chinaski"
						},
						adamBrooke : {
							name : "Adam Brooke"
						},
						leoMangard : {
							name : "Leo Mangard"
						},
						margieSparks : {
							name : "Margie Sparks"
						},
						hendrikDeloitee : {
							name : "Hendrik Deloitte"
						}
					}
				};

				// Cache for all the report folder structure

				this.rootFolder = {};

				// Cache for all loaded report definitions

				this.loadedReportDefinitions = {};
				
				this.clientDateFormat = "mm/dd/yy";
				
				this.serverDateFormat = "yy/mm/dd";

				/**
				 * 
				 */
				ReportingService.prototype.getPrimaryObject = function(
						primaryObject) {
					return this.metadata.objects[primaryObject];
				};

				/**
				 * 
				 */
				ReportingService.prototype.getDimension = function(
						primaryObject, dimension) {
					return this.getPrimaryObject(primaryObject).dimensions[dimension];
				};

				/**
				 * 
				 */
				ReportingService.prototype.getEnumeratorsForDimension = function(
						primaryObject, dimension) {
					if (this.getDimension(primaryObject, dimension).enumerationType) {
						var qualifier = this.getDimension(primaryObject,
								dimension).enumerationType.split(":");

						return this.getEnumerators(qualifier[0], qualifier[1],
								qualifier[2]);
					}

					return [];
				};

				/**
				 * 
				 */
				ReportingService.prototype.getEnumerators = function(type,
						scope, property) {
					var enumerators = [];

					for ( var n in this[type][scope]) {
						enumerators.push(this[type][scope][n][property]);
					}

					return enumerators;
				};

				ReportingService.prototype.getEnumerators2 = function(type,
						scope) {
					var enumerators = [];

					for ( var n in this[type][scope]) {
						enumerators.push(this[type][scope][n]);
					}	

					return enumerators;
				};
				
				/**
				 * 
				 */
				ReportingService.prototype.refreshModelData = function() {
					var deferred = jQuery.Deferred();

					if(! (typeof model_data === 'undefined')){//email based report viewer
						this.modelData = model_data;
						this.addDescriptorData();
						deferred.resolve();
					}else{
						if (this.mode === "test") {
							// Mock data simulating server return values

							this.modelData = {
								processDefinitions : {
									newAccountOpening : {
										id : "newAccountOpening",
										name : "New Account Opening"
									},
									complianceCheck : {
										id : "complianceCheck",
										name : "Compliance Check"
									},
									payment : {
										id : "payment",
										name : "Payment"
									}
								},
								participants : {
									accountManager : {
										id : "accountManager",
										name : "Account Manager"
									},
									complianceOfficer : {
										id : "complianceOfficer",
										name : "Compliance Officer"
									},
									mailroomClerk : {
										id : "mailroomClerk",
										name : "Mailroom Clerk"
									},
									branchManager : {
										id : "branchManager",
										name : "Branch Manager"
									}
								}
							};

							deferred.resolve();
						} else {
							var self = this;

							jQuery
									.ajax(
											{
												type : "GET",
												async: false,
												beforeSend : function(request) {
													request
															.setRequestHeader(
																	"Authentication",
																	self
																			.getBasicAuthenticationHeader());
												},
												url : self.getRootUrl()
														+ "/services/rest/bpm-reporting/model-data",
												contentType : "application/json"
											}).done(function(data) {
										self.modelData = data;

										self.addDescriptorData();

										deferred.resolve();
									}).fail(function() {
										deferred.reject();
									});

						}
					}

					return deferred.promise();
				};
				
				/**
				 * 
				 */
				ReportingService.prototype.getModelParticipants = function() {
					var modelParticipantsObj = this.modelData.participants;
					var modelParticipants = {};
					for (var n in modelParticipantsObj) {
						modelParticipants[modelParticipantsObj[n].id] = modelParticipantsObj[n].name; 
					}
					return modelParticipants;
				}

				/**
				 * 
				 */
				ReportingService.prototype.refreshPreferenceData = function() {
					var deferred = jQuery.Deferred();
					
					if(! (typeof preference_data === 'undefined')){//email based report viewer
						this.preferenceData = preference_data;
						deferred.resolve();
					}else{
						var self = this;
						jQuery
							.ajax(
									{
										type : "GET",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl()
												+ "/services/rest/bpm-reporting/preference-data",
										contentType : "application/json"
									}).done(function(data) {
								self.preferenceData = data;
								deferred.resolve();
							}).fail(function() {
								deferred.reject();
							});
						this.getDateFormats();
					}

					return deferred.promise();
				};
				
				/**
				 * 
				 */
				ReportingService.prototype.downloadReportDefinition = function(reportPath) {
					var deferred = jQuery.Deferred();
					window.location = this.getRootUrl() 
						+ "/services/rest/bpm-reporting/report-definition" + "/download" + reportPath;
					deferred.resolve();
					
					return deferred.promise();
				};
				
				
				ReportingService.prototype.search = function(serviceName, searchValue) {
					var deferred = jQuery.Deferred();
						var self = this;
						jQuery
							.ajax(
									{
										type : "GET",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl()
												+ "/services/rest/bpm-reporting/search/"+ serviceName + "/" + searchValue,
										contentType : "application/json"
									}).done(function(data) {
								deferred.resolve(data);
							}).fail(function() {
								deferred.reject();
							});

					return deferred.promise();
				};
				
				
				
				/**
				 * Adds Process Model Descriptors to Reporting Metadata.
				 */
				ReportingService.prototype.addDescriptorData = function() {
					for ( var descriptorId in this.modelData.descriptors) {
						var descriptor = this.modelData.descriptors[descriptorId];
						var type = this.metadata[descriptor.type];

						for ( var objectId in this.metadata.objects) {
							var object = this.metadata.objects[objectId];

							if (object.supportsDescriptors) {
								if (type == this.metadata.integerType) {
									object.facts[descriptor.id] = {
										id : descriptor.id,
										name : descriptor.name,
										type : type,
										metadata : descriptor.metadata
									};
								}

								object.dimensions[descriptor.id] = {
									id : descriptor.id,
									name : descriptor.name,
									type : type,
									metadata : descriptor.metadata
								};
							}
						}
					}
				};

				/**
				 * 
				 */
				ReportingService.prototype.loadReportDefinitionsFolderStructure = function() {
					var deferred = jQuery.Deferred();
					var self = this;

					jQuery
							.ajax(
									{
										type : "GET",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl()
												+ "/services/rest/bpm-reporting/report-definitions",
									}).done(function(rootFolder) {
								self.rootFolder = rootFolder;
								deferred.resolve();
							}).fail(function() {
								deferred.reject();
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				ReportingService.prototype.retrieveData = function(report, parameters) {
					
					revertUIAdjustment(report);
					
					var deferred = jQuery.Deferred();
					if(! (typeof report_data === 'undefined')){//email based report viewer
						deferred.resolve(angular.copy(report_data));
					}else{
						if (this.mode === "test") {
							if (this.metadata.objects[report.data.primaryObject].id === "processInstance") {
								deferred.resolve(this
										.retrieveProcessInstanceData(report));
							} else if (this.metadata.objects[report.data.primaryObject].id === "activityInstance") {
								deferred.resolve(this
										.retrieveActivityInstanceData(report));
							} else if (this.metadata.objects[report.data.primaryObject].id === "role") {
								deferred.resolve(this.retrieveRoleData(report));
							}
						} else {
							if(report.layout.table && report.layout.table.preview && report.layout.type == "simpleReport"){
									deferred.resolve(this.getTestSimpleReportData(report));
							}else{
								var self = this;
	
								console.debug("Report Definition");
								console.debug(report);
								
								//convert parameters
								var parametersString = convertToParametersString(parameters);
								
								jQuery
										.ajax(
												{
													type : "POST",
													async: false,
													beforeSend : function(request) {
														request
																.setRequestHeader(
																		"Authentication",
																		self
																				.getBasicAuthenticationHeader());
													},
													url : self.getRootUrl()
															+ "/services/rest/bpm-reporting/report-data?" + parametersString,
													contentType : "application/json",
													data : JSON.stringify(report)
												}).done(function(data) {
											deferred.resolve(data);
										}).fail(function() {
											deferred.reject([]);
										});
							}
						}
					}	
					
					applyUIAdjustment(report);
					
					return deferred.promise();
				};

				/**
				 * 
				 */
				ReportingService.prototype.retrieveDataForReportPath = function(path, parameters) {
					var deferred = jQuery.Deferred();

					var self = this;

					console.debug("Report Definition");
					
					//convert parameters
 					var parametersString = convertToParametersString(parameters);
					
					if (parametersString && parametersString != "") {
						parametersString = "reportPath=" + path + "&" + parametersString;
					} else {
						parametersString = "reportPath=" + path;
					}
					
					jQuery
							.ajax(
									{
										type : "GET",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl()
												+ "/services/rest/bpm-reporting/report-data?" + parametersString,
										contentType : "application/json",
									}).done(function(data) {
								deferred.resolve(data);
							}).fail(function() {
								deferred.reject([]);
							});
					
					return deferred.promise();
				};
				
				
				/**
				 * 
				 */
				ReportingService.prototype.retrieveProcessInstanceData = function(
						report) {
					if (report.type === "table") {
						var rows = [];

						for ( var n = 0; n < 100; ++n) {
							rows.push({
								startTimestamp : this
										.formatDateTime(new Date()),
								processName : this.processNames[n
										% this.processNames.length],
								startingUserName : this.users[n
										% this.users.length]
							});
						}

						return rows;
					}

					var seriesArray = [];
					var groupCount = 1;

					if (report.data.groupBy != -1) {
						groupCount = 3;
					}

					for ( var m = 0; m < groupCount; ++m) {
						var series = [];

						for ( var n = 0; n < this.roles.length; ++n) {
							series.push([ this.roles[n],
									30 + Math.random() * 11 ]);
						}

						seriesArray.push(series);
					}

					return seriesArray;
				};

				/**
				 * 
				 */
				ReportingService.prototype.retrieveActivityInstanceData = function(
						report) {
					if (report.type === "table") {
						var rows = [];

						for ( var n = 0; n < 100; ++n) {
							rows.push({
								startTimestamp : this
										.formatDateTime(new Date()),
								activityName : this.activityNames[n
										% this.activityNames.length],
								processName : this.processNames[n
										% this.processNames.length],
								userPerformerName : this.users[n
										% this.users.length],
								participantPerformerName : this.roles[n
										% this.roles.length]
							});
						}

						return rows;
					}

					var seriesArray = [];
					var groupCount = 1;

					if (report.data.groupBy != -1) {
						groupCount = 3;
					}

					for ( var m = 0; m < groupCount; ++m) {
						var series = [];
						var n;

						if (report.data.firstDimension === "startTimestamp") {
							var startDate = new Date();

							startDate.setTime(startDate.getTime() - 1000 * 60
									* 60 * 24 * 100);

							for (n = 0; n < 100; ++n) {
								var date = new Date();

								date.setTime(startDate.getTime() + 1000 * 60
										* 60 * 24 * n);

								var value = 30 + Math.random() * 5;
								var high = value + Math.random() * 2;
								var low = value - Math.random() * 2;
								var average = low + (high - low)
										* Math.random();

								series
										.push([ date, value, high, low, average ]);
							}
						} else if (report.data.firstDimension === "userPerformerName") {
							for (n = 0; n < this.users.length; ++n) {
								series.push([ this.users[n],
										30 + Math.random() * 11 ]);
							}
						} else {
							for (n = 0; n < this.roles.length; ++n) {
								series.push([ this.roles[n],
										30 + Math.random() * 11 ]);
							}
						}

						seriesArray.push(series);
					}

					return seriesArray;
				};

				/**
				 * 
				 */
				ReportingService.prototype.retrieveRoleData = function(report) {
					var series = [];

					for ( var n = 0; n < this.roles.length; ++n) {
						series.push([ this.roles[n],
								Math.floor(Math.random() * 11) ]);
					}

					return [ series ];
				};

				/**
				 * 
				 */
				ReportingService.prototype.saveReportDefinition = function(
						report) {
					var deferred = jQuery.Deferred();
					var self = this;

					revertUIAdjustment(report);
					
					jQuery
							.ajax(
									{
										type : "PUT",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl()
												+ "/services/rest/bpm-reporting/report-definition",
										contentType : "application/json",
										data : JSON.stringify({
											operation : "save",
											report : report
										})
									}).done(function(report) {
								applyUIAdjustment(report);		
								deferred.resolve(report);
							}).fail(function(response) {
								deferred.reject(response);
							});

					applyUIAdjustment(report);
					return deferred.promise();
				};

				/**
				 * 
				 */
				ReportingService.prototype.saveReportInstance = function(
						reportDI) {
					var deferred = jQuery.Deferred();
					var self = this;

					revertUIAdjustment(reportDI.definition);
					
					jQuery
							.ajax(
									{
										type : "PUT",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl()
												+ "/services/rest/bpm-reporting/report-instance",
										contentType : "application/json",
										data : JSON.stringify(reportDI)
									}).done(function(report) {
								deferred.resolve(report);
							}).fail(function(response) {
								deferred.reject(response);
							});

					return deferred.promise();
				};
				
				/**
				 * 
				 */
				ReportingService.prototype.isFavoriteReport = function(id) {
					return this.preferenceData.favoriteReports.indexOf(id) > -1;
				};
				
				/**
				 * 
				 */
				ReportingService.prototype.addToFavorites = function(name, id) {
					var deferred = jQuery.Deferred();
					var self = this;

					self.preferenceData.favoriteReports.push(id);
					
					var reportMetadata = {
						name : name,
						id : id
					};
					
					jQuery
							.ajax(
									{
										type : "PUT",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl()
												+ "/services/rest/bpm-reporting/favorite",
										contentType : "application/json",
										data : JSON.stringify(reportMetadata)
									}).done(function() {
									
								deferred.resolve();
							}).fail(function(response) {
								deferred.reject();
							});

					return deferred.promise();
				};
				
				
				ReportingService.prototype.removeFromFavorites = function(id) {
					var deferred = jQuery.Deferred();
					var self = this;

					var index = self.preferenceData.favoriteReports.indexOf(id);
					self.preferenceData.favoriteReports.splice(index, 1);
					
					jQuery
							.ajax(
									{
										type : "DELETE",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl()
												+ "/services/rest/bpm-reporting/favorite?reportId=" + window.btoa(id),
										contentType : "application/json",
									}).done(function() {
									deferred.resolve();
							}).fail(function(response) {
								deferred.reject();
							});

					return deferred.promise();
				};
				/**
				 * Saves all cached Report Definitions.
				 */
				ReportingService.prototype.saveReportDefinitions = function() {
					var deferred = jQuery.Deferred();
					var self = this;

					jQuery
							.ajax(
									{
										type : "PUT",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl()
												+ "/services/rest/bpm-reporting/report-definitions",
										contentType : "application/json",
										data : JSON
												.stringify(this.loadedReportDefinitions)
									}).done(function() {
								deferred.resolve();
							}).fail(function() {
								deferred.reject();
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				ReportingService.prototype.retrieveReportDefinition = function(
						path) {
					var deferred = jQuery.Deferred();
					var self = this;

					jQuery
							.ajax(
									{
										type : "GET",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl() + "/services/rest/bpm-reporting/report-definition" + path
									}).done(function(response) {
								if(response.definition){
									applyUIAdjustment(response.definition);
								}else{
									applyUIAdjustment(response);
								}		
								self.loadedReportDefinitions[path] = response;

								console.debug("Loaded Report Definitions ");
								console.debug(self.loadedReportDefinitions);

								deferred.resolve(response);
							}).fail(function(response) {
								deferred.reject(response);
							});

					return deferred.promise();
				};
				
				/**
				 * 
				 */
				ReportingService.prototype.retrieveExternalData = function(uri) {
					var deferred = jQuery.Deferred();
					var self = this;

					jQuery.ajax({
						type : "GET",
						async: false,
						beforeSend : function(request) {
							// request
							// .setRequestHeader(
							// "Authentication",
							// self
							// .getBasicAuthenticationHeader());
						},
						url : uri,
						contentType : "application/json"
					}).done(function(response) {
						console.debug("Retrieved external data");
						console.debug(response);

						// Use heuristics to obtain records - first
						// element is
						// the record set
						// TODO Fine tune

						for ( var x in response) {
							deferred.resolve(response[x]);

							break;
						}

					}).fail(function(response) {
						deferred.reject(response);
					});

					return deferred.promise();
				};

				/**
				 * 
				 */
				ReportingService.prototype.renameReportDefinition = function(
						path, name) {
					var deferred = jQuery.Deferred();
					var self = this;

					jQuery
							.ajax(
									{
										type : "PUT",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl()
												+ "/services/rest/bpm-reporting/report-definition",
										contentType : "application/json",
										data : JSON.stringify({
											operation : "rename",
											path : path,
											name : name
										})
									}).done(function(response) {
								deferred.resolve(response);
							}).fail(function(response) {
								deferred.reject(response);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				ReportingService.prototype.deleteReportDefinition = function(
						path) {
					var deferred = jQuery.Deferred();
					var self = this;

					jQuery
							.ajax(
									{
										type : "DELETE",
										async: false,
										beforeSend : function(request) {
											request
													.setRequestHeader(
															"Authentication",
															self
																	.getBasicAuthenticationHeader());
										},
										url : self.getRootUrl() + "/services/rest/bpm-reporting/report-definition" + path
									}).done(function() {
								deferred.resolve();
							}).fail(function() {
								deferred.reject([]);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				ReportingService.prototype.getBasicAuthenticationHeader = function() {
					// TODO Inherit security context

					return "Basic "
							+ jQuery.base64.encode("motu" + ":" + "motu");
				};

				/**
				 * 
				 */
				ReportingService.prototype.getRootUrl = function(html) {
					return window.location.href.substring(0, location.href
							.indexOf("/plugins"));

				};

				/**
				 * 
				 */
				ReportingService.prototype.isDiscreteType = function(type) {
					return type == this.metadata.enumerationType
							|| type == this.metadata.stringType
							|| type == this.metadata.integerType;
				};

				/**
				 * 
				 */
				ReportingService.prototype.getISODateTime = function(d, options) {
					if (!options) {
						options = {};
					}

					// Padding function

					var s = function(a, b) {
						return (1e15 + a + "").slice(-b);
					};

					// Default date parameter

					if (typeof d === 'undefined') {
						d = new Date();
					}

					// Return ISO datetime

					if (options.showSeconds) {
						return d.getFullYear() + '-' + s(d.getMonth() + 1, 2)
								+ '-' + s(d.getDate(), 2) + ' '
								+ s(d.getHours(), 2) + ':'
								+ s(d.getMinutes(), 2) + ':'
								+ s(d.getSeconds(), 2);
					} else {
						return d.getFullYear() + '-' + s(d.getMonth() + 1, 2)
								+ '-' + s(d.getDate(), 2) + ' '
								+ s(d.getHours(), 2) + ':'
								+ s(d.getMinutes(), 2);
					}
				};

				/**
				 * 
				 */
				ReportingService.prototype.getPrimitiveTypes = function() {
					return [ this.metadata.stringType,
							this.metadata.integerType,
							this.metadata.decimalType, this.metadata.countType,
							this.metadata.timestampType,
							this.metadata.durationType ];
				};

				/**
				 * Returns a consolidated list of possible dimensions including
				 * joined data from external data sources.
				 */
				ReportingService.prototype.getCumulatedDimensions = function(
						report) {
					var dimensions = [];

					for (var m in this.getPrimaryObject(report.dataSet.primaryObject).dimensions) {
					    var dim = this.getPrimaryObject(report.dataSet.primaryObject).dimensions[m];

					    if (dim.metadata && dim.metadata.isDescriptor) {
					        dim.group = this.getI18N("reporting.definitionView.descriptors");
					        dim.order = 2;
					    } else {
					        dim.group = this.getI18N("reporting.definitionView." + report.dataSet.primaryObject);
					        dim.order = 1;
					    }

					    dimensions.push(dim);
					}

					// Joined external data
					if (report.dataSet.joinExternalData && report.dataSet.externalJoins) {
					    for (var l in report.dataSet.externalJoins) {
					        var join = report.dataSet.externalJoins[l];

					        for (var k in join.fields) {
					            var field = join.fields[k];

					            dimensions.push({
					                id: field.name,
					                name: field.name,
					                type: this.metadata[field.type]
					            });
					        }
					    }
					}

					// Computed columns
					for (var n in report.dataSet.computedColumns) {
					    var column = report.dataSet.computedColumns[n];

					    var type = this.metadata[column.type]
					    
					    dimensions.push({
					        id: column.id,
					        name: column.name,
					        type: type,
					        metadata: {
					            isComputedType: true,
					            name : column.name,
					            type : type.id
					        },
					        group: this.getI18N("reporting.definitionView.computedColumns"),
					        order: 3,
					    });
					}

					//sort data
					dimensions.sort(function(object1, object2) {
					    return object1.name.localeCompare(object2.name);
					});

					return dimensions;
				};

				/**
				 * 
				 */
				ReportingService.prototype.getUserDefinedField = function(
						report, id) {
					// Joined data

					for ( var l in report.dataSet.externalJoins) {
						var join = report.dataSet.externalJoins[l];

						for ( var k in join.fields) {
							var field = join.fields[k];

							if (field.id === id) {
								return {
									id : field.name,
									name : field.name,
									type : this.metadata[field.type]
								};
							}
						}
					}

					// Computed columns

					for ( var m in report.dataSet.computedColumns) {
						var field = report.dataSet.computedColumns[m];

						if (field.id === id) {
							return {
								id : field.name,
								name : field.name,
								type : this.metadata[field.type]
							};
						}
					}

					return null;
				};

				/**
				 * Get dimension objects for report columns.
				 */
				ReportingService.prototype.getColumnDimensions = function(
						report) {
					var dimensions = [];

					for ( var m in report.dataSet.columns) {
						if (this.getPrimaryObject(report.dataSet.primaryObject).dimensions[report.dataSet.columns[m].id] != null) {
							dimensions.push(this.getDimension(
									report.dataSet.primaryObject,
									report.dataSet.columns[m].id));
						} else {
							// Must be a joined field or computed column

							dimensions.push(this.getUserDefinedField(report,
									report.dataSet.columns[m].id));
						}
					}

					return dimensions;
				};
				
				
	           /**
             * Calculates Next Execution Date.
             */
            ReportingService.prototype.getNextExecutionDate = function(scheduling) {
               var deferred = jQuery.Deferred();
               var self = this;

               jQuery
                     .ajax(
                           {
                              type : "GET",
                              async: false,
                              beforeSend : function(request) {
                                 request
                                       .setRequestHeader(
                                             "Authentication",
                                             self
                                                   .getBasicAuthenticationHeader());
                              },
                              url : self.getRootUrl()
                                    + "/services/rest/bpm-reporting/nextExecutionDate?schedulingJSON=" + JSON.stringify(scheduling)
                           }).done(function(response) {
                              console.log(response);
                              var a = 0;
                              a++;
                        deferred.resolve(response);
                     }).fail(function(response) {
                        deferred.reject(response);
                     });

               return deferred.promise();
            };
            
            /**
             * Following are samples of input data data, can be kept for future references 
             */
            
            ReportingService.prototype.getTestSimpleReportData = function(report){
				var countCumulantsCol = {
					  "activity_instances": [
	                         [
	                           "2014/01",
	                           5
	                         ],
	                         [
	                           "2014/04",
	                           3
	                         ]
	                       ]
						};
				
				var countgroupbyCumulantsCol = {
						  "A1": [
						         [
						           "2014/01",
						           10
						         ],
						         [
						           "2014/04",
						           2
						         ]
						       ],
						     "A2": [
						         [
						           "2014/01",
						           12
						         ],
						         [
						           "2014/04",
						           15
						         ]
						       ]
						     }
					
				var nonCountCumulantsCol = {
						  "Activity_Instances": [
						         [
						           "2014/01",
						           30,
						           20,
						           10,
						           140,
						           150
						         ],
						         [
						           "2014/02",
						           30,
						           20,
						           10,
						           140,
						           150
						         ],
						         [
						           "2014/03",
						           150,
						           120,
						           135,
						           140,
						           150
						         ],
						         [
						           "2014/04",
						           150,
						           120,
						           135,
						           140,
						           150
						         ],
						         [
						           "2014/05",
						           150,
						           120,
						           135,
						           140,
						           150
						         ],
						         [
						           "2014/06",
						           150,
						           120,
						           135,
						           140,
						           150
						         ],
						         [
						           "2014/07",
						           150,
						           120,
						           135,
						           140,
						           150
						         ],
						         [
						           "2014/08",
						           150,
						           120,
						           135,
						           140,
						           150
						         ],
						         [
						           "2014/09",
						           150,
						           120,
						           135,
						           140,
						           150
						         ],
						         [
						           "2014/10",
						           150,
						           120,
						           135,
						           140,
						           150
						         ]
						       ]};
				
				var nonCountGroupbyCumulantsCol = {
						"Activity A-2" : [ [ "2014/05/02", 0, 0, 0, 0, 0 ],
								[ "2014/05/03", 12, 7, 9.5, 3.53, 2 ] ],
						"Manual Activity 1" : [
								[ "2014/05/02", 11, 3, 6.66, 4.04, 3 ],
								[ "2014/05/03", 1041, 0, 244.8, 378.74, 10 ] ],
						"Activity A-1" : [ [ "2014/05/02", 0, 0, 0, 0, 0 ],
								[ "2014/05/03", 35, 19, 27.0, 11.31, 2 ] ]
				}
				
				var nonCountGroupbyCumulantsCol1 = {
						  "A1": [
						         [
						           "2014/01",
						           300,
						           100,
						           200,
						           40,
						           50
						         ],
						         [
						           "2014/02",
						           30,
						           20,
						           25,
						           40,
						           50
						         ],
						         [
						           "2014/03",
						           30,
						           20,
						           25,
						           40,
						           50
						         ],
						         [
						           "2014/04",
						           30,
						           20,
						           25,
						           40,
						           50
						         ],
						         [
						           "2014/05",
						           30,
						           20,
						           25,
						           40,
						           50
						         ],
						         [
						           "2014/06",
						           30,
						           20,
						           25,
						           40,
						           50
						         ],
						         [
						           "2014/07",
						           30,
						           20,
						           25,
						           40,
						           50
						         ],
						         [
						           "2014/08",
						           30,
						           20,
						           25,
						           40,
						           50
						         ],
						         [
						           "2014/09",
						           30,
						           20,
						           25,
						           40,
						           50
						         ],
						         [
						           "2014/10",
						           50,
						           20,
						           35,
						           40,
						           50
						         ]
						       ],
						       "A2": [
								         [
								           "2014/01",
								           30,
								           10,
								           20,
								           140,
								           150
								         ],
								         [
								           "2014/02",
								           30,
								           10,
								           20,
								           140,
								           150
								         ],
								         [
								           "2014/03",
								           150,
								           120,
								           135,
								           140,
								           150
								         ],
								         [
								           "2014/04",
								           100,
								           50,
								           75,
								           140,
								           150
								         ],
								         [
								           "2014/05",
								           150,
								           120,
								           135,
								           140,
								           150
								         ],
								         [
								           "2014/06",
								           150,
								           120,
								           135,
								           140,
								           150
								         ],
								         [
								           "2014/07",
								           150,
								           120,
								           135,
								           140,
								           150
								         ],
								         [
								           "2014/08",
								           150,
								           120,
								           135,
								           140,
								           150
								         ],
								         [
								           "2014/09",
								           150,
								           120,
								           135,
								           140,
								           150
								         ],
								         [
								           "2014/10",
								           150,
								           120,
								           135,
								           140,
								           150
								         ]
								       ]
						     };
				
					var priority_example = {
						    				 "-1" : [["2014/05/08",88],
						    				       	["2014/05/09",76]],
						    				 "0" :[["2014/05/08",48],
						    				   	   ["2014/05/09",56]],
					                         "1" : [["2014/05/08",28],
						                             ["2014/05/09",96]]      
											};
				
					var fact_count = (report.dataSet.fact == this.metadata.objects.processInstance.facts.count.id);
					var indData;
					if(report.layout.table.preview){
						//data = countCumulantsCol;
						inData = countgroupbyCumulantsCol;
					
	                    if(fact_count){
	                    	inData = countCumulantsCol;
	                 	   if(report.dataSet.groupBy == 'activityName'){
	                 		  inData = countgroupbyCumulantsCol;   
	                 	   }else if(report.dataSet.groupBy == 'priority'){
	                 		  inData = priority_example;    
	                 	   }
	                    }else{
	                    	inData = nonCountCumulantsCol;
	                 	   if(report.dataSet.groupBy == 'activityName'){
	                 		  inData = nonCountGroupbyCumulantsCol;   
	                 	   }
	                    }
					}
					
					if(report.dataSet.type == "recordSet"){
						return {"columns":["activityOID","activityName","startTimestamp"],"rows":[[15,"Activity A-2","2014/05/16 09:30:24:581"],[14,"Activity A-1","2014/05/16 09:29:49:230"],[1,"Manual Activity 1","2014/05/09 08:25:50:457"]]};
					}
					
					return inData;
            };
            
            /**
             * 
             */
            ReportingService.prototype.getDateFormats = function() {
               var deferred = jQuery.Deferred();
               var self = this;

               jQuery
                     .ajax(
                           {
                              type : "GET",
                              async: false,
                              beforeSend : function(request) {
                                 request
                                       .setRequestHeader(
                                             "Authentication",
                                             self
                                                   .getBasicAuthenticationHeader());
                              },
                              url : self.getRootUrl()
                                    + "/services/rest/bpm-reporting/dateFormats",
                              contentType : "application/json"
                           }).done(function(data) {
                              /*self.clientDateFormat = data.dateFormat;
                              self.serverDateFormat = data.serverDateFormat;*/
                              deferred.resolve(data);
                     }).fail(function() {
                        deferred.reject();
                     });
               return deferred.promise();
            };
            
            /**
             * 
             */
            ReportingService.prototype.uploadReport = function(uuid) {
               var deferred = jQuery.Deferred();
               var self = this;

               jQuery
                     .ajax(
                           {
                              type : "GET",
                              async: false,
                              beforeSend : function(request) {
                                 request
                                       .setRequestHeader(
                                             "Authentication",
                                             self
                                                   .getBasicAuthenticationHeader());
                              },
                              url : self.getRootUrl()
                                    + "/services/rest/bpm-reporting/report-definition/upload?uuid=" + uuid
                           }).done(function(data) {
                              deferred.resolve(data);
                     }).fail(function() {
                        deferred.reject(data);
                     });
               return deferred.promise();
            };
            
            /**
            *
            */
            ReportingService.prototype.isValidReportName = function(reportName) {
              var VALID_FILENAME_PATTERN = /[\\\\/:*?\"<>|\\[\\]]*/;
              return (! reportName.trim().match(VALID_FILENAME_PATTERN)) ? true : false;
           };
            
            /**
             * Removes any forward slashes from the input parameter
             */
            ReportingService.prototype.validateReportName = function(reportName) {
               if(! this.isValidReportName(reportName)) {
                 return reportName.replace(/\//g, '');
               };
               return reportName;
            }
            
			}
			
			/**
			 *	convert parameters 
			 */
			function convertToParametersString(parameters){
				var parametersString = "";
				if(parameters){
					for(var itemInd in parameters){
						if(parameters[itemInd].metadata == null || !parameters[itemInd].metadata.parameterizable){
							continue;
						}
						
						parametersString += parameters[itemInd].dimension + "=";
						//here uiValue is the actual value that needs to be sent to server
						if(parameters[itemInd].uiValue){
							//uiValue is array of strings
							parametersString += getFlatStringFromValue(parameters[itemInd].uiValue); 		
						}else{
							//TODO: remove this when filter and parameter format is same for DATE
							//special parameter, in case of date, there are multiple fields so change the format here
							if(parameters[itemInd].value.from){
								var pValue = ["from", "to", "duration", "durationUnit"];
								var actualValue = parameters[itemInd].value; //complex object startDate = {from : "", to : ""};
								var formattedValue = ""; 
								for (var int = 0; int < pValue.length; int++) {
									if(actualValue[pValue[int]]){
										formattedValue += actualValue[pValue[int]] + ",";	
									}
								}
								parametersString += formattedValue.slice(0,-1);
							}else{
								parametersString += getFlatStringFromValue(parameters[itemInd].value);
							}
						}
						parametersString += "&";
					}
				}
				parametersString = parametersString.slice(0, -1);
				return parametersString; 
			}
			
			/**
			 * 
			 */
			function getFlatStringFromValue(value){
				var flatString = "";
				if(isArray(value)){
					for(var i = 0; i < value.length; i++){
						flatString += value[i] + ",";
					}	
					flatString = flatString.slice(0, -1);
				}else{
					flatString = value;
				}
				return flatString;
			}
			
			/**
			 * returns true if the object is of type Array
			 */
			function isArray(obj) {
				return Object.prototype.toString.call(obj) === '[object Array]';
			}
			
			/**
			 * 
			 */
			function applyUIAdjustment(report) {
				if(report.uiAdjustmentApplied){
					return;
				}
				
				var filters = report.dataSet.filters;
			    if (!filters) {
			        return;
			    }
			    for (var int = 0; int < filters.length; int++) {
			        if (filters[int].uiValue) {
			            var tmp = filters[int].uiValue;
			            filters[int].uiValue = filters[int].value;
			            filters[int].value = tmp;
			        }
			    }
			    report.uiAdjustmentApplied = true;
			};
			
			/**
			 *
			 */
			function revertUIAdjustment(report) {
				if(report.uiAdjustmentApplied == 'false'){
					return;
				}
				
				var filters = report.dataSet.filters;	
				
				if (!filters) {
			        return;
			    }
			    for (var int = 0; int < filters.length; int++) {
			        if (filters[int].uiValue) {
			            var tmp = filters[int].uiValue;
			            filters[int].uiValue = filters[int].value;
			            filters[int].value = tmp;
			        }
			    }
			    report.uiAdjustmentApplied = false;
			};
			
		});