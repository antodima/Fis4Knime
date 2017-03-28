package fisprowriter;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FisProWriter" Node.
 * 
 *
 * @author Antonio Di Mauro
 */
public class FisProWriterNodeFactory 
        extends NodeFactory<FisProWriterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FisProWriterNodeModel createNodeModel() {
        return new FisProWriterNodeModel();
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
    public NodeView<FisProWriterNodeModel> createNodeView(final int viewIndex,
            final FisProWriterNodeModel nodeModel) {
        return new FisProWriterNodeView(nodeModel);
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
        return new FisProWriterNodeDialog();
    }

}

