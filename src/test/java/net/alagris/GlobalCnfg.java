package net.alagris;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GlobalCnfg extends DoubleHashGlobalConfig {

	@JsonIgnore
	private Locale locale;
	
	private String country;
	
	@Override
	public void onLoad() {
		String lang = (String) getOpts().get("lang");
		if (lang != null) {
			locale = Locale.forLanguageTag(lang);
			setProgrammaticOpt("locale", locale);
			setProgrammaticOpt("country", lang.substring(Math.max(0,lang.length()-2)));
		}
	}
	@Override
	public void applyCover(GlobalConfig other) {
		super.applyCover(other);
		onLoad();
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

}
