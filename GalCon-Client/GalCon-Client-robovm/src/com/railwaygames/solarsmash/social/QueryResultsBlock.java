package com.railwaygames.solarsmash.social;

import org.robovm.apple.foundation.NSError;
import org.robovm.objc.ObjCBlock;
import org.robovm.objc.ObjCBlock.Wrapper;
import org.robovm.rt.bro.annotation.Callback;

import com.railwaygames.solarsmash.social.google.GTLPlusPeopleFeed;
import com.railwaygames.solarsmash.social.google.GTLServiceTicket;

public interface QueryResultsBlock {

	void invoke(GTLServiceTicket ticket, GTLPlusPeopleFeed peopleFeed, NSError error);

	static class Callbacks {
		@Callback
		static void run(ObjCBlock block, GTLServiceTicket ticket, GTLPlusPeopleFeed peopleFeed, NSError error) {
			((QueryResultsBlock) block.object()).invoke(ticket, peopleFeed, error);
		}
	}

	static class Marshaler {
		private static final Wrapper WRAPPER = new Wrapper(Callbacks.class);

		public static ObjCBlock toObjCBlock(QueryResultsBlock o) {
			return WRAPPER.toObjCBlock(o);
		}
	}
}
