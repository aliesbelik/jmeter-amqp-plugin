package org.example.amqp_consumer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.testelement.ThreadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AMQPSampler extends AbstractSampler implements ThreadListener {

    private static final Logger log = LoggerFactory.getLogger(AMQPSampler.class);

    //++ These are JMX names, and must not be changed
    protected static final String EXCHANGE              = "AMQPSampler.Exchange";
    protected static final String EXCHANGE_TYPE         = "AMQPSampler.ExchangeType";
    protected static final String EXCHANGE_DURABLE      = "AMQPSampler.ExchangeDurable";
    protected static final String EXCHANGE_REDECLARE    = "AMQPSampler.ExchangeRedeclare";
    protected static final String EXCHANGE_AUTO_DELETE  = "AMQPSampler.ExchangeAutoDelete";
    protected static final String QUEUE                 = "AMQPSampler.Queue";
    protected static final String ROUTING_KEY           = "AMQPSampler.RoutingKey";
    protected static final String VIRTUAL_HOST          = "AMQPSampler.VirtualHost";
    protected static final String HOST                  = "AMQPSampler.Host";
    protected static final String PORT                  = "AMQPSampler.Port";
    protected static final String SSL                   = "AMQPSampler.SSL";
    protected static final String XQMODE ="AMQPSampler.xqMode";

    protected static final String XOVERFLOW ="AMQPSampler.xOverflow";
    protected static final String USERNAME              = "AMQPSampler.Username";
    protected static final String PASSWORD              = "AMQPSampler.Password";
    protected static final String HEARTBEAT             = "AMQPSampler.Heartbeat";
    private static final String TIMEOUT                 = "AMQPSampler.Timeout";
    private static final String ITERATIONS              = "AMQPSampler.Iterations";
    private static final String MESSAGE_TTL             = "AMQPSampler.MessageTTL";
    private static final String MESSAGE_EXPIRES         = "AMQPSampler.MessageExpires";
    private static final String MAX_PRIORITY            = "AMQPSampler.MaxPriority";
    private static final String QUEUE_DURABLE           = "AMQPSampler.QueueDurable";
    private static final String QUEUE_REDECLARE         = "AMQPSampler.Redeclare";
    private static final String QUEUE_EXCLUSIVE         = "AMQPSampler.QueueExclusive";
    private static final String QUEUE_AUTO_DELETE       = "AMQPSampler.QueueAutoDelete";

//    public String x_queue_mode = AMQPSampler.DEFAULT_XQ_NAME_MODE;

    public static final String[] EXCHANGE_TYPES = new String[] {
            "direct",
            "topic",
            "headers",
            "fanout"
    };

    public static final int DEFAULT_EXCHANGE_TYPE = Arrays.asList(EXCHANGE_TYPES).indexOf("direct");

    public static final String DEFAULT_EXCHANGE_NAME = "jmeterExchange";




    public static final boolean DEFAULT_EXCHANGE_DURABLE = true;
    public static final boolean DEFAULT_EXCHANGE_AUTO_DELETE = false;
    public static final boolean DEFAULT_EXCHANGE_REDECLARE = false;

    public static final String DEFAULT_QUEUE_NAME = "jmeterQueue";
    public static final String DEFAULT_ROUTING_KEY = "jmeterRoutingKey";

    public static final boolean DEFAULT_QUEUE_DURABLE = true;
    public static final boolean DEFAULT_QUEUE_AUTO_DELETE = false;
    public static final boolean DEFAULT_QUEUE_REDECLARE = false;
    public static final boolean DEFAULT_QUEUE_EXCLUSIVE = false;

    public static final String DEFAULT_MSG_TTL = "";
    public static final String DEFAULT_MSG_EXPIRES = "";
    public static final String DEFAULT_MSG_PRIORITY = "";

    public static final String DEFAULT_VIRTUAL_HOST = "/";
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final String DEFAULT_USERNAME = "guest";
    public static final String DEFAULT_XQMODE = "lazy";
    public static final String DEFAULT_XOVERFLOW = "reject-publish";
    public static final String DEFAULT_PASSWORD = "guest";

    public static final boolean DEFAULT_SSL_STATE = false;
    public static final String DEFAULT_SSL_PROTOCOL = "TLS";

    public static final int DEFAULT_PORT = 5672;
    public static final String DEFAULT_PORT_STRING = Integer.toString(DEFAULT_PORT);

    public static final int DEFAULT_TIMEOUT = 1000;
    public static final String DEFAULT_TIMEOUT_STRING = Integer.toString(DEFAULT_TIMEOUT);

    public static final int DEFAULT_ITERATIONS = 1;
    public static final String DEFAULT_ITERATIONS_STRING = Integer.toString(DEFAULT_ITERATIONS);

    public static final int DEFAULT_MIN_PRIORITY = 0;
    public static final int DEFAULT_MAX_PRIORITY = 255;

    // The value is in seconds, and default value suggested by RabbitMQ is 60.
    public static final int DEFAULT_HEARTBEAT = 60;
    public static final String DEFAULT_HEARTBEAT_STRING = Integer.toString(DEFAULT_HEARTBEAT);

    private final transient ConnectionFactory factory;
    private transient Connection connection;

    protected AMQPSampler() {
        this.factory = new ConnectionFactory();
        this.factory.setRequestedHeartbeat(DEFAULT_HEARTBEAT);
    }

    protected boolean initChannel() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        Channel channel = getChannel();

        try {
            if (channel != null && !channel.isOpen()) {
                log.warn("Channel {} closed unexpectedly: {}", channel.getChannelNumber(), channel.getCloseReason());
                channel = null;     // so we re-open it below
            }

            if (channel == null) {
                channel = createChannel();
                setChannel(channel);

                boolean queueConfigured = configureQueue(channel);

                if (!StringUtils.isBlank(getExchange())) {   // use a named exchange
                    if (getExchangeRedeclare()) {
                        deleteExchange();
                    }

                    AMQP.Exchange.DeclareOk declareExchangeResp = channel.exchangeDeclare(getExchange(), getExchangeType(), getExchangeDurable(), getExchangeAutoDelete(), Collections.<String, Object>emptyMap());

                    if (queueConfigured) {
                        channel.queueBind(getQueue(), getExchange(), getRoutingKey());
                    }
                }

                log.debug("Bound to:"
                                + "\n\t queue: {}"
                                + "\n\t exchange: {}"
                                + "\n\t durable: {}"
                                + "\n\t routing key: {}"
                                + "\n\t arguments: {}",
                        getQueue(), getExchange(), getExchangeDurable(), getRoutingKey(), getQueueArguments());
            }
        } catch (Exception ex) {
            log.debug(ex.toString(), ex);
            // ignore it
        }

        return true;
    }

    protected boolean configureQueue(Channel channel) throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        boolean queueConfigured = (getQueue() != null && !getQueue().isEmpty());

        if (queueConfigured) {
            if (getQueueRedeclare()) {
                deleteQueue();
            }

            AMQP.Queue.DeclareOk declareQueueResp = channel.queueDeclare(getQueue(), queueDurable(), queueExclusive(), queueAutoDelete(), getQueueArguments());
        }
        return queueConfigured;
    }

    private Map<String, Object> getQueueArguments() {
        Map<String, Object> arguments = new HashMap<>();



        arguments.put("x-queue-mode", getXqmode());
        arguments.put("x-overflow",getXOverFlow());


        if (getMessageTTL() != null && !getMessageTTL().isEmpty()) {
            arguments.put("x-message-ttl", getMessageTTLAsInt());
        }

        if (getMessageExpires() != null && !getMessageExpires().isEmpty()) {
            arguments.put("x-expires", getMessageExpiresAsInt());
        }

        if (getMaxPriority() != null && !getMaxPriority().isEmpty()) {
            arguments.put("x-max-priority", getMaxPriorityAsInt());
        }

        return arguments;
    }

    protected abstract Channel getChannel();

    protected abstract void setChannel(Channel channel);

    /**
     * @return a string for the sampleResult Title
     */
    protected String getTitle() {
        return this.getName();
    }

    protected int getTimeoutAsInt() {
        if (getPropertyAsInt(TIMEOUT) < 1) {
            return DEFAULT_TIMEOUT;
        }

        return getPropertyAsInt(TIMEOUT);
    }

    public String getTimeout() {
        return getPropertyAsString(TIMEOUT, DEFAULT_TIMEOUT_STRING);
    }

    public void setTimeout(String s) {
        setProperty(TIMEOUT, s);
    }

    public String getIterations() {
        return getPropertyAsString(ITERATIONS, DEFAULT_ITERATIONS_STRING);
    }

    public void setIterations(String s) {
        setProperty(ITERATIONS, s);
    }

    public int getIterationsAsInt() {
        return getPropertyAsInt(ITERATIONS, DEFAULT_ITERATIONS);
    }

    public String getExchange() {
        return getPropertyAsString(EXCHANGE);
    }

    public void setExchange(String name) {
        setProperty(EXCHANGE, name);
    }

    public boolean getExchangeDurable() {
        return getPropertyAsBoolean(EXCHANGE_DURABLE);
    }

    public void setExchangeDurable(boolean durable) {
        setProperty(EXCHANGE_DURABLE, durable);
    }

    public String getExchangeType() {
        return getPropertyAsString(EXCHANGE_TYPE);
    }

    public boolean getExchangeAutoDelete() {
        return getPropertyAsBoolean(EXCHANGE_AUTO_DELETE);
    }

    public void setExchangeAutoDelete(boolean autoDelete) {
        setProperty(EXCHANGE_AUTO_DELETE, autoDelete);
    }

    public void setExchangeType(String name) {
        setProperty(EXCHANGE_TYPE, name);
    }

    public boolean getExchangeRedeclare() {
        return getPropertyAsBoolean(EXCHANGE_REDECLARE);
    }

    public void setExchangeRedeclare(boolean content) {
        setProperty(EXCHANGE_REDECLARE, content);
    }

    public String getQueue() {
        return getPropertyAsString(QUEUE);
    }

    public void setQueue(String name) {
        setProperty(QUEUE, name);
    }

    public String getRoutingKey() {
        return getPropertyAsString(ROUTING_KEY);
    }

    public void setRoutingKey(String name) {
        setProperty(ROUTING_KEY, name);
    }

    public String getVirtualHost() {
        return getPropertyAsString(VIRTUAL_HOST);
    }

    public void setVirtualHost(String name) {
        setProperty(VIRTUAL_HOST, name);
    }

    public String getMessageTTL() {
        return getPropertyAsString(MESSAGE_TTL);
    }

    public void setMessageTTL(String name) {
        setProperty(MESSAGE_TTL, name);
    }

    protected Integer getMessageTTLAsInt() {
        if (getPropertyAsInt(MESSAGE_TTL) < 1) {
            return null;
        }

        return getPropertyAsInt(MESSAGE_TTL);
    }

    public String getMessageExpires() {
        return getPropertyAsString(MESSAGE_EXPIRES);
    }

    public void setMessageExpires(String name) {
        setProperty(MESSAGE_EXPIRES, name);
    }

    protected Integer getMessageExpiresAsInt() {
        if (getPropertyAsInt(MESSAGE_EXPIRES) < 1) {
            return null;
        }

        return getPropertyAsInt(MESSAGE_EXPIRES);
    }

    public String getMaxPriority() {
        return getPropertyAsString(MAX_PRIORITY);
    }

    public void setMaxPriority(String name) {
        setProperty(MAX_PRIORITY, name);
    }

    protected Integer getMaxPriorityAsInt() {
        // The message priority field is defined as an unsigned byte,
        // so in practice priorities should be between 0 and 255
        if (getPropertyAsInt(MAX_PRIORITY) < DEFAULT_MIN_PRIORITY) {
            return DEFAULT_MIN_PRIORITY;
        } else if (getPropertyAsInt(MAX_PRIORITY) > DEFAULT_MAX_PRIORITY) {
            return DEFAULT_MAX_PRIORITY;
        }

        return getPropertyAsInt(MAX_PRIORITY);
    }

    public String getHost() {
        return getPropertyAsString(HOST);
    }

    public void setHost(String name) {
        setProperty(HOST, name);
    }

    public String getPort() {
        return getPropertyAsString(PORT);
    }

    public void setPort(String name) {
        setProperty(PORT, name);
    }

    protected int getPortAsInt() {
        if (getPropertyAsInt(PORT) < 1) {
            return DEFAULT_PORT;
        }

        return getPropertyAsInt(PORT);
    }

    public boolean getConnectionSSL() {
        return getPropertyAsBoolean(SSL);
    }

    public void setConnectionSSL(String content) {
        setProperty(SSL, content);
    }

    public void setConnectionSSL(Boolean value) {
        setProperty(SSL, value.toString());
    }

    public String getUsername() {
        return getPropertyAsString(USERNAME);
    }

    public void setUsername(String name) {
        setProperty(USERNAME, name);
    }
    public String getXqmode() {
        return getPropertyAsString(XQMODE);
    }

    public void setXqmode(String name) {
        setProperty(XQMODE, name);
    }

    public String getXOverFlow() {
        return getPropertyAsString(XOVERFLOW);
    }

    public void setXOverFlow(String name) {
        setProperty(XOVERFLOW, name);
    }

    public String getPassword() {
        return getPropertyAsString(PASSWORD);
    }

    public void setPassword(String name) {
        setProperty(PASSWORD, name);
    }

    public String getHeartbeat() {
        return getPropertyAsString(HEARTBEAT);
    }

    public void setHeartbeat(String value) {
        setProperty(HEARTBEAT, value);
    }

    public int getHeartbeatAsInt() {
        int hb = getPropertyAsInt(HEARTBEAT);

        if ((hb >= 0) && (hb <= 60)) {
            return hb;
        }

        return DEFAULT_HEARTBEAT;
    }

    /**
     * @return the whether the queue is durable
     */
    public String getQueueDurable() {
        return getPropertyAsString(QUEUE_DURABLE);
    }

    public void setQueueDurable(String content) {
        setProperty(QUEUE_DURABLE, content);
    }

    public void setQueueDurable(Boolean value) {
        setProperty(QUEUE_DURABLE, value.toString());
    }

    public boolean queueDurable() {
        return getPropertyAsBoolean(QUEUE_DURABLE);
    }

    /**
     * @return the whether the queue is exclusive
     */
    public String getQueueExclusive() {
        return getPropertyAsString(QUEUE_EXCLUSIVE);
    }

    public void setQueueExclusive(String content) {
        setProperty(QUEUE_EXCLUSIVE, content);
    }

    public void setQueueExclusive(Boolean value) {
        setProperty(QUEUE_EXCLUSIVE, value.toString());
    }

    public boolean queueExclusive() {
        return getPropertyAsBoolean(QUEUE_EXCLUSIVE);
    }

    /**
     * @return the whether the queue should auto delete
     */
    public String getQueueAutoDelete() {
        return getPropertyAsString(QUEUE_AUTO_DELETE);
    }

    public void setQueueAutoDelete(String content) {
        setProperty(QUEUE_AUTO_DELETE, content);
    }

    public void setQueueAutoDelete(Boolean value) {
        setProperty(QUEUE_AUTO_DELETE, value.toString());
    }

    public boolean queueAutoDelete() {
        return getPropertyAsBoolean(QUEUE_AUTO_DELETE);
    }

    /**
     * @return the whether the queue should be redeclared
     */
    public boolean getQueueRedeclare() {
        return getPropertyAsBoolean(QUEUE_REDECLARE);
    }

    public void setQueueRedeclare(Boolean content) {
        setProperty(QUEUE_REDECLARE, content);
    }

    protected void cleanup() {
        try {
            // getChannel().close();   // closing the connection will close the channel if it's still open
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (IOException e) {
            log.error("Failed to close connection", e);
        } catch (AlreadyClosedException e) {
            log.error("Connection already closed", e);
        } catch (ShutdownSignalException e) {
            log.error("Connection shutdown by thread close", e);
        }
    }

    @Override
    public void threadFinished() {
        log.info("AMQPSampler.threadFinished called");
        cleanup();
    }

    @Override
    public void threadStarted() {
        log.info("AMQPSampler.threadStarted called");
    }

    protected Channel createChannel() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        log.info("Creating channel {}:{}", getVirtualHost(), getPortAsInt());

        if (connection == null || !connection.isOpen()) {
            factory.setConnectionTimeout(getTimeoutAsInt());
            factory.setVirtualHost(getVirtualHost());
            factory.setUsername(getUsername());
            factory.setPassword(getPassword());
            factory.setRequestedHeartbeat(getHeartbeatAsInt());

            if (getConnectionSSL()) {
                factory.useSslProtocol(DEFAULT_SSL_PROTOCOL);
            }

            log.info("RabbitMQ ConnectionFactory using:"
                            + "\n\t virtual host: {}"
                            + "\n\t host: {}"
                            + "\n\t port: {}"
                            + "\n\t username: {}"
                            + "\n\t password: {}"
                            + "\n\t timeout: {}"
                            + "\n\t heartbeat: {}"
                            + "\nin {}",
                    getVirtualHost(), getHost(), getPort(), getUsername(), getPassword(), getTimeout(),
                    getHeartbeatAsInt(), this);

            String[] hosts = getHost().split(",");
            Address[] addresses = new Address[hosts.length];

            for (int i = 0; i < hosts.length; i++) {
                addresses[i] = new Address(hosts[i], getPortAsInt());
            }

            if (log.isDebugEnabled()) {
                log.debug("Using hosts: {} addresses: {}", Arrays.toString(hosts), Arrays.toString(addresses));
            }

            connection = factory.newConnection(addresses);
        }

        Channel channel = connection.createChannel();

        if (!channel.isOpen()) {
            log.error("Failed to open channel: {}", channel.getCloseReason().getLocalizedMessage());
        }

        return channel;
    }

    protected void deleteQueue() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        // use a different channel since channel closes on exception.
        Channel channel = createChannel();

        try {
            log.info("Deleting queue {}", getQueue());
            channel.queueDelete(getQueue());
        } catch (Exception ex) {
            log.debug(ex.toString(), ex);
            // ignore it
        } finally {
            if (channel.isOpen()) {
                try {
                    channel.close();
                } catch (TimeoutException e) {
                    log.error("Timeout Exception: cannot close channel", e);
                }
            }
        }
    }

    protected void deleteExchange() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        // use a different channel since channel closes on exception
        Channel channel = createChannel();

        try {
            log.info("Deleting exchange {}", getExchange());
            channel.exchangeDelete(getExchange());
        } catch (Exception ex) {
            log.warn(ex.toString(), ex);
            // ignore it
        } finally {
            if (channel.isOpen()) {
                try {
                    channel.close();
                } catch (TimeoutException e) {
                    log.error("Timeout Exception: cannot close channel", e);
                }
            }
        }
    }
//    public void updateQueueParameters(String queueName) throws IOException {
//        try {
//            initChannel(); // Initialize the channel
//            Map<String, Object> queueArgs = new HashMap<>();
//            queueArgs.put("x-queue-mode", "lazy");
//            getChannel().queueDeclare(queueName, true, false, false, queueArgs);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new IOException("Failed to update queue parameters", e);
//        }
//    }
}
