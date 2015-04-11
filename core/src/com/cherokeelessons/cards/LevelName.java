package com.cherokeelessons.cards;

public enum LevelName {
	Newbie("Newbie", 0, "CgkI4-75m_EMEAIQAQ"), Novice("Novice", 1,
			"CgkI4-75m_EMEAIQAg"), Rookie("Rookie", 2, "CgkI4-75m_EMEAIQAw"), Beginner(
			"Beginner", 3, "CgkI4-75m_EMEAIQBA"), Apprentice("Apprentice",
			4, "CgkI4-75m_EMEAIQBQ"), Intermediate("Intermediate", 5,
			"CgkI4-75m_EMEAIQBw"), Advanced("Advanced", 6,
			"CgkI4-75m_EMEAIQCA"), Proficient("Proficient", 7,
			"CgkI4-75m_EMEAIQCg"), Expert("Expert", 8, "CgkI4-75m_EMEAIQCw"), Master(
			"Master", 9, "CgkI4-75m_EMEAIQDA"), GrandMaster("Grandmaster",
			10, "CgkI4-75m_EMEAIQDQ");
	
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

	public String getEnglish() {
		return engrish;
	}

	@Override
	public String toString() {
		return getEnglish();
	}

}