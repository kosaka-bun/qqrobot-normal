package de.honoka.qqrobot.normal.test;

import de.honoka.qqrobot.normal.util.EmojiUtils;
import org.junit.jupiter.api.Test;

public class AllTest {

	@Test
	public void test1() {
		System.out.println(EmojiUtils.emojiToUnicode("\uD83C\uDF4B"));
		System.out.println(EmojiUtils.unicodeToEmoji(127819));
		System.out.println(EmojiUtils.emojiToUnicode("\uD83C\uDF81"));
	}
}
