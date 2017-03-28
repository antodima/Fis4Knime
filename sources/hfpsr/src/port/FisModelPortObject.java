package port;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

/**
 * FisPro Model Port Object
 *
 * @author Antonio Di Mauro
 */
public class FisModelPortObject implements PortObject {

	// https://tech.knime.org/forum/knime-developers/switch-fispro-model-generated-by-a-node-to-another-node
	// https://tech.knime.org/forum/knime-developers/unable-to-clone-input-data-at-port-1-in-port-name-no-value-present

	/**
	 * Serializer for {@link FisModelPortObject}s.
	 */
	public static final class Serializer extends PortObjectSerializer<FisModelPortObject> {

		/** {@inheritDoc} */
		@Override
		public void savePortObject(final FisModelPortObject portObject, final PortObjectZipOutputStream out,
				final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			portObject.save(out);
		}

		/** {@inheritDoc} */
		@Override
		public FisModelPortObject loadPortObject(final PortObjectZipInputStream in, final PortObjectSpec spec,
				final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			return load(in, (FisModelPortObjectSpec) spec);
		}
	}

	/**
	 * Save the port object in the output stream
	 * 
	 * @param out
	 *            output stream
	 */
	private void save(final PortObjectZipOutputStream out) {
		ObjectOutputStream oo = null;
		try {
			out.putNextEntry(new ZipEntry("fismodel.objectout"));
			oo = new ObjectOutputStream(new NonClosableOutputStream.Zip(out));
			oo.writeObject(mModel);
		} catch (IOException ioe) {
			LOGGER.error("Internal error: Could not save settings", ioe);
		} finally {
			if (oo != null) {
				try {
					oo.close();
				} catch (Exception e) {
					LOGGER.debug("Could not close stream", e);
				}
			}
		}
	}

	/**
	 * Load the port object in the output stream
	 * 
	 * @param in
	 *            input stream
	 * @param spec
	 *            FisModelPortObjectSpec
	 * 
	 * @return Fispro Model
	 */
	private static FisModelPortObject load(final PortObjectZipInputStream in, final FisModelPortObjectSpec spec) {
		ObjectInputStream oi = null;
		FisModel model = null;
		try {
			// load classifier
			ZipEntry zentry = in.getNextEntry();
			assert zentry.getName().equals("fismodel.objectout");
			oi = new ObjectInputStream(new NonClosableInputStream.Zip(in));
			model = (FisModel) oi.readObject();
		} catch (IOException | ClassNotFoundException e) {
			LOGGER.error("Internal error: Could not load settings", e);
		} finally {
			if (oi != null) {
				try {
					oi.close();
				} catch (Exception e) {
					LOGGER.debug("Could not close stream", e);
				}
			}
		}
		return new FisModelPortObject(model, spec);
	}

	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(FisModelPortObject.class);
	private static final NodeLogger LOGGER = NodeLogger.getLogger(FisModelPortObject.class);

	private FisModel mModel;
	private FisModelPortObjectSpec mSpec;

	public FisModelPortObject(FisModel model, FisModelPortObjectSpec spec) {
		mModel = model;
		mSpec = spec;
	}

	public FisModel getFisModel() {
		return mModel;
	}

	@Override
	public String getSummary() {
		return mModel.getSum();
	}

	@Override
	public PortObjectSpec getSpec() {
		return mSpec;
	}

	@Override
	public JComponent[] getViews() {
		return null;
	}
}
