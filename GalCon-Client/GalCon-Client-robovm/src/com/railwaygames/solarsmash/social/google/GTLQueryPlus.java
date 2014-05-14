package com.railwaygames.solarsmash.social.google;

import org.robovm.apple.foundation.NSString;
import org.robovm.objc.ObjCClass;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.objc.annotation.Property;
import org.robovm.rt.bro.annotation.Bridge;
import org.robovm.rt.bro.annotation.Library;

@Library(Library.INTERNAL)
@NativeClass()
public class GTLQueryPlus extends GTLQuery {
	private static final ObjCClass objCClass = ObjCClass.getByType(GTLQueryPlus.class);

	private static final Selector queryForPeopleListWithUserId$userId$collection$ = Selector
			.register("queryForPeopleListWithUserId:collection:");

	@Bridge
	private native static GTLQueryPlus objc_queryForPeopleListWithUserId(ObjCClass __self__, Selector __cmd__,
			NSString userId, NSString collection);

	public static GTLQueryPlus queryForPeopleListWithUserId(String userId, String collection) {
		return objc_queryForPeopleListWithUserId(objCClass, queryForPeopleListWithUserId$userId$collection$,
				new NSString(userId), new NSString(collection));
	}

	@Property
	public native void setMaxResults(int maxResults);
}
