/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
define([ "bpm-modeler/js/m_utils"],
		function(m_utils) {
			return {
				create : function(ruleSet, uuid, id, name) {
					var technicalRule = new TechnicalRule();
					technicalRule.initialize(ruleSet, uuid, id, name);
					return technicalRule;
				}
			};

			function TechnicalRule() {
				this.type = "TechnicalRule";
				this.getDRL=function(){
					return this.drl;
				};
				
				this.setDRL=function(val){
					this.drl=val;
				};
				
				TechnicalRule.prototype.initialize = function(ruleSet, uuid, id, name) {
					var currentDateTime=(new Date()).toString();
					this.ruleSet = ruleSet;
					this.drl="";
					this.uuid = uuid;
					this.id = id;
					this.name = name;
					this.description="";
					this.lastModificationDate=currentDateTime;
					this.creationDate=currentDateTime;
				};

				TechnicalRule.prototype.bindToRuleSet = function(ruleSet) {
					this.ruleSet = ruleSet;
				};
			}
		});