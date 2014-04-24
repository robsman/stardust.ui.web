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
package org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.extract;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.DataQuery;
import org.eclipse.stardust.engine.api.runtime.DataQueryResult;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.runtime.command.impl.ExtractPageCommand;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.Highlight;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.Note;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PageBookmark;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PageOrientation;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotations;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotationsImpl;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.Stamp;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.common.views.PortalConfiguration;
import org.eclipse.stardust.ui.web.viewscommon.common.ValidationMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.SpawnProcessHelper;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.DocumentTypeWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DocumentHandlerBean;
import org.eclipse.stardust.ui.web.viewscommon.views.document.ImageViewerConfigurationBean;
import org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.ImageUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.extract.TiffImageInfo.BookmarkPageRange;
import org.springframework.beans.BeanUtils;



/**
 * Dialog bean to extract page from Tiff image and open spawn processes for extracted
 * document
 * 
 * @author vikas.mishra
 * @Since 7.0
 */
public class ExtractPageDialog extends PopupUIComponentBean implements ConfirmationDialogHandler
{
   //class variable
   private static final long serialVersionUID = -2027430667698783833L;
   private static final String BEAN_NAME = "extractPageDialogBean";

   //column names
   public static String COL_EXTRACT_PAGE = "extractPage";
   public static String COL_PAGES = "pageRange";
   public static String COL_START_PROCESS = "startProcess";
   public static String COL_PROCESS_TYPE = "processType";
   public static String COL_DOCUMENT_TYPE = "documentType";
   public static String COL_DOCUMENT_DATA = "documentData";
   public static String COL_COPY_IMAGE_DATA = "copyImageData";
   public static String COL_COPY_PROCESS_DATA = "copyProcessData";
   public static String COL_DOCUMENT_DESCRIPTION = "documentDescription";
   public static String COL_VERSION_COMMENT = "versionComment";
   public static String COL_DELETE_PAGES = "deletePages";
   public static String PAGE_RANGE_REG_EX="^(\\d+)-(\\d+)$";
   public static String NUMBER_VAL_REG_EX="^\\d+$";
   
   //instance valriables   
   private final MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();
   private final String DEFAULT_LABEL = COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.documentType.default");
   private final ExtractPageDataCache dataCache = new ExtractPageDataCache();
   private static final String SUB_PROCESS_TYPE = "0";
   private static final String ROOT_PROCESS_TYPE = "1";
   private static final String VALIDATION_STYLE="messagePanel";

   private DataTable<ExtractPageTableEntry> extractTable;
   private List<SelectItem> processItems;
   private List<SelectItem> processTypes;
   private boolean showExtractPageView = true;
   private TiffImageInfo imageInfo;
   private UserPreferencesHelper userPrefsHelper;
   private boolean deletePageEnable = true;
   private SpawnProcessHelper spawnProcessHelper;
   private Set<Integer> updatePageList;
   private boolean updateOrigDoc = false;
   private String defaultProcessFQID;   
   private ConfirmationDialog extractPageConfirmationDialog;
   private ValidationMessageBean validationMessageBean;
  


   /**
    * get {@link ExtractPageDialog} Bean object
    * 
    * @return
    */
   public static ExtractPageDialog getCurrent()
   {
      ExtractPageDialog bean = (ExtractPageDialog) FacesUtils.getBeanFromContext(BEAN_NAME);

      return bean;
   }

   /**
    * method to open ExtractPageDialog
    */
   @Override
   public void openPopup()
   {      
      try
      {
         initialize();
         super.openPopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException("",
               COMMON_MESSAGE_BEAN.getString("common.exception") + " : " + e.getLocalizedMessage());
      }
   }

   /**
    * method to close ExtractPageDialog
    */
   @Override
   public void closePopup()
   {
      resetData();
      super.closePopup();
   }

   /**
    * method to reset instance variable values
    */
   public void resetData()
   {
      showExtractPageView = true;
      extractTable = null;
      imageInfo = null;
      spawnProcessHelper.reset();
      dataCache.clearCache();
   }

   /**
    * method to initialize instance variable values
    */
   @Override
   public void initialize()
   {
      // will store a list of messages for each page range deleted, seperated by <br/>      
      spawnProcessHelper = new SpawnProcessHelper();
      userPrefsHelper = getUserPrefenceHelper();
      validationMessageBean = new ValidationMessageBean();
      // Set the style for inline Validation message
      validationMessageBean.setStyleClass(VALIDATION_STYLE);
      deletePageEnable = ImageViewerConfigurationBean.isEnablePageDelete();

      List<ProcessDefinition> startableProcesses = null;

      startableProcesses = getStartableProcess();

      // if startable process is not available for user then show error message
      if (CollectionUtils.isNotEmpty(startableProcesses))
      {
         // sorting the startable processes alphabetically
         ProcessDefinitionUtils.sort(startableProcesses);
         defaultProcessFQID = startableProcesses.get(0).getQualifiedId();
         processItems = getProcesses(startableProcesses);
         processTypes = CollectionUtils.newArrayList();
         processTypes.add(new SelectItem(SUB_PROCESS_TYPE, COMMON_MESSAGE_BEAN
               .getString("views.extractPages.spawnProcess.subprocess")));
         processTypes.add(new SelectItem(ROOT_PROCESS_TYPE, COMMON_MESSAGE_BEAN
               .getString("views.extractPages.spawnProcess.rootprocess")));
         updatePageList = new LinkedHashSet<Integer>();
         loadDocumentPageList(imageInfo.getMaxPages());
         createTable();
         spawnProcessHelper.setRootProcessInstance(imageInfo.getProcessInstance());
         spawnProcessHelper.initialize();
         buildTable();
         populateBookmarkPageList(imageInfo.getBookmarkPageRange());
      }
      else
      {
         throw new RuntimeException(COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.noStartableProcesses"));
      }
   }

   

   /**
    * action method to process Extract Page
    */
   public void apply()
   {
      // if contains error message in context then not allowed to submit
      if (FacesContext.getCurrentInstance().getMessages().hasNext() || validationMessageBean.isContainsMessage())
      {
         return;
      }
      try
      {
         loadDocumentPageList(imageInfo.getMaxPages());

         if (CollectionUtils.isNotEmpty(extractTable.getList()) && validate(extractTable.getList()))
         {
            startSpawnProcesses();
            spawnProcessHelper.update();
            showExtractPageView = false; // show second page
            openConfirmationDialog();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(
               "",
               COMMON_MESSAGE_BEAN.getString("common.exception")
                     + " : "
                     + (StringUtils.isNotEmpty(e.getLocalizedMessage()) ? e.getLocalizedMessage() : COMMON_MESSAGE_BEAN
                           .getString("common.unknownError")));
      }
   }

   /**
    * action method to add row on Extract Page table
    * 
    * @param event
    */
   public void add(ActionEvent event)
   {
      // Page Range are not set on click of add button
      addRow(null,null);
   }

   /**
    * Creates a new row of ExtractPageDialog, if page range is specified create 
    * row with pageRange
    * 
    * @param pageRange
    * @param pageRangeStr
    */
   public void addRow(Set<Integer> pageRange, String pageRangeStr)
   {
      ExtractPageTableEntry row = null;
      String processType=userPrefsHelper.getSingleString(UserPreferencesEntries.V_IMAGE_VIEWER_CONFIG, UserPreferencesEntries.F_IMAGE_VIEWER_SPAWN_PROCESS_TYPE, SUB_PROCESS_TYPE);
      if (!CollectionUtils.isEmpty(pageRange))
      {
         // if process context is not available then default value for copy process data
         // should be false
         row = new ExtractPageTableEntry(imageInfo.getDocument().getId(), defaultProcessFQID,
               imageInfo.isProcessAvailable(), pageRange, pageRangeStr, imageInfo.getProcessInstance(), processType);
      }
      else
      {
         // if process context is not available then default value for copy process data
         // should be false
         row = new ExtractPageTableEntry(imageInfo.getDocument().getId(), defaultProcessFQID,
               imageInfo.isProcessAvailable(), imageInfo.getProcessInstance(), processType);
      }

      List<DocumentTypeWrapper> documentTypeList = dataCache.getDocumentType(row.getSpawnProcessFQID(), row.getModel());
      List<SelectItem> documentTypeItemList = getDocumentTypeSelectItem(documentTypeList);
      row.setDocTypeItems(documentTypeItemList);
      row.setDocTypeId(DEFAULT_LABEL);

      List<Data> dataList = dataCache.getDataPath(row.getSpawnProcessFQID(), DEFAULT_LABEL, row.getModel());
      List<SelectItem> dataItems = getDataSelectItem(row, dataList);
      row.setDataItems(dataItems);

      extractTable.getList().add(row);
   }

   /**
    * value change listener for process selection change
    * 
    * @param event
    */
   public void processChange(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();

         return;
      }
      else
      {
         ExtractPageTableEntry row = (ExtractPageTableEntry) event.getComponent().getAttributes().get("row");

         if (null != row)
         {
            List<DocumentTypeWrapper> documentTypeList = dataCache.getDocumentType(row.getSpawnProcessFQID(), row.getModel());
            List<SelectItem> documentTypeItemList = getDocumentTypeSelectItem(documentTypeList);
            row.setDocTypeId(DEFAULT_LABEL);
            row.setDocTypeItems(documentTypeItemList);

            List<Data> dataList = dataCache.getDataPath(row.getSpawnProcessFQID(), row.getDocTypeId(), row.getModel());
            List<SelectItem> dataItems = getDataSelectItem(row, dataList);
            row.setDataItems(dataItems);
         }
      }
   }

   /**
    * 
    * @param event
    */
   public void documentTypeChange(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();

         return;
      }
      else
      {
         ExtractPageTableEntry row = (ExtractPageTableEntry) event.getComponent().getAttributes().get("row");

         if (null != row)
         {
            List<Data> dataList = dataCache.getDataPath(row.getSpawnProcessFQID(), row.getDocTypeId(), row.getModel());
            List<SelectItem> dataItems = getDataSelectItem(row, dataList);
            row.setDataItems(dataItems);
         }
      }
   }

   /**
    * action method to remove row from extract page table
    * 
    * @param event
    */
   public void delete(ActionEvent event)
   {
      for (Iterator<ExtractPageTableEntry> it = extractTable.getList().iterator(); it.hasNext();)
      {
         ExtractPageTableEntry row = it.next();

         if (row.isSelect())
         {
            it.remove();
         }
      }
   }
   
   /*
    * 
    */
   public void openConfirmationDialog()
   {
      extractPageConfirmationDialog = new ConfirmationDialog(DialogContentType.INFO, DialogActionType.YES_NO, null,
            DialogStyle.COMPACT, this);
      extractPageConfirmationDialog.setIncludePath(ResourcePaths.V_EXTRACT_PAGE_CONF_DLG);
      super.closePopup();
      extractPageConfirmationDialog.openPopup();
   }

   /**
    * 
    */
   public boolean accept()
   {
      extractPageConfirmationDialog = null;
      openActivities();
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      extractPageConfirmationDialog = null;
      resetData();
      return true;
   }
   
   /**
    * method to create extract page table defination
    */
   private void createTable()
   {
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      List<ColumnPreference> fixCols = new ArrayList<ColumnPreference>();

      ColumnPreference extractPageCol = new ColumnPreference(COL_EXTRACT_PAGE,
            COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.column.extractPages"));

      ColumnPreference pageRangeCol = new ColumnPreference(COL_PAGES, "pageRange",
            COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.column.pageRange"),
            ResourcePaths.V_EXTRACT_PAGES_TABLE_COLUMNS, true, false);

      extractPageCol.addChildren(pageRangeCol);
     
      ColumnPreference startProcessCol = new ColumnPreference(COL_START_PROCESS, "spawnProcessFQID",
            COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.column.startProcess"),
            ResourcePaths.V_EXTRACT_PAGES_TABLE_COLUMNS, true, false);

      ColumnPreference processTypeCol = new ColumnPreference(COL_PROCESS_TYPE, "processType",
            COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.column.processType"),
            ResourcePaths.V_EXTRACT_PAGES_TABLE_COLUMNS, true, false);
      
      ColumnPreference docTypeCol = new ColumnPreference(COL_DOCUMENT_TYPE, "docTypeId",
            COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.column.docType"),
            ResourcePaths.V_EXTRACT_PAGES_TABLE_COLUMNS, true, false);

      ColumnPreference docDataCol = new ColumnPreference(COL_DOCUMENT_DATA, "dataId",
            COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.column.docData"),
            ResourcePaths.V_EXTRACT_PAGES_TABLE_COLUMNS, true, false);

      ColumnPreference copyImageDataCol = new ColumnPreference(COL_COPY_IMAGE_DATA, "copyImageData",
            COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.column.copyImageData"),
            ResourcePaths.V_EXTRACT_PAGES_TABLE_COLUMNS, true, false);
      copyImageDataCol.setNoWrap(false);

      ColumnPreference copyProcessDataCol = new ColumnPreference(COL_COPY_PROCESS_DATA, "copyProcessData",
            COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.column.copyProcessData"),
            ResourcePaths.V_EXTRACT_PAGES_TABLE_COLUMNS, true, false);
      copyProcessDataCol.setNoWrap(false);

      ColumnPreference descriptionDataCol = new ColumnPreference(COL_DOCUMENT_DESCRIPTION, "docDecription",
            COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.column.docDecription"),
            ResourcePaths.V_EXTRACT_PAGES_TABLE_COLUMNS, false, false);
      descriptionDataCol.setNoWrap(false);

      ColumnPreference commentCol = new ColumnPreference(COL_VERSION_COMMENT, "versionComment",
            COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.column.versionComment"),
            ResourcePaths.V_EXTRACT_PAGES_TABLE_COLUMNS, false, false);
      commentCol.setNoWrap(false);

      fixCols.add(extractPageCol);
      
      fixCols.add(startProcessCol);
      fixCols.add(processTypeCol);
      fixCols.add(docTypeCol);
      fixCols.add(docDataCol);

      // if delete page is then only add column
      if (deletePageEnable)
      {
         ColumnPreference deletePagesCol = new ColumnPreference(COL_DELETE_PAGES, "deletePages",
               COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.column.deletePage"),
               ResourcePaths.V_EXTRACT_PAGES_TABLE_COLUMNS, true, false);

         fixCols.add(deletePagesCol);
      }

      fixCols.add(copyImageDataCol);
      fixCols.add(copyProcessDataCol);

      cols.add(descriptionDataCol);
      cols.add(commentCol);

      IColumnModel extractPageColumnModel = new DefaultColumnModel(cols, fixCols, null,
            UserPreferencesEntries.M_VIEWS_COMMON, BEAN_NAME);
      TableColumnSelectorPopup colSelectPopup = new TableColumnSelectorPopup(extractPageColumnModel);      
      extractTable = new DataTable<ExtractPageTableEntry>(colSelectPopup, (TableDataFilters)null);
      extractTable.setRowSelector(new DataTableRowSelector("select"));
   }

   /**
    * Initialize table with list
    */
   private void buildTable()
   {
      extractTable.setList(new ArrayList<ExtractPageTableEntry>());
      extractTable.initialize();
   }

   /**
    * <p>
    * If bookmark is selected,it creates a new row of extract table with bookmark page
    * range in From Page and To Page else create new row without PageRange set
    * 
    * @param bookmarkPageList
    */
   private void populateBookmarkPageList(List<BookmarkPageRange> bookmarkPageList)
   {
      if (CollectionUtils.isNotEmpty(bookmarkPageList))
      {
         for (BookmarkPageRange bookmarkRange : bookmarkPageList)
         {
            StringBuffer pageRangeStr = new StringBuffer();
            Set<Integer> pageRange = CollectionUtils.newTreeSet();
            Integer fromPage = bookmarkRange.getFromPage();
            Integer toPage = bookmarkRange.getToPage();
            for (int i = fromPage; i <= toPage; i++)
            {
               pageRange.add(i);
            }
            if (fromPage != toPage)
            {
               pageRangeStr.append(fromPage.toString() + "-" + toPage.toString());
            }
            else
            {
               pageRangeStr.append(fromPage.toString());
            }
            addRow(pageRange,pageRangeStr.toString());
         }
      }
      else
      {
         validationMessageBean.addInfoMessage(COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.pagesRangeMsg"), "extractPageMsg");
         addRow(null,null);
      }
   }

   /**
    * remove extracted pages from original document and upload remaining pages
    * 
    * @param pages
    * @return
    * @throws Exception
    */
   private Document deleteExtractedPages(Set<Integer> pages) throws Exception
   {
      Document document = null;
      PrintDocumentAnnotations saveMetadata = null;

      if (isUpdateOrigDoc() & CollectionUtils.isNotEmpty(pages))
      {
         byte[] fileData = ImageUtils
               .createTiffImage(ImageUtils.extractTIFFImage(imageInfo.getDocumentContent(), pages));
         saveMetadata = getDocumentAnnotationsList(imageInfo.getDocument(), pages);

         Document originalDoc = imageInfo.getDocument();
         originalDoc.setDocumentAnnotations(saveMetadata);
         String sourceVersionComment = getSourceVersionComment(spawnProcessHelper.getSubprocessInstances());
        
         document = DocumentMgmtUtility.updateDocument(originalDoc, fileData, "", sourceVersionComment);
         
         if (imageInfo.isProcessAvailable())
         {
            ProcessInstance pi = imageInfo.getProcessInstance();           
            DMSHelper.updateProcessAttachment(pi, document);
         }
         
         //Refresh the viewer with the modified document.
         DocumentHandlerBean.getInstance().refreshViewer();
      }

      return document;
   }

   /**
    * 
    * @throws Exception
    */
   private void processExtractPageTableRows() throws Exception
   {
      for (ExtractPageTableEntry row : extractTable.getList())
      {
        Set<Integer> pageRange= row.getPages();

         byte[] fileData = ImageUtils.createTiffImage(ImageUtils.extractTIFFImage(imageInfo.getDocumentContent(),
               pageRange));
         row.setContent(fileData);

         if (row.isCopyImageData())
         {
            Set<Integer> currentPageList = new LinkedHashSet<Integer>();
            currentPageList = updateCurrentPageList(pageRange, currentPageList);

            PrintDocumentAnnotationsImpl saveMetadata = getDocumentAnnotationsList(imageInfo.getDocument(),
                  currentPageList);
            row.setDocMetadata(saveMetadata);
         }
      }
   }

   /**
    * method to spawn /start new processes in added rows
    */
   private void startSpawnProcesses() throws Exception
   {
      processExtractPageTableRows();
      
      ExtractPageCommand command = new ExtractPageCommand();
      command.setMimeType(MimeTypesHelper.TIFF.getType());
      command.setProcessInstance(imageInfo.getProcessInstance());    
      command.setSourceDocumentName(imageInfo.getDocument().getName());
      command.setSourceDocumentPath(FileUtils.getDocumentPath(imageInfo.getDocument().getPath()));

      List<ExtractPageCommand.PageModel> pages = CollectionUtils.newArrayList();
      for (ExtractPageTableEntry entry : extractTable.getList())
      {
         ExtractPageCommand.PageModel pageModel = createPageModel(entry);
         pages.add(pageModel);
      }
      command.setPages(pages);

      List<ProcessInstance> processInstances = (List<ProcessInstance>) ServiceFactoryUtils.getWorkflowService()
            .execute(command);
      spawnProcessHelper.setSubprocessInstances(processInstances);
      
      
      
      // delete extracted pages from original page and update document
      deleteExtractedPages(updatePageList);

      // delete old versions if not to retain version
      if (deletePageEnable && !ImageViewerConfigurationBean.isRetainPriorVersion())
      {
         DocumentMgmtUtility.deleteOldVersions(imageInfo.getDocument());
      }

      // if root process is available then de-reference new document from old process
      if (imageInfo.isProcessAvailable())
      {
         // check CopyData is selected for that row,before call this method
         deLinkDocumentFromProcess(processInstances, imageInfo.getDocument());
      }

      extractTable.getList().clear(); // release memory as it contains file data
   }
   
   /**
    * 
    * @param tableEntry
    * @return
    */
   private ExtractPageCommand.PageModel createPageModel(ExtractPageTableEntry tableEntry)
   {
      ExtractPageCommand.PageModel pageModel = new ExtractPageCommand.PageModel(tableEntry.getContent(), tableEntry.getVersionComment(),
            tableEntry.getDocDecription(), tableEntry.getDocMetadata(), tableEntry.getSpawnProcessFQID(),
            tableEntry.isCopyProcessData(), tableEntry.getDataId(), tableEntry.getProcessType()
                  .equals(SUB_PROCESS_TYPE) ? true : false);
      if(!pageModel.isAbortProcessInstance())
      {
         pageModel.setLinkComment(COMMON_MESSAGE_BEAN.getString("view.linkedProcess.label.spawn.linkComment"));
      }
      return pageModel;
   }   
  /**
   * 
   * @param pis
   */
   private String getSourceVersionComment(List<ProcessInstance> pis)
   {
      StringBuilder version=new StringBuilder();
      for (int i = 0; i < pis.size(); i++)
      {
         ExtractPageTableEntry row = extractTable.getList().get(i);
         ProcessInstance pi = pis.get(i);

         String documentId = row.getDocId();
         if (row.isDeletePages())
         {
            documentId += (" " + COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.sourceDocumentVersion.deleted"));
         }
         else
         {
            documentId += (" " + COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.sourceDocumentVersion.copied"));
         }

         String comment = COMMON_MESSAGE_BEAN.getParamString("views.extractPageDialog.sourceDocumentVersion.comment",
               row.getPageRange(), documentId,
               ProcessInstanceUtils.getProcessLabel(pi));
         version.append(comment);
      }
      return version.toString();
   } 
   

   /**
    * remove document reference from processes
    */
   private void deLinkDocumentFromProcess(List<ProcessInstance> processInstances, Document document)
   {
      for (ProcessInstance pi : processInstances)
      {
         DMSHelper.deleteProcessAttachment(pi, document);
      }
   }  

   /**
    * 
    * @param maxPageIndex
    */
   private void loadDocumentPageList(int maxPageIndex)
   {
      for (int i = 1; i <= maxPageIndex; i++)
      {
         updatePageList.add(i);
      }
   }

   /**
    * 
    * @param pageRange
    */
   private void updateDocumentPageList(Set<Integer> pageRange)
   {
       updatePageList.removeAll(pageRange);
   }

   /**
    * 
    * @param pageRange
    * @param currentPageList
    * @return
    */
   private Set<Integer> updateCurrentPageList(Set<Integer> pageRange, Set<Integer> currentPageList)
   {
         currentPageList.addAll(pageRange);

      return currentPageList;
   }

   /**
    * 
    * @param event
    */
   public void pageRangeChange(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();

         return;
      }
      else
      {
         try
         {
            ExtractPageTableEntry row = (ExtractPageTableEntry) event.getComponent().getAttributes().get("row");
            String pageRange = event.getNewValue().toString();
            String range = String.valueOf(pageRange);
            if(StringUtils.isEmpty(range))
            {
               return;
            }
            int index = extractTable.getList().indexOf(row);
            validationMessageBean.reset();
            String[] rangeArr = range.split(",");
            int[] rangeResults = new int[2];
            Set<Integer> ranges= CollectionUtils.newTreeSet();
            if (rangeArr != null && rangeArr.length > 0)
            {
               for (int i = 0; i < rangeArr.length; i++)
               {
                  String rangeTemp = rangeArr[i];
                  if (rangeTemp.matches(PAGE_RANGE_REG_EX))
                  {
                     String[] numbers = rangeTemp.split("-");
                     rangeResults[0] = Integer.parseInt(numbers[0]);
                     rangeResults[1] = Integer.parseInt(numbers[1]);
                     if ((rangeResults[0] < 1 || rangeResults[1] < 1) || rangeResults[0] > imageInfo.getMaxPages()
                           || rangeResults[1] > imageInfo.getMaxPages())
                     {
                        validationMessageBean.addError(COMMON_MESSAGE_BEAN.getParamString(
                              "views.extractPageDialog.table.error.invalidPageRange", String.valueOf(index + 1)),
                              "extractPageMsg");
                        break;
                     }
                     if(rangeResults[0] == rangeResults[1])
                     {
                        validationMessageBean.addError(COMMON_MESSAGE_BEAN.getParamString(
                              "views.movePagesDialog.error.overlappingPageRange", String.valueOf(index + 1)),
                              "movePageMsg");
                        break;
                     }
                     for(int j=rangeResults[0];j<=rangeResults[1];j++)
                     {
                        if(ranges.contains(j))
                        {
                           validationMessageBean.addError(COMMON_MESSAGE_BEAN.getParamString(
                                 "views.movePagesDialog.error.overlappingPageRange", String.valueOf(index + 1)),
                                 "movePageMsg");
                           break;
                        }
                        ranges.add(j);
                     }
                  }
                  else if (rangeTemp.matches(NUMBER_VAL_REG_EX))
                  {
                     Integer val = Integer.valueOf(rangeTemp);
                     if (val > 0)
                     {
                        if(ranges.contains(val))
                        {
                           validationMessageBean.addError(COMMON_MESSAGE_BEAN.getParamString(
                                 "views.movePagesDialog.error.overlappingPageRange", String.valueOf(index + 1)),
                                 "movePageMsg");
                           break;
                        }
                        ranges.add(val);
                     }
                     else
                     {
                        validationMessageBean.addError(COMMON_MESSAGE_BEAN.getParamString(
                              "views.extractPageDialog.table.error.invalidPageRange", String.valueOf(index + 1)),
                              "extractPageMsg");
                        break;
                     }
                  }
                  else
                  {
                     validationMessageBean.addError(COMMON_MESSAGE_BEAN.getParamString(
                           "views.extractPageDialog.table.error.invalidPageRange", String.valueOf(index + 1)),
                           "extractPageMsg");
                     break;
                  }
               }
               row.setPages(ranges);
            }
            
            if (validationMessageBean.isContainErrorMessages())
            {
               return;
            }
            
         }
         catch (Exception e)
         {
            FacesUtils.addErrorMessage(null, COMMON_MESSAGE_BEAN.getString("common.invalidValue.error"));
         }
      }
   }
   
   /**
    * method to open worklist table for started process/activities
    */
   public void openActivities()
   {
      boolean success = spawnProcessHelper.openActivities(COMMON_MESSAGE_BEAN
            .getString("views.extractPageDialog.worklist.title"));

      if (success)
      {
         closePopup();
      }
   }

   public boolean isDeletePageEnable()
   {
      return deletePageEnable;
   }

   public boolean isUpdateOrigDoc()
   {
      return updateOrigDoc;
   }

   /**
    * method return true any of row is selected
    * 
    * @return
    */
   public boolean isRowSelected()
   {
      boolean selected = false;

      if (null != extractTable)
      {
         for (ExtractPageTableEntry row : extractTable.getList())
         {
            if (row.isSelect())
            {
               selected = true;

               break;
            }
         }
      }

      return selected;
   }

   /**
    * 
    * @return
    */
   public boolean isShowExtractPageView()
   {
      return showExtractPageView;
   }

   /**
    * check ExtractPage Table contains any row
    * 
    * @return
    */
   public boolean isRowAdded()
   {
      return CollectionUtils.isNotEmpty(extractTable.getList());
   }

   /**
    * 
    * @return
    */
   public final DataTable<ExtractPageTableEntry> getExtractTable()
   {
      return extractTable;
   }

   public ConfirmationDialog getExtractPageConfirmationDialog()
   {
      return extractPageConfirmationDialog;
   }

   public ValidationMessageBean getValidationMessageBean()
   {
      return validationMessageBean;
   }

   /**
    * 
    * @param highlightList
    * @param fromPageIndex
    * @param toPageIndex
    */
   private Set<Highlight> getHighlightsList(Set<Highlight> highlightList, Set<Integer> updatePageList)
   {
      Set<Highlight> highlightLst = new LinkedHashSet<Highlight>();

      for (Highlight highlight : highlightList)
      {
         if (updatePageList.contains(highlight.getPageNumber()))
         {
            int j = 1;

            for (Integer idx : updatePageList)
            {
               if (highlight.getPageNumber() == idx)
               {
                  Highlight objHighlight = new Highlight();
                  BeanUtils.copyProperties(highlight, objHighlight);
                  objHighlight.setPageNumber(j);
                  highlightLst.add(objHighlight);

                  break;
               }

               j++;
            }
         }
      }

      return highlightLst;
   }

   /**
    * 
    * @param startableProcesses
    * @return
    */
   private List<SelectItem> getProcesses(List<ProcessDefinition> startableProcesses)
   {
      List<SelectItem> items = new ArrayList<SelectItem>();

      for (ProcessDefinition pd : startableProcesses)
      {
         items.add(new SelectItem(pd.getQualifiedId(), I18nUtils.getProcessName(pd)));
      }

      return items;
   }

   /**
    * 
    * @param notesList
    * @param fromPageIndex
    * @param toPageIndex
    */
   private Set<Note> getNotesList(Set<Note> notesList, Set<Integer> updatePageList)
   {
      Set<Note> noteList = new LinkedHashSet<Note>();

      for (Note note : notesList)
      {
         if (updatePageList.contains(note.getPageNumber()))
         {
            int j = 1;

            for (Integer idx : updatePageList)
            {
               if (note.getPageNumber() == idx)
               {
                  Note objNote = new Note();
                  BeanUtils.copyProperties(note, objNote);
                  objNote.setPageNumber(j);
                  noteList.add(objNote);

                  break;
               }

               j++;
            }
         }
      }

      return noteList;
   }

   /**
    * 
    * @param stampList
    * @param fromPageIndex
    * @param toPageIndex
    */
   private Set<Stamp> getStampsList(Set<Stamp> stampList, Set<Integer> updatePageList)
   {
      Set<Stamp> stampLst = new LinkedHashSet<Stamp>();

      for (Stamp stamp : stampList)
      {
         if (updatePageList.contains(stamp.getPageNumber()))
         {
            int j = 1;

            for (Integer idx : updatePageList)
            {
               if (stamp.getPageNumber() == idx)
               {
                  Stamp objStamp = new Stamp();
                  BeanUtils.copyProperties(stamp, objStamp);
                  objStamp.setPageNumber(j);
                  stampLst.add(objStamp);

                  break;
               }

               j++;
            }
         }
      }

      return stampLst;
   }

   /**
    * validation method to check pages range and numeric check
    * 
    * @param list
    * @return
    */
   private boolean validate(List<ExtractPageTableEntry> tableList)
   {
      boolean success = true;
      updateOrigDoc = false;
      validationMessageBean.reset();
      
      for (int i = 0; i < tableList.size(); i++)
      {
         ExtractPageTableEntry row = tableList.get(i);

         if(CollectionUtils.isEmpty(row.getPages()))
         {
            validationMessageBean.addError(COMMON_MESSAGE_BEAN.getParamString(
                  "views.extractPageDialog.table.error.invalidPageRange", String.valueOf(i + 1)),
                  "extractPageMsg");
            success = false;
         }
         else if ((((TreeSet<Integer>)row.getPages()).last() > imageInfo.getMaxPages()))
         {
            validationMessageBean.addError(
                  COMMON_MESSAGE_BEAN.getParamString("views.extractPageDialog.table.error.invalidPageRange",
                        String.valueOf(i + 1)), "extractPageMsg");
            success = false;
         }
         else if (row.isDeletePages())
         {
            updateDocumentPageList(row.getPages());
            updateOrigDoc = true;
         }
      }

      if (isUpdateOrigDoc() & CollectionUtils.isEmpty(updatePageList))
      {
         validationMessageBean.addError(
               COMMON_MESSAGE_BEAN.getString("views.extractPageDialog.table.error.deleteAllPages"), "extractPageMsg");
         success = false;
      }

      return success;
   }
   /**
    * 
    * @param pageOrientationList
    * @param fromPageIndex
    * @param toPageIndex
    */
   private Set<PageOrientation> getPageOrientationsList(Set<PageOrientation> pageOrientationList,
         Set<Integer> updatePageList)
   {
      Set<PageOrientation> pageOrientationLst = new LinkedHashSet<PageOrientation>();

      for (PageOrientation pageOrientation : pageOrientationList)
      {
         if (updatePageList.contains(pageOrientation.getPageNumber()))
         {
            int j = 1;

            for (Integer idx : updatePageList)
            {
               if (pageOrientation.getPageNumber() == idx)
               {
                  PageOrientation objPage = new PageOrientation();
                  BeanUtils.copyProperties(pageOrientation, objPage);
                  objPage.setPageNumber(j);
                  pageOrientationLst.add(objPage);

                  break;
               }

               j++;
            }
         }
      }

      return pageOrientationLst;
   }

   /**
    * @return
    */
   private static UserPreferencesHelper getUserPrefenceHelper()
   {
      return UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON, PortalConfiguration.getInstance()
            .getPrefScopesHelper().getSelectedPreferenceScope());
   }

   

   /**
    * 
    * @param originalDoc
    * @param currentPageList
    * @return
    */
   private PrintDocumentAnnotationsImpl getDocumentAnnotationsList(Document originalDoc, Set<Integer> currentPageList)
   {
      PrintDocumentAnnotationsImpl saveMetadata = null;
      PrintDocumentAnnotations annots = ((PrintDocumentAnnotations) originalDoc.getDocumentAnnotations());

      if (null != annots)
      {
         saveMetadata = new PrintDocumentAnnotationsImpl();

         if (!CollectionUtils.isEmpty(annots.getBookmarks()))
         {
            Set<PageBookmark> newBookmarkList = getBookmarkList(annots.getBookmarks(), currentPageList);
            saveMetadata.setBookmarks(newBookmarkList);
         }

         if (!CollectionUtils.isEmpty(annots.getNotes()))
         {
            Set<Note> noteList = getNotesList(annots.getNotes(), currentPageList);
            saveMetadata.setNotes(noteList);
         }

         if (!CollectionUtils.isEmpty(annots.getHighlights()))
         {
            Set<Highlight> highlightList = getHighlightsList(annots.getHighlights(), currentPageList);
            saveMetadata.setHighlights(highlightList);
         }

         if (!CollectionUtils.isEmpty(annots.getStamps()))
         {
            Set<Stamp> stampList = getStampsList(annots.getStamps(), currentPageList);
            saveMetadata.setStamps(stampList);
         }

         if (!CollectionUtils.isEmpty(annots.getPageOrientations()))
         {
            Set<PageOrientation> pageOrientationList = getPageOrientationsList(annots.getPageOrientations(),
                  currentPageList);
            saveMetadata.setPageOrientations(pageOrientationList);
         }
      }

      return saveMetadata;
   }

   /**
    * 
    * @param bookmarkList
    * @param fromPageIndex
    * @param toPageIndex
    */
   private Set<PageBookmark> getBookmarkList(Set<PageBookmark> bookmarkList, Set<Integer> updatePageList)
   {
      Set<PageBookmark> bookmarklst = new LinkedHashSet<PageBookmark>();

      for (PageBookmark bookmark : bookmarkList)
      {
         if (updatePageList.contains(bookmark.getStartPage()) && updatePageList.contains(bookmark.getEndPage()))
         {
            int j = 1;
            PageBookmark objBookmark = new PageBookmark();
            objBookmark.setId(bookmark.getId());

            for (Integer idx : updatePageList)
            {
               if (bookmark.getStartPage() == idx)
               {
                  objBookmark.setStartPage(j);
               }

               if (bookmark.getEndPage() == idx)
               {
                  objBookmark.setEndPage(j);
               }

               j++;
            }

            bookmarklst.add(objBookmark);
         }
      }

      return bookmarklst;
   }

   /**
    * 
    * @return
    */
   public List<SelectItem> getProcessItems()
   {
      return processItems;
   }
   
   public List<SelectItem> getProcessTypes()
   {
      return processTypes;
   }

   public SpawnProcessHelper getSpawnProcessHelper()
   {
      return spawnProcessHelper;
   }

   public TiffImageInfo getImageInfo()
   {
      return imageInfo;
   }

   public void setImageInfo(TiffImageInfo imageInfo)
   {
      this.imageInfo = imageInfo;
   }

   /**
    * return true if document is open from view where ProcessInstance is available.
    * 
    * @return
    */
   public boolean isProcessContextAvailable()
   {
      return imageInfo.isProcessAvailable();
   }

   /**
    * returns This should show the startable processes for the user (associated with a
    * Manual Trigger) and which support Process Attachments or reference Document Data
    * (via datapaths, datamappings, triggers).
    * 
    * @return List of ProcessDefinition
    */
   private List<ProcessDefinition> getStartableProcess()
   {
      // if ProcessInstance is available then find model id from instance or load from
      // all active models
      if (imageInfo.isProcessAvailable())
      {
         return ProcessDefinitionUtils.getStartableProcessSupportAttachment(imageInfo.getProcessInstance()
               .getModelOID());
      }
      else
      {
         return ProcessDefinitionUtils.getStartableProcessSupportAttachmentInActiveModels();
      }
   }

   /**
    * 
    * @param processFQID
    * @param dataList
    * @return
    */
   private List<SelectItem> getDataSelectItem(ExtractPageTableEntry row, List<Data> dataList)
   {
      List<SelectItem> dataItems = CollectionUtils.newArrayList();

      if (null != row)
      {
         String processId = ModelUtils.extractParticipantId(row.getSpawnProcessFQID());
         boolean isAttachmentAllowed = DocumentMgmtUtility.isProcessAttachmentAllowed(row.getModel(), processId);

         if (isAttachmentAllowed)
         {
            dataItems.add(new SelectItem(ExtractPageTableEntry.PROCESS_ATTACHMENT, COMMON_MESSAGE_BEAN
                  .getString("views.extractPageDialog.documentData.processAttachment")));
         }

         if (CollectionUtils.isNotEmpty(dataList))
         {
            for (Data data : dataList)
            {
               dataItems.add(new SelectItem(data.getId(), I18nUtils.getDataName(data)));
            }

            row.setDataId(dataList.get(0).getId());
         }
         else
         {
            row.setDataId(ExtractPageTableEntry.PROCESS_ATTACHMENT);
         }
      }

      return dataItems;
   }

   /**
    * 
    * @param typeList
    * @return
    */
   private List<SelectItem> getDocumentTypeSelectItem(List<DocumentTypeWrapper> documentTypes)
   {
      List<SelectItem> docTypeItems = CollectionUtils.newArrayList();

      if (CollectionUtils.isNotEmpty(documentTypes))
      {
         for (DocumentTypeWrapper type : documentTypes)
         {
            String label = type.getDocumentTypeI18nName();
            docTypeItems.add(new SelectItem(type.getDocumentTypeId(), StringUtils.isNotEmpty(label) ? label : type
                  .getDocumentTypeId()));
         }
      }

      return docTypeItems;
   }

   /**
    * 
    * @author Vikas.Mishra
    * @version $Revision: $
    */
   private final class ExtractPageDataCache
   {
      private final Map<String, List<DocumentTypeWrapper>> docTypeCache = CollectionUtils.newHashMap();
      private final Map<ProcessToDocumentType, List<Data>> dataPathCache = CollectionUtils.newHashMap();
      private final Map<String, DocumentTypeWrapper> allDocumentTypes = CollectionUtils.newHashMap();

      /**
       * clear cache values
       */
      public void clearCache()
      {
         docTypeCache.clear();
         dataPathCache.clear();
         allDocumentTypes.clear();
      }

      /**
       * 
       * @param processFQID
       * @param documentType
       * @return
       */
      public List<Data> getDataPath(String processFQID, String documentTypeId, DeployedModel model)
      {
         List<Data> list = null;
         ProcessToDocumentType processToDocumentType = new ProcessToDocumentType(processFQID, documentTypeId);

         if (dataPathCache.containsKey(processToDocumentType))
         {
            list = dataPathCache.get(processToDocumentType);
         }
         else
         {
            String processId = ModelUtils.extractParticipantId(processFQID);
            List<Data> result = findDocumentData(documentTypeId, model, processId);

            dataPathCache.put(processToDocumentType, result);
            list = result;
         }

         return list;
      }

      /**
       * 
       * @param docTypeId
       * @param model
       * @param processId
       * @return
       */
      public List<Data> findDocumentData(String docTypeId, DeployedModel model, String processId)
      {
         QueryService QueryService = ServiceFactoryUtils.getQueryService();

         DataQuery dataQuery = null;

         if (DEFAULT_LABEL.equals(docTypeId))
         {
            dataQuery = DataQuery.findUsedInProcessHavingDataType(model.getModelOID(), processId,
                  DmsConstants.DATA_TYPE_DMS_DOCUMENT);
         }
         else
         {
            DocumentTypeWrapper type = allDocumentTypes.get(docTypeId);
            dataQuery = DataQuery.findUsedInProcessHavingDocumentWithDocType(Long.valueOf(model.getModelOID())
                  .longValue(), processId, type.getDocumentType());

            return QueryService.getAllData(dataQuery);
         }

         return QueryService.getAllData(dataQuery);
      }

      /**
       * If Process supports Process Attachments, this should list all Document Types. If
       * Process does not support Process Attachments, this should list the Document Types
       * referenced by the Document Data being referenced (via datapaths, datamappings,
       * triggers) by the Process Definition.
       * 
       * @return
       */
      public List<DocumentTypeWrapper> getDocumentType(String processFQID, DeployedModel model)
      {
         List<DocumentTypeWrapper> list = null;

         if (docTypeCache.containsKey(processFQID))
         {
            list = docTypeCache.get(processFQID);
         }
         else
         {
            String processId = ModelUtils.extractParticipantId(processFQID);
            List<DocumentTypeWrapper> docTypes = CollectionUtils.newArrayList();
            docTypes.add(new DocumentTypeWrapper(new DocumentType(DEFAULT_LABEL, DEFAULT_LABEL),null));

            boolean isAttachmentAllowed = DocumentMgmtUtility.isProcessAttachmentAllowed(model, processId);

            // if selected process support attachment then show all document type
            if (isAttachmentAllowed)
            {
               docTypes.addAll(ModelUtils.getDeclaredDocumentTypes(model));
            }
            else
            {
               QueryService QueryService = ServiceFactoryUtils.getQueryService();
               DataQuery dataQuery = DataQuery.findUsedInProcess(model.getModelOID(), processId);
               DataQueryResult result = QueryService.getAllData(dataQuery);

               if (CollectionUtils.isNotEmpty(result))
               {
                  Set<DocumentType> types = DocumentTypeUtils.getDocumentTypesFromData(model, result);
                  for (DocumentType documentType : types)
                  {
                     docTypes.add(new DocumentTypeWrapper(documentType, model));
                  }
               }
            }

            docTypeCache.put(processFQID, docTypes);

            for (DocumentTypeWrapper type : docTypes)
            {
               if (!allDocumentTypes.containsKey(type.getDocumentTypeId()))
               {
                  allDocumentTypes.put(type.getDocumentTypeId(), type);
               }
            }

            list = docTypes;
         }

         return list;
      }

      /**
       * 
       * @author Vikas.Mishra
       * @version $Revision: $
       */
      class ProcessToDocumentType
      {
         private final String processId;
         private final String documentTypeId;

         public ProcessToDocumentType(String processId, String documentTypeId)
         {
            this.processId = processId;
            this.documentTypeId = documentTypeId;
         }

         public String getProcessId()
         {
            return processId;
         }

         public String getDocumentTypeId()
         {
            return documentTypeId;
         }

         @Override
         public boolean equals(Object obj)
         {
            if ((null != obj) && obj instanceof ProcessToDocumentType)
            {
               ProcessToDocumentType other = (ProcessToDocumentType) obj;

               if (other.getProcessId().equals(processId) && other.getDocumentTypeId().equals(documentTypeId))
               {
                  return true;
               }
            }

            return false;
         }

         @Override
         public int hashCode()
         {
            int result = 17;
            result += ((37 * result) + processId.hashCode());
            result += ((37 * result) + documentTypeId.hashCode());

            return result;
         }
      }
   }

}