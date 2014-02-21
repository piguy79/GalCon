package com.xxx.galcon.views;

import org.robovm.cocoatouch.uikit.UIKeyInput;
import org.robovm.cocoatouch.uikit.UIKeyboardAppearance;
import org.robovm.cocoatouch.uikit.UIKeyboardType;
import org.robovm.cocoatouch.uikit.UIReturnKeyType;
import org.robovm.cocoatouch.uikit.UITextAutocapitalizationType;
import org.robovm.cocoatouch.uikit.UITextAutocorrectionType;
import org.robovm.cocoatouch.uikit.UITextSpellCheckingType;
import org.robovm.cocoatouch.uikit.UIView;
import org.robovm.objc.ObjCRuntime;

public class KeyBoardView extends UIView implements UIKeyInput {

	static {
		ObjCRuntime.bind();
	}

	@Override
	public UITextAutocapitalizationType getAutocapitalizationType() {
		return UITextAutocapitalizationType.None;
	}

	@Override
	public void setAutocapitalizationType(UITextAutocapitalizationType v) {

	}

	@Override
	public UITextAutocorrectionType getAutocorrectionType() {
		return UITextAutocorrectionType.No;
	}

	@Override
	public void setAutocorrectionType(UITextAutocorrectionType v) {

	}

	@Override
	public boolean isEnablesReturnKeyAutomatically() {
		return true;
	}

	@Override
	public void setEnablesReturnKeyAutomatically(boolean v) {

	}

	@Override
	public UIKeyboardAppearance getKeyboardAppearance() {
		return UIKeyboardAppearance.Default;
	}

	@Override
	public void setKeyboardAppearance(UIKeyboardAppearance v) {

	}

	@Override
	public UIKeyboardType getKeyboardType() {
		return UIKeyboardType.Alphabet;
	}

	@Override
	public void setKeyboardType(UIKeyboardType v) {

	}

	@Override
	public UIReturnKeyType getReturnKeyType() {
		return UIReturnKeyType.Go;
	}

	@Override
	public void setReturnKeyType(UIReturnKeyType v) {

	}

	@Override
	public boolean isSecureTextEntry() {
		return false;
	}

	@Override
	public void setSecureTextEntry(boolean v) {

	}

	@Override
	public UITextSpellCheckingType getSpellCheckingType() {
		return UITextSpellCheckingType.No;
	}

	@Override
	public void setSpellCheckingType(UITextSpellCheckingType v) {

	}

	@Override
	public void deleteBackward() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasText() {
		return true;
	}

	@Override
	public void insertText(String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canBecomeFirstResponder() {
		return true;
	}
}
