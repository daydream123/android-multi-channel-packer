package com.zf.android.packer.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class IOUtils {

    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int EOF = -1;
    public static final char DIR_SEPARATOR_UNIX = '/';
    public static final char DIR_SEPARATOR_WINDOWS = '\\';

    public static final String LINE_SEPARATOR_UNIX = "\n";
    public static final String LINE_SEPARATOR_WINDOWS = "\r\n";

    public static BufferedReader toBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = null;

        byte[] var2;
        try {
            output = new ByteArrayOutputStream();
            copy((InputStream) input, (OutputStream) output);
            var2 = output.toByteArray();
        } finally {
            closeQuietly((OutputStream) output);
        }

        return var2;
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int) count;
    }

    public static void copy(InputStream input, Writer output) throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        copy((Reader) in, (Writer) output);
    }

    public static int copy(Reader input, Writer output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int) count;
    }

    public static long copyLarge(Reader input, Writer output) throws IOException {
        long count = 0L;
        boolean n = false;

        int n1;
        for (char[] buffer = new char[4096]; -1 != (n1 = input.read(buffer)); count += (long) n1) {
            output.write(buffer, 0, n1);
        }

        output.flush();
        return count;
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        long count = 0L;
        boolean n = false;

        int n1;
        for (byte[] buffer = new byte[4096]; (n1 = input.read(buffer)) != -1; count += (long) n1) {
            output.write(buffer, 0, n1);
        }

        output.flush();
        return count;
    }

    public static int read(Reader input, char[] buffer, int offset, int length) throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        } else {
            int remaining;
            int count;
            for (remaining = length; remaining > 0; remaining -= count) {
                int location = length - remaining;
                count = input.read(buffer, offset + location, remaining);
                if (-1 == count) {
                    break;
                }
            }

            return length - remaining;
        }
    }

    public static int read(Reader input, char[] buffer) throws IOException {
        return read((Reader) input, (char[]) buffer, 0, buffer.length);
    }

    public static int read(InputStream input, byte[] buffer, int offset, int length) throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        } else {
            int remaining;
            int count;
            for (remaining = length; remaining > 0; remaining -= count) {
                int location = length - remaining;
                count = input.read(buffer, offset + location, remaining);
                if (-1 == count) {
                    break;
                }
            }

            return length - remaining;
        }
    }

    public static int read(InputStream input, byte[] buffer) throws IOException {
        return read((InputStream) input, (byte[]) buffer, 0, buffer.length);
    }

    public static List<String> readLines(InputStream input) throws IOException {
        InputStreamReader reader = new InputStreamReader(input, Charset.defaultCharset().name());
        return readLines((Reader) reader);
    }

    public static List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = toBufferedReader(input);
        ArrayList list = new ArrayList();

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            list.add(line);
        }

        return list;
    }

    public static void write(byte[] data, OutputStream output) throws IOException {
        if (data != null && data.length > 0) {
            output.write(data);
            output.flush();
        }

    }

    public static void write(byte[] data, Writer output) throws IOException {
        if (data != null && data.length > 0) {
            output.write(new String(data));
            output.flush();
        }

    }

    public static void write(char[] data, Writer output) throws IOException {
        if (data != null && data.length > 0) {
            output.write(data);
            output.flush();
        }

    }

    public static void write(char[] data, OutputStream output) throws IOException {
        if (data != null && data.length > 0) {
            output.write((new String(data)).getBytes());
            output.flush();
        }

    }

    public static void write(String data, Writer output) throws IOException {
        if (data != null && data.length() > 0) {
            output.write(data);
            output.flush();
        }

    }

    public static void write(String data, OutputStream output) throws IOException {
        if (data != null && data.length() > 0) {
            output.write(data.getBytes());
            output.flush();
        }

    }

    public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream((InputStream) input1);
        }

        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream((InputStream) input2);
        }

        int ch2;
        for (int ch = ((InputStream) input1).read(); -1 != ch; ch = ((InputStream) input1).read()) {
            ch2 = ((InputStream) input2).read();
            if (ch != ch2) {
                return false;
            }
        }

        ch2 = ((InputStream) input2).read();
        return ch2 == -1;
    }

    public static boolean contentEquals(Reader input1, Reader input2) throws IOException {
        BufferedReader input11 = toBufferedReader(input1);
        BufferedReader input21 = toBufferedReader(input2);

        int ch2;
        for (int ch = input11.read(); -1 != ch; ch = input11.read()) {
            ch2 = input21.read();
            if (ch != ch2) {
                return false;
            }
        }

        ch2 = input21.read();
        return ch2 == -1;
    }

    public static boolean contentEqualsIgnoreEOL(Reader input1, Reader input2) throws IOException {
        BufferedReader br1 = toBufferedReader(input1);
        BufferedReader br2 = toBufferedReader(input2);
        String line1 = br1.readLine();

        String line2;
        for (line2 = br2.readLine(); line1 != null && line2 != null && line1.equals(line2); line2 = br2.readLine()) {
            line1 = br1.readLine();
        }

        return line1 == null ? line2 == null : line1.equals(line2);
    }

    public static void closeQuietly(HttpURLConnection conn) {
        if (conn != null) {
            conn.disconnect();
            conn = null;
        }

    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                closeable = null;
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }

    public static void closeQuietly(InputStream input) {
        if (input != null) {
            try {
                input.close();
                input = null;
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }

    public static void closeQuietly(OutputStream output) {
        if (output != null) {
            try {
                output.close();
                output = null;
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }

    public static void closeQuietly(Reader input) {
        if (input != null) {
            try {
                input.close();
                input = null;
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }

    public static void closeQuietly(Selector selector) {
        if (selector != null) {
            try {
                selector.close();
                selector = null;
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }

    public static void closeQuietly(ServerSocket socket) {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }

    public static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }

    public static void closeQuietly(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
                writer = null;
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }
}
