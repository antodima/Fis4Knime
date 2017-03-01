package hfpsr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
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
 * This is the model implementation of HFPSR. HFPSR's FisPro Function to make
 * configuration for Learner
 *
 * @author Antonio Di Mauro
 */
public class HFPSRNodeModel extends NodeModel {

	private final boolean DEBUG = false;

	private static final NodeLogger LOGGER = NodeLogger.getLogger(HFPSRNodeModel.class);

	/**
	 * Keys used by {@link hfpsr.HFPSRNodeDialog} to store settings
	 */
	public static final String FUZZY_SETS_STRING_INPUT_STR = "fuzzy_sets_string_input_str";
	public static final String HY_TYPE_INPUT_STR = "hy_type_input_str";
	public static final String TOLERANCE_INPUT_STR = "tolerance_input_str";
	public static final String FUZZY_SETS_NUMBER_OUTPUT_STR = "fuzzy_sets_number_output_str";
	public static final String HY_TYPE_OUTPUT_STR = "hy_type_output_str";
	public static final String DEFUZ_OP_STR = "defuz_op_str";
	public static final String DISJ_OP_STR = "disj_op_str";
	public static final String TOLERANCE_OUTPUT_STR = "tolerance_output_str";
	public static final String CLASSIF_OUTPUT_STR = "classif_output_str";

	/**
	 * Settings information by key
	 */
	private final SettingsModelString FUZZY_SETS_STRING_INPUT = new SettingsModelString(
			HFPSRNodeModel.FUZZY_SETS_STRING_INPUT_STR, "3 3 3 3");
	private final SettingsModelString HY_TYPE_INPUT = new SettingsModelString(HFPSRNodeModel.HY_TYPE_INPUT_STR, "1");
	private final SettingsModelDouble TOLERANCE_INPUT = new SettingsModelDouble(HFPSRNodeModel.TOLERANCE_INPUT_STR,
			0.01D);
	private final SettingsModelInteger FUZZY_SETS_NUMBER_OUTPUT = new SettingsModelInteger(
			HFPSRNodeModel.FUZZY_SETS_NUMBER_OUTPUT_STR, 3);
	private final SettingsModelString HY_TYPE_OUTPUT = new SettingsModelString(HFPSRNodeModel.HY_TYPE_OUTPUT_STR, "3");
	private final SettingsModelString DEFUZ_OP = new SettingsModelString(HFPSRNodeModel.DEFUZ_OP_STR, "MeanMax");
	private final SettingsModelString DISJ_OP = new SettingsModelString(HFPSRNodeModel.DISJ_OP_STR, "sum");
	private final SettingsModelDouble TOLERANCE_OUTPUT = new SettingsModelDouble(HFPSRNodeModel.TOLERANCE_OUTPUT_STR,
			0.01D);
	private final SettingsModelBoolean CLASSIF_OUTPUT = new SettingsModelBoolean(HFPSRNodeModel.CLASSIF_OUTPUT_STR,
			false);

	private final String TABLE_SEP = ",";
	private final String HFPSR_FIS = "hfp-sr.fis";
	private final String FIS_BIN_PATH = "FIS_BIN_PATH";
	private final String CLASSIF_YES = "Classif='yes'";
	private final String CLASSIF_NO = "Classif='no'";

	private FisModel mModel;
	private double timeStart;
	private double timeEnd;
	private double timeCommandStart;
	private double timeCommandEnd;

	/**
	 * Constructor for the node model.
	 */
	protected HFPSRNodeModel() {
		// (number-input-ports, number-output-ports)
		super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] { FisModelPortObject.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		BufferedDataTable table = (BufferedDataTable) inData[0];
		FisModelPortObject out = null;

		String DATA_NAME = table.getSpec().getName();
		String VERTEX_NAME = DATA_NAME + ".vertex";
		String HFP_NAME = DATA_NAME + ".hfp";

		String inFuzzySets = FUZZY_SETS_STRING_INPUT.getStringValue();
		String inHierarchyType = HY_TYPE_INPUT.getStringValue();
		double inToleranceValue = TOLERANCE_INPUT.getDoubleValue();
		int outFuzzySets = FUZZY_SETS_NUMBER_OUTPUT.getIntValue();
		String outHierarchyType = HY_TYPE_OUTPUT.getStringValue();
		String defuzOp = DEFUZ_OP.getStringValue();
		String disjOp = DISJ_OP.getStringValue();
		double outToleranceValue = TOLERANCE_OUTPUT.getDoubleValue();
		boolean classifOutput = CLASSIF_OUTPUT.getBooleanValue();

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

				int nInput = table.getDataTableSpec().getNumColumns();

				/*
				 * sequence of functions to create Fuzzy Inference System Model
				 * file .fis from a dataset
				 */
				timeStart = System.currentTimeMillis();
				if (loadDataset(table, homeDir, DATA_NAME)) {
					if (hfpSrFunction(fisDir, homeDir, DATA_NAME, nInput, inFuzzySets, inHierarchyType,
							inToleranceValue, outFuzzySets, outHierarchyType, defuzOp, disjOp, outToleranceValue,
							classifOutput)) {
						File fis = new File(homeDir, HFPSR_FIS);
						File hfp = new File(homeDir, HFP_NAME);
						File vertex = new File(homeDir, VERTEX_NAME);

						// set classif=yes in hfp file
						if (classifOutput) {
							if (!setClassifYes(hfp)) {
								LOGGER.error("Unable to set classif option!");
								return null;
							}
						}

						// creation of the modelPortObject from the fis
						// file
						mModel = new FisModel(fis, "HFPSR");
						mModel.setHfp(hfp);
						mModel.setVertex(vertex);
						if (DEBUG)
							LOGGER.error("Model Summary: " + mModel.getSum());
						out = new FisModelPortObject(mModel, new FisModelPortObjectSpec());
						timeEnd = System.currentTimeMillis();
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
	 * Change classif option of hfp file to yes
	 * 
	 * @param hfp
	 *            hfp file
	 * 
	 * @return true if function is successful, else false
	 */
	private boolean setClassifYes(File hfp) {

		File file = hfp;
		String content = "";
		try {
			Scanner s = new Scanner(file);
			while (s.hasNext()) {
				String line = s.nextLine();
				if (line.contains(CLASSIF_NO)) {
					content += CLASSIF_YES + "\n";
				} else
					content += line + "\n";
			}
			FileOutputStream data = new FileOutputStream(file);
			PrintStream write = new PrintStream(data);
			write.println(content);
			write.close();
			s.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("Unable to find hfp file!");
			return false;
		}
		return true;
	}

	/**
	 * Execute hfpsr function
	 * 
	 * e.g. hfpsr dataset "3 3 3 3" 1 0.01 3 3 MeanMax sum 0.01 -f
	 * 
	 * @param fisDir
	 *            fispro bin path
	 * @param homeDir
	 *            workspace directory
	 * @param dataName
	 *            dataset name
	 * @param nInput
	 *            input number
	 * @param inFuzzySets
	 *            number of input fuzzy sets
	 * @param inHierarchyType
	 *            input hierarchy type
	 * @param inToleranceValue
	 *            input tolerance value
	 * @param outFuzzySets
	 *            number of output fuzzy sets
	 * @param outHierarchyType
	 *            output hierarchy type
	 * @param defuzOp
	 *            defuzzification operator
	 * @param disjOp
	 *            disjunction operator
	 * @param outToleranceValue
	 *            output tolerance value
	 * @param classifOutput
	 *            classification output
	 * 
	 * @return true if function is successful, else false
	 * 
	 */
	private boolean hfpSrFunction(String fisDir, String homeDir, String dataName, int nInput, String inFuzzySets,
			String inHierarchyType, double inToleranceValue, int outFuzzySets, String outHierarchyType, String defuzOp,
			String disjOp, double outToleranceValue, boolean classifOutput) {

		File home = new File(homeDir);
		File hfpsr = new File(fisDir, "hfpsr");
		File data = new File(home, dataName);
		File hfpsrFis = new File(home, HFPSR_FIS);

		if (outFuzzySets == 0 && defuzOp != "sugeno") {
			LOGGER.error("**Output crisp (Number of fuzzy sets for each output variable) defuzzification " + defuzOp
					+ " not allowed! Please select 'sugeno' defuz. operator!**");
			return false;
		}

		String tmp = inFuzzySets;
		if (tmp.split(" ").length >= nInput - 1) {

			String fuzzySetsStr = "\"" + inFuzzySets + "\"";
			String arg = fuzzySetsStr + " " + inHierarchyType + " " + Double.toString(inToleranceValue) + " "
					+ Integer.toString(outFuzzySets) + " " + outHierarchyType + " " + defuzOp + " " + disjOp + " "
					+ Double.toString(outToleranceValue) + " -o" + HFPSR_FIS + (classifOutput ? " -f" : "");
			if (DEBUG)
				LOGGER.error("hfpsr args: " + arg);
			try {
				if (DEBUG)
					LOGGER.error("trying hfpsr command for Linux...");
				String[] cmd = { "/bin/sh", "-c", hfpsr.getPath() + " " + data.getPath() + " " + arg };
				timeCommandStart = System.currentTimeMillis();
				Process p = Runtime.getRuntime().exec(cmd, null, home);
				int control = p.waitFor();
				timeCommandEnd = System.currentTimeMillis();
				if (control == 0) {
					if (!hfpsrFis.exists())
						if (DEBUG)
							LOGGER.error("hfpsr fis NOT Created (Linux).");
				}
			} catch (IOException | InterruptedException e) {
				if (DEBUG)
					LOGGER.error("trying hfpsr command for Win...");
				String[] cmd = { hfpsr.getPath(), data.getPath(), fuzzySetsStr, inHierarchyType,
						Double.toString(inToleranceValue), Integer.toString(outFuzzySets), outHierarchyType, defuzOp,
						disjOp, Double.toString(outToleranceValue), "-o" + HFPSR_FIS, (classifOutput ? " -f" : "") };
				try {
					timeCommandStart = System.currentTimeMillis();
					Process p = Runtime.getRuntime().exec(cmd, null, home);
					int control = p.waitFor();
					timeCommandEnd = System.currentTimeMillis();
					if (control == 0) {
						if (!hfpsrFis.exists())
							if (DEBUG)
								LOGGER.error("hfpsr fis NOT Created (Win).");
					}
				} catch (IOException | InterruptedException e1) {
					LOGGER.error("hfpsr fis NOT Created.");
					return false;
				}
			}
		} else {
			LOGGER.error("Number of fuzzy sets or spaces wrong!");
		}
		return hfpsrFis.exists();
	}

	/**
	 * Return the string path of a random directory
	 * 
	 * @return random path
	 */
	private String getRandomDirectory() {

		File dir = new File(System.getProperty("user.home"), "HFPSR");
		int rand = (int) (Math.random() * 10000000);
		dir = new File(dir + "-" + rand);
		return dir.getPath();
	}

	/**
	 * Create dataset file from BufferedDataTable
	 * 
	 * @param t
	 *            data table
	 * @param homeDir
	 *            directory to save the file
	 * @param dataName
	 *            dataset file name
	 * 
	 * @return true if creation is successful, else false
	 */
	private boolean loadDataset(BufferedDataTable t, String homeDir, String dataName) {

		String[] names = t.getDataTableSpec().getColumnNames();
		String header = "";
		for (int i = 0; i < names.length; i++) {
			if (i + 1 != names.length)
				header += names[i] + TABLE_SEP;
			else
				header += names[i];
		}

		File file = new File(homeDir, dataName);
		try {
			FileOutputStream data = new FileOutputStream(file);
			PrintStream write = new PrintStream(data);
			write.println(header);

			// write data cell's rows in dataset file
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
		FUZZY_SETS_STRING_INPUT.saveSettingsTo(settings);
		HY_TYPE_INPUT.saveSettingsTo(settings);
		TOLERANCE_INPUT.saveSettingsTo(settings);
		FUZZY_SETS_NUMBER_OUTPUT.saveSettingsTo(settings);
		HY_TYPE_OUTPUT.saveSettingsTo(settings);
		DEFUZ_OP.saveSettingsTo(settings);
		DISJ_OP.saveSettingsTo(settings);
		TOLERANCE_OUTPUT.saveSettingsTo(settings);
		CLASSIF_OUTPUT.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		FUZZY_SETS_STRING_INPUT.loadSettingsFrom(settings);
		HY_TYPE_INPUT.loadSettingsFrom(settings);
		TOLERANCE_INPUT.loadSettingsFrom(settings);
		FUZZY_SETS_NUMBER_OUTPUT.loadSettingsFrom(settings);
		HY_TYPE_OUTPUT.loadSettingsFrom(settings);
		DEFUZ_OP.loadSettingsFrom(settings);
		DISJ_OP.loadSettingsFrom(settings);
		TOLERANCE_OUTPUT.loadSettingsFrom(settings);
		CLASSIF_OUTPUT.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		FUZZY_SETS_STRING_INPUT.validateSettings(settings);
		HY_TYPE_INPUT.validateSettings(settings);
		TOLERANCE_INPUT.validateSettings(settings);
		FUZZY_SETS_NUMBER_OUTPUT.validateSettings(settings);
		HY_TYPE_OUTPUT.validateSettings(settings);
		DEFUZ_OP.validateSettings(settings);
		DISJ_OP.validateSettings(settings);
		TOLERANCE_OUTPUT.validateSettings(settings);
		CLASSIF_OUTPUT.validateSettings(settings);
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
