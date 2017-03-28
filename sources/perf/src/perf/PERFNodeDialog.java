package perf;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "PERF" Node. Perf Node to make inference on a
 * FisPro Model
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Antonio Di Mauro
 */
public class PERFNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring PERF node dialog. This is just a suggestion to
	 * demonstrate possible default dialog components.
	 */
	protected PERFNodeDialog() {
		super();

		// addDialogComponent(new DialogComponentNumber());

	}
}
