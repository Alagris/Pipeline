package net.alagris;

public class Observer implements Pipe<String>, ConfigChangeListener<String>{

	@Config
	ObservableConfig<String> observed;
	
	String lastValue = "";
	@Override
	public void close() throws Exception {
		observed.removeListener(this);
	}

	@Override
	public void onLoad() throws Exception {
		observed.addListener(this);
		lastValue = observed.getValue();
	}

	@Override
	public Output<String> process(String input) throws Exception {
		return Output.none(input+" "+lastValue);
	}

	@Override
	public void onChange(String newValue, String oldValue) {
		lastValue = newValue;
	}

}
