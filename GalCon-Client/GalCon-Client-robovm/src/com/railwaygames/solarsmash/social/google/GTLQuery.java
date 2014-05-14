package com.railwaygames.solarsmash.social.google;

import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCClass;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.rt.bro.annotation.Library;

@Library(Library.INTERNAL)
@NativeClass()
public class GTLQuery extends NSObject implements GTLQueryProtocol {
	private static final ObjCClass objCClass = ObjCClass.getByType(GTLQuery.class);

	static {
		ObjCRuntime.bind(GTLQuery.class);
	}
}
