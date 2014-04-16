package gov.epa.emissions.commons.io;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class CustomCharSetInputStreamReader extends InputStreamReader {

    public CustomCharSetInputStreamReader(FileInputStream stream) throws UnsupportedEncodingException {
        super(stream, "Latin9");
    }

    public CustomCharSetInputStreamReader(FileInputStream stream, String charSet) throws UnsupportedEncodingException {
        super(stream, charSet);
    }
}
