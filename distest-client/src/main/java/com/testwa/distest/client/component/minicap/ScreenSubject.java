package com.testwa.distest.client.component.minicap;

public interface ScreenSubject {

	public void registerObserver(ScreenProjectionObserver o);

	public void removeObserver(ScreenProjectionObserver o);

	public void notifyObservers(byte[] image);

}
