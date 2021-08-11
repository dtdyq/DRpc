package dyq.rpc.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.netty.buffer.ByteBuf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class CommonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

    public static String getUTF8(ByteBuf byteBuf) {
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        return new String(data, StandardCharsets.UTF_8);
    }

    public static ExecutorService getNormalExecutor(String name) {
        int coreSize = Runtime.getRuntime().availableProcessors();
        return getExec(name, coreSize / 2, coreSize);
    }

    public static ExecutorService getSuperExecutor(String name) {
        int coreSize = Runtime.getRuntime().availableProcessors();
        return getExec(name, coreSize, coreSize * 2);
    }

    private static ExecutorService getExec(String name, int size, int maxSize) {
        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(name + "-thread-%s")
            .setUncaughtExceptionHandler((t, e) -> LOGGER.error("exec {} get unexpected exception:", t.getName(), e))
            .build();
        return new ThreadPoolExecutor(size, maxSize, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), factory,
            (r, executor) -> {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    LOGGER.error("executor unexpected exception catched:", e);
                }
            });
    }

    public static BiConsumer<Void, Throwable> completableCompleteConsume(String msg) {
        return (a, throwable) -> LOGGER.info(msg + ":{},{}", a, throwable);
    }

    public static String genServiceIde(Class<?> clz, String tag) {
        return clz.getCanonicalName() + "#" + tag;
    }
}
