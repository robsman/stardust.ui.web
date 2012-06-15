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
package org.eclipse.stardust.ui.web.viewscommon.views.documentsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterDate;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPickList;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter.DataType;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter.FilterCriteria;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterPickList.RenderType;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.ISearchHandler;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler;
import org.eclipse.stardust.ui.web.common.table.export.ExportType;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.SelectProcessDialog;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.user.UserAutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.viewscommon.user.UserWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.DocumentTypeWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.DocumentVersionDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.DownloadPopupDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.OutputResource;
import org.eclipse.stardust.ui.web.viewscommon.views.documentsearch.DocumentSearchProvider.FilterAttributes;



/**
 * Class is managed bean class for Documents search UI.
 * 
 * @author Vikas.Mishra
 */
public class DocumentSearchBean extends UIComponentBean
      implements IUserObjectBuilder<DocumentSearchTableEntry>, ICallbackHandler, ViewEventHandler
{
   // ~ Constants
   // ================================================================================================
   private static final long serialVersionUID = -350521151560426925L;
   public static final String BEAN_ID = "DocumentSearchBean";
   public static final String WORKFLOW_PERSPECTIVE = "workflow-perspective";
   public static final String DOCUMENT_NAME = "documentName";
   public static final String DOCUMENT_PATH = "path";
   public static final String AUTHOR = "authorFullName";
   public static final String DOCUMENT_ID = "documentId";
   public static final String FILE_TYPE = "fileType";
   public static final String DOCUMENT_TYPE = "documentType";
   public static final String FILE_SIZE = "fileSize";
   public static final String DATE_LAST_MODIFIED = "modificationDate";
   public static final String DATE_CREATED = "createDate";
   public static final String METADATA = "metadata";
   protected static final Logger trace = LogManager.getLogger(DocumentSearchBean.class);

   // ~ Instance fields
   // ================================================================================================
   private PaginatorDataTable<DocumentSearchTableEntry, Document> documentSearchTable;
   private DocumentSearchFilterHandler filterHandler;
   private IppSortHandler sortHandler;
   private DocumentSearchProvider searchProvider;
   private boolean expandSearchCriteria = true;
   private UserAutocompleteMultiSelector autoCompleteSelector;
   private MessagesViewsCommonBean propsBean;
   private DocumentSearchTableEntry selectedDoc;
   private Set<DocumentTypeWrapper> declaredDocumentTypes;

   /**
    * Constructor DocumentSearchBean
    */
   public DocumentSearchBean()
   {
      super("documentSearchView");
      propsBean = MessagesViewsCommonBean.getInstance();
   }

   private void createTable()
   {

      declaredDocumentTypes = ModelUtils.getAllActiveDeclaredDocumentTypes();
      searchProvider = new DocumentSearchProvider(declaredDocumentTypes);

      List<ColumnPreference> documentSearchFixedCols = new ArrayList<ColumnPreference>();

      ColumnPreference nameCol = new ColumnPreference(DOCUMENT_NAME, DOCUMENT_NAME, this.getMessages().getString(
            "documentSearchTable.column.documentName"), ResourcePaths.VIEW_DOCUMENT_SEARCH_COLUMN,
            new TableDataFilterPopup(new TableDataFilterSearch()), true, true);

      ColumnPreference pathCol = new ColumnPreference(DOCUMENT_PATH, DOCUMENT_PATH, this.getMessages().getString(
            "documentSearchTable.column.documentPath"), ResourcePaths.VIEW_DOCUMENT_SEARCH_COLUMN, false, false);

      ColumnPreference authorCol = new ColumnPreference(AUTHOR, AUTHOR, this.getMessages().getString(
            "documentSearchTable.column.author"), ResourcePaths.VIEW_DOCUMENT_SEARCH_COLUMN, new TableDataFilterPopup(
            new TableDataFilterSearch()), true, true);
      authorCol.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference documentsIdCol = new ColumnPreference(DOCUMENT_ID, DOCUMENT_ID, ColumnDataType.STRING, this
            .getMessages().getString("documentSearchTable.column.documentId"), new TableDataFilterPopup(
            new TableDataFilterSearch()), false, true);

      ColumnPreference documentTypeCol = new ColumnPreference(DOCUMENT_TYPE, DOCUMENT_TYPE, ColumnDataType.STRING, this
            .getMessages().getString("documentSearchTable.column.documentType"), new TableDataFilterPopup(
            new TableDataFilterPickList(FilterCriteria.SELECT_MANY, searchProvider.getFilterAttributes()
                  .getDocumentTypes(), RenderType.LIST, 10, null)), true, true);
      documentTypeCol.setColumnAlignment(ColumnAlignment.LEFT);

      ColumnPreference fileTypeCol = new ColumnPreference(FILE_TYPE, FILE_TYPE, ColumnDataType.STRING, this
            .getMessages().getString("documentSearchTable.column.fileType"), new TableDataFilterPopup(
            new TableDataFilterSearch()), false, true);
      fileTypeCol.setColumnAlignment(ColumnAlignment.LEFT);

      ColumnPreference fileSizeCol = new ColumnPreference(FILE_SIZE, "fileSizeLabel", this.getMessages().getString(
      "documentSearchTable.column.fileSize"), ResourcePaths.VIEW_DOCUMENT_SEARCH_COLUMN, true, false);
      fileSizeCol.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference modificationDateCol = new ColumnPreference(DATE_LAST_MODIFIED, DATE_LAST_MODIFIED,
            ColumnDataType.DATE, this.getMessages().getString("documentSearchTable.column.modificationDate"),
            new TableDataFilterPopup(new TableDataFilterDate(DataType.DATE)), true, true);
      modificationDateCol.setNoWrap(true);

      ColumnPreference createDateCol = new ColumnPreference(DATE_CREATED, DATE_CREATED, ColumnDataType.DATE, this
            .getMessages().getString("documentSearchTable.column.createDate"), new TableDataFilterPopup(
            new TableDataFilterDate(DataType.DATE)), true, true);
      createDateCol.setNoWrap(true);

      ColumnPreference metadataCol = new ColumnPreference(METADATA, METADATA, this.getMessages().getString(
            "documentSearchTable.column.metadata"), ResourcePaths.VIEW_DOCUMENT_SEARCH_COLUMN, true, false);
      metadataCol.setNoWrap(true);

      List<ColumnPreference> documentSearchFixedCols2 = new ArrayList<ColumnPreference>();
      ColumnPreference actionsCol = new ColumnPreference("Actions", "", this.getMessages().getString(
            "documentSearchTable.column.actions"), ResourcePaths.VIEW_DOCUMENT_SEARCH_COLUMN, true, false);
      actionsCol.setColumnAlignment(ColumnAlignment.RIGHT);
      actionsCol.setExportable(false);
      
      documentSearchFixedCols2.add(actionsCol);

      List<ColumnPreference> documentSearchCols = new ArrayList<ColumnPreference>();
      documentSearchCols.add(pathCol);
      documentSearchCols.add(documentsIdCol);
      documentSearchCols.add(authorCol);
      documentSearchCols.add(createDateCol);
      documentSearchCols.add(modificationDateCol);
      documentSearchCols.add(fileTypeCol);
      documentSearchCols.add(documentTypeCol);
      documentSearchCols.add(fileSizeCol);
      documentSearchCols.add(metadataCol);

      documentSearchFixedCols.add(nameCol);

      IColumnModel documentSearchColumnModel = new DefaultColumnModel(documentSearchCols, documentSearchFixedCols,
            documentSearchFixedCols2, WORKFLOW_PERSPECTIVE, "documentSearch");
      TableColumnSelectorPopup colSelecPopup = new TableColumnSelectorPopup(documentSearchColumnModel);

      filterHandler = new DocumentSearchFilterHandler();
      sortHandler = searchProvider.getSortHandler();

      ISearchHandler searchHandler = searchProvider.getSearchHandler();
      documentSearchTable = new PaginatorDataTable<DocumentSearchTableEntry, Document>(colSelecPopup, null,
            filterHandler, sortHandler, this, new DataTableSortModel<DocumentSearchTableEntry>("modificationDate",
                  false));
      documentSearchTable.setRowSelector(new DataTableRowSelector("selectedRow", true));
      documentSearchTable.setISearchHandler(searchHandler);
      documentSearchTable.setISortHandler(sortHandler);
      documentSearchTable.setDataTableExportHandler(new DocumentSearchExportHandler());
   }
   
   /**
    * 
    */
   public void handleEvent(ViewEvent event)
   {

      if (ViewEventType.CREATED == event.getType())
      {
         createTable();
         initialize();
      }
   }

   /**
    * method toggle popupVisible value,also fetch process list data on visible true
    * condition.
    */
   public void openProcessesDialog(ActionEvent event)
   {
      try
      {
         DocumentSearchTableEntry doc = (DocumentSearchTableEntry) event.getComponent().getAttributes().get("row");
         doc.getProcessesDialog().openPopup();

      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   public void openDocument()
   {
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         String documentId = (String) context.getExternalContext().getRequestParameterMap().get("documentId");
         DocumentViewUtil.openJCRDocument(documentId);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }

   /**
    * 
    * @param event
    */
   public void downloadDocument(ActionEvent event)
   {
      try
      {
         DocumentSearchTableEntry doc = (DocumentSearchTableEntry) event.getComponent().getAttributes().get("row");
         DownloadPopupDialog downloadPopupDialog = DownloadPopupDialog.getCurrent();
         Document document = doc.getDocument();
         OutputResource resource = new OutputResource(document.getName(), document.getId(),
               MimeTypesHelper.DEFAULT.toString(), downloadPopupDialog,
               DocumentMgmtUtility.getDocumentManagementService(), true);
         downloadPopupDialog.open(resource);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }
   
   /**
    * 
    * @param event
    */
   public void documentVersionHistory(ActionEvent event)
   {
      try
      {
         DocumentSearchTableEntry doc = (DocumentSearchTableEntry) event.getComponent().getAttributes().get("row");
         Document document = doc.getDocument();
         DocumentVersionDialog documentVersionDialog = DocumentVersionDialog.getCurrent();
         documentVersionDialog.open(document);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }
   
   public final IppSortHandler getSortHandler()
   {
      return sortHandler;
   }

   public final void setSortHandler(DocumentSearchSortHandler sortHandler)
   {
      this.sortHandler = sortHandler;
   }

   public final DocumentSearchProvider getSearchProvider()
   {
      return searchProvider;
   }

   public final void setSearchProvider(DocumentSearchProvider searchProvider)
   {
      this.searchProvider = searchProvider;
   }

   /**
    * @return ProcessSearchBean object
    */
   public static DocumentSearchBean getCurrent()
   {
      return (DocumentSearchBean) FacesContext.getCurrentInstance().getApplication().getVariableResolver()
            .resolveVariable(FacesContext.getCurrentInstance(), BEAN_ID);
   }

   /**
    * action method to reset filter attribute
    */
   public void resetSearch()
   {
      searchProvider.initializeFilterAttributes();
      autoCompleteSelector = new UserAutocompleteMultiSelector(false, true);
      autoCompleteSelector.setShowOnlineIndicator(false);

   }

   /**
    * Searches all documents by applying the filter attributes and the specified
    * descriptor values.
    */
   public void searchDocuments()
   {
      try
      {
         expandSearchCriteria = false;

         UserWrapper userWrapper = autoCompleteSelector.getSelectedValue();
         if (userWrapper != null)
         {
            User u = userWrapper.getUser();
            searchProvider.getFilterAttributes().setAuthor(u.getAccount());

         }
         else
         {
            searchProvider.getFilterAttributes().setAuthor(null);
         }

         documentSearchTable.refresh(true);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public void initialize()
   {
      documentSearchTable.initialize();
      autoCompleteSelector = new UserAutocompleteMultiSelector(false, true);
      autoCompleteSelector.setShowOnlineIndicator(false);
      trace.debug("DocumentSearchBean initialized");
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder#create(java.lang.Object)
    */
   public DocumentSearchTableEntry createUserObject(Object resultRow)
   {
      DocumentSearchTableEntry documentSearchTableEntry = null;
      Document document = (Document) resultRow;

      try
      {
         documentSearchTableEntry = new DocumentSearchTableEntry(document);
      }
      catch (Exception e)
      {
         trace.error(e);
         documentSearchTableEntry = new DocumentSearchTableEntry();
         documentSearchTableEntry.setCause(e);
         documentSearchTableEntry.setLoaded(false);
      }
      return documentSearchTableEntry;
   }

   /**
    * opens a popup for process difinition selection
    * 
    * @param event
    */
   public void attachDocumentsToProcess(ActionEvent event)
   {
      selectedDoc = (DocumentSearchTableEntry) event.getComponent().getAttributes().get("row");
      SelectProcessDialog processDialog = SelectProcessDialog.getInstance();
      processDialog.initialize();
      processDialog.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType == EventType.APPLY)
            {
               try
               {
                  attachDocuments();
               }
               catch (Exception e)
               {
               }
            }
         }
      });
      processDialog.openPopup();
   }

   /**
    * attaches document to the process
    */
   private void attachDocuments()
   {
      SelectProcessDialog processDialog = SelectProcessDialog.getInstance();
      Long processOid = processDialog.getSelectedProcess();
      ProcessInstance pi = null;
      try
      {
         pi = ProcessInstanceUtils.getProcessInstance(processOid);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, propsBean.getString("views.common.process.invalidProcess.message"));
         return;
      }

      if (DocumentMgmtUtility.isProcessAttachmentAllowed(pi))
      {
         List<Document> documentList = new ArrayList<Document>();

         if (null != selectedDoc)
         {// single document is selected
            documentList.add(selectedDoc.getDocument());
         }
         else
         { // multiple documents are selected
            List<DocumentSearchTableEntry> documentEntryList = documentSearchTable.getCurrentList();
            for (DocumentSearchTableEntry documentSearchTableEntry : documentEntryList)
            {
               if (documentSearchTableEntry.isSelectedRow())
               {
                  documentList.add(documentSearchTableEntry.getDocument());
               }
            }
         }
         if (documentList.size() > 0)
         {
            // create copy of the documents and update process instance
            try
            {
               if (DocumentMgmtUtility.getDuplicateDocuments(pi, documentList).size() > 0)
               {
                  MessageDialog.addErrorMessage(propsBean
                        .getString("views.common.process.duplicateDocAttached.message"));
                  return;
               }
               DocumentMgmtUtility.addDocumentsToProcessInstance(pi, documentList);
               MessageDialog
                     .addInfoMessage(propsBean.getString("views.common.process.documentAttachedSuccess.message"));
            }
            catch (Exception e)
            {
               ExceptionHandler.handleException(e,
                     propsBean.getString("views.common.process.documentAttachedFailure.message"));
            }
         }
      }
      else
      {
         MessageDialog.addErrorMessage(propsBean.getString("views.common.process.invalidProcess.message"));
      }
   }

   /**
    * Returns selected items count
    * 
    * @return
    */
   public int getSelectedItemCount()
   {
      int count = 0;
      List<DocumentSearchTableEntry> documentList = documentSearchTable.getCurrentList();
      for (DocumentSearchTableEntry documentSearchTableEntry : documentList)
      {
         if (documentSearchTableEntry.isSelectedRow())
            count++;
      }
      return count;
   }

   public void handleEvent(EventType eventType)
   {
      if (eventType == EventType.APPLY)
      {
         expandSearchCriteria = false;
         documentSearchTable.refresh(true);
      }
      else if (eventType == EventType.CANCEL)
      {
         trace.info("Cancel call");
      }
   }

   public final DocumentSearchFilterHandler getFilterHandler()
   {
      return filterHandler;
   }

   public final void setFilterHandler(DocumentSearchFilterHandler filterHandler)
   {
      this.filterHandler = filterHandler;
   }

   public PaginatorDataTable<DocumentSearchTableEntry, Document> getDocumentSearchTable()
   {
      return documentSearchTable;
   }

   public void setDocumentSearchTable(PaginatorDataTable<DocumentSearchTableEntry, Document> documentSearchTable)
   {
      this.documentSearchTable = documentSearchTable;
   }

   public FilterAttributes getFilterAttributes()
   {
      return searchProvider.getFilterAttributes();
   }

   public boolean isExapndSearchCriteria()
   {
      return expandSearchCriteria;
   }

   public void setExapndSearchCriteria(boolean exapndSearchCriteria)
   {
      this.expandSearchCriteria = exapndSearchCriteria;
   }

   public final UserAutocompleteMultiSelector getAutoCompleteSelector()
   {
      return autoCompleteSelector;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   private class DocumentSearchExportHandler implements DataTableExportHandler<DocumentSearchTableEntry>
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleCellExport(org.eclipse.stardust.ui.web.common.table.export.ExportType, org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.Object, java.lang.Object)
       */
      public Object handleCellExport(ExportType exportType, ColumnPreference column,
            DocumentSearchTableEntry row, Object value)
      {
         if (METADATA.equals(column.getColumnName()))
         {
            String separator = ExportType.EXCEL == exportType ? "\n" : ", ";

            StringBuffer exportData = new StringBuffer();
            List<Pair<String, String>> metadata = row.getMetadata();
            for (Pair<String, String> data : metadata)
            {
               exportData.append(data.getFirst()).append(": ").append(data.getSecond()).append(separator);
            }

            String data = exportData.toString();
            if (data.length() > 0)
            {
               data = data.substring(0, data.length() - separator.length());
            }

            return data;
         }
         else
         {
            return value;
         }
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleHeaderCellExport(org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler.ExportType, org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.String)
       */
      public String handleHeaderCellExport(ExportType exportType, ColumnPreference column, String text)
      {
         return text;
      }
   }
}