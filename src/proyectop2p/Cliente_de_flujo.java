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
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Cliente_de_flujo implements Runnable {

    private List<Integer> ubicaciones;
    private String nombre_ar;
    File directorio;
    Nodo nodo;

    public Cliente_de_flujo(Nodo nodo, List<Integer> ubicaciones, String nombre_ar, File directorio) {
        this.nodo = nodo;
        this.ubicaciones = ubicaciones;
        this.nombre_ar = nombre_ar;
        this.directorio = directorio;
    }

    @Override
    public void run() {
        try {
            if (ubicaciones.size() == 1) {
                Socket cl = new Socket("localhost", ubicaciones.get(0));
                DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                dos.writeUTF(nombre_ar);
                dos.flush();

                DataInputStream dis = new DataInputStream(cl.getInputStream());
                String nombre = dis.readUTF();
                long tam = dis.readLong();
                int puerto = dis.readInt();

                dos.writeLong(tam);
                dos.flush();
                dos.writeLong(0);
                dos.flush();

                DataOutputStream dos_archivo = new DataOutputStream(new FileOutputStream(directorio.getAbsolutePath() + "/" + nombre));
                long recibidos = 0;
                int n = 0;
                byte[] b = new byte[1024];
                
                while (recibidos < tam) {
                    n = dis.read(b);
                    dos_archivo.write(b, 0, n);
                    dos_archivo.flush();
                    recibidos = recibidos + n;
                }//While
                System.out.print("\n\nArchivo recibido.\n");
                dos.close();
                dis.close();
                dos_archivo.close();
                cl.close();
            } else {
                Socket cl = null;
                DataInputStream dis = null;
                DataOutputStream dos = null;
                String nombre = "";

                byte[] b = new byte[1024];
                List<Integer> m = new ArrayList<Integer>();
                DataOutputStream dos_archivo = null;

                for (int i = 0; i < ubicaciones.size(); i++) {
                    cl = new Socket("localhost", ubicaciones.get(i));
                    dos = new DataOutputStream(cl.getOutputStream());
                    dos.writeUTF(nombre_ar);
                    dos.flush();

                    dis = new DataInputStream(cl.getInputStream());
                    nombre = dis.readUTF();
                    long tam = dis.readLong();
                    int puerto = dis.readInt();

                    int p = nodo.getParticion();
                    int pos = 0;

                    if (ubicaciones.get(i) == puerto) {
                        pos = i + 1;
                    }

                    long t;
                    long r;
                   
                    t = ((tam / p) * pos);
                    r = ((tam / p) * i);
                    
                    nodo.mensaje.mensaje += "<br/>" + "Nodo: " + ubicaciones.get(i)
                            + " rango de bytes de descarga: " + r + "-" + t;

                    
                    if(i == 0)
                        dos_archivo = new DataOutputStream(new FileOutputStream(directorio.getAbsolutePath() + "/" + nombre));
                    
                    dos.writeLong(t);
                    dos.flush();
                    dos.writeLong(r);
                    dos.flush();

                    long recibidos = r;
                    int n = 0;

                    while (recibidos < t) {

                        n = dis.read(b);

                        dos_archivo.write(b, 0, n);
                        dos_archivo.flush();

                        recibidos = recibidos + n;
                    }//While

                    System.out.print("\n\nArchivo recibido.\n");

                }

                dos.close();
                dis.close();
                dos_archivo.close();
                cl.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
