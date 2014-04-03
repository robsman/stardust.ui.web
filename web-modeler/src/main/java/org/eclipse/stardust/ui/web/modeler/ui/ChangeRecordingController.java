package org.eclipse.stardust.ui.web.modeler.ui;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.modeler.edit.recording.ModelChangeRecorder;

@Component
@Scope("session")
public class ChangeRecordingController
{
   @Resource
   private ModelChangeRecorder modelChangeRecorder;
}
