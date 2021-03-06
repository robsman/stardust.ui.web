/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "bpm-modeler/js/m_utils",
         "rules-manager/js/m_i18nUtils"],
		function(m_utils, m_i18nUtils) {
			return {
				create : function(ruleSet, uuid, id, name) {
					var decisionTable = new DecisionTable();
					decisionTable.initialize(ruleSet, uuid, id, name);
					return decisionTable;
				}
			};

			function DecisionTable() {
				/*
				var tableData={
						columns:[{type:"text"},{type:"text"}],
				        data:[
				               [0,'rule 1'],
				               [0,'rule 2'],
				               [0,'rule 3'],
				               [0,'rule 4'],
				               [0,'rule 5']
				           ],
				        colWidths:[35,90],
				        colHeaders:["|NA|Header",
				                     "Description|NA|Header"]
				};
				*/
				this.type = "DecisionTable";
				
				this.getTableData=function(){
					return this.tableData;
				};
				
				this.setTableData=function(val){
					this.tableData=val;
				};
				
				DecisionTable.prototype.initialize = function(ruleSet, uuid, id, name) {
					var currentDateTime=(new Date()).toString();
					this.ruleSet = ruleSet;
					this.uuid = uuid;
					this.id = id;
					this.name = name;
					this.description="";
					this.lastModificationDate=currentDateTime;
					this.creationDate=currentDateTime;
					this.tableData={
							columns:[{type:"text"},{type:"checkbox","default":true}],
					        data:[
					               ['',true],['',true],['',true],['',true],['',true]
					           ],
					        colWidths:[90,90],
					        colHeaders:[m_i18nUtils.getProperty("rules.element.properties.commonProperties.description","Description") + "|NA|Header",
					                    m_i18nUtils.getProperty("rules.propertyView.decisiontableview.lockOnActive","lock-on-active") + "|NA|Attribute"]
					};
				};
				
				//TODO: is this circular reference needed? Fubars JSON.stringify
				DecisionTable.prototype.bindToRuleSet = function(ruleSet) {
					this.ruleSet = ruleSet;
				};
			}
		});