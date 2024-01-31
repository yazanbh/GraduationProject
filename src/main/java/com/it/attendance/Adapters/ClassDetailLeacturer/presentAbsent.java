package com.it.attendance.Adapters.ClassDetailLeacturer;

public class presentAbsent {
    String present,absent;

    public presentAbsent() {
    }
    public presentAbsent(String present, String absent) {
        this.present = present;
        this.absent = absent;
    }

    public String getPresent() {
        return present;
    }

    public void setPresent(String present) {
        this.present = present;
    }

    public String getAbsent() {
        return absent;
    }

    public void setAbsent(String absent) {
        this.absent = absent;
    }

}
