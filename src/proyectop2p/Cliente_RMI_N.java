/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectop2p;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author erick
 */
public class Cliente_RMI_N {
    int pto;
    FuncionesRMI stub;
    Registry registry;
    
    public Cliente_RMI_N(int pto) {
        this.pto = pto;
    }
       
    public void Conectar(){
        try {
            registry = LocateRegistry.getRegistry(pto);
            stub = (FuncionesRMI) registry.lookup("FuncionesRMI");
            
        } catch (Exception e) {
            System.err.println("Excepción del cliente: " +e.toString());
            e.printStackTrace();
        }
    }
}
