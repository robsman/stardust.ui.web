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
	 */
	function benchmarkController(benchmarkService){
		
		var that = this; //Self reference
		
		this.benchmarkService = benchmarkService;
		this.selectedBenchmark = undefined;
		this.benchmarks = [];
		this.models = [];
		this.treeApi = {};
		this.benchmarkTable = {};
		
		benchmarkService.getBenchmarks()
		.then(function(data){
			that.benchmarks = data;
		});
		
		//retrieve all deployed models, add additional data to help
		//in building a tree structure.
		benchmarkService.getModels()
		.then(function(data){
			var models = that.treeifyModels(data.models);
			that.models = models;
		});
	}
	
	/**
	 * callback for the data table to handle when a benchmark has
	 * been selected.
	 * @param d
	 */
	benchmarkController.prototype.benchmarkSelected = function(d){
		if(d.action==="select"){
			this.selectedBenchmark = d.current.benchmark;
		}
		else if(d.action==="deselect"){
			this.selectedBenchmark = undefined;
		}
	}
	
	
	benchmarkController.prototype.generateSelectedCategories = function(id,nodeType){
		console.log(id + ":" + nodeType);
	};
	/**
	 * Callback function tied to our model tree through which we will
	 * receive the trees API.
	 * @param api
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
			this.generateSelectedCategories(d.valueItem.nodeId,d.valueItem.nodeType)
		}
		d.deferred.resolve();
		console.log(d);
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
	 * Given the raw data from our getModels call (for example)
	 * add additional properties to help guide our tree structure.
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
	benchmarkController.$inject = ["benchmarkService"];
	
	angular.module("benchmark-app")
	.controller("benchmarkCtrl",benchmarkController);
	

	
})();