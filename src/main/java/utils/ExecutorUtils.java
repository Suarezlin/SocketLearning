package utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {

    public static void closeExecutor(ExecutorService executorService) throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(5000, TimeUnit.MILLISECONDS);
        executorService.shutdownNow();
    }

}
