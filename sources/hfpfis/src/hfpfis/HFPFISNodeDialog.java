package hfpfis;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import hfpfis.HFPFISNodeModel;

/**
 * <code>NodeDialog</code> for the "HFPFIS" Node. HFPFIS' FisPro Learner
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Antonio Di Mauro
 */
public class HFPFISNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring HFPFIS node dialog. This is just a suggestion to
	 * demonstrate possible default dialog components.
	 */
	protected HFPFISNodeDialog() {
		super();

		createNewGroup("Insert parameters");

		addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(HFPFISNodeModel.SET_WM_STR, false),
				"Choose wm as rule induction method (default: fpa is used)"));

		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(HFPFISNodeModel.MIN_WEIGHT_STR, 0.0D),
				"Minimum cumulated weight for a rule to be generated (0.0 default value):", Double.valueOf(0.1)));

		addDialogComponent(
				new DialogComponentStringSelection(new SettingsModelString(HFPFISNodeModel.CARD_STRATEGY_STR, "1"),
						"Strategy to determine the data subset used to initialize the rule conclusion, 0 for MIN, 1 for DEC (default value):",
						new String[] { "0", "1" }));

		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(HFPFISNodeModel.MIN_DEGREE_STR, 0.3D),
				"Minimum matching degree (default value: 0.3):", Double.valueOf(0.1)));

		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(HFPFISNodeModel.MIN_CARD_STR, 3),
				"Minimum cardinality (default value: 3):", Integer.valueOf(1)));

		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(HFPFISNodeModel.OUT_NUM_STR, 0),
				"Output number (default value: 0, first output):", Integer.valueOf(1)));
	}
}
