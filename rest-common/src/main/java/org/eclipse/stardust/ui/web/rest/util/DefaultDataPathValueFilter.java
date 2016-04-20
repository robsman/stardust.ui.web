/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.DataPathDTO;
import org.eclipse.stardust.ui.web.rest.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.response.DataPathValueDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DefaultDataPathValueFilter implements IDataPathValueFilter
{
   @SuppressWarnings("unchecked")
   @Override
   public List< ? extends AbstractDTO> filter(DataPath dataPath, Object dataValue)
   {
      List<DataPathValueDTO> dataPathValueDTOs = new ArrayList<DataPathValueDTO>();

      DataPathValueDTO dataPathValueDTO = new DataPathValueDTO();
      dataPathValueDTOs.add(dataPathValueDTO);

      Model model = ModelCache.findModelCache().getModel(dataPath.getModelOID());
      DataDetails dataDetails = model != null ? (DataDetails) model.getData(dataPath.getData()) : null;

      dataPathValueDTO.dataPath = DTOBuilder.build(dataPath, DataPathDTO.class);
      dataPathValueDTO.dataPath.name = I18nUtils.getDataPathName(dataPath);
      dataPathValueDTO.dataPath.dataType = (null!= dataDetails && null != dataDetails.getTypeId()) ? dataDetails.getTypeId() : "";

      if ((dataValue != null) && (dataValue instanceof Map))
      {
         dataPathValueDTO.value = dataValue.toString(); // TODO: should it contain a
                                                        // json string?
      }
      else if ((DmsConstants.DATA_ID_ATTACHMENTS.equals(dataPath.getData()))
            || (null != dataDetails && (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataDetails.getTypeId()) || DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST
                  .equals(dataDetails.getTypeId()))))
      {
         List<Document> documents = new ArrayList<Document>();
         if (dataValue != null)
         {
            if (dataValue instanceof Document)
            {
               documents.add((Document) dataValue);
            }
            else if (dataValue instanceof List)
            {
               documents = ((List<Document>) dataValue);
            }
         }
         
         dataPathValueDTO.documents = new ArrayList<DocumentDTO>();
         
         for (Document document : documents)
         {
            DocumentDTO documentDTO = DocumentDTOBuilder.build(document, null);
            // determine the attachment type
            Long activityInstanceOid = DmsUtils.getActivityInstanceOid(document);
            Long processInstanceOid = DmsUtils.getProcessInstanceOid(document);
            if (activityInstanceOid != null)
            {
               documentDTO.attachmentType = "activity";
               documentDTO.oid = activityInstanceOid;
            }
            else if (processInstanceOid != null)
            {
               documentDTO.attachmentType = "process";
               documentDTO.oid = processInstanceOid;
            }
            dataPathValueDTO.documents.add(documentDTO);
         }
      }
      else if(dataValue != null)
      {
         // it is primitive
         dataPathValueDTO.value = dataValue.toString();
      }
      return dataPathValueDTOs;
   }
}