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
package org.eclipse.stardust.ui.web.bcc.views.report;

import java.util.HashMap;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;


/**
 * @author Yogesh.Manware
 * 
 */
public class MyReportsPanelBean extends AbstractLaunchPanel
{
   private static final long serialVersionUID = 1L;
   private HashMap<String, String> favoriteReports;

   public MyReportsPanelBean()
   {
      super(ResourcePaths.V_MY_FAV_REPORT);
   }

   /**
    * open report
    */
   public void openReport(ActionEvent ae)
   {
      String documentId = (String) ae.getComponent().getAttributes().get("documentId");
      if (documentId.contains(".rptdesign"))
      {
         String documentName = favoriteReports.get(documentId);
         DocumentViewUtil.openFileSystemDocument(documentId, documentName, false);
      }
      else
      {
         DocumentViewUtil.openJCRDocument(documentId);
      }
   }

   /**
    * remove from favorite
    */
   public void removeFromFavorite(ActionEvent ae)
   {
      String documentId = (String) ae.getComponent().getAttributes().get("documentId");
      RepositoryUtility.removeFromFavorite(documentId);
      update();
   }

   public void refreshFavoriteReports()
   {
      favoriteReports = RepositoryUtility.getFavoriteReports();
   }

   /**
    * @return favorite reports
    */
   public HashMap<String, String> getFavoriteReports()
   {
      if (null == favoriteReports)
      {
         favoriteReports = RepositoryUtility.getFavoriteReports();
      }
      return favoriteReports;
   }

   @Override
   public void update()
   {
      refreshFavoriteReports();
   }
}