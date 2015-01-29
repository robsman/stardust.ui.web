package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil.ICON_COLOR;

public class CriticalityDTO extends AbstractDTO {

	public int value;

	@DTOAttribute("iconColor")
	public String color;

	@DTOAttribute("label")
	public String label;

	@DTOAttribute("iconCount")
	public int count;

	public void setColor(ICON_COLOR iconColor) {
		this.color = iconColor.toString();
	}

}
