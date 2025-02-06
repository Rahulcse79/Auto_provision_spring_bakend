package com.coraltele.autoprovisioning.background;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import com.coraltele.autoprovisioning.component.condition.EnableTR069;
import com.coraltele.autoprovisioning.component.service.DeviceManagerInfoService;
import com.coraltele.autoprovisioning.component.service.DeviceManagerService;
import com.coraltele.autoprovisioning.component.thread.AutoProvisionThread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

@Service
@Conditional(EnableTR069.class)
public class AutoProvisionMonitoringProcess {

    private final DeviceManagerService deviceManagerService;
    private final DeviceManagerInfoService deviceManagerInfoService;
    private static final Logger logger = LogManager.getLogger(AutoProvisionMonitoringProcess.class);
    private static final Integer MAX_THREADS = 1;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);
    private volatile Thread currentThread;

    @Autowired
    public AutoProvisionMonitoringProcess(DeviceManagerService deviceManagerService,
            DeviceManagerInfoService deviceManagerInfoService) {
        this.deviceManagerService = deviceManagerService;
        this.deviceManagerInfoService = deviceManagerInfoService;
    }

    @Scheduled(fixedRate = 20000)
    public void startNewThreadEvery20Seconds() {
        logger.info("Starting a new thread every 20 seconds");
        try {
            if (currentThread != null && currentThread.isAlive()) {
                logger.info("Interrupting old thread...");
                currentThread.interrupt();
                if (currentThread.isAlive()) {
                    logger.warn("Old thread did not terminate gracefully, forcefully exiting.");
                    currentThread.join();
                }
                logger.info("Old thread stopped successfully.");
            }
            currentThread = new Thread(new AutoProvisionThread(deviceManagerService, deviceManagerInfoService));
            threadPool.execute(currentThread);
            logger.info("New thread started successfully.");

        } catch (InterruptedException e) {
            logger.error("Thread scheduling was interrupted: ", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("An error occurred while starting the new thread: ", e);
        }
    }

    public void shutdown() {
        try {
            logger.info("Shutting down thread pool...");
            threadPool.shutdown();
            if (currentThread != null && currentThread.isAlive()) {
                logger.info("Interrupting active thread...");
                currentThread.interrupt();
                currentThread.join();
                logger.info("Current thread interrupted and completed.");
            }
        } catch (InterruptedException e) {
            logger.error("Shutdown interrupted: ", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Error during shutdown: ", e);
        }
    }
}