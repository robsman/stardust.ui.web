package org.eclipse.stardust.ui.web.common.messages;

import java.util.Locale;

/**
 * @author Shrikant.Gangal
 *
 */
public class CommonPropertiesMessageBean extends AbstractMessageBean
{
   private static final long serialVersionUID = 1L;

   private static final String BUNDLE_NAME = "portal-common-messages";

   /**
    * @param locale
    */
   public CommonPropertiesMessageBean(Locale locale)
   {
      super(BUNDLE_NAME, locale);
   }
}
