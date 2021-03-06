/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ 'bpm-modeler/js/m_processBasicPropertiesPage',
				'bpm-modeler/js/m_modelElementCommentsPropertiesPage',
				'bpm-modeler/js/m_processProcessInterfacePropertiesPage',
				'bpm-modeler/js/m_processDataPathPropertiesPage',
				'bpm-modeler/js/m_processDisplayPropertiesPage',
				'bpm-modeler/js/m_processProcessAttachmentsPropertiesPage',
				'bpm-modeler/js/ProcessRuntimePropertiesPage',
				'bpm-modeler/js/m_propertiesPageProvider',
				'bpm-modeler/js/m_i18nUtils'],
		function(m_processBasicPropertiesPage,
				m_modelElementCommentsPropertiesPage,
				m_processProcessInterfacePropertiesPage,
				m_processDataPathPropertiesPage,
				m_processDisplayPropertiesPage,
				m_processProcessAttachmentsPropertiesPage,
				ProcessRuntimePropertiesPage,
				m_propertiesPageProvider,
				m_i18nUtils) {
			return {
				propertiesPage : [
						{
							panelId : "processPropertiesPanel",
							id : "basicPropertiesPage",
							provider : m_processBasicPropertiesPage,
							visibility : "always"
						},
						{
							panelId : "processPropertiesPanel",
							id : "processCommentsPropertiesPage",
							pageHtmlUrl : "plugins/bpm-modeler/views/modeler/modelElementCommentsPropertiesPage.html",
							provider : m_modelElementCommentsPropertiesPage,
							visibility : "always"
						},
						{
							panelId : "processPropertiesPanel",
							id : "processAuthorizationsPropertiesPage",
							provider : m_propertiesPageProvider,
							visibility : "always",
							pageHtmlUrl : "plugins/bpm-modeler/views/modeler/authorizationsPropertiesPage.html",
							imageUrl : "plugins/views-common/images/icons/key.png",
							title : m_i18nUtils.getProperty("modeler.common.authorization"),
							html5 : true
						},
						{
							panelId : "processPropertiesPanel",
							id : "processInterfacePropertiesPage",
							provider : m_processProcessInterfacePropertiesPage,
							visibility : "always"
						},
						{
							panelId : "processPropertiesPanel",
							id : "dataPathPropertiesPage",
							provider : m_processDataPathPropertiesPage,
							visibility : "always"
						},
						{
							panelId : "processPropertiesPanel",
							id : "displayPropertiesPage",
							provider : m_processDisplayPropertiesPage,
							visibility : "always"
						},
						{
							panelId : "processPropertiesPanel",
							id : "processAttachmentsPropertiesPage",
							pageHtmlUrl : "plugins/bpm-modeler/views/modeler/processDefinitionProcessAttachmentsPropertiesPage.html",
							provider : m_processProcessAttachmentsPropertiesPage,
							visibility : "always"
						}, {
							panelId : "processPropertiesPanel",
							id : "processRuntimePropertiesPage",
							pageHtmlUrl : "plugins/bpm-modeler/views/modeler/processRuntimePropertiesPage.html",
							provider : ProcessRuntimePropertiesPage,
							visibility : "always"
						} ]
			};
		});