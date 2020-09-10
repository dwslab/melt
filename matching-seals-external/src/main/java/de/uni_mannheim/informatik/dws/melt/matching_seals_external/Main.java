package de.uni_mannheim.informatik.dws.melt.matching_seals_external;

import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is used as wrapper for the SEALS external matcher build process.
 * It follows the MELT ExternalMatcher communication protocol.
 */
public class Main {

    public static void main(String[] args) {
        if(args.length != 3 || args.length == 4){
            System.err.println("Did not get an implementing class and 2 or 3 input URLs.");
            return;
        }
        System.err.println("The following parameters have been received: " + String.join(" | ", args));
        IOntologyMatchingToolBridge matcher = getBridge(args[0]);
        if (matcher == null) {
            return;
        }
        try {
            if (args.length == 3) {
                System.out.println(matcher.align(new URL(args[1]), new URL(args[2])));
            } else if (args.length == 4) {
                System.out.println(matcher.align(new URL(args[1]), new URL(args[2]), new URL(args[3])));
            }
        } catch (MalformedURLException ex) {
            System.err.println("Could not create URL. " + ex.getMessage());
        } catch (ToolBridgeException ex) {
            System.err.println("ToolBridgeException: " + ex.getMessage());
        }
    }

    private static IOntologyMatchingToolBridge getBridge(String implementingClass) {
        implementingClass = implementingClass.replace("\r", "").replace("\n", "").trim();
        if(implementingClass.length() == 0){
             System.err.println("The name of the implementing class is empty");
             return null;
        }
        IOntologyMatchingToolBridge bridge;
        try {
            Class clazz = Class.forName(implementingClass);
            bridge = (IOntologyMatchingToolBridge) clazz.newInstance();
        } catch (ClassNotFoundException ex) {
            System.err.println("Could not find class " + implementingClass);
            return null;
        } catch (InstantiationException ex) {
            System.err.println("Could not instantiate class " + implementingClass);
            return null;
        } catch (IllegalAccessException ex) {
            System.err.println("Could not access class " + implementingClass);
            return null;
        }
        return bridge;
    }
}