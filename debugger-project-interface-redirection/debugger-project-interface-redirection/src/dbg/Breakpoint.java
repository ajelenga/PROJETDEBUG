package dbg;


import com.sun.jdi.request.BreakpointRequest;

public class Breakpoint {
    private String className;
    private int lineNumber;
    private int count;
    private BreakpointRequest breakpointRequest;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public BreakpointRequest getBreakpointRequest() {
        return breakpointRequest;
    }

    public void setBreakpointRequest(BreakpointRequest breakpointRequest) {
        this.breakpointRequest = breakpointRequest;
    }

    public void disableBreakpointRequest(){
        this.breakpointRequest.disable();
    }
    public void enableBreakpointRequest(){
        this.breakpointRequest.enable();
    }
}
