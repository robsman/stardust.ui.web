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
		[ "m_utils", "m_constants", "m_session", "m_commandsController",
				"m_command", "m_propertiesPage" ],
		function(m_utils, m_constants, m_session, m_commandsController,
				m_command, m_propertiesPage) {
			return {
				create : function(propertiesPanel, id) {
					var page = new AnnotationsPropertiesPage(propertiesPanel,
							id);

					page.initialize();

					return page;
				}
			};

			/**
			 * 
			 */
			function AnnotationsPropertiesPage(propertiesPanel, id) {
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, id, "Annotations");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(AnnotationsPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				AnnotationsPropertiesPage.prototype.initialize = function() {
					this.annotationsTableBody = jQuery("#" + this.id
							+ " #annotationsTable tbody");
					this.contentTextArea = this.mapInputId("contentTextArea");
					this.submitButton = this.mapInputId("submitButton");

					this.submitButton.click({
						page : this
					}, function(event) {
						event.data.page.addAnnotation();
					});
				};

				/**
				 * 
				 */
				AnnotationsPropertiesPage.prototype.setElement = function() {
					if (this.propertiesPanel.getModelElement().attributes == null) {
						return null;
					}

					if (this.propertiesPanel.getModelElement().attributes["documentation:annotations"] != null) {

						this.annotations = jQuery
								.parseJSON(this.propertiesPanel
										.getModelElement().attributes["documentation:annotations"]);

						// Convert timestamps

						for ( var n = 0; n < this.annotations.length; ++n) {
							this.annotations[n].timestamp = new Date(
									this.annotations[n].timestamp);
						}
					} else {
						this.annotations = [];
					}

					this.populateAnnotationsTable();
				};

				/**
				 * 
				 */
				AnnotationsPropertiesPage.prototype.addAnnotation = function() {
					this.annotations
							.push({
								timestamp : new Date(),
								userFirstName : m_session.getInstance().loggedInUser.firstName,
								userLastName : m_session.getInstance().loggedInUser.lastName,
								userAccount : m_session.getInstance().loggedInUser.account,
								content : this.contentTextArea.val()
							});

					this.submitChanges(this.propertiesPanel
							.wrapModelElementProperties({
								attributes : {
									"documentation:annotations" : JSON
											.stringify(this.annotations)
								}
							}));

					this.contentTextArea.val(null);
				};

				/**
				 * 
				 */
				AnnotationsPropertiesPage.prototype.populateAnnotationsTable = function() {
					this.annotationsTableBody.empty();

					for ( var n = 0; n < this.annotations.length; ++n) {
						var annotation = this.annotations[n];
						var rowContent = "<tr id='annotation-" + n + "'>";

						rowContent += "<td style='padding-left: 0px;'>";
						rowContent += "<table width='100%' cellspacing='0' cellpadding='0'>";
						rowContent += "<tr>";
						rowContent += "<td><span class='annotationUserTimestampSpan'>";
						rowContent += annotation.userFirstName + " "
								+ annotation.userLastName;
						rowContent += " &bull; ";
						rowContent += m_utils.formatDate(annotation.timestamp,
								"n/j/Y  H:i:s");
						rowContent += " (";
						rowContent += m_utils
								.prettyDateTime(annotation.timestamp);
						rowContent += ")";
						rowContent += "</span></td>";
						rowContent += "</tr>";
						rowContent += "<tr>";
						rowContent += "<td><span class='annotationContentSpan'>";
						rowContent += annotation.content;
						rowContent += "</span></td>";
						rowContent += "</tr>";
						rowContent += "</table>";
						rowContent += "</td>";
						rowContent += "</tr>";

						var row = jQuery(rowContent);

						row.mousedown({
							page : this
						}, function(event) {
							m_utils.debug("Toggle class");
							jQuery(this).toggleClass("selected");
						});

						this.annotationsTableBody.append(rowContent);
					}
				};

				/**
				 * 
				 */
				AnnotationsPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();

					return true;
				};
			}
		});