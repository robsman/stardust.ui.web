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

				this.dataPathTable = this.mapInputId("dataPathTable");
				this.addDataPathButton = this.mapInputId("addDataPathButton");
				this.deleteDataPathButton = this
						.mapInputId("deleteDataPathButton");
				this.moveDataPathUpButton = this
						.mapInputId("moveDataPathUpButton");
				this.moveDataPathDownButton = this
						.mapInputId("moveDataPathDownButton");
				this.dataPathNameInput = this.mapInputId("dataPathNameInput");
				this.inDataPathInput = this.mapInputId("inDataPathInput");
				this.outDataPathInput = this.mapInputId("outDataPathInput");
				this.descriptorInput = this.mapInputId("descriptorInput");
				this.keyDescriptorInput = this.mapInputId("keyDescriptorInput");
				this.dataPathDataSelect = this.mapInputId("dataPathDataSelect");
				this.dataPathPathInput = this.mapInputId("dataPathPathInput");

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

				this.deleteDataPathButton.click({
					"page" : this
				}, function(event) {
					var dataPathId = jQuery("table#dataPathTable tr.selected")
							.attr("id");

					event.data.page.removeDataPath(dataPathId);
				});

				this.moveDataPathUpButton.click({
					"page" : this
				}, function(event) {
					var dataPathId = jQuery("table#dataPathTable tr.selected")
							.attr("id");

					m_utils.debug("Moving up" + dataPathId);
				});

				this.moveDataPathDownButton.click({
					"page" : this
				}, function(event) {
					var dataPathId = jQuery("table#dataPathTable tr.selected")
							.attr("id");

					m_utils.debug("Moving down" + dataPathId);
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
				ProcessDataPathPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element;
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
					this.getModelElement().dataPathes[this.dataPathNameInput
							.val()] = {
						name : this.dataPathNameInput.val(),
						direction : this.inDataPathInput.is(":checked") ? "IN"
								: "OUT",
						descriptor : this.descriptorInput.is(":checked"),
						keyDescriptor : this.keyDescriptorInput.is(":checked"),
						dataFullId : this.dataPathDataSelect.val(),
						dataPath : this.dataPathPathInput.val()
					};

					this
							.submitChanges({
								modelElement : {
									dataPathes : this.getModelElement().dataPathes
								}
							});

					this.populateDataPathTable();
				};

				/**
				 * 
				 */
				ProcessDataPathPropertiesPage.prototype.removeDataPath = function(
						dataPathId) {
					delete this.getModelElement().dataPathes[dataPathId];

					this
							.submitChanges({
								modelElement : {
									dataPathes : this.getModelElement().dataPathes
								}
							});

					this.populateDataPathTable();
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

						item += "\"><td class=\"";

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

					jQuery("table#dataPathTable tr").mousedown(function() {
						jQuery("tr.selected").removeClass("selected");
						jQuery(this).addClass("selected");
					});

					// this.dataPathTable.tableScroll({
					// height : 200
					// });
				};
			}
		});