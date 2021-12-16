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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FuncionesRMI extends Remote {
    void NC(char opc) throws RemoteException;
    int ConectadosN() throws RemoteException;
    void setListaConectadsN(int n) throws RemoteException;
    List<Integer> getListaConectadosN() throws RemoteException;
    void setCantidadArchivos(int cant) throws RemoteException;
    int getCantidadArchivos() throws RemoteException;
    void setContarArchivos(char opc) throws RemoteException;
    int getContarArchivos() throws RemoteException;
    void setNodoEliminado(int n) throws RemoteException;
    int getNodoEliminado() throws RemoteException;
    void eliminarNodo(int pos) throws RemoteException;
    int getPreviaCantidad() throws RemoteException;
    void setNombre(int pos, String nombre, char opc) throws RemoteException;
    List<String> getNombre() throws RemoteException;
    boolean ContieneArchivo(String nombre, int ubicacion) throws RemoteException;
    void setUbicacion(int pos, int ubicacion, char opc) throws RemoteException;
    List<Integer> getListaUbicaciones() throws RemoteException;
    void setMD5(int pos, String md5, char opc) throws RemoteException;
    List<String> getListaMD5() throws RemoteException;
    List<String> BuscarMD5(String nombre) throws RemoteException;
    List<Integer> BuscarUbicaciones(String nombre, String md5) throws RemoteException;
    List<String> getNombresEncontrados() throws RemoteException;
    void remover(int p) throws RemoteException;
}
