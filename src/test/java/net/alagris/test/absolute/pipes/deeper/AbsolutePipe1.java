package net.alagris.test.absolute.pipes.deeper;

import net.alagris.Output;
import net.alagris.Pipe;

public class AbsolutePipe1 implements Pipe<String> {

    @Override
    public void close() throws Exception {
        
    }

    @Override
    public void onLoad() throws Exception {
        
    }

    @Override
    public Output<String> process(String input) throws Exception {
        return Output.none(input+" deeper1");
    }

}
