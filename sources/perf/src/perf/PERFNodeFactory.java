package perf;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "PERF" Node.
 * Perf Node to make inference on a FisPro Model
 *
 * @author Antonio Di Mauro
 */
public class PERFNodeFactory 
        extends NodeFactory<PERFNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PERFNodeModel createNodeModel() {
        return new PERFNodeModel();
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
    public NodeView<PERFNodeModel> createNodeView(final int viewIndex,
            final PERFNodeModel nodeModel) {
        return new PERFNodeView(nodeModel);
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
        return new PERFNodeDialog();
    }

}

