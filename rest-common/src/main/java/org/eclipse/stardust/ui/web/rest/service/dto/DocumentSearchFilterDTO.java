package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.FilterDTO;

public class DocumentSearchFilterDTO implements FilterDTO {

public TextSearchDTO documentName;

public TextSearchDTO fileType;

public TextSearchDTO documentId;

public TextSearchDTO author;

public RangeDTO createDate;

public RangeDTO modificationDate;

public EqualsDTO documentType;
	
}
