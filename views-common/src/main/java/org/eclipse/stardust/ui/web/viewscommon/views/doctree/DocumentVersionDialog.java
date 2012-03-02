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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DefaultColumnModelEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRVersionTracker;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DocumentVersionDialog extends PopupUIComponentBean
{

   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "documentVersionDialog";
   private SortableTable<DocumentVersion> docVersionTable;

   private String documentName;

   private MessagesViewsCommonBean propsBean;

   /**
    * Default Constructor
    */
   public DocumentVersionDialog()
   {
      super("documentView");

      propsBean = MessagesViewsCommonBean.getInstance();

      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colVersionNo = new ColumnPreference("VersionNo", "versionNo", ColumnDataType.NUMBER, propsBean
            .getString("views.documentView.documentVersion.versionNo"));
      colVersionNo.setColumnContentUrl(ResourcePaths.V_DOCUMENT_VERSION_COLUMNS);
      colVersionNo.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colDocName = new ColumnPreference("DocumentName", "documentName", propsBean
            .getString("views.documentPanelView.documentPropertiesPanel.DocumentName"),
            ResourcePaths.V_DOCUMENT_VERSION_COLUMNS, true, true);
      colDocName.setColumnAlignment(ColumnAlignment.CENTER);
      
      ColumnPreference colAuthor = new ColumnPreference("Author", "author", propsBean
            .getString("views.documentPanelView.documentPropertiesPanel.author"),
            ResourcePaths.V_DOCUMENT_VERSION_COLUMNS, true, true);
      colAuthor.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colModifiedDate = new ColumnPreference("ModifiedDate", "modifiedDate", propsBean
            .getString("views.documentView.documentVersion.modifiedDate"),
            ResourcePaths.V_DOCUMENT_VERSION_COLUMNS, true, true);
      colModifiedDate.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colComments = new ColumnPreference("Comments", CommonProperties.COMMENTS, propsBean
            .getString("views.myDocumentsTreeView.saveDocumentVersionDialog.comments"),
            ResourcePaths.V_DOCUMENT_VERSION_COLUMNS, true, true);
      colComments.setColumnAlignment(ColumnAlignment.CENTER);

      cols.add(colVersionNo);
      cols.add(colDocName);
      cols.add(colAuthor);
      cols.add(colModifiedDate);
      cols.add(colComments);
      DefaultColumnModelEventHandler columnHandler = new DefaultColumnModelEventHandler();
     
      IColumnModel columnModel = new DefaultColumnModel(cols, null, null, CommonProperties.CONTEXT_PORTAL,
            "VersionHistory", columnHandler);
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      docVersionTable = new SortableTable<DocumentVersion>(colSelecpopup, null,
            new SortableTableComparator<DocumentVersion>("versionNo", false));
      columnHandler.setNeedRefresh(false);
      docVersionTable.initialize();
      columnHandler.setNeedRefresh(true);
   }

   @Override
   public void initialize()
   {}

   /**
    * returns current instance
    * 
    * @return
    */
   public static DocumentVersionDialog getCurrent()
   {
      return (DocumentVersionDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * initiates display of document history table
    * 
    * @param document
    */
   public void open(Document document)
   {
      try
      {
         document = DocumentMgmtUtility.getDocument(document.getId());
         docVersionTable.setList(getDocumentVersionList(document));
         docVersionTable.initialize();
         this.documentName = document.getName();
         super.openPopup();
      }
      catch (Exception exception)
      {
         ExceptionHandler.handleException(exception,
               this.getMessages().getString("documentVersion.notSupported"));
      }
   }

   /**
    * prepares the Document Version list for display purpose
    * 
    * @return
    */
   public List<DocumentVersion> getDocumentVersionList(Document document)
   {
      JCRVersionTracker vt = new JCRVersionTracker(document);
      List<DocumentVersion> documentVersionList = new ArrayList<DocumentVersion>();
      Map<Integer, Document> docVersions = vt.getVersions();
      if (docVersions.size() > 0)
      {
         TreeSet<Integer> sortedVersions = new TreeSet<Integer>(docVersions.keySet());
         int version;
         DocumentVersion docVersion = null;
         String documentName = "";
         for (Iterator<Integer> iterator = sortedVersions.iterator(); iterator.hasNext();)
         {
            version = (Integer) iterator.next();
            docVersion = new DocumentVersion(version, (Document) docVersions.get(version));

            if (documentName.equals(docVersion.getDocumentName()))
            {
               docVersion.setDocumentName("");
            }
            else
            {
               documentName = docVersion.getDocumentName();
            }
            documentVersionList.add(docVersion);
         }
         Collections.reverse(documentVersionList);
      }
      return documentVersionList;
   }

   /**
    * Opens selected version
    * 
    * @param event
    */
   public void openSelectedVersion(ActionEvent event)
   {
      super.closePopup();
      DocumentVersion docVersion = (DocumentVersion) event.getComponent().getAttributes().get("documentVersion");
      DocumentViewUtil.openJCRDocument(docVersion.getDocument());
   }

   public String getDocumentName()
   {
      return documentName;
   }

   public void setDocumentName(String documentName)
   {
      this.documentName = documentName;
   }

   

   public SortableTable<DocumentVersion> getDocVersionTable()
   {
      return docVersionTable;
   }

}
