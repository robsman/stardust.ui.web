package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */

@XmlRootElement(name = "UserGroupDTO")
public class UserGroupDTO extends AbstractDTO {
	
	@DTOAttribute("id")
	private String id;
	
	@DTOAttribute("name")
	private String name;
	
	@DTOAttribute("validFrom")
	private Date validFrom;
	
	@DTOAttribute("validTo")
	private Date validTo;
	
	@DTOAttribute("description")
	private String description;
	
	/**
	 * 
	 */
	public UserGroupDTO() {
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the validFrom
	 */
	public Date getValidFrom() {
		return validFrom;
	}

	/**
	 * @param validFrom the validFrom to set
	 */
	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	/**
	 * @return the validTo
	 */
	public Date getValidTo() {
		return validTo;
	}

	/**
	 * @param validTo the validTo to set
	 */
	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	
//	private String password;
	
	

}
