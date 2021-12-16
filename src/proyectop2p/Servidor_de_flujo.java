/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectop2p;

/**
 *
 * @author erick
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor_de_flujo implements Runnable {

    private int puerto;
    private File directorio;

    public Servidor_de_flujo(int puerto) {
        this.puerto = puerto;
    }

    public void setDirectorio(File directorio) {
        this.directorio = directorio;
    }

    private File archivo(String nombre) {
        File ar = new File(directorio.getAbsolutePath() + "/" + nombre);

        return ar;
    }

    @Override
    public void run() {
        try {
            ServerSocket s = new ServerSocket(puerto);

            for (;;) {
                Socket cl = s.accept();
                System.out.println("Conexión establecida desde" + cl.getInetAddress() + ":" + cl.getPort());
                DataInputStream dis = new DataInputStream(cl.getInputStream());
                String nombre_archivo = dis.readUTF();
                System.out.println("Recibimos el archivo:" + nombre_archivo);

                DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                File f = archivo(nombre_archivo);

                long enviados = 0;
                long tam = f.length();

                dos.writeUTF(nombre_archivo);
                dos.flush();
                dos.writeLong(tam);
                dos.flush();
                dos.writeInt(puerto);
                dos.flush();

                tam = dis.readLong();
                enviados = dis.readLong();

                int n = 0;
                
                RandomAccessFile file = new RandomAccessFile(f, "r");
                file.seek(enviados);

                byte[] b = new byte[(int)(tam-enviados)];
                
                while (enviados < tam) {
                    n = file.read(b);

                    dos.write(b, 0, n);
                    dos.flush();
                    enviados = enviados + n;
                }//While
                System.out.print("\n\nArchivo enviado");
                dos.close();
                dis.close();
                cl.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }
}
