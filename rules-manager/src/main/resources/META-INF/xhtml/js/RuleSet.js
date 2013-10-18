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
		"rules-manager/js/m_uuid", 
		"rules-manager/js/Rule","rules-manager/js/m_technicalRule",
		"rules-manager/js/m_decisionTable","bpm-modeler/js/m_model",
		"rules-manager/js/hotDecisionTable/m_typeParser",
		"rules-manager/js/m_ruleSetParser",
		"rules-manager/js/m_stateFactory"], function(m_utils, m_constants, m_command,
		m_commandsController, m_dialog, m_urlUtils, m_communicationController,
		Uuid, Rule,TechnicalRule,DecisionTable,m_model,typeParser,m_ruleSetParser,m_stateFactory) {

	return {
		create : function(id, name) {
			var ruleSet = new RuleSet();
			var state=m_stateFactory.create(false,false,false);
			ruleSet.initialize(id, name);
			ruleSet.state=state;
			getRuleSets()[ruleSet.uuid] = ruleSet;
			return ruleSet;
		},
		createFromJSON : function(data,serializer){
			return m_ruleSetParser.fromPreDRLformat(data,serializer,new RuleSet());
		},
		markRuleSetForDeletion: function(uuid){
			var rSet= getRuleSets()[uuid];
			rSet.state.isDeleted=true;
		},
		deleteRuleSet : function(uuid) {
			var rSet= getRuleSets()[uuid];
			rSet.state.isDeleted=true;
			if(window.top.ruleSets.hasOwnProperty(uuid)){
				delete window.top.ruleSets[uuid];
				console.log("Hard deletion of ruleSet: "+ uuid);
			}
		},
		getRuleSets : getRuleSets,
		emptyRuleSets : emptyRuleSets,
		getRuleSetsCount : function() {
			var count = 0;
			for ( var i in getRuleSets()) {
				count++;
			}
			return count;
		},
		findRuleSetByUuid : function(uuid) {
			return getRuleSets()[uuid];
		},
		findRuleByUuid : function(uuid) {
			for ( var ruleSetUuid in getRuleSets()) {
				var rule = getRuleSets()[ruleSetUuid].findRuleByUuid(uuid);

				if (rule) {
					return rule;
				}
			}
			return null;
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
				Rule.typeObject(json.rules[id]);
			}

			return json;
		}
	};

	/**
	 * 
	 */
	function getRuleSets(force) {
		if (!force && window.top.ruleSets) {
			return window.top.ruleSets;
		}
		refreshRuleSets();
		return window.top.ruleSets;
	}

	/**
	 * 
	 */
	function emptyRuleSets() {
		window.top.ruleSets = {};
	}
	
	/**
	 * 
	 */
	function refreshRuleSets() {
		m_communicationController.syncGetData({
			url : m_urlUtils.getContextName() + "/services/rest/rules-manager/rules/" + new Date().getTime() + "/rule-sets"
		}, {
			"success" : function(json) {
				/********/
				var key,
					ruleSetHashMap={},
					tempRset,
					jsonRset,
					defaultSerializer={method:"JSON.stringify",version:"0.0"};
				
				for(key in json){
					if(json.hasOwnProperty(key)){
						jsonRset=json[key];
						tempRset=m_ruleSetParser.fromPreDRLformat(jsonRset,defaultSerializer,new RuleSet());
						ruleSetHashMap[key]=tempRset;
					}
				}
				window.top.ruleSets = ruleSetHashMap;
				return
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
			this.uuid = Uuid.generate();
			this.id = id;
			this.name = name;
			this.creationDate = new Date();
			this.lastModificationDate = new Date();
		};
		RuleSet.prototype.addDecisionTable=function(id,name){
			var uuid=Uuid.generate();
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
			var uuid=Uuid.generate();
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
		
		
		RuleSet.prototype.addRule = function(id, name) {
			var uuid = Uuid.generate();
			var rule = Rule.create(this, uuid, id, name);

			this.rules[uuid] = rule;

			return rule;
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