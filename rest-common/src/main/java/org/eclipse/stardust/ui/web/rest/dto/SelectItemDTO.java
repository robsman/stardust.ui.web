package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

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
