package fistree;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FISTREE" Node.
 * 
 *
 * @author Antonio Di Mauro
 */
public class FISTREENodeFactory 
        extends NodeFactory<FISTREENodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FISTREENodeModel createNodeModel() {
        return new FISTREENodeModel();
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
    public NodeView<FISTREENodeModel> createNodeView(final int viewIndex,
            final FISTREENodeModel nodeModel) {
        return new FISTREENodeView(nodeModel);
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
        return new FISTREENodeDialog();
    }

}

