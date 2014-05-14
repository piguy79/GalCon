package com.railwaygames.solarsmash.social.google;

import org.robovm.apple.foundation.NSArray;
import org.robovm.objc.ObjCClass;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.objc.annotation.Property;
import org.robovm.rt.bro.annotation.Library;

@Library(Library.INTERNAL)
@NativeClass()
public class GTLPlusPeopleFeed extends GTLCollectionObject {
	private static final ObjCClass objCClass = ObjCClass.getByType(GTLPlusPeopleFeed.class);

	static {
		ObjCRuntime.bind(GTLPlusPeopleFeed.class);
	}

	@Property(selector = "setItems:", strongRef = true)
	public native void setItems(NSArray<?> items);
}
