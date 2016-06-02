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

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.exception.ModelerException;

public class CommandHandlerRegistry
{
   private static final Logger trace = LogManager.getLogger(CommandHandlerRegistry.class);

   private final Map<String, List<HandlerRegistration>> handlerRegistry = newHashMap();

   public <M extends EObject, T extends EObject> ICommandHandlerInvoker findCommandHandler(
         String commandId, M model, T contextElement)
   {
      List<HandlerRegistration> handlers = handlerRegistry.get(commandId);

      // TODO only invoke most specific handler?
      for (final HandlerRegistration handler : handlers)
      {
         try
         {
            if (handler.isCompatibleWith(model, contextElement))
            {
               return handler;
            }
         }
         catch (Exception e)
         {
            // TODO: handle exception
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
         this.handlerRegistry.put(commandId, handlers);
      }

      handlers.add(new HandlerRegistration(beanFactory, beanName, handlerMethod));
   }

   public static interface ICommandHandlerInvoker
   {
      void handleCommand(String commandId, EObject model, EObject targetElement, JsonObject request);
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

      <M extends EObject, T extends EObject> boolean isCompatibleWith(M model,
            T contextElement)
      {
         return isCompatibleWith(model.getClass(), contextElement.getClass());
      }

      <M extends EObject, T extends EObject> boolean isCompatibleWith(Class<M> modelType,
            Class<T> contextElementType)
      {
         // TODO parameter annotations instead of positions?
         if (3 == handlerMethod.getParameterTypes().length)
         {
            return (handlerMethod.getParameterTypes()[0].isAssignableFrom(modelType) && handlerMethod.getParameterTypes()[1].isAssignableFrom(contextElementType));
         }
         else if (2 == handlerMethod.getParameterTypes().length)
         {
            return (handlerMethod.getParameterTypes()[0].isAssignableFrom(modelType) && ((null == contextElementType) || modelType.equals(contextElementType)));
         }
         else
         {
            return false;
         }
      }

      @Override
      public void handleCommand(String commandId, EObject model, EObject contextElement,
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

            if (3 == handlerMethod.getParameterTypes().length)
            {
               // TODO parameter annotations instead of positions?
               handlerMethod.invoke(handlerBean, model, contextElement, request);
            }
            else if ((2 == handlerMethod.getParameterTypes().length) && (model == contextElement))
            {
               // TODO parameter annotations instead of positions?
               handlerMethod.invoke(handlerBean, model, request);
            }
            else
            {
               throw new IllegalArgumentException("Incompatible command handler method: "
                     + handlerMethod);
            }
         }
         catch (IllegalArgumentException iae)
         {
            trace.error("Failed invoking handler for command '" + commandId + "'.", iae);
         }
         catch (IllegalAccessException iae)
         {
            trace.error("Failed invoking handler for command '" + commandId + "'.", iae);
         }
         catch (InvocationTargetException ite)
         {
            if (ite.getTargetException() instanceof ModelerException)
            {
               throw (ModelerException) ite.getTargetException();
            }

            trace.error("Failed invoking handler for command '" + commandId + "'.",
                  ite.getTargetException());

            if (ite.getTargetException() instanceof RuntimeException)
            {
               throw (RuntimeException) ite.getTargetException();
            }
            else
            {
               throw new RuntimeException(ite.getTargetException().getMessage(), ite.getTargetException());
            }
         }
      }
   }
}
