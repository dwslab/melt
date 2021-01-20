package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.dataStructures;

import java.io.Serializable;

/**
 * Simple data structure representing a String Tuple.
 */
public class StringString implements Serializable {

    public String string1;
    public String string2;

    public StringString(String string1, String string2) {
        this.string1 = string1;
        this.string2 = string2;
    }

    @Override
    public int hashCode() {
        return 2 + string1.hashCode() + string2.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != this.getClass()) {
            return false;
        }
        StringString other = (StringString) o;

        if (
                (other.string1.equals(this.string1) || other.string1.equals(this.string2)) &&
                        (other.string2.equals(this.string2) || other.string2.equals(this.string1))
        ) return true;

        return false;
    }

}
