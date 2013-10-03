/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
		"bpm-modeler/js/m_model", "bpm-modeler/js/m_dialog",
		"rules-manager/js/CommandsDispatcher", "rules-manager/js/RuleSet",
		"rules-manager/js/PopupSelector", "rules-manager/js/RuleEditor" ],
		function(m_utils, m_i18nUtils, m_model, m_dialog, CommandsDispatcher,
				RuleSet, PopupSelector, RuleEditor) {
			return {
				initialize : function(ruleSetUuid, uuid) {
					var ruleSet = RuleSet.findRuleSetByUuid(ruleSetUuid);
					var rule = RuleSet.findRuleByUuid(uuid);
					var ruleView = new RuleView();

					CommandsDispatcher.registerCommandHandler(ruleView);

					ruleView.initialize(ruleSet, rule);
					ruleView.activate(ruleSet, rule);
				}
			};

			/**
			 * 
			 */
			function RuleView() {
				/**
				 * 
				 */
				RuleView.prototype.initialize = function(ruleSet, rule) {
					this.id = "ruleView";

					this.nameInput = m_utils.jQuerySelect("#ruleView #nameInput");
					this.ruleEditor = RuleEditor.create();

					m_utils.jQuerySelect("#ruleTabs").tabs();
					
					this.nameInput.change({view: this}, function(event){
						event.data.view.rule.name = event.data.view.nameInput.val();

						CommandsDispatcher.submitCommand();
					});
				};

				/**
				 * 
				 */
				RuleView.prototype.activate = function(ruleSet, rule) {
					this.rule = rule;
					this.ruleSet = ruleSet;

					this.nameInput.val(this.rule.name);
					this.ruleEditor.activate(this.ruleSet, this.rule);
				};

				/**
				 * 
				 */
				RuleView.prototype.toString = function() {
					return "rules-manager.RuleView";
				};

				/**
				 * 
				 */
				RuleView.prototype.processCommand = function(command) {
					// TODO Dummy
					
					this.activate(this.ruleSet, this.rule);
				};
			}
		});