package com.siki.cashcount.helper;

import com.siki.cashcount.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class DebugWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugWriter.class);

    private final boolean enabled;
    private StringBuilder contentBuilder;

    public DebugWriter() {
        enabled = ConfigManager.getBooleanProperty("ExportDataForDebug");
        if (enabled) {
            contentBuilder = new StringBuilder();
        }
    }

    public void insertLine(String... items) {
        if (enabled) {
            for (int i = 0; i < items.length; i++) {
                if (i != 0) {
                    contentBuilder.append(";");
                }

                contentBuilder.append(items[i]);
            }

            contentBuilder.append("\n");
        }
    }

    public void writeToFile(String filename) {
        if (enabled) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("debug/" + filename), "UTF-8"))) {
                bw.write(contentBuilder.toString());
            } catch (IOException ex) {
                LOGGER.error("", ex);
            }
        }
    }
}
