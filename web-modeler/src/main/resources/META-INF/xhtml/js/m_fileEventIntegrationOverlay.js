/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_model", "m_accessPoint", "m_parameterDefinitionsPanel",
				"m_eventIntegrationOverlay" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel,
				m_eventIntegrationOverlay) {

			return {
				create : function(page, id) {
					var overlay = new FileEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function FileEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(FileEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					this.fileOrDirectoryNameInput = this
							.mapInputId("fileOrDirectoryNameInput");
					this.recursiveInput = this.mapInputId("recursiveInput");
					this.initialIntervalInput = this
							.mapInputId("initialIntervalInput");
					this.initialIntervalUnitSelect = this
							.mapInputId("initialIntervalUnitSelect");
					this.lockBehaviorSelect = this
							.mapInputId("lockBehaviorSelect");
					this.postprocessingSelect = this
							.mapInputId("postprocessingSelect");
					this.alwaysConsumeInput = this
							.mapInputId("alwaysConsumeInput");

					this.registerForRouteChanges(this.fileOrDirectoryNameInput);
					this.registerForRouteChanges(this.recursiveInput);
					this.registerForRouteChanges(this.initialIntervalInput);
					this
							.registerForRouteChanges(this.initialIntervalUnitSelect);
					this.registerForRouteChanges(this.lockBehaviorSelect);
					this.registerForRouteChanges(this.postprocessingSelect);
					this.registerForRouteChanges(this.alwaysConsumeInput);
				};

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var uri = "file://";

					uri += this.fileOrDirectoryNameInput.val();

					uri += "?consumer.recursive="
							+ this.recursiveInput.is("checked");
					uri += "&consumer.initialDelay="
							+ (this.initialIntervalInput.val() == null ? 0
									: this.initialIntervalInput.val())
							* this.initialIntervalUnitSelect.val();
					uri += "&consumer.=" + this.lockBehaviorSelect.val();
					uri += "&consumer.alwaysConsume="
							+ this.alwaysConsumeInput.is("checked");

					if (this.postprocessingSelect.val() == "noop") {
						uri += "&consumer.noop=true";
					} else if (this.postprocessingSelect.val() == "delete") {
						uri += "&consumer.delete=true";
					}

					return uri;
				};

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.activate = function() {
					this.submitEventClassChanges();
				};

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.update = function() {
					this.submitEventClassChanges();
				};
			}
		});