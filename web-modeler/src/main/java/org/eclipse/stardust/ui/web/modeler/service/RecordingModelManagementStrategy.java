/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.service;

import java.util.UUID;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.google.gson.Gson;

import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.ui.web.modeler.common.ModelPersistenceService;
import org.eclipse.stardust.ui.web.modeler.common.ServiceFactoryLocator;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController.ChangeJto;

/**
 *
 * @author Rainer Pielmann
 *
 */
public class RecordingModelManagementStrategy extends DefaultModelManagementStrategy
{
   StringBuffer requestBuffer = new StringBuffer();

   StringBuffer responseBuffer = new StringBuffer();

   // Gson jsonIo = new GsonBuilder().serializeNulls().create();

   Gson jsonIo = new JsonMarshaller().gson();

   @Autowired
   public RecordingModelManagementStrategy(ModelPersistenceService persistenceService,
         ServiceFactoryLocator serviceFactoryLocator)
   {
      super(persistenceService, serviceFactoryLocator);
      // TODO Auto-generated constructor stub
   }

   @Override
   public EObjectUUIDMapper uuidMapper()
   {
      if (this.eObjectUUIDMapper == null)
      {
         this.eObjectUUIDMapper = new EObjectUUIDMapper()
         {
            private long counter = 0;

            @Override
            public void empty()
            {
               super.empty();
               counter = 0;
            }

            @Override
            public String map(EObject obj)
            {
               counter++;
               UUID uuid = UUID.fromString("0-0-0-0-" + Long.toString(counter));
               uuidEObjectMap.put(uuid, obj);
               return uuid.toString();
            }

         };
      }
      return eObjectUUIDMapper;

   }

   public void handleRecording(CommandJto commandJto, ChangeJto changeJto)
   {

      String requestString = jsonIo.toJson(commandJto).toString();

      String responseString = jsonIo.toJson(changeJto).toString();

      requestBuffer.append(requestString + "\n");
      responseBuffer.append(responseString + "\n");

      DocumentInfo requestDocInfo = DmsUtils.createDocumentInfo("request.txt");
      DocumentInfo responseDocInfo = DmsUtils.createDocumentInfo("response.txt");

      requestDocInfo
            .setOwner(getServiceFactory().getUserService().getUser().getAccount());
      requestDocInfo.setContentType(MediaType.TEXT_PLAIN_VALUE);

      responseDocInfo.setOwner(getServiceFactory().getUserService().getUser()
            .getAccount());
      responseDocInfo.setContentType(MediaType.TEXT_PLAIN_VALUE);

      Document requestDocument = getDocumentManagementService().getDocument(
            MODELS_DIR + "request.txt");
      Document responseDocument = getDocumentManagementService().getDocument(
            MODELS_DIR + "response.txt");

      if (requestDocument == null)
      {
         requestDocument = getDocumentManagementService().createDocument(MODELS_DIR,
               requestDocInfo, requestBuffer.toString().getBytes(), null);
      }
      else
      {
         getDocumentManagementService().updateDocument(requestDocument,
               requestBuffer.toString().getBytes(), "UTF-8", false, "", "", false);
      }

      if (responseDocument == null)
      {
         responseDocument = getDocumentManagementService().createDocument(MODELS_DIR,
               responseDocInfo, responseBuffer.toString().getBytes(), null);
      }
      else
      {
         getDocumentManagementService().updateDocument(responseDocument,
               responseBuffer.toString().getBytes(), "UTF-8", false, "", "", false);
      }
   }

}
