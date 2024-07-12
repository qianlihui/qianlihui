package com.qlh.base;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(chain = true)
@Data
public class QlhException extends RuntimeException {

    private int code;
    private String msg;

    public QlhException() {

    }

    public QlhException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public QlhException(int code, String msg, Exception e) {
        super(e);
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getMessage() {
        return String.format("%s: %s", code, msg);
    }

    public QlhException clone(String msg) {
        return new QlhException().setCode(this.code).setMsg(msg);
    }

    public static <T> T runtime(Runnable runnable) {
        try {
            return (T) runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void runtime(RunnableNoReturn runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void ignore(RunnableNoReturn runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static <T> T ignore(Runnable runnable) {
        try {
            return (T) runnable.run();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public interface Runnable<T> {
        T run() throws Exception;
    }

    public interface RunnableNoReturn {
        void run() throws Exception;
    }

}
