package by.bsuir;

import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class App {

    public static void main(String[] args) throws IOException, URISyntaxException {
        ProxyServer proxyServer = new ProxyServer();
        proxyServer.start();

//        String s = String.valueOf('\uD800');
//        System.out.println(s);
//        OutputStream outputStream = new FileOutputStream(new File(App.class.getResource("/test.txt").toURI()));
//        outputStream.write('a');
//
//        outputStream.write(0b11101101);
//        outputStream.write(0b10100000);
//        outputStream.write(0b10000000);
//
//        outputStream.write('b');
//        outputStream.close();

//        char c = 'б';
//        System.out.println((int) c);
//        char a = (char) 3626818929L;
//        System.out.println((int) a);
//        System.out.println((Character.toCodePoint((char) 55340, (char) 56689)));
//        System.out.println();
        /*
        Смотри, есть Unicode Code Points, это лишь числа, абстрация. В памяти компьютера эту абстрацию надо как-то
        выразить, то есть закодировать. То, как это кодируется, определяется кодировкой. В Java char есть
        16-bit тип. Содержимое этих 16 bit интерпретируется кодировкой UTF-16.
         */

//        InputStream inputStream = App.class.getResourceAsStream("/test.txt");
//        int buffer;
//        while ((buffer = inputStream.read()) != -1) {
//            System.out.print(buffer + " ");
//        }
//        inputStream.close();

//        BufferedReader br = new BufferedReader(new InputStreamReader(App.class.getResourceAsStream("/test.txt"), StandardCharsets.UTF_8));
//        int buffer;
//        while ((buffer = br.read()) != -1) {
//            System.out.print(buffer + " ");
//        }
//        br.close();

        /*
        Я могу на основе кодировки прочитать какую-то единицу данных. Затем на основе кодировки я могу эту единицу
        данных преоброзовать в какое-то число. Затем на основе этого числа и какой-то таблицы я могу получить
        символ.
        Потерпеть неудачу я могу
        1) если единица данных окажется невалидной в контексте рассматриваемой кодировки
        2) если полученное число не определено в моей таблице
         */

//        ServerSocket serverSocket = new ServerSocket(53138);
//        Socket socket = serverSocket.accept();
//
//        InputStream inputStream = socket.getInputStream();
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//        String line;
//        while ((line = bufferedReader.readLine()) != null) {
//            System.out.println(line);
//        }


//        InputStream inputStream = socket.getInputStream();
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//        StringBuilder stringBuilder = new StringBuilder();
//        String line;
//        while (!(line = bufferedReader.readLine()).isEmpty()) {
//            stringBuilder.append(line).append((char) 13).append((char) 10);
//        }
//        String string = stringBuilder.append((char) 13).append((char) 10).toString();
//        System.out.println(string);
//        String[] split = string.split(" ");
//        String strUrl = split[1];
//        URL url = new URL(strUrl);
//        String host = url.getHost();
//        InetAddress inetAddress = InetAddress.getByName(host);
//        System.out.println("DONE");
//        Socket connection = new Socket(inetAddress, 8000);
//        System.out.println("DONE");
//        OutputStream outputStream = connection.getOutputStream();
//        outputStream.write(string.getBytes(StandardCharsets.US_ASCII));
//        outputStream.flush();
//        System.out.println("DONE");
//        InputStream stream = connection.getInputStream();
//        stream.transferTo(socket.getOutputStream());
    }
}
