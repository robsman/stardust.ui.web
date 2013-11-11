package org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup;

import static edu.emory.mathcs.backport.java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.eclipse.stardust.common.CollectionUtils.isEmpty;

import java.util.Map;

public class MashupContext
{
   public final String uri;

   public final Map<String, String> credentials;

   public MashupContext(String mashupUri, Map<String, String> credentials)
   {
      this.uri = mashupUri;
      this.credentials = !isEmpty(credentials)
            ? unmodifiableMap(credentials)
            : emptyMap();
   }
}
