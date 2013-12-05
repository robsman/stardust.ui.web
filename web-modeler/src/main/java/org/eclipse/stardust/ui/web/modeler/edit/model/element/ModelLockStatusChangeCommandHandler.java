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

package org.eclipse.stardust.ui.web.modeler.edit.model.element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.stardust.model.xpdl.builder.utils.WebModelerModelManager;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

/**
 * @author rainer.pielmann
 *
 */
@CommandHandler
public class ModelLockStatusChangeCommandHandler
{
   private static final String STARDUST_SECURITY_HASH = "stardust:security:hash";
   @Resource
   private ApplicationContext springContext;

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "modelLockStatus.update")
   public void updateModelLockStatus(ModelType model, JsonObject request)
   {
      boolean readOnly = request.get("readOnly").getAsBoolean();
      String password = request.get("password").getAsString();

      boolean sucess = false;
      if (readOnly)
      {
         lockModel(model, password);
      }
      else
      {
         sucess = unlockModel(model, password);
         if ( !sucess)
         {
            throw new RuntimeException("modeler.propertyView.modelView.readOnlyPage.error.invalidPassword");
         }
      }
   }

   private boolean unlockModel(ModelType model, String password)
   {
      AttributeType attribute = AttributeUtil.getAttribute(model, STARDUST_SECURITY_HASH);
      String savedHash = attribute.getAttributeValue();
      model.getAttribute().remove(attribute);
      String xml = transformEcore2XML(model);
      String hashString = doHash(password, xml);
      if (hashString.equals(savedHash))
      {
         modelService().saveModel(model.getId());
         return true;
      }
      else
      {
         model.getAttribute().add(attribute);
         return false;
      }
   }

   private void lockModel(ModelType model, String password)
   {
      String xml = transformEcore2XML(model);
      String hashString = doHash(password, xml);
      AttributeUtil.setAttribute(model, STARDUST_SECURITY_HASH, hashString);
      modelService().saveModel(model.getId());
   }

   private String doHash(String password, String modelXML)
   {
      MessageDigest md = null;
      try
      {
         md = MessageDigest.getInstance("SHA-256");
      }
      catch (NoSuchAlgorithmException e)
      {
         throw new RuntimeException(e);
      }
      md.update((modelXML + password).getBytes());

      byte byteData[] = md.digest();

      // convert the byte to hex format method 1
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < byteData.length; i++ )
      {
         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
      }

      // convert the byte to hex format method 2
      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < byteData.length; i++ )
      {
         String hex = Integer.toHexString(0xff & byteData[i]);
         if (hex.length() == 1)
            hexString.append('0');
         hexString.append(hex);
      }
      return hexString.toString();
   }

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }

   public String transformEcore2XML(ModelType model)
   {
      try
      {
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         WebModelerModelManager mgr = new WebModelerModelManager();
         mgr.setModel(model);
         mgr.save(URI.createFileURI("temp.xpdl"), stream);
         return stream.toString("UTF8");
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not complete transformation", e);
      }
   }
}
