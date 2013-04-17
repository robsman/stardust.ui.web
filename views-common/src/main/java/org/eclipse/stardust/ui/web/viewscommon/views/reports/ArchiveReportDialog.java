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
package org.eclipse.stardust.ui.web.viewscommon.views.reports;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;



/**
 * @author Yogesh.Manware
 * 
 */
public class ArchiveReportDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = -4885174153129315317L;
   private static final String BEAN_NAME = "archiveReportDialog";
   private String reportName;
   private String reportUri;

   public ArchiveReportDialog()
   {
      super("myReportsView");
   }

   /**
    * @return fileUploadAdminDialog object
    */
   public static ArchiveReportDialog getCurrent()
   {
      ArchiveReportDialog archiveReportDialog = (ArchiveReportDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
      return archiveReportDialog;
   }

   @Override
   public void initialize()
   {}

   /**
    * Archive Report
    * 
    * @throws Exception
    */
   public void archive() throws Exception
   {
      String birtSessionId = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(
            "birtSessionId");

      HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
            .getRequest();
      String saveReportUrl = DocumentMgmtUtility.getReportingBaseURL() + "/frameset";

      String userPartition = SessionContext.findSessionContext().getUser().getPartitionId();
      if (!PredefinedConstants.DEFAULT_PARTITION_ID.equalsIgnoreCase(userPartition))
      {
         saveReportUrl += "/" + userPartition;
      }

      // read the PDF into a byte array
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      URLConnection conn = new URL(saveReportUrl).openConnection();
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Cookie", "JSESSIONID=" + request.getSession().getId());

      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      String requestParameters = "__report=" + reportUri    
            + "&__format=pdf&__pageoverflow=0&__asattachment=true&__overwrite=false";
      if (StringUtils.isNotEmpty(birtSessionId))
      {
         requestParameters += "&__sessionId=" + birtSessionId;
      }
      wr.write(requestParameters);
      wr.flush();
      wr.close();

      InputStream in = conn.getInputStream();
      int b;
      while ((b = in.read()) != -1)
      {
         out.write(b);
      }
      out.close();
      in.close();

      String reportFolderPath = DocumentMgmtUtility.getMyArchivedReportsPath();
      String msgKey = DocumentMgmtUtility.isFileNameValid(reportFolderPath, reportName);

      if (msgKey != null)
      {
         RepositoryUtility.showErrorPopup(msgKey, reportName, null);
      }
      else
      {
         Folder reportFolder = DocumentMgmtUtility.createFolderIfNotExists(reportFolderPath);
         DocumentMgmtUtility.createDocument(reportFolder.getId(), reportName, out.toByteArray(), null,
               MimeTypesHelper.PDF.getType(), null, null, null, null);
         closePopup();
      }
   }

   public void setReportUri(String reportUri)
   {
      this.reportUri = reportUri;
   }

   public String getReportName()
   {
      return reportName;
   }

   public void setReportName(String reportName)
   {
      this.reportName = reportName;
   }
}
