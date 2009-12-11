package org.drools.planner.examples.manners2009.domain;

/**
 * @author Geoffrey De Smet
 */
public enum JobType {
    POLITICIAN("Politician"),
    SOCIALITE("Socialite"),
    DOCTOR("Doctor"),
    SPORTS_STAR("Sports"),
    TEACHER("Teacher"),
    PROGRAMMER("Programmer");

    public static JobType valueOfCode(String code) {
        for (JobType jobType : values()) {
            if (code.equalsIgnoreCase(jobType.getCode())) {
                return jobType;
            }
        }
        return null;
    }

    private String code;

    private JobType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}