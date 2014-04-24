define(["jquery",
        "rules-manager/js/hotDecisionTable/m_utilities",
        "rules-manager/js/m_i18nUtils"],
        function(
        		JQuery,
        		m_utilities,
        		m_i18nUtils){
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
		
		/*Constants mapped to the event name we will trigger via the command object.*/
		var constants={
				ruleSetRedoCmd:"redo",
				ruleSetUndoCmd:"undo",
				decTableRenameCmd:"DecisionTable.Name.Change",
				decTableDescriptionCmd:"DecisionTable.Description.Change",
				decTableDataCmd:"DecisionTable.Data.Change",
				ruleSetRenameCmd:"RuleSet.Name.Change",
				ruleSetDescriptionCmd:"RuleSet.Description.Change",
				ruleSetFactCmd:"RuleSet.Fact.Change",
				ruleCreateCmd: "Rule.Create",
				ruleDeleteCmd: "Rule.Delete",
				ruleRenameCmd:"Rule.Name.Change",
				ruleDescriptionCmd:"Rule.Description.Change",
				ruleScriptChangeCmd:"Rule.Script.Change",
				decTableCreateCmd: "DecisionTable.Create",
				decTableDeleteCmd: "DecisionTable.Delete"
		};
		/*Return individual functions for each command we require for our RuleSet perspecitve,
		 *as well as the two base functions used to create all commands.*/
		return {
			"commands":constants,
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
				var cmd=createCommand(constants.ruleSetRedoCmd,false,undefined,"Redo event",undefined,ruleSetUUID,elementID,elementType);
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
				var cmd=createCommand(constants.ruleSetUndoCmd,false,undefined,"Undo event",undefined,ruleSetUUID,elementID,elementType);
				cmd.nameSpace="undo";
				return cmd;
			},
			"decTableRenameCmd" : function(ruleSet,decTable,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"decisionTable",decTable.uuid,"",newVal);
				var cmd=createCommand(
						constants.decTableRenameCmd,true,changeObj,
						ruleSet.name + "." +
						decTable.id + " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.decTableRenameCmd") +
						" \"" + newVal + "\".",
						baseEvent,ruleSet.uuid,decTable.uuid,"decisionTable");
				return cmd;
			},
			"decTableCreateCmd" : function(ruleSet,decTable,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"decisionTable",decTable.uuid,"",newVal);
				var cmd=createCommand(
						constants.decTableCreateCmd,true,changeObj,
						ruleSet.name + "." +
						decTable.id + " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.decTableCreateCmd"),
						baseEvent,ruleSet.uuid,decTable.uuid,"decisionTable");
				return cmd;
			},
			"decTableDeleteCmd" : function(ruleSet,decTable,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"decisionTable",decTable.uuid,"",newVal);
				var cmd=createCommand(
						constants.decTableDeleteCmd,true,changeObj,
						ruleSet.name + "." +
						decTable.id + " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.decTableDeleteCmd"),
						baseEvent,ruleSet.uuid,decTable.uuid,"decisionTable");
				return cmd;
			},
			"decTableDescriptionCmd" : function(ruleSet,decTable,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"decisionTable",decTable.uuid,"",newVal);
				var cmd=createCommand(
						constants.decTableDescriptionCmd,true,changeObj,
						ruleSet.name + "." +
						decTable.id + " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.decTableDescriptionCmd") +
						" \"" + newVal + "\".",
						baseEvent,ruleSet.uuid,decTable.uuid,"decisionTable");
				return cmd;
			},
			"decTableDataCmd" : function(ruleSet,decTable,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"decisionTable",decTable.uuid,"",newVal);
				var cmd=createCommand(
						constants.decTableDataCmd,true,changeObj,
						ruleSet.name + "." +
						decTable.id + " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.decTableDataCmd"),
						baseEvent,ruleSet.uuid,decTable.uuid,"decisionTable");
				return cmd;
			},
			"ruleSetRenameCmd" : function(ruleSet,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"ruleSet",ruleSet.uuid,"",newVal);
				var cmd=createCommand(
						constants.ruleSetRenameCmd,true,changeObj,
						ruleSet.name + "." +
						ruleSet.id+ " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.ruleSetRenameCmd") +
						" \"" + newVal +"\"."
						,baseEvent,ruleSet.uuid,ruleSet.uuid,"ruleSet");
				return cmd;
			},
			"ruleSetDescriptionCmd" : function(ruleSet,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"ruleSet",ruleSet.uuid,"",newVal);
				var cmd=createCommand(
						constants.ruleSetDescriptionCmd,true,changeObj,
						ruleSet.name + "." +
						ruleSet.id + " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.ruleSetDescriptionCmd") +
						" \"" + newVal +"\"."
						,baseEvent,ruleSet.uuid,ruleSet.uuid,"ruleSet");
				return cmd;
			},
			"ruleSetFactCmd" : function(ruleSet,newVal,baseEvent){
				var changeObj=createChangeObj(ruleSet.uuid,"ruleSet",ruleSet.uuid,"",newVal);
				var cmd=createCommand(
						constants.ruleSetFactCmd,true,changeObj,
						ruleSet.name + "." +
						ruleSet.id + " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.ruleSetFactCmd")
						,baseEvent,ruleSet.uuid,ruleSet.uuid,"ruleSet");
				return cmd;
			},
			"ruleCreateCmd" : function(ruleSet,rule,newVal,baseEvent){
				var changeObj=createChangeObj(rule.uuid,"rule",rule.uuid,"",newVal);
				var cmd=createCommand(
						constants.ruleCreateCmd,true,changeObj,
						ruleSet.name + "." +
						rule.id+ " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.ruleCreateCmd")
						,baseEvent,ruleSet.uuid,rule.uuid,"rule");
				return cmd;
			},
			"ruleDeleteCmd" : function(ruleSet,rule,newVal,baseEvent){
				var changeObj=createChangeObj(rule.uuid,"rule",rule.uuid,"",newVal);
				var cmd=createCommand(
						constants.ruleDeleteCmd,true,changeObj,
						ruleSet.name + "." +
						rule.id+ " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.ruleDeleteCmd")
						,baseEvent,ruleSet.uuid,rule.uuid,"rule");
				return cmd;
			},
			"ruleRenameCmd" : function(ruleSet,rule,newVal,baseEvent){
				var changeObj=createChangeObj(rule.uuid,"rule",rule.uuid,"",newVal);
				var cmd=createCommand(
						constants.ruleRenameCmd,true,changeObj,
						ruleSet.name + "." +
						rule.id+ " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.ruleRenameCmd") +
						" \"" + newVal + "\"."
						,baseEvent,ruleSet.uuid,rule.uuid,"rule");
				return cmd;
			},
			"ruleDescriptionCmd" : function(ruleSet,rule,newVal,baseEvent){
				var changeObj=createChangeObj(rule.uuid,"rule",rule.uuid,"",newVal);
				var cmd=createCommand(
						constants.ruleDescriptionCmd,true,changeObj,
						ruleSet.name + "." +
						rule.id+ " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.ruleDescriptionCmd") +
						" \"" + newVal + "\"."
						,baseEvent,ruleSet.uuid,rule.uuid,"rule");
				return cmd;
			},
			"ruleScriptChangeCmd" : function(ruleSet,rule,newVal,baseEvent){
				var changeObj=createChangeObj(rule.uuid,"rule",rule.uuid,"",newVal);
				var cmd=createCommand(
						constants.ruleScriptChangeCmd,true,changeObj,
						ruleSet.name + "." +
						rule.id+ " - " +
						m_i18nUtils.getProperty("rules.rulesetcommands.description.ruleScriptChangeCmd")
						,baseEvent,ruleSet.uuid,rule.uuid,"rule");
				return cmd;
			}
		};

});