package com.cherokeelessons.cards;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Card implements Serializable, Comparable<Card> {
	public int id;
	public String challenge;
	public String answer;

	public Card() {
	}

	public Card(Card card) {
		this.answer = card.answer.intern();
		this.challenge = card.challenge.intern();
		this.id = card.id;
	}

	@Override
	public int compareTo(Card o) {
		return sortKey().compareTo(o.sortKey());
	}

	private String sortKey() {
		StringBuilder key = new StringBuilder();
		key.append(answer);
		key.append("+");
		key.append(challenge);
		return key.toString();
	}
}
