def loadLibrary(shell, binding, path) {
    def jennyConfig = binding._jennyConfig

    if (jennyConfig.verbose) {
        _log.message "> Loading library ${path}"
    }

    def libFolder = new File(path, "vars")

    if (!libFolder.exists()) {
        throw new IllegalArgumentException("${libFolder} does not exists")
    }

    libFolder.listFiles().each { commandFile ->
        registerCommandInBinding(shell, binding, commandFile)
    }
}

def loadInfoLibrary(shell, binding, path) {
    def jennyConfig = binding._jennyConfig

    if (jennyConfig.verbose) {
        _log.message "> Loading library ${path}"
    }

    def libFolder = new File(path, "vars")

    if (!libFolder.exists()) {
        throw new IllegalArgumentException("${libFolder} does not exists")
    }

    libFolder.listFiles().each { commandFile ->
        def commandName = commandFile.getName().substring(0, commandFile.getName().lastIndexOf("."))

        if (isCommandAllowed(binding, commandName)) {
            registerCommandInBinding(shell, binding, commandFile)
        } else {
            binding[commandName] = { Object...config ->
                shell.evaluate("_log.message(_currentIndent('${commandName}'))")
            }
        }
    }
}

def isCommandAllowed(binding, commandName) {
    for (def expression: binding._jennyConfig.libInfoAllowed) {
        if (commandName.matches(expression)) {
            return true
        }
    }

    return false
}

def registerCommandInBinding(shell, binding, commandFile) {
    def command = shell.parse(commandFile)
    binding[command.class.name] = { Object... config ->
        return command.invokeMethod("call", config)
    }
}

loadLibraries = { shell, binding ->
    binding._jennyConfig.libs.each {
        loadLibrary(shell, binding, (it as File).isAbsolute() ?
            it:
            new File(binding._jennyConfig.projectFolder, it).canonicalPath)
    }
}

loadInfoLibraries = { shell, binding ->
    binding._jennyConfig.libs.each {
        loadInfoLibrary(shell, binding, (it as File).isAbsolute() ?
            it:
            new File(binding._jennyConfig.projectFolder, it).canonicalPath)
    }
}
