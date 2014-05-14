package com.railwaygames.solarsmash.social.google;

import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCBlock;
import org.robovm.objc.ObjCClass;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.rt.bro.annotation.Bridge;
import org.robovm.rt.bro.annotation.Library;

@Library(Library.INTERNAL)
@NativeClass()
public class GTLService extends NSObject {
	private static final ObjCClass objCClass = ObjCClass.getByType(GTLService.class);

	static {
		ObjCRuntime.bind(GTLService.class);
	}

	private static final Selector executeQuery = Selector.register("executeQuery:completionHandler:");

	@Bridge
	private native static GTLServiceTicket objc_executeQuery(GTLService __self__, Selector __cmd__,
			GTLQueryProtocol query, ObjCBlock block);

	public GTLServiceTicket executeQuery(GTLQueryProtocol query, ObjCBlock block) {
		return objc_executeQuery(this, executeQuery, query, block);
	}

}
