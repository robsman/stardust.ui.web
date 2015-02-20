package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.DocumentToolTip;
import org.eclipse.stardust.ui.web.viewscommon.common.ToolTip;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.FormatterUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;

public class DocumentSearchResultDTO {
	private Date createDate;
	private Date modificationDate;
	// private Document document;
	private String author = "";
	private String containingText;
	private String fileType;
	private String documentType;
	private List<Pair<String, String>> metadata;

	// ~ Instance fields
	// ================================================================================================
	private String documentId;
	private String documentName;
	private String documentPath;
	private String repositoryId;
	private String documentOwner = "";
	// private User user;
	private long fileSize;
	private boolean selectedRow;
	private String fileSizeLabel;
	private ToolTip documentToolTip;

	private String iconPath;

	// ~ Constructor
	// ================================================================================================

	public DocumentSearchResultDTO(Document doc) {
		this.documentId = doc.getId();
		this.documentName = doc.getName();
		this.fileType = doc.getContentType();
		this.documentType = TypedDocumentsUtil.getDocumentTypeLabel(doc
				.getDocumentType());
		this.createDate = doc.getDateCreated();
		this.modificationDate = doc.getDateLastModified();
		this.fileSize = doc.getSize();
		this.fileSizeLabel = DocumentMgmtUtility
				.getHumanReadableFileSize(this.fileSize);
		this.documentPath = getFolderFromFullPath(doc.getPath());
		this.repositoryId = RepositoryIdUtils.extractRepositoryId(doc);
		this.iconPath = getIconPath();
		// this.document = doc;

		this.documentOwner = doc.getOwner();

		User user = DocumentMgmtUtility.getOwnerOfDocument(doc);
		if (null != user) {
			author = FormatterUtils.getUserLabel(user);
		} else if (StringUtils.isNotEmpty(doc.getOwner())) {
			author = doc.getOwner();
		}

		documentToolTip = new DocumentToolTip(null, doc);

		metadata = TypedDocumentsUtil.getMetadataAsList(doc, true);
		if (metadata.size() > 5) // Requirement is to only show first 5 entries
		{
			metadata = metadata.subList(0, 5);
		}

	}

	public final String getAuthor() {
		return author;
	}

	public String getContainingText() {
		return containingText;
	}

	public String getFileType() {
		return fileType;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public String getDocumentId() {
		return documentId;
	}

	public String getDocumentName() {
		return documentName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public String getFileSizeLabel() {
		return fileSizeLabel;
	}

	public String getIconPath() {
		MIMEType mimeType = MimeTypesHelper.detectMimeType(documentName,
				fileType);
		if (null != mimeType) {
			return mimeType.getIconPath();
		} else {
			return MimeTypesHelper.DEFAULT.getIconPath();
		}
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setContainingText(String containingText) {
		this.containingText = containingText;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public final String getDocumentPath() {
		return documentPath;
	}

	public final void setDocumentPath(String documentPath) {
		this.documentPath = documentPath;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public boolean isSelectedRow() {
		return selectedRow;
	}

	public void setSelectedRow(boolean selectedRow) {
		this.selectedRow = selectedRow;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public ToolTip getDocumentToolTip() {
		return documentToolTip;
	}

	public List<Pair<String, String>> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Pair<String, String>> metadata) {
		this.metadata = metadata;
	}

	/**
	 * method returns folder path from full document path.
	 * 
	 * @param fullPath
	 * @return
	 */
	private static String getFolderFromFullPath(String fullPath) {
		if (StringUtils.isNotEmpty(fullPath)) {
			int lastSeperator = fullPath.lastIndexOf("/");
			return fullPath.substring(0, lastSeperator);
		}
		return fullPath;
	}
}
