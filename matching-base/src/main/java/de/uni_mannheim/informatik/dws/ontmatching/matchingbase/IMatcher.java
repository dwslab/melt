package de.uni_mannheim.informatik.dws.ontmatching.matchingbase;

import java.util.Properties;

/**
 * Generic Matcher Interface.
 * @param <Alignment> Alignment Class Type.
 * @param <Model> Model Class Type.
 */
public interface IMatcher <Alignment, Model>{
    Alignment match(Model source, Model target, Alignment input, Properties p) throws Exception;
}