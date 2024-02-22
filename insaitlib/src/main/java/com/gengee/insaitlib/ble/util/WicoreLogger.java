package com.gengee.insaitlib.ble.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 日志类
 *
 */
public class WicoreLogger {
	protected Logger logger = Logger.getLogger(getClass().getName());

	public WicoreLogger() {
		File logFile = new File(Environment.getExternalStorageDirectory(), "wicore.log");
		try {
			FileHandler handler = new FileHandler(logFile.getAbsolutePath(), true);
			handler.setEncoding("utf-8");
			handler.setFormatter(new TestLogFormatter());
			// handler.setFormatter(new SimpleFormatter());
			logger.addHandler(handler);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class TestLogFormatter extends Formatter {
		SimpleDateFormat mFormatter = new SimpleDateFormat("HH:mm:ss MM-dd-yyyy ", Locale.CHINESE);

		@Override
		public String format(LogRecord r) {
			StringBuilder builder = new StringBuilder(mFormatter.format(r.getMillis()));
			builder.append(r.getSourceClassName()).append(" ").append(r.getSourceMethodName())
					.append("\r\n").append(r.getLevel()).append("：" + r.getMessage())
					.append("\r\n").append("\r\n");
			return builder.toString();
		}

	}

}
