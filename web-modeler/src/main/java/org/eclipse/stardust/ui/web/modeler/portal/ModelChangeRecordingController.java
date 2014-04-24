/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.portal;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.icesoft.faces.context.effects.JavascriptContext;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.ResourcePaths;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEvent.PerspectiveEventType;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.modeler.edit.recording.ModelChangeRecorder;
import org.eclipse.stardust.ui.web.modeler.edit.recording.ModelChangeRecording;

/**
 * @author Robert Sauer
 */
@Component("sdWebModelerRecordingsController")
@Scope("session")
public class ModelChangeRecordingController extends AbstractLaunchPanel implements ResourcePaths
{
   private static final Logger trace = LogManager.getLogger(ModelChangeRecordingController.class);

   private static final long serialVersionUID = -1;

   @Resource
   private ModelChangeRecorder recorder;

   private Map<String, ModelChangeRecording> recordings = new LinkedHashMap<String, ModelChangeRecording>();

   public ModelChangeRecordingController()
   {
      super("sdWebModelerRecordingsController");
   }

   @Override
   public void update()
   {
      // TODO Auto-generated method stub
   }

   @Override
   public void toggle()
   {
      super.toggle();
      if (isExpanded())
      {
         activateModelerRecordingsIframe();
      }
      else
      {
         deactivateModelerRecordingsIframe();
      }
   }

   public Iterable<ModelChangeRecording> listRecordings()
   {
      return recordings.values();
   }

   public ModelChangeRecording findRecording(String id)
   {
      return recordings.get(id);
   }

   public ModelChangeRecording startRecording()
   {
      trace.info("Starting a new model change recording ...");

      ModelChangeRecording recording = recorder.startRecording();

      recordings.put(recording.getId(), recording);

      return recording;
   }


   public ModelChangeRecording stopRecording(String id)
   {
      ModelChangeRecording recording = findRecording(id);
      if ((null != recording) && recording.isActive())
      {
         trace.info("Stopping the model change recording with ID " + id + " ...");

         recording.stopRecording();
      }

      return recording;
   }

   public ModelChangeRecording deleteRecording(String id)
   {
      ModelChangeRecording recording = findRecording(id);
      if (null != recording)
      {
         if (recording.isActive())
         {
            stopRecording(id);
         }

         trace.info("Deleting the model change recording with ID " + id + " ...");

         recordings.remove(id);
      }

      return recording;
   }

   public void repositionPanelIframe()
   {
      if (isExpanded())
      {
         activateModelerRecordingsIframe();
      }
   }

   /**
    *
    */
   protected static void activateModelerRecordingsIframe()
   {
      String activateSessionLogPanelIframeJS = "InfinityBpm.ProcessPortal.createOrActivateContentFrame('sdWebModelerRecordingsFrame', 'plugins/bpm-modeler/launchpad/modelerRecordings.html', {anchorId:'portalLaunchPanels:sdWebModelerRecordingsAnchor', width:280, height:400, maxWidth:350, maxHeight:1000, anchorYAdjustment:0, zIndex:800, noUnloadWarning: 'true'});";

      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
            activateSessionLogPanelIframeJS);
      PortalApplication.getInstance().addEventScript(activateSessionLogPanelIframeJS);

      PortalUiController.getInstance().broadcastNonVetoablePerspectiveEvent(
            PerspectiveEventType.LAUNCH_PANELS_ACTIVATED);
   }

   /**
    *
    */
   protected static void deactivateModelerRecordingsIframe()
   {
      String deactivateSessionLogPanelPanelIframeJS = "InfinityBpm.ProcessPortal.deactivateContentFrame('sdWebModelerRecordingsFrame');";

      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
            deactivateSessionLogPanelPanelIframeJS);
      PortalApplication.getInstance().addEventScript(
            deactivateSessionLogPanelPanelIframeJS);

      PortalUiController.getInstance().broadcastNonVetoablePerspectiveEvent(
            PerspectiveEventType.LAUNCH_PANELS_DEACTIVATED);
   }
}