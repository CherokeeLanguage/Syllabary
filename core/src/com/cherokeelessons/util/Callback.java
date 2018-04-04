package com.cherokeelessons.util;

public abstract class Callback<T> {
	public Callback() {
	}
	
	public Runnable with(final Exception e) {
		return new Runnable() {				
			@Override
			public void run() {
				Callback.this.error(e);
			}
		};
	}
	
	public Runnable with(final T data) {
		return new Runnable() {				
			@Override
			public void run() {
				Callback.this.success(data);
			}
		};
	}
	
	public Runnable withNull() {
		return new Runnable() {				
			@Override
			public void run() {
				Callback.this.success(null);
			}
		};
	}
	
	public void error(Exception exception) {
		exception.printStackTrace();
		System.err.flush();
	};
	
	public abstract void success(T result);
}