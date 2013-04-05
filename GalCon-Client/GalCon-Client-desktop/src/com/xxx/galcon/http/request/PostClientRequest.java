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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

public class PostClientRequest implements ClientRequest {

	@Override
	public HttpRequestBase createHttpBaseRequest(URIBuilder builder,
			Map<String, String> parameters) throws URISyntaxException {
		
		HttpRequestBase request = new HttpPost(builder.build());
		StringEntity params = setJsonAsContentType(parameters);
		
		
		((HttpPost) request).setEntity(params);
		
		return request;
	}

	private StringEntity setJsonAsContentType(Map<String, String> parameters) {
		StringEntity params = null;
		try {
			params = new StringEntity(parameters.get("json"));
			params.setContentType(ContentType.APPLICATION_JSON.toString());

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return params;
		
	}

}
