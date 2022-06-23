package com.zeroclue.jmeter.protocol.amqp.gui;

import com.zeroclue.jmeter.protocol.amqp.AMQPPublisher;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * GUI for AMQP Publisher.
 */
public class AMQPPublisherGui extends AMQPSamplerGui {

    private static final long serialVersionUID = 1L;

    private final JLabeledTextArea message = new JLabeledTextArea("Message Content");
    private final JLabeledTextField messageRoutingKey = new JLabeledTextField("          Routing Key");
    private final JLabeledTextField messageType = new JLabeledTextField("     Message Type");
    private final JLabeledTextField replyToQueue = new JLabeledTextField("   Reply-To Queue");
    private final JLabeledTextField messageId = new JLabeledTextField("           Message ID");
    private final JLabeledTextField correlationId = new JLabeledTextField("       Correlation ID");
    private final JLabeledTextField messagePriority = new JLabeledTextField(" Message Priority");
    private final JLabeledTextField contentType = new JLabeledTextField("        Content-Type");
    private final JLabeledTextField contentEncoding = new JLabeledTextField("Content Encoding");

    private final JCheckBox persistent = new JCheckBox("Persistent", AMQPPublisher.DEFAULT_PERSISTENT);
    private final JCheckBox useTx = new JCheckBox("Use Transactions", AMQPPublisher.DEFAULT_USE_TX);

    private final ArgumentsPanel headers = new AMQPHeadersPanel("Headers");

    private JPanel mainPanel;

    public AMQPPublisherGui() {
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getStaticLabel() {
        return "AMQP Publisher";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AMQPPublisher)) {
            return;
        }
        AMQPPublisher sampler = (AMQPPublisher) element;

        persistent.setSelected(sampler.getPersistent());
        useTx.setSelected(sampler.getUseTx());

        messageRoutingKey.setText(sampler.getMessageRoutingKey());
        messageType.setText(sampler.getMessageType());
        replyToQueue.setText(sampler.getReplyToQueue());
        contentType.setText(sampler.getContentType());
        contentEncoding.setText(sampler.getContentEncoding());
        correlationId.setText(sampler.getCorrelationId());
        messagePriority.setText(sampler.getMessagePriority());
        messageId.setText(sampler.getMessageId());
        message.setText(sampler.getMessage());

        configureHeaders(sampler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        AMQPPublisher sampler = new AMQPPublisher();
        modifyTestElement(sampler);

        return sampler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement te) {
        AMQPPublisher sampler = (AMQPPublisher) te;
        sampler.clear();
        configureTestElement(sampler);

        super.modifyTestElement(sampler);

        sampler.setPersistent(persistent.isSelected());
        sampler.setUseTx(useTx.isSelected());

        sampler.setMessageRoutingKey(messageRoutingKey.getText());
        sampler.setMessage(message.getText());
        sampler.setMessageType(messageType.getText());
        sampler.setReplyToQueue(replyToQueue.getText());
        sampler.setCorrelationId(correlationId.getText());
        sampler.setMessagePriority(messagePriority.getText());
        sampler.setContentType(contentType.getText());
        sampler.setContentEncoding(contentEncoding.getText());
        sampler.setMessageId(messageId.getText());

        sampler.setHeaders((Arguments) headers.createTestElement());
    }

    @Override
    protected void setMainPanel(JPanel panel) {
        mainPanel = panel;
    }

    /*
     * Helper method to set up the GUI screen
     */
    @Override
    protected final void init() {
        super.init();

        mainPanel.add(initMessagePanel());
    }

    private JPanel initMessagePanel() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new java.awt.Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.weightx = 1.0;
        constraints.weighty = 0.5;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        JPanel messagePanel = new JPanel(new GridBagLayout());
        messagePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getMessageSettingsLabel()));

        JPanel optionsPanel = new HorizontalPanel();
        optionsPanel.add(persistent);
        optionsPanel.add(useTx);
        messagePanel.add(optionsPanel, constraints);

        messagePanel.add(initMessagePropertyPanel(), constraints);
        message.setPreferredSize(new Dimension(400, 200));
        messagePanel.add(message, constraints);
        messagePanel.add(headers, constraints);

        return messagePanel;
    }

    private JPanel initMessagePropertyPanel() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new java.awt.Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.weightx = 1.0;
        constraints.weighty = 0.5;
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        JPanel propertyPanel = new JPanel(new GridBagLayout());
        propertyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), getPropertiesSettingsLabel()));

        propertyPanel.add(messageRoutingKey, constraints);
        propertyPanel.add(replyToQueue, constraints);
        propertyPanel.add(messageType, constraints);
        propertyPanel.add(correlationId, constraints);
        propertyPanel.add(messageId, constraints);
        propertyPanel.add(messagePriority, constraints);
        propertyPanel.add(contentType, constraints);
        propertyPanel.add(contentEncoding, constraints);

        return propertyPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();
        persistent.setSelected(AMQPPublisher.DEFAULT_PERSISTENT);
        useTx.setSelected(AMQPPublisher.DEFAULT_USE_TX);
        messageRoutingKey.setText("");
        messageType.setText("");
        replyToQueue.setText("");
        correlationId.setText("");
        messagePriority.setText("");
        contentType.setText(AMQPPublisher.DEFAULT_CONTENT_TYPE);
        contentEncoding.setText(AMQPPublisher.DEFAULT_ENCODING);
        messageId.setText("");
        message.setText("");
        headers.clearGui();
    }

    private void configureHeaders(AMQPPublisher sampler) {
        Arguments sampleHeaders = sampler.getHeaders();
        if (sampleHeaders != null) {
            headers.configure(sampleHeaders);
        } else {
            headers.clearGui();
        }
    }

    protected String getMessageSettingsLabel() {
        return "Message";
    }

    protected String getPropertiesSettingsLabel() {
        return "Properties";
    }
}
