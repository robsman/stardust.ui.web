/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * Holds common code for report management
 * 
 * @author Yogesh.Manware
 * 
 */
define(
		[ "bpm-reporting/public/js/report/I18NUtils" ],
		function(I18NUtils) {
			return {
				create : function(reportingService) {
					var helper = new ReportHelper();
					helper.reportingService = reportingService;
					return helper;
				}
			};

			function ReportHelper() {

				ReportHelper.prototype.getI18N = function(key) {
					return I18NUtils.getProperty(key);
				};

				/**
				 * 
				 * @param id
				 * @param report
				 * @returns
				 */
				ReportHelper.prototype.primaryObjectEnumGroup = function(id,
						report) {
					var dimension = this.getDimension(id, report);

					if (!dimension) {
						dimension = this.getComputedColumnAsDimensions(report)[id];
					}

					if (!dimension) {
						return;
					}

					if (dimension.metadata && dimension.metadata.isDescriptor) {
						return this
								.getI18N("reporting.definitionView.descriptors");
					} else if (dimension.metadata
							&& dimension.metadata.isComputedType) {
						return this
								.getI18N("reporting.definitionView.computedColumns");
					}
					return this.getI18N("reporting.definitionView."
							+ report.dataSet.primaryObject);
				};

				/**
				 * 
				 * @param report
				 * @returns
				 */
				ReportHelper.prototype.getComputedColumnAsDimensions = function(
						report) {
					var dimensions = {};
					for ( var n in report.dataSet.computedColumns) {
						var column = report.dataSet.computedColumns[n];

						dimensions[column.id] = {
							id : column.id,
							name : column.name,
							type : this.reportingService.metadata[column.type],
							metadata : {
								isComputedType : true
							}
						};
					}

					return dimensions;
				};

				/**
				 * 
				 * @param id
				 * @returns
				 */
				ReportHelper.prototype.getDimension = function(id, report) {
					return this.getPrimaryObject(report).dimensions[id];
				};

				/**
				 * 
				 * @returns
				 */
				ReportHelper.prototype.getPrimaryObject = function(report) {
					return this.reportingService.metadata.objects[report.dataSet.primaryObject];
				};

				/**
				 * Returns the order index of a dimension depending on its group .
				 */
				ReportHelper.prototype.getDimensionsDisplayOrder = function(id,
						report) {
					var dimension = this.getDimension(id, report);

					if (!dimension) {
						dimension = this.getComputedColumnAsDimensions(report)[id];
						return;
					}

					if (dimension.metadata && dimension.metadata.isDescriptor) {
						return 2;
					} else if (dimension.metadata
							&& dimension.metadata.isComputedType) {
						return 3;
					}
					return 1;
				};
				
				/**
				 * 
				 */
				ReportHelper.prototype.prepareParameters = function(filters) {
					var parameters = [];
					for (var int = 0; int < filters.length; int++) {
						if (filters[int].metadata.parameterizable) {
							parameters.push(angular.copy(filters[int]));
						}
					}
					return parameters;
				};
			}
		});