/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectop2p;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author erick
 */
public class Nodo {

    Cliente_multidifusion_N ClienteMulticastNodo;
    Cliente_RMI_N ClienteRMI;
    Servidor_de_flujo ServidorFlujo;
    int pto, aux, p;
    boolean primera;
    ID mio;
    Mensaje mensaje;
    File directorio;
    List<String> Nombres, ListaArchivos, NombresE, MD5E;
    List<Integer> UbicacionesE;

    Nodo(String IP, int puerto) throws InterruptedException {
        mio = new ID(IP, puerto);
        aux = 0;
        p = 0;
        primera = true;
        mensaje = new Mensaje();
        Nombres = new ArrayList<>();
        NombresE = new ArrayList<>();
        MD5E = new ArrayList<>();
        UbicacionesE = new ArrayList<>();
        ListaArchivos = new ArrayList<>();
        ClienteMulticastNodo = new Cliente_multidifusion_N(puerto, this, mensaje);
        ServidorFlujo = new Servidor_de_flujo(puerto);
        new Thread(ClienteMulticastNodo).start();
        new Thread(ServidorFlujo).start();
    }

    public Nodo getNodo() {
        return this;
    }

    public int getPto() {
        return pto;
    }

    public void setPto(int pto) {
        this.pto = pto;
    }

    public Mensaje getMensaje() {
        return mensaje;
    }

    public void Conectar() {
        ClienteRMI = new Cliente_RMI_N(pto);
        ClienteRMI.Conectar();
    }

    public boolean DisponibilidadSN() {
        boolean d = false;
        try {
            int num = ClienteRMI.stub.ConectadosN();

            if (num < 2) {
                d = true;
            }
        } catch (RemoteException ex) {
            Logger.getLogger(Nodo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return d;
    }

    public void setNC() {
        try {
            ClienteRMI.stub.NC('c');
            ClienteRMI.stub.setListaConectadsN(mio.getPuerto());
        } catch (RemoteException ex) {
            Logger.getLogger(Nodo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void CrearCarpeta(String puerto) {
        String NuevaCarpeta = "Carpetas/" + puerto;
        File carpeta = new File(NuevaCarpeta);
        carpeta.mkdir();

        directorio = carpeta;

        ServidorFlujo.setDirectorio(directorio);
    }

    public void ListarArchivos() {
        try {
            File[] archivos = this.directorio.listFiles();
            String nombre;
            int ubicacion = mio.getPuerto();

            int cant = archivos.length;

            if (aux > cant) {
                ListaArchivos.clear();

                for (int i = 0; i < cant; i++) {
                    ListaArchivos.add(archivos[i].getName());
                }

                int tam = ClienteRMI.stub.getNombre().size();
                int pos = 0;

                for (int i = 0; i < tam; i++, pos++) {
                    if (!ListaArchivos.contains(ClienteRMI.stub.getNombre().get(pos)) && ClienteRMI.stub.getListaUbicaciones().get(pos) == ubicacion) {
                        ClienteRMI.stub.remover(pos);
                        ClienteRMI.stub.setContarArchivos('d');
                        pos--;
                    }
                }

                if (cant == 0) {
                    primera = true;
                }
            }

            for (File archivo : archivos) {
                int tam = ClienteRMI.stub.getNombre().size();

                nombre = archivo.getName();

                MD5 md5 = new MD5(archivo);
                String m = md5.ObtenerMD5();

                if (cant > aux || aux == 0) {
                    if (!ClienteRMI.stub.ContieneArchivo(nombre, ubicacion)) {
                        ClienteRMI.stub.setNombre(0, nombre, 'a');
                        ClienteRMI.stub.setUbicacion(0, ubicacion, 'a');
                        ClienteRMI.stub.setMD5(0, m, 'a');
                        ClienteRMI.stub.setContarArchivos('i');
                    }
                } else {
                    for (int i = 0; i < tam; i++) {
                        if (tam <= ClienteRMI.stub.getListaMD5().size()) {
                            if (ClienteRMI.stub.getListaMD5().get(i).equals(m) && ClienteRMI.stub.getListaUbicaciones().get(i).equals(ubicacion)) {
                                ClienteRMI.stub.setNombre(i, nombre, 'r');
                                ClienteRMI.stub.setUbicacion(i, ubicacion, 'r');
                                ClienteRMI.stub.setMD5(i, m, 'r');
                                ClienteRMI.stub.setContarArchivos('i');
                            }
                        }
                    }
                }
            }

            aux = cant;
            if (cant > 0) {
                primera = false;
            }
        } catch (RemoteException ex) {
            Logger.getLogger(Nodo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Integer> buscar(String nombre) {
        MD5E.clear();
        UbicacionesE.clear();

        String nom = " ";
        String md5 = " ";

        try {
            MD5E = ClienteRMI.stub.BuscarMD5(nombre);
            
            if (MD5E.size() > 1) {
                NombresE = ClienteRMI.stub.getNombresEncontrados();

                String[] m = MD5E.toArray(new String[0]);

                md5 = (String) JOptionPane.showInputDialog(
                        null,
                        "Se tiene mas de un archivo\n"
                         + "Selecciona uno de los siguientes: ","MD5",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        m,m[0]);
                
                for (int i = 0; i < MD5E.size(); i++) {
                    if(MD5E.get(i).equals(md5)){
                        nom = NombresE.get(i);
                        break;
                    }
                }
            }

            UbicacionesE.clear();

            if (!nom.equals(" ")) {
                UbicacionesE = ClienteRMI.stub.BuscarUbicaciones(nom, md5);
            } else {
                UbicacionesE = ClienteRMI.stub.BuscarUbicaciones(nombre, MD5E.get(0));
            }

            setParticion(UbicacionesE.size());
        } catch (RemoteException ex) {
            Logger.getLogger(Nodo.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return UbicacionesE;
    }

    public int getParticion() {
        return p;
    }

    public void setParticion(int p) {
        this.p = p;
    }

    public File ObtenerDirectorio() {
        return directorio;

    }
}

class MD5 {

    File f;

    public MD5(File f) {
        this.f = f;
    }

    public String ObtenerMD5() {
        MessageDigest mdigest;
        String md5 = "";

        try {
            mdigest = MessageDigest.getInstance("MD5");
            md5 = checksum(mdigest, f);
        } catch (NoSuchAlgorithmException | IOException ex) {
            Logger.getLogger(MD5.class.getName()).log(Level.SEVERE, null, ex);
        }

        return md5;
    }

    private static String checksum(MessageDigest digest, File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);

        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        fis.close();

        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
