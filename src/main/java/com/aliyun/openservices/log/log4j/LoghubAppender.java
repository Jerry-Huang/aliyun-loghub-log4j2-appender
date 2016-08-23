package com.aliyun.openservices.log.log4j;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.producer.LogProducer;
import com.aliyun.openservices.log.producer.ProducerConfig;
import com.aliyun.openservices.log.producer.ProjectConfig;

@Plugin(name = "Loghub", category = "Core", elementType = "appender", printObject = true)
public final class LoghubAppender extends AbstractAppender {

    private String topic;
    private String logstore;
    private DateFormat dateFormat;
    private LogProducer logProducer;
    private ProjectConfig projectConfig = new ProjectConfig();
    private ProducerConfig producerConfig = new ProducerConfig();

    private LoghubAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, ProjectConfig projectConfig, ProducerConfig producerConfig,
            String topic, String logstore, DateFormat dateFormat) {
        super(name, filter, layout, ignoreExceptions);

        this.producerConfig = producerConfig;
        this.projectConfig = projectConfig;

        this.topic = topic == null ? "" : topic;
        this.logstore = logstore;
        this.dateFormat = dateFormat;
    }

    @Override
    public void append(LogEvent event) {

        List<LogItem> logItems = new ArrayList<>();

        LogItem item = new LogItem();
        logItems.add(item);

        item.SetTime((int) (event.getTimeMillis() / 1000L));
        item.PushBack("time", dateFormat.format(new Date(event.getTimeMillis())));
        item.PushBack("level", event.getLevel().toString());
        item.PushBack("thread", event.getThreadName());
        item.PushBack("location", event.getLoggerName());
        item.PushBack("message", event.getMessage().getFormattedMessage());

        if (event.getThrown() != null) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw, true);
            event.getThrown().printStackTrace(pw);
            item.PushBack("thrown", sw.getBuffer().toString());
        }

        Map<String, String> properties = event.getContextMap();
        if (properties.size() > 0) {
            Object[] keys = properties.keySet().toArray();
            java.util.Arrays.sort(keys);
            for (int i = 0; i < keys.length; i++) {
                item.PushBack(keys[i].toString(), properties.get(keys[i]).toString());
            }
        }

        logProducer.send(projectConfig.projectName, logstore, topic, null, logItems);
    }

    @Override
    public void start() {

        logProducer = new LogProducer(producerConfig);
        logProducer.setProjectConfig(projectConfig);

        super.start();
    }

    @Override
    public void stop() {

        super.stop();

        logProducer.flush();
        logProducer.close();
    }

    @PluginFactory
    public static LoghubAppender createAppender(@PluginAttribute("name") final String name, @PluginAttribute("accessKey") final String accessKey,
            @PluginAttribute("accessKeyId") final String accessKeyId, @PluginAttribute("endpoint") final String endpoint, @PluginAttribute("logstore") final String logstore,
            @PluginAttribute("projectName") final String projectName, @PluginAttribute("ioThreadsCount") final int ioThreadsCount,
            @PluginAttribute("logsBytesPerPackage") final int logsBytesPerPackage, @PluginAttribute("logsCountPerPackage") final int logsCountPerPackage,
            @PluginAttribute("memPoolSizeInByte") final int memPoolSizeInByte, @PluginAttribute("packageTimeoutInMS") final int packageTimeoutInMS,
            @PluginAttribute("retryTimes") final int retryTimes, @PluginAttribute("shardHashUpdateIntervalInMS") final int shardHashUpdateIntervalInMS,
            @PluginAttribute("stsToken") final String stsToken, @PluginAttribute(value = "timeFormat", defaultString = "yyyy-MM-dd'T'HH:mmZ") final String timeFormat,
            @PluginAttribute(value = "timeZone", defaultString = "UTC") final String timeZone, @PluginAttribute("topic") final String topic,
            @PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filter") Filter filter,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions) {

        if (name == null) {
            LOGGER.error("No name provided for LoghubAppender");
            return null;
        }

        if (projectName == null) {
            LOGGER.error("No projectName provided for LoghubAppender");
            return null;
        }

        if (logstore == null) {
            LOGGER.error("No logstore provided for LoghubAppender");
            return null;
        }

        if (endpoint == null) {
            LOGGER.error("No endpoint provided for LoghubAppender");
            return null;
        }

        if (accessKeyId == null) {
            LOGGER.error("No accessKeyId provided for LoghubAppender");
            return null;
        }

        if (accessKey == null) {
            LOGGER.error("No accessKey provided for LoghubAppender");
            return null;
        }

        ProjectConfig projectConfig = new ProjectConfig();
        projectConfig.accessKey = accessKey;
        projectConfig.accessKeyId = accessKeyId;
        projectConfig.endpoint = endpoint;
        projectConfig.projectName = projectName;
        projectConfig.stsToken = stsToken;

        ProducerConfig producerConfig = new ProducerConfig();

        if (ioThreadsCount > 0) {
            producerConfig.ioThreadsCount = ioThreadsCount;
        }

        if (logsBytesPerPackage > 0) {
            producerConfig.logsBytesPerPackage = logsBytesPerPackage;
        }

        if (logsCountPerPackage > 0) {
            producerConfig.logsCountPerPackage = logsCountPerPackage;
        }

        if (memPoolSizeInByte > 0) {
            producerConfig.memPoolSizeInByte = memPoolSizeInByte;
        }

        if (packageTimeoutInMS > 0) {
            producerConfig.packageTimeoutInMS = packageTimeoutInMS;
        }

        if (retryTimes > 0) {
            producerConfig.retryTimes = retryTimes;
        }

        if (shardHashUpdateIntervalInMS > 0) {
            producerConfig.shardHashUpdateIntervalInMS = shardHashUpdateIntervalInMS;
        }

        DateFormat dateFormat = null;

        if (timeFormat != null) {
            dateFormat = new SimpleDateFormat(timeFormat);
            if (timeZone != null) {
                dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
            }
        }

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new LoghubAppender(name, filter, layout, ignoreExceptions, projectConfig, producerConfig, topic, logstore, dateFormat);
    }

}
