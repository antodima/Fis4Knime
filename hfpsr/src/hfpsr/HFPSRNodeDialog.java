package hfpsr;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "HFPSR" Node. Hfpsr FisPro Function to make
 * configuration model for Learner
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Antonio Di Mauro
 */
public class HFPSRNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring HFPSR node dialog. This is just a suggestion to
	 * demonstrate possible default dialog components.
	 */
	protected HFPSRNodeDialog() {

		super();

		createNewGroup("Insert parameters");

		addDialogComponent(new DialogComponentString(
				new SettingsModelString(HFPSRNodeModel.FUZZY_SETS_STRING_INPUT_STR, "3 3 3 3"),
				"Number of fuzzy sets for each input variable (numbers separated by space):"));

		addDialogComponent(new DialogComponentStringSelection(
				new SettingsModelString(HFPSRNodeModel.HY_TYPE_INPUT_STR, "1"),
				"Input hierarchy type (1 for hfp, 2 for k-means, 3 for regular):", new String[] { "1", "2", "3" }));

		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(HFPSRNodeModel.TOLERANCE_INPUT_STR, 0.01D),
				"Tolerance value for input:", Double.valueOf(0.01)));

		addDialogComponent(
				new DialogComponentNumber(new SettingsModelInteger(HFPSRNodeModel.FUZZY_SETS_NUMBER_OUTPUT_STR, 3),
						"Number of fuzzy sets for each output variable (if nmf=0 output crisp)", Integer.valueOf(1)));

		addDialogComponent(new DialogComponentStringSelection(
				new SettingsModelString(HFPSRNodeModel.HY_TYPE_OUTPUT_STR, "3"),
				"Output hierarchy type (1 for hfp, 2 for k-means, 3 for regular):", new String[] { "1", "2", "3" }));

		addDialogComponent(
				new DialogComponentStringSelection(new SettingsModelString(HFPSRNodeModel.DEFUZ_OP_STR, "MeanMax"),
						"Defuzzification operator:", new String[] { "area", "MeanMax", "sugeno" }));

		addDialogComponent(
				new DialogComponentStringSelection(new SettingsModelString(HFPSRNodeModel.DISJ_OP_STR, "sum"),
						"Disjunction operator:", new String[] { "max", "sum" }));

		addDialogComponent(
				new DialogComponentNumber(new SettingsModelDouble(HFPSRNodeModel.TOLERANCE_OUTPUT_STR, 0.01D),
						"Tolerance value for output:", Double.valueOf(0.01)));

		addDialogComponent(
				new DialogComponentBoolean(new SettingsModelBoolean(HFPSRNodeModel.CLASSIF_OUTPUT_STR, false),
						"Sets the Classif='yes' output option (Valid only when #output mf=0)"));
	}

}
