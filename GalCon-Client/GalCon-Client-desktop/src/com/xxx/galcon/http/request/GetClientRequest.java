package com.xxx.galcon.http.request;

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

public class GetClientRequest implements ClientRequest {

	@Override
	public HttpRequestBase createHttpBaseRequest(URIBuilder builder,
			Map<String, String> parameters) throws URISyntaxException {
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			builder.setParameter(entry.getKey(), entry.getValue());
		}

		return new HttpGet(builder.build());
	}

}
