class Jenkinsfile {
    public static original
    public static docker
    public static defaultNodeName
    public static repoSlug = null
    public static instance = new Jenkinsfile()
    public static declarative = false
    public static pipelineTemplate

    def node(Closure closure) {
        closure.delegate = original
        String label = getNodeName()
        if (label != null) {
            echo "Using node: ${label}"
            original.node(label, closure)
        } else {
            echo "defaultNodeName and DEFAULT_NODE_NAME environment variable are null"
            original.node(closure)
        }
    }

    def invokeMethod(String name, args) {
        original.invokeMethod(name, args)
    }

    def String getStandardizedRepoSlug() {
        if (repoSlug != null) {
            return repoSlug
        }

        def scmUrl = getScmUrl()
        def scmMap = parseScmUrl(scmUrl)
        repoSlug = "${standardizeString(scmMap['organization'])}/${standardizeString(scmMap['repo'])}"
        return repoSlug
    }

    def String getScmUrl() {
        def closure = {
            scm.getUserRemoteConfigs()[0].getUrl()
        }
        closure.delegate = original
        closure.call()
    }

    def Map parseScmUrl(String scmUrl) {
        def matcher = scmUrl =~ /.*(?:\/\/|\@)[^\/:]+[\/:]([^\/]+)\/([^\/.]+)(.git)?/
        def Map results = new HashMap<String,String>()
        results.put("organization", matcher[0][1])
        results.put("repo", matcher[0][2])
        return results
    }

    def String standardizeString(String original) {
        original.replaceAll( /-/, '_').replaceAll( /([A-Z]+)/, /_$1/ ).toLowerCase().replaceAll( /^_/, '' ).replaceAll( /_+/, '_')
    }

    def String getRepoName() {
        def Map scmMap = parseScmUrl(getScmUrl())
        return scmMap['repo']
    }

    def String getOrganization() {
        def Map scmMap = parseScmUrl(getScmUrl())
        return scmMap['organization']
    }

    def static void init(original, Class customizations=null) {
        this.original = original
        this.docker   = original.docker

        if (customizations != null) {
            customizations.init()
        }
    }

    // Deprecate this, env should come from original
    def static void init(original, env, Class customizations=null) {
        this.original = original
        this.docker   = original.docker

        if (customizations != null) {
            customizations.init()
        }
    }

    def static String getNodeName() {
        return defaultNodeName ?: instance.getEnv().DEFAULT_NODE_NAME
    }

    public static void build(Closure closure) {
        closure.delegate = this.instance
        closure.call()
    }

    public static void build(List<Stage> stages) {
        if (!declarative) {
            stages.each { Stage stage -> stage.build() }
        } else {
            if (pipelineTemplate == null) {
                this.pipelineTemplate = getPipelineTemplate(stages)
            }

            pipelineTemplate.call(stages)
        }
    }

    public static getPipelineTemplate(List<Stage> stages) {
        switch (stages.size()) {
            case 2:
                return original.Pipeline2Stage
            case 3:
                return original.Pipeline3Stage
            case 4:
                return original.Pipeline4Stage
            case 5:
                return original.Pipeline5Stage
            case 6:
                return original.Pipeline6Stage
            case 7:
                return original.Pipeline7Stage
        }

        throw new RuntimeException("Your pipeline has ${stages.size()} stages - the maximum supported by default is 7.  Define a custom pipeline template and assign it to Jenkinsfile.pipelineTemplate to create your pipeline.")

    }

    public getEnv() {
        return original.env
    }
}
