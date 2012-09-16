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
		initialize : function(modelId, processId) {
			var view = new ProcessReport();

			m_model.loadModels();

			view.initialize(m_model.findModel(modelId).processes[processId]);
		}
	};

	/**
	 * 
	 */
	function ProcessReport() {
		/**
		 * 
		 */
		ProcessReport.prototype.initialize = function(process) {
			this.process = process;

			this.reportHeading = jQuery("#reportHeading");
			this.processDescription = jQuery("#processDescription");
			this.annotationsTableBody = jQuery("#annotationsTable tbody");
			this.activitiesList = jQuery("#activitiesList");

			this.reportHeading.append("Process " + this.process.name);

			this.processDescription.append(this.process.description);

			if (this.process.attributes["documentation:annotations"] != null) {
				var annotations = jQuery
						.parseJSON(this.process.attributes["documentation:annotations"]);

				for ( var n = 0; n < annotations.length; ++n) {
					var annotation = annotations[n];
					
					annotation.timestamp = new Date(
							annotation.timestamp);
					
					var row = "<tr>";

					row += "<td>";
					row += m_utils.formatDate(annotation.timestamp,
							"n/j/Y  H:i:s");
					row += "</td>";
					row += "<td>";
					row += annotation.userFirstName + " "
							+ annotation.userLastName
					row += "</td>";
					row += "<td>";
					row += annotation.content;
					row += "</td>";
					
					row += "</tr>";

					this.annotationsTableBody.append(row);
				}
			}
		};

		/**
		 * 
		 */
		ProcessReport.prototype.toString = function() {
			return "Lightdust.ProcessReport";
		};
	}
});