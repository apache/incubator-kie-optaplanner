package org.drools.solver.examples.common.domain;

import java.io.Serializable;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractPersistable implements Serializable {

    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

// This part is currently commented out because it's probably a bad thing to mix identification with equality

//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (id == null || !(o instanceof AbstractPersistable)) {
//            return false;
//        } else {
//            AbstractPersistable other = (AbstractPersistable) o;
//            return id.equals(other.id);
//        }
//    }
//
//    public int hashCode() {
//        if (id == null) {
//            return super.hashCode();
//        } else {
//            return id.hashCode();
//        }
//    }

//    public int compareTo(AbstractPersistable abstractPersistable) {
//        return id.compareTo(abstractPersistable.id);
//    }

    public String toString() {
        return "[" + getClass().getName().replaceAll(".*\\.", "") + "-"+ id + "]";
    }

}
