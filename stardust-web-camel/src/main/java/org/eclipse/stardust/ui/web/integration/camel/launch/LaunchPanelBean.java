package org.eclipse.stardust.ui.web.integration.camel.launch;

public class LaunchPanelBean {

	private boolean expanded = true;

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void toggle() {
		expanded = !expanded;
	}

}
