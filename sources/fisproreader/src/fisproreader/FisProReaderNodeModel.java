package fisproreader;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import port.FisModel;
import port.FisModelPortObject;
import port.FisModelPortObjectSpec;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the model implementation of FisProReader.
 * 
 *
 * @author Antonio Di Mauro
 */
public class FisProReaderNodeModel extends NodeModel {

	private final boolean DEBUG = false;

	// the logger instance
	private static final NodeLogger LOGGER = NodeLogger.getLogger(FisProReaderNodeModel.class);

	/**
	 * Keys used by {@link fisProWriter.FisProWriterNodeDialog} to store
	 * settings
	 */
	public static final String FIS_DIR_STR = "fis_str";

	/**
	 * Settings information by key
	 */
	private final SettingsModelString FIS_DIR = new SettingsModelString(FisProReaderNodeModel.FIS_DIR_STR, null);

	private FisModel mModel;

	/**
	 * Constructor for the node model.
	 */
	protected FisProReaderNodeModel() {
		// (number-input-ports, number-output-ports)
		super(new PortType[0], new PortType[] { FisModelPortObject.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		FisModelPortObject out = null;

		String fisDir = FIS_DIR.getStringValue();
		File fis = new File(fisDir);

		if (fis.exists()) {
			mModel = new FisModel(fis, "Unknown");
			out = new FisModelPortObject(mModel, new FisModelPortObjectSpec());
			if (DEBUG)
				LOGGER.error("Model Summary: " + mModel.getSum());
		} else {
			LOGGER.error("Fis file NOT found in " + fis.getPath());
		}

		return new PortObject[] { out };
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
		FIS_DIR.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		FIS_DIR.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		FIS_DIR.validateSettings(settings);
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
