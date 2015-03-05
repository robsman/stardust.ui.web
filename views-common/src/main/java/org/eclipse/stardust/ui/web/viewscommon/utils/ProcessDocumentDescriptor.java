package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.List;

import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentInfo;


public class ProcessDocumentDescriptor extends ProcessDescriptor{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6291058549843019654L;

	private List<DocumentInfo> documents;
	
	public ProcessDocumentDescriptor(String id, String key, String value,
			List<DocumentInfo> isDocumemnt) {
		super(id, key, value);
		setDocuments(isDocumemnt);
	}

	public List<DocumentInfo> getDocuments() {
		return documents;
	}

	public void setDocuments(List<DocumentInfo> documents) {
		this.documents = documents;
	}



}
