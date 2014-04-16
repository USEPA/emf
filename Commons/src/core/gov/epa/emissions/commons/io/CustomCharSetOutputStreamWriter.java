package gov.epa.emissions.commons.io;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class CustomCharSetOutputStreamWriter extends OutputStreamWriter {

    public CustomCharSetOutputStreamWriter(OutputStream out) throws UnsupportedEncodingException {
        super(out, "Latin9");
    }

    public CustomCharSetOutputStreamWriter(OutputStream out, String charSet) throws UnsupportedEncodingException {
        super(out, charSet);
    }

}
