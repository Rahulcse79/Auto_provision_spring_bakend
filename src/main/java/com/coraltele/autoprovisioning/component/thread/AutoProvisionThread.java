package com.coraltele.autoprovisioning.component.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.coraltele.autoprovisioning.component.helper.Constants;
import com.coraltele.autoprovisioning.component.service.DeviceManagerInfoService;
import com.coraltele.autoprovisioning.component.service.DeviceManagerService;

public class AutoProvisionThread implements Runnable {

    private static final Logger logger = LogManager.getLogger(AutoProvisionThread.class);
    private volatile boolean isRunning = true;
    private final DeviceManagerService deviceManagerService;
    private final DeviceManagerInfoService deviceManagerInfoService;
    private static final int SLEEP_INTERVAL = 20000; 
    private static final int ONE_HOUR_MS = 3600000; 
    private static final int TWENTY_FOUR_HOUR_MS = 24 * 60 * 60 * 1000;

    private long lastLoginCallTime = 0; 
    private long lastSyncConfigTime = 0; 

    public AutoProvisionThread(DeviceManagerService deviceManagerService,
                               DeviceManagerInfoService deviceManagerInfoService) {
        this.deviceManagerService = deviceManagerService;
        this.deviceManagerInfoService = deviceManagerInfoService;
    }

    private void runDeleteBackup() {
        try {
            if (!Constants.DEVICE_MANAGER_IP.isEmpty()) {
                logger.info("Starting backup deletion and upload process.");
                deviceManagerService.deleteBackup();
                deviceManagerService.automaticallyUploadAndUpdateStart();
                String token = deviceManagerService.getAcsGlobelToken();
                deviceManagerInfoService.getAllDevices(token);
                logger.info("Backup deletion and upload completed.");
            } else {
                logger.warn("Device manager IP is empty, skipping backup operation.");
            }
        } catch (Exception e) {
            logger.error("Error during backup operation: {}", e.getMessage(), e);
        }
    }

    private void runACSLoginCallIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLoginCallTime >= ONE_HOUR_MS) {
            try {
                logger.info("Running ACSLoginCall.");
                deviceManagerService.ACSLoginCall();
                lastLoginCallTime = currentTime; 
            } catch (Exception e) {
                logger.error("Error during ACSLoginCall: {}", e.getMessage(), e);
            }
        }
    }

    private void runSyncConfig() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSyncConfigTime >= TWENTY_FOUR_HOUR_MS) {
            try {
                logger.info("Running sync config.");
                deviceManagerInfoService.SyncConfigAutoRunable();
                lastSyncConfigTime = currentTime; 
            } catch (Exception e) {
                logger.error("Error during sync config: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                runDeleteBackup();
                runACSLoginCallIfNeeded();
                runSyncConfig();
                try {
                    Thread.sleep(SLEEP_INTERVAL);
                } catch (InterruptedException e) {
                    logger.warn("Thread sleep interrupted. Continuing to run the task.");
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error in auto provision thread: {}", e.getMessage(), e);
        } finally {
            logger.info("Auto Provisioning Thread has completed its task.");
        }
    }

    public void stopThread() {
        logger.info("Stopping the auto provision thread.");
        isRunning = false;
    }
}
