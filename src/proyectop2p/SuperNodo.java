/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectop2p;

import java.io.File;

/**
 *
 * @author erick
 */
public class SuperNodo {

    Cliente_multidifusion_SN ClienteMulticast;
    Cliente_RMI_SN ClienteRMI;
    private Servidor_multidifusion ServidorMulticast;
    Servidor_RMI ServidorRMI;
    ID mio;

    SuperNodo(String IP, int puerto) throws InterruptedException {
        mio = new ID(IP, puerto);
        ServidorMulticast = new Servidor_multidifusion(puerto);
        ClienteMulticast = new Cliente_multidifusion_SN(this);
        ServidorRMI = new Servidor_RMI(this);
        ClienteRMI = new Cliente_RMI_SN(puerto, ClienteMulticast);

        new Thread(ServidorMulticast).start();
        new Thread(ClienteMulticast).start();
        new Thread(ServidorRMI).start();
        new Thread(ClienteRMI).start();
    }
}
