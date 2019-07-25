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

        @PipeID
        String myID;
        
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

You may also specify the field name in annotation:

    @Config("ints")
    int[] anyNameYouLike;
    
Pipeline will automatically parse JSON into the following types:

* int, byte, char, short ...
* Integer, Byte, Character, Short ...
* String
* int[], byte[], char[], short[] ...
* Integer[], Byte[], Character[], Short[] ...
* String[]
* ArrayList\<Object> - *Warning!* due to type erasure it's impossible for framework to know generic parameter of ArrayList. Therefore, everytime you use ArrayList, framework will just *assume* it to be ArrayList\<Object>. You could techincally use any other kind of ArrayList but it might optentially result in ClassCastException at some point.
* Map\<String,Object> - same problem as with ArrayList
* Pattern
* Pattern[]
* Any type ``T`` that has a constructor ``T(String)`` (with one single ``String`` parameter). You can also use ``T[]``. For example ``File``, ``Locale``,``StringBuilder``.


If you wish to use ``@Config`` for any other type you should parse it in your own ``GlobalConfig`` (read below).

Fields annotated with ``@PipeID`` will be injected with value of "id" specified in JSON. They must be of String type and you are not allowed to mix ``@PipeID`` with ``@Config``.

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

Sometimes you might wish to reuse exiString pipeline with some tiny configuration modifications. This is exactly what BlueprintCover is for. JSON example:

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

##### Generic example
Pipeline makes it very easy for you to create tests.
First you define test JSON: (remember to remove comments as they are not valid json)

    {
        "input": "aB-43",  //deserialized with CargoBuilder<Cargo>
        "test": {
            "Preprocessor-id": {
                "input": "aB-43" //deserialized with TestUnit
            },
            "Branching-id": {
                "input": "aB-43-t", //deserialized with TestUnit
                "output": "AB-43-T" //deserialized with TestUnit
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
    
Then you create teString pipeline:


    BlueprintLoader loader = new BlueprintLoader("your.package.with.pipe.classes");

    File jsonFile = ...;
    Blueprint<GlobalCnfg> blueprint = Blueprint.load(jsonFile ,GlobalCnfg.class);
    /**Verifier checks if produced Cargo is acceptable by TestUnit*/
    PipeTestVerifier<Cargo, TestUnit> verifier = ...; 
    GroupTest<Cargo, TestUnit> testPipeline = loader.makeTest(blueprint, verifier);
    BlueprintTest<Cargo, CargoBuilder<String>, TestUnit> blueprintTest = BlueprintTest.load(
        new File(...), 
        CargoBuilder.class, //Class that deserializes initial input
        Cargo.class, 
        TestUnit.class //Class that deserializes expected input/output
    );
        

and run it:

    testPipeline.runWith(blueprintTest);
    
``Cargo`` should be your class that carries data inside pipeline (most basic example is just plain String as in previous examples).

##### TestUnit
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

##### CargoBuilder

``CargoBuilder`` deserializes intial input and builds Cargo based on deserialized values. If you decide to use ``String`` as Cargo, then you might want to use ``DefaultStringBuilder`` as a straight-forward implementation:

    BlueprintTest<Cargo, CargoBuilder<String>, TestUnit> blueprintTest = BlueprintTest.load(
        new File(...), 
        DefaultStringBuilder.class,
        String.class, 
        String.class 
    );
    
But you might use something more complex like this:


    public class CustomStringBuilder implements CargoBuilder<String>{
    
        private String str;
        private int num
        
        @Override
        public String get() {
            return getStr() + getNum(); //concatenation
        }
    
        public String getStr() {
            return str;
        }
    
        public void setStr(String str) {
            this.str = str;
        }
        
        public int getNum() {
            return num;
        }
    
        public void setNum(int num) {
            this. num = num;
        }
    
    }

##### PipeTestVerifier
In order to make use of all this, you need to verify if input and output of each pipe passes the tests. To do this you need to implement ``PipeTestVerifier<Cargo, TestUnit>``. The simplest choice is ``PipeStringTestVerifier`` which just compares string. But you could make something custom like this:

    public class CustomTestVerifier implements PipeTestVerifier<String, String> {
    
        @Override
        public TestResult verifyInput(String input, String testUnit) {
            if (testUnit == null)
                return TestResult.pass(); //nothing specified in JSON, then there is nothing to test
            if (input.length()<3)
                return TestResult.fail("expected longer string","found of length" + input.length());
            return new TestResult(input.equals(testUnit), testUnit, input);
        }
    
        @Override
        public TestResult verifyOutput(String output, String testUnit) {
            if (testUnit == null)
                return TestResult.pass();
            //assert they are equal
            TestResult.equals(testUnit, output); //works just like JUnit! 
            return TestResult.pass();
        }
    
    }

Inside the methods ``verifyInput`` and ``verifyOutput``, you can put all your validation logic. You need to return ``TestResult.pass()`` if the test passed and ``TestResult.fail()`` otherwise. However, you might alternatively use:

- ``return new TestResult(boolean passed, String expected, String found)``
- ``TestResult.equals(expected, found)`` - this will throw special exception that gets caught by framework and promoted as test failure.

### Emitters

There exists an additional way of returning output from pipeline. The standard way allows you to receive only single output:

    Group<String> group = loader.make(blueprint);
    Cargo output = group.process(intput).getValue()
    
But you could use emitter and receiver to get multiple results from at any point in pipeline. The emitter pipe works like this:


    public class Emitter<Cargo> implements Pipe<Cargo> {
    
       
        @Override
        public final Output<Cargo> process(Cargo input) throws Exception {
            //compute output that you would like to emit
            Cargo outputToEmit = ...;
            
            //compute output that you would like to return back 
            //to pipeline for further processing (the standard way)
            Cargo outputToForward = ...;
            
            //pack the output into Result with code and description
            Result<Cargo> resultToEmit = new Result<Cargo>(outputToEmit, description, code)
            
            //you might emit multiple results but in this case we have only one
            List<Result<Cargo>> resultsToEmit = Arrays.asList(resultToEmit);
            
            //return outputToForward and emit resultsToEmit
            return Output.none(outputToForward, resultToEmit);
        }
    
    }

then you can catch all emitted results like this:

    ResultReceiver<String> receiver = new ResultReceiver<String>() {

        @Override
        public void receive(Result<String> result) {
            //do something with result
        }
    };

    Blueprint<GlobalCnfg> blueprint = loader.load(...);
    Group<String> group = loader.make(blueprint);
    group.process(input, receiver);

every ``Result`` carries a ``code`` which is meant to help you identify it. It's completely up to you what those codes are. Additionally there is also ``description`` which is meant to be human readable extra information.

For your convenience there exists a basic implementation of ``Pipe`` that serves purely as emitter. It's called ``EndpointPipe<Cargo>``. You could use it like this:

    //create new class that extends EndpointPipe and put it
    //in the same package as the rest of your pipes 
    //(dependency injection looks for classes
    // only in that one package that you specify)
    public class Emitter extends EndpointPipe<String>{}
	
Include it in JSON:

    {
        "name": "Emitter",
        "config": {
            "code": "your code in form of String",
            "description": "this Emitter will emit whatever comes to it"
        }
    }

### No-common-end branching

Originally everytime you choose some alternative, the data flows like this:

               --- alt1 ---
             /              \    
    ---branch                 ---- common end           
             \              /              
               --- alt2 ---
               
you can either choose alternative ``alt1`` or ``alt2`` but you cannot choose both at once. And you always end up at ``common end`` no matter what.

You may however, use a different type of branching:

               --- alt1 --- results discarded
             /                  
    ---branch --- pipeline continues
             \                            
               --- alt2 --- results discarded
               
In this scenario you will execute all branches but their outputs will be discarded and then pipeline will continue as if nothing ever happened. The only way to get anything back from such branches is to emit results (instead of forwarding them to the next pipe, because at some point there is no next pipe)

Here is an example of such flow:

           --- +1 = 2 --- 2 (discarded)
         /                  
    --- 1 --- +2 = 3 --- +4 = 7 --- outputs 7
         \                            
           --- -1 = 0 --- 0 (discarded)
    
In order to trigger such branching you need to use ``"runAllAlternatives": "true"``. Example:

    {
        "name": "Branching",
        "id": "Branching-id",
        "runAllAlternatives": "true",
        "config": {
            ...
        },
        "alternatives": {
            ...
        }
    }
    
### Observable config

Sometimes you might need to change configuration at runtime and notify all your pipes about it. The solution is very simple. Pipeline provides you with some ready made utilities specifically for this task: ``ObservableConfig`` and ``ConfigChangeListener ``

First you add ObservableConfig to global config:


    class GlobalCnfg extends DoubleHashGlobalConfig{
        
        @Override
        public void onMake() {
            // this will be called whenever you 
            // actually instantiate pipeline
            // (after applying all covers and modifing config)
            ObservableConfig<String> observed = new ObservableConfig<>();
            // we might keep default value in separate field
            String def = get("defaultObserved",String.class);
            observed.setValue(def);
            setProgrammaticOpt("observed",observed);
            
        }
    }

Then you add listeners in pipe:

    public class ObserverPipe implements Pipe<String>, ConfigChangeListener<String>{
    
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
            // pipe's onLoad is called after global config's onLoad
            // therefore listener's won't be called during initialization.
            // If you wish to use initial value then do it now
            lastValue = observed.getValue();
        }
    
        @Override
        public Output<String> process(String input) throws Exception {
            return Output.none(input+" "+lastValue);
        }
    
        @Override
        public void onChange(String newValue, String oldValue) {
            // do something whenever value changes
            lastValue = newValue;
        }
    
    }
    
In order to change value you need to do this:

    Group<String> gr = loader.make(blueprint);
    GlobalCnfg cnfg = blueprint.getGlobal();
    @SuppressWarnings("unchecked")
    ObservableConfig<String> observed = cnfg.get("observed", ObservableConfig.class);
    gr.process("...");
    observed.setValue("foo");
    gr.process("..."); //this time output should be different
    
    
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
            <version>1.11</version>
        </dependency>
    </dependencies>    
    
## Gradle


    repositories {
        maven { url "https://raw.github.com/Alagris/Pipeline/repository/" }
    }
    
    dependencies {
        compile group: 'pipeline', name: 'pipeline', version:'1.11'
    }
