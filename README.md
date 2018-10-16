# Example

### JSON

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
                "id": "Preprocessor-id",
                "config": {
                    "suffix_": "-t",
                    "enabled": "true"
                }
            },
            {
                "name": "Branching",
                "id": "Branching-id",
                "config": {
                    "num1": "1",
                    "num2": "2"
                },
                "alternatives": {
                    "left": [
                        {
                            "name": "Uppercase"
                            "id": "Uppercase-id",
                        }
                    ],
                    "right": [
                        {
                            "name": "Lowercase"
                            "id": "Lowercase-id",
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
    
### Basics

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

You may also specify the filed name in annotation:

    @Config("ints")
    int[] anyNameYouLike;
    
Pipeline will automatically parse JSON into the following types:

* int, byte, char, short ...
* Integer, Byte, Character, Short ...
* String
* int[], byte[], char[], short[] ...
* Integer[], Byte[], Character[], Short[] ...
* String[]
* ArrayList\<String>

If you wish to use ``@Config`` for any other type you should parse it in your own ``GlobalConfig`` (read below).

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
                 // You can then inject Locale 
                // directly with @Config like this:
                //
                // @Config
                // Locale locale;
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

    BlueprintLoader loader = new BlueprintLoader("your.package.with.pipe.classes");
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

### Covers

Sometimes you might wish to reuse existing pipeline with some tiny configuration modifications. This is exactly what BlueprintCover is for. JSON example:

    {
        "global": {
            "lang": "pl-PL",
            "suffix": "-g",
            "paths": [
                "tap1",
                "tap2"
            ],
            "ints": [
                "099",
                "1499",
                "43"
            ]
        },
        "cover": {
            "Preprocessor-id": {
                "config": {
                    "suffix": "-new"
                }
            },
            "Branching-id": {
                "config": {
                    "left": "10",
                    "right": "20"
                }
            },
            "Uppercase-id": {
                "config": {
                    "enabled": "false"
                }
            },
            "Lowercase-id": {
                "config": {
                    "enabled": "false"
                }
            }
        }
    }

You can apply this cover with a method like this:

    blueprint.applyCover(new File("path/to/cover.json"), GlobalCnfg.class);
    
#### Cover from command line parameters

There is a nice and simple utility called ``CommandLineToCover``. It allows you to parse incoming command line parameters and turn them into ``BlueprintCover``. 

    public static void main(String[] args) {
        CommandLineToCover cmdCover = new CommandLineToCover(args);
        try {
            BlueprintCover<GlobalCnfg> cover = cmdCover.make(GlobalCnfg.class);
            Blueprint<GlobalCnfg> blueprint = ...;
            blueprint.applyCover(cover);
        } catch (InstantiationException | IllegalAccessException | ParseException | DuplicateIdException e) {
            e.printStackTrace();
        }
    }
    
The format for such parameters is:

    $java -jar myApp.jar globalCnfg1=foo globalCnfg2=bar ... --pipeID1 pipe1Param1="[a, b, c]" pipe1Param2="[4, 5, 6]" ... --pipeID2 pipe2Param1=baz pipe2Param2="foo bar" ...
    
Example:

    $java -jar myApp.jar paths="[my/path, /etc]" lang=en-GB --Preprocessor-id suffix="su fix" paths="[/try/hard]" --Branching-id left=X right=Y
    
### Aliases

A very useful feature that suppliments IDs is aliases. Every pipe can be assigned to zero or more of them. Here is how:

    "aliases": { // here you define allowed aliases
        "preprocessors": { // alias name
            "fields": [ // specify which fields can be affected by this alias
                "enabled"
            ]
        },
        "suffixed": {
            "fields": [
                "enabled",
                "suffix"
            ]
        }
    },
    "pipeline": [
        ...
        {
            "name": "Preprocessor",
            "id": "Preprocessor-id",
            "config": {
                "suffix": "-t1"
            },
            "aliases": [
                "preprocessors",
                "suffixed",
            ]
        }
        ...
    ]
    
Then you can apply covers and use aliases just like you use IDs. The only difference is that while ID affects only one single pipe (and you can cover any configuration field you wish), an alias affects every aliased pipe but restricts which fields can be modified. Notice that if one pipe is affected by multiple aliases, then they are applied in the order in which they were defined in ``"aliases": {...}`` (and ID-specific covers are applied at the very end, after all aliases)
    
### Tests

Pipeline makes it very easy for you to create tests.
First you define test JSON:

    {
        "input": "aB-43",
        "test": {
            "Preprocessor-id": {
                "input": "aB-43"
            },
            "Branching-id": {
                "input": "aB-43-t",
                "output": "AB-43-T"
            },
            "Truecaser-id": {
                "input": "",
                "output": ""
            },
            "Lowercase-id": {
                "input": "",
                "output": ""
            }
        }
    }
    
Then you create testing pipeline:


    BlueprintLoader loader = new BlueprintLoader("your.package.with.pipe.classes");

    File jsonFile = ...;
    Blueprint<GlobalCnfg> blueprint = Blueprint.load(jsonFile ,GlobalCnfg.class);
    /**Verifier checks if produced Cargo is acceptable by TestUnit*/
    PipeTestVerifier<Cargo, TestUnit> verifier = ...; 
    GroupTest<Cargo, TestUnit> testPipeline = loader.makeTest(blueprint, verifier);
    BlueprintTest<Cargo, TestUnit> blueprintTest = BlueprintTest.load(new File(...),Cargo.class,TestUnit.class);
        

and run it:

    testPipeline.runWith(blueprintTest);
    
``Cargo`` should be your class that carries data inside pipeline (most basic example is just plain String as in previous examples).
``TestUnit `` is the class that deserializes ``"input":...`` and ``"output":...``. Simplest choice is String but it might be for example something like this:

java:

    class TestUnit{
        int someInt;
        String stringA;
        String stringB;
    }

json:

    {
        "input": "blah",
        "test": {
            "Preprocessor-id": {
                "output": {
                    "stringA": "nice",
                    "stringB": "420",
                    "someInt": "69"
                 }
            }
        }
    }
    
You can very easily use this from JUnit (although it works separate just fine).

### Convenience and extras

If you plan on applying many covers and loading multiple pipelines but all with the same generic types you might choose to use ``BlueprintTypedLoader``. Example:

    try {
        //you specify classes only once in the contructor
        BlueprintTypedLoader<String, GlobalCnfg> loader = new BlueprintTypedLoader<String, GlobalCnfg>(Main.class,
                String.class, GlobalCnfg.class);
        //and then those classes are filled in for you
        Blueprint<GlobalCnfg> blueprint = loader.load(new File("pipeline.json"));
        loader.applyCover(blueprint, new File("cover.json"));
        Group<String> gr = loader.make(blueprint);
        gr.close();
    } catch (Exception e) {
        e.printStackTrace();
    }

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
            <version>1.4</version>
        </dependency>
    </dependencies>    
    
## Gradle


    repositories {
        maven { url "https://raw.github.com/Alagris/Pipeline/repository/" }
    }
    
    dependencies {
        compile group: 'pipeline', name: 'pipeline', version:'1.4'
    }
