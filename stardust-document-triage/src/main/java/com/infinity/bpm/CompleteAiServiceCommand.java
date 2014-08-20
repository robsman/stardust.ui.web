package com.infinity.bpm;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

public class CompleteAiServiceCommand implements ServiceCommand {

	private static final long serialVersionUID = 1L;

	private long activityInstanceOID;
	private Map<String, ?> accessPoints;

	public CompleteAiServiceCommand(long activityInstanceOID,
			Map<String, ?> accessPoints, String partitionId) {
		this.activityInstanceOID = activityInstanceOID;
		this.accessPoints = accessPoints;
	}

	public Serializable execute(ServiceFactory sf) {
		AdministrationService aService = new AdministrationServiceImpl();
		return aService.forceCompletion(activityInstanceOID, accessPoints);
	}
}
