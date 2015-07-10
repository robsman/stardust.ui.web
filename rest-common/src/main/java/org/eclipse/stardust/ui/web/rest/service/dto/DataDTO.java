package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@DTOClass
public class DataDTO extends AbstractDTO
{
	@DTOAttribute("elementOID")
	public long oid;

	@DTOAttribute("id")
	public String id;

	@DTOAttribute("name")
	public String name;

	@DTOAttribute("description")
	public String description;

	@DTOAttribute("qualifiedId")
	public String qualifiedId;

	@DTOAttribute("typeId")
	public String typeId;

}