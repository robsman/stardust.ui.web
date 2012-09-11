package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.ui.web.modeler.model.ModelElementJto;
import org.eclipse.stardust.ui.web.modeler.model.di.NodeSymbolJto;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;

public class Bpmn2Binding extends ModelBinding
{
   private AtomicLong oidGenerator = new AtomicLong(1L);

   private final ConcurrentMap<Definitions, ConcurrentMap<EObject, Long>> oidRegistry = newConcurrentHashMap();

   public Bpmn2Binding()
   {
      super(new Bpmn2ModelMarshaller(), new Bpmn2ModelUnmarshaller());

      ((Bpmn2ModelMarshaller) marshaller).setBinding(this);
      ((Bpmn2ModelUnmarshaller) unmarshaller).setBinding(this);
   }

   @Override
   public boolean isCompatible(EObject model)
   {
      return model instanceof Definitions;
   }

   public Long findOid(BaseElement element)
   {
      EObject context = element;
      do
      {
         if (context instanceof Definitions)
         {
            return findOid((Definitions) context, element);
         }
         else
         {
            context = context.eContainer();
         }
      }
      while (context != null);

      throw new IllegalArgumentException("Element must be part of a BPMN2 model.");
   }

   public Long findOid(Definitions model, EObject element)
   {
      if (null == element)
      {
         throw new IllegalArgumentException("Element must not be null");
      }

      ConcurrentMap<EObject, Long> modelOids = oidRegistry.get(model);
      if (null == modelOids)
      {
         oidRegistry.putIfAbsent(model, new ConcurrentHashMap<EObject, Long>());
         modelOids = oidRegistry.get(model);
      }
      if ( !modelOids.containsKey(element))
      {
         modelOids.putIfAbsent(element, oidGenerator.getAndIncrement());
      }

      return modelOids.get(element);
   }

   @Override
   public <T extends ModelElementJto> EObject createModelElement(EObject model, T jto)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void attachModelElement(EObject container, EObject modelElement)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public <T extends NodeSymbolJto<? extends ModelElementJto>> EObject createNodeSymbol(
         EObject model, T jto, EObject modelElement)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void attachNodeSymbol(EObject container, EObject nodeSymbol)
   {
      // TODO Auto-generated method stub

   }

}
