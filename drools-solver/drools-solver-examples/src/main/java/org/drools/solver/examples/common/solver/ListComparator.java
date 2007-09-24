package org.drools.solver.examples.common.solver;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author Geoffrey De Smet
*/
public class ListComparator implements Comparator<List<? extends Comparable>> {

    public int compare(List<? extends Comparable> a, List<? extends Comparable> b) {
        if (a == b) {
            return 0;
        } else if (a.size() < b.size()) {
            return -1;
        } else if (a.size() > b.size()) {
            return 1;
        } else {
            Iterator<? extends Comparable> aIt = a.iterator();
            Iterator<? extends Comparable> bIt = b.iterator();
            while (aIt.hasNext()) {
                int compared = aIt.next().compareTo(bIt.next());
                if (compared != 0) {
                    return compared;
                }
            }
            return 0;
        }
    }
}
