package com.railwaygames.solarsmash.social.google;

import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCClass;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.rt.bro.annotation.Library;

@NativeClass()
@Library(Library.INTERNAL)
public class GTLServiceTicket extends NSObject {
	private static final ObjCClass objCClass = ObjCClass.getByType(GTLServiceTicket.class);

	static {
		ObjCRuntime.bind(GTLServiceTicket.class);
	}
}
