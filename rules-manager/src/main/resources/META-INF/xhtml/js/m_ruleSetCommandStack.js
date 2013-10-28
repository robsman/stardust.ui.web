define(["rules-manager/js/m_ruleSet",
        "rules-manager/js/m_ruleSetCommand",
        "bpm-modeler/js/m_jsfViewManager"],
		function(m_ruleSet,m_ruleSetCommand,m_jsfViewManager){
	
	
	/* private factory to generate pointer commands. These are the commands we will
	 * issue when the pointer of the commandStack is moved. Commands are triggered on the associated
	 * sink for listeners who are tuned into the sinks events. */
	var stackCommandFactory={
			pointerMoveCommand: function(nextCmd,previousCmd){
				var cmd,changeObj;
				changeObj=m_ruleSetCommand.createChangeObj("","","",nextCmd,previousCmd);
				cmd=m_ruleSetCommand.createCommand(
						"CommandStack.Move.Pointer",
						false,changeObj,
						"Stack Pointer Moved.",
						undefined,"","","");
				return cmd;
			}
	};
	
	/*technicalRuleView*/
	var openView=function(cmdObj,viewType){
		var elementUUID=cmdObj.elementID;
		var obj=cmdObj.changes[0].value.after;
		var ruleSet = m_ruleSet.findRuleSetByUuid(cmdObj.ruleSetUUID);
		m_jsfViewManager.create().openView(viewType, "id="
				+ obj.id + "&ruleSetId="
				+ ruleSet.id + "&name="
				+ obj.name + "&uuid="
				+ obj.uuid + "&ruleSetUuid="
				+ ruleSet.uuid + "&parentUUID=" + ruleSet.uuid, obj.uuid);
	};
	
	var closeView=function(cmsObj){
		
	};
	/*Helper function to mediate changes to our portal tab name.*/
	var renameView=function(cmdObj){
		var viewType,
			newVal,
			uuid;
		switch(cmdObj.event){
		case "DecisionTable.Name.Change":
			viewType="decisionTableView";
			break;
		case "RuleSet.Name.Change":
			viewType="ruleSetView";
			break;
		case "Rule.Name.Change":
			viewType="technicalRuleView";
			break;
		}
		if(viewType){
			newVal=cmdObj.changes[0].value.after;
			uuid=cmdObj.elementID;
			m_jsfViewManager.create().updateView(viewType, "name" + "=" + newVal,uuid);
		}
	};
	
	/* Helper function to apply changes that we wish to effect secondarily.
	 * For example: Our primary command is to rename a ruleset. However, a secondary
	 * effect of that would be that we need to change the ruleSet portal tab name as well.
	 * This is where those effects are implemented. Generally, we effect things here that
	 * are out of the domain of our views. Portal UI elements, 
	 * top level Objects (Window.top.RuleSets)etc . We would not manipulate DOM elements of
	 * individual views here. That is what our event communication is for.
	 *  etc...)*/
	var applyToRuleSet=function(cmdObj){
		if(cmdObj.nameSpace==="RuleSet"){
			var rSet=m_ruleSet.findRuleSetByUuid(cmdObj.ruleSetUUID);
			var tempObj;
			var cmdVal=cmdObj.changes[0].value.after;
			console.log("Applying command to ruleset");
			console.log(cmdObj);
			console.log(rSet);
			switch (cmdObj.event){
			case "DecisionTable.Create":
				openView(cmdObj,"decisionTableView");
				rSet.isDirty=true;
				break;
			case "DecisionTable.Name.Change":
				tempObj=rSet.findDecisionTableByUuid(cmdObj.elementID);
				tempObj.name=cmdVal;
				rSet.isDirty=true;
				renameView(cmdObj);
				break;
			case "DecisionTable.Description.Change":
				tempObj=rSet.findDecisionTableByUuid(cmdObj.elementID);
				tempObj.description=cmdVal;
				rSet.isDirty=true;
				break;
			case "DecisionTable.Data.Change":
				tempObj=rSet.findDecisionTableByUuid(cmdObj.elementID);
				tempObj.setTableData($.extend(true,{},cmdVal));
				rSet.isDirty=true;
				break;
			case "RuleSet.Name.Change":
				rSet.name=cmdVal;
				rSet.isDirty=true;
				renameView(cmdObj);
				break;
			case "RuleSet.Description.Change":
				rSet.description=cmdVal;
				rSet.isDirty=true;
				break;
			case "RuleSet.Fact.Change":
				rSet.parameterDefinitions=cmdVal.slice(0);
				rSet.isDirty=true;
				break;
			case "Rule.Create":
				/*add technical rule?*/
				openView(cmdObj,"technicalRuleView");
				rSet.isDirty=true;
				break;
			case "Rule.Name.Change":
				tempObj=rSet.findTechnicalRuleByUuid(cmdObj.elementID);
				tempObj.name=cmdVal;
				renameView(cmdObj);
				break;
			case "Rule.Description.Change":
				tempObj=rSet.findTechnicalRuleByUuid(cmdObj.elementID);
				tempObj.description=cmdVal;
				break;
			case "Rule.Script.Change":
				tempObj=rSet.findTechnicalRuleByUuid(cmdObj.elementID);
				tempObj.setDRL(cmdVal);
				break;
			}
		}
	};
	
	/* Three level hashmap where each leaf is an array of commands affecting the
	 * element as mapped via its hashmap path. 
	 * Structure:
	 * 			 [ruleSetUUID]
	 * 					|_
	 * 					  ['decisionTable' || 'technicalRule' || 'RuleSet']
	 * 								|_
	 * 								  [elementID]-[commands[]]
	 * 
	 * */
	var ruleSetStack =function(){
		  this.stack={};
		  this.redo=function(obj){
			  var commandStack;
			  var cmdObj;
			  if(this.stack[obj.ruleSetUUID] && 
				 this.stack[obj.ruleSetUUID][obj.elementType] && 
				 this.stack[obj.ruleSetUUID][obj.elementType][obj.elementID]){
				  
				  commandStack=this.stack[obj.ruleSetUUID][obj.elementType][obj.elementID];
				  if(commandStack.pointer < commandStack.commands.length-1){
					  commandStack.pointer=commandStack.pointer+1;
					  cmdObj=commandStack.commands[commandStack.pointer];
				  }
			  }
			  return cmdObj;
		  };
		  
		  this.undo=function(obj){
			  var commandStack;
			  var cmdObj;
			  var bizarroCmdObj; /* mirrored commandObjects for Create and Delete undos.*/
			  if(this.stack[obj.ruleSetUUID] && 
				 this.stack[obj.ruleSetUUID][obj.elementType] && 
				 this.stack[obj.ruleSetUUID][obj.elementType][obj.elementID]){
				  
				  commandStack=this.stack[obj.ruleSetUUID][obj.elementType][obj.elementID];
				  if(commandStack.pointer > 0){
					  commandStack.pointer=commandStack.pointer-1;
					  cmdObj=commandStack.commands[commandStack.pointer];
				  }
			  }
			  console.log(cmdObj);
			  
			  return retObj;
		  };
		  
		  /*Push a command object onto the appropriate stack and move pointer to the top.*/
		  this.push=function(obj){
		    if(this.stack.hasOwnProperty(obj.ruleSetUUID)===false){
		      this.stack[obj.ruleSetUUID]={};
		    }
		    
		    if(this.stack[obj.ruleSetUUID].hasOwnProperty(obj.elementType)===false){
		      this.stack[obj.ruleSetUUID][obj.elementType]={};
		    }
		    
		    if(this.stack[obj.ruleSetUUID][obj.elementType].hasOwnProperty(obj.elementID)===false){
		      this.stack[obj.ruleSetUUID][obj.elementType][obj.elementID]={"commands":[],pointer:0};
		    }
		     this.stack[obj.ruleSetUUID][obj.elementType][obj.elementID].commands.push(obj);
		     this.stack[obj.ruleSetUUID][obj.elementType][obj.elementID].pointer=this.stack[obj.ruleSetUUID][obj.elementType][obj.elementID].commands.length-1;
		  };
		};
		
		/*Simple stack with no hashing.All commands go on the same stack
		 * 	[cmd,cmd,cmd,cmd,...n]
		 * */
		var globalRuleSetStack=function($sink){
			var $sinkRef=$sink;
			this.stack=[];
			this.pointer=0;
			/*TODO: [ZZM] Extract and normalize common code between undo and redo functions*/
			this.redo=function(obj){
				  var cmdObj,nextCmd, prevCmd;
				  if(this.pointer < this.stack.length-1){
					  this.pointer=this.pointer+1;
					  cmdObj=this.stack[this.pointer];
					  nextCmd=this.stack[this.pointer+1];/*allow undefined on out of bounds*/
					  prevCmd=this.stack[this.pointer-2];/*allow undefined on out of bounds*/
					  pntrCmd=stackCommandFactory.pointerMoveCommand(nextCmd,prevCmd);
					  $sinkRef.trigger(pntrCmd.nameSpace,[pntrCmd]);
				  }
				  applyToRuleSet(cmdObj);
				  return cmdObj;
			  };	
			  
			this.undo=function(obj){
				  var cmdObj,retObj;
				  if(this.pointer >0){
					  this.pointer=this.pointer-1;
					  cmdObj=this.stack[this.pointer];
					  nextCmd=this.stack[this.pointer+2];/*allow undefined on out of bounds*/
					  prevCmd=this.stack[this.pointer-1];/*allow undefined on out of bounds*/
					  pntrCmd=stackCommandFactory.pointerMoveCommand(nextCmd,prevCmd);
					  $sinkRef.trigger(pntrCmd.nameSpace,[pntrCmd]);
				  }
				  applyToRuleSet(cmdObj);
				  retObj=cmdObj;
				  if(cmdObj.event==="Rule.Create"){
					  var ruleSet=m_ruleSet.findRuleSetByUuid(cmdObj.ruleSetUUID);
					  var rule=ruleSet.technicalRules[cmdObj.elementID];
					  bizarroCmdObj=m_ruleSetCommand.ruleDeleteCmd(ruleSet,rule,undefined,undefined);
					  retObj=bizarroCmdObj;
				  }
				  else if(cmdObj.event==="DecisionTable.Create"){
					  var ruleSet=m_ruleSet.findRuleSetByUuid(cmdObj.ruleSetUUID);
					  var decTable=ruleSet.decisionTables[cmdObj.elementID];
					  bizarroCmdObj=m_ruleSetCommand.decTableDeleteCmd(ruleSet,decTable,undefined,undefined);
					  retObj=bizarroCmdObj;
				  }
				  return retObj;
			  };
			  
			 this.push=function(obj){
				 this.stack.push($.extend({},obj));
				 renameView(obj);
				 this.pointer=this.stack.length-1;
				 nextCmd=undefined;
				 prevCmd=this.stack[this.pointer-1];/*allow undefined on out of bounds*/
				 pntrCmd=stackCommandFactory.pointerMoveCommand(nextCmd,prevCmd);
				 $sinkRef.trigger(pntrCmd.nameSpace,[pntrCmd]);
			 };
		};
		
		return {
			createHashStack: function(){
				return new ruleSetStack();
			},
			createSimpleStack: function($sink){
				return new globalRuleSetStack($sink);
			}
		};
	
});