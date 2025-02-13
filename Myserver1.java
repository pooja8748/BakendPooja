import java.net.*;
import java.io.*;
import java.security.*;
import javax.crypto.Cipher;
import java.util.Base64;
import java.util.concurrent.*;

public class Myserver1 {
    private static final int PORT = 3333;
    private static KeyPair keyPair;

    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(PORT);
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Limit to 10 clients

        // Generate RSA key pair
        keyPair = generateRSAKeyPair();
        savePublicKey(keyPair.getPublic());

        System.out.println("Server is running on port " + PORT);

        while (true) {
            Socket s = ss.accept();
            System.out.println("New client connected: " + s.getInetAddress());
            executorService.submit(new ClientHandler(s, keyPair.getPrivate()));
        }
    }

    // Method to generate RSA key pair
    private static KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    // Save public key to a file
    private static void savePublicKey(PublicKey publicKey) throws IOException {
        try (FileWriter fw = new FileWriter("publicKey.txt")) {
            String encodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            fw.write(encodedKey);
        }
        System.out.println("Public key saved to publicKey.txt");
    }
}

class ClientHandler implements Runnable {
    private Socket s;
    private PrivateKey privateKey;

    public ClientHandler(Socket s, PrivateKey privateKey) {
        this.s = s;
        this.privateKey = privateKey;
    }

    public void run() {
        try {
            DataInputStream din = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            // Send public key file to client
            File publicKeyFile = new File("publicKey.txt");
            sendFile(dout, publicKeyFile);

            // Receive encrypted file from client
            receiveFile(din);

            din.close();
            dout.close();
            s.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to send a file to the client
    private void sendFile(DataOutputStream dout, File file) throws IOException {
        byte[] buffer = new byte[4096];

        dout.writeUTF(file.getName()); // Send file name
        dout.writeLong(file.length()); // Send file size

        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dout.write(buffer, 0, bytesRead);
            }
        }

        dout.flush();
        System.out.println("Public key sent to client.");
    }

    // Method to receive encrypted file and decrypt it
    private void receiveFile(DataInputStream din) throws IOException {
        String fileName = din.readUTF();
        long fileSize = din.readLong();
        File file = new File("received_" + fileName);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] buffer = new byte[4096];
        int bytesRead;
        long remaining = fileSize;

        while (remaining > 0 && (bytesRead = din.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
            fos.write(buffer, 0, bytesRead);
            remaining -= bytesRead;
        }
        fos.close();

        System.out.println("Encrypted file received. Decrypting...");

        // Read encrypted content
        String encryptedText = new String(java.nio.file.Files.readAllBytes(file.toPath()));

        // Decrypt the content
        String decryptedText = decryptRSA(encryptedText, privateKey);
        System.out.println("Decrypted text: \n" + decryptedText);
    }

    // Method to decrypt using RSA
    private static String decryptRSA(String encryptedText, PrivateKey privateKey) throws IOException {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new IOException("Decryption failed: " + e.getMessage());
        }
    }
}