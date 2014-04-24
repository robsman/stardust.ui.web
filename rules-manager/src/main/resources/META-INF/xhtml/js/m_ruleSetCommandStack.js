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
			},
			stackChangeCommand: function(nextCmd,previousCmd){
				var cmd,changeObj;
				changeObj=m_ruleSetCommand.createChangeObj("","","",nextCmd,previousCmd);
				cmd=m_ruleSetCommand.createCommand(
						"CommandStack.Change.Stacks",
						false,changeObj,
						"A change has occured in the undo/redo stacks.",
						undefined,"","","");
				return cmd;
			},
			noopCommand: function(){
				cmd=m_ruleSetCommand.createCommand(
						"NOOP",
						false,{},
						"No Operation To Perform.",
						undefined,"","","");
				return cmd;
			}
	};
	
	/*Helper function to openViews.*/
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
	
	/*Wrap jsf_viewmanager functionality and centralize in our commandStack*/
	var closeView=function(cmsObj){
		//TODO:[ZZM] - stubbed
	};
	
	/*Helper function to mediate changes to our portal tab name.
	 * It will filter the event associated with the command object
	 * to determine if it should rename a view, and (if so) how it should
	 * construct the call to the viewManager.
	 * */
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
	
	/* Simple stack with no hashing. Incoming commands are put on this.stack.
	 * On an undo event the command on top of the the stack is moved to the redo stack.
	 * Likewise, on a redo command the command on top of the redo stack is moved to undo stack.
	 * Then (with several caveats-the command on top of the undo stack is triggered.) 
	 * For a command to be placed on the stack (in the first place) the cmd object must be marked as
	 * .isUndoable=true. All commands placed on the stack have this property set to false so that
	 * when they are re-triggered on undo/redo they do not filter back onto our stack as an echo.
	 * */
	var globalRuleSetStack=function($sink){
		var $sinkRef=$sink; /*Reference to the commandDispatcher we are associated with.*/
		this.stack=[];		/*our commandStack (Undo), barren as a desert, for now.*/
		this.redoStack=[];		/*Our redo stack, commands undone will push to here*/
		
		/*Given a Create command this function will generate a corresponding Delete command
		 *and vice versa. In short, the undo operation is bizarre when considering creates and 
		 *deletes. The corresponding command for each will not be on our stack. Rather, we will have
		 *to generate these reverse commands when we come across a Create or Delete command in the context
		 *of an undo event. Care most also be taken that listeners for these inverse commands do not
		 *echo them back up to us as we really wish to pretend these reverse commands never happened after
		 *we send them down to the listeners. If they were to echo back to our commandStack it would cause quite
		 *a bit of confusion.*/
		var bizarroObjGenerator=function(cmdFuncName,cmdObj){
			ruleSet=m_ruleSet.findRuleSetByUuid(cmdObj.ruleSetUUID);
			obj=$.extend(true,{},cmdObj.changes[0].value.after);
			bizarroCmdObj=m_ruleSetCommand[cmdFuncName](ruleSet,obj,obj,undefined);
			bizarroCmdObj.isUndoable=false;/*These commands must not go onto our stack.*/
			return bizarroCmdObj;
		};
		
		/*Returns the next commands that would be triggered on either an undo or redo operation.
		 * In cases where the next command is a NOOP we will keep moving down the stack until 
		 * the next command is not a NOOP or there are no more next commands.
		 * */
		this.retrieveHotStackItems=function(){
			var nextCmd,prevCmd,stackChangeCmd,i=1;
			nextCmd=this.redoStack[this.redoStack.length-1];
			prevCmd=this.stack[this.stack.length-2];
			while(prevCmd && prevCmd.event ==="NOOP"){
				prevCmd=this.stack[this.stack.length-(2+i)];
				i=i+1;
			}
			return {"redoNext":nextCmd , "undoNext":prevCmd};
		};
		
		/*Pops a command from our redo stack and pushes it to our undo stack. 
		 *Finally signal listeners that our command stacks (undo/redo) have changed.
		 **/
		this.popRedoStack=function(){
			var redoTop=this.redoStack.pop();
			var nextCmds,stackChangeCmd;
			if(redoTop){
				this.stack.push(redoTop);
				//applyToRuleSet(redoTop);
				redoTop.isUndoable=false;/*command is on stack already, do not push again on trigger..*/
				$sinkRef.trigger(redoTop.nameSpace,[redoTop]);
				nextCmds=this.retrieveHotStackItems();
				stackChangeCmd=stackCommandFactory.stackChangeCommand(nextCmds.undoNext,nextCmds.redoNext);
				$sinkRef.trigger(stackChangeCmd.nameSpace,[stackChangeCmd]);
			}
		};
		
		/*Dump our undo and redo stack and send a stackChange command to all listeners
		 *with undefined for nextCmd and prevCmd*/
		this.purgeStacks=function(){
			var stackChangeCmd;
			this.stack=[];
			this.redoStack=[];
			stackChangeCmd=stackCommandFactory.stackChangeCommand(undefined,undefined);
			$sinkRef.trigger(stackChangeCmd.nameSpace,[stackChangeCmd]);
		};
		
		/*Pops a command from our undo stack and pushes it to our redo stack.
		 *Afterwards, it will trigger the command on top of the undo stack, with
		 *a few caveats. Finally signal listeners that our command stacks (undo/redo)
		 *have changed.
		 * */
		this.popUndoStack=function(){
			var poppedCmd;
			var noopCmd;
			var inverseObject;
			var doTrigger =false;
			var undoTopIndex;
			var undoTopEvent;
			var retObj;
			var nextCmds,stackChangeCmd;
			var pattern= /(\.Create|\.Delete)/;
			if(this.stack.length===0){return;}
			
			poppedCmd=this.stack.pop();
			if(poppedCmd){
				if(pattern.test(poppedCmd.event)){
					//1.Create inverse command object
					if(poppedCmd.event==="Rule.Create"){
					  inverseObject=bizarroObjGenerator("ruleDeleteCmd",poppedCmd);
					}
					else if(poppedCmd.event==="Rule.Delete"){
					  inverseObject=bizarroObjGenerator("ruleCreateCmd",poppedCmd);
					}
				 	else if(poppedCmd.event==="DecisionTable.Create"){
				 	  inverseObject=bizarroObjGenerator("decTableDeleteCmd",poppedCmd);
				 	}
				 	else if(poppedCmd.event==="DecisionTable.Delete"){
				 	  inverseObject=bizarroObjGenerator("decTableCreateCmd",poppedCmd);
				 	}
					//2.Trigger inverse command on sink
					$sinkRef.trigger(inverseObject.nameSpace,[inverseObject]);
					//3.Push original command to redo stack.
					this.redoStack.push(poppedCmd);
					//4.Generate NOOP command
					noopCmd=stackCommandFactory.noopCommand();
					//5.Push Noop command to undo stack
					this.stack.push(noopCmd);
					//6. Set doTrigger===false as we are not going to trigger undoTop in this case
					doTrigger=false;
				}
				else if(poppedCmd.event !=="NOOP"){
					//We have a pure data mod event (!Create,!Delete,!NOOP)
					//1.Push command to redo stack.
					this.redoStack.push(poppedCmd);
					//2.Set doTrigger===true as we are going to trigger undoTop
					doTrigger=true;
				}
				
				if(doTrigger){
					//1.Inspect cmd currently on undoTop.
					//2.while cmd currently on undoTop is===NOOP, pop and loop
					undoTopIndex=this.stack.length-1;
					if(undoTopIndex > -1){
						undoTopEvent=this.stack[undoTopIndex].event;
						while(undoTopEvent==="NOOP"){
							this.stack.pop();//Pop and forget
							undoTopIndex=this.stack.length-1;
							undoTopEvent=this.stack[undoTopIndex].event;
						}
						//3.Now we have a non NOOP command so if it is a create or delete command recurse.
						if(pattern.test(undoTopEvent)){
							this.popUndoStack();
						}//4.If not a Create or Delete command then trigger the command
						else{
							undoTopEvent=this.stack[undoTopIndex];
							undoTopEvent.isUndoable=false;
							//applyToRuleSet(undoTopEvent);
							$sinkRef.trigger(undoTopEvent.nameSpace,[undoTopEvent]);
						}
					}
				}
				nextCmds=this.retrieveHotStackItems();
				stackChangeCmd=stackCommandFactory.stackChangeCommand(nextCmds.undoNext,nextCmds.redoNext);
				$sinkRef.trigger(stackChangeCmd.nameSpace,[stackChangeCmd]);
			}
		};	
		
		/*Basic push operation which places a command onto our stack.
		 *Ensure all commands pushed on our stack are toggled off (obj.isUndoable=false) 
		 *so that if our sink sees them again it will not push them back onto 
		 *our stack as a duplicate. Undo/Redo events will cause
		 *the sink to process commands already on our stack. After the push we signal StackChange
		 *listeners that our stacks have been modified.
		 * */
		this.push=function(obj){
			var nextCmds;
			var cmdClone;
			obj.isUndoable=false;
			cmdClone=$.extend(true,{},obj);
			this.stack.push(cmdClone);
			//applyToRuleSet(cmdClone);
			renameView(obj);/*function filters events to ignore*/
			nextCmds=this.retrieveHotStackItems();
			stackChangeCmd=stackCommandFactory.stackChangeCommand(nextCmds.undoNext,nextCmds.redoNext);
			$sinkRef.trigger(stackChangeCmd.nameSpace,[stackChangeCmd]);
		};
	};
	
	return {
		createSimpleStack: function($sink){
			return new globalRuleSetStack($sink);
		}
	};
	
});