package wm;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "WM" Node. Wang & Mendel's Fispro Function
 * Node
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Antonio Di Mauro
 */
public class WMNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring WM node dialog. This is just a suggestion to
	 * demonstrate possible default dialog components.
	 */
	protected WMNodeDialog() {
		super();
	}

}
