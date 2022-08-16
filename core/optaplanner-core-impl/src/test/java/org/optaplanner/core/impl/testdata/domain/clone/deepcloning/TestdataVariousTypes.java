package org.optaplanner.core.impl.testdata.domain.clone.deepcloning;

import java.util.UUID;

public final class TestdataVariousTypes {

    public boolean booleanValue = true;
    public byte byteValue = 1;
    public char charValue = 2;
    public short shortValue = 3;
    public int intValue = 4;
    public long longValue = 5;
    public float floatValue = 6;
    public double doubleValue = 7;

    public Boolean booleanRef = true;
    public Byte byteRef = 8;
    public Character charRef = 9;
    public Short shortRef = 10;
    public Integer intRef = 11;
    public Long longRef = 12L;
    public Float floatRef = 13f;
    public Double doubleRef = 14d;
    public UUID uuidRef = UUID.randomUUID();
    public String stringRef = uuidRef.toString();

}
