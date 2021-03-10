class TerraformPlanCommand implements TerraformCommand, Pluggable<TerraformPlanCommandPlugin>, Resettable {
    private static final DEFAULT_PLUGINS = []
    private boolean input = false
    private String command = "plan"
    private prefixes = []
    private suffixes = []
    private arguments = []
    private String directory
    private String errorFile
    private Closure variablePattern
    private Closure mapPattern

    // The static initializer is needed to initialize the static variable inherited
    // from the Pluggable trait when the class is loaded.
    static {
        this.plugins = DEFAULT_PLUGINS.clone()
    }

    public TerraformPlanCommand(String environment) {
        this.environment = environment
    }

    public TerraformPlanCommand withInput(boolean input) {
        this.input = input
        return this
    }

    public TerraformPlanCommand withPrefix(String prefix) {
        prefixes << prefix
        return this
    }

    public TerraformPlanCommand withSuffix(String suffix) {
        suffixes << suffix
        return this
    }

    public TerraformPlanCommand withDirectory(String directory) {
        this.directory = directory
        return this
    }

    public TerraformPlanCommand withArgument(String argument) {
        this.arguments << argument
        return this
    }

    public TerraformPlanCommand withVariable(String key, Map value) {
        return withVariable(key, convertMapToCliString(value))
    }

    public TerraformPlanCommand withVariable(String key, String value) {
        def pattern = variablePattern ?: { myKey, myValue -> "-var '${myKey}=${myValue}'" }
        this.arguments << pattern.call(key, value).toString()
        return this
    }

    public TerraformPlanCommand withVariablePattern(Closure pattern) {
        this.variablePattern = pattern
        return this
    }

    public String convertMapToCliString(Map newMap) {
        def pattern = mapPattern ?: { map ->
            def result = map.collect { key, value -> "${key}=\"${value}\"" }.join(',')
            return "{${result}}"
        }

        return pattern.call(newMap)
    }

    public TerraformPlanCommand withMapPattern(Closure pattern) {
        this.mapPattern = pattern
        return this
    }

    public TerraformPlanCommand withStandardErrorRedirection(String errorFile) {
        this.errorFile = errorFile
        return this
    }

    public String toString() {
        applyPlugins()
        def pieces = []
        pieces = pieces + prefixes
        pieces << terraformBinary
        pieces << command
        if (!input) {
            pieces << "-input=false"
        }
        pieces += arguments
        if (directory) {
            pieces << directory
        }

        // This should be built out to handle more complex redirection
        // and should be standardized across all TerraformCommands
        if (errorFile) {
            pieces << "2>${errorFile}"
        }

        pieces += suffixes

        return pieces.join(' ')
    }

    public static TerraformPlanCommand instanceFor(String environment) {
        return new TerraformPlanCommand(environment)
            .withInput(false)
    }

    public static reset() {
        this.plugins = DEFAULT_PLUGINS.clone()
    }
}
