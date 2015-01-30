package org.eclipse.stardust.ui.web.documenttriage.common;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageUtil {
	// Map that holds accept-language header vs locale strings
	// Language entry only needs to be added when these are different
	// e.g. "zh" vs "zh_CN"
	private static Map<String, String> LANG_POST_FIX_MAP = new HashMap<String, String>();;

	// TODO - find a better way of initializing the map
	static {
		LANG_POST_FIX_MAP.put("zh", "zh_CN");
	}

	/**
	 * @param langHeaderString
	 * @return
	 */
	public static String getLocale(String langHeaderString) {
		String langPostFix = LANG_POST_FIX_MAP.get(langHeaderString.substring(
				0, 2));
		if (null == langPostFix) {
			langPostFix = langHeaderString.substring(0, 2);
		}

		return langPostFix;
	}

	/**
	 * @param locale
	 * @return
	 */
	public static Locale getLocaleObject(String locale) {
		String[] localeParts = locale.split("_");
		if (2 < localeParts.length) {
			return new Locale(localeParts[0], localeParts[1], localeParts[2]);
		} else if (1 < localeParts.length) {
			return new Locale(localeParts[0], localeParts[1]);
		} else if (0 < localeParts.length) {
			return new Locale(localeParts[0]);
		} else {
			return new Locale("en");
		}
	}
}
