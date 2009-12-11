package org.drools.planner.examples.manners2009.domain;

/**
 * @author Geoffrey De Smet
 */
public enum Hobby {
    TENNIS("1"),
    GOLF("2"),
    MOTORCYCLES("3"),
    CHESS("4"),
    POKER("5");

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