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
package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.FormatterUtils;

/**
 * @author Abhay.Thappan
 * @version $Revision: $
 */
@DTOClass
public class DocumentVersionDTO extends AbstractDTO
{
   private static final long serialVersionUID = 1L;

   public String comments;

   public float versionNo;

   public String revisionId;

   public String documentName;

   public String author = "";

   public String modifiedDate;

   public Long userOID;

   /**
    * @param version
    * @param document
    */
   public DocumentVersionDTO(int version, Document document)
   {
      this.comments = RepositoryUtility.getVersionComment(document);
      this.versionNo = version;
      this.revisionId = document.getRevisionId();
      this.documentName = document.getName();

      User user = DocumentMgmtUtility.getOwnerOfDocument(document);

      if (null != user)
      {
         userOID = user.getOID();
         this.author = FormatterUtils.getUserLabel(user);
      }
      else if (StringUtils.isNotEmpty(document.getOwner()))
      {
         this.author = document.getOwner();
      }
      this.modifiedDate = DateUtils.formatDateTime(document.getDateLastModified());
   }
}
