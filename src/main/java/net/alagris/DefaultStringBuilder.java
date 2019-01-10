package net.alagris;

public class DefaultStringBuilder implements CargoBuilder<String>{

    private String str;
    
    @Override
    public String get() {
        return getStr();
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

}
