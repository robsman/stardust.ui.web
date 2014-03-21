package org.eclipse.stardust.ui.web.reporting.ui.i18n;

import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.ui.web.common.util.AbstractMessageBean;

/**
 *
 * @author Marc.Gille
 *
 */

@Component(value=Messages.BEAN_NAME)
@Scope(value = "session")
public class Messages extends AbstractMessageBean
{
   private static final String BUNDLE_NAME = "bpm-reporting-messages";
   public static final String BEAN_NAME = "bpmReportingMessages";

   public Messages()
   {
      super("carnot");

      ResourceBundle resBundle = ResourceBundle.getBundle(BUNDLE_NAME, FacesContext.getCurrentInstance()
            .getExternalContext().getRequestLocale());
      Reflect.setFieldValue(this, "bundle", resBundle);
   }

   public static Messages getInstance()
   {
      return (Messages) FacesContext.getCurrentInstance().getApplication().getVariableResolver()
            .resolveVariable(FacesContext.getCurrentInstance(), BEAN_NAME);
   }
}
