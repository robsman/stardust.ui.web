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
/**
 * @author Marc.Gille
 */
define([ "bpm-modeler/js/m_utils", 
         "bpm-modeler/js/m_constants",
		 "bpm-modeler/js/m_command", 
		 "bpm-modeler/js/m_commandsController",
		 "bpm-modeler/js/m_dialog", 
		 "bpm-modeler/js/m_urlUtils", 
		 "bpm-modeler/js/m_communicationController",
		 "rules-manager/js/m_i18nUtils",
		 "rules-manager/js/hotDecisionTable/m_utilities", 
		 "rules-manager/js/m_technicalRule",
		 "rules-manager/js/m_decisionTable","bpm-modeler/js/m_model",
		 "rules-manager/js/hotDecisionTable/m_typeParser",
		 "rules-manager/js/m_ruleSetParser",
		 "rules-manager/js/m_stateFactory"], function(m_utils, m_constants, m_command,
		m_commandsController, m_dialog, m_urlUtils, m_communicationController, m_i18nUtils,
		m_utils,TechnicalRule,DecisionTable,m_model,typeParser,m_ruleSetParser,m_stateFactory) {

	return {
		
		create : function(id, name) {
			var ruleSet = new RuleSet();
			var state=m_stateFactory.create(false,false,false);
			ruleSet.initialize(id, name);
			ruleSet.state=state;
			//We only create design time ruleSets
			getRuleSets(false,"DESIGN")[ruleSet.uuid] = ruleSet;
			return ruleSet;
		},
		
		createFromJSON : function(data,serializer){
			return m_ruleSetParser.fromPreDRLformat(data,serializer,new RuleSet());
		},
		
		//Mode should always be DESIGN
		markRuleSetForDeletion: function(uuid,mode){
			var rSet;
			
			mode = (mode === "DESIGN" || mode ==="PUBLISHED")?mode:"DESIGN";
			
			rSet= getRuleSets(false,mode)[uuid];
			rSet.state.isDeleted=true;
		},
		
		//Specific for designTime rules only! Design time rules are saved as part of the 
		//save operation whereas runtime rules cannot be saved.
		deleteRuleSet : function(uuid) {
			var rSet= getRuleSets(false)[uuid];
			rSet.state.isDeleted=true;
			if(window.top.designTimeRuleSets.hasOwnProperty(uuid)){
				delete window.top.designTimeRuleSets[uuid];
				console.log("Hard deletion of design time ruleSet: "+ uuid);
			}
		},
		
		getRuleSets : getRuleSets,
		
		emptyRuleSets : emptyRuleSets,
		
		getRuleSetsCount : function(mode) {
			var count = 0;
			
			mode = (mode === "DESIGN" || mode ==="PUBLISHED")?mode:"DESIGN";
			
			for ( var i in getRuleSets(false,mode)) {
				count++;
			}
			return count;
		},
		
		getNextRuleSetNamePostfix : function(mode) {
			var index = 0;
			var matchFound = true;
			var rsName = m_i18nUtils.getProperty("rules.object.ruleset.name","Rule Set");
			
			mode = (mode === "DESIGN" || mode ==="PUBLISHED")?mode:"DESIGN";
			
			while (matchFound) {
				index++;
				var name = rsName + " " + index;
				var id = name.replace(/\s/g,"");
				matchFound = false;
				for ( var i in getRuleSets(false,mode)) {
					var rs = getRuleSets(false,mode)[i];
					if (rs.id == id || rs.name == name) {
						matchFound = true;
						break;
					}
				}
			}
			return index;
		},
		
		findRuleSetByUuid : function(uuid,mode) {
			mode = (mode === "DESIGN" || mode ==="PUBLISHED")?mode:"DESIGN";
			return getRuleSets(false,mode)[uuid];
		},
		
		findRuleByUuid : function(uuid,mode) {
			mode = (mode === "DESIGN" || mode ==="PUBLISHED")?mode:"DESIGN";
			for ( var ruleSetUuid in getRuleSets(false,mode)) {
				var rule = getRuleSets(false,mode)[ruleSetUuid].findRuleByUuid(uuid);

				if (rule) {
					return rule;
				}
			}
			return null;
		},
		
		//Publish a rule set from design time
		publishRuleSet : function(id, successCallback, errorCallback){
			
			var url,
				ruleSet,
				callbacks,
				options,
				data;

			//Publish URL
			url = m_urlUtils.getContextName();
			url += "/services/rest/rules-manager/rules/";
			url += new Date().getTime(); 
			url += "/rule-sets/run-time";
			
			//Set up options object
			options = {
				url : url
			};
			
			//Guard our callbacks
			successCallback = successCallback || NOOPfx;
			errorCallback = errorCallback || NOOPfx;
			
			//setup data object
			data= JSON.stringify({"ruleSetId" : id});
			
			//Set up callback object
			callbacks = {
					success : function(){alert("TODO: Success Info Dialog");},
					error : function(){alert("TODO: Error Info Dialog");}
			};
			
			debugger;
			m_communicationController.postData(options,data,callbacks);
		},
		
		typeObject : function(json) {
			m_utils.inheritMethods(json, new RuleSet());

			if (!json.creationDate) {
				json.creationDate = new Date();
			}

			if (!json.lastModificationDate) {
				json.lastModificationDate = new Date();
			}

			if (!json.parameterDefinitions) {
				json.parameterDefinitions = [];
			}

			if (!json.rules) {
				json.rules = [];
			}

			for ( var id in json.rules) {
				//Rule.typeObject(json.rules[id]);
			}

			return json;
		}
	};

	/**
	 * @param force - if true then retrieve rules from the server end-point.
	 * @param mode - DESIGN | PUBLISHED - Defaults to DESIGN
	 */
	function getRuleSets(force,mode) {
		
		//default to design if no mode specified
		mode = (mode === "DESIGN" || mode ==="PUBLISHED")?mode:"DESIGN";
		
		//If force is false and designTimeRulesets are not undefined
		if(!force &&  mode==="DESIGN" && window.top.designTimeRuleSets ){
			return window.top.designTimeRuleSets;
		}
		
		//If force is false and runTimeRulesets are not undefined
		if(!force &&  mode==="PUBLISHED" && window.top.runTimeRuleSets){
			return window.top.runTimeRuleSets;
		}
		
		//If we reach here then we are going to the server for something!
		refreshRuleSets(mode);//<-Asynch
		
		//now someone should have the updated data
		if(mode==="DESIGN"){
			return window.top.designTimeRuleSets
		}
		else if(mode==="PUBLISHED"){
			return window.top.runTimeRuleSets
		}
	}

	/**
	 * 
	 */
	function emptyRuleSets(mode) {
		//default to design if no mode specified
		mode = (mode === "DESIGN" || mode ==="PUBLISHED")?mode:"DESIGN";
		
		if(mode==="DESIGN"){
			window.top.designTimeRuleSets = {};
		}
		else if(mode==="PUBLISHED"){
			window.top.runTimeRuleSets = {};
		}
		
	}
	
	//NOOP Guard function for success and error handlers
	function NOOPfx(){};

	/**
	 * @param mode - DESIGN | PUBLISHED - determines type of rules we will receive from the server.
	 */
	function refreshRuleSets(mode) {
		var url = m_urlUtils.getContextName(),
			terminalPoint;
		
		terminalPoint = (mode==="DESIGN")? "design-time" : "run-time";
		
		url += "/services/rest/rules-manager/rules/";
		url += new Date().getTime(); 
		url += "/rule-sets/" + terminalPoint;
		
		m_communicationController.syncGetData({
			url : url
		}, {
			"success" : function(json) {

				/********/
				var key,
					ruleSetHashMap={},
					tempRset,
					i=0,
					jsonRset,
					defaultSerializer={method:"JSON.stringify",version:"0.0"};

				if(mode==="DESIGN"){
					for(key in json){
						if(json.hasOwnProperty(key)){
							jsonRset=json[key];
							tempRset=m_ruleSetParser.fromPreDRLformat(jsonRset,defaultSerializer,new RuleSet());
							ruleSetHashMap[key]=tempRset;
						}
					}
					window.top.designTimeRuleSets = ruleSetHashMap;
				}
				else if(mode==='PUBLISHED'){
					for(i=0;i < json.length ; i++){
						jsonRset = json[i];
						tempRset=m_ruleSetParser.fromPreDRLformat(jsonRset,defaultSerializer,new RuleSet());
						ruleSetHashMap[tempRset.uuid]=tempRset;
					}
					window.top.runTimeRuleSets = ruleSetHashMap;
				}
				
				/********/
			},
			"error" : function() {
				alert('Error occured while fetching rules');
			}
		});
	};
	
	/**
	 * 
	 */
	function RuleSet() {
		this.type = "ruleSet";
		this.description="";
		this.parameterDefinitions = [];
		this.rules = {};
		this.technicalRules={};
		this.decisionTables={};
		this.maxExecutions=100000;
		
		RuleSet.prototype.toJSON=function(format){
			var parsedData;
			/*TODO:JSON.stringify support? IE7 will fail*/
			/*TODO:Add default function (beware circular references)*/
			switch (format.toUpperCase()){
			case "PRE-DRL":
				parsedData=m_ruleSetParser.toPreDRLFormat(this);
				break;
			default:
				parsedData="['Not Implemented']";
			}
			return parsedData;
		};
		RuleSet.prototype.setState=function(persisted,dirty,deleted){
			this.state=m_stateFactory.create(persisted,dirty,deleted);
		};
		RuleSet.prototype.initialize = function(id, name) {
			this.uuid = m_utils.uuidV4();
			this.id = id;
			this.name = name;
			this.creationDate = new Date();
			this.lastModificationDate = new Date();
		};
		RuleSet.prototype.addDecisionTable=function(id,name){
			var uuid=m_utils.uuidV4();
			var decisionTable=DecisionTable.create(this,uuid,id,name);
			this.decisionTables[uuid]=decisionTable;
			this.state.isDirty=true;
			return decisionTable;
		};
		
		RuleSet.prototype.getDecisionTableCount=function(){
			var count = 0;
			for (var k in this.decisionTables) {
			    if (this.decisionTables.hasOwnProperty(k)) {
			       ++count;
			    }
			}
			return count;
		};
		RuleSet.prototype.deleteDecisionTable=function(id){
			if(this.decisionTables.hasOwnProperty(id)){
				this.state.isDirty=true;
				delete this.decisionTables[id];
			}
		};
		
		RuleSet.prototype.addTechnicalRule=function(id,name){
			var uuid=m_utils.uuidV4();
			var techRule=TechnicalRule.create(this,uuid,id,name);
			this.state.isDirty=true;
			this.technicalRules[uuid]=techRule;
			return techRule;
		};
		
		RuleSet.prototype.getTechnicalRuleCount=function(){
			var count = 0;
			for (var k in this.technicalRules) {
			    if (this.technicalRules.hasOwnProperty(k)) {
			       ++count;
			    }
			}
			return count;
		};
		RuleSet.prototype.deleteTechnicalRule=function(id){
			if(this.technicalRules.hasOwnProperty(id)){
				this.state.isDirty=true;
				delete this.technicalRules[id];
			}
		};
		

		/**
		 * 
		 */
		RuleSet.prototype.findRuleByUuid = function(uuid) {
			return this.rules[uuid];
		};
		
		RuleSet.prototype.findTechnicalRuleByUuid = function(uuid) {
			return this.technicalRules[uuid];
		};
		
		RuleSet.prototype.findDecisionTableByUuid = function(uuid) {
			return this.decisionTables[uuid];
		};
		/**
		 * 
		 */
		RuleSet.prototype.getRulesCount = function() {
			var count = 0;

			for ( var i in this.rules) {
				++count;
			}

			return count;
		};

		/**
		 * 
		 */
		RuleSet.prototype.getParameterDefinitionByName = function(name) {
			for ( var n = 0; n < this.parameterDefinitions.length; ++n) {
				if (this.parameterDefinitions[n].name == name) {
					return this.parameterDefinitions[n];
				}
			}

			return null;
		};
		RuleSet.prototype.generateDRLTypes=function(){
			var paramDefLength=this.parameterDefinitions.length,
				tempTypeDecl,
			    typeDecls=[],
			    results=[],
			    options={},
			    paramDef;
			for (var k in this.parameterDefinitions) {
			    if (this.parameterDefinitions.hasOwnProperty(k)) {
			    	paramDef=this.parameterDefinitions[k];
			    	tempTypeDecl=$.extend({},m_model.findTypeDeclaration(paramDef.structuredDataTypeFullId));
			    	options={
			    			direction: paramDef.direction,
			    			id: paramDef.id,
			    			dataTypeID: paramDef.structuredDataTypeFullId,
			    			name: paramDef.name
			    	};
			    	tempTypeDecl.paramDefOptions=options;
					typeDecls.push(tempTypeDecl);
			    }
			}
			results=typeParser.parseTypeDeclToDRL(typeDecls);
			return results.join("\n\n");
		};
		/**
		 * 
		 */
		RuleSet.prototype.generateDrl = function() {
			var drl = "";

			drl += "/*\n";
			drl += "*\n";
			drl += "* Last Modified:" + new Date() + "\n";
			drl += "*\n";
			drl += "*/\n\n";

			for ( var i in this.rules) {
				var rule = this.rules[i];

				drl += rule.generateDrl();
			}

			return drl;
		};
	}
});