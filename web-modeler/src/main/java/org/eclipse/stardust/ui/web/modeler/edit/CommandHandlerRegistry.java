package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.BeanFactory;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class CommandHandlerRegistry
{
   private static final Logger trace = LogManager.getLogger(CommandHandlerRegistry.class);

   private final Map<String, List<HandlerRegistration>> handlerRegistry = newHashMap();

   public <T extends EObject> ICommandHandlerInvoker findCommandHandler(String commandId,
         T targetElement)
   {
      List<HandlerRegistration> handlers = handlerRegistry.get(commandId);

      // TODO only invoke most specific handler?
      for (final HandlerRegistration handler : handlers)
      {
         if (handler.isCompatibleWith(targetElement))
         {
            return handler;
         }
      }

      return null;
   }

   public void registerCommandHandler(String commandId, BeanFactory beanFactory,
         String beanName, Method handlerMethod)
   {
      List<HandlerRegistration> handlers = handlerRegistry.get(commandId);
      if (null == handlers)
      {
         handlers = newArrayList();
         handlerRegistry.put(commandId, handlers);
      }

      handlers.add(new HandlerRegistration(beanFactory, beanName, handlerMethod));
   }

   public static interface ICommandHandlerInvoker
   {
      void handleCommand(String commandId, EObject targetElement, JsonObject request);
   }

   protected static class HandlerRegistration implements ICommandHandlerInvoker
   {
      final BeanFactory beanFactory;

      final String beanName;

      final Method handlerMethod;

      public HandlerRegistration(BeanFactory beanFactory, String beanName,
            Method handlerMethod)
      {
         this.beanFactory = beanFactory;
         this.beanName = beanName;
         this.handlerMethod = handlerMethod;
      }

      <T extends EObject> boolean isCompatibleWith(T targetElement)
      {
         return isCompatibleWith(targetElement.getClass());
      }

      boolean isCompatibleWith(Class<? > targetType)
      {
         // TODO parameter annotations instead of positions?
         return (1 <= handlerMethod.getParameterTypes().length)
               && handlerMethod.getParameterTypes()[0].isAssignableFrom(targetType);
      }

      @Override
      public void handleCommand(String commandId, EObject targetElement,
            JsonObject request)
      {
         Object handlerBean = beanFactory.getBean(beanName);
         try
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("About to invoke handler for '" + commandId + "' command: "
                     + handlerBean + "#" + handlerMethod.getName() + "]");
            }
            // TODO parameter annotations instead of positions?
            handlerMethod.invoke(handlerBean, targetElement, request);
         }
         catch (IllegalArgumentException iae)
         {
            trace.error("Failed invoking handler for '" + commandId + "' command.", iae);
         }
         catch (IllegalAccessException iae)
         {
            trace.error("Failed invoking handler for '" + commandId + "' command.", iae);
         }
         catch (InvocationTargetException ite)
         {
            trace.error("Failed invoking handler for '" + commandId + "' command.",
                  ite.getTargetException());
         }
      }
   }
}
