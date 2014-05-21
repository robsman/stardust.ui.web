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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RepositoryPolicy;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.thirdparty.encoding.Text;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DocumentTypeWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.QueryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DocumentHandlersRegistryBean;


/**
 * @author Vikas.Mishra
 * @version
 */
public class DocumentSearchProvider implements Serializable
{
   // ~ Constants
   // ================================================================================================
   private static final long serialVersionUID = 4819215015562743113L;
   private static final String POSTFIX_OPEN = " (";
   private static final String POSTFIX_CLOSE = ")";
   // ~ Instance fields
   // ================================================================================================
   private FilterAttributes filterAttributes;
   private Set <DocumentTypeWrapper> declaredDocumentTypes;
   private List<IRepositoryInstanceInfo> repositoryInstances;

   // ~ Constructor
   // ================================================================================================
   /**
    * @param portalId
    */
   public DocumentSearchProvider(Set <DocumentTypeWrapper> declaredDocumentTypes)
   {
      this(declaredDocumentTypes, null);
   }
   
   /**
    * @param portalId
    */
   public DocumentSearchProvider(Set <DocumentTypeWrapper> declaredDocumentTypes, List<IRepositoryInstanceInfo> repositoryInstances)
   {
      this.declaredDocumentTypes = declaredDocumentTypes;
      this.repositoryInstances = repositoryInstances;
      filterAttributes = getFilterAttributes();
   }

   public static DocumentManagementService getDocumentManagementService()
   {
      return ServiceFactoryUtils.getDocumentManagementService();
   }

   // ~ Methods
   // ================================================================================================
   /**
    * @return FilterAttributes
    */
   public FilterAttributes getFilterAttributes()
   {
      if (filterAttributes == null)
      {
         initializeFilterAttributes();
      }

      return filterAttributes;
   }
   
   public void initializeFilterAttributes()
   {
      filterAttributes= new FilterAttributes(declaredDocumentTypes, repositoryInstances);     
   }

   public static QueryService getQueryService()
   {
      return ServiceFactoryUtils.getQueryService();
   }

   /**
    * @return
    */
   public IppSearchHandler<Document> getSearchHandler()
   {
      return new DocumentSearchHandler();
   }

   /**
    * @return
    */
   public IppSortHandler getSortHandler()
   {
      return new DocumentSearchSortHandler();
   }

   public static class FilterAttributes implements Serializable
   {
      private static final long serialVersionUID = -4502464782731754575L;

      private static final String MIME_TYPE_PREFIX = "views.documentSearchView.mimeType.";
      
      private static final String ALL = "All";

      private ArrayList<SelectItem> fileSize;
      private Date createDateFrom;
      private Date createDateTo;
      private Date modificationDateFrom;
      private Date modificationDateTo;
      private String author;

      private String[] selectedFileTypes = {ALL};
      private String advancedFileType;
      private SelectItem[] fileTypes;
      private SelectItem[] typicalFileTypes;
      private ArrayList<SelectItem> repositories;
      private boolean showAll;
      private String[] selectedDocumentTypes = {ALL};
      private String[] selectedRepository = {ALL};
      private List<SelectItem> documentTypes;
      private String containingText;
      private boolean searchContent = true;
      private boolean searchData = true;
      private String selectedFileSize;

      private String documentId;
      private String documentName;
      private String documentPath;
      private boolean selectFileTypeAdvance = false;
      private static MessagesViewsCommonBean messageCommonBean;

      

      public FilterAttributes(Set <DocumentTypeWrapper> declaredDocumentTypes)
      {
         this(declaredDocumentTypes, null);
      }
      
      public FilterAttributes(Set <DocumentTypeWrapper> declaredDocumentTypes, List<IRepositoryInstanceInfo> repositoryInstances)
      {
         super();
         messageCommonBean=MessagesViewsCommonBean.getInstance();
         
         //set file Size list
         fileSize = new ArrayList<SelectItem>();
         fileSize.add(getFileSizeInSelectItemFormat(FileSize.ALL));
         fileSize.add(getFileSizeInSelectItemFormat(FileSize.ZEROTO10KB));
         fileSize.add(getFileSizeInSelectItemFormat(FileSize.TENTO100KB));
         fileSize.add(getFileSizeInSelectItemFormat(FileSize.HUNDREDTO1MB));
         fileSize.add(getFileSizeInSelectItemFormat(FileSize.ONETO16MB));
         fileSize.add(getFileSizeInSelectItemFormat(FileSize.SIXTEENORMORE));
         selectedFileSize = FileSize.ALL.getId();
         
         // set file types list
         typicalFileTypes = new SelectItem[5];
         typicalFileTypes[0] = new SelectItem(ALL,
               messageCommonBean.getString("views.documentSearchView.documentType.All"));
         typicalFileTypes[1] = getMimeTypeInSelectItemFormat(MimeTypesHelper.PDF);
         typicalFileTypes[2] = getMimeTypeInSelectItemFormat(MimeTypesHelper.HTML);
         typicalFileTypes[3] = getMimeTypeInSelectItemFormat(MimeTypesHelper.TXT);
         typicalFileTypes[4] = getMimeTypeInSelectItemFormat(MimeTypesHelper.TIFF);
         fileTypes = typicalFileTypes;
       
         // set document types list
         documentTypes = new ArrayList<SelectItem>(declaredDocumentTypes.size());
         documentTypes
              .add(new SelectItem(ALL, messageCommonBean.getString("views.documentSearchView.documentType.All")));

         for (DocumentTypeWrapper documentTypeWrapper : declaredDocumentTypes)
         {
            documentTypes.add(new SelectItem(documentTypeWrapper.getDocumentTypeId(), documentTypeWrapper.getDocumentTypeI18nName()));
         }
         repositories = new ArrayList<SelectItem>();
         repositories
               .add(new SelectItem(ALL, messageCommonBean.getString("views.documentSearchView.documentType.All")));
         
         if (!CollectionUtils.isEmpty(repositoryInstances) && repositoryInstances.size() > 1)
         {
            for (IRepositoryInstanceInfo repos : repositoryInstances)
            {
               repositories.add(new SelectItem(repos.getRepositoryId(), repos.getRepositoryId() + POSTFIX_OPEN
                     + repos.getRepositoryName() + POSTFIX_CLOSE));
            }
         }
         
      }

      public ArrayList<SelectItem> getFileSize()
      {
         return fileSize;
      }

      public String getAuthor()
      {
         return author;
      }

      public String getContainingText()
      {
         return containingText;
      }

      public String[] getSelectedFileTypes()
      {
         return selectedFileTypes;
      }

      public String getDocumentId()
      {
         return documentId;
      }

      public String getDocumentName()
      {
         return documentName;
      }

      public void setAuthor(String author)
      {
         this.author = author;
      }

      public void setContainingText(String containingText)
      {
         this.containingText = containingText;
      }

      public void setSelectedFileTypes(String[] selectedFileTypes)
      {
         this.selectedFileTypes = selectedFileTypes;
      }

      public void setDocumentId(String documentId)
      {
         this.documentId = documentId;
      }

      public void setDocumentName(String documentName)
      {
         this.documentName = documentName;
      }

      public Date getCreateDateFrom()
      {
         return createDateFrom;
      }

      public void setCreateDateFrom(Date createDateFrom)
      {
         this.createDateFrom = createDateFrom;
      }

      public Date getCreateDateTo()
      {
         if (null == createDateTo)
         {
            return new Date();
         }
         else
         {
        return createDateTo;
      }
      }

      public void setCreateDateTo(Date createDateTo)
      {
         this.createDateTo = createDateTo;
      }

      public Date getModificationDateFrom()
      {
         return modificationDateFrom;
      }

      public void setModificationDateFrom(Date modificationDateFrom)
      {
         this.modificationDateFrom = modificationDateFrom;
      }

      public Date getModificationDateTo()
      {
         if (null == modificationDateTo)
         {
            return new Date();
         }
         else
         {
         return modificationDateTo;
      }
      }

      public void setModificationDateTo(Date modificationDateTo)
      {
         this.modificationDateTo = modificationDateTo;
      }

      public SelectItem[] getFileTypes()
      {
         return fileTypes;
      }

      // need to change once DocumentQuery API get finalize
      protected DocumentQuery buildQuery()
      {
         DocumentQuery query = DocumentQuery.findAll();

         return query;
      }

      protected boolean validParameters()
      {
         return true;
      }

      public final String getDocumentPath()
      {
         return documentPath;
      }

      public final void setDocumentPath(String documentPath)
      {
         this.documentPath = documentPath;
      }

      public boolean isShowAll()
      {
         return showAll;
      }

      public void setShowAll(boolean showAll)
      {
         this.showAll = showAll;
      }

      public String[] getSelectedDocumentTypes()
      {
         return selectedDocumentTypes;
      }

      public void setSelectedDocumentTypes(String[] selectedDocumentTypes)
      {
         this.selectedDocumentTypes = selectedDocumentTypes;
      }

      public String[] getSelectedRepository()
      {
         return selectedRepository;
      }

      public void setSelectedRepository(String[] selectedRepository)
      {
         this.selectedRepository = selectedRepository;
      }

      public List<SelectItem> getDocumentTypes()
      {
         return documentTypes;
      }

      public boolean isSearchContent()
      {
         return searchContent;
      }

      public void setSearchContent(boolean searchContent)
      {
         this.searchContent = searchContent;
      }

      public boolean isSearchData()
      {
         return searchData;
      }

      public void setSearchData(boolean searchData)
      {
         this.searchData = searchData;
      }

      public String getSelectedFileSize()
      {
         return selectedFileSize;
      }

      public void setSelectedFileSize(String selectedFileSize)
      {
         this.selectedFileSize = selectedFileSize;
      }

      public boolean isSelectFileTypeAdvance()
      {
         return selectFileTypeAdvance;
      }

      public String getAdvancedFileType()
      {
         return advancedFileType;
      }

      public void setAdvancedFileType(String advancedFileType)
      {
         this.advancedFileType = advancedFileType;
      }

      public ArrayList<SelectItem> getRepositories()
      {
         return repositories;
      }

      public void setRepositories(ArrayList<SelectItem> repositories)
      {
         this.repositories = repositories;
      }

      /**
       * @param valueChangeEvent
       */
      public void showAllChangeListener(ValueChangeEvent valueChangeEvent)
      {
         if (valueChangeEvent.getNewValue().equals(true))
         {
            DocumentHandlersRegistryBean documentHandlersRegistryBean = DocumentHandlersRegistryBean.getInstance();
            Set<MIMEType> mimeTypes = documentHandlersRegistryBean.getAllRegisteredMimeTypes();
            fileTypes = new SelectItem[mimeTypes.size() + 1];
            fileTypes[0] = new SelectItem("All", messageCommonBean.getString("views.documentSearchView.documentType.All"));
            int index = 1;
            for (MIMEType mimeType : mimeTypes)
            {
               fileTypes[index++] = getMimeTypeInSelectItemFormat(mimeType);
            }
         }
         else
         {
            fileTypes = typicalFileTypes;
         }
         selectedFileTypes = new String[1];
         selectedFileTypes[0] = "All";
      }

      /**
       * advanced File Type option
       */
      public void advanced()
      {
         if (selectFileTypeAdvance)
         {
            selectFileTypeAdvance = false;
         }
         else
         {
            selectFileTypeAdvance = true;
         }
      }

      /**
       * advanced text search
       */
      public void advancedTextSearch()
      {
         AdvancedTextSearchBean advancedTextSearchBean = AdvancedTextSearchBean.getInstance();
         advancedTextSearchBean.initialize();
         advancedTextSearchBean.openPopup();
         advancedTextSearchBean.setICallbackHandler(new ICallbackHandler()
         {
            public void handleEvent(EventType eventType)
            {
               AdvancedTextSearchBean advancedTextSearchBean = AdvancedTextSearchBean.getInstance();
               String containingText = advancedTextSearchBean.getFinalTextForSearch();
               setContainingText(containingText);
            }
         });
      }

      private static SelectItem getMimeTypeInSelectItemFormat(MIMEType mimeType)
      {
         return new SelectItem(mimeType.getType(), getI18nLabel(mimeType));
      }
      
      private static String getI18nLabel(MIMEType mimeType)
      {
        String label=  messageCommonBean.getString(MIME_TYPE_PREFIX+mimeType.getType());
        if(label==null)
        {
           label=  mimeType.getUserFriendlyName() + " (" + mimeType.getType() + ")";
        }
        return label;
      }

      private SelectItem getFileSizeInSelectItemFormat(FileSize fileSize)
      {
         return new SelectItem(fileSize.getId(), fileSize.getDescription());
      }
   }

   public static class FileSize
   {
      private static final String KEY_PREFIX = "views.documentSearchView.fileSize.value.";
      private String id;
      private long from;
      private long to;

      public static final FileSize ALL = new FileSize("FS1", 0, 0);
      public static final FileSize ZEROTO10KB = new FileSize("FS2", 0, 10);
      public static final FileSize TENTO100KB = new FileSize("FS3", 10, 100);
      public static final FileSize HUNDREDTO1MB = new FileSize("FS4", 0, 10);
      public static final FileSize ONETO16MB = new FileSize("FS5", 0, 10);
      public static final FileSize SIXTEENORMORE = new FileSize("FS6", 0, 10);

      public FileSize(String id, long from, long to)
      {
         super();
         this.id = id;
         this.from = from;
         this.to = to;
      }

      public String getId()
      {
         return this.id;
      }

      public long getFrom()
      {
         return from;
      }

      public long getTo()
      {
         return to;
      }

      public String getDescription()
      { 
         return MessagesViewsCommonBean.getInstance().getString(KEY_PREFIX + this.getId());
      }
   }

   public class DocumentSearchHandler extends IppSearchHandler<Document>
   {
      private static final long serialVersionUID = 6208755392414522634L;

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.jsf.common.ISearchHandler#createQuery()
       */
      public Query createQuery()
      {

         return filterAttributes.buildQuery();
      }

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.jsf.common.ISearchHandler#performSearch(org.eclipse.stardust.engine.api.query
       * .Query)
       */
      public QueryResult<Document> performSearch(Query query)
      {
         FilterAndTerm filter = query.where(DocumentQuery.NAME.like(QueryUtils
               .getFormattedString(getFilterAttributes().getDocumentName())));

         if (null != getFilterAttributes().getCreateDateFrom() && null != getFilterAttributes().getCreateDateTo())
         {
            filter.and(DocumentQuery.DATE_CREATED.between(DateUtils.convertToGmt(getFilterAttributes()
                  .getCreateDateFrom()), DateUtils.convertToGmt(getFilterAttributes().getCreateDateTo())));
         }

         if (null != getFilterAttributes().getModificationDateFrom()
               && null != getFilterAttributes().getModificationDateTo())
         {
            filter.and(DocumentQuery.DATE_LAST_MODIFIED.between(DateUtils.convertToGmt(getFilterAttributes()
                  .getModificationDateFrom()), DateUtils.convertToGmt(getFilterAttributes().getModificationDateTo())));
         }

         if (StringUtils.isNotEmpty(getFilterAttributes().getAuthor()))
         {
            filter.and(DocumentQuery.OWNER.like(QueryUtils.getFormattedString(getFilterAttributes().getAuthor())));
         }
         
         //Document types
         String[] documentTypeIds = getFilterAttributes().getSelectedDocumentTypes();
         if (documentTypeIds.length > 0 && !checkIfAllOptionSelect(documentTypeIds))
         {
            FilterOrTerm filterOrTerm = filter.addOrTerm();
            for (int i = 0; i < documentTypeIds.length; i++)
            {
               filterOrTerm.add(DocumentQuery.DOCUMENT_TYPE_ID.isEqual(documentTypeIds[i]));
            }
         }
         
         // Repository types
         String[] selectedRepo = getFilterAttributes().getSelectedRepository();
         if (selectedRepo.length > 0 && !checkIfAllOptionSelect(selectedRepo))
         {
            query.setPolicy(RepositoryPolicy.includeRepositories(CollectionUtils.newArrayList(Arrays.asList(selectedRepo))));
         }
         else
         {
            query.setPolicy(RepositoryPolicy.includeAllRepositories());
         }
           
         //File Type   
         if (getFilterAttributes().isSelectFileTypeAdvance())
         {
            filter.and(DocumentQuery.CONTENT_TYPE.like(QueryUtils.getFormattedString(getFilterAttributes()
                  .getAdvancedFileType())));
         }
         else
         {
            String[] mimeTypes = getFilterAttributes().getSelectedFileTypes();
            if (mimeTypes.length > 0 && !checkIfAllOptionSelect(mimeTypes))
            {
               FilterOrTerm filterOrTerm = filter.addOrTerm();
               for (int i = 0; i < mimeTypes.length; i++)
               {
                  filterOrTerm.add(DocumentQuery.CONTENT_TYPE.isEqual(mimeTypes[i]));
               }
            }
         }

         if (StringUtils.isNotEmpty(getFilterAttributes().getDocumentId()))
         {
            filter.and(DocumentQuery.ID.like(QueryUtils.getFormattedString(getFilterAttributes().getDocumentId())));
         }

         FilterCriterion contentFilter = null, dataFilter = null;
         if (StringUtils.isNotEmpty(getFilterAttributes().getContainingText()))
         {
            if (getFilterAttributes().isSearchContent())
            {
               contentFilter = DocumentQuery.CONTENT.like(QueryUtils.getFormattedString(Text
                     .escapeIllegalJcrChars(getFilterAttributes().getContainingText())));
            }

            if (getFilterAttributes().isSearchData())
            {
               dataFilter = DocumentQuery.META_DATA.any().like(
                     QueryUtils.getFormattedString(Text
                           .escapeIllegalJcrChars(getFilterAttributes().getContainingText())));
            }
          
            if (null != contentFilter && null != dataFilter)
            {
               FilterOrTerm filterOrTerm = filter.addOrTerm();
               filterOrTerm.add(contentFilter);
               filterOrTerm.add(dataFilter);
            }
            else if (null != contentFilter)
            {
               filter.and(contentFilter);
            }
            else if (null != dataFilter)
            {
               filter.and(dataFilter);
            }
         }
         
         Documents docs = getQueryService().getAllDocuments((DocumentQuery) query);

         return docs;
      }
      
      private boolean checkIfAllOptionSelect(String[] selectedValues)
      {
         for (String value : selectedValues)
         {
            if ("All".equalsIgnoreCase(value))
            {
               return true;
            }
         }
         return false;
      }
   }
}
