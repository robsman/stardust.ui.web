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
				this.inDataPathInput = jQuery("#" + this.propertiesPanel.id
						+ " #" + this.id + " #inDataPathInput");
				this.outDataPathInput = jQuery("#" + this.propertiesPanel.id
						+ " #" + this.id + " #outDataPathInput");				
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
					this.populateDataPathTable();
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
					this.propertiesPanel.element.dataPathes[this.dataPathNameInput
							.val()] = {
						name : this.dataPathNameInput.val(),
						direction : this.inDataPathInput.is(":checked") ? "IN" : "OUT",
						descriptor : this.descriptorInput.is(":checked"),
						keyDescriptor : this.keyDescriptorInput.is(":checked"),
						dataFullId : this.dataPathDataSelect.val(),
						dataPath : this.dataPathPathInput.val()
					};

					this
							.submitChanges({
								modelElement : {
									dataPathes : this.propertiesPanel.element.dataPathes
								}
							});

					this.populateDataPathTable();
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.removeDataPath = function() {
				};
				
				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.populateDataPathTable = function() {
					if (this.propertiesPanel.element.dataPathes == null) {
						return;
					}

					this.dataPathTable.empty();

					for ( var m in this.propertiesPanel.element.dataPathes) {
						var dataPath = this.propertiesPanel.element.dataPathes[m];

						m_utils.debug("Data Path");
						m_utils.debug(dataPath);

						var item = "<tr id=\"";

						item += dataPath.id;

						item += "TableRow\"><td class=\"";

						if (dataPath.direction == "IN") {
							if (dataPath.descriptor) {
								if (dataPath.keyDescriptor) {
									item += "keyDescriptorDataPathListItem";
								} else {
									item += "descriptorDataPathListItem";
								}
							} else {
								item += "outDataPathListItem";
							}
						} else {
							item += "inDataPathListItem";
						}

						item += "\"></td><td class=\"dataPathTableCell\">";
						item += dataPath.name;
						item += "</td><td class=\"dataPathTableCell\">";
						item += dataPath.dataFullId;

						if (dataPath.dataPath != null
								&& dataPath.dataPath != "") {
							item += ".";
							item += dataPath.dataPath;
						}

						item += "</td></tr>";

						this.dataPathTable.append(item);
					}

					// this.dataPathTable.tableScroll({
					// height : 200
					// });
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