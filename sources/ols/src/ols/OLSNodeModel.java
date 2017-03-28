package ols;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
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
 * This is the model implementation of OLS. Orthogonal Least Squares' FisPro
 * Learner
 *
 * @author Antonio Di Mauro
 */
public class OLSNodeModel extends NodeModel {

	private final boolean DEBUG = false;

	// the logger instance
	private static final NodeLogger LOGGER = NodeLogger.getLogger(OLSNodeModel.class);

	private final String TABLE_SEP = ",";
	private final String OLS_FIS = "ols.fis";
	private final String CFG_FIS = "cfg.fis";
	private final String FIS_BIN_PATH = "FIS_BIN_PATH";

	private FisModel mModel;
	private double timeStart;
	private double timeEnd;
	private double timeCommandStart;
	private double timeCommandEnd;

	/**
	 * Constructor for the node model.
	 */
	protected OLSNodeModel() {
		super(new PortType[] { FisModelPortObject.TYPE, BufferedDataTable.TYPE },
				new PortType[] { FisModelPortObject.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		FisModelPortObject cfg = (FisModelPortObject) inData[0];
		BufferedDataTable table = (BufferedDataTable) inData[1];
		FisModelPortObject out = null;
		String dataName = "dataset.csv";

		// generate random directory
		File baseDir = new File(getRandomDirectory());
		while (baseDir.exists())
			baseDir = new File(getRandomDirectory());

		// get the flow variables
		getAvailableFlowVariables();
		try {
			String fisDir = peekFlowVariableString(FIS_BIN_PATH);

			// if the directory is created start the creation of fis
			if (baseDir.mkdirs()) {
				String homeDir = baseDir.getPath();
				if (DEBUG)
					LOGGER.error("Base Directory is created! (" + homeDir + ")");

				File cfgFisDir = new File(homeDir, CFG_FIS);
				File cfgFis = cfg.getFisModel().getFis(cfgFisDir.getPath());

				if (cfgFis.exists()) {
					/*
					 * sequence of functions to create Fuzzy Inference System
					 * Model file .fis from a dataset
					 */
					timeStart = System.currentTimeMillis();
					if (loadDataset(table, homeDir, dataName)) {
						if (olsFunction(fisDir, homeDir, dataName)) {
							File fis = new File(homeDir, OLS_FIS);
							// creation of the modelPortObject from the fis
							// file
							mModel = new FisModel(fis, "Orthogonal Least Squares");
							if (DEBUG)
								LOGGER.error("Model Summary: " + mModel.getSum());
							out = new FisModelPortObject(mModel, new FisModelPortObjectSpec());
							timeEnd = System.currentTimeMillis();
						}
					}
				}

				double time = (timeEnd - timeStart);
				double timeCommand = (timeCommandEnd - timeCommandStart);
				String stat = "Integration execution time: " + time + " ms\nCommand line execution time: " + timeCommand
						+ " ms";
				saveStats(homeDir, stat);
				if (DEBUG)
					LOGGER.error(stat);

				// delete the base directory
				if (!DEBUG)
					FileUtils.deleteDirectory(new File(baseDir.getPath()));
				if (!baseDir.exists()) {
					if (DEBUG)
						LOGGER.error("Base Directory deleted! (" + baseDir.getPath() + ")");
				} else
					LOGGER.error("Base Directory NOT deleted! (" + baseDir.getPath() + ")");
			}
			// catch exception if the flow variable doesn't exist
		} catch (Exception e) {
			String message = "Set Flow Variables:\n-Right Click on 'Workflow Project';\n-Click on 'Workflow Variables';"
					+ "\n-Press 'Add';\n-Set 'Variable name' as 'FIS_BIN_PATH';"
					+ "\n-Set 'Variable type' as 'STRING';\n-Put in 'Default value' the FisPro bin Path;"
					+ "\n-Press OK.\nOr sets all configuration options in Configure panel!";
			LOGGER.error(message);
			JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
			FileUtils.deleteDirectory(new File(baseDir.getPath()));
		}
		return new PortObject[] { out };
	}

	/**
	 * Save statistics to a file
	 * 
	 * @param homeDir
	 *            home directory
	 * @param stat
	 *            statistics string to save
	 */
	private void saveStats(String homeDir, String stat) {
		File home = new File(homeDir);
		File file = new File(home, "stats");
		try {
			FileOutputStream data = new FileOutputStream(file);
			PrintStream write = new PrintStream(data);
			write.println(stat);
			write.close();
		} catch (FileNotFoundException e) {
			if (DEBUG)
				LOGGER.error("Statistics not saved!");
		}
	}

	/**
	 * Execute ols function
	 * 
	 * e.g. ols dataset -fconfig.fis -cout.fis
	 * 
	 * @param fisDir
	 *            fispro bin path
	 * @param homeDir
	 *            workspace home path
	 * @param dataName
	 *            csv file name
	 * 
	 * @return true if function is successful, else false
	 */
	private boolean olsFunction(String fisDir, String homeDir, String dataName) {

		File home = new File(homeDir);
		File ols = new File(fisDir, "ols");
		File data = new File(homeDir, dataName);
		File olsFis = new File(homeDir, OLS_FIS);
		try {
			String[] cmd = { ols.getPath(), data.getPath(), "-f" + CFG_FIS, "-c" + OLS_FIS };
			timeCommandStart = System.currentTimeMillis();
			Process p = Runtime.getRuntime().exec(cmd, null, home);
			int control = p.waitFor();
			timeCommandEnd = System.currentTimeMillis();
			if (control == 0) {
				if (!olsFis.exists())
					LOGGER.error("Model " + OLS_FIS + " NOT created.");
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Model " + OLS_FIS + " NOT created.");
			return false;
		}
		return olsFis.exists();
	}

	/**
	 * Create csv file from BufferedDataTable
	 * 
	 * @param t
	 *            data table
	 * @param homeDir
	 *            directory to save the file
	 * @param dataName
	 *            csv file name
	 * 
	 * @return true if creation is successful, else false
	 */
	private boolean loadDataset(BufferedDataTable t, String homeDir, String dataName) {

		File file = new File(homeDir, dataName);
		try {
			FileOutputStream data = new FileOutputStream(file);
			PrintStream write = new PrintStream(data);

			// write data cell's rows in csv file
			CloseableRowIterator it = t.iterator();
			while (it.hasNext()) {
				String rowStr = "";
				DataRow row = it.next();
				for (int i = 0; i < row.getNumCells(); i++) {
					if (i == 0)
						rowStr += row.getCell(i).toString();
					else
						rowStr += TABLE_SEP + row.getCell(i).toString();
				}
				write.println(rowStr);
			}
			write.close();

			if (!file.exists())
				LOGGER.error("Dataset file NOT created.");
		} catch (FileNotFoundException e) {
			LOGGER.error("Dataset file NOT created.");
			return false;
		}
		return file.exists();
	}

	/**
	 * Return the string path of a random directory
	 * 
	 * @return random path
	 */
	private String getRandomDirectory() {

		File dir = new File(System.getProperty("user.home"), "OLS");
		int rand = (int) (Math.random() * 10000000);
		dir = new File(dir + "-" + rand);
		return dir.getPath();
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
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
