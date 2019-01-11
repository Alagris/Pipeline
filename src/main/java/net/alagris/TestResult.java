package net.alagris;

public class TestResult {

    private boolean passed;

    private String expected;

    private String found;

    public TestResult(boolean passed, String expected, String found) {
        this.passed = passed;
        this.expected = expected;
        this.found = found;
    }

    public static TestResult pass() {
        return new TestResult(true, null, null);
    }

    public static TestResult fail(String expected, String found) {
        return new TestResult(false, expected, found);
    }

    public static void equals(Object expected, Object found) {
        if (!expected.equals(found))
            throw new TestAssertionException(fail(expected.toString(), found.toString()));
    }
    
    public static void equals(int expected, int found) {
        if (expected != found)
            throw new TestAssertionException(fail(Integer.toString(expected), Integer.toString(found)));
    }

    public static void equals(float expected, float found,float delta) {
        if (Math.abs(expected - found)>delta)
            throw new TestAssertionException(fail(Float.toString(expected), Float.toString(found)));
    }
    
    public static void equals(double expected, double found,double delta) {
        if (Math.abs(expected - found)>delta)
            throw new TestAssertionException(fail(Double.toString(expected), Double.toString(found)));
    }
    
    public static void equals(long expected, long found) {
        if (expected != found)
            throw new TestAssertionException(fail(Long.toString(expected), Long.toString(found)));
    }
    
    public static void equals(short expected, short found) {
        if (expected != found)
            throw new TestAssertionException(fail(Short.toString(expected), Short.toString(found)));
    }
    
    public static void equals(byte expected, byte found) {
        if (expected != found)
            throw new TestAssertionException(fail(Byte.toString(expected), Byte.toString(found)));
    }
    
    public static void equals(char expected, char found) {
        if (expected != found)
            throw new TestAssertionException(fail(Character.toString(expected), Character.toString(found)));
    }
    
    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getFound() {
        return found;
    }

    public void setFound(String found) {
        this.found = found;
    }
}
