package org.eclipse.stardust.ui.web.modeler.bpmn2.edit;

import static java.util.Collections.unmodifiableSet;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.util.Collections;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TouchedElementsCollector
{
   private Set<EObject> touchedElements;

   public void touchElement(EObject element)
   {
      if (null == touchedElements)
      {
         this.touchedElements = newHashSet();
      }
      touchedElements.add(element);
   }

   public Set<EObject> getTouchedElements()
   {
      return (null != touchedElements)
            ? unmodifiableSet(touchedElements)
            : Collections.<EObject> emptySet();
   }
}
