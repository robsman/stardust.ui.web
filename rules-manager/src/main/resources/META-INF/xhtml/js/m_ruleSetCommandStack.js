define(["rules-manager/js/m_ruleSet"],function(m_ruleSet){
	
	
	/*helper function to apply changes to our actual ruleSet data*/
	var applyToRuleSet=function(cmdObj){
		if(cmdObj.nameSpace==="RuleSet"){
			var rSet=m_ruleSet.findRuleSetByUuid(cmdObj.ruleSetUUID);
			var tempObj;
			var cmdVal=cmdObj.changes[0].value.after;
			console.log("Applying command to ruleset");
			console.log(cmdObj);
			console.log(rSet);
			switch (cmdObj.event){
			case "DecisionTable.Name.Change":
				tempObj=rSet.findDecisionTableByUuid(cmdObj.elementID);
				tempObj.name=cmdVal;
				rSet.isDirty=true;
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
				break;
			case "RuleSet.Description.Change":
				rSet.description=cmdVal;
				rSet.isDirty=true;
				break;
			case "RuleSet.Fact.Change":
				rSet.parameterDefinitions=cmdVal.slice(0);
				rSet.isDirty=true;
				break;
			case "Rule.Name.Change":
				tempObj=rSet.findTechnicalRuleByUuid(cmdObj.elementID);
				tempObj.name=cmdVal;
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
			  if(this.stack[obj.ruleSetUUID] && 
				 this.stack[obj.ruleSetUUID][obj.elementType] && 
				 this.stack[obj.ruleSetUUID][obj.elementType][obj.elementID]){
				  
				  commandStack=this.stack[obj.ruleSetUUID][obj.elementType][obj.elementID];
				  if(commandStack.pointer > 0){
					  commandStack.pointer=commandStack.pointer-1;
					  cmdObj=commandStack.commands[commandStack.pointer];
				  }
			  }
			  return cmdObj;
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
		var globalRuleSetStack=function(){
			this.stack=[];
			this.pointer=0;
			
			this.redo=function(obj){
				  var cmdObj;
				  if(this.pointer < this.stack.length-1){
					  this.pointer=this.pointer+1;
					  cmdObj=this.stack[this.pointer];
				  }
				  applyToRuleSet(cmdObj);
				  return cmdObj;
			  };	
			  
			this.undo=function(obj){
				  var cmdObj;
				  if(this.pointer >0){
					  this.pointer=this.pointer-1;
					  cmdObj=this.stack[this.pointer];
				  }
				  applyToRuleSet(cmdObj);
				  return cmdObj;
			  };
			  
			 this.push=function(obj){
				 this.stack.push($.extend({},obj));
				 this.pointer=this.stack.length-1;
			 };
		};
		
		return {
			createHashStack: function(){
				return new ruleSetStack();
			},
			createSimpleStack: function(){
				return new globalRuleSetStack();
			}
		};
	
});