/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectop2p;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;

/**
 *
 * @author erick
 */
public class Actualiza_ventana implements Runnable {

    private VentanaSuperNodo venSN;
    private VentanaNodo venN;
    Nodo nodo;
    List<String> elementos;
    List<String> Archivos;
    DefaultListModel modelo = new DefaultListModel();

    public Actualiza_ventana(VentanaSuperNodo venSN, VentanaNodo venN) {
        this.venSN = venSN;
        this.venN = venN;
        Archivos = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (;;) {
            try {
                if (venSN != null) {
                    elementos = venSN.nodo.ClienteMulticast.getListaConectadosSN();
                    actualizar(venSN.ActivosListaSN, elementos);

                    elementos = venSN.nodo.ClienteMulticast.getListaTiemposSN();
                    actualizar(venSN.TiempoSN, elementos);

                    elementos = venSN.nodo.ClienteMulticast.getListaConectadosN();
                    actualizar(venSN.ActivosListaN, elementos);

                    elementos = venSN.nodo.ClienteMulticast.getListaTiemposN();
                    actualizar(venSN.TiempoN, elementos);
                    
                    int tam = venSN.nodo.ServidorRMI.getNombre().size();
                    Archivos.clear();
                    
                    for (int i = 0; i < tam; i++) {
                        Archivos.add("Nombre: " + venSN.nodo.ServidorRMI.getNombre().get(i) 
                                + " Ubicacion: " + venSN.nodo.ServidorRMI.getListaUbicaciones().get(i)
                                + " MD5: " + venSN.nodo.ServidorRMI.getListaMD5().get(i));
                    }
                    
                    actualizar(venSN.Archivos, Archivos);
                }

                if (venN != null) {
                    venN.jLabel1.setText("Conectado al Super Nodo: " + venN.nodo.getPto());
                    venN.MensajeLabel.setText("<html>" + venN.nodo.getMensaje().mensaje + "</html>");
                }

                Thread.sleep(500);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            } catch (RemoteException ex) {
                Logger.getLogger(Actualiza_ventana.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void actualizar(javax.swing.JList lista, List<String> elementos) {
        modelo = new DefaultListModel();
        lista.removeAll();

        for (String u : elementos) {
            modelo.addElement(u);
        }
        lista.setModel(modelo);
    }
}
