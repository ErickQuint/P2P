/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectop2p;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author erick
 */
public class Cliente_RMI_SN implements Runnable {

    int pto, ubicacion, cta, cont, cant, aux;
    String nombre, md5;
    Cliente_multidifusion_SN ClienteMulticast;
    FuncionesRMI stub, stub2;
    Registry registry, registry2;
    List<Integer> PuertosSN, PuertosN;
    List<String> ListaNombres;
    List<Integer> ListaUbicaciones, ListaUbicacionesAux;
    List<String> ListaMD5, ListaMD5Aux;

    public Cliente_RMI_SN(int pto, Cliente_multidifusion_SN ClienteMulticast) {
        this.pto = pto;
        this.ClienteMulticast = ClienteMulticast;
        PuertosSN = new ArrayList<>();
        PuertosN = new ArrayList<>();
        ListaNombres = new ArrayList<>();
        ListaUbicaciones = new ArrayList<>();
        ListaUbicacionesAux = new ArrayList<>();
        ListaMD5 = new ArrayList<>();
        ListaMD5Aux = new ArrayList<>();
        cta = 0;
        cont = 0;
        cant = 0;
        aux = 0;
    }

    public void run() {
        for (;;) {
            try {
                PuertosSN = ClienteMulticast.ListaPuertosSN;

                for (int p : PuertosSN) {
                    registry2 = LocateRegistry.getRegistry(p);
                    stub2 = (FuncionesRMI) registry2.lookup("FuncionesRMI");

                    ListaNombres.clear();
                    ListaUbicaciones.clear();
                    ListaMD5.clear();

                    ListaNombres = stub2.getNombre();
                    ListaUbicaciones = stub2.getListaUbicaciones();
                    ListaUbicacionesAux = stub2.getListaUbicaciones();
                    ListaMD5 = stub2.getListaMD5();

                    int tam = ListaUbicacionesAux.size();

                    ListaNombres.clear();
                    ListaUbicaciones.clear();
                    ListaMD5.clear();

                    cant = 0;

                    for (int i = 0; i < tam; i++) {
                        if (stub2.getContarArchivos() > 0) {
                            if (stub2.getListaConectadosN().contains(ListaUbicacionesAux.get(i))) {
                                ListaNombres.add(stub2.getNombre().get(i));
                                ListaUbicaciones.add(stub2.getListaUbicaciones().get(i));
                                ListaMD5.add(stub2.getListaMD5().get(i));
                                cant++;
                            }
                        }
                    }

                    registry = LocateRegistry.getRegistry(pto);
                    stub = (FuncionesRMI) registry.lookup("FuncionesRMI");

                    tam = stub.getNombre().size();
                    aux = 0;

                    ListaMD5Aux.clear();
                    ListaUbicacionesAux.clear();

                    for (int i = 0; i < tam; i++) {
                        for (int j = 0; j < stub2.getListaConectadosN().size(); j++) {
                            if (stub.getListaUbicaciones().get(i).equals(stub2.getListaConectadosN().get(j))) {
                                aux++;
                                ListaMD5Aux.add(stub.getListaMD5().get(i));
                                ListaUbicacionesAux.add(stub.getListaUbicaciones().get(i));
                            }
                        }

                        tam = stub.getNombre().size();
                    }

                    if (stub2.getListaConectadosN().size() > stub2.getPreviaCantidad()) {
                        int nodo = stub2.getNodoEliminado();
                        tam = stub2.getListaConectadosN().size();

                        for (int i = 0; i < tam; i++) {
                            if (stub2.getListaConectadosN().get(i) == nodo) {
                                stub2.eliminarNodo(i);
                            }
                        }
                    }

                    if (aux > cant) {
                        tam = ListaMD5Aux.size();
                        int pos;
                        boolean agregado = false;

                        for (int i = 0; i < tam; i++) {
                            for (int j = 0; j < ListaMD5.size(); j++) {
                                if (ListaMD5.get(j).equals(ListaMD5Aux.get(i))) {
                                    if (ListaUbicaciones.get(j).equals(ListaUbicacionesAux.get(i))) {
                                        agregado = true;
                                        break;
                                    }
                                }
                            }

                            if (!agregado) {
                                pos = 0;
                                for (int j = 0; j < stub.getListaMD5().size(); j++, pos++) {
                                    if (stub.getListaMD5().get(j).equals(ListaMD5Aux.get(i))) {
                                        if (stub.getListaUbicaciones().get(j).equals(ListaUbicacionesAux.get(i))) {
                                            stub.remover(pos);
                                            pos--;
                                        }
                                    }
                                }
                            }

                            agregado = false;
                        }
                    }

                    for (int i = 0; i < cant; i++) {
                        nombre = ListaNombres.get(i);
                        ubicacion = ListaUbicaciones.get(i);
                        md5 = ListaMD5.get(i);

                        tam = stub.getNombre().size();

                        if (cant > aux || aux == 0) {
                            if (!stub.ContieneArchivo(nombre, ubicacion)) {
                                stub.setNombre(0, nombre, 'a');
                                stub.setUbicacion(0, ubicacion, 'a');
                                stub.setMD5(0, md5, 'a');
                            }
                        } else {
                            for (int j = 0; j < tam; j++) {
                                if (tam <= stub.getListaMD5().size()) {
                                    if (stub.getListaMD5().get(j).equals(md5) && stub.getListaUbicaciones().get(j).equals(ubicacion)) {
                                        stub.setNombre(j, nombre, 'r');
                                        stub.setUbicacion(j, ubicacion, 'r');
                                        stub.setMD5(j, md5, 'r');
                                    }
                                }
                            }
                        }
                    }
                }
                Thread.sleep(4333);
            } catch (Exception e) {
                System.err.println("Excepción del cliente: " + e.toString());
                e.printStackTrace();
            }
        }
    }
}
