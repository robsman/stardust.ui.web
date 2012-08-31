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
package org.eclipse.stardust.ui.web.processportal.view.worklistConfiguration;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.PreferenceStoreUtils;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadDialogAttributes;

import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.context.Resource;

/**
 * @author Yogesh.Manware
 * 
 */
public abstract class WorklistColumnConfigurationBean
{
   protected Resource fileResource;
   protected Map<String, Object> worklistConfiguration;
   protected DataTable<WorklistConfigTableEntry> participantWorkConfTable;
   List<WorklistConfigTableEntry> participantWorkConfTableEntries;

   public Map<String, Object> defaultConf;

   public abstract void initializeFileResource();

   public void importConfiguration()
   {
      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getInstance();
      fileUploadDialog.initializeBean();

      FileUploadDialogAttributes attributes = fileUploadDialog.getAttributes();
      attributes.setHeaderMessage("Upload configuration TODO");
      attributes.setTitle("Upload configuration TODO21");
      attributes.setViewDescription(false);
      attributes.setViewComment(false);
      attributes.setViewDocumentType(false);
      attributes.setEnableOpenDocument(false);
      attributes.setShowOpenDocument(false);
      fileUploadDialog.setCallbackHandler(new FileUploadCallbackHandler()
      {
         public void handleEvent(FileUploadEvent eventType)
         {
            if (eventType == FileUploadEvent.FILE_UPLOADED)
            {
               try
               {
                  loadConfiguration(getFileWrapper().getFileInfo());
               }
               catch (Exception e)
               {
               }
            }
         }
      });

      fileUploadDialog.openPopup();

   }

   private void loadConfiguration(FileInfo file)
   {
      String filePath = file.getPhysicalPath();
      InputStream inputStream = null;

      try
      {
         if (filePath.endsWith(FileUtils.ZIP_FILE))
         {
            inputStream = new FileInputStream(file.getFile());
            ServiceFactory serviceFactory = SessionContext.findSessionContext().getServiceFactory();
            PreferenceStoreUtils.loadFromZipFile(inputStream, serviceFactory);
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      finally
      {
         FileUtils.close(inputStream);
      }
      initialize();
   }

   /**
    * @return
    */
   public Resource getFileResource()
   {
      return fileResource;
   }

   public abstract void initialize();

   public abstract void add();

   public abstract void delete();

   public abstract void save();

   public abstract void reset();

   public boolean isDeleteDisabled()
   {
      boolean disabled = true;
      for (WorklistConfigTableEntry confTableEntry : participantWorkConfTableEntries)
      {
         if (confTableEntry.isSelected())
         {
            disabled = false;
            if (WorklistConfigurationUtil.DEFAULT.equals(confTableEntry.getElementOID()))
            {
               disabled = true;
               break;
            }
         }
      }
      return disabled;
   }

}
