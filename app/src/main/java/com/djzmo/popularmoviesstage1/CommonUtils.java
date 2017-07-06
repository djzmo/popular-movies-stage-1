package com.djzmo.popularmoviesstage1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class CommonUtils {

    public static String readInputStream(InputStream in) {
        String result = null;

        try {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();

            String line;
            while((line = streamReader.readLine()) != null)
                sb.append(line);

            result = sb.toString();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
