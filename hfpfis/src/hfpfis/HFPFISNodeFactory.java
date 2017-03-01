package hfpfis;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "HFPFIS" Node.
 * HFPFIS' FisPro Learner
 *
 * @author Antonio Di Mauro
 */
public class HFPFISNodeFactory 
        extends NodeFactory<HFPFISNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public HFPFISNodeModel createNodeModel() {
        return new HFPFISNodeModel();
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
    public NodeView<HFPFISNodeModel> createNodeView(final int viewIndex,
            final HFPFISNodeModel nodeModel) {
        return new HFPFISNodeView(nodeModel);
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
        return new HFPFISNodeDialog();
    }

}

