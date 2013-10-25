define(["bpm-modeler/js/m_jsfViewManager",
        "rules-manager/js/m_ruleSetCommandStack"],
        function(m_jsfViewManager,m_ruleSetCommandStack) {
	
	var getSinkObject=function(){
			var $sink;
			var changesCount;
			if(window.top.ruleSetCommandSink===undefined){
				$sink= $({});
				$sink.listeners=[];
				//$sink.commandStack=m_ruleSetCommandStack.createHashStack();
				$sink.commandStack=m_ruleSetCommandStack.createSimpleStack($sink);
				
				/*Special handler for Undo operations on our ruleSet commandStack.
				 *Command objects from our command stack must be published to listeners
				 *outside the scope of our regular [RuleSet.command] trigger so as to avoid pushing the 
				 *command object retrieved from the command stack onto the top 
				 *of the commandstack as a duplicate (and resetting the stack pointer).
				 */
				$sink.on("undo",function(event,cmd){
					console.log("undo recieved!");
					console.log(cmd);
					var cmdObj=$sink.commandStack.undo(cmd,$sink);
					var cmdClone=$.extend(true,{},cmdObj);/*Ensure we hide all refs to object on the stack*/
					if(cmdObj!=undefined){
						var listenerCount=$sink.listeners.length;
						while(listenerCount--){
							temp=$sink.listeners[listenerCount];
							if(cmdObj.event===temp.eventName){
								temp.ref.trigger(cmdClone.event,cmdClone);
							}
						}
					}
					console.log("UNDO STACK Command object");
					console.log(cmdObj.changes[0].value.after);
				});
				
				$sink.on("redo",function(event,cmd){
					console.log("Redo recieved!");
					console.log(cmd);
					var cmdObj=$sink.commandStack.redo(cmd,$sink);
					var cmdClone=$.extend(true,{},cmdObj);/*Ensure we hide all refs to object on the stack*/
					if(cmdObj!=undefined){
						var listenerCount=$sink.listeners.length;
						while(listenerCount--){
							temp=$sink.listeners[listenerCount];
							if(cmdObj.event===temp.eventName){
								temp.ref.trigger(cmdClone.event,cmdClone);
							}
						}
					}
					console.log("Redo STACK Command object");
					console.log(cmdObj.changes[0].value.after);
				});
				
				/*Generic handler for all ruleSet commands. Objects registered with us
				 *will trigger this event with a command object. The sink will then publish
				 *the event to all registered listeners.*/
				$sink.on("RuleSet.command",function(event,cmd){
					var temp;
					var cmdClone=$.extend(true,{},cmd);/*Ensure we hide all refs to object on the stack*/
					var listenerCount=$sink.listeners.length;
					if(cmdClone.isUndoable){
						$sink.commandStack.push(cmdClone);
					}
					while(listenerCount--){
						temp=$sink.listeners[listenerCount];
						if(cmd.event===temp.eventName){
							temp.ref.trigger(cmd.event,cmd);
						}
					}
				});
				window.top.ruleSetCommandSink=$sink;
			}
			else{
				$sink=window.top.ruleSetCommandSink;
			}
			return $sink;
	};
	
	return {
		sink: getSinkObject(),
		register: function(listener,eventName){
			var count;
			var listeners=getSinkObject().listeners;
			if($.isArray(eventName)===false){
				eventName=[eventName];
			}
			count=eventName.length;
			while(count--){
				listeners.push({
				"ref": $(listener),
				"eventName": eventName[count]});
			}
		},
		trigger: function(command){
			getSinkObject().trigger(command.nameSpace,[command]);
		}
	};
});