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
		[ "js/WorkflowService" ],
		function(WorkflowService) {
			return {
				create : function(deck) {
					var page = new LoginPage();

					page.initialize(deck);

					return page;
				}
			};

			function LoginPage() {
				this.id = "loginPage";

				/**
				 * 
				 */
				LoginPage.prototype.initialize = function(deck) {
					this.deck = deck;

					$("#notificationDialog").popup();
				};

				/**
				 * 
				 */
				LoginPage.prototype.show = function(deck) {
					var deferred = jQuery.Deferred();

					deferred.resolve();

					return deferred.promise();
				};

				/**
				 * 
				 */
				LoginPage.prototype.login = function() {
					var self = this;

					WorkflowService
							.instance()
							.login(this.account, this.password)
							.done(
									function(user) {
										self.deck.user = user;
										self.deck
												.pushPage(self.deck.dashboardPage);
									}).fail(function() {
								self.openNotificationDialog("Login failed.");
							});
				};

				/**
				 * 
				 */
				LoginPage.prototype.openNotificationDialog = function(message) {
					$("#notificationDialog #message").empty();
					$("#notificationDialog #message").append(message);
					$("#notificationDialog").popup("open");
				};

				/**
				 * 
				 */
				LoginPage.prototype.closeNotificationDialog = function() {
					$("#notificationDialog").popup("close");
				};
			}
		});
