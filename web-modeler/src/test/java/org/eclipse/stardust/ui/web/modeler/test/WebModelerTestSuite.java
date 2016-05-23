/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.ui.web.modeler.test;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.ui.web.modeler.edit.TestModelEditLocking;
import org.eclipse.stardust.ui.web.modeler.edit.batch.TestJsonPathEvaluation;
import org.eclipse.stardust.ui.web.modeler.marshaling.TestGsonMarshaling;
import org.eclipse.stardust.ui.web.modeler.marshaling.TestGsonUnmarshalling;
import org.eclipse.stardust.ui.web.modeler.marshaling.TestIdFromNameGeneration;
import org.eclipse.stardust.ui.web.modeler.portal.spi.TestExtensionsDiscovery;
import org.eclipse.stardust.ui.web.modeler.service.rest.TestModellingSessionRestController;
import org.eclipse.stardust.ui.web.modeler.xpdl.edit.*;
import org.eclipse.stardust.ui.web.modeler.xpdl.edit.batch.TestChecklistEditing;
import org.eclipse.stardust.ui.web.modeler.xpdl.validation.TestModelValidation;
import org.eclipse.stardust.ui.web.modeler.xpdl.validation.TestXpdlValidation;

/**
 * <p>
 * This test suite bundles test classes focussing on administration operations
 * exposed by {@link AdministrationService}.
 * </p>
 *
 * @author Rainer.Pielmann
 */
@RunWith(Suite.class)
@SuiteClasses({
               TestAuthorization.class,
               TestConditionalPerformerInTrigger.class,  
               TestCreateDescriptorAnnotation.class,
               TestCrossModelDataSymbol.class,
               TestCrossModelReferenceTracking.class,
               TestCrossModelSupport.class,
               TestDataDuplicateId.class,
               TestDataMappings.class,
               TestEditXSDImportNoNamespace.class,
               TestEventHandler.class,
               TestEventSetDataAction.class,
               TestGeneralModeling.class,
               TestImplementsProcessInterface.class,
               TestLocalUIMashup.class,
               TestMakerChecker.class,
               TestModelCommands.class,
               TestModelCommandsReplay.class,
               TestQualityControl.class,
               TestResubmission.class,
               TestSignalEventEditing.class,
               TestStandardLoop.class,
               TestUndoOperation.class,
               TestUpgradeModel.class,  
               TestJsonPathEvaluation.class,
               TestModelEditLocking.class,
               TestGsonMarshaling.class,
               TestGsonUnmarshalling.class,
               TestModellingSessionRestController.class,
               TestChecklistEditing.class,
               TestModelValidation.class,
               TestXpdlValidation.class,
               TestExtensionsDiscovery.class,
               TestIdFromNameGeneration.class,
               TestCompositeDescriptors.class,
               TestMandatoryDataMappings.class,
               TestAnnotations.class,
               TestUUIDMapping.class,
               TestMultipleParticipants.class
             })
public class WebModelerTestSuite
{
}
