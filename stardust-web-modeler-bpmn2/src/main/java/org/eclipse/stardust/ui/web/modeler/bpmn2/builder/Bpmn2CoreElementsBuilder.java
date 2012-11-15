package org.eclipse.stardust.ui.web.modeler.bpmn2.builder;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.createInternalId;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils.setExtensionAttribute;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ProcessType;

import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.model.ModelJto;
import org.eclipse.stardust.ui.web.modeler.model.ProcessDefinitionJto;

public class Bpmn2CoreElementsBuilder
{
   public Definitions createModel(ModelJto jto)
   {
      Definitions model = Bpmn2Utils.bpmn2Factory().createDefinitions();
      model.setName(jto.name);

      model.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.deriveElementIdFromName(jto.name));

      setExtensionAttribute(model, ModelerConstants.UUID_PROPERTY, createInternalId());

      // TODO review, externalize values
      model.setExporter("Eclipse Stardust");
      model.setExporterVersion(CurrentVersion.getVersionName());

      // TODO expression language: JavaScript
      // TODO type language: XSD (this is the default, though)

      // TODO verify URL compatibility of ID
      model.setTargetNamespace("http://eclipse.org/stardust/model/" + model.getId());

      DocumentRoot docRoot = Bpmn2Utils.bpmn2Factory().createDocumentRoot();
      docRoot.setDefinitions(model);

      return model;
   }

   public Process createProcess(Definitions model, ProcessDefinitionJto jto)
   {
      Process process = Bpmn2Utils.bpmn2Factory().createProcess();

      process.setName(jto.name);
      process.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.deriveElementIdFromName(jto.name));

      // apply defaults
      process.setProcessType(ProcessType.PRIVATE);
      process.setIsExecutable(true);

      return process;
   }

   public void attachProcess(Definitions model, Process process)
   {
      assert (null == process.eContainer());

      model.getRootElements().add(process);
   }

}
