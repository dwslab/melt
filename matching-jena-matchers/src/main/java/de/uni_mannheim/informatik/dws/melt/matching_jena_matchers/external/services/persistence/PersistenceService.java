package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.dataStructures.StringString;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.GroupSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;


/**
 * A simple persistence service offering stripped-down database operations to other applications.
 */
public class PersistenceService {

    /**
     * Directory where all persistence database files will be saved.
     */
    public final static String PERSITENCE_DIRECTORY = "./persitences";

    /**
     * Logger.
     */
    private static Logger LOGGER = LoggerFactory.getLogger("PersistenceService");

    /**
     * Singleton instance.
     */
    private static PersistenceService service;

    /**
     * List of all active databases (required for collective close.).
     */
    private ArrayList<DB> activeDatabases;

    /**
     * Private constructor, singleton pattern.
     */
    private PersistenceService() {
        activeDatabases = new ArrayList<>();
    }

    /**
     * Singleton pattern: Get persistence service instance.
     *
     * @return Persistence service instance.
     */
    public static PersistenceService getService() {
        if (service == null) {
            service = new PersistenceService();
        }
        return service;
    }


    /**
     * Obtain a pre-configured persistence.
     *
     * @param desiredPersistence The persistence that shall be obtained.
     * @return Database
     */
    public ConcurrentMap getMapDatabase(PreconfiguredPersistences desiredPersistence) {

        if (new File(desiredPersistence.getFilePath()).getParentFile().mkdir())
            LOGGER.info("Persistence Directory Created");
        DB db;
        db = DBMaker
                .fileDB(desiredPersistence.getFilePath())
                .fileMmapEnable()
                .fileLockDisable() // ignore file lock
                .checksumHeaderBypass() // ignore header checksum (should work in *most* cases in which writing was interrupted
                .closeOnJvmShutdown()
                .make();
        activeDatabases.add(db);
        return db
                .hashMap("map", desiredPersistence.getKeySerializer(), desiredPersistence.getValueSerializer())
                .createOrOpen();
    }


    /**
     * Close all opened databases and shut down service.
     */
    public void close() {
        // close all databases
        for (DB db : activeDatabases) {
            db.close();
        }
        // set instance to null
        service = null;
    }

    /**
     * Enum with the preconfigured database persistences.
     * DEV Remark: Run Unit Test after adding an additional persistence configuration.
     */
    public enum PreconfiguredPersistences {

        ALOD_CLASSIC_SYONYMY_BUFFER, ALOD_CLASSIC_LABEL_URI_BUFFER,
        ALOD_XL_SYONYMY_BUFFER, ALOD_XL_LABEL_URI_BUFFER,
        ALOD_CLASSIC_HYPERNYMY_BUFFER, ALOD_XL_HYPERNYMY_BUFFER,

        /**
         * Stores existing and non-existing concepts.
         * Data Structure: String -&gt; String
         */
        BABELNET_SINGLE_CONCEPT_BUFFER,

        /**
         * Stores 1:n links for BabelNet
         * Data Structure: String -&gt; {@code HashSet<String>}
         */
        BABELNET_MULTI_CONCEPT_BUFFER,

        /**
         * Stores the synonyms of BabelNet
         * Data Structure: String -&gt; {@code HashSet<String>}
         */
        BABELNET_SYNONYM_BUFFER,

        /**
         * Stores the synonyms of BabelNet
         * Data Structure: String -&gt; {@code HashSet<String>}.
         */
        BABELNET_HYPERNYMY_BUFFER;

        public Class getKeyClass() {
            switch (this) {
                case ALOD_CLASSIC_SYONYMY_BUFFER:
                case ALOD_XL_SYONYMY_BUFFER:
                case ALOD_CLASSIC_HYPERNYMY_BUFFER:
                case ALOD_XL_HYPERNYMY_BUFFER:
                    return StringString.class;
                case ALOD_CLASSIC_LABEL_URI_BUFFER:
                case ALOD_XL_LABEL_URI_BUFFER:
                    return String.class;
                case BABELNET_SYNONYM_BUFFER:
                case BABELNET_HYPERNYMY_BUFFER:
                    return String.class;
                case BABELNET_SINGLE_CONCEPT_BUFFER:
                    return String.class;
                case BABELNET_MULTI_CONCEPT_BUFFER:
                    return String.class;
            }
            return null;
        }

        public GroupSerializer getKeySerializer() {
            switch (this) {
                case ALOD_CLASSIC_SYONYMY_BUFFER:
                case ALOD_XL_SYONYMY_BUFFER:
                case ALOD_CLASSIC_HYPERNYMY_BUFFER:
                case ALOD_XL_HYPERNYMY_BUFFER:
                    return Serializer.JAVA;
                case ALOD_CLASSIC_LABEL_URI_BUFFER:
                case ALOD_XL_LABEL_URI_BUFFER:
                    return Serializer.STRING;
                case BABELNET_SYNONYM_BUFFER:
                case BABELNET_HYPERNYMY_BUFFER:
                    return Serializer.STRING;
                case BABELNET_SINGLE_CONCEPT_BUFFER:
                    return Serializer.STRING;
                case BABELNET_MULTI_CONCEPT_BUFFER:
                    return Serializer.STRING;
            }
            return null;
        }

        public GroupSerializer getValueSerializer() {
            switch (this) {
                case ALOD_CLASSIC_SYONYMY_BUFFER:
                case ALOD_XL_SYONYMY_BUFFER:
                case ALOD_CLASSIC_HYPERNYMY_BUFFER:
                case ALOD_XL_HYPERNYMY_BUFFER:
                    return Serializer.BOOLEAN;
                case ALOD_CLASSIC_LABEL_URI_BUFFER:
                case ALOD_XL_LABEL_URI_BUFFER:
                    return Serializer.STRING;
                case BABELNET_SYNONYM_BUFFER:
                case BABELNET_HYPERNYMY_BUFFER:
                    return Serializer.JAVA;
                case BABELNET_SINGLE_CONCEPT_BUFFER:
                    return Serializer.STRING;
                case BABELNET_MULTI_CONCEPT_BUFFER:
                    return Serializer.JAVA;
            }
            return null;
        }


        /**
         * Get the file path to the persistence.
         *
         * @return Path where the persistence file can be found.
         */
        public String getFilePath() {
            switch (this) {
                case ALOD_CLASSIC_SYONYMY_BUFFER:
                    return PERSITENCE_DIRECTORY + "/webisalod_classic_synonymy_buffer.mapdb";
                case ALOD_XL_SYONYMY_BUFFER:
                    return PERSITENCE_DIRECTORY + "/webisalod_xl_synonymy_buffer.mapdb";
                case ALOD_CLASSIC_LABEL_URI_BUFFER:
                    return PERSITENCE_DIRECTORY + "/webisalod_classic_label_uri_buffer.mapdb";
                case ALOD_XL_LABEL_URI_BUFFER:
                    return PERSITENCE_DIRECTORY + "/webisalod_xl_label_uri_buffer.mapdb";
                case ALOD_CLASSIC_HYPERNYMY_BUFFER:
                    return PERSITENCE_DIRECTORY + "/webisalod_classic_hypernymy_buffer.mapdb";
                case ALOD_XL_HYPERNYMY_BUFFER:
                    return PERSITENCE_DIRECTORY + "/webisalod_xl_hypernymy_buffer.mapdb";
                case BABELNET_SYNONYM_BUFFER:
                    return PERSITENCE_DIRECTORY + "/babelnet_synonym_buffer.mapdb";
                case BABELNET_HYPERNYMY_BUFFER:
                    return PERSITENCE_DIRECTORY + "/babelnet_hypernymy_buffer.mapdb";
                case BABELNET_SINGLE_CONCEPT_BUFFER:
                    return PERSITENCE_DIRECTORY + "/babelnet_single_concept_buffer.mapdb";
                case BABELNET_MULTI_CONCEPT_BUFFER:
                    return PERSITENCE_DIRECTORY + "/babelnet_multi_concept_buffer.mapdb";
            }
            return null;
        }
    }
}
