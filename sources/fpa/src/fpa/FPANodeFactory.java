package fpa;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FPA" Node.
 * Fpa's FisPro Function Learner
 *
 * @author Antonio Di Mauro
 */
public class FPANodeFactory 
        extends NodeFactory<FPANodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FPANodeModel createNodeModel() {
        return new FPANodeModel();
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
    public NodeView<FPANodeModel> createNodeView(final int viewIndex,
            final FPANodeModel nodeModel) {
        return new FPANodeView(nodeModel);
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
        return new FPANodeDialog();
    }

}

