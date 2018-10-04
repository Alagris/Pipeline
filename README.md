# Example

``Pipeline`` is a a tiny library that allows for sequential data processing. Below you can find a usage example. First of all you need to create JSON file like this:

    {
    	"global": {
    		"lang": "es-ES",
    		"suffix": "-g",
    		"strings": [
    			"string one",
    			"and two"
    		],
    		"ints": [
    			"0",
    			"14"
    		]
    	},
    	"pipeline": [
    		{
    			"name": "Preprocessor",
    			"config": {
    				"suffix_": "-t",
    				"enabled": "true"
    			}
    		},
    		{
    			"name": "Branching",
    			"config": {
    				"num1": "1",
    				"num2": "2"
    			},
    			"alternatives": {
    				"left": [
    					{
    						"name": "Uppercase"
    					}
    				],
    				"right": [
    					{
    						"name": "Lowercase"
    					}
    				]
    			}
    		},
    		{
    			"name": "Trim"
    		},
    		{
    			"name": "Fix"
    		}
    	]
    }
    
Then you need to create classes ``Preprocessor``, ``Branching``, ``Uppercase``, ``Lowercase``, ``Trim``, ``Fix``. All of them have to implement ``Pipe`` interface. 

    public class Autofixer implements Pipe<String>{

    	@Override
    	public Output<String> process(String input) {
    		return new Output<String>(input);
    	}
    
    	@Override
    	public void onLoad() {
    		
    	}
    }	
	
Or if you wish to make a pipe optional (so you can use ``"enable":"false"`` to temporarily disable it) just extend ``OptionalPipe``

    public class Preprocessor extends OptionalPipe<String> {
    
    	@Config
    	String suffix;
    
    	@Config
    	String[] paths;
    
    	@Config
    	int[] ints;
    
    	@Override
    	public Output<String> proc(String input) {
    		return new Output<String>(input + suffix);
    	}
    
    	@Override
        public void onLoad() {

	    }
    }
    
Fields annotated with ``@Config`` will be injected with values specified in JSON. For example to inject

    @Config
    int[] ints;

1. first will be checked ``"config": {}`` field that belongs to given pipe. If no ``"ints": `` is found inside it, then
2. ``"global": {}`` configuration will be searched

Pipelines can branch into subpipelines. This is what ``"alternatives":{}`` is for. Inside you put ``"BRANCH_NAME":[{},{},...]`` that specify possible subpipelines to follow. There are 2 standard branch names (they are a nice convention but you are not limited to them): "left" and "right". In order to branch to an alternative pipeline you need to use ``Output`` class.

    public class Branching implements Pipe<String> {
    
    	@Override
    	public Output<String> process(String input) {
    	    if(input.length() < 5 ){
    		    return Output.right(input);
    		}else{
    		    return Output.left(input);
    		}
    	}
    
    	@Override
    	public void onLoad() {
    	}
    }

You can configure your own class for the ``"global"`` configurations or use one if premade generic classes:
* ``HashGlobalConfig`` - saves all configurations into HashMap<String,Object>
* ``DoubleHashGlobalConfig`` - same as ``HashGlobalConfig`` but it also contains one additional ``HashMap`` for configurations that you generate programmatically in ``onLoad()`` method.
* custom class that implements ``GlobalConfig``
 
When JSON file is fully parsed (all ``Pipe``s are instantiated and all ``@Config``s injected), library will start calling ``onLoad()`` callbacks. You can leave them empty, but you may also use them to perform any extra initialization. 

Here is an example of custom class that uses ``DoubleHashGlobalConfig``:

    
    public class GlobalCnfg extends DoubleHashGlobalConfig {
    
    	@JsonIgnore
    	private Locale locale;
    	
    	@JsonIgnore
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


Once you have everything ready you can finally run the pipeline with this piece of code:

    BlueprintLoader loader = new BlueprintLoader("net.alagris");
	try {
	    File jsonFile = ...;
	    Blueprint<GlobalCnfg> blueprint = Blueprint.load(jsonFile ,GlobalCnfg.class);
		System.out.println(blueprint);
		Group<String> gr = loader.make(blueprint, String.class);
		System.out.println(gr.process("sOmE sTrInG").getValue());
	} catch (IOException e) {
		e.printStackTrace();
	}

Notice that your pipeline might operate on anything. It doesn't have to be ``String``. Just change generic parameters into some other class.

By default pipeline prints intermediate outputs. You can change level of verbosity by modyfing parameters in class ``Logger``.

# Download

## Maven

    <repositories>
    	<repository>
    		<id>pipeline</id>
    		<url>https://raw.github.com/Alagris/Pipeline/repository/</url>
    	</repository>
    </repositories>
    
    <dependencies>
    	<dependency>
    		<groupId>pipeline</groupId>
    		<artifactId>pipeline</artifactId>
    		<version>1.0</version>
    	</dependency>
    </dependencies>	
    
## Gradle


    repositories {
        maven { url "https://raw.github.com/Alagris/Pipeline/repository/" }
    }
    
    dependencies {
    	compile group: 'pipeline', name: 'pipeline', version:'1.0'
    }
