package com.railwaygames.solarsmash.http;

import java.io.IOException;
import java.util.Map;

import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSMutableURLRequest;
import org.robovm.apple.foundation.NSOperationQueue;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.foundation.NSURLConnection;
import org.robovm.apple.foundation.NSURLResponse;
import org.robovm.objc.block.VoidBlock3;

public class Connection {
	private static final int READ_TIMEOUT = 20;

	public static void establishGetConnection(VoidBlock3<NSURLResponse, NSData, NSError> completionHandler,
			String protocol, String host, String port, String path, Map<String, String> args) throws IOException {
		StringBuilder sb = new StringBuilder("?");

		for (Map.Entry<String, String> arg : args.entrySet()) {
			sb.append(arg.getKey()).append("=").append(arg.getValue()).append("&");
		}

		NSMutableURLRequest request = new NSMutableURLRequest();
		request.setURL(new NSURL(protocol + "://" + host + ":" + port + path + sb.toString()));
		request.setHTTPMethod("GET");
		request.setTimeoutInterval(READ_TIMEOUT);

		NSURLConnection.sendAsynchronousRequest(request, NSOperationQueue.getMainQueue(), completionHandler);
	}

	public static void establishPostConnection(VoidBlock3<NSURLResponse, NSData, NSError> completionHandler,
			String protocol, String host, String port, String path, String... params) throws IOException {

		byte[] bytes = params[0].getBytes("UTF-8");
		NSData data = new NSData(bytes);

		NSMutableURLRequest request = new NSMutableURLRequest();
		request.setURL(new NSURL(protocol + "://" + host + ":" + port + path));
		request.setHTTPMethod("POST");
		request.setHTTPBody(data);
		request.setTimeoutInterval(READ_TIMEOUT);
		request.setValue$forHTTPHeaderField$("application/json", "Content-Type");
		request.setValue$forHTTPHeaderField$("application/json", "Accept");

		NSURLConnection.sendAsynchronousRequest(request, NSOperationQueue.getMainQueue(), completionHandler);
	}
}
