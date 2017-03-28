package hfpfis;

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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of HFPFIS. HFPFIS's FisPro Learner
 *
 * @author Antonio Di Mauro
 */
public class HFPFISNodeModel extends NodeModel {

	private final boolean DEBUG = false;

	private static final NodeLogger LOGGER = NodeLogger.getLogger(HFPFISNodeModel.class);

	/**
	 * Keys used by {@link hfpfis.HFPFISNodeDialog} to store settings
	 */
	public static final String SET_WM_STR = "set_wm_str";
	public static final String MIN_WEIGHT_STR = "min_weight_str";
	public static final String CARD_STRATEGY_STR = "card_strategy_str";
	public static final String MIN_DEGREE_STR = "min_degree_str";
	public static final String MIN_CARD_STR = "min_card_str";
	public static final String OUT_NUM_STR = "out_num_str";

	/**
	 * Settings information by key
	 */
	private final SettingsModelBoolean SET_WM = new SettingsModelBoolean(HFPFISNodeModel.SET_WM_STR, false);
	private final SettingsModelDouble MIN_WEIGHT = new SettingsModelDouble(HFPFISNodeModel.MIN_WEIGHT_STR, 0.0D);
	private final SettingsModelString CARD_STRATEGY = new SettingsModelString(HFPFISNodeModel.CARD_STRATEGY_STR, "1");
	private final SettingsModelDouble MIN_DEGREE = new SettingsModelDouble(HFPFISNodeModel.MIN_DEGREE_STR, 0.3D);
	private final SettingsModelInteger MIN_CARD = new SettingsModelInteger(HFPFISNodeModel.MIN_CARD_STR, 3);
	private final SettingsModelInteger OUT_NUM = new SettingsModelInteger(HFPFISNodeModel.OUT_NUM_STR, 0);

	private final String TABLE_SEP = ",";
	private final String HFPFIS_FIS = "hfpfis.fis";
	private final String DATA_NAME = "dataset.csv";
	private final String VERTEX_NAME = DATA_NAME + ".vertex";
	private final String HFP_NAME = DATA_NAME + ".hfp";
	private final String FIS_BIN_PATH = "FIS_BIN_PATH";

	private FisModel mModel;
	private double timeStart;
	private double timeEnd;
	private double timeCommandStart;
	private double timeCommandEnd;

	/**
	 * Constructor for the node model.
	 */
	protected HFPFISNodeModel() {
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

		boolean setWm = SET_WM.getBooleanValue();
		double minWeight = MIN_WEIGHT.getDoubleValue();
		String cardStrategy = CARD_STRATEGY.getStringValue();
		double minDegree = MIN_DEGREE.getDoubleValue();
		int minCard = MIN_CARD.getIntValue();
		int outNum = OUT_NUM.getIntValue();

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

				File cfgHfpDir = new File(homeDir, HFP_NAME);
				File cfgHfp = cfg.getFisModel().getHfp(cfgHfpDir.getPath());
				File vertexDir = new File(homeDir, VERTEX_NAME);
				File vertex = cfg.getFisModel().getVertex(vertexDir.getPath());

				if (cfgHfp.exists()) {
					/*
					 * sequence of functions to create Fuzzy Inference System
					 * Model file .fis from a dataset
					 */
					timeStart = System.currentTimeMillis();
					if (loadDataset(table, homeDir, DATA_NAME)) {
						if (hfpfisFunction(fisDir, homeDir, DATA_NAME, cfgHfp.getPath(), vertex.getPath(), setWm,
								minWeight, cardStrategy, minDegree, minCard, outNum)) {
							File fis = new File(homeDir, HFPFIS_FIS);
							// creation of the modelPortObject from the fis
							// file
							mModel = new FisModel(fis, "HFPFIS");
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
	 * Execute hfpfis function
	 * 
	 * e.g. hfpfis dataset configfile
	 * 
	 * @param fisDir
	 *            fispro bin path
	 * @param homeDir
	 *            workspace home path
	 * @param dataName
	 *            csv file name
	 * @param hfp
	 *            hfp config file path
	 * @param vert
	 *            vertex file path
	 * @param setWm
	 *            choose wm as rule induction method
	 * @param minWeight
	 *            minimum cumulated weight for a rule to be generated
	 * @param cardStrategy
	 *            strategy to determine the data subset used to initialize the
	 *            rule conclusion
	 * @param minDegree
	 *            minimum matching degree
	 * @param minCard
	 *            minimum cardinality
	 * @param outNum
	 *            output number
	 * 
	 * @return true if function is successful, else false
	 */
	private boolean hfpfisFunction(String fisDir, String homeDir, String dataName, String hfp, String vert,
			boolean setWm, double minWeight, String cardStrategy, double minDegree, int minCard, int outNum) {

		File home = new File(homeDir);
		File hfpfis = new File(fisDir, "hfpfis");
		File data = new File(homeDir, dataName);
		File cfgHfp = new File(hfp);
		File vertex = new File(vert);
		File hfpfisFis = new File(homeDir, HFPFIS_FIS);

		if (!setWm && outNum != 0) {
			LOGGER.error(
					"**Segmentation error with fpa induction method! Please insert 'Output number=0' in the Node Dialog!**");
			return false;
		}

		try {
			if (DEBUG)
				LOGGER.error("trying hfpfis command for Linux...");
			String[] cmd = { "/bin/sh", "-c",
					hfpfis.getPath() + " " + DATA_NAME + " " + HFP_NAME + (setWm ? " -r" : "") + " -s" + minWeight
							+ " -t" + cardStrategy + " -m" + minDegree + " -e" + minCard + " -o" + HFPFIS_FIS + " -p"
							+ outNum + " -l" + VERTEX_NAME };
			timeCommandStart = System.currentTimeMillis();
			Process p = Runtime.getRuntime().exec(cmd, null, home);
			int control = p.waitFor();
			timeCommandEnd = System.currentTimeMillis();
			if (control == 0) {
				if (!hfpfisFis.exists())
					LOGGER.error("Model " + HFPFIS_FIS + " NOT created.");
			}
		} catch (IOException | InterruptedException e) {
			if (DEBUG)
				LOGGER.error("trying hfpfis command for Win...");
			try {
				String[] cmd = { hfpfis.getPath(), data.getPath(), cfgHfp.getPath(), "-l" + vertex.getPath(),
						(setWm ? "-r" : ""), "-s" + minWeight, "-t" + cardStrategy, "-m" + minDegree, "-e" + minCard,
						"-o" + HFPFIS_FIS, "-p" + outNum };
				if (DEBUG) {
					String args = (setWm ? "-r " : "") + "-s" + minWeight + " -t" + cardStrategy + " -m" + minDegree
							+ " -e" + minCard + " -o" + HFPFIS_FIS + " -p" + outNum;
					LOGGER.error("args:" + args);
				}
				timeCommandStart = System.currentTimeMillis();
				Process p = Runtime.getRuntime().exec(cmd, null, home);
				int control = p.waitFor();
				timeCommandEnd = System.currentTimeMillis();
				if (control == 0) {
					if (!hfpfisFis.exists())
						LOGGER.error("Model " + HFPFIS_FIS + " NOT created.");
				}
			} catch (IOException | InterruptedException e1) {
				LOGGER.error("Model " + HFPFIS_FIS + " NOT created.");
				return false;
			}
		}
		return hfpfisFis.exists();
	}

	/**
	 * Return the string path of a random directory
	 * 
	 * @return random path
	 */
	private String getRandomDirectory() {

		File dir = new File(System.getProperty("user.home"), "HFPFIS");
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
		SET_WM.saveSettingsTo(settings);
		MIN_WEIGHT.saveSettingsTo(settings);
		CARD_STRATEGY.saveSettingsTo(settings);
		MIN_DEGREE.saveSettingsTo(settings);
		MIN_CARD.saveSettingsTo(settings);
		OUT_NUM.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		SET_WM.loadSettingsFrom(settings);
		MIN_WEIGHT.loadSettingsFrom(settings);
		CARD_STRATEGY.loadSettingsFrom(settings);
		MIN_DEGREE.loadSettingsFrom(settings);
		MIN_CARD.loadSettingsFrom(settings);
		OUT_NUM.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		SET_WM.validateSettings(settings);
		MIN_WEIGHT.validateSettings(settings);
		CARD_STRATEGY.validateSettings(settings);
		MIN_DEGREE.validateSettings(settings);
		MIN_CARD.validateSettings(settings);
		OUT_NUM.validateSettings(settings);
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