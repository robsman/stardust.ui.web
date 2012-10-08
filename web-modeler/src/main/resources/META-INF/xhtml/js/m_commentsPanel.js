/**
 * Utility functions for dialog programming.
 *
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_extensionManager", "m_session",
				"m_dialog" ],
		function(m_utils, m_constants, m_extensionManager, m_session, m_dialog) {
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

					this.commentsTableBody = jQuery("#" + this.scope
							+ " #commentsTable tbody");
					this.contentTextArea = jQuery("#" + this.scope
							+ " #contentTextArea");
					this.submitButton = jQuery("#" + this.scope
							+ " #submitButton");

					this.submitButton.click({
						panel : this
					}, function(event) {
						event.data.panel.addComment();
					});
				};

				/**
				 *
				 */
				CommentsPanel.prototype.setComments = function(comments) {
					this.comments = comments;

					this.populateCommentsTable();
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
						m_utils.debug("n = " + n);
						m_utils.debug(comment);

						var rowContent = "<tr id='comment-" + n + "'>";

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

						row.mousedown({
							page : this
						}, function(event) {
							jQuery(this).toggleClass("selected");
						});

						this.commentsTableBody.append(rowContent);
					}
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