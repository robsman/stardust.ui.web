/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_propertiesPage", "bpm-modeler/js/m_activity", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_propertiesPage, m_activity, m_i18nUtils) {
			return {
				create: function(propertiesPanel) {
					return new ActivityProcessingPropertiesPage(propertiesPanel);
				}
			};

			function ActivityProcessingPropertiesPage(propertiesPanel) {
				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "processingPropertiesPage",
						"Processing",  "../../images/icons/arrow-circle.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ActivityProcessingPropertiesPage.prototype,
						propertiesPage);

				// Field initialization

				this.singleProcessingTypeInput = this
						.mapInputId("singleProcessingTypeInput");
				this.parallelMultiProcessingTypeInput = this
						.mapInputId("parallelMultiProcessingTypeInput");
				this.sequentialMultiProcessingTypeInput = this
						.mapInputId("sequentialMultiProcessingTypeInput");
				this.listDataMappingInput = this.mapInputId("listDataMappingInput");
				this.itemDataList = this.mapInputId("itemDataList");

				// Initialize callbacks

				this.singleProcessingTypeInput.click({
					"callbackScope" : this
				}, function(event) {
					m_utils.debug("single checked");
					if (event.data.callbackScope.singleProcessingTypeInput
							.is(":checked")) {
						event.data.callbackScope.setSingleProcessingType();
					}
				});

				this.parallelMultiProcessingTypeInput
						.click(
								{
									"callbackScope" : this
								},
								function(event) {
									m_utils.debug("parallel checked");
									if (event.data.callbackScope.parallelMultiProcessingTypeInput
											.is(":checked")) {
										event.data.callbackScope
												.setParallelMultiProcessingType();
									}
								});

				this.sequentialMultiProcessingTypeInput
						.click(
								{
									"callbackScope" : this
								},
								function(event) {
									m_utils.debug("sequential checked");
									if (event.data.callbackScope.sequentialMultiProcessingTypeInput
											.is(":checked")) {
										event.data.callbackScope
												.setSequentialMultiProcessingType();
									}
								});

				// Populate list data list from model

				this.listDataMappingInput.empty();
				this.listDataMappingInput.append("<option value='"
						+ m_constants.TO_BE_DEFINED
						+ "'>"
						+ m_i18nUtils
								.getProperty("modeler.general.toBeDefined")
						+ "</option>");

				for ( var n in this.propertiesPanel.models) {
					for ( var m in this.propertiesPanel.models[n].dataItems) {
						this.listDataMappingInput
								.append("<option value='"
										+ this.propertiesPanel.models[n].dataItems[m].getFullId()
										+ "'>"
										+ this.propertiesPanel.models[n].name
										+ "/"
										+ this.propertiesPanel.models[n].dataItems[m].name
										+ "</option>");
					}
				}

				// Populate list data list from model

				this.itemDataList.empty();
				this.itemDataList.append("<option value='"
						+ m_constants.TO_BE_DEFINED
						+ "'>" + m_i18nUtils.getProperty("modeler.general.toBeDefined") + "</option>");

				for ( var n in this.propertiesPanel.models) {
					for ( var m in this.propertiesPanel.models[n].dataItems) {
						this.itemDataList
								.append("<option value='"
										+ this.propertiesPanel.models[n].dataItems[m].getFullId()
										+ "'>"
										+ this.propertiesPanel.models[n].name
										+ "/"
										+ this.propertiesPanel.models[n].dataItems[m].name
										+ "</option>");
					}
				}

				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.setSingleProcessingType = function() {
					this.singleProcessingTypeInput.attr("checked", true);
					this.parallelMultiProcessingTypeInput
							.attr("checked", false);
					this.sequentialMultiProcessingTypeInput.attr("checked",
							false);
					this.listDataMappingInput.attr("disabled", true);
					this.itemDataList.attr("disabled", true);
				};

				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.setParallelMultiProcessingType = function() {
					this.singleProcessingTypeInput.attr("checked", false);
					this.parallelMultiProcessingTypeInput.attr("checked", true);
					this.sequentialMultiProcessingTypeInput.attr("checked",
							false);
					this.listDataMappingInput.removeAttr("disabled");
					this.itemDataList.removeAttr("disabled");
				};

				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.setSequentialMultiProcessingType = function() {
					this.singleProcessingTypeInput.attr("checked", false);
					this.parallelMultiProcessingTypeInput
							.attr("checked", false);
					this.sequentialMultiProcessingTypeInput.attr("checked",
							true);
					this.listDataMappingInput.removeAttr("disabled");
					this.itemDataList.removeAttr("disabled");
				};

				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.setElement = function() {
					if (this.propertiesPanel.element.modelElement.processingType == m_constants.SINGLE_PROCESSING_TYPE) {
						this.setSingleProcessingType();
					} else if (this.propertiesPanel.element.modelElement.processingType == m_constants.PARALLEL_MULTI_PROCESSING_TYPE) {
						this.setParallelMultiProcessingType();
					} else if (this.propertiesPanel.element.modelElement.processingType == m_constants.SEQUENTIAL_MULTI_PROCESSING_TYPE) {
						this.setSequentialMultiProcessingType();
					}
				};

				/**
				 *
				 */
				ActivityProcessingPropertiesPage.prototype.apply = function() {
					if (this.singleProcessingTypeInput.is(":checked")) {
						this.propertiesPanel.element.modelElement.processingType = m_constants.SINGLE_PROCESSING_TYPE;
					} else if (this.parallelMultiProcessingTypeInput
							.is(":checked")) {
						this.propertiesPanel.element.modelElement.processingType = m_constants.PARALLEL_MULTI_PROCESSING_TYPE;
					} else if (this.sequentialMultiProcessingTypeInput
							.is(":checked")) {
						this.propertiesPanel.element.modelElement.processingType = m_constants.SEQUENTIAL_MULTI_PROCESSING_TYPE;
					}
				};
			}
		});