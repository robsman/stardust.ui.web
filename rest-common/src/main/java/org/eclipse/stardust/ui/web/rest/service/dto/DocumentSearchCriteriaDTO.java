/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Date;
import java.util.List;

public class DocumentSearchCriteriaDTO {
	public boolean showAll;
	public Date createDateFrom;
	public Date createDateTo;
	public Date modificationDateFrom;
	public Date modificationDateTo;
	public String author;
	public String documentId;
	public String documentName;
	public String documentPath;
	public List<String> selectedFileTypes;
	public String advancedFileType;
	public List<String> selectedDocumentTypes;
	public List<String> selectedRepository;
	public String containingText;
	public boolean searchContent;
	public boolean searchData;
	public String selectedFileSize;
	public boolean selectFileTypeAdvance;
}
