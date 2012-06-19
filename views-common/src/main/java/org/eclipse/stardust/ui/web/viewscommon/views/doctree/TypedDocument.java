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
package org.eclipse.stardust.ui.web.viewscommon.views.doctree;

import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * @author Yogesh.Manware
 * 
 */
public class TypedDocument
{
   private ProcessInstance processInstance;
   private Document document;
   private boolean outMappingExist = false;
   private DataPath dataPath;
   private String name;
   private DataDetails dataDetails;

   /**
    * @param processInstance
    * @param dataPath
    * @param dataDetails
    */
   public TypedDocument(ProcessInstance processInstance, DataPath dataPath, DataDetails dataDetails)
   {
      this.processInstance = processInstance;
      this.dataPath = dataPath;
      this.dataDetails = dataDetails;
      this.name = I18nUtils.getDataName(dataDetails);

      Object objectDocument = ServiceFactoryUtils.getWorkflowService().getInDataPath(processInstance.getOID(),
            dataPath.getId());
      if (null != objectDocument)
      {
         this.document = (Document) objectDocument;
         if (null == this.document.getId())
         {
            this.document = null;
         }
      }
   }
   
   /**
    * @return
    */
   public DocumentType getDocumentType()
   {
      return DocumentTypeUtils.getDocumentTypeFromData((Model) ModelUtils.getModel(processInstance.getModelOID()),
            dataDetails);
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public Document getDocument()
   {
      return document;
   }

   public boolean isOutMappingExist()
   {
      return outMappingExist;
   }

   public void setOutMappingExist(boolean outMappingExist)
   {
      this.outMappingExist = outMappingExist;
   }

   public String getName()
   {
      return name;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }

   public DataPath getDataPath()
   {
      return dataPath;
   }

   public void setDataPath(DataPath dataPath)
   {
      this.dataPath = dataPath;
   }

   public void setDocument(Document document)
   {
      this.document = document;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      TypedDocument other = (TypedDocument) obj;
      if (dataDetails == null)
      {
         if (other.dataDetails != null)
            return false;
      }
      else if (!dataDetails.equals(other.dataDetails))
         return false;
      return true;
   }
}