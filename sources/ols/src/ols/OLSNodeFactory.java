package ols;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "OLS" Node.
 * Orthogonal Least Squares' FisPro Learner
 *
 * @author Antonio Di Mauro
 */
public class OLSNodeFactory 
        extends NodeFactory<OLSNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public OLSNodeModel createNodeModel() {
        return new OLSNodeModel();
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
    public NodeView<OLSNodeModel> createNodeView(final int viewIndex,
            final OLSNodeModel nodeModel) {
        return new OLSNodeView(nodeModel);
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
        return new OLSNodeDialog();
    }

}

