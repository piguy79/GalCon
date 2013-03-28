/**
 * 
 */
package com.xxx.galcon.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author conormullen
 * 
 */
public class BaseDesktopGameAction {

	private String host;
	private int port;

	public BaseDesktopGameAction(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * 
	 * This method is used to establish a HTTP POST connection to a given url.
	 * 
	 * @param url
	 * @param urlParameters
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected String executeHttpRequest(String path, Map<String, String> parameters) throws IOException,
			URISyntaxException {

		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(host).setPort(port).setPath(path);

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			builder.setParameter(entry.getKey(), entry.getValue());
		}

		HttpGet httpGet = new HttpGet(builder.build());

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httpGet);
		HttpEntity responseEntity = response.getEntity();
		if (responseEntity != null) {
			InputStream instream = responseEntity.getContent();

			try {
				return readConnectionData(instream);
			} finally {
				instream.close();
			}
		}

		return "";
	}

	/**
	 * This method is used to read the return value from a HttpUrlConnection.
	 * 
	 * @param connection
	 * @return
	 * @throws IOException
	 */
	private String readConnectionData(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		InputStreamReader input = null;
		try {
			input = new InputStreamReader(is);
			char[] buffer = new char[0x1000];
			int read = 0;
			while ((read = input.read(buffer, 0, buffer.length)) > 0) {
				sb.append(buffer, 0, read);
			}
		} finally {
			if (input != null) {
				input.close();
			}
		}

		return sb.toString();
	}
}
