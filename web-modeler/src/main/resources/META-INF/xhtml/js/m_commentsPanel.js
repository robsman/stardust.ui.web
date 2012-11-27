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
 * Utility functions for dialog programming.
 *
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_user",
				"bpm-modeler/js/m_session", "bpm-modeler/js/m_dialog" ],
		function(m_utils, m_constants, m_extensionManager, m_user, m_session,
				m_dialog) {
			return {
				create : function(options) {
					var panel = new CommentsPanel();

					panel.initialize(options);

					return panel;
				}
			};

			/**
			 */
			function CommentsPanel() {
				/**
				 *
				 */
				CommentsPanel.prototype.initialize = function(options) {
					this.options = options;
					this.scope = options.scope;

					this.commentsTableBody = jQuery(this.scope
							+ " #commentsTable tbody");
					this.contentTextArea = jQuery(this.scope
							+ " #contentTextArea");
					this.submitButton = jQuery(this.scope
							+ " #submitButton");
					this.deleteButton = jQuery(this.scope
							+ " #deleteButton");
					this.deleteButton.attr("title", "+Delete");
					this.submitButton.click({
						panel : this
					}, function(event) {
						event.data.panel.addComment();
					});

					this.deleteButton.click({
						panel : this
					}, function(event) {
						event.data.panel.deleteSelectedComments();
					});
				};

				/**
				 *
				 */
				CommentsPanel.prototype.setComments = function(comments) {
					this.comments = comments;

					// TODO Should not be necessary; patched for Activity for client demo

					if (this.comments == null) {
						this.comments = [];
					}

					this.populateCommentsTable();
					this.deleteButton.attr("disabled", true);
				};

				/**
				 *
				 */
				CommentsPanel.prototype.addComment = function() {
					this.comments
							.push({
								timestamp : new Date(),
								userFirstName : m_session.getInstance().loggedInUser.firstName,
								userLastName : m_session.getInstance().loggedInUser.lastName,
								userAccount : m_session.getInstance().loggedInUser.account,
								content : this.contentTextArea.val()
							});

					this.submitChanges();
					this.contentTextArea.val(null);
				};

				/**
				 *
				 */
				CommentsPanel.prototype.populateCommentsTable = function() {
					this.commentsTableBody.empty();

					for ( var n = 0; (this.comments && n < this.comments.length); ++n) {
						var comment = this.comments[n];

						var rowContent = "<tr id='" + n
								+ "' class='commentRow'>";

						rowContent += "<td style='padding-left: 0px;'>";
						rowContent += "<table width='100%' cellspacing='0' cellpadding='0'>";
						rowContent += "<tr>";
						rowContent += "<td><span class='commentUserTimestampSpan'>";
						rowContent += comment.userFirstName + " "
								+ comment.userLastName;
						rowContent += " &bull; ";
						rowContent += m_utils.formatDate(new Date(
								comment.timestamp), "n/j/Y  H:i:s");
						rowContent += " (";
						rowContent += m_utils.prettyDateTime(new Date(
								comment.timestamp));
						rowContent += ")";
						rowContent += "</span></td>";
						rowContent += "</tr>";
						rowContent += "<tr>";
						rowContent += "<td><span class='commentContentSpan'>";
						rowContent += comment.content;
						rowContent += "</span></td>";
						rowContent += "</tr>";
						rowContent += "</table>";
						rowContent += "</td>";
						rowContent += "</tr>";

						var row = jQuery(rowContent);

						this.commentsTableBody.append(row);

						row.mousedown({
							page : this
						}, function(event) {
							jQuery(this).toggleClass("selected");

							event.data.page.changeSelection();
						});
					}
				};

				/**
				 *
				 */
				CommentsPanel.prototype.changeSelection = function() {
					var selectedRows = jQuery(this.scope + " table#commentsTable tr.selected");

					if (selectedRows.length == 0) {
						this.deleteButton.attr("disabled", true);

						return;
					}

					for ( var n = 0; n < selectedRows.length; ++n) {
						var comment = this.comments[jQuery(selectedRows[n])
								.attr("id")];

						if (comment.userAccount != m_user.getCurrentUser().account) {
							this.deleteButton.attr("disabled", true);

							return;
						}
					}

					this.deleteButton.removeAttr("disabled");
				};

				/**
				 *
				 */
				CommentsPanel.prototype.deleteSelectedComments = function() {
					var remainingComments = [];
					var rows = jQuery(this.scope + " table#commentsTable tr.commentRow");

					for ( var n = 0; n < rows.length; ++n) {
						if (!jQuery(rows[n]).is(".selected")) {
							remainingComments
									.push(this.comments[jQuery(rows[n]).attr(
											"id")]);
						}
					}

					this.comments = remainingComments;

					this.submitChanges();
				};

				/**
				 *
				 */
				CommentsPanel.prototype.submitChanges = function() {
					if (this.options.submitHandler) {
						this.options.submitHandler
								.submitCommentsChanges(this.comments);
					}
				};
			}
		});