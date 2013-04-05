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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import com.xxx.galcon.http.request.ClientRequest;

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
	 */
	protected String executeHttpRequest(ClientRequest clientRequest, String path, Map<String, String> parameters)
			throws IOException, URISyntaxException {

		HttpRequestBase request = createTheBaseHttpRequest(clientRequest, path, parameters);

		HttpResponse response = executeResponseOnClient(request);
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

	private HttpResponse executeResponseOnClient(HttpRequestBase request) throws IOException, ClientProtocolException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(request);
		return response;
	}

	private HttpRequestBase createTheBaseHttpRequest(ClientRequest clientRequest, String path,
			Map<String, String> parameters) throws URISyntaxException {
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(host).setPort(port).setPath(path);

		HttpRequestBase request = clientRequest.createHttpBaseRequest(builder, parameters);
		return request;
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
