/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * 
 */

define(
		[ "js/Utils", "js/WorkflowService" ],
		function(Utils, WorkflowService) {
			return {
				create : function(deck) {
					var page = new StartableProcessesPage();

					page.initialize(deck);

					return page;
				}
			};

			function StartableProcessesPage() {
				this.id = "startableProcessesPage";

				/**
				 * 
				 */
				StartableProcessesPage.prototype.initialize = function(deck) {
					this.deck = deck;
				};

				/**
				 * 
				 */
				StartableProcessesPage.prototype.show = function() {
					var deferred = jQuery.Deferred();
					var self = this;

					WorkflowService.instance().getStartableProcesses().done(
							function(startableProcesses) {
								$("#notificationDialog").popup();

								self.startableProcesses = startableProcesses;

								deferred.resolve();
							}).fail(function() {
						deferred.reject();
					});

					return deferred.promise();
				};

				/**
				 * 
				 */
				StartableProcessesPage.prototype.startProcess = function(
						process) {
					var self = this;

					WorkflowService
							.instance()
							.startProcess(process)
							.done(
									function(activityInstance) {
										if (activityInstance != null) {
											self.deck.activityInstancePage.activityInstance = activityInstance;
											self.deck
													.pushPage(self.deck.activityInstancePage);
										} else {
											self
													.openNotificationDialog("Process has been started but no activity is assigned to you.");
										}
									});
				};

				/**
				 * 
				 */
				StartableProcessesPage.prototype.back = function(process) {
					this.deck.popPage();
				};

				/**
				 * 
				 */
				StartableProcessesPage.prototype.openNotificationDialog = function(
						message) {
					$("#notificationDialog #message").empty();
					$("#notificationDialog #message").append(message);
					$("#notificationDialog").popup("open");
				};

				/**
				 * 
				 */
				StartableProcessesPage.prototype.closeNotificationDialog = function() {
					$("#notificationDialog").popup("close");
				};
			}
		});
