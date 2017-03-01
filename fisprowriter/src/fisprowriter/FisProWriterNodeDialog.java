package fisprowriter;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "FisProWriter" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Antonio Di Mauro
 */
public class FisProWriterNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring FisProWriter node dialog. This is just a
	 * suggestion to demonstrate possible default dialog components.
	 */
	protected FisProWriterNodeDialog() {
		super();

		createNewGroup("Insert parameters");

		addDialogComponent(new DialogComponentString(
				new SettingsModelString(FisProWriterNodeModel.OUT_DIR_STR, System.getProperty("user.home")),
				"Insert output directory:"));

		addDialogComponent(new DialogComponentString(
				new SettingsModelString(FisProWriterNodeModel.OUT_FILE_NAME_STR, null), "Insert output filename:"));

	}
}
