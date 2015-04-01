package com.cherokeelessons.cards;

import java.util.ArrayList;
import java.util.List;

public class ActiveDeck {
	private String signature="";
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public long lastrun=0;
	public List<ActiveCard> deck=new ArrayList<ActiveCard>();
}
