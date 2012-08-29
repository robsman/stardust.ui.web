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
		[ "m_utils", "m_constants", "m_extensionManager", "m_command", "m_commandsController", "m_basicPropertiesPage", "m_dataTypeSelector" ],
		function(m_utils, m_constants, m_extensionManager, m_command, m_commandsController, m_basicPropertiesPage, m_dataTypeSelector) {
			return {
				create : function(propertiesPanel) {
					var page = new DataBasicPropertiesPage(propertiesPanel);
					
					page.initialize();
					
					return page;
				}
			};

			function DataBasicPropertiesPage(propertiesPanel) {
				var propertiesPage = m_basicPropertiesPage.create(propertiesPanel);

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(DataBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.initialize = function() {	
					this.initializeBasicPropertiesPage();

					this.dataTypeSelector = m_dataTypeSelector.create("dataPropertiesPanel", this);
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.data;
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.assembleChangedObjectFromProperty = function(
						property, value) {
					var element = {};

					element[property] = value;

					return element;
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.assembleChangedObjectFromAttribute = function(
						attribute, value) {
					var element = {
						attributes : {}
					};

					element.attributes[attribute] = value;

					return element;
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.data;
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					m_utils.debug("===> Data");
					m_utils.debug(this.propertiesPanel.element);
					m_utils.debug(this.getModelElement());

					this.dataTypeSelector.setScopeModel(this.getModelElement().model);
					this.dataTypeSelector.setDataType(this.getModelElement());
				};

				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.validate = function() {
					if (this.validateModelElement()) {
						return true;
					}
					
					return false;
				};
				
				/**
				 * 
				 */
				DataBasicPropertiesPage.prototype.submitDataChanges = function(dataChanges) {
					this.submitChanges(dataChanges);
				};
			}
		});