/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_globalVariables", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_user", "bpm-modeler/js/m_session", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_urlUtils"],
		function(m_utils, m_globalVariables, m_constants, m_user, m_session, m_command,
				m_commandsController, m_model,
				m_dialog,
 				m_communicationController, m_i18nUtils, m_extensionManager, m_urlUtils) {

			function i18nsessionpanel() {
			  // TODO
			}

			function RecordingsController() {
				this.recordingsTable = m_utils.jQuerySelect("#recordingsTablePanel #recordingsTable");
				this.recordingDetailsTable = m_utils.jQuerySelect("#recordingDetailsPanel #recordingDetailsTable");

        this.startRecordingButton = m_utils.jQuerySelect("#startRecording");

        this.startRecordingButton.click({self: this}, function(event) {
          event.data.self.startRecording();
        });
			}

     RecordingsController.prototype.initialize = function() {
       this.fetchRecordings();
     };

     RecordingsController.prototype.fetchRecordings = function() {
       // TODO retrieve status and update UI
       var self = this;

       m_communicationController.syncGetData({
         url : m_communicationController.getEndpointUrl() + "/recordings/"
       }, {
         "success" : function(json) {
           self.populateRecordingsTables(json);
         },
         "error" : function(e) {
           // TODO show error
         }
       });
     };

     RecordingsController.prototype.populateRecordingsTables = function(recordings) {
       var recording, rowMarkup;

       this.recordingsTable.children("tbody").empty();

       for (var i in recordings) {
         recording = recordings[i];

         rowMarkup = this.renderRecording(recording);
         this.recordingsTable.append(rowMarkup);
       }
     };

     RecordingsController.prototype.renderRecording = function(recording) {
       var row = m_utils.jQuerySelect("<tr></tr>");

       row.append("<td>" + recording.recordingId + "</td><td>" + recording.affectedModels + "</td>");
       // TODO stop/delete button
       if ("active" === recording.state) {
         row.append("<td><a class='stop'><img src='../images/icons/stop.png'></a></td>");
         row.find("a.stop").click({self: this}, function(event) {
           event.data.self.stopRecording(recording);
         });
       } else {
         var controlsContainer = $("<td></td>").appendTo(row);
         $("<a class='replay'><img src='../images/icons/control_play.png'></a>").appendTo(controlsContainer);
         $("<a class='delete'><img src='../images/icons/delete.png'></a>").appendTo(controlsContainer);
         row.find("a.delete").click({self: this}, function(event) {
           event.data.self.deleteRecording(recording);
         });
         row.find("a.replay").click({self: this}, function(event) {
           event.data.self.replayRecording(recording);
         });
       }

       row.mousedown({self: this, recording: recording}, function(event) {
         event.data.self.populateRecordingDetailsTable(event.data.recording);
       });

       return row;
     };

     RecordingsController.prototype.populateRecordingDetailsTable = function(recording) {
       var self = this, step, rowMarkup;

       this.recordingDetailsTable.children("tbody").empty();

       for (var i in recording.steps) {
         step = recording.steps[i];

         rowMarkup = this.renderRecordingStep(recording, step);
         this.recordingDetailsTable.append(rowMarkup);

         rowMarkup.data('recording', recording);
         rowMarkup.data('step', step);
       }

       $("td.stepLabel", this.recordingDetailsTable).editable(function(value, settings) {
         return value;
       }, {
         event: "dblclick",
         type: "textarea",
         rows: 6,
         submit: "OK",
         callback: function(value, settings) {
           var step = $(this).parent().data("step");
           if ("notification" === step.kind) {
             step.label = value;
           }
         }
       });
       $("td.stepKind", this.recordingDetailsTable).editable(function(value, settings) {
         return value;
       }, {
         event: "dblclick",
         type: "select",
         data: {"Alert": "Alert", "Pause": "Pause"},
         submit: "OK",
         callback: function(value, settings) {
           var step = $(this).parent().data("step");
           if ("notification" === step.kind) {
             step.notificationKind = value;
           }
         }
       });
     };

     RecordingsController.prototype.renderRecordingStep = function(recording, step) {
       var row, icon;

       row = m_utils.jQuerySelect("<tr></tr>");

       if ("notification" === step.kind) {
         icon = "../images/icons/comment.png";
       } else {
         icon = "../images/icons/wrench.png";
       }

       row.append("<td><img src='" + icon + "'></td>");
       if ("notification" == step.kind) {
         row.append("<td class='stepLabel'>" + step.label + "</td><td class='stepKind'>" + (step.notificationKind || "alert") + "</td>");
       } else {
         row.append("<td class='stepLabel' colspan='2'>" + step.commandId + "</td>");
       }
       var controlsContainer = $("<td class='controls'></td>").appendTo(row);
       $("<a class='add'><img src='../images/icons/comment_add.png'></a>") //
       .appendTo(controlsContainer) //
       .click({ self : this, recording : recording, step : step }, function(event) {
         event.data.self.insertNotification(event.data.recording, event.data.step);
       });

       if ("notification" == step.kind) {
          $("<a class='delete'><img src='../images/icons/comment_delete.png'></a>")//
          .appendTo(controlsContainer) //
          .click({ self : this, recording : recording, step : step }, function(event) {
            event.data.self.deleteNotification(event.data.recording, event.data.step);
          });
       }

       return row;
     };

     RecordingsController.prototype.insertNotification = function(recording, step) {
       recording.steps.splice(recording.steps.indexOf(step), 0, {id: "n" + recording.steps.length,
         label: "To be defined ...",
         kind: "notification",
         notificationKind: "Alert"});

       this.populateRecordingDetailsTable(recording);
     };

     RecordingsController.prototype.deleteNotification = function(recording, step) {
       if ("notification" === step.kind) {
         recording.steps.splice(recording.steps.indexOf(step), 1);
       }

       this.populateRecordingDetailsTable(recording);
     };

     RecordingsController.prototype.startRecording = function() {
       m_utils.debug("===> Start new recording");

       var self = this;

       m_communicationController.syncPostData({
         url : m_communicationController.getEndpointUrl() + "/recordings/"
       }, true, {
         "success" : function(json) {
           self.fetchRecordings();
         },
         "error" : function(e) {
           // TODO show error
         }
       });
     };

     RecordingsController.prototype.replayRecording = function(recording) {
       m_utils.debug("===> Starting to replay recording ...");

       var self = this;

       var container = $('html body', m_globalVariables.get("document"));
       var overlay = $('<div style="position: absolute; top: 0px; left: 0px; width: 100%; height: 100%; z-index: 20000"></div>');
       var controlsContainer = $('<div style="margin-top: 40px; margin-left: auto; margin-right: auto; width: 200px; text-align: center;"></div>').appendTo(overlay);
       var statusBar = $('<div style="width: 200px; height: 16px; color: lightgray;"></div>').appendTo(controlsContainer);
       var btnPlay = $('<a><img title="Run" src="' + m_urlUtils.getPlugsInRoot() + 'bpm-modeler/images/icons/control_play.png"></a>').appendTo(controlsContainer);
       var btnStep = $('<a><img title="Step" src="' + m_urlUtils.getPlugsInRoot() + 'bpm-modeler/images/icons/control_fastforward.png"></a>').appendTo(controlsContainer);
       var btnPause = $('<a><img title="Pause" src="' + m_urlUtils.getPlugsInRoot() + 'bpm-modeler/images/icons/control_pause.png"></a>').appendTo(controlsContainer);
       var btnStop = $('<a><img title="Stop" src="' + m_urlUtils.getPlugsInRoot() + 'bpm-modeler/images/icons/control_stop.png"></a>').appendTo(controlsContainer);

       var notification = $('<div id="replayNotification" title="Notification" style="position: absolute; left: 30%; right: 30%; top: 30%; bottom: 30%; padding: 10px; background-color: white; color: black;"></div>').appendTo(overlay).hide();

       // closure state
       var steps = recording.steps;
       var stepIdx = 0;
       var mode = 'pause';

       btnPlay.click(function() {
         if ('pause' === mode) {
           replay('run');
         }
       });
       btnStep.click(function() {
         if ('pause' === mode) {
           replay('step');
         }
       });
       btnPause.click(function() {
         replay('pause');
       });
       btnStop.click(function() {
         // stop replay
         replay('stop');

         overlay.remove();
       });

       container.append(overlay);

       var replay = function(replayMode) {
         if (replayMode) {
           mode = replayMode;
           statusBar.text(mode);
         }

         if (('run' !== mode) && ('step' !== mode)) {
           return;
         }

         var step = steps[stepIdx];

         if ('notification' === step.kind) {
           // TODO fancier message box

           notification.html(step.label).show();
           overlay.css('opacity', '0.75').css('background-color', 'gray');

           resumeReplay('pause');
         } else {
           notification.html('').hide();
           overlay.css('opacity', '').css('background-color', '');

           step.command.path = "/sessions/changes";
           var p = m_commandsController.submitCommand(step.command, false);
           p.done(function(response) {

             // simulate redo to get live updates of added/removed elements
             response.isRedo = true;
             m_commandsController.broadcastCommand(response);

             self.maybeOpenModelElementView(response);

             resumeReplay();
           });
           p.fail(function(response) {
             // TODO better error reporting
             alert('Ooops! ' + response);
           });
         }
       };

       var resumeReplay = function(replayMode) {
         if (('stop' === mode) || ('pause' === mode)) {
           return;
         }

         stepIdx = stepIdx + 1;
         if (stepIdx >= steps.length) {
           replayMode = 'stop';
         }

         if ('step' === mode) {
           replayMode = 'pause';
         }
         if (replayMode) {
           mode = replayMode;
           statusBar.text(mode);
         }
         if (('stop' === mode) || ('pause' === mode)) {
           return;
         }

         var step = steps[stepIdx];

         var delay = (('modelElement.update' === step.commandId) || ('notification' === step.type)) ? 1000 : 1500;
         setTimeout(function() {
           replay();
         }, delay);
       };

       // actually start the replay
       replay('pause');
     };

     RecordingsController.prototype.maybeOpenModelElementView = function(cmd) {
       if (null != cmd && null != cmd.changes) {
         for ( var i = 0; i < cmd.changes.added.length; i++) {
           // Create Process
           if (m_constants.PROCESS === cmd.changes.added[i].type || "processDefinition" === cmd.changes.added[i].type) {
             var model = m_model.findModel(cmd.changes.added[i].modelId);
             var process = model.findModelElementByUuid(cmd.changes.added[i].uuid);

         var viewManager = m_extensionManager.findExtension("viewManager").provider.create();
         viewManager
             .openView(
                 "processDefinitionView",
                 "processId="
                     + encodeURIComponent(process.id)
                     + "&modelId="
                     + encodeURIComponent(model.id)
                     + "&processName="
                     + encodeURIComponent(process.name)
                     + "&fullId="
                     + encodeURIComponent(process
                         .getFullId())
                     + "&uuid="
                     + process.uuid
                     + "&modelUUID="
                     + model.uuid,
                 process.uuid);
           }
         }
       }
     };

     RecordingsController.prototype.stopRecording = function(recording) {
       var self = this;
       m_utils.debug("===> Stop recording " + recording.recordingId);

       m_communicationController.postData({
         url : m_communicationController.getEndpointUrl() + "/recordings/" + recording.recordingId + "/state"
       }, "saved", {
         "success" : function(json) {
           self.fetchRecordings();
         },
         "error" : function(e) {
           // TODO show error
         }
       });
     };

     RecordingsController.prototype.deleteRecording = function(recording) {
       var self = this;
       m_utils.debug("===> Delete recording " + recording.recordingId);

       m_communicationController.deleteData({
         url : m_communicationController.getEndpointUrl() + "/recordings/" + recording.recordingId
       }, "", {
         "success" : function(json) {
           self.fetchRecordings();
         },
         "error" : function(e) {
           // TODO show error
         }
       });
     };

			// expose the module API
      return {
        initialize : function() {
          if (m_globalVariables.get("sdWebModelerRecordingsPanel") == null) {
            i18nsessionpanel();
            m_globalVariables.set("sdWebModelerRecordingsPanel", new RecordingsController());

            m_globalVariables.get("sdWebModelerRecordingsPanel").initialize();
          }
        }
      };

		});