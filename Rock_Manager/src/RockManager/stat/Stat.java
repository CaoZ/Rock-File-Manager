
package RockManager.stat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import javax.microedition.io.Connection;
import javax.microedition.io.HttpConnection;
import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Ui;
import RockManager.config.Config;
import RockManager.ui.MyUI;


public class Stat {

	public Stat() {

		Thread statThread = createStatThread();
		statThread.start();

	}


	private Thread createStatThread() {

		Thread statThread = new Thread() {

			public void run() {

				int delayTimeSecond = 10 + new Random().nextInt(20);
				long delayTime = delayTimeSecond * 1000;

				if (Config.DEBUG_MODE) {
					delayTime = 0;
				}

				try {
					Thread.sleep(delayTime);
				} catch (InterruptedException e) {
				}
				try {
					doStat();
				} catch (IOException e) {
				}

			}

		};

		return statThread;

	}


	private void doStat() throws IOException {

		Hashtable result = collect();

		String url = buildURL();

		HttpConnection httpConn = buildConnection(url);

		try {
			sendResult(httpConn, result);
		} catch (Exception e) {
		}

		try {
			httpConn.close();
		} catch (Exception e) {
		}

	}


	private void sendResult(HttpConnection httpConn, Hashtable result) throws IOException {

		if (httpConn == null) {
			return;
		}

		httpConn.setRequestMethod(HttpConnection.POST);

		URLEncodedPostData data = new URLEncodedPostData("UTF-8", false);

		Enumeration names = result.keys();
		Enumeration values = result.elements();

		while (names.hasMoreElements()) {

			String thisName = (String) names.nextElement();
			String thisValue = (String) values.nextElement();
			data.append(thisName, thisValue);

		}

		OutputStream os = httpConn.openOutputStream();
		os.write(data.getBytes());
		os.close();

		httpConn.getResponseCode();

	}


	private String buildURL() {

		String dot = ".";

		StringBuffer sb = new StringBuffer();

		sb.append("ht").append("tp").append("://");

		if (Config.DEBUG_MODE) {
			sb.append(127).append(dot).append(0).append(dot).append(0).append(dot).append(1);
			sb.append(":").append(9090);
		} else {
			sb.append("stat").append(dot).append("rock-it.org");
		}

		sb.append("/");
		sb.append("stat");

		sb.append(";").append("device").append("side").append("=").append("true");

		return sb.toString();

	}


	private HttpConnection buildConnection(String url) {

		ConnectionFactory connFactory = new ConnectionFactory();
		ConnectionDescriptor connDes = connFactory.getConnection(url);

		if (connDes != null) {
			Connection conn = connDes.getConnection();
			if (conn instanceof HttpConnection) {
				return (HttpConnection) conn;
			}
		}
		return null;

	}


	private Hashtable collect() {

		Hashtable table = new Hashtable();

		String deviceModel = DeviceInfo.getDeviceName();
		table.put("DEV_MOD", deviceModel);

		String osVersion = DeviceInfo.getSoftwareVersion();
		table.put("OS_VER", osVersion);

		String manufacturerName = DeviceInfo.getManufacturerName();
		table.put("MAN_NAM", manufacturerName);

		String pin = Integer.toString(DeviceInfo.getDeviceId());
		table.put("PIN", pin);

		boolean isSimulator = DeviceInfo.isSimulator();
		table.put("IS_SIM", String.valueOf(isSimulator));

		String locale = Locale.getDefaultForSystem().toString();
		table.put("LOC", locale);

		String inputLocale = Locale.getDefaultInputForSystem().toString();
		table.put("INP_LOC", inputLocale);

		int systemFontHeight = MyUI.SYSTEM_FONT.getHeight(Ui.UNITS_pt);
		table.put("SYS_F_H", Integer.toString(systemFontHeight));

		String appName = Config.APP_NAME;
		table.put("APP_NAM", appName);

		String appVersion = Config.VERSION_NUMBER;
		table.put("APP_VER", appVersion);

		int statVersion = 1;
		table.put("STA_VER", Integer.toString(statVersion));

		return table;

	}

}
