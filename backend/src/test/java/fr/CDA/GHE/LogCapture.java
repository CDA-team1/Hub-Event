package fr.CDA.GHE;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Aide de test : récupère ce qu'un journal a écrit (message complet, y compris la pile d'une
 * exception) pour vérifier qu'aucune donnée sensible n'y figure (SEC-14).
 */
public final class LogCapture implements AutoCloseable {

    private final Logger logger;
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    /** @param loggerName nom du journal à écouter, {@link Logger#ROOT_LOGGER_NAME} pour tous */
    public LogCapture(String loggerName) {
        this.logger = (Logger) LoggerFactory.getLogger(loggerName);
        appender.start();
        logger.addAppender(appender);
    }

    /** Tout ce qui a été écrit, une entrée par événement, pile d'exception comprise. */
    public List<String> lines() {
        return appender.list.stream()
                .map(event -> event.getFormattedMessage()
                        + (event.getThrowableProxy() == null ? "" : "\n" + ThrowableProxyUtil.asString(event.getThrowableProxy())))
                .collect(Collectors.toList());
    }

    public String all() {
        return String.join("\n", lines());
    }

    @Override
    public void close() {
        logger.detachAppender(appender);
        appender.stop();
    }
}
