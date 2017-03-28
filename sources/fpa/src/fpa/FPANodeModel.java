package fpa;

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
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of FPA. FPA's FisPro Learner
 *
 * @author Antonio Di Mauro
 */
public class FPANodeModel extends NodeModel {

	private final boolean DEBUG = false;

	private static final NodeLogger LOGGER = NodeLogger.getLogger(FPANodeModel.class);

	/**
	 * Keys used by {@link fpa.FPARNodeDialog} to store settings
	 */
	public static final String STRATEGY_STR = "strategy_str";
	public static final String MIN_DEGREE_STR = "min_degree_str";
	public static final String MIN_CARD_STR = "min_card_str";
	public static final String THRESHOLD_STR = "threshold_str";

	/**
	 * Settings information by key
	 */
	private final SettingsModelString STRATEGY = new SettingsModelString(FPANodeModel.STRATEGY_STR, "1");
	private final SettingsModelDouble MIN_DEGREE = new SettingsModelDouble(FPANodeModel.MIN_DEGREE_STR, 0.3D);
	private final SettingsModelInteger MIN_CARD = new SettingsModelInteger(FPANodeModel.MIN_CARD_STR, 3);
	private final SettingsModelDouble THRESHOLD = new SettingsModelDouble(FPANodeModel.THRESHOLD_STR, 0.1D);

	private final String TABLE_SEP = ",";
	private final String CFG_FIS = "cfg.fis";
	private final String GEN_CFG_FIS = "genconfig.fis";
	private final String FPA_FIS = "configfpa.fis";
	private final String FIS_BIN_PATH = "FIS_BIN_PATH";

	private FisModel mModel;
	private double timeStart;
	private double timeEnd;
	private double timeCommandGen;
	private double timeCommandFpa;

	/**
	 * Constructor for the node model.
	 */
	protected FPANodeModel() {
		// (number-input-ports, number-output-ports)
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

		String strategy = STRATEGY.getStringValue();
		double minDegree = MIN_DEGREE.getDoubleValue();
		int minCard = MIN_CARD.getIntValue();
		double threshold = THRESHOLD.getDoubleValue();

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
						if (genrulesFunction(fisDir, homeDir)) {
							if (fpaFunction(fisDir, homeDir, dataName, strategy, minDegree, minCard, threshold)) {
								File fis = new File(homeDir, FPA_FIS);
								// creation of the modelPortObject from the fis
								// file
								mModel = new FisModel(fis, "FPA");
								if (DEBUG)
									LOGGER.error("Model Summary: " + mModel.getSum());
								out = new FisModelPortObject(mModel, new FisModelPortObjectSpec());
								timeEnd = System.currentTimeMillis();
							}
						}
					}
				}

				double time = (timeEnd - timeStart);
				double timeCommand = timeCommandGen + timeCommandFpa;
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
	 * Execute fpa function
	 * 
	 * e.g. fpa file.fis file.csv -s0 -d0.3 -e3 -u0
	 * 
	 * @param fisDir
	 *            fispro bin path
	 * @param homeDir
	 *            workspace home path
	 * @param dataName
	 *            csv file name
	 * @param strategy
	 *            strategy to determine the data subset used to initialize the
	 *            rule conclusion
	 * @param minDegree
	 *            minimum matching degree for rule generation
	 * @param minCard
	 *            minimum cardinality
	 * @param threshold
	 *            activity threshold for performance computation
	 * 
	 * @return true if function is successful, else false
	 */
	private boolean fpaFunction(String fisDir, String homeDir, String dataName, String strategy, double minDegree,
			int minCard, double threshold) {

		File home = new File(homeDir);
		File fpa = new File(fisDir, "fpa");
		File data = new File(homeDir, dataName);
		File cfgFis = new File(homeDir, GEN_CFG_FIS);
		File fpaFis = new File(homeDir, FPA_FIS);
		String args = "-s" + strategy + " -d" + Double.toString(minDegree) + " -e" + Integer.toString(minCard) + " -u"
				+ Double.toString(threshold);
		if (DEBUG)
			LOGGER.error("args: " + args);
		try {
			String[] cmd = { fpa.getPath(), cfgFis.getPath(), data.getPath(), "-s" + strategy, "-d" + minDegree,
					"-e" + minCard, "-u" + threshold };
			double timeCommandStart = System.currentTimeMillis();
			Process p = Runtime.getRuntime().exec(cmd, null, home);
			int control = p.waitFor();
			double timeCommandEnd = System.currentTimeMillis();
			timeCommandFpa = timeCommandEnd - timeCommandStart;
			if (control == 0) {
				if (!fpaFis.exists())
					LOGGER.error("Model " + FPA_FIS + " NOT created.");
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Model " + FPA_FIS + " NOT created.");
			return false;
		}
		return fpaFis.exists();
	}

	/**
	 * Execute genrules function
	 * 
	 * @param fisDir
	 *            FisPro bin path
	 * @param homeDir
	 *            working directory
	 * 
	 * @return true if function is successful, else false
	 */
	private boolean genrulesFunction(String fisDir, String homeDir) {

		File home = new File(homeDir);
		File gen = new File(fisDir, "genrules");
		File cfgFis = new File(homeDir, CFG_FIS);
		File genFis = new File(homeDir, GEN_CFG_FIS);
		try {
			String[] cmd = { gen.getPath(), cfgFis.getPath(), "-f" + GEN_CFG_FIS };
			double timeCommandStart = System.currentTimeMillis();
			Process p = Runtime.getRuntime().exec(cmd, null, home);
			int control = p.waitFor();
			double timeCommandEnd = System.currentTimeMillis();
			timeCommandGen = timeCommandEnd - timeCommandStart;
			if (control == 0) {
				if (!genFis.exists())
					LOGGER.error("Model " + FPA_FIS + " NOT created.");
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Model " + FPA_FIS + " NOT created.");
			return false;
		}
		return genFis.exists();
	}

	/**
	 * Return the string path of a random directory
	 * 
	 * @return random path
	 */
	private String getRandomDirectory() {

		File dir = new File(System.getProperty("user.home"), "FPA");
		int rand = (int) (Math.random() * 10000000);
		dir = new File(dir + "-" + rand);
		return dir.getPath();
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
		STRATEGY.saveSettingsTo(settings);
		MIN_DEGREE.saveSettingsTo(settings);
		MIN_CARD.saveSettingsTo(settings);
		THRESHOLD.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		STRATEGY.loadSettingsFrom(settings);
		MIN_DEGREE.loadSettingsFrom(settings);
		MIN_CARD.loadSettingsFrom(settings);
		THRESHOLD.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		STRATEGY.validateSettings(settings);
		MIN_DEGREE.validateSettings(settings);
		MIN_CARD.validateSettings(settings);
		THRESHOLD.validateSettings(settings);
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
