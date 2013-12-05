/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.validation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.beans.EventHandlerBean;
import org.eclipse.stardust.engine.core.model.beans.TransitionBean;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.EventHandlerType;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.XmlTextNode;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.modeling.validation.IModelElementValidator;
import org.eclipse.stardust.modeling.validation.Issue;
import org.eclipse.stardust.modeling.validation.ValidationException;

public class EventValidator implements IModelElementValidator
{
   private static final String TAG_INTERMEDIATE_EVENT_HOST = "stardust:bpmnIntermediateEventHost"; //$NON-NLS-1$
      
   public Issue[] validate(IModelElement element) throws ValidationException
   {
      List<Issue> issues = new ArrayList<Issue>();
            
      EventHandlerType event = (EventHandlerType) element;
      
      ActivityType activity = ModelUtils.findContainingActivity(event);
      List<TransitionType> outTransitions = activity.getOutTransitions();
      List<TransitionType> inTransitions = activity.getInTransitions();
            
      checkBoundaryEventConsistency(event, outTransitions, issues);
      checkBoundaryEventsConsistency(activity.getEventHandler(), issues);
      checkIntermediateEventConsistency(activity, outTransitions, inTransitions, issues);
         
      return issues.toArray(new Issue[issues.size()]);
   }

   private void checkBoundaryEventConsistency(EventHandlerType event, List<TransitionType> outTransitions, List<Issue> issues)
   {
      if (AttributeUtil.getAttribute(event, EventHandlerBean.BOUNDARY_EVENT_TYPE_KEY) != null)
      {
         TransitionType exceptionTransition = getExceptionTransition(outTransitions, event.getId());
         if (exceptionTransition == null)
         {    
            issues.add(new Issue(Issue.WARNING, event, "No exception flow transition for event handler with ID '" + event.getId() + "'."));            
         }
      }
   }
   
   public TransitionType getExceptionTransition(List<TransitionType> outTransitions, String eventHandlerId)
   {
      if (outTransitions == null)
      {
         return null;
      }
      
      String condition = TransitionBean.ON_BOUNDARY_EVENT_PREDICATE + "(" + eventHandlerId + ")";
      for (TransitionType t : outTransitions)
      {
         String expression = getExpression(t);
         if (expression != null && condition.equals(expression))
         {
            return t;
         }
      }
      return null;
   }
   
   private String getExpression(TransitionType transition)
   {
      XmlTextNode type = transition.getExpression();
      String expression = type == null ? null : ModelUtils.getCDataString(transition.getExpression().getMixed());
      return expression;
   }
      
   private void checkBoundaryEventsConsistency(List<EventHandlerType> eventHandlers, final List<Issue> issues)
   {
      ActivityType activity = null;
      
      for (int i=0; i<eventHandlers.size(); i++)
      {
         EventHandlerType x = (EventHandlerType) eventHandlers.get(i);
         if(activity == null)
         {
            activity = ModelUtils.findContainingActivity(x);
         }
         
         if ( !isErrorBoundaryEvent(x))
         {
            continue;
         }
         
         for (int j=i+1; j<eventHandlers.size(); j++)
         {
            EventHandlerType y = (EventHandlerType) eventHandlers.get(j);
            if(activity == null)
            {
               activity = ModelUtils.findContainingActivity(y);
            }
            
            if ( !isErrorBoundaryEvent(y))
            {
               continue;
            }

            if ( !exceptionHierarchiesAreDisjunct(x, y))
            {
               issues.add(new Issue(Issue.WARNING, activity, "Multiple boundary events for exceptions not having disjunct type hierarchies ('"
                                                      + x.getId() + "' and '" + y.getId() + "'). Only one will be processed during event handling."));                                                       
            }
         }
      }
   }
   
   private boolean isErrorBoundaryEvent(EventHandlerType x)
   {
      if (AttributeUtil.getAttribute(x, EventHandlerBean.BOUNDARY_EVENT_TYPE_KEY) == null)
      {
         return false;
      }
      
      if ( !PredefinedConstants.EXCEPTION_CONDITION.equals(x.getType().getId()))
      {
         return false;
      }
      
      return true;
   }
   
   private boolean exceptionHierarchiesAreDisjunct(EventHandlerType x, EventHandlerType y)
   {
      String xExceptionName = (String) AttributeUtil.getAttributeValue(x, PredefinedConstants.EXCEPTION_CLASS_ATT);
      String yExceptionName = (String) AttributeUtil.getAttributeValue(y, PredefinedConstants.EXCEPTION_CLASS_ATT);
      
      Class<?> xException = Reflect.getClassFromClassName(xExceptionName);
      Class<?> yException = Reflect.getClassFromClassName(yExceptionName);
      
      if (xException.isAssignableFrom(yException) || yException.isAssignableFrom(xException))
      {
         return false;
      }
      
      return true;
   }
   
   private void checkIntermediateEventConsistency(ActivityType activity, List<TransitionType> outTransitions, List<TransitionType> inTransitions, List<Issue> issues)
   {
      Boolean isIntermediateEvent = (Boolean) AttributeUtil.getBooleanValue(activity, TAG_INTERMEDIATE_EVENT_HOST);
      if (isIntermediateEvent != null && isIntermediateEvent.booleanValue())
      {
         if (inTransitions.size() != 1 || outTransitions.size() != 1)
         {
            issues.add(new Issue(Issue.WARNING, activity, "Intermediate events must have one inbound and one outbound sequence flow."));            
         }
      }
   }
}