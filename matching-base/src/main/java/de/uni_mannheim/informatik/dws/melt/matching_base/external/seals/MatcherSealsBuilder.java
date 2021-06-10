package de.uni_mannheim.informatik.dws.melt.matching_base.external.seals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MatcherSealsBuilder {


    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherSealsBuilder.class);

    /**
     * Path to the JAR of the SEALS client.
     */
    private File sealsClientJar = null;
    
    /**
     * Path to a temporary folder. Default is set to the systems tmp.
     */
    private File tmpFolder = new File(System.getProperty("java.io.tmpdir"));

    /**
     * Time out for the external seals process. The timeout is applied for each testcase and not track.
     */
    private long timeout = 12;

    /**
     * Time unit for the process time out.
     */
    private TimeUnit timeoutTimeUnit = TimeUnit.HOURS;

    /**
     * The parameters that appear between java [parameters] -jar.
     * Example: ("-Xmx25g", "-Xms15g").
     */
    private List<String> javaRuntimeParameters = new ArrayList<>();
    
    /**
     * If true, the original matcher folder is untouched and the folder is copied.
     * Some matchers require this, because the do not close some resources.
     */
    private boolean freshMatcherInstance = false;

    /**
     * If true, the input alignment is not passed to SEALS even if one is provided.
     */
    private boolean doNotUseInputAlignment = false;
    
    /**
     * The command to start java in the terminal. Typically, this is "java"
     * Seals needs java version 1.8
     */
    private String javaCommand = "java";
    
    //setter methods for builder

    public MatcherSealsBuilder setSealsClientJar(File sealsClientJar) {
        this.sealsClientJar = sealsClientJar;
        return this;
    }

    public MatcherSealsBuilder setTmpFolder(File tmpFolder) {
        this.tmpFolder = tmpFolder;
        return this;
    }

    public MatcherSealsBuilder setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public MatcherSealsBuilder setTimeoutTimeUnit(TimeUnit timeoutTimeUnit) {
        this.timeoutTimeUnit = timeoutTimeUnit;
        return this;
    }

    public MatcherSealsBuilder setJavaRuntimeParameters(List<String> javaRuntimeParameters) {
        this.javaRuntimeParameters = javaRuntimeParameters;
        return this;
    }
    
    public MatcherSealsBuilder addJavaRuntimeParameters(String javaRuntimeParameter) {
        this.javaRuntimeParameters.add(javaRuntimeParameter);
        return this;
    }

    public MatcherSealsBuilder setFreshMatcherInstance(boolean freshMatcherInstance) {
        this.freshMatcherInstance = freshMatcherInstance;
        return this;
    }
    
    public MatcherSealsBuilder setDoNotUseInputAlignment(boolean doNotUseInputAlignment) {
        this.doNotUseInputAlignment = doNotUseInputAlignment;
        return this;
    }

    public MatcherSealsBuilder setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public MatcherSeals build(File fileOrFolder){
        File tmpSealsClientJar = this.sealsClientJar == null ? new File(this.tmpFolder, "seals-omt-client-v" + MatcherSeals.getSealsDownloadUrlVersion() + ".jar") : this.sealsClientJar;
        return new MatcherSeals(fileOrFolder, 
                tmpSealsClientJar,
                this.tmpFolder,
                this.timeout,
                this.timeoutTimeUnit, 
                this.javaRuntimeParameters, 
                this.freshMatcherInstance,
                this.doNotUseInputAlignment,
                this.javaCommand);
    }

    /**
     * Returns all possible MatcherSeals instances for a given file or directory.
     * If it is a file with zip extension, it will unzip it.
     * If it is a directory, it will check if this directory is a matcher.
     * If not, it will inspect the whole directory for matchers.
     * @param matcher the directoroy or file which represents a matcher or a directory of matchers.
     * @return a map of the matcher directory and corresponding matcher name(file name)
     */
    protected Map<String, MatcherSeals> buildFromFolder(File matcher){
        Map<String, MatcherSeals> map = new HashMap<>();
        if (!matcher.exists()) {
            LOGGER.error("The given matcher path does not exist. Returning no matchers.");
            return map;
        }
        if (matcher.isDirectory()) {
            if(MatcherSeals.isDirectoryRunnableInSeals(matcher)){
                MatcherSeals m = build(matcher);
                map.put(m.getName(), m);
            } else {
                LOGGER.info("Inspect all direct subdirectories/subfiles(zip) in folder {}.", matcher);
                for (File fileInMatcher : matcher.listFiles()) {
                    if(fileInMatcher.isDirectory()){
                        File sealsMatcherDir = MatcherSeals.getFirstSubDirectoryRunnableInSeals(fileInMatcher);
                        if(sealsMatcherDir != null){
                            MatcherSeals m = build(sealsMatcherDir);
                            map.put(m.getName(), m);
                        }else{
                            LOGGER.error("Found no matcher in folder: {}", fileInMatcher);
                        }
                    }else if(fileInMatcher.isFile() && fileInMatcher.getName().toLowerCase().endsWith(".zip")){
                        MatcherSeals m = build(fileInMatcher);
                        if(m.getMatcherFolder() != null){
                            map.put(m.getName(), m);
                        }else{
                            LOGGER.error("Matcher is not runnable in SEALS: {}", fileInMatcher);
                        }
                    }
                }
            }
        } else if (matcher.getName().endsWith(".zip")) {
            MatcherSeals m = build(matcher);
            if(m.getMatcherFolder() != null){
                map.put(m.getName(), m);
            }else{
                LOGGER.error("Matcher is not runnable in SEALS: {}", matcher);
            }
        }
        return map;
    }
}
