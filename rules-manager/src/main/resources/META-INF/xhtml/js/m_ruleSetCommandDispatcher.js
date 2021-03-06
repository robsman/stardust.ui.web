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
define(["bpm-modeler/js/m_jsfViewManager",
        "rules-manager/js/m_ruleSetCommandStack"],
        function(m_jsfViewManager,m_ruleSetCommandStack) {
	
	var getSinkObject=function(){
			var $sink;
			var changesCount;
			if(window.top.ruleSetCommandSink===undefined){
				$sink= $({});
				$sink.listeners=[];
				$sink.commandStack=m_ruleSetCommandStack.createSimpleStack($sink);
				
				/*Special handler for Undo operations on our ruleSet commandStack.
				 *Command objects from our command stack must be published to listeners
				 *outside the scope of our regular [RuleSet.command] trigger so as to avoid pushing the 
				 *command object retrieved from the command stack onto the top 
				 *of the commandstack as a duplicate (and resetting the stack pointer).
				 */
				$sink.on("undo.Ruleset",function(event,cmd){
					$sink.commandStack.popUndoStack();
				});
				
				$sink.on("redo",function(event,cmd){
					$sink.commandStack.popRedoStack();
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
		commandStack: function(){
			return getSinkObject().commandStack;
		},
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