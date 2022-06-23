package com.zeroclue.jmeter.protocol.amqp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMeter creates an instance of a sampler class for every occurrence of the
 * element in every thread. Some additional copies may be created before the
 * test run starts.
 *
 * <p>Thus each sampler is guaranteed to be called by a single thread - there is no
 * need to synchronize access to instance variables.
 *
 * <p>However, access to class fields must be synchronized.
 */
public class AMQPPublisher extends AMQPSampler implements Interruptible {

    private static final long serialVersionUID = -8420658040465788497L;

    private static final Logger log = LoggerFactory.getLogger(AMQPPublisher.class);
    private static final Map<String, Function<String, Object>> CONVERTERS = getConverters();

    //++ These are JMX names, and must not be changed
    private static final String MESSAGE             = "AMQPPublisher.Message";
    private static final String MESSAGE_ROUTING_KEY = "AMQPPublisher.MessageRoutingKey";
    private static final String MESSAGE_TYPE        = "AMQPPublisher.MessageType";
    private static final String REPLY_TO_QUEUE      = "AMQPPublisher.ReplyToQueue";
    private static final String CONTENT_TYPE        = "AMQPPublisher.ContentType";
    private static final String CORRELATION_ID      = "AMQPPublisher.CorrelationId";
    private static final String CONTENT_ENCODING    = "AMQPPublisher.ContentEncoding";
    private static final String MESSAGE_ID          = "AMQPPublisher.MessageId";
    private static final String MESSAGE_PRIORITY    = "AMQPPublisher.MessagePriority";
    private static final String HEADERS             = "AMQPPublisher.Headers";
    private static final String PERSISTENT          = "AMQPPublisher.Persistent";
    private static final String USE_TX              = "AMQPPublisher.UseTx";

    public static final boolean DEFAULT_PERSISTENT   = false;
    public static final boolean DEFAULT_USE_TX       = false;
    public static final int DEFAULT_MESSAGE_PRIORITY = 0;
    public static final String DEFAULT_RESPONSE_CODE = "500";
    public static final String DEFAULT_CONTENT_TYPE  = "text/plain";
    public static final String DEFAULT_ENCODING      = "utf-8";

    private transient Channel channel;

    public AMQPPublisher() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry e) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false);
        result.setResponseCode(DEFAULT_RESPONSE_CODE);

        try {
            initChannel();
        } catch (Exception ex) {
            log.error("Failed to initialize channel : ", ex);
            result.setResponseMessage(ex.toString());
            return result;
        }

        String data = getMessage();     // sampler data

        result.setSampleLabel(getTitle());

        /*
         * Perform the sampling
         */

        // aggregate samples
        int loop = getIterationsAsInt();
        result.sampleStart();   // start timing

        try {
            AMQP.BasicProperties messageProperties = getProperties();
            byte[] messageBytes = getMessageBytes();

            for (int idx = 0; idx < loop; idx++) {
                // try to force jms semantics.
                // but this does not work since RabbitMQ does not sync to disk if consumers are connected as
                // seen by iostat -cd 1. TPS value remains at 0.

                channel.basicPublish(getExchange(), getMessageRoutingKey(), messageProperties, messageBytes);
            }

            // commit the sample
            if (getUseTx()) {
                channel.txCommit();
            }

            /*
             * Set up the sample result details
             */

            result.setSamplerData(data);
            result.setResponseData("OK", null);
            result.setDataType(SampleResult.TEXT);

            result.setRequestHeaders(formatHeaders());

            result.setResponseCodeOK();
            result.setResponseMessage("OK");
            result.setSuccessful(true);
        } catch (Exception ex) {
            log.warn(ex.getMessage(), ex);
            result.setResponseCode("000");
            result.setResponseMessage(ex.toString());
        } finally {
            result.sampleEnd();     // end timing
        }

        return result;
    }

    private byte[] getMessageBytes() {
        return getMessage().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @return the message routing key for the sample
     */
    public String getMessageRoutingKey() {
        return getPropertyAsString(MESSAGE_ROUTING_KEY);
    }

    public void setMessageRoutingKey(String content) {
        setProperty(MESSAGE_ROUTING_KEY, content);
    }

    /**
     * @return the message for the sample
     */
    public String getMessage() {
        return getPropertyAsString(MESSAGE);
    }

    public void setMessage(String content) {
        setProperty(MESSAGE, content);
    }

    /**
     * @return the message type for the sample
     */
    public String getMessageType() {
        return getPropertyAsString(MESSAGE_TYPE);
    }

    public void setMessageType(String content) {
        setProperty(MESSAGE_TYPE, content);
    }

    /**
     * @return the reply-to queue for the sample
     */
    public String getReplyToQueue() {
        return getPropertyAsString(REPLY_TO_QUEUE);
    }

    public void setReplyToQueue(String content) {
        setProperty(REPLY_TO_QUEUE, content);
    }

    public String getContentType() {
        return getPropertyAsString(CONTENT_TYPE);
    }

    public void setContentType(String contentType) {
        setProperty(CONTENT_TYPE, contentType);
    }

    public String getContentEncoding() {
        return getPropertyAsString(CONTENT_ENCODING);
    }

    public void setContentEncoding(String contentEncoding) {
        setProperty(CONTENT_ENCODING, contentEncoding);
    }

    /**
     * @return the correlation identifier for the sample
     */
    public String getCorrelationId() {
        return getPropertyAsString(CORRELATION_ID);
    }

    public void setCorrelationId(String content) {
        setProperty(CORRELATION_ID, content);
    }

    /**
     * @return the message id for the sample
     */
    public String getMessageId() {
        return getPropertyAsString(MESSAGE_ID);
    }

    public void setMessageId(String content) {
        setProperty(MESSAGE_ID, content);
    }

    /**
     * @return the message priority for the sample
     */
    public String getMessagePriority() {
        return getPropertyAsString(MESSAGE_PRIORITY);
    }

    public void setMessagePriority(String content) {
        setProperty(MESSAGE_PRIORITY, content);
    }

    protected int getMessagePriorityAsInt() {
        if (getPropertyAsInt(MESSAGE_PRIORITY) < 0) {
            return 0;
        }

        return getPropertyAsInt(MESSAGE_PRIORITY);
    }

    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

    public boolean getPersistent() {
        return getPropertyAsBoolean(PERSISTENT, DEFAULT_PERSISTENT);
    }

    public void setPersistent(Boolean persistent) {
        setProperty(PERSISTENT, persistent);
    }

    public boolean getUseTx() {
        return getPropertyAsBoolean(USE_TX, DEFAULT_USE_TX);
    }

    public void setUseTx(Boolean tx) {
        setProperty(USE_TX, tx);
    }

    @Override
    public boolean interrupt() {
        cleanup();
        return true;
    }

    @Override
    protected Channel getChannel() {
        return channel;
    }

    @Override
    protected void setChannel(Channel channel) {
        this.channel = channel;
    }

    protected AMQP.BasicProperties getProperties() {
        final AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        final int deliveryMode = getPersistent() ? 2 : 1;
        final String contentType = StringUtils.defaultIfEmpty(getContentType(), DEFAULT_CONTENT_TYPE);

        builder.contentType(contentType)
            .contentEncoding(getContentEncoding())
            .deliveryMode(deliveryMode)
            .correlationId(getCorrelationId())
            .replyTo(getReplyToQueue())
            .type(getMessageType())
            .headers(prepareHeaders());

        if (getMessageId() != null && !getMessageId().isEmpty()) {
            builder.messageId(getMessageId());
        }

        if (getMessagePriority() != null && !getMessagePriority().isEmpty()) {
            builder.priority(getMessagePriorityAsInt());
        } else {
            builder.priority(DEFAULT_MESSAGE_PRIORITY);
        }

        return builder.build();
    }

    @Override
    protected boolean initChannel() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        boolean ret = super.initChannel();

        if (getUseTx()) {
            channel.txSelect();
        }

        return ret;
    }

    private Map<String, Object> prepareHeaders() {
        Arguments headers = getHeaders();

        if (headers != null) {
            final Map<String, Object> preparedHeaders = new HashMap<>();
            for (final JMeterProperty property : headers) {
                final Argument arg = (Argument) property.getObjectValue();
                final String type = arg.getDescription();
                final Object value = StringUtils.isBlank(type) ? arg.getValue() : convert(type, arg.getValue());
                preparedHeaders.put(arg.getName(), value);
            }

            return preparedHeaders;
        }

        return Collections.emptyMap();
    }

    private String formatHeaders() {
        Map<String, String> headers = getHeaders().getArgumentsAsMap();
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String,String> entry : headers.entrySet()) {
            sb.append(entry.getKey())
                .append(": ")
                .append(entry.getValue())
                .append("\n");
        }

        return sb.toString();
    }

    private static Object convert(final String type, final String value) {
        final Function<String, Object> converter = CONVERTERS.get(type);

        if (converter == null) {
            log.error("No converter found for type '{}'", type);
            throw new NullPointerException("No converter found for type " + type);
        }

        return converter.apply(value);
    }

    private static Map<String, Function<String, Object>> getConverters() {
        final Map<String, Function<String, Object>> converters = new HashMap<>();
        converters.put("integer", Integer::valueOf);
        converters.put("date", AMQPPublisher::convertDate);
        converters.put("string", v -> v);
        return converters;
    }

    private static Date convertDate(final String dateString) {
        final Date date;

        if (StringUtils.isNumeric(dateString)) {
            date = new Date(Long.parseLong(dateString));
        } else {
            try {
                date = new SimpleDateFormat().parse(dateString);
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }

        return date;
    }
}
