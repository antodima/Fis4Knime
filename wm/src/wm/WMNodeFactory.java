package wm;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "WM" Node. Wang & Mendel's Fispro Function
 * Node
 *
 * @author Antonio Di Mauro
 */
public class WMNodeFactory extends NodeFactory<WMNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WMNodeModel createNodeModel() {
		return new WMNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<WMNodeModel> createNodeView(final int viewIndex, final WMNodeModel nodeModel) {
		return new WMNodeView(nodeModel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new WMNodeDialog();
	}

}
