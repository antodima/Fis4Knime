package hfpsr;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "HFPSR" Node.
 * Hfpsr FisPro Function to make configuration model for the Learner
 *
 * @author Antonio Di Mauro
 */
public class HFPSRNodeFactory 
        extends NodeFactory<HFPSRNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public HFPSRNodeModel createNodeModel() {
        return new HFPSRNodeModel();
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
    public NodeView<HFPSRNodeModel> createNodeView(final int viewIndex,
            final HFPSRNodeModel nodeModel) {
        return new HFPSRNodeView(nodeModel);
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
        return new HFPSRNodeDialog();
    }

}

