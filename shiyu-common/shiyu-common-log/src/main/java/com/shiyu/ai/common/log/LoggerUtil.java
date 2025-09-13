package com.shiyu.ai.common.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

@Getter
@AllArgsConstructor
public enum LoggerUtil {
    COMMON_LOGGER(LoggerFactory.getLogger("COMMON_LOGGER")),
    WEB_LOGGER(LoggerFactory.getLogger("WEB_LOGGER")),
    ;

    private final Logger normalLogger;

    public void info(String message){
        normalLogger.info(message);
    }
    public void info(String message,Object... objects){
        normalLogger.info(msgHandle(message,objects));
    }
    public void warn(String message){
        normalLogger.warn(message);
    }
    public void warn(String message,Object... objects){
        normalLogger.warn(msgHandle(message,objects));
    }
    public void error(String message){
        normalLogger.error(message);
    }
    public void error(String message,Object... objects){
        Throwable throwable = getThrowable(objects);
        normalLogger.error(msgHandle(message,objects),throwable);
    }
    public void error(Throwable throwable,String message,Object... objects){
        normalLogger.error(msgHandle(message,objects),throwable);
    }

    private Throwable getThrowable(Object[] objects) {
        Throwable throwable = null;
        for (Object object : objects) {
            if (object instanceof Throwable){
                throwable = (Throwable) object;
            }
        }
        return throwable;
    }


    private String msgHandle(String message, Object[] objects) {
        return MessageFormat.format(message, objects);
    }

}

