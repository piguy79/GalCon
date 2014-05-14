package com.railwaygames.solarsmash.social.google;

import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCClass;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.objc.annotation.Property;
import org.robovm.rt.bro.annotation.Library;

@Library(Library.INTERNAL)
@NativeClass()
public class GTLPlusPerson extends NSObject {
	private static final ObjCClass objCClass = ObjCClass.getByType(GTLPlusPerson.class);

	static {
		ObjCRuntime.bind(GTLPlusPerson.class);
	}

	@Property
	public native String getDisplayName();

	@Property
	public native String getIdentifier();
}
