package com.railwaygames.solarsmash.social.google;

import org.robovm.objc.ObjCClass;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.rt.bro.annotation.Library;

@Library(Library.INTERNAL)
@NativeClass()
public class GTLServicePlus extends GTLService {
	private static final ObjCClass objCClass = ObjCClass.getByType(GTLServicePlus.class);

	static {
		ObjCRuntime.bind(GTLServicePlus.class);
	}
}
