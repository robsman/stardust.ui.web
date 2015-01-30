package org.eclipse.stardust.ui.web.business_object_management.portal.messages;

import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.ui.web.common.util.AbstractMessageBean;

/**
 *
 * @author Marc.Gille
 *
 */
@SuppressWarnings("unchecked")
public class Messages extends AbstractMessageBean {

	private static final long serialVersionUID = 1L;

	private static final String BUNDLE_NAME = "business-object-management-messages";
	private static final String BEAN_NAME = "businessObjectManagementMessages";

	public Messages() {
		super("carnot");

		ResourceBundle resBundle = ResourceBundle.getBundle(BUNDLE_NAME,
				FacesContext.getCurrentInstance().getExternalContext()
						.getRequestLocale());
		Reflect.setFieldValue(this, "bundle", resBundle);
	}

	public static Messages getInstance() {
		return (Messages) FacesContext.getCurrentInstance().getApplication()
				.getVariableResolver()
				.resolveVariable(FacesContext.getCurrentInstance(), BEAN_NAME);
	}
}
