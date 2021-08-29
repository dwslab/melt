package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.dataStructures.StringString;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.GroupSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;


/**
 * A simple persistence service offering stripped-down database operations to other applications.
 */
public class PersistenceService {


    /**
     * Directory where all persistence database files will be saved.
     */
    public final static String DEFAULT_PERSISTENCE_DIRECTORY = "." + File.separator + "persistences";

    public static String persistenceDirectory;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("PersistenceService");

    /**
     * Singleton instance.
     */
    private static PersistenceService service;

    /**
     * Map of all active databases (required for collective close and for commits).
     */
    private Map<PreconfiguredPersistences, DB> activeDatabases;

    /**
     * Private constructor, singleton pattern.
     */
    private PersistenceService(String persistenceDirectory) {
        activeDatabases = new HashMap<>();
        if (persistenceDirectory != null) {
            PersistenceService.persistenceDirectory = persistenceDirectory;
        } else PersistenceService.persistenceDirectory = DEFAULT_PERSISTENCE_DIRECTORY;

    }

    /**
     * Singleton pattern: Get persistence service instance.
     * Warning: Only use this getter, if you really need another persistence directory.
     * In all other cases, use {@link PersistenceService}. You cannot request multiple persistence services
     * using different persistenceDirectories at the same time.
     *
     * @param persistenceDirectory The directory where persistence files shall be saved to.
     * @return The service instance.
     */
    public static PersistenceService getService(String persistenceDirectory) {
        if (persistenceDirectory == null) {
            persistenceDirectory = DEFAULT_PERSISTENCE_DIRECTORY;
        }
        if (persistenceDirectory.endsWith("/")) {
            persistenceDirectory = persistenceDirectory.substring(0, persistenceDirectory.length() - 1);
        }
        if (!persistenceDirectory.equals(DEFAULT_PERSISTENCE_DIRECTORY) &&
                PersistenceService.persistenceDirectory != null &&
                PersistenceService.persistenceDirectory.equals(persistenceDirectory)) {
            LOGGER.info("Request for new persistence (" + persistenceDirectory + ").\n" +
                    "However, new persistence is equal to current persistence, doing nothing.");
        } else {
            if (service != null) {
                LOGGER.info("Closing persistence service to open new one using new persistence directory.");
                service.closePersistenceService();
            }
            service = new PersistenceService(persistenceDirectory);
        }
        return service;
    }

    /**
     * Singleton pattern: Get persistence service instance.
     *
     * @return Persistence service instance.
     */
    public static PersistenceService getService() {
        if(service != null){
            return service;
        }
        return getService(DEFAULT_PERSISTENCE_DIRECTORY);
    }

    /**
     * Obtain a pre-configured persistence.
     *
     * @param desiredPersistence The persistence that shall be obtained.
     * @return Database
     */
    public ConcurrentMap getMapDatabase(PreconfiguredPersistences desiredPersistence) {
        if (activeDatabases.containsKey(desiredPersistence)) {
            return activeDatabases.get(desiredPersistence)
                    .hashMap("map", desiredPersistence.getKeySerializer(), desiredPersistence.getValueSerializer())
                    .open();
        }

        if (new File(desiredPersistence.getFilePath()).getParentFile().mkdir()) {
            LOGGER.info("Persistence Directory Created");
        }
        DB db;
        db = DBMaker
                .fileDB(desiredPersistence.getFilePath())
                .fileMmapEnable()
                //.fileLockDisable() // ignore file lock
                .transactionEnable()
                //.checksumHeaderBypass() // ignore header checksum (should work in *most* cases in which writing was interrupted
                .closeOnJvmShutdown()
                .make();
        activeDatabases.put(desiredPersistence, db);
        return db
                .hashMap("map", desiredPersistence.getKeySerializer(), desiredPersistence.getValueSerializer())
                .createOrOpen();
    }

    public void commit(PreconfiguredPersistences persistence) {
        if (!activeDatabases.containsKey(persistence)) {
            LOGGER.warn("Cannot commit for " + persistence + " - DB not active.");
            return;
        }
        activeDatabases.get(persistence).commit();
    }

    public String getPersistenceDirectory() {
        return persistenceDirectory;
    }

    /**
     * Close a single persistence.
     *
     * @param persistence The persistence to be closed.
     */
    public void closeDatabase(PreconfiguredPersistences persistence) {

        if (!activeDatabases.containsKey(persistence)) {
            LOGGER.warn("Cannot close persistence " + persistence + " - not active.");
            return;
        }
        if (!activeDatabases.get(persistence).isClosed()) {
            activeDatabases.get(persistence).commit();
        }
        activeDatabases.get(persistence).close();
        activeDatabases.remove(persistence);
    }

    /**
     * Close all opened databases and shut down service.
     * Make sure that dependent services are also closed.
     */
    public void closePersistenceService() {
        // close all databases
        for (DB db : activeDatabases.values()) {
            if (!db.isClosed()) {
                db.commit();
                db.close();
            }
        }
        // remove all active DBs
        activeDatabases = new HashMap<>();
    }

    /**
     * Enum with the preconfigured database persistences.
     * DEV Remark: Run Unit Test after adding an additional persistence configuration.
     */
    public enum PreconfiguredPersistences {
        WIKIDATA_SYNONYMY_BUFFER,
        WIKIDATA_HYPERNYMY_BUFFER,
        WIKIDATA_LABEL_LINK_BUFFER,
        WIKIDATA_ASK_BUFFER,

        DBPEDIA_LABEL_LINK_BUFFER,
        DBPEDIA_SYNONYMY_BUFFER,
        DBPEDIA_HYPERNYMY_BUFFER,

        ALOD_CLASSIC_SYONYMY_BUFFER,
        ALOD_CLASSIC_LABEL_URI_BUFFER,
        ALOD_CLASSIC_HYPERNYMY_ASK_BUFFER,
        ALOD_CLASSIC_HYPERNYM_BUFFER,

        ALOD_XL_SYONYMY_BUFFER,
        ALOD_XL_LABEL_URI_BUFFER,
        ALOD_XL_HYPERNYMY_ASK_BUFFER,
        ALOD_XL_HYPERNYM_BUFFER,

        WIKTIONARY_SYNONYMY_BUFFER,
        WIKTIONARY_HYPERNYMY_BUFFER,
        WIKTIONARY_ASK_BUFFER,
        WIKTIONARY_TRANSLATION_BUFFER,
        WIKTIONARY_TRANSLATION_OF_BUFFER,

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
                case ALOD_CLASSIC_HYPERNYMY_ASK_BUFFER:
                case ALOD_XL_HYPERNYMY_ASK_BUFFER:
                    return StringString.class;
                case ALOD_CLASSIC_LABEL_URI_BUFFER:
                case ALOD_XL_LABEL_URI_BUFFER:
                case ALOD_CLASSIC_HYPERNYM_BUFFER:
                case ALOD_XL_HYPERNYM_BUFFER:
                case BABELNET_SYNONYM_BUFFER:
                case BABELNET_HYPERNYMY_BUFFER:
                case BABELNET_SINGLE_CONCEPT_BUFFER:
                case BABELNET_MULTI_CONCEPT_BUFFER:
                case WIKIDATA_HYPERNYMY_BUFFER:
                case WIKIDATA_SYNONYMY_BUFFER:
                case WIKIDATA_LABEL_LINK_BUFFER:
                case WIKIDATA_ASK_BUFFER:
                case DBPEDIA_LABEL_LINK_BUFFER:
                case DBPEDIA_HYPERNYMY_BUFFER:
                case DBPEDIA_SYNONYMY_BUFFER:
                case WIKTIONARY_HYPERNYMY_BUFFER:
                case WIKTIONARY_SYNONYMY_BUFFER:
                case WIKTIONARY_ASK_BUFFER:
                case WIKTIONARY_TRANSLATION_BUFFER:
                case WIKTIONARY_TRANSLATION_OF_BUFFER:
                    return String.class;
            }
            return null;
        }

        public GroupSerializer getKeySerializer() {
            switch (this) {
                case ALOD_CLASSIC_SYONYMY_BUFFER:
                case ALOD_XL_SYONYMY_BUFFER:
                case ALOD_CLASSIC_HYPERNYMY_ASK_BUFFER:
                case ALOD_XL_HYPERNYMY_ASK_BUFFER:
                    return Serializer.JAVA;
                case ALOD_CLASSIC_LABEL_URI_BUFFER:
                case WIKIDATA_HYPERNYMY_BUFFER:
                case WIKIDATA_SYNONYMY_BUFFER:
                case ALOD_XL_LABEL_URI_BUFFER:
                case BABELNET_SYNONYM_BUFFER:
                case BABELNET_HYPERNYMY_BUFFER:
                case BABELNET_SINGLE_CONCEPT_BUFFER:
                case BABELNET_MULTI_CONCEPT_BUFFER:
                case WIKIDATA_LABEL_LINK_BUFFER:
                case WIKIDATA_ASK_BUFFER:
                case ALOD_CLASSIC_HYPERNYM_BUFFER:
                case ALOD_XL_HYPERNYM_BUFFER:
                case DBPEDIA_LABEL_LINK_BUFFER:
                case DBPEDIA_SYNONYMY_BUFFER:
                case DBPEDIA_HYPERNYMY_BUFFER:
                case WIKTIONARY_HYPERNYMY_BUFFER:
                case WIKTIONARY_SYNONYMY_BUFFER:
                case WIKTIONARY_ASK_BUFFER:
                case WIKTIONARY_TRANSLATION_BUFFER:
                case WIKTIONARY_TRANSLATION_OF_BUFFER:
                    return Serializer.STRING;
            }
            return null;
        }

        public GroupSerializer getValueSerializer() {
            switch (this) {
                case ALOD_CLASSIC_SYONYMY_BUFFER:
                case ALOD_XL_SYONYMY_BUFFER:
                case ALOD_CLASSIC_HYPERNYMY_ASK_BUFFER:
                case ALOD_XL_HYPERNYMY_ASK_BUFFER:
                case WIKIDATA_ASK_BUFFER:
                case WIKTIONARY_ASK_BUFFER:
                    return Serializer.BOOLEAN;
                case BABELNET_SYNONYM_BUFFER:
                case BABELNET_HYPERNYMY_BUFFER:
                case BABELNET_MULTI_CONCEPT_BUFFER:
                case WIKIDATA_HYPERNYMY_BUFFER:
                case WIKIDATA_SYNONYMY_BUFFER:
                case WIKIDATA_LABEL_LINK_BUFFER:
                case ALOD_CLASSIC_HYPERNYM_BUFFER:
                case ALOD_XL_HYPERNYM_BUFFER:
                case DBPEDIA_LABEL_LINK_BUFFER:
                case DBPEDIA_SYNONYMY_BUFFER:
                case DBPEDIA_HYPERNYMY_BUFFER:
                case WIKTIONARY_HYPERNYMY_BUFFER:
                case WIKTIONARY_SYNONYMY_BUFFER:
                case WIKTIONARY_TRANSLATION_BUFFER:
                case WIKTIONARY_TRANSLATION_OF_BUFFER:
                    return Serializer.JAVA;
                case BABELNET_SINGLE_CONCEPT_BUFFER:
                case ALOD_CLASSIC_LABEL_URI_BUFFER:
                case ALOD_XL_LABEL_URI_BUFFER:
                    return Serializer.STRING;
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
                    return persistenceDirectory + File.separator + "webisalod_classic_synonymy_buffer.mapdb";
                case ALOD_XL_SYONYMY_BUFFER:
                    return persistenceDirectory + File.separator + "webisalod_xl_synonymy_buffer.mapdb";
                case ALOD_CLASSIC_LABEL_URI_BUFFER:
                    return persistenceDirectory + File.separator + "webisalod_classic_label_uri_buffer.mapdb";
                case ALOD_XL_LABEL_URI_BUFFER:
                    return persistenceDirectory + File.separator + "webisalod_xl_label_uri_buffer.mapdb";
                case ALOD_CLASSIC_HYPERNYMY_ASK_BUFFER:
                    return persistenceDirectory + File.separator + "webisalod_classic_hypernymy_buffer.mapdb";
                case ALOD_XL_HYPERNYMY_ASK_BUFFER:
                    return persistenceDirectory + File.separator + "webisalod_xl_hypernymy_buffer.mapdb";
                case BABELNET_SYNONYM_BUFFER:
                    return persistenceDirectory + File.separator + "babelnet_synonym_buffer.mapdb";
                case BABELNET_HYPERNYMY_BUFFER:
                    return persistenceDirectory + File.separator + "babelnet_hypernymy_buffer.mapdb";
                case BABELNET_SINGLE_CONCEPT_BUFFER:
                    return persistenceDirectory + File.separator + "babelnet_single_concept_buffer.mapdb";
                case BABELNET_MULTI_CONCEPT_BUFFER:
                    return persistenceDirectory + File.separator + "babelnet_multi_concept_buffer.mapdb";
                case WIKIDATA_SYNONYMY_BUFFER:
                    return persistenceDirectory + File.separator + "wikidata_synonymy_buffer.mapdb";
                case WIKIDATA_HYPERNYMY_BUFFER:
                    return persistenceDirectory + File.separator + "wikidata_hypernymy_buffer.mapdb";
                case WIKIDATA_LABEL_LINK_BUFFER:
                    return persistenceDirectory + File.separator + "wikidata_label_link_buffer.mapdb";
                case WIKIDATA_ASK_BUFFER:
                    return persistenceDirectory + File.separator + "wikidata_ask_buffer.mapdb";
                case ALOD_CLASSIC_HYPERNYM_BUFFER:
                    return persistenceDirectory + File.separator + "webisalod_classic_hypernym_buffer.mapdb";
                case ALOD_XL_HYPERNYM_BUFFER:
                    return persistenceDirectory + File.separator + "webisalod_xl_hypernym_buffer.mapdb";
                case DBPEDIA_LABEL_LINK_BUFFER:
                    return persistenceDirectory + File.separator + "dbpedia_label_link_buffer.mapdb";
                case DBPEDIA_SYNONYMY_BUFFER:
                    return persistenceDirectory + File.separator + "dbpedia_synonymy_buffer.mapdb";
                case DBPEDIA_HYPERNYMY_BUFFER:
                    return persistenceDirectory + File.separator + "dbpedia_hypernymy_buffer.mapdb";
                case WIKTIONARY_HYPERNYMY_BUFFER:
                    return persistenceDirectory + File.separator + "wiktionary_hypernymy_buffer.mapdb";
                case WIKTIONARY_SYNONYMY_BUFFER:
                    return persistenceDirectory + File.separator + "wiktionary_synonymy_buffer.mapdb";
                case WIKTIONARY_ASK_BUFFER:
                    return persistenceDirectory + File.separator + "wiktionary_ask_buffer.mapdb";
                case WIKTIONARY_TRANSLATION_BUFFER:
                    return persistenceDirectory + File.separator + "wiktionary_translation_buffer.mapdb";
                case WIKTIONARY_TRANSLATION_OF_BUFFER:
                    return persistenceDirectory + File.separator + "wiktionary_translation_of_buffer.mapdb";
            }
            return null;
        }
    }
}
