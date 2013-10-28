define(["rules-manager/js/m_ruleSet",
        "rules-manager/js/m_technicalRule",
        "rules-manager/js/m_decisionTable",
        "rules-manager/js/m_stateFactory"],
        function(m_RuleSet,m_TechnicalRule,
        		m_DecisionTable,m_stateFactory){
		
	/* m_RuleSet is not being injected through the define function. Current
	 * workaround is to simply inject the object we need through our function call(s)*/
	var __fromPreDRLFormat=function(data,ver,RS){
		var rSet={},
			txColHdrs,
			key,       /*Key var for our for loop*/
			tempRule,  /*temp techrule obj for - for loop*/
			tempTable, /*temp dectable obj for -for loop*/
		    l_RuleSet=m_RuleSet || RS; /*hack for undefined m_RuleSet dependency*/
		
		switch(ver){
			case "0.1":
				rSet.uuid=data.uuid;
				rSet.id=data.id;
				rSet.name=data.name;
				rSet.description=data.description;
				rSet.creationDate=data.creationDate;
				rSet.lastModificationDate=data.lastModificationDate;
				rSet=$.extend({},RS,rSet);
				rSet.parameterDefinitions=data.facts;
				for(key in data.rules){
					if(data.rules.hasOwnProperty(key)){
						tempRule=data.rules[key];
						rSet.technicalRules[key]=$.extend(m_TechnicalRule.create(),tempRule);
					}
				}
				for(key in data.decisionTables){
					if(data.decisionTables.hasOwnProperty(key)){
						tempTable=data.decisionTables[key];
						rSet.decisionTables[key]=$.extend(m_DecisionTable.create(),tempTable);
					}
				}
				break;
			default:
				rSet={"Error": "Unsupported Version",
					  "data": data,
					  "ver": ver};
		}
		return rSet;
	};
	
	var fromJSONStringify=function(data,ver,RS){
		var key;
		var rSet;
		switch (ver){
			case "0.0":
				rSet=$.extend({},RS,data);
				for(key in rSet.technicalRules){
					if (rSet.technicalRules.hasOwnProperty(key)){
						rSet.technicalRules[key]=$.extend(m_TechnicalRule.create(),rSet.technicalRules[key]);
					}
				}
				for(key in rSet.decisionTables){
					if (rSet.decisionTables.hasOwnProperty(key)){
						rSet.decisionTables[key]=$.extend(m_DecisionTable.create(),rSet.decisionTables[key]);
					}
				}
				break;
			default:
				rSet={"Error": "Unsupported Version",
					  "data": data,
					  "ver": ver};
		}
		return rSet;
	};
	
	return {
		/*return JSON string primed for parsing into DRL*/
		fromPreDRLformat: function(data,serializer,RS){
			var rSet,state;
			/*data.serializer has precedence when present*/
			serializer=data.serializer || serializer;
			
			switch (serializer.method){
				case "m_ruleSetParser.toPreDRLFormat":
					rSet=__fromPreDRLFormat(data,serializer.version,RS);
					break;
				case "JSON.stringify":
					rSet=fromJSONStringify(data,serializer.version,RS);
					break;
				default:
					rSet={"Error": "Unsupported Method",
						  "data": data,
						  "serializer": serializer};
			}
			/*set initial state of ruleset*/
			state=m_stateFactory.create(true,false,false);
			rSet.state=state;
			return rSet;
		},
		toPreDRLFormat: function(rSet){
			var decTableCounter, /*number of decision tables in our ruleSet*/
				i,				 /*a counter for our 'for' loop*/
				tempTable,		 /*loop var for decision tables*/
				tempData,		 /*loop var for decision table.data*/
				tempRule,		 /*loop var for technicalRules*/
				tempOp,			 /*loop var for operators*/
				hashCode,		 /*computed hashcode used to look up operator objects*/	
				opCounter,		 /*number of operators we will loop over*/
				drlOps=[],		 /*array to hold operators corresponding to our colHeaders*/
				key,			 /*key var to use in for-in loops*/
				version="0.1",   /*version of the serializer code, included with data for reconstitution.*/
				data;			 /*our return object we collect all our data into*/	
			
			/*basic initialization of our return object*/
			data={  uuid: rSet.uuid,
					id: rSet.id,
					name: rSet.name,
					description: rSet.description,
					creationDate: rSet.creationDate,
					lastModificationDate: (new Date()).toString(),
					facts: {},
					rules: {},
					decisionTables: {}
				};
			
			/*Set our parameter Definitions*/
			data.facts= rSet.parameterDefinitions;
			
			/*Set our Technical Rules*/
			data.rules=rSet.technicalRules;
			for(key in rSet.technicalRules){	
				if(rSet.technicalRules.hasOwnProperty(key)){
					tempRule=rSet.technicalRules[key];
					data.rules[key].drl=tempRule.getDRL();
				}
			}
			
			/*Set our Decision Tables*/
			for(key in rSet.decisionTables ){
				if(rSet.decisionTables.hasOwnProperty(key)){
					tempTable=rSet.decisionTables[key];
					data.decisionTables[key]=tempTable;
					tempData=tempTable.getTableData();
					opCounter=tempData.colHeaders.length;
					data.decisionTables[key].tableData={
							columns: tempData.columns,
							data: tempData.data,
							colWidths: tempData.colWidths,
							colHeaders: tempData.colHeaders
					};
				}
			}
			data.serializer={
				"version": version,
				"method" : "m_ruleSetParser.toPreDRLFormat",
				"dateTime" :  new Date().toString()
			};
			return data;
		}
	};
});