package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * 
 * @author Nikhil.Gahlot
 *
 */
@DTOClass
public class SelectItemDTO extends AbstractDTO{

	   @DTOAttribute("value")
	   public String value;

	   @DTOAttribute("label")
	   public String label;

	public SelectItemDTO(String value, String label) {
		super();
		this.value = value;
		this.label = label;
	}

	public SelectItemDTO() {
		// TODO Auto-generated constructor stub
	}
	   
	   
}
