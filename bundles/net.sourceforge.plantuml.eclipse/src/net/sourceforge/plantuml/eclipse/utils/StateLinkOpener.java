package net.sourceforge.plantuml.eclipse.utils;


import java.net.URL;



public class StateLinkOpener implements ILinkOpener {
	
	@Override
	public int supportsLink(LinkData link) {
		System.out.println("trying state link opener");
		String href = link.href;
		if (href.contains("FSM")) {
			return STATE_SUPPORT;
		}
		return NO_SUPPORT;
	}

	//private boolean preferInternal = false;
	
	@Override
	public void openLink(LinkData link) {
//		System.out.println("In statelink opner :)");
//		try {
//			URL url = new URL(link.href);
//			IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
//			IWebBrowser browser = (preferInternal ? browserSupport.createBrowser(IWorkbenchBrowserSupport.AS_VIEW, "plantuml", "PlantUML Browser", null) : browserSupport.getExternalBrowser());
//			browser.openURL(url);
//		} catch (Exception e) {
//		}
	}
}
