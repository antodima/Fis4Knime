package ols;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "OLS" Node. Orthogonal Least Squares' FisPro
 * Learner
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Antonio Di Mauro
 */
public class OLSNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring OLS node dialog. This is just a suggestion to
	 * demonstrate possible default dialog components.
	 */
	protected OLSNodeDialog() {
		super();
	}
}
