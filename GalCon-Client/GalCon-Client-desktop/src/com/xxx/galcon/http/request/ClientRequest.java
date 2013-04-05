package com.xxx.galcon.http.request;

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

public interface ClientRequest {
	
	HttpRequestBase createHttpBaseRequest(URIBuilder builder, Map<String, String> parameters) throws URISyntaxException;

}
