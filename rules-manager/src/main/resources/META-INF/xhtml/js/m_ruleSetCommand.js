define(["jquery","rules-manager/js/hotDecisionTable/m_utilities"],function(JQuery,m_utilities){
		/* Wrapping command object that contains a single logical change
		 * to a ruleSet. Physically, this may represent multiple changes to the
		 * ruleSet thus the changes element is an array.
		 * ------------------------------------------------------------------------------
		 * @Param name: Name of the change being made, will be used for command processing.
		 * 	      E.G. RuleSet.TechnicalRule.Rename,RuleSet.DecisionTable.CellChange,...
		 * @Param isUndoable: Can this command be undone (determines whether the command goes on
		 * 				the undo-redo stack.)
		 * @Param changes: Array of changes associated with the command. Usually only one change but
		 * 			 supports multiple.
		 * @Paramdescription: User friendly description of the change as you would use on a tooltip.
		 * */
		var createCommand = function(name,canUndo,changes,description,baseEvent,ruleSetUUID,elementID,elementType){
			if(JQuery.isArray(changes)===false){
				changes=[changes];
			}
			var cmd={
				"nameSpace" : "RuleSet",
				"event"     : name,
				"isUndoable": canUndo || false,
				"changes": changes || [],
				"description": description,
				"baseEvent": baseEvent,
				"ruleSetUUID":ruleSetUUID || "",
				"elementID": elementID || "",
				"elementType": elementType || "",
				"eventID": m_utilities.uuidV4()
			};
			
			return cmd;
		};
		/*Factory function to help ensure users create accurate change objects for use
		 * in the createCommand factory.
		 * ----------------------------------------------------------------------------------
		 * @Param ruleSetUUID: UUID of the ruleSet associated with this change
		 * @Param elementType: String identifying the element in the RuleSet that is changing.
		 * 					   Valid Values- ["technicalRules",
		 * 									  "decisionTables",
		 * 									  "parameterDefinitions",
		 * 									   "this"] 
		 * @Param elementID: ID of the element being modified in the corresponding 
		 * 					 elementType structure.
		 * */
		var createChangeObj= function(ruleSetUUID,elementType,elementID,oldVal,newVal){
			var chng={
					"ruleSetUUID": ruleSetUUID,
					"elementType": elementType,
					"elementID": elementID,
					value: {"before":oldVal,"after":newVal}
			};
			return chng;
		};
		
		/* Return individual functions for each command we require for our RuleSet perspecitve,
		 * as well as the two base functions used to create all commands.*/
		return {
			"createCommand": createCommand,
			"createChangeObj" :createChangeObj,
			"ruleSetRedoCmd" : function(ruleSetUUID,elementType,elementID){
				if(elementType==="ruleSetView"){
					elementType="ruleSet";
				}else if(elementType==="decisionTableView"){
					elementType="decisionTable";
				}else if(elementType==="technicalRuleView"){
					elementType="rule";
				}
				var cmd=createCommand("redo",false,undefined,"Redo event",undefined,ruleSetUUID,elementID,elementType);
				cmd.nameSpace="redo";
				return cmd;
			},
			"ruleSetUndoCmd" : function(ruleSetUUID,elementType,elementID){
				if(elementType==="ruleSetView"){
					elementType="ruleSet";
				}else if(elementType==="decisionTableView"){
					elementType="decisionTable";
				}else if(elementType==="technicalRuleView"){
					elementType="rule";
				}
				var cmd=createCommand("undo",false,undefined,"Undo event",undefined,ruleSetUUID,elementID,elementType);
				cmd.nameSpace="undo";
				return cmd;
			},
			"decTableRenameCmd" : function(ruleSet,decTable,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"decisionTable",decTable.uuid,"",newVal);
				var cmd=createCommand(
						"DecisionTable.Name.Change",true,changeObj,
						"Changed Name of description of decision table.",baseEvent,ruleSet.uuid,decTable.uuid,"decisionTable");
				return cmd;
			},
			"decTableDescriptionCmd" : function(ruleSet,decTable,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"decisionTable",decTable.uuid,"",newVal);
				var cmd=createCommand(
						"DecisionTable.Description.Change",true,changeObj,
						"Changed description of decision table.",baseEvent,ruleSet.uuid,decTable.uuid,"decisionTable");
				return cmd;
			},
			"decTableDataCmd" : function(ruleSet,decTable,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"decisionTable",decTable.uuid,"",newVal);
				var cmd=createCommand(
						"DecisionTable.Data.Change",true,changeObj,
						"Data or configuration of the table changed.",baseEvent,ruleSet.uuid,decTable.uuid,"decisionTable");
				return cmd;
			},
			"ruleSetRenameCmd" : function(ruleSet,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"ruleSet",ruleSet.uuid,"",newVal);
				var cmd=createCommand(
						"RuleSet.Name.Change",true,changeObj,
						"Changed the Name of a ruleSet.",baseEvent,ruleSet.uuid,ruleSet.uuid,"ruleSet");
				return cmd;
			},
			"ruleSetDescriptionCmd" : function(ruleSet,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"ruleSet",ruleSet.uuid,"",newVal);
				var cmd=createCommand(
						"RuleSet.Description.Change",true,changeObj,
						"Changed the Description of a ruleSet.",baseEvent,ruleSet.uuid,ruleSet.uuid,"ruleSet");
				return cmd;
			},
			"ruleSetFactCmd" : function(ruleSet,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"ruleSet",ruleSet.uuid,"",newVal);
				var cmd=createCommand(
						"RuleSet.Fact.Change",true,changeObj,
						"Changed the Underlying Facts for a RuleSet.",baseEvent,ruleSet.uuid,ruleSet.uuid,"ruleSet");
				return cmd;
			},
			"ruleRenameCmd" : function(ruleSet,rule,newVal,baseEvent){
				var changeObj=createChangeObj(rule.uuid,"rule",rule.uuid,"",newVal);
				var cmd=createCommand(
						"Rule.Name.Change",true,changeObj,
						"Changed Name of Scripted Rule.",baseEvent,ruleSet.uuid,rule.uuid,"rule");
				return cmd;
			},
			"ruleDescriptionCmd" : function(ruleSet,rule,newVal,baseEvent){
				var changeObj=createChangeObj(rule.uuid,"rule",rule.uuid,"",newVal);
				var cmd=createCommand(
						"Rule.Description.Change",true,changeObj,
						"Changed description of rule.",baseEvent,ruleSet.uuid,rule.uuid,"rule");
				return cmd;
			},
			"ruleScriptChangeCmd" : function(ruleSet,rule,newVal,baseEvent){
				var changeObj=createChangeObj(rule.uuid,"rule",rule.uuid,"",newVal);
				var cmd=createCommand(
						"Rule.Script.Change",true,changeObj,
						"Changed Scripted Rule.",baseEvent,ruleSet.uuid,rule.uuid,"rule");
				return cmd;
			}
		};

});