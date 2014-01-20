package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.findContainingModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.eclipse.stardust.ui.web.modeler.spi.ModelFormat;
import org.eclipse.stardust.ui.web.modeler.spi.ModelingSessionScoped;

@Service
@ModelFormat(ModelFormat.BPMN2)
@ModelingSessionScoped
public class Bpmn2Binding extends ModelBinding<Definitions>
{
   static
   {
      ModelBinding.trace.info("Loaded BPMN2 model binding.");
   }

   private AtomicLong oidGenerator = new AtomicLong(1L);

   private final ConcurrentMap<Definitions, ConcurrentMap<EObject, String>> uuidRegistry = newConcurrentHashMap();

   private final ConcurrentMap<Definitions, ConcurrentMap<EObject, Long>> oidRegistry = newConcurrentHashMap();

   private final Bpmn2Navigator navigator;

   private final Bpmn2ModelMarshaller marshaller;

   private final Bpmn2ModelUnmarshaller unmarshaller;

   @Autowired
   public Bpmn2Binding(ModelingSession session)
   {
      super(session);

      this.navigator = new Bpmn2Navigator(this);
      this.marshaller = new Bpmn2ModelMarshaller(this);
      this.unmarshaller = new Bpmn2ModelUnmarshaller(this);
   }

   @Override
   public boolean isCompatible(EObject model)
   {
      return model instanceof Definitions;
   }

   @Override
   public String getModelFormat(Definitions model)
   {
      return "bpmn2";
   }

   @Override
   public String getModelId(Definitions model)
   {
      return Bpmn2Utils.getModelUuid(model);
   }

   @Override
   public Bpmn2Navigator getNavigator()
   {
      return navigator;
   }

   @Override
   public Bpmn2ModelMarshaller getMarshaller()
   {
      return marshaller;
   }

   @Override
   public Bpmn2ModelUnmarshaller getUnmarshaller()
   {
      return unmarshaller;
   }

   @Override
   public boolean isReadOnly(Definitions model)
   {
      // no BPMN2 specific read-only flag (for teh time being)
      return false;
   }

   public String getModelFileName(Definitions model)
   {
      return getModelingSession().modelRepository().getModelFileName(model);
   }

   public String findUuid(BaseElement element)
   {
      Definitions model = findContainingModel(element);
      if (null != model)
      {
         return findUuid(model, element);
      }

      throw new IllegalArgumentException("Element must be part of a BPMN2 model.");
   }

   public String findUuid(Definitions model, EObject element)
   {
      if (null == element)
      {
         throw new IllegalArgumentException("Element must not be null");
      }

      ConcurrentMap<EObject, String> modelUuids = uuidRegistry.get(model);
      if (null == modelUuids)
      {
         uuidRegistry.putIfAbsent(model, new ConcurrentHashMap<EObject, String>());
         modelUuids = uuidRegistry.get(model);
      }

      if (!modelUuids.containsKey(element))
      {
         String uuid = null;
         if (element instanceof BaseElement)
         {
            uuid = Bpmn2ExtensionUtils.getExtensionAttribute((BaseElement) element,
                  "uuid");
         }

         if (isEmpty(uuid))
         {
            uuid = Bpmn2Utils.createInternalId();
         }

         modelUuids.putIfAbsent(element, uuid);
      }

      return modelUuids.get(element);
   }

   public EObject findElementByUuid(Definitions model, String uuid)
   {
      if (null == model)
      {
         throw new IllegalArgumentException("Model must not be null");
      }

      Map<EObject, String> modelUuids = uuidRegistry.get(model);
      if (null != modelUuids)
      {
         for (Map.Entry<EObject, String> entry : modelUuids.entrySet())
         {
            if (uuid.equals(entry.getValue()))
            {
               return entry.getKey();
            }
         }
      }

      return null;
   }

   public void reassociateUuid(Definitions model, EObject oldElement, EObject newElement)
   {
      ConcurrentMap<EObject, String> modelUuids = uuidRegistry.get(model);
      String uuid = modelUuids.get(oldElement);
      if (!isEmpty(uuid))
      {
         modelUuids.putIfAbsent(newElement, uuid);
         modelUuids.remove(oldElement, uuid);
      }
   }

   public Long findOid(BaseElement element)
   {
      Definitions model = findContainingModel(element);
      if (null != model)
      {
         return findOid(model, element);
      }

      throw new IllegalArgumentException("Element must be part of a BPMN2 model.");
   }

   public Long findOid(DiagramElement element)
   {
      Definitions model = findContainingModel(element);
      if (null != model)
      {
         return findOid(model, element);
      }

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
      if (!modelOids.containsKey(element))
      {
         modelOids.putIfAbsent(element, oidGenerator.getAndIncrement());
      }

      return modelOids.get(element);
   }

   public EObject findElementByOid(Definitions model, long oid)
   {
      if (null == model)
      {
         throw new IllegalArgumentException("Model must not be null");
      }

      Map<EObject, Long> modelOids = oidRegistry.get(model);
      if (null != modelOids)
      {
         for (Map.Entry<EObject, Long> entry : modelOids.entrySet())
         {
            if (oid == entry.getValue())
            {
               return entry.getKey();
            }
         }
      }

      return null;
   }

   public void reassociateOid(Definitions model, EObject oldElement, EObject newElement)
   {
      ConcurrentMap<EObject, Long> modelOids = oidRegistry.get(model);
      Long oid = modelOids.get(oldElement);
      if (null != oid)
      {
         modelOids.putIfAbsent(newElement, oid);
         modelOids.remove(oldElement, oid);
      }
   }

}
