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

package org.eclipse.stardust.ui.web.processportal.interaction.iframe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.processportal.interaction.DocumentController;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry;
import org.eclipse.stardust.ui.web.processportal.interaction.IppDocumentController;
import org.eclipse.stardust.ui.web.processportal.view.ActivityPanelConfigurationBean;
import org.eclipse.stardust.ui.web.processportal.view.manual.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;

/**
 * 
 * @author Yogesh.Manware
 *
 */
public class DocumentHelper
{
  /**
   * 
   * @param interaction
   * @param inData
   */
   public static void initializeDocumentControllers(Interaction interaction,
         Map<String, ? extends Serializable> inData)
   {
      ApplicationContext context = interaction.getDefinition();

      List<DataMapping> dataMappings = context.getAllInDataMappings();

      Map<String, DocumentController> documentControllers = new HashMap<String, DocumentController>();

      for (DataMapping dm : dataMappings)
      {
         for (Entry<String, ? extends Serializable> entry : inData.entrySet())
         {
            if (entry.getKey().equals(dm.getId()))
            {
               if (ModelUtils.isDocumentType(interaction.getModel(), dm))
               {
                  DocumentController dc = new IppDocumentController(
                        (Document) entry.getValue(), dm, interaction);
                  documentControllers.put(dc.getDocument().getId(), dc);
               }
            }
         }
      }
      interaction.setDocumentControllers(documentControllers);
   }
   
   /**
    * 
    * @param activityInstance
    * @param activityPanel
    */
   public static void openMappedDocuments(ActivityInstance activityInstance, View activityPanel)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(
            facesContext, InteractionRegistry.BEAN_ID);
      Interaction interaction = registry.getInteraction(Interaction.getInteractionId(activityInstance));

      for (Entry<String, DocumentController> documentController : interaction.getDocumentControllers()
            .entrySet())
      {
         if (ActivityPanelConfigurationBean.isAutoDisplayMappedDocuments())
         {
            PortalApplication.getInstance().setFocusView(activityPanel);
            documentController.getValue().openDocument(true);
         }
      }
      PortalApplication.getInstance().setFocusView(activityPanel);
   }

}
