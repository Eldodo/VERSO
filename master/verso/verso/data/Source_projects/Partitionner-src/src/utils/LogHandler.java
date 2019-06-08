package utils;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import ui.Ui;

public class LogHandler extends Handler {
	Formatter formatter;
	private static LogHandler instance = null;
	public static synchronized LogHandler getInstance(){
		if(instance == null)
			instance = new LogHandler();
		return instance;
	}
	
	private LogHandler() {
	    formatter = new BasicFormatter();
	    setFormatter(formatter);
	}
	@Override
	public synchronized void publish(LogRecord record) {
		String message = null;
	    if (!isLoggable(record))
	      return;
	   
	    message = getFormatter().format(record);
	    Ui.getInstance().log(message);
	}
	
	@Override
	public void flush() {
	}
	
	@Override
	public void close() throws SecurityException {
	}
}
