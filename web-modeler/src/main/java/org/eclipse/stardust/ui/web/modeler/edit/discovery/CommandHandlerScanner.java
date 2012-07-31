package org.eclipse.stardust.ui.web.modeler.edit.discovery;

import java.lang.reflect.Method;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.edit.CommandHandlerRegistry;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;

/**
 * Bean factory post processor to automatically detect and register command handlers. See
 * {@link CommandHandler} and {@link OnCommand}.
 * <p>
 * This is a Spring bean but needs to be wired up with XML due to SPR-4935.
 *
 * @author Robert.Sauer
 */
public class CommandHandlerScanner implements BeanFactoryPostProcessor
{
   private static final Logger trace = LogManager.getLogger(CommandHandlerScanner.class);

   private final CommandHandlerRegistry registry;

   public CommandHandlerScanner(CommandHandlerRegistry registry)
   {
      this.registry = registry;
   }

   @Override
   public void postProcessBeanFactory(ConfigurableListableBeanFactory bf)
         throws BeansException
   {
      // TODO
      for (String beanName : bf.getBeanDefinitionNames())
      {
         BeanDefinition candidateDef = bf.getBeanDefinition(beanName);

         if ( !candidateDef.isAbstract())
         {
            Class<? > beanType = bf.getType(beanName);
            if (null != beanType)
            {
               if (null != AnnotationUtils.findAnnotation(beanType, CommandHandler.class))
               {
                  // found a handler bean definition, scan for and register handler
                  // methods
                  registerOnCommandMethods(bf, beanName, beanType);
               }
            }
         }
      }
   }

   protected void registerOnCommandMethods(ConfigurableListableBeanFactory bf,
         String beanName, Class<? > beanType)
   {
      for (Method candidateMethod : beanType.getMethods())
      {
         OnCommand onCmdAnnotation = AnnotationUtils.findAnnotation(candidateMethod,
               OnCommand.class);
         if (null != onCmdAnnotation)
         {
            // verify arguments
            // TODO parameter annotations instead of positions?
            if ((2 == candidateMethod.getParameterTypes().length)
                  && EObject.class.isAssignableFrom(candidateMethod.getParameterTypes()[0])
                  && (JsonObject.class == candidateMethod.getParameterTypes()[1]))
            {
               registry.registerCommandHandler(onCmdAnnotation.commandId(), bf, beanName,
                     candidateMethod);

               trace.info("Discovered handler for '" + onCmdAnnotation.commandId()
                     + "' command: " + beanName + "[" + candidateMethod + "]");
            }
            else
            {
               trace.error("Ignoring handler for '" + onCmdAnnotation.commandId()
                     + "' command: " + beanName + "[" + candidateMethod
                     + "]: incompatible argument list.");
            }
         }
      }
   };
}
