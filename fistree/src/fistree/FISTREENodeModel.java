package fistree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
 * This is the model implementation of FisTree. FisTree's FisPro Learner
 *
 * @author Antonio Di Mauro
 */
public class FISTREENodeModel extends NodeModel {

	private final boolean DEBUG = false;

	private static final NodeLogger LOGGER = NodeLogger.getLogger(FISTREENodeModel.class);

	/**
	 * Keys used by {@link fistree.FISTREENodeDialog} to store settings
	 */
	public static final String OUT_NUM_STR = "out_num_str";
	public static final String MEMB_THRES_STR = "memb_thres_str";
	public static final String MIN_LEAF_STR = "min_leaf_str";
	public static final String TOL_STR = "tol_str";
	public static final String MAX_DEPTH_STR = "max_depth_str";
	public static final String MIN_GAIN_STR = "min_gain_str";
	public static final String GAIN_TYPE_STR = "gain_type_str";
	public static final String PRUNING_TYPE_STR = "pruning_type_str";
	public static final String PERF_LOSS_STR = "perf_loss_str";
	public static final String COV_LEVEL_STR = "cov_level_str";

	/**
	 * Settings information by key
	 */
	private final SettingsModelInteger OUT_NUM = new SettingsModelInteger(FISTREENodeModel.OUT_NUM_STR, 0);
	private final SettingsModelDouble MEMB_THRES = new SettingsModelDouble(FISTREENodeModel.MEMB_THRES_STR, 0.2D);
	private final SettingsModelInteger MIN_LEAF = new SettingsModelInteger(FISTREENodeModel.MIN_LEAF_STR, 10);
	private final SettingsModelDouble TOL = new SettingsModelDouble(FISTREENodeModel.TOL_STR, 0.1D);
	private final SettingsModelInteger MAX_DEPTH = new SettingsModelInteger(FISTREENodeModel.MAX_DEPTH_STR, 0);
	private final SettingsModelInteger MIN_GAIN = new SettingsModelInteger(FISTREENodeModel.MIN_GAIN_STR, 1);
	private final SettingsModelString GAIN_TYPE = new SettingsModelString(FISTREENodeModel.GAIN_TYPE_STR, "0");
	private final SettingsModelString PRUNING_TYPE = new SettingsModelString(FISTREENodeModel.PRUNING_TYPE_STR, "0");
	private final SettingsModelDouble PERF_LOSS = new SettingsModelDouble(FISTREENodeModel.PERF_LOSS_STR, 0.0D);
	private final SettingsModelDouble COV_LEVEL = new SettingsModelDouble(FISTREENodeModel.COV_LEVEL_STR, 0.90D);

	private final String TABLE_SEP = ",";
	private final String CFG_FIS = "cfg.fis";
	private final String FISTREE_FIS = CFG_FIS + ".tree.fis";
	private final String DATA_NAME = "dataset.csv";
	private final String FIS_BIN_PATH = "FIS_BIN_PATH";

	private FisModel mModel;
	private double timeStart;
	private double timeEnd;
	private double timeCommandStart;
	private double timeCommandEnd;
	private String fistreeStdout;

	/**
	 * Constructor for the node model.
	 */
	protected FISTREENodeModel() {
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

		int outNum = OUT_NUM.getIntValue();
		double membThres = MEMB_THRES.getDoubleValue();
		int minLeaf = MIN_LEAF.getIntValue();
		double tol = TOL.getDoubleValue();
		int maxDepth = MAX_DEPTH.getIntValue();
		int minGain = MIN_GAIN.getIntValue();
		String gainType = GAIN_TYPE.getStringValue();
		String pruningType = PRUNING_TYPE.getStringValue();
		double perfLoss = PERF_LOSS.getDoubleValue();
		double covLevel = COV_LEVEL.getDoubleValue();

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
					if (loadDataset(table, homeDir, DATA_NAME)) {
						if (fistreeFunction(fisDir, homeDir, DATA_NAME, outNum, membThres, minLeaf, tol, maxDepth,
								minGain, gainType, pruningType, perfLoss, covLevel)) {
							File fis = new File(homeDir, FISTREE_FIS);
							// creation of the modelPortObject from the fis
							// file
							mModel = new FisModel(fis, "FisTree");
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
						+ " ms\n\nStandard output:\n" + fistreeStdout;
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
			LOGGER.error("Statistics not saved!");
		}
	}

	/**
	 * Execute fistree function
	 * 
	 * e.g. fistree file.fis dataset -o0 -s0.2 -x10 -t0.1 -d0 -g1e-6 -e0 -p0
	 * -l0.0 -c0.90
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
	private boolean fistreeFunction(String fisDir, String homeDir, String dataName, int outNum, double membThres,
			int minLeaf, double tol, int maxDepth, int minGain, String gainType, String pruningType, double perfLoss,
			double covLevel) {

		File home = new File(homeDir);
		File fistree = new File(fisDir, "fistree");
		File data = new File(homeDir, dataName);
		File cfgFis = new File(homeDir, CFG_FIS);
		File fistreeFis = new File(homeDir, FISTREE_FIS);
		String args = "-o" + outNum + " -s" + membThres + " -x" + minLeaf + " -t" + tol + " -d" + maxDepth + " -g"
				+ minGain + "e-6 -e" + gainType + " -p" + pruningType + " -l" + perfLoss + " -c" + covLevel;
		if (DEBUG)
			LOGGER.error("args: " + args);
		try {
			String[] cmd = { fistree.getPath(), cfgFis.getPath(), data.getPath(), "-o" + outNum, "-s" + membThres,
					"-x" + minLeaf, "-t" + tol, "-d" + maxDepth, "-g" + minGain + "e-6", "-e" + gainType,
					"-p" + pruningType, "-l" + perfLoss, "-c" + covLevel };
			timeCommandStart = System.currentTimeMillis();
			Process p = Runtime.getRuntime().exec(cmd, null, home);
			String line = "";
			String in = "";
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((in = input.readLine()) != null) {
				line += in;
			}
			input.close();
			fistreeStdout = line;
			int control = p.waitFor();
			timeCommandEnd = System.currentTimeMillis();
			if (control == 0) {
				if (!fistreeFis.exists())
					LOGGER.error("Model " + FISTREE_FIS + " NOT created.");
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Model " + FISTREE_FIS + " NOT created.");
			return false;
		}
		return fistreeFis.exists();
	}

	/**
	 * Return the string path of a random directory
	 * 
	 * @return random path
	 */
	private String getRandomDirectory() {

		File dir = new File(System.getProperty("user.home"), "FISTREE");
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
		OUT_NUM.saveSettingsTo(settings);
		MEMB_THRES.saveSettingsTo(settings);
		MIN_LEAF.saveSettingsTo(settings);
		TOL.saveSettingsTo(settings);
		MAX_DEPTH.saveSettingsTo(settings);
		MIN_GAIN.saveSettingsTo(settings);
		GAIN_TYPE.saveSettingsTo(settings);
		PRUNING_TYPE.saveSettingsTo(settings);
		PERF_LOSS.saveSettingsTo(settings);
		COV_LEVEL.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		OUT_NUM.loadSettingsFrom(settings);
		MEMB_THRES.loadSettingsFrom(settings);
		MIN_LEAF.loadSettingsFrom(settings);
		TOL.loadSettingsFrom(settings);
		MAX_DEPTH.loadSettingsFrom(settings);
		MIN_GAIN.loadSettingsFrom(settings);
		GAIN_TYPE.loadSettingsFrom(settings);
		PRUNING_TYPE.loadSettingsFrom(settings);
		PERF_LOSS.loadSettingsFrom(settings);
		COV_LEVEL.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		OUT_NUM.validateSettings(settings);
		MEMB_THRES.validateSettings(settings);
		MIN_LEAF.validateSettings(settings);
		TOL.validateSettings(settings);
		MAX_DEPTH.validateSettings(settings);
		MIN_GAIN.validateSettings(settings);
		GAIN_TYPE.validateSettings(settings);
		PRUNING_TYPE.validateSettings(settings);
		PERF_LOSS.validateSettings(settings);
		COV_LEVEL.validateSettings(settings);
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
