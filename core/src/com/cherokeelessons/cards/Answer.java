package com.cherokeelessons.cards;

import java.util.ArrayList;
import java.util.List;

public class Answer {
	public static class AnswerList {
		public List<Answer> list = new ArrayList<Answer>(8);
		public AnswerList(AnswerList answerSetsFor) {
			for (Answer answer: answerSetsFor.list) {
				list.add(new Answer(answer));
			}
		}
		public AnswerList() {
		}
		public int correctCount(){
			int c=0;
			for (Answer a: list) {
				if (a.correct) {
					c++;
				}
			}
			return c;
		}
	}
	public Answer() {
	}

	public Answer(boolean correct, String answer, int distance) {
		super();
		this.correct = correct;
		this.answer = answer;
		this.distance = distance;
	}

	public Answer(Answer answer2) {
		this.answer=answer2.answer;
		this.correct=answer2.correct;
		this.distance=answer2.distance;
	}

	public int distance = 0;
	public boolean correct = false;
	public String answer = "";

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AnswerSet [distance=").append(distance)
				.append(", correct=").append(correct).append(", ");
		if (answer != null)
			builder.append("answer=").append(answer);
		builder.append("]");
		return builder.toString();
	}
}