package fpa;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import fpa.FPANodeModel;

/**
 * <code>NodeDialog</code> for the "FPA" Node. Fpa's FisPro Function Learner
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Antonio Di Mauro
 */
public class FPANodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring FPA node dialog. This is just a suggestion to
	 * demonstrate possible default dialog components.
	 */
	protected FPANodeDialog() {
		super();

		createNewGroup("Insert parameters");

		addDialogComponent(new DialogComponentStringSelection(new SettingsModelString(FPANodeModel.STRATEGY_STR, "1"),
				"Strategy to determine the data subset used to initialize the rule conclusion, 0 for MIN, 1 for DEC (default value):",
				new String[] { "0", "1" }));

		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(FPANodeModel.MIN_DEGREE_STR, 0.3D),
				"Minimum matching degree for rule generation (default value: 0.3):", Double.valueOf(0.1D)));

		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(FPANodeModel.MIN_CARD_STR, 3),
				"Minimum cardinality (default value: 3):", Integer.valueOf(1)));

		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(FPANodeModel.THRESHOLD_STR, 0.1D),
				"Activity threshold for performance computation (default value: 0.1):", Double.valueOf(0.1D)));
	}
}
