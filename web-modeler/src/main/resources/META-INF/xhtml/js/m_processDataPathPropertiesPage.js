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
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_propertiesPage" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_propertiesPage) {
			return {
				create : function(propertiesPanel) {
					return new ProcessDataPathPropertiesPage(propertiesPanel);
				}
			};

			function ProcessDataPathPropertiesPage(newPropertiesPanel, newId,
					newTitle) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "dataPathPropertiesPage",
						"DataPath",
						"../../images/icons/data-path-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ProcessDataPathPropertiesPage.prototype,
						propertiesPage);

				this.dataPaths = {};

				this.dataPathTable = jQuery("#" + this.propertiesPanel.id
						+ " #" + this.id + " #dataPathTable");
				this.addDataPathButton = jQuery("#" + this.propertiesPanel.id
						+ " #" + this.id + " #addDataPathButton");
				this.dataPathNameInput = jQuery("#" + this.propertiesPanel.id
						+ " #" + this.id + " #dataPathNameInput");
				this.descriptorInput = jQuery("#" + this.propertiesPanel.id
						+ " #" + this.id + " #descriptorInput");
				this.keyDescriptorInput = jQuery("#" + this.propertiesPanel.id
						+ " #" + this.id + " #keyDescriptorInput");
				this.dataPathDataSelect = jQuery("#" + this.propertiesPanel.id
						+ " #" + this.id + " #dataPathDataSelect");
				this.dataPathPathInput = jQuery("#" + this.propertiesPanel.id
						+ " #" + this.id + " #dataPathPathInput");

				for ( var n in this.propertiesPanel.models) {
					for ( var m in this.propertiesPanel.models[n].dataItems) {
						var dataItem = this.propertiesPanel.models[n].dataItems[m];

						this.dataPathDataSelect.append("<option value='"
								+ dataItem.getFullId() + "'>"
								+ this.propertiesPanel.models[n].name + "/"
								+ dataItem.name + "</option>");
					}
				}

				this.addDataPathButton.click({
					"page" : this
				}, function(event) {
					event.data.page.addDataPath();
				});
				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.setElement = function() {
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();

					return true;
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.addDataPath = function() {
					var item = "<tr><td class=\"";

					if (this.descriptorInput.is(":checked")) {
						if (this.keyDescriptorInput.is(":checked")) {
							item += "keyDescriptorDataPathListItem";
						} else {
							item += "descriptorDataPathListItem";
						}
					} else {
						item += "inDataPathListItem";
					}
					
					item += "\"></td><td class=\"dataPathTableCell\">";
					item += this.dataPathNameInput.val();
					item += "</td><td class=\"dataPathTableCell\">";
					item += this.dataPathDataSelect.val();

					if (this.dataPathPathInput.val() != null
							&& this.dataPathPathInput.val() != "") {
						item += ".";
						item += this.dataPathPathInput.val();
					}

					item += "</td></tr>";

					this.dataPathTable.append(item);
					this.dataPaths[this.dataPathNameInput.val()] = {
						name : this.dataPathNameInput.val(),
						type : this.dataPathNameInput.val(),
						descriptor : this.descriptorInput.is(":checked"),
						keyDescriptor : this.keyDescriptorInput.is(":checked"),
						data : this.dataPathDataSelect.val(),
						expression : this.dataPathPathInput.val()						
					};
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.submitChanges = function(
						changes) {
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.propertiesPanel.element.model.id,
									this.propertiesPanel.element.oid, changes));
				};

			}
		});