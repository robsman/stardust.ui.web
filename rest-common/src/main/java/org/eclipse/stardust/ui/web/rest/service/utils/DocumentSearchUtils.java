/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.DocumentFilter;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RepositoryPolicy;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.thirdparty.encoding.Text;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.rest.FilterDTO;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentSearchCriteriaDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentSearchFilterAttributesDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentSearchFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentVersionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.InfoDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.InfoDTO.MessageType;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.SelectItemDTO;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DocumentTypeWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.QueryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DocumentHandlersRegistryBean;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRVersionTracker;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class DocumentSearchUtils {

	private static final String ALL = "All";
	private static final String MIME_TYPE_PREFIX = "views.documentSearchView.mimeType.";
	private static final String POSTFIX_OPEN = " (";
	private static final String POSTFIX_CLOSE = ")";

	private static final String COL_DOCUMENT_NAME = "documentName";

	private static final String COL_CREATE_DATE = "createDate";

	private static final String COL_MODIFICATION_DATE = "modificationDate";

	private static final String COL_FILE_TYPE = "fileType";

	private static final String COL_DOCUMENT_TYPE = "documentType";

	private static final String COL_DOCUMENT_ID = "documentId";
	private static final String COL_AUTHOR = "author";

	private static final String VIEW_ACTIVITY_PANEL = "activityPanel";

	private MessagesViewsCommonBean messageCommonBean;

	private static final Logger trace = LogManager
			.getLogger(DocumentSearchUtils.class);

	/**
	 * 
	 * @return
	 */
	public String getFilterAttributes() {

		messageCommonBean = MessagesViewsCommonBean.getInstance();

		List<IRepositoryInstanceInfo> repositoryInstances = DocumentMgmtUtility
				.getDocumentManagementService().getRepositoryInstanceInfos();
		Set<DocumentTypeWrapper> declaredDocumentTypes = ModelUtils
				.getAllActiveDeclaredDocumentTypes();

		// set file types list
		ArrayList<SelectItemDTO> typicalFileTypes = new ArrayList<SelectItemDTO>();
		typicalFileTypes.add(new SelectItemDTO(ALL, messageCommonBean
				.getString("views.documentSearchView.documentType.All")));
		typicalFileTypes
				.add(getMimeTypeInSelectItemFormat(MimeTypesHelper.PDF));
		typicalFileTypes
				.add(getMimeTypeInSelectItemFormat(MimeTypesHelper.HTML));
		typicalFileTypes
				.add(getMimeTypeInSelectItemFormat(MimeTypesHelper.TXT));
		typicalFileTypes
				.add(getMimeTypeInSelectItemFormat(MimeTypesHelper.TIFF));

		// set document types list
		List<SelectItemDTO> documentTypes = new ArrayList<SelectItemDTO>(
				declaredDocumentTypes.size());
		documentTypes.add(new SelectItemDTO(ALL, messageCommonBean
				.getString("views.documentSearchView.documentType.All")));

		for (DocumentTypeWrapper documentTypeWrapper : declaredDocumentTypes) {
			documentTypes.add(new SelectItemDTO(documentTypeWrapper
					.getDocumentTypeId(), documentTypeWrapper
					.getDocumentTypeI18nName()));
		}

		// set repository list
		ArrayList<SelectItemDTO> repositories = new ArrayList<SelectItemDTO>();
		repositories.add(new SelectItemDTO(ALL, messageCommonBean
				.getString("views.documentSearchView.documentType.All")));

		if (!CollectionUtils.isEmpty(repositoryInstances)
				&& repositoryInstances.size() > 1) {
			for (IRepositoryInstanceInfo repos : repositoryInstances) {
				repositories.add(new SelectItemDTO(repos.getRepositoryId(),
						repos.getRepositoryId() + POSTFIX_OPEN
								+ repos.getRepositoryName() + POSTFIX_CLOSE));
			}
		}

		// Set the RegisteredMimeFileTypes

		ArrayList<SelectItemDTO> allRegisteredMimeFileTypes = getAllRegisteredMimeFileTypes();

		DocumentSearchFilterAttributesDTO dsfaDTO = new DocumentSearchFilterAttributesDTO();
		dsfaDTO.typicalFileTypes = typicalFileTypes;
		dsfaDTO.documentTypes = documentTypes;
		dsfaDTO.repositories = repositories;
		dsfaDTO.allRegisteredMimeFileTypes = allRegisteredMimeFileTypes;
		Gson gson = new Gson();
		return gson.toJson(dsfaDTO);
	}

	/**
	 * 
	 * @param mimeType
	 * @return
	 */
	private SelectItemDTO getMimeTypeInSelectItemFormat(MIMEType mimeType) {
		return new SelectItemDTO(mimeType.getType(), getI18nLabel(mimeType));
	}

	private String getI18nLabel(MIMEType mimeType) {
		String label = messageCommonBean.getString(MIME_TYPE_PREFIX
				+ mimeType.getType());
		if (label == null) {
			label = mimeType.getUserFriendlyName() + " (" + mimeType.getType()
					+ ")";
		}
		return label;
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<SelectItemDTO> getAllRegisteredMimeFileTypes() {

		DocumentHandlersRegistryBean documentHandlersRegistryBean = DocumentHandlersRegistryBean
				.getInstance();
		Set<MIMEType> mimeTypes = documentHandlersRegistryBean
				.getAllRegisteredMimeTypes();
		ArrayList<SelectItemDTO> fileTypes = new ArrayList<SelectItemDTO>(
				mimeTypes.size() + 1);
		fileTypes
				.add(0,
						new SelectItemDTO(
								"All",
								messageCommonBean
										.getString("views.documentSearchView.documentType.All")));
		int index = 1;
		for (MIMEType mimeType : mimeTypes) {
			fileTypes.add(index++, getMimeTypeInSelectItemFormat(mimeType));
		}
		return fileTypes;
	}
    /**
     * 
     * @param options
     * @param documentSearchAttributes
     * @return
     */
	public QueryResult<Document> performSearch(Options options,
			DocumentSearchCriteriaDTO documentSearchAttributes) {
		DocumentQuery query = new DocumentQuery();
		
		SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip,
               true);
        query.setPolicy(subsetPolicy);

		addSortCriteria(query, options);

		if (options.filter != null) {
			applyFiltering(query, options.filter);
		}

		FilterAndTerm filter = query.where(DocumentQuery.NAME.like(QueryUtils
				.getFormattedString(documentSearchAttributes.documentName)));

		if (null != documentSearchAttributes.createDateFrom
				&& null != documentSearchAttributes.createDateTo) {
			filter.and(DocumentQuery.DATE_CREATED.between(
					DateUtils
							.convertToGmt(documentSearchAttributes.createDateFrom),
					DateUtils
							.convertToGmt(documentSearchAttributes.createDateTo)));
		}

		if (null != documentSearchAttributes.modificationDateFrom
				&& null != documentSearchAttributes.modificationDateTo) {
			filter.and(DocumentQuery.DATE_LAST_MODIFIED.between(
					DateUtils
							.convertToGmt(documentSearchAttributes.modificationDateFrom),
					DateUtils
							.convertToGmt(documentSearchAttributes.modificationDateTo)));
		}

		if (StringUtils.isNotEmpty(documentSearchAttributes.author)) {
			filter.and(DocumentQuery.OWNER.like(QueryUtils
					.getFormattedString(documentSearchAttributes.author)));
		}

		// Document types
		List<String> documentTypeIds = documentSearchAttributes.selectedDocumentTypes;
		if (documentTypeIds.size() > 0
				&& !checkIfAllOptionSelect(documentTypeIds)) {
			FilterOrTerm filterOrTerm = filter.addOrTerm();
			for (String documentTypeId : documentTypeIds) {
				filterOrTerm.add(DocumentQuery.DOCUMENT_TYPE_ID
						.isEqual(documentTypeId));
			}
		}

		// Repository types
		List<String> selectedRepo = documentSearchAttributes.selectedRepository;
		if (selectedRepo.size() > 0 && !checkIfAllOptionSelect(selectedRepo)) {
			query.setPolicy(RepositoryPolicy
					.includeRepositories(CollectionUtils
							.newArrayList(selectedRepo)));
		} else {
			query.setPolicy(RepositoryPolicy.includeAllRepositories());
		}

		// File Type
		if (documentSearchAttributes.selectFileTypeAdvance) {
			filter.and(DocumentQuery.CONTENT_TYPE.like(QueryUtils
					.getFormattedString(documentSearchAttributes.advancedFileType)));
		} else {
			List<String> mimeTypes = documentSearchAttributes.selectedFileTypes;
			if (mimeTypes.size() > 0 && !checkIfAllOptionSelect(mimeTypes)) {
				FilterOrTerm filterOrTerm = filter.addOrTerm();
				for (String mimeType : mimeTypes) {
					filterOrTerm.add(DocumentQuery.CONTENT_TYPE
							.isEqual(mimeType));
				}
			}
		}

		if (StringUtils.isNotEmpty(documentSearchAttributes.documentId)) {
			filter.and(DocumentQuery.ID.like(QueryUtils
					.getFormattedString(documentSearchAttributes.documentId)));
		}

		FilterCriterion contentFilter = null, dataFilter = null;
		if (StringUtils.isNotEmpty(documentSearchAttributes.containingText)) {
			if (documentSearchAttributes.searchContent) {
				contentFilter = DocumentQuery.CONTENT
						.like(QueryUtils.getFormattedString(Text
								.escapeIllegalJcrChars(documentSearchAttributes.containingText)));
			}

			if (documentSearchAttributes.searchData) {
				dataFilter = DocumentQuery.META_DATA
						.any()
						.like(QueryUtils.getFormattedString(Text
								.escapeIllegalJcrChars(documentSearchAttributes.containingText)));
			}

			if (null != contentFilter && null != dataFilter) {
				FilterOrTerm filterOrTerm = filter.addOrTerm();
				filterOrTerm.add(contentFilter);
				filterOrTerm.add(dataFilter);
			} else if (null != contentFilter) {
				filter.and(contentFilter);
			} else if (null != dataFilter) {
				filter.and(dataFilter);
			}
		}

		DocumentManagementService documentManagementService = ServiceFactoryUtils
				.getDocumentManagementService();
		Documents docs = documentManagementService.findDocuments(query);

		return docs;
	}
    
	/**
	 * 
	 * @param query
	 * @param filters
	 */
	private void applyFiltering(Query query, FilterDTO filters) {
		DocumentSearchFilterDTO documentSearchFilter = (DocumentSearchFilterDTO) filters;

		FilterAndTerm filter = query.getFilter().addAndTerm();

		if (null != documentSearchFilter.documentName) {

			if (StringUtils
					.isNotEmpty(documentSearchFilter.documentName.textSearch)) {
				filter.and(DocumentQuery.NAME.like(QueryUtils
						.getFormattedString(documentSearchFilter.documentName.textSearch)));
			}
		} else if (null != documentSearchFilter.createDate) {
			Date startTime = new Date(documentSearchFilter.createDate.from);
			Date endTime = new Date(documentSearchFilter.createDate.to);
			if (startTime != null)
				filter.and(DocumentQuery.DATE_CREATED.greaterOrEqual(DateUtils
						.convertToGmt(startTime)));

			if (endTime != null)
				filter.and(DocumentQuery.DATE_CREATED.lessOrEqual(DateUtils
						.convertToGmt(endTime)));

		} else if (null != documentSearchFilter.modificationDate) {
			Date startTime = new Date(
					documentSearchFilter.modificationDate.from);
			Date endTime = new Date(documentSearchFilter.modificationDate.to);
			if (startTime != null)
				filter.and(DocumentQuery.DATE_LAST_MODIFIED
						.greaterOrEqual(DateUtils.convertToGmt(startTime)));

			if (endTime != null)
				filter.and(DocumentQuery.DATE_LAST_MODIFIED
						.lessOrEqual(DateUtils.convertToGmt(endTime)));
		} else if (null != documentSearchFilter.author) {
			if (StringUtils.isNotEmpty(documentSearchFilter.author.textSearch)) {
				filter.and(DocumentQuery.OWNER.like(QueryUtils
						.getFormattedString(documentSearchFilter.author.textSearch)));
			}
		} else if (null != documentSearchFilter.fileType) {

			if (StringUtils
					.isNotEmpty(documentSearchFilter.fileType.textSearch)) {
				filter.and(DocumentQuery.CONTENT_TYPE.like(QueryUtils
						.getFormattedString(documentSearchFilter.fileType.textSearch)));
			}
		} else if (null != documentSearchFilter.documentId) {
			if (StringUtils
					.isNotEmpty(documentSearchFilter.documentId.textSearch)) {
				filter.and(DocumentQuery.ID.like(QueryUtils
						.getFormattedString(documentSearchFilter.documentId.textSearch)));
			}
		} else if (null != documentSearchFilter.documentType) {

			List<String> filterByValues = documentSearchFilter.documentType.like;

			if (!CollectionUtils.isEmpty(filterByValues)
					&& !checkIfAllOptionSelect(filterByValues)) {
				FilterOrTerm filterOrTerm = filter.addOrTerm();
				for (String object : filterByValues) {
					filterOrTerm.add(DocumentQuery.DOCUMENT_TYPE_ID
							.isEqual(object));
				}
			}
		}

		query.where(filter);
	}
	
    /**
     * 
     * @param documentId
     * @return
     */
	public List<ProcessInstanceDTO> getProcessInstancesFromDocument(
			String documentId) {
		ProcessInstanceQuery query = new ProcessInstanceQuery();

		query.where(new DocumentFilter(documentId, null));

		ProcessInstances processInstances = ServiceFactoryUtils
				.getQueryService().getAllProcessInstances(query);

		List<ProcessInstanceDTO> processList = new ArrayList<ProcessInstanceDTO>();
		if (CollectionUtils.isNotEmpty(processInstances)) {
			for (ProcessInstance processInstance : processInstances) {

				ProcessInstanceDTO processInstanceDTO = new ProcessInstanceDTO();
				processInstanceDTO.processName = ProcessInstanceUtils
						.getProcessLabel(processInstance);
				processInstanceDTO.oid = processInstance.getOID();
				processList.add(processInstanceDTO);
			}
		}
		return processList;

	}
    
	/**
	 * 
	 * @param query
	 * @param options
	 */
	private void addSortCriteria(Query query, Options options) {
		if (COL_DOCUMENT_NAME.equals(options.orderBy)) {
			query.orderBy(DocumentQuery.NAME, options.asc);
		} else if (COL_AUTHOR.equals(options.orderBy)) {
			query.orderBy(DocumentQuery.OWNER, options.asc);
		} else if (COL_FILE_TYPE.equals(options.orderBy)) {
			query.orderBy(DocumentQuery.CONTENT_TYPE, options.asc);
		} else if (COL_CREATE_DATE.equals(options.orderBy)) {
			query.orderBy(DocumentQuery.DATE_CREATED, options.asc);
		} else if (COL_MODIFICATION_DATE.equals(options.orderBy)) {
			query.orderBy(DocumentQuery.DATE_LAST_MODIFIED, options.asc);
		} else if (COL_DOCUMENT_ID.equals(options.orderBy)) {
			query.orderBy(DocumentQuery.ID, options.asc);
		} else if (COL_DOCUMENT_TYPE.equals(options.orderBy)) {
			query.orderBy(DocumentQuery.DOCUMENT_TYPE_ID, options.asc);
		}
	}

	/**
	 * prepares the Document Version list for display purpose
	 * 
	 * @return
	 * @throws ResourceNotFoundException
	 */
	public List<DocumentVersionDTO> getDocumentVersions(String id)
			throws ResourceNotFoundException {
		Document document = DocumentMgmtUtility.getDocument(id);
		JCRVersionTracker vt = new JCRVersionTracker(document);
		List<DocumentVersionDTO> documentVersionList = new ArrayList<DocumentVersionDTO>();
		Map<Integer, Document> docVersions = vt.getVersions();
		if (docVersions.size() > 0) {
			TreeSet<Integer> sortedVersions = new TreeSet<Integer>(
					docVersions.keySet());
			int version;
			DocumentVersionDTO docVersion = null;
			String documentName = "";
			for (Iterator<Integer> iterator = sortedVersions.iterator(); iterator
					.hasNext();) {
				version = (Integer) iterator.next();
				docVersion = new DocumentVersionDTO(version,
						(Document) docVersions.get(version));

				if (documentName.equals(docVersion.getDocumentName())) {
					docVersion.setDocumentName("");
				} else {
					documentName = docVersion.getDocumentName();
				}
				documentVersionList.add(docVersion);
			}
			Collections.reverse(documentVersionList);
		}
		return documentVersionList;
	}
    
	/**
	 * 
	 * @param searchValue
	 * @param onlyActive
	 * @param maxMatches
	 * @return
	 */
	public List<User> searchUsers(String searchValue, boolean onlyActive,
			int maxMatches) {
		return UserUtils.searchUsers(searchValue, true, 20);
	}
    
	/**
	 * 
	 * @param selectedValues
	 * @return
	 */
	private boolean checkIfAllOptionSelect(List<String> selectedValues) {
		for (String value : selectedValues) {
			if ("All".equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}
    
	/**
	 * 
	 * @return
	 */
	public List<SelectItemDTO> loadAvailableProcessDefinitions() {
		List<View> openViews = PortalApplication.getInstance().getOpenViews();
		List<SelectItemDTO> allProcessDefns = new ArrayList<SelectItemDTO>();
		for (Iterator<View> iterator = openViews.iterator(); iterator.hasNext();) {
			View view = (View) iterator.next();
			if (VIEW_ACTIVITY_PANEL.equals(view.getName())) {
				Object activityOid = (String) view.getViewParams().get("oid");
				if (null != activityOid && activityOid instanceof String) {
					long activityOidLong = Long
							.parseLong(((String) activityOid).trim());
					ActivityInstance actInstance = ActivityInstanceUtils
							.getActivityInstance(activityOidLong);
					ProcessInstance pi = actInstance.getProcessInstance();
					StringBuffer processLabel = new StringBuffer(
							I18nUtils.getProcessName(ProcessDefinitionUtils
									.getProcessDefinition(pi.getProcessID())));
					processLabel.append(" (#").append(pi.getOID()).append(")");
					allProcessDefns.add(new SelectItemDTO(String.valueOf(pi
							.getOID()), processLabel.toString()));
				}
			}
		}
		return allProcessDefns;
	}
    
	/**
     * 
     * @param processOid
     * @param documentId
     * @return
     * @throws ResourceNotFoundException
     */
	public InfoDTO attachDocuments(Long processOid, String documentId)
			throws ResourceNotFoundException {
		InfoDTO infoDTO = null;
		ProcessInstance pi = null;
		try {
			pi = ProcessInstanceUtils.getProcessInstance(processOid);
		} catch (Exception e) { // Todo for Errors
			return new InfoDTO(MessageType.ERROR, MessagesViewsCommonBean
					.getInstance().getString(
							"views.common.process.invalidProcess.message"));

		}

		if (DocumentMgmtUtility.isProcessAttachmentAllowed(pi)) {
			List<Document> documentList = new ArrayList<Document>();
			Document selectedDoc = DocumentMgmtUtility.getDocument(documentId);
			if (null != selectedDoc) {// single document is selected
				documentList.add(selectedDoc);
			} else { // multiple documents are selected
						// Can be implemented later as for me it was not in
						// scope.
			}

			if (documentList.size() > 0) {
				// create copy of the documents and update process instance
				try {
					if (DocumentMgmtUtility.getDuplicateDocuments(pi,
							documentList).size() > 0) {
						return new InfoDTO(
								MessageType.ERROR,
								MessagesViewsCommonBean
										.getInstance()
										.getString(
												"views.common.process.duplicateDocAttached.message"));
					}
					DocumentMgmtUtility.addDocumentsToProcessInstance(pi,
							documentList);
					infoDTO = new InfoDTO(
							MessageType.INFO,
							MessagesViewsCommonBean
									.getInstance()
									.getString(
											"views.common.process.documentAttachedSuccess.message"));
				} catch (Exception e) {
					return new InfoDTO(
							MessageType.ERROR,
							MessagesViewsCommonBean
									.getInstance()
									.getString(
											"views.common.process.documentAttachedFailure.message"));
				}
			}
		} else {
			infoDTO = new InfoDTO(MessageType.ERROR, MessagesViewsCommonBean
					.getInstance().getString(
							"views.common.process.invalidProcess.message"));
		}

		return infoDTO;
	}

}
