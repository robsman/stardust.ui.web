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
 * 
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_model" ],

		function(m_utils, m_constants, m_urlUtils, m_i18nUtils, m_model) {
			return {
				openCreateApplicationWrapperProcessWizard : function(node) {
					var application = m_model.findApplication(node
							.attr("fullId"));

					var popupData = {
						attributes : {
							width : "700px",
							height : "500px",
							src : m_urlUtils.getPlugsInRoot()
									+ "bpm-modeler/views/modeler/serviceWrapperWizard.html"
						},
						payload : {
							callerWindow : window,
							application : application,
							viewManager : viewManager
						}
					};
					

					parent.iPopupDialog.openPopup(popupData);
				},
				openProcessInterfaceTestWrapperProcessWizard : function(node) {
					var process = m_model.findProcess(node
							.attr("fullId"));

					var popupData = {
						attributes : {
							width : "700px",
							height : "500px",
							src : m_urlUtils.getPlugsInRoot()
									+ "bpm-modeler/views/modeler/processInterfaceTestWrapperWizard.html"
						},
						payload : {
							callerWindow : window,
							process : process,
							viewManager : viewManager						}
					};
					

					parent.iPopupDialog.openPopup(popupData);
				}
			};
		});