package org.eclipse.stardust.engine.extensions.templating.core;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.camel.component.velocity.CamelVelocityClasspathResourceLoader;
import org.apache.camel.spi.ClassResolver;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.extensions.velocity.component.CamelVelocityDelegateClassLoader;

public class VelocityEngineEvaluator
{
   private final Logger logger = LogManager.getLogger(VelocityEngineEvaluator.class);
   private VelocityEngine velocityEngine;

   public VelocityEngineEvaluator(ClassResolver resolver)
   {
      this.velocityEngine = getVelocityEngine(resolver);
   }

   public StringWriter evaluate(String content, Context velocityContext)
   {
      if(logger.isDebugEnabled())
         logger.debug("-->evaluate: content:"+content+", velocityContext:"+velocityContext.toString());
      StringWriter buffer = new StringWriter();
      String logTag = getClass().getName();
      Reader reader = new StringReader(content);
      velocityEngine.evaluate(velocityContext, buffer, logTag, reader);
      if(logger.isDebugEnabled())
         logger.debug("<--evaluate");
      return buffer;
   }

   private synchronized VelocityEngine getVelocityEngine(ClassResolver resolver)
   {
      if (velocityEngine == null)
      {
         velocityEngine = new VelocityEngine();
         velocityEngine.addProperty("CamelClassResolver", resolver);
         Properties properties = new Properties();
         properties.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "false");
         properties.setProperty(RuntimeConstants.RESOURCE_LOADER,
               "file, class, classpath");
         properties.setProperty("class.resource.loader.class",
               CamelVelocityClasspathResourceLoader.class.getName());
         properties.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
               CommonsLogLogChute.class.getName());
         properties.setProperty(CommonsLogLogChute.LOGCHUTE_COMMONS_LOG_NAME,
               VelocityEngineEvaluator.class.getName());
         properties.setProperty("runtime.log.invalid.references", "true");
         ClassLoader old = Thread.currentThread().getContextClassLoader();
         try
         {
            ClassLoader delegate = new CamelVelocityDelegateClassLoader(old);
            Thread.currentThread().setContextClassLoader(delegate);
            velocityEngine.init(properties);
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(old);
         }
      }
      return velocityEngine;
   }
}
