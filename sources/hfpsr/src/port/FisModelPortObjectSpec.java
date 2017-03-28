package port;

import java.io.IOException;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

/**
 * FisPro Model Port Object Spec
 *
 * @author Antonio Di Mauro
 */
public class FisModelPortObjectSpec implements PortObjectSpec {

	public static final class Serializer extends PortObjectSpecSerializer<FisModelPortObjectSpec> {

		@Override
		public void savePortObjectSpec(FisModelPortObjectSpec portObjectSpec, PortObjectSpecZipOutputStream out)
				throws IOException {
			// LOGGER.error("savePortObjectSpec");
			portObjectSpec.save(out);
		}

		@Override
		public FisModelPortObjectSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
			// LOGGER.error("loadPortObjectSpec");
			return load(in);
		}
	}

	private void save(PortObjectSpecZipOutputStream out) {
		// TODO Auto-generated method stub
	}

	private static FisModelPortObjectSpec load(final PortObjectSpecZipInputStream in) {
		return new FisModelPortObjectSpec();
	}

	// private static final NodeLogger LOGGER =
	// NodeLogger.getLogger(FisModelPortObjectSpec.class);

	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		return null;
	}

}
