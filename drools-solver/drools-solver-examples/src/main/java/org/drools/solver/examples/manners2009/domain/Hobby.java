package org.drools.solver.examples.manners2009.domain;

/**
 * @author Geoffrey De Smet
 */
public enum Hobby {
    Tennis("1"),
    Golf("2"),
    Motorcycles("3"),
    Chess("4"),
    Poker("5");

    public static Hobby valueOfCode(String code) {
        for (Hobby hobby : values()) {
            if (code.equalsIgnoreCase(hobby.getCode())) {
                return hobby;
            }
        }
        return null;
    }
    
    private String code;

    private Hobby(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}