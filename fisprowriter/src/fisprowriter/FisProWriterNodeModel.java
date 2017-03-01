package fisprowriter;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import port.FisModelPortObject;
import port.FisModelPortObjectSpec;

/**
 * This is the model implementation of FisProWriter.
 * 
 *
 * @author Antonio Di Mauro
 */
public class FisProWriterNodeModel extends NodeModel {

	private final boolean DEBUG = false;

	// the logger instance
	private static final NodeLogger LOGGER = NodeLogger.getLogger(FisProWriterNodeModel.class);

	/**
	 * Keys used by {@link fisProWriter.FisProWriterNodeDialog} to store
	 * settings
	 */
	public static final String OUT_DIR_STR = "out_dir_str";
	public static final String OUT_FILE_NAME_STR = "out_file_name_str";

	/**
	 * Settings information by key
	 */
	private final SettingsModelString OUT_DIR = new SettingsModelString(FisProWriterNodeModel.OUT_DIR_STR,
			System.getProperty("user.home"));
	private final SettingsModelString OUT_FILE_NAME = new SettingsModelString(FisProWriterNodeModel.OUT_FILE_NAME_STR,
			"outmodel-" + (int) (Math.random() * 10000000) + ".fis");

	/**
	 * Constructor for the node model.
	 */
	protected FisProWriterNodeModel() {
		// (number-input-ports, number-output-ports)
		super(new PortType[] { FisModelPortObject.TYPE }, new PortType[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		FisModelPortObject in = (FisModelPortObject) inData[0];

		String outDir = OUT_DIR.getStringValue();
		String outFileName = OUT_FILE_NAME.getStringValue();

		File fisDir = new File(outDir, outFileName);
		File fis = in.getFisModel().getFis(fisDir.getPath());
		if (!fis.exists())
			LOGGER.error("Fis file NOT saved in " + fis.getPath());
		else if (DEBUG)
			LOGGER.error("Fis file saved in " + fis.getPath());

		return new PortObject[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { new FisModelPortObjectSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		OUT_DIR.saveSettingsTo(settings);
		OUT_FILE_NAME.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		OUT_DIR.loadSettingsFrom(settings);
		OUT_FILE_NAME.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		OUT_DIR.validateSettings(settings);
		OUT_FILE_NAME.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

}
