package org.eclipse.stardust.engine.extensions.templating.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.velocity.VelocityContext;

import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.utils.TemplateUtils;

public class XDocVelocityContext extends VelocityContext implements IContext
{
   public XDocVelocityContext(VelocityContext velocityContext)
   {
      super(velocityContext);
   }

   /**
    * Overridden so that the <code>null</code> values are accepted.
    * 
    * @see AbstractVelocityContext#put(String,Object)
    */
   public Object put(String key, Object value)
   {
      if (key == null)
      {
         return null;
      }
      Object result = TemplateUtils.putContextForDottedKey(this, key, value);
      if (result == null)
      {
         return this.internalPut(key, value);
      }
      return result;
   }

   public void putMap(Map<String, Object> contextMap)
   {
      Set<Entry<String, Object>> entries = contextMap.entrySet();
      for (Entry<String, Object> entry : entries)
      {
         put(entry.getKey(), entry.getValue());
      }
   }

   public Map<String, Object> getContextMap()
   {
      Map<String, Object> contextMap = new HashMap<String, Object>();
      Object[] keys = this.getKeys();
      for (int i = 0; i < keys.length; i++)
      {
         if (keys[i] != null)
         {
            String key = keys[i].toString();
            contextMap.put(key, get(key));
         }
      }
      return contextMap;
   }

}
