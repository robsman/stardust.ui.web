/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Zachary Z McCain
 */

(function() {
	
	'use strict';
	
	/**
	 * constructor, initialize our properties we will
	 * tie to our dependency injected services and expose
	 * them to our prototypes.
	 * @param benchmarkService - Service to interact with our REST layer.
	 */
	function benchmarkController(benchmarkService,benchmarkBuilderService,sdLoggedInUserService){
		
		//Self reference
		var that = this; 
		
		//Injected dependencies we need in our functions
		this.benchmarkService = benchmarkService;
		this.benchmarkBuilderService = benchmarkBuilderService;
		
		//Function level properties
		this.benchmarkCache = {}; //cache of benchmarks pulled from server.
		this.selectedBenchmark = undefined; //Currently selected benchmark from our data table
		this.benchmarks = []; //Benchmarks pulled from the server
		this.models = []; //models used to populate our model tree
		this.treeApi = {};//model tree api returned to our callback
		this.selectedTab = "General"; //Default tab
		this.dataTableApi = {}; //Api retrieved from our dataTable directive
		this.showBenchmarks = true; //should we show the benchmarks data table
		this.benchmarkDataRows = []; //???
		this.currentUser = sdLoggedInUserService.getUserInfo(); //current user
		this.benchmarkFilter ="Design"; //Filter value for our benchmark service call.
		this.benchmarkIsDirty = false;
		this.lastSaveTime = Number.NEGATIVE_INFINITY;
		
		//Retrieve all benchmarks
		benchmarkService.getBenchmarks()
		.then(function(data){
			that.benchmarks = data;
		});
		
		//retrieve all deployed models, add additional data to help
		//in building a tree structure.
		benchmarkService.getModels()
		.then(function(data){
			that.models = that.treeifyModels(data.models);
		});
	}
	
	/**
	 * Load benchmarks from our Service, passing a status of
	 * 'Design' or 'Published' to filter.
	 * @param status - string ['Design' | 'Publish']
	 */
	benchmarkController.prototype.loadBenchmarks = function(status){
		var that = this;
		
		//Default to Design
		status = !status ? "Design" : status;
		
		//load models and reset UI
		this.benchmarkService.getBenchmarkStubs(status)
		.then(function(data){ 
			that.selectedBenchmark = undefined;
			that.benchmarks = data;
			that.dataTableApi.refresh();
		});
	};
	
	//TODO: remove
	benchmarkController.prototype.getLastSaveTime = function(){
		var currentTime = (new Date()).getTime();
		
		if(this.lastSaveTime === Number.NEGATIVE_INFINITY){
			return "Never";
		}
		else{
			return "x minutes ago";
		}
	}
	
	/**
	 * callback for the data table to handle when a benchmark has
	 * been selected.
	 * @param d
	 */
	benchmarkController.prototype.benchmarkSelected = function(d){
		var that = this;

		if(d.action==="select"){
			/*
			this.benchmarkService.getBenchmark(d.current.benchmark.id)
			.then(function(data){
				that.selectedBenchmark = data.benchmark;
			});
			*/ 
			that.selectedBenchmark = d.current.benchmark;
		}
		else if(d.action==="deselect"){
			this.selectedBenchmark = undefined;
		}
	}
	
	
	/**
	 * Given an id and a nodeType find the corresponding item within the currently selected
	 * benchmark and set it as our selected benchmark instance.
	 * @param idChain - Based on the nodeIds of our model data as transformed by the treeify function.
	 * 		  nodeType model    = modelId
	 * 		  nodeType process  = {modelId}processId
	 *        nodeType activity = {modelId}processId:activityId
	 * @param nodeType - meta data for a node in our tree [model|process|activity]
	 */
	benchmarkController.prototype.generateSelectedCategories = function(idChain, nodeType){
		console.log(id + ":" + nodeType);
		//based on the node type use the idChain to extract the correct benchmark instance
		//from our selected benchmark.
	};
	
	/**
	 * Callback function tied to our model tree through which we will
	 * receive the trees API.
	 * @param api - PI returned for our tree directive
	 */
	benchmarkController.prototype.onTreeInit = function(api){
		this.treeApi = api;
	}
	
	/**
	 * Callback function tied to our model tree through which we will
	 * mediate all of our tree events.
	 * @param d - the treeNode which was the original target of the event.
	 */
	benchmarkController.prototype.treeCallback = function(d){
		//based on the nodeType interrogate the currently selected benchmark (if any)
		//to determine any existing benchmarks, if not present the empty ui.
		if(d.treeEvent==="node-click" && this.selectedBenchmark){
			this.buildOutBenchmark(this.selectedBenchmark,d);
		}
		d.deferred.resolve();
		console.log(d);
	}
	
	/**
	 * Given a benchmark JSON structure and nodePath, filter the benchmarks model
	 * array for the model instance identifies in the nodePath. If no model instance
	 * is found then push a new model instance onto the array using the specified id.
	 * @param benchMark
	 * @param nodePath
	 * @returns
	 */
	benchmarkController.prototype.buildOutModel = function(benchmark,modelId,treeModel){
		var model,
			searchArray;
		
		searchArray = benchmark.models.filter(function(v){
			return (v.id === modelId );
		});
		
		if(searchArray.length ===0){
			model = this.benchmarkBuilderService.getBaseModel();
			model.id = treeModel.id;
			model.oid = treeModel.oid
			benchmark.models.push(model);
		}
		else{
			model = searchArray[0];
		}
		
		return model;
	}
	
	/**
	 * Given a model filter its processDefinitions array for the processDefinition
	 * present in the nodePath parameter. If found return that value, if not found
	 * use our builder service to create a new structure and add it to the models
	 * array.
	 * @param model
	 * @param nodePath
	 * @returns
	 */
	benchmarkController.prototype.buildOutProcDef = function(model,procDefId,treeProcDef){
		var searchArray,
			procDef;
		
		searchArray = model.processDefinitions.filter(function(v){
			return (v.id === procDefId);
		});
		
		if(searchArray.length === 0){
			procDef = this.benchmarkBuilderService.getBaseProcessDefinition();
			procDef.id = treeProcDef.id;
			procDef.oid = treeProcDef.oid;
			model.processDefinitions.push(procDef);
		}
		else{
			procDef = searchArray[0];
		}
		
		return procDef;
	}
	
	/**
	 * Given a procDef filter its activities array for the activity
	 * present in the nodePath parameter. If found return that value, if not found
	 * use our builder service to create a new structure and add it to the models
	 * @param procDef
	 * @param nodePath
	 * @returns
	 */
	benchmarkController.prototype.buildOutActivity = function(procDef,activityId,treeActivity){
		var searchArray,
			activity;

		searchArray = procDef.activities.filter(function(v){
			return (v.id === activityId);
		});
		
		if(searchArray.length === 0){
			activity = this.benchmarkBuilderService.getBaseActivity();
			activity.id = treeActivity.id;
			activity.oid = treeActivity.oid;
			procDef.activities.push(activity);
		}
		else{
			activity = searchArray[0];
		}
		
		return activity;
	}
	
	benchmarkController.prototype.buildOutBenchmark = function(benchmark,item){
		var searchArray,
			model,
			parentModel,
			parentProcDef,
			procDef,
			activity;
		
		//Model level clicks should only build out a new model element
		// in the parent benchmark's models array, no benchmarkData at
		//this level.
		if(item.valueItem.nodeType==="model"){
			this.buildOutModel(benchmark,item.valueItem.id,item.valueItem);
		}
		
		//procDef clicks should build out model/procDef structures as
		//needed and enter a row into our benchmarkDataRows collection.
		else if(item.valueItem.nodeType==="process"){
			
			parentModel = this.treeApi.getParentItem(item.valueItem.nodeId);
			model = this.buildOutModel(benchmark,parentModel.id,parentModel);
			procDef = this.buildOutProcDef(model,item.valueItem.id,item.valueItem);
			
			this.benchmarkDataRows.push({
				"benchmark" : benchmark, 
				"element" : "Process Definition",
				"nodePath" : "{" + model.id + "}" + procDef.id, 
				"benchmarkData": procDef.benchmarkData});
		}
		
		//Activity clicks should build out model/procDef/activity structures as
		//needed and enter a row into our benchmarkDataRows collection.
		else if(item.valueItem.nodeType==="activity"){
			
			parentProcDef = this.treeApi.getParentItem(item.valueItem.nodeId);
			parentModel = this.treeApi.getParentItem(parentProcDef.nodeId);
			
			model = this.buildOutModel(benchmark,parentModel.id,parentModel);
			procDef = this.buildOutProcDef(model,parentProcDef.id,parentProcDef);
			activity = this.buildOutActivity(procDef,item.valueItem.id,item.valueItem);
			
			this.benchmarkDataRows.push({
				"benchmark" : benchmark, 
				"element" : "Activity",
				"nodePath" : "{" + model.id + "}" + procDef.id + ":" + activity.id, 
				"benchmarkData": activity.benchmarkData});
		}
	}
	
	/**
	 * Callback function tied to our model tree through which we will
	 * provide the icon css classes for our tree nodes.
	 * @param d - the data object tied to a node in our tree.
	 * @param e - null
	 * @returns {String}
	 */
	benchmarkController.prototype.iconCallback = function(d,e){
		var iconCss = "";
		
		//TODO-ZZM: need appropriate icons
		if(d.nodeType === "model"){
			iconCss = "sc sc-wrench";
		}
		else if(d.nodeType === "process"){
			iconCss = "sc sc-cog";
		}
		else{
			iconCss = "sc sc-cogs";
		}
		
		return iconCss;
	};
	
	/**
	 * Get the default benchmark JSON and set it as our selected benchmark.
	 * Update default values for author/name/last modified
	 */
	benchmarkController.prototype.createBenchmark = function(){
		
		var bmark = this.benchmarkBuilderService.getBaseBenchmark();
		
		//Default values
		bmark.benchmark.author = this.currentUser.displayName;
		bmark.benchmark.name = "Default Benchmark"; //TODO i18N
		bmark.benchmark.lastModified = (new Date()).toString();
		bmark.benchmark.validFrom = (new Date()).toString();
		
		this.addToBenchmarks(bmark);
	};
	
	/**
	 * adds a benchmark to our controllers benchmarks array. Checks first for
	 * name collisions and appends a numeral to the name in that event.
	 */
	benchmarkController.prototype.addToBenchmarks = function(bmark){
		this.benchmarks.push(bmark);
		this.dataTableApi.refresh();
	}
	
	/**
	 * Given the raw data from our getModels call (for example)
	 * add additional properties to help guide our tree structure.
	 * @param data - model data from our service call
	 * @returns data - modified model data such that each level now
	 * 		contains a nodeId and a nodeType.
	 */
	benchmarkController.prototype.treeifyModels = function(data){
		var i,j,k,tempModel,tempProcess,tempActivity;
		
		for(i = 0;i < data.length;i++){
			tempModel = data[i];
			tempModel.nodeType = "model";
			tempModel.nodeId = tempModel.id;
			for(j = 0; j < tempModel.processDefinitions.length;j++){
				tempProcess = tempModel.processDefinitions[j];
				tempProcess.nodeType = "process";
				tempProcess.nodeId = "{" + tempModel.nodeId + "}" + tempProcess.id;
				for(k = 0;k<tempProcess.activities.length;k++){
					tempActivity = tempProcess.activities[k];
					tempActivity.nodeType = "activity";
					tempActivity.nodeId = tempProcess.nodeId + ":" + tempActivity.id;
				}//activity loop end
			}//process loop end
		}//model loop end
		
		return data;
	};
	
	//angular dependencies
	benchmarkController.$inject = ["benchmarkService","benchmarkBuilderService","sdLoggedInUserService"];
	
	//add controller to our app
	angular.module("benchmark-app")
	.controller("benchmarkCtrl",benchmarkController);
	

	
})();