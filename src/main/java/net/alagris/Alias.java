package net.alagris;

public class Alias {

	private String[] fields;

	/** List of fields visible (modifiable) to this alias */
	public String[] getFields() {
		return fields;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}

	public boolean allowsField(String field) {
		if (field == null)
			return false;
		for (String f : fields)
			if (field.equals(f))
				return true;
		return false;
	}
}
