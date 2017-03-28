package fisproreader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FisProReader" Node.
 * 
 *
 * @author Antonio Di Mauro
 */
public class FisProReaderNodeFactory 
        extends NodeFactory<FisProReaderNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FisProReaderNodeModel createNodeModel() {
        return new FisProReaderNodeModel();
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
    public NodeView<FisProReaderNodeModel> createNodeView(final int viewIndex,
            final FisProReaderNodeModel nodeModel) {
        return new FisProReaderNodeView(nodeModel);
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
        return new FisProReaderNodeDialog();
    }

}

