/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "m_utils", "m_constants", "m_model" ], function(m_utils, m_constants,
		m_model) {
	return {
		initialize : function(modelId) {
			var view = new ModelReport();

			m_model.loadModels();
			
			view.initialize(m_model.findModel(modelId));
		}
	};

	/**
	 * 
	 */
	function ModelReport() {
		/**
		 * 
		 */
		ModelReport.prototype.initialize = function(model) {
			this.model = model;
			this.reportHeading = jQuery("#reportHeading");
			this.modelDescription = jQuery("#modelDescription");
			this.processesList = jQuery("#processesList");
			this.applicationsList = jQuery("#applicationsList");

			this.reportHeading.append("Model " + this.model.name);
			this.modelDescription.append(this.model.description);

			for ( var p in this.model.processes) {
				var process = this.model.processes[p];

				this.processesList.append("<a href='../public/processReport.html?modelId=" + model.id + "&processId=" + process.id + "'>" + process.name
						+ "</a><br/>");
			}

			for ( var a in this.model.applications) {
				var application = this.model.applications[a];

				this.applicationsList.append("<a href=''>" + application.name
						+ "</a><br/>");
			}
		};

		/**
		 * 
		 */
		ModelReport.prototype.toString = function() {
			return "Lightdust.ModelReport";
		};
	}
});