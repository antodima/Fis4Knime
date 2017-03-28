package fistree;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "FISTREE" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Antonio Di Mauro
 */
public class FISTREENodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring FISTREE node dialog. This is just a suggestion
	 * to demonstrate possible default dialog components.
	 */
	protected FISTREENodeDialog() {
		super();

		createNewGroup("Insert parameters");

		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(FISTREENodeModel.OUT_NUM_STR, 0),
				"Output number (default=0, first output):", Integer.valueOf(1)));

		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(FISTREENodeModel.MEMB_THRES_STR, 0.2D),
				"Membership significance threshold for data items (default=0.2):", Double.valueOf(0.1)));

		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(FISTREENodeModel.MIN_LEAF_STR, 10),
				"Minimum leaf cardinality (default=min(10,#rows/10)):", Integer.valueOf(1)));

		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(FISTREENodeModel.TOL_STR, 0.1D),
				"Tolerance in case of ambiguous classification at node (default=0.1):", Double.valueOf(0.1)));

		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(FISTREENodeModel.MAX_DEPTH_STR, 0),
				"Maximum tree depth (default=0, maximum depth):", Integer.valueOf(1)));

		addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(FISTREENodeModel.MIN_GAIN_STR, 1),
				"Minimum relative entropy/deviance gain for splitting nodes (default=1e-6):", Integer.valueOf(1)));

		addDialogComponent(
				new DialogComponentStringSelection(new SettingsModelString(FISTREENodeModel.GAIN_TYPE_STR, "0"),
						"Entropy gain type (0=absolute entropy gain, 1=relative entropy gain (default=absolute)):",
						new String[] { "0", "1" }));

		addDialogComponent(
				new DialogComponentStringSelection(new SettingsModelString(FISTREENodeModel.PRUNING_TYPE_STR, "0"),
						"Tree pruning type (0=no pruning, 1=pruning using performance criterion-full split removal, 2=pruning using performance criterion-leaf pruning (default=no pruning)):",
						new String[] { "0", "1", "2" }));

		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(FISTREENodeModel.PERF_LOSS_STR, 0.0D),
				"Relative performance loss tolerated during pruning (default=0.0):", Double.valueOf(0.1)));

		addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(FISTREENodeModel.COV_LEVEL_STR, 0.90D),
				"Minimum coverage level required during pruning (default=0.90):", Double.valueOf(0.01)));
	}
}
