package org.hobbit.commonelements;

import java.util.HashSet;
import java.util.Set;


public class OaeiTask {
    public static boolean matchClassDefault = false;
    public static boolean matchObjectPropDefault = false;
    public static boolean matchDataPropDefault = false;
    public static boolean matchInstancesDefault = false;
    public static Set<String> allowedInstanceTypesDefault = new HashSet<>();
    
    private static int idCounter = 0;
    
    private String taskQueueName;
    private String sourceFileName;
    private String targetFileName;
    private String referenceFileName;
    private String taskId;
    
    private boolean matchClass;
    private boolean matchObjectProp;
    private boolean matchDataProp;
    private boolean matchInstances;
    private Set<String> allowedInstanceTypes;

    public OaeiTask(String taskQueueName, String sourceFileName, String targetFileName, String referenceFileName) {
        this(taskQueueName, sourceFileName, targetFileName, referenceFileName, matchClassDefault, matchObjectPropDefault, matchDataPropDefault, matchInstancesDefault, allowedInstanceTypesDefault);
    }

    public OaeiTask(String taskQueueName, String sourceFileName, String targetFileName, String referenceFileName, boolean matchClass, boolean matchObjectProp, boolean matchDataProp, boolean matchInstances, Set<String> allowedInstanceTypes) {
        this.taskQueueName = taskQueueName;
        this.sourceFileName = sourceFileName;
        this.targetFileName = targetFileName;
        this.referenceFileName = referenceFileName;
        this.matchClass = matchClass;
        this.matchObjectProp = matchObjectProp;
        this.matchDataProp = matchDataProp;
        this.matchInstances = matchInstances;
        this.allowedInstanceTypes = allowedInstanceTypes;
        this.taskId = Integer.toString(idCounter);
        idCounter++;
    }

    public static boolean isMatchClassDefault() {
        return matchClassDefault;
    }

    public static boolean isMatchObjectPropDefault() {
        return matchObjectPropDefault;
    }

    public static boolean isMatchDataPropDefault() {
        return matchDataPropDefault;
    }

    public static boolean isMatchInstancesDefault() {
        return matchInstancesDefault;
    }

    public String getTaskQueueName() {
        return taskQueueName;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public String getReferenceFileName() {
        return referenceFileName;
    }

    public boolean isMatchClass() {
        return matchClass;
    }

    public boolean isMatchObjectProp() {
        return matchObjectProp;
    }

    public boolean isMatchDataProp() {
        return matchDataProp;
    }

    public boolean isMatchInstances() {
        return matchInstances;
    }

    public String getTaskId() {
        return taskId;
    }

    public Set<String> getAllowedInstanceTypes() {
        return allowedInstanceTypes;
    }    
    
}
