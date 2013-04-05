package com.xxx.galcon.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

public class PostClientRequest implements ClientRequest {

	@Override
	public HttpRequestBase createHttpBaseRequest(URIBuilder builder,
			Map<String, String> parameters) throws URISyntaxException {
		
		HttpRequestBase request = new HttpPost(builder.build());

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		
		setUrlEncoding(request, nameValuePairs);
		
		
		return request;
	}

	private void setUrlEncoding(HttpRequestBase request,
			List<NameValuePair> nameValuePairs) {
		try {
			((HttpPost) request).setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}

}
