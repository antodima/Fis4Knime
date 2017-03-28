package perf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import port.*;

/**
 * This is the model implementation of PERF. Perf's FisPro Predictor
 *
 * @author Antonio Di Mauro
 */
public class PERFNodeModel extends NodeModel {

	private final boolean DEBUG = false;

	// the logger instance
	private static final NodeLogger LOGGER = NodeLogger.getLogger(PERFNodeModel.class);

	private final String MODEL_FIS = "model.fis";
	private final String FIS_BIN_PATH = "FIS_BIN_PATH";
	private final String TABLE_SEP = ",";
	private final String DATA_NAME = "dataset.csv";
	private final String PERF_RES = "perf.res";
	private final String RESULT_PERF = "result.perf";
	private DataTableSpec mOutputSpec;

	private double timeStart;
	private double timeEnd;
	private double timeCommandStart;
	private double timeCommandEnd;

	/**
	 * Constructor for the node model.
	 */
	protected PERFNodeModel() {
		// (number-input-ports, number-output-ports)
		super(new PortType[] { FisModelPortObject.TYPE, BufferedDataTable.TYPE },
				new PortType[] { BufferedDataTable.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		FisModelPortObject modelPort = (FisModelPortObject) inData[0];
		BufferedDataTable data = (BufferedDataTable) inData[1];
		FisModel fisModel = modelPort.getFisModel();
		BufferedDataTable out = null;

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

				File modelDir = new File(homeDir, MODEL_FIS);
				File model = fisModel.getFis(modelDir.getPath());
				if (model.exists()) {
					/*
					 * sequence of functions to calculate performance of Fuzzy
					 * Inference System Model file .fis from a dataset
					 */
					timeStart = System.currentTimeMillis();
					if (loadDataset(data, homeDir, DATA_NAME)) {
						if (perfFunction(fisDir, homeDir, model.getPath(), DATA_NAME)) {
							out = appendColumn(data, perfToTable(homeDir, PERF_RES, exec), exec);
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
	 * Append column to table
	 * 
	 * @param data
	 *            original table
	 * @param perf
	 *            table with the column to add
	 * 
	 * @return data table with column addiction from perf table
	 */
	private BufferedDataTable appendColumn(BufferedDataTable data, BufferedDataTable perf, ExecutionContext exec) {

		BufferedDataTable out = null;
		DataColumnSpec[] allColSpecs = new DataColumnSpec[data.getSpec().getNumColumns() + 1];
		for (int i = 0; i < allColSpecs.length; i++) {
			if (i != allColSpecs.length - 1) {
				allColSpecs[i] = new DataColumnSpecCreator(data.getSpec().getColumnSpec(i).getName(), DoubleCell.TYPE)
						.createSpec();
			} else {
				allColSpecs[i] = new DataColumnSpecCreator("INF", DoubleCell.TYPE).createSpec();
			}
		}
		mOutputSpec = new DataTableSpec(allColSpecs);
		BufferedDataContainer container = exec.createDataContainer(mOutputSpec);
		CloseableRowIterator itData = data.iterator();
		CloseableRowIterator itPerf = perf.iterator();
		int n = 0;
		int l = data.getSpec().getNumColumns() + 1;
		while (itData.hasNext() && itPerf.hasNext()) {
			double[] vett = new double[l];
			DataRow dataR = itData.next();
			DataRow perfR = itPerf.next();
			for (int i = 0; i < vett.length; i++) {
				if (i != vett.length - 1) {
					vett[i] = Double.parseDouble(dataR.getCell(i).toString());
				} else {
					vett[i] = Double.parseDouble(perfR.getCell(perf.getSpec().findColumnIndex("INF")).toString());
				}
			}
			DataRow row = new DefaultRow("Row" + n, vett);
			container.addRowToTable(row);
			n++;
		}
		container.close();
		out = container.getTable();
		return out;
	}

	/**
	 * Convert the perf file into a table
	 * 
	 * @param homeDir
	 *            home directory
	 * @param perfResName
	 *            perf file name
	 * @param exec
	 *            execution context
	 * 
	 * @return the corrisponding perf table
	 */
	private BufferedDataTable perfToTable(String homeDir, String perfResName, ExecutionContext exec) {

		BufferedDataTable out = null;
		File res = new File(homeDir, perfResName);
		DataColumnSpec[] allColSpecs = new DataColumnSpec[6];
		allColSpecs[0] = new DataColumnSpecCreator("OBS", StringCell.TYPE).createSpec();
		allColSpecs[1] = new DataColumnSpecCreator("INF", StringCell.TYPE).createSpec();
		allColSpecs[2] = new DataColumnSpecCreator("Al", StringCell.TYPE).createSpec();
		allColSpecs[3] = new DataColumnSpecCreator("ERR", StringCell.TYPE).createSpec();
		allColSpecs[4] = new DataColumnSpecCreator("Bl", StringCell.TYPE).createSpec();
		allColSpecs[5] = new DataColumnSpecCreator("CERR2", StringCell.TYPE).createSpec();
		DataTableSpec spec = new DataTableSpec(allColSpecs);
		BufferedDataContainer container = exec.createDataContainer(spec);
		try {
			Scanner s = new Scanner(res);
			int n = 0;
			while (s.hasNextLine()) {
				String line = s.nextLine();
				if (n > 0)
					container.addRowToTable(getDataRow(line, n));
				n++;
			}
			container.close();
			s.close();
			out = container.getTable();
		} catch (FileNotFoundException e) {
		}
		return out;
	}

	/**
	 * Extract a data row from a line
	 * 
	 * @param line
	 *            line
	 * @param n
	 *            line number
	 * 
	 * @return the corresponding data row
	 */
	private DataRow getDataRow(String line, int n) {

		RowKey key = new RowKey("Row" + n);
		DataRow row = null;
		String[] vett = new String[6];

		int pos = 0;
		int i = 0;
		while (i <= line.length() - 1) {
			if (line.charAt(i) != ' ') {
				int j = i + 1;
				while (j <= line.length() - 1 && line.charAt(j) != ' ')
					j++;
				String tmp = "";
				for (int k = i; k < j; k++)
					tmp += line.charAt(k);
				i = j - 1;
				if (pos != 6) {
					vett[pos] = tmp;
					pos++;
				}
				System.out.println(tmp);
			}
			i++;
		}
		row = new DefaultRow(key, vett);
		return row;
	}

	/**
	 * Execute perf function
	 * 
	 * @param fisDir
	 *            fispro bin path
	 * @param homeDir
	 *            home directory
	 * @param modelPath
	 *            model path
	 * @param dataName
	 *            dataset name
	 * 
	 * @return true if creation of perf files is successful, else false
	 */
	private boolean perfFunction(String fisDir, String homeDir, String modelPath, String dataName) {

		File perf = new File(fisDir, "perf");
		File data = new File(homeDir, dataName);
		File home = new File(homeDir);
		File perfRes = new File(homeDir, PERF_RES);
		File resultPerf = new File(homeDir, RESULT_PERF);
		String[] cmd = { perf.getPath(), modelPath, data.getPath() };

		try {
			timeCommandStart = System.currentTimeMillis();
			Process p = Runtime.getRuntime().exec(cmd, null, home);
			int control = p.waitFor();
			timeCommandEnd = System.currentTimeMillis();
			if (control == 0) {
				if (!perfRes.exists() && !resultPerf.exists())
					LOGGER.error("Perf files NOT Created.");
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Perf files NOT Created.");
			return false;
		}
		return perfRes.exists() && resultPerf.exists();
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

		File dir = new File(System.getProperty("user.home"), "PERF");
		int rand = (int) (Math.random() * 10000000);
		dir = new File(dir + "-" + rand);
		return dir.getPath();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { mOutputSpec };
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
