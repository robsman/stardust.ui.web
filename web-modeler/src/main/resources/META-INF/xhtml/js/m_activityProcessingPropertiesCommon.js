/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * Activity processing related common function that are needed by
 * support activity basic properties page and activity processing page
 *
 * @author Shrikant.Gangal
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_propertiesPage", "bpm-modeler/js/m_activity", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_propertiesPage, m_activity, m_i18nUtils) {
			return {
				initProcessingType : initProcessingType,

				getProcessingType : getProcessingType
			};

			/**
			 *
			 */
			function initProcessingType(page) {
				var me = page.propertiesPanel.getModelElement();
				page.processingTypeSelect.empty();
				page.processingTypeSelect
						.append("<option value='"
								+ m_constants.SINGLE_PROCESSING_TYPE
								+ "'>"
								+ m_i18nUtils
										.getProperty("modeler.activity.propertyPages.general.processingType.options.singleInstance")
								+ "</option>");
				if (me.subprocessMode !== "synchShared") {
					page.processingTypeSelect
							.append("<option value='"
									+ m_constants.PARALLEL_MULTI_PROCESSING_TYPE
									+ "'>"
									+ m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.processingType.options.multiInstanceParallel")
									+ "</option>");
				}
				page.processingTypeSelect
						.append("<option value='"
								+ m_constants.SEQUENTIAL_MULTI_PROCESSING_TYPE
								+ "'>"
								+ m_i18nUtils
										.getProperty("modeler.activity.propertyPages.general.processingType.options.multiInstanceSequential")
								+ "</option>");

				page.processingTypeSelect
						.val(m_constants.SINGLE_PROCESSING_TYPE);
				if(page.processingTypeLink != undefined){
					page.processingTypeLink.css("visibility",'hidden');
				}
				if (me
						&& me.supportsProcessingType()
						&& me.attributes["carnot:engine:relocate:source"] !== true) {
					page.processingTypeSelect.removeAttr("disabled", true);
					var loop = page.getModelElement().loop;
					if (loop) {
						if (loop.type === m_constants.MULTI_INSTANCE_LOOP_TYPE) {
							if (loop.sequential) {
								page.processingTypeSelect
										.val(m_constants.SEQUENTIAL_MULTI_PROCESSING_TYPE);
							} else {
								page.processingTypeSelect
										.val(m_constants.PARALLEL_MULTI_PROCESSING_TYPE);
							}
							if(page.processingTypeLink != undefined){
								page.processingTypeLink.css("visibility",'visible');
							}
						}
					}
				} else {
					page.processingTypeSelect.attr("disabled", true);
				}
			}
			;

			function getProcessingType(page) {
				var me = page.propertiesPanel.getModelElement();
				if (me && me.supportsProcessingType()) {
					page.processingTypeSelect.removeAttr("disabled", true);
					var loop = page.getModelElement().loop;
					if (loop) {
						if (loop.type === m_constants.MULTI_INSTANCE_LOOP_TYPE) {
							if (loop.sequential) {
								return m_constants.SEQUENTIAL_MULTI_PROCESSING_TYPE;
							} else {
								return m_constants.PARALLEL_MULTI_PROCESSING_TYPE;
							}
						}
					}
				}

				return m_constants.SINGLE_PROCESSING_TYPE;
			}
});
