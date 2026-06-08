package com.example.billz;

public class Language {
    private String name;
    private String nativeName;
    private String code;

    public Language(String name, String nativeName, String code) {
        this.name = name;
        this.nativeName = nativeName;
        this.code = code;
    }

    public String getName() { return name; }
    public String getNativeName() { return nativeName; }
    public String getCode() { return code; }
}
