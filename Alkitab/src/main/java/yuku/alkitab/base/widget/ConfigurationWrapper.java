package yuku.alkitab.base.widget;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import android.provider.Settings;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import yuku.afw.storage.Preferences;
import yuku.alkitab.base.App;
import yuku.alkitab.debug.BuildConfig;
import yuku.alkitab.debug.R;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Context wrapper for changing app-wide locale or font scale.
 */
public class ConfigurationWrapper extends ContextWrapper {
	static final String TAG = ConfigurationWrapper.class.getSimpleName();

	public ConfigurationWrapper(final Context base) {
		super(base);
	}

	public static Context wrap(final Context base) {
		final Configuration config = base.getResources().getConfiguration();

		final Locale prefLocale = getLocaleFromPreferences();
		if (BuildConfig.DEBUG) Log.d(TAG, "@@wrap: config locale will be updated to: " + prefLocale);

		Locale.setDefault(prefLocale);
		ConfigurationCompat.setLocale(config, prefLocale);

		final float fontScale = getFontScaleFromPreferences();
		if (config.fontScale != fontScale) {
			if (BuildConfig.DEBUG) Log.d(TAG, "@@wrap: fontScale will be updated to: " + fontScale);

			config.fontScale = fontScale;
		}

		return new ConfigurationWrapper(ConfigurationCompat.updateConfiguration(base, config));
	}

	private static AtomicInteger serialCounter = new AtomicInteger();

	public static int getSerialCounter() {
		return serialCounter.get();
	}

	public static void notifyConfigurationNeedsUpdate() {
		serialCounter.incrementAndGet();
	}

	@SuppressWarnings("deprecation")
	public static class ConfigurationCompat {
		@Nullable
		public static Locale getLocale(Configuration config) {
			if (Build.VERSION.SDK_INT >= 24) {
				final LocaleList locales = config.getLocales();
				if (locales.size() > 0) {
					return locales.get(0);
				} else {
					return null;
				}
			} else {
				return config.locale;
			}
		}

		public static void setLocale(Configuration config, @NonNull Locale locale) {
			if (Build.VERSION.SDK_INT >= 24) {
				config.setLocales(new LocaleList(locale));
			} else if (Build.VERSION.SDK_INT >= 17) {
				config.setLocale(locale);
			} else {
				config.locale = locale;
			}
		}

		@CheckResult
		public static Context updateConfiguration(Context context, Configuration config) {
			if (Build.VERSION.SDK_INT >= 17) {
				return context.createConfigurationContext(config);
			} else {
				context.getResources().updateConfiguration(config, null);
				return context;
			}
		}
	}


	@NonNull
	public static Locale getLocaleFromPreferences() {
		final String lang = Preferences.getString(R.string.pref_language_key, R.string.pref_language_default);
		if (lang == null || "DEFAULT".equals(lang)) {
			return Locale.getDefault();
		}

		switch (lang) {
			case "zh-CN":
				return Locale.SIMPLIFIED_CHINESE;
			case "zh-TW":
				return Locale.TRADITIONAL_CHINESE;
			default:
				return new Locale(lang);
		}
	}

	private static float getFontScaleFromPreferences() {
		float res = 0.f;

		final String forceFontScale = Preferences.getString(R.string.pref_forceFontScale_key, R.string.pref_forceFontScale_default);
		if (!forceFontScale.equals(Preferences.getString(R.string.pref_forceFontScale_default))) {
			if (forceFontScale.equals(Preferences.getString(R.string.pref_forceFontScale_value_x1_5))) {
				res = 1.5f;
			} else if (forceFontScale.equals(Preferences.getString(R.string.pref_forceFontScale_value_x1_7))) {
				res = 1.7f;
			} else if (forceFontScale.equals(Preferences.getString(R.string.pref_forceFontScale_value_x2_0))) {
				res = 2.0f;
			}
		}

		if (res == 0.f) {
			final float defFontScale = Settings.System.getFloat(App.context.getContentResolver(), Settings.System.FONT_SCALE, 1.f);
			if (BuildConfig.DEBUG) Log.d(TAG, "defFontScale: " + defFontScale);
			res = defFontScale;
		}

		return res;
	}
}