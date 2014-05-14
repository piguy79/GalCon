package com.railwaygames.solarsmash.social.google;

import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCClass;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.rt.bro.annotation.Bridge;
import org.robovm.rt.bro.annotation.Library;

@Library(Library.INTERNAL)
@NativeClass()
public class GTLCollectionObject extends NSObject {
	private static final ObjCClass objCClass = ObjCClass.getByType(GTLCollectionObject.class);

	static {
		ObjCRuntime.bind(GTLCollectionObject.class);
	}

	private static final Selector items = Selector.register("items");

	@Bridge
	private native static NSArray<?> objc_getItems(GTLCollectionObject __self__, Selector __cmd__);

	public NSArray<?> getItems() {
		return objc_getItems(this, items);
	}
}
