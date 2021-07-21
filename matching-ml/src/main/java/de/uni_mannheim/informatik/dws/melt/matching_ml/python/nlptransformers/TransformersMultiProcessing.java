package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

/**
 * The transformers library may not free all memory from GPU.
 * Thus the prediction and training are wrapped in an external process.
 * This enum defines how the process is started and if multiprocessing should be used at all.
 */
public enum TransformersMultiProcessing {
    
    /**
     * The transformer model is executed directly in the main process.
     * If you call the Transformers matcher in MELT multiple times, then this can lead to memory issues.
     */
    NO_MULTI_PROCESS,
    
    /**
     * This uses the default start method for the operating system.
     * This is {@link TransformersMultiProcessing#SPAWN} for Windows and macOS, and {@link TransformersMultiProcessing#FORK} for Unix.
     * See also <a href="https://docs.python.org/3/library/multiprocessing.html#contexts-and-start-methods">the python documentation about it</a>.
     */
    DEFAULT_MULTI_PROCESS,
    
    /**
     * The main process starts a fresh python interpreter.
     * Available on Unix and Windows.
     * See also <a href="https://docs.python.org/3/library/multiprocessing.html#contexts-and-start-methods">the python documentation about it</a>.
     */
    SPAWN,
    
    /**
     * This uses os.fork() to fork the python interpreter.
     * Available on Unix only.
     * This may result in the following error:
     * <a href="https://github.com/huggingface/transformers/issues/6753#issuecomment-824176684">
     * Cannot re-initialize CUDA in forked subprocess. To use CUDA with multiprocessing, you must use the 'spawn' start method.
     * </a>.
     * See also <a href="https://docs.python.org/3/library/multiprocessing.html#contexts-and-start-methods">the python documentation about it</a>.
     */
    FORK,
    
    /**
     * This uses a server process to start a new process. Available only on Unix.
     * See also <a href="https://docs.python.org/3/library/multiprocessing.html#contexts-and-start-methods">the python documentation about it</a>.
     */
    FORKSERVER;
    
    @Override
    public String toString() {
        return name().toLowerCase();
    }    
}
