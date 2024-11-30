package org.example.amqp_consumer;

//import com.zeroclue.jmeter.protocol.amqp.AMQPSampler;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import kg.apc.jmeter.JMeterPluginsUtils;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AMQPSamplerGui extends AbstractSamplerGui {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(AMQPSamplerGui.class);

    private final JLabeledTextField exchange = new JLabeledTextField("    Exchange");
    private final JLabeledChoice exchangeType = new JLabeledChoice("            Type ", AMQPSampler.EXCHANGE_TYPES, false, false);
    private final JCheckBox exchangeDurable = new JCheckBox("Durable", AMQPSampler.DEFAULT_EXCHANGE_DURABLE);
    private final JCheckBox exchangeRedeclare = new JCheckBox("Redeclare", AMQPSampler.DEFAULT_EXCHANGE_REDECLARE);
    private final JCheckBox exchangeAutoDelete = new JCheckBox("Auto Delete", AMQPSampler.DEFAULT_EXCHANGE_AUTO_DELETE);

    protected JLabeledTextField queue = new JLabeledTextField("             Queue");
    protected JLabeledTextField routingKey = new JLabeledTextField("   Routing Key");
    protected JLabeledTextField messageTTL = new JLabeledTextField("Message TTL");
    protected JLabeledTextField messageExpires = new JLabeledTextField("           Expires");
    protected JLabeledTextField maxPriority = new JLabeledTextField("   Max Priority");
    protected final JCheckBox queueDurable = new JCheckBox("Durable", AMQPSampler.DEFAULT_QUEUE_DURABLE);
    protected final JCheckBox queueRedeclare = new JCheckBox("Redeclare", AMQPSampler.DEFAULT_QUEUE_REDECLARE);
    protected final JCheckBox queueExclusive = new JCheckBox("Exclusive", AMQPSampler.DEFAULT_QUEUE_EXCLUSIVE);
    protected final JCheckBox queueAutoDelete = new JCheckBox("Auto Delete", AMQPSampler.DEFAULT_QUEUE_AUTO_DELETE);

    protected JLabeledTextField virtualHost = new JLabeledTextField("Virtual Host");
    protected JLabeledTextField host = new JLabeledTextField("             Host");
    protected JLabeledTextField port = new JLabeledTextField("              Port");
    protected JLabeledTextField timeout = new JLabeledTextField("      Timeout");
    protected JLabeledTextField username = new JLabeledTextField("   Username");
    protected JLabeledTextField password = new JLabeledTextField("   Password");
    protected JLabeledTextField heartbeat = new JLabeledTextField("  Heartbeat");
    private final JCheckBox ssl = new JCheckBox("SSL", AMQPSampler.DEFAULT_SSL_STATE);

    protected final JLabeledTextField iterations = new JLabeledTextField("Number of Samples to Aggregate");

    private static final String EXCHANGE_SETTINGS_LABEL = "Exchange";
    private static final String QUEUE_SETTINGS_LABEL = "Queue";
    private static final String CONNECTION_SETTINGS_LABEL = "Connection";

    private static final String WIKI_PAGE = "https://github.com/aliesbelik/jmeter-amqp-plugin";

    protected abstract void setMainPanel(JPanel panel);

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof AMQPSampler)) {
            return;
        }
        AMQPSampler sampler = (AMQPSampler) element;

        exchange.setText(sampler.getExchange());
        exchangeType.setText(sampler.getExchangeType());
        exchangeDurable.setSelected(sampler.getExchangeDurable());
        exchangeRedeclare.setSelected(sampler.getExchangeRedeclare());
        exchangeAutoDelete.setSelected(sampler.getExchangeAutoDelete());

        queue.setText(sampler.getQueue());
        routingKey.setText(sampler.getRoutingKey());
        messageTTL.setText(sampler.getMessageTTL());
        messageExpires.setText(sampler.getMessageExpires());
        maxPriority.setText(sampler.getMaxPriority());

        queueDurable.setSelected(sampler.queueDurable());
        queueRedeclare.setSelected(sampler.getQueueRedeclare());
        queueAutoDelete.setSelected(sampler.queueAutoDelete());
        queueExclusive.setSelected(sampler.queueExclusive());

        virtualHost.setText(sampler.getVirtualHost());
        host.setText(sampler.getHost());
        port.setText(sampler.getPort());
        username.setText(sampler.getUsername());
        password.setText(sampler.getPassword());
        timeout.setText(sampler.getTimeout());
        heartbeat.setText(sampler.getHeartbeat());
        ssl.setSelected(sampler.getConnectionSSL());

        iterations.setText(sampler.getIterations());

        log.debug("AMQPSamplerGui.configure() called");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        exchange.setText(AMQPSampler.DEFAULT_EXCHANGE_NAME);
        exchangeType.setSelectedIndex(AMQPSampler.DEFAULT_EXCHANGE_TYPE);
        exchangeDurable.setSelected(AMQPSampler.DEFAULT_EXCHANGE_DURABLE);
        exchangeRedeclare.setSelected(AMQPSampler.DEFAULT_EXCHANGE_REDECLARE);
        exchangeAutoDelete.setSelected(AMQPSampler.DEFAULT_EXCHANGE_AUTO_DELETE);

        queue.setText(AMQPSampler.DEFAULT_QUEUE_NAME);
        routingKey.setText(AMQPSampler.DEFAULT_ROUTING_KEY);
        messageTTL.setText(AMQPSampler.DEFAULT_MSG_TTL);
        messageExpires.setText(AMQPSampler.DEFAULT_MSG_EXPIRES);
        maxPriority.setText(AMQPSampler.DEFAULT_MSG_PRIORITY);

        queueDurable.setSelected(AMQPSampler.DEFAULT_QUEUE_DURABLE);
        queueRedeclare.setSelected(AMQPSampler.DEFAULT_QUEUE_REDECLARE);
        queueAutoDelete.setSelected(AMQPSampler.DEFAULT_QUEUE_AUTO_DELETE);
        queueExclusive.setSelected(AMQPSampler.DEFAULT_QUEUE_EXCLUSIVE);

        virtualHost.setText(AMQPSampler.DEFAULT_VIRTUAL_HOST);
        host.setText(AMQPSampler.DEFAULT_HOSTNAME);
        port.setText(AMQPSampler.DEFAULT_PORT_STRING);
        username.setText(AMQPSampler.DEFAULT_USERNAME);
        password.setText(AMQPSampler.DEFAULT_PASSWORD);
        timeout.setText(AMQPSampler.DEFAULT_TIMEOUT_STRING);
        heartbeat.setText(AMQPSampler.DEFAULT_HEARTBEAT_STRING);
        ssl.setSelected(AMQPSampler.DEFAULT_SSL_STATE);

        iterations.setText(AMQPSampler.DEFAULT_ITERATIONS_STRING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement element) {
        AMQPSampler sampler = (AMQPSampler) element;
        sampler.clear();
        configureTestElement(sampler);

        sampler.setExchange(exchange.getText());
        sampler.setExchangeType(exchangeType.getText());
        sampler.setExchangeDurable(exchangeDurable.isSelected());
        sampler.setExchangeRedeclare(exchangeRedeclare.isSelected());
        sampler.setExchangeAutoDelete(exchangeAutoDelete.isSelected());

        sampler.setQueue(queue.getText());
        sampler.setRoutingKey(routingKey.getText());
        sampler.setMessageTTL(messageTTL.getText());
        sampler.setMessageExpires(messageExpires.getText());
        sampler.setMaxPriority(maxPriority.getText());

        sampler.setQueueDurable(queueDurable.isSelected());
        sampler.setQueueRedeclare(queueRedeclare.isSelected());
        sampler.setQueueAutoDelete(queueAutoDelete.isSelected());
        sampler.setQueueExclusive(queueExclusive.isSelected());

        sampler.setVirtualHost(virtualHost.getText());
        sampler.setHost(host.getText());
        sampler.setPort(port.getText());
        sampler.setUsername(username.getText());
        sampler.setPassword(password.getText());
        sampler.setTimeout(timeout.getText());
        sampler.setHeartbeat(heartbeat.getText());
        sampler.setConnectionSSL(ssl.isSelected());

        sampler.setIterations(iterations.getText());

        log.debug("AMQPSamplerGui.modifyTestElement() called, set user/pass to {}/{} on sampler {}",
                username.getText(), password.getText(), sampler);
    }

    protected void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        // TOP panel
        Container topPanel = makeTitlePanel();
        add(JMeterPluginsUtils.addHelpLinkToPanel(topPanel, WIKI_PAGE), BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);

        // MAIN panel
        JPanel mainPanel = new VerticalPanel();
        mainPanel.add(makeCommonPanel());
        mainPanel.add(iterations);

        add(mainPanel);
        setMainPanel(mainPanel);
    }

    private Component makeCommonPanel() {
        GridBagConstraints gridBagConstraintsCommon;

        gridBagConstraintsCommon = new GridBagConstraints();
        gridBagConstraintsCommon.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsCommon.anchor = GridBagConstraints.WEST;
        gridBagConstraintsCommon.weightx = 0.5;

        GridBagConstraints gridBagConstraints;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;

        JPanel commonPanel = new JPanel(new GridBagLayout());

        // Exchange section

        JPanel exchangeSettings = new JPanel(new GridBagLayout());
        exchangeSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), EXCHANGE_SETTINGS_LABEL));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        exchangeSettings.add(exchange, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        exchangeType.setLayout(new BoxLayout(exchangeType, BoxLayout.X_AXIS));
        exchangeSettings.add(exchangeType, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        exchangeSettings.add(exchangeDurable, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        exchangeSettings.add(exchangeRedeclare, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        exchangeSettings.add(exchangeAutoDelete, gridBagConstraints);

        exchangeType.setPreferredSize(exchange.getPreferredSize());
        exchangeSettings.validate();

        // Queue section

        JPanel queueSettings = new JPanel(new GridBagLayout());
        queueSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), QUEUE_SETTINGS_LABEL));
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        queueSettings.add(queue, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        queueSettings.add(routingKey, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        queueSettings.add(messageTTL, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        queueSettings.add(messageExpires, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        queueSettings.add(maxPriority, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        queueSettings.add(queueDurable, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        queueSettings.add(queueRedeclare, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        queueSettings.add(queueAutoDelete, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        queueSettings.add(queueExclusive, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 0;
        gridBagConstraintsCommon.gridy = 0;

        JPanel exchangeQueueSettings = new VerticalPanel();
        exchangeQueueSettings.add(exchangeSettings);
        exchangeQueueSettings.add(queueSettings);

        commonPanel.add(exchangeQueueSettings, gridBagConstraintsCommon);

        // Connection section

        JPanel serverSettings = new JPanel(new GridBagLayout());
        serverSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), CONNECTION_SETTINGS_LABEL));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        serverSettings.add(virtualHost, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        serverSettings.add(host, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        serverSettings.add(port, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        serverSettings.add(username, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        serverSettings.add(password, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        serverSettings.add(timeout, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        serverSettings.add(heartbeat, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        serverSettings.add(ssl, gridBagConstraints);

        gridBagConstraintsCommon.gridx = 1;
        gridBagConstraintsCommon.gridy = 0;

        commonPanel.add(serverSettings, gridBagConstraintsCommon);

        return commonPanel;
    }
}
