package com.cherokeelessons.cards;

public enum LevelName {
	Newbie("Newbie", 0, "CgkIy7GTtc0TEAIQBg"), Novice("Novice", 1,
			"CgkIy7GTtc0TEAIQBw"), Rookie("Rookie", 2, "CgkIy7GTtc0TEAIQCA"), Beginner(
			"Beginner", 3, "CgkIy7GTtc0TEAIQCQ"), Apprentice("Apprentice",
			4, "CgkIy7GTtc0TEAIQCg"), Intermediate("Intermediate", 5,
			"CgkIy7GTtc0TEAIQCw"), Advanced("Advanced", 6,
			"CgkIy7GTtc0TEAIQDA"), Proficient("Proficient", 7,
			"CgkIy7GTtc0TEAIQDQ"), Expert("Expert", 8, "CgkIy7GTtc0TEAIQDg"), Master(
			"Master", 9, "CgkIy7GTtc0TEAIQDw"), GrandMaster("Grandmaster",
			10, "CgkIy7GTtc0TEAIQEA");
	
	public LevelName next() {
		LevelName[] values = LevelName.values();
		int ix=(ordinal()+1)%(values.length);
		return values[ix];
	}

	private final int level;
	private final String engrish;
	private final String id;

	public static LevelName getById(String id) {
		for (LevelName level : LevelName.values()) {
			if (level.id.equals(id)) {
				return level;
			}
		}
		return Newbie;
	}

	public static LevelName getNextById(String id) {
		return getById(id).next();
	}

	public static LevelName getNext(LevelName current) {
		return current.next();
	}

	public String getId() {
		return id;
	}

	public int getAchievementPoints() {
		return (level + 1) * 5;
	}

	private LevelName(String engrish, int level, String id) {
		this.engrish = engrish;
		this.level = level;
		this.id = id;
	}

	public static LevelName forLevel(int level_number) {
		LevelName level = Newbie;
		for (LevelName maybe : LevelName.values()) {
			if (maybe.level == level_number) {
				return maybe;
			}
			if (maybe.level > level.level && maybe.level < level_number) {
				level = maybe;
			}
		}
		return level;
	}

	public int getLevel() {
		return level;
	}

	public String getEngrish() {
		return engrish;
	}

	@Override
	public String toString() {
		return getEngrish();
	}

}