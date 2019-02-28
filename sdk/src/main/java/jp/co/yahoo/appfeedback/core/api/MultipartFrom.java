package jp.co.yahoo.appfeedback.core.api;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by taicsuzu on 2017/10/31.
 */

class MultipartFrom {
    protected static final String BOUNDARY = "AppFeedbackAndroidSDKBoundary";
    private HashMap<String, String> texts = new HashMap<>();
    private HashMap<String, Binary> binaries = new HashMap<>();

    class Binary {
        public byte[] bytes;
        public String contentType;

        public Binary(byte[] bytes, String contentType) {
            this.bytes = bytes;
            this.contentType = contentType;
        }
    }

    public void entryText(String key, String value) {
        texts.put(key, value);
    }

    public void entryBinary(String key, byte[] bytes, String contentType) {
        binaries.put(key, new Binary(bytes, contentType));
    }

    public byte[] makePostData() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            for (Map.Entry<String, String> entry : texts.entrySet()) {
                String key = entry.getKey();
                String text = entry.getValue();

                out.write(("--" + BOUNDARY + "\r\n").getBytes());
                out.write(("Content-Disposition: form-data;").getBytes());
                out.write(("name=\"" + key + "\"\r\n\r\n").getBytes());
                out.write((text + "\r\n").getBytes());
            }

            for (Map.Entry<String, Binary> entry: binaries.entrySet()) {
                String key = entry.getKey();
                Binary binary = entry.getValue();
                byte[] data = binary.bytes;

                out.write(("--" + BOUNDARY + "\r\n").getBytes());
                out.write(("Content-Disposition: form-data;").getBytes());
                out.write(("name=\"" + key + "\";").getBytes());
                out.write(("filename=\"" + key + "\"\r\n").getBytes());
                out.write(("Content-Type: "+binary.contentType+"\r\n\r\n").getBytes());
                out.write(data);
                out.write(("\r\n").getBytes());
            }

            out.write(("--" + BOUNDARY + "--\r\n").getBytes());

            return out.toByteArray();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                out.close();
            } catch (Exception e) {}
        }
    }
}
