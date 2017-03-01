package fisproreader;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "FisProReader" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Antonio Di Mauro
 */
public class FisProReaderNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring FisProReader node dialog. This is just a
	 * suggestion to demonstrate possible default dialog components.
	 */
	protected FisProReaderNodeDialog() {
		super();

		createNewGroup("Choose fis file:");
		addDialogComponent(new DialogComponentFileChooser(
				new SettingsModelString(FisProReaderNodeModel.FIS_DIR_STR, null), "Choose fis file:"));

	}
}
