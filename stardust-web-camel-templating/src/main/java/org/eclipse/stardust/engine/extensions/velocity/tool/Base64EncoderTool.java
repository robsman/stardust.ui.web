package org.eclipse.stardust.engine.extensions.velocity.tool;

import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;

@DefaultKey("base64Encoder")
@InvalidScope({Scope.APPLICATION, Scope.SESSION})
public class Base64EncoderTool extends SafeConfig
{

   public void configure(ValueParser parser)
   {

   }

   public String encodeBase64String(byte[] bytes)
   {
      return new String(Base64.encodeBase64(bytes));
   }
}
