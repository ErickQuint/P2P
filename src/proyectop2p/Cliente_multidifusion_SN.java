/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectop2p;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author erick
 */
public class Cliente_multidifusion_SN implements Runnable {

    static private List<String> ListaConectadosSN;
    List<Integer> ListaPuertosSN;
    static private List<String> ListaTiemposSN;
    static private List<String> ListaConectadosN;
    List<Integer> ListaPuertosN;
    static private List<String> ListaTiemposN;
    FuncionesRMI stub;
    Registry registry;
    SuperNodo sn;

    public Cliente_multidifusion_SN(SuperNodo sn) {
        ListaConectadosSN = new ArrayList<>();
        ListaTiemposSN = new ArrayList<>();
        ListaPuertosSN = new ArrayList<>();
        ListaConectadosN = new ArrayList<>();
        ListaTiemposN = new ArrayList<>();
        ListaPuertosN = new ArrayList<>();
        this.sn = sn;
    }

    @Override
    public void run() {
        escuchar();
    }

    private void escuchar() {
        try {
            NetworkInterface ni = NetworkInterface.getByName("lo");
            InetSocketAddress dir = new InetSocketAddress(2000);
            DatagramChannel s = DatagramChannel.open(StandardProtocolFamily.INET);
            s.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            s.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
            InetAddress group = InetAddress.getByName("228.1.1.10");
            s.join(group, ni);
            s.configureBlocking(false);
            s.socket().bind(dir);

            Selector sel = Selector.open();

            s.register(sel, SelectionKey.OP_READ);

            ByteBuffer b = ByteBuffer.allocate(11);
            byte[] bytes;
            Tiempo t[] = new Tiempo[100];
            Tiempo t2[] = new Tiempo[100];
            int i = 0, j = 0;

            while (true) {
                sel.select();
                Iterator<SelectionKey> it = sel.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey k = (SelectionKey) it.next();
                    it.remove();

                    if (k.isReadable()) {
                        DatagramChannel ch = (DatagramChannel) k.channel();

                        SocketAddress emisor = ch.receive(b);
                        InetSocketAddress d = (InetSocketAddress) emisor;
                        b.clear();
                        bytes = new byte[b.limit()];
                        b.get(bytes, 0, b.limit());
                        b.flip();

                        String[] datos = new String(bytes).split(" ");
                        int puerto = Integer.parseInt(datos[0].trim());
                        String opc = datos[1].trim();

                        if (opc.contains("S")) {
                            opc = "S";
                        }

                        String info = d.getHostString() + ":" + puerto;

                        if (opc.equals("S")) {
                            int tpo = 30;

                            if (!ListaPuertosSN.contains(puerto) && puerto != sn.mio.getPuerto()) {
                                ListaConectadosSN.add(info);
                                ListaPuertosSN.add(puerto);

                                t[i] = new Tiempo(tpo);

                                ListaTiemposSN.add(String.valueOf(t[i].getTiempo()));

                                Thread h = new Thread(t[i]);
                                h.start();
                                i++;
                            } else if (ListaPuertosSN.contains(puerto)) {
                                int pos = ListaPuertosSN.indexOf(puerto);

                                t[pos].setTiempo(tpo);
                            }

                            for (int l = 0; l < ListaTiemposSN.size(); l++) {
                                int tiempo = t[l].getTiempo();

                                if (tiempo == 0) {
                                    ListaConectadosSN.remove(l);
                                    ListaPuertosSN.remove(l);
                                    ListaTiemposSN.remove(l);
                                    i--;
                                } else {
                                    ListaTiemposSN.set(l, String.valueOf(tiempo));
                                }
                            }
                        }

                        if (opc.equals("N" + sn.mio.getPuerto())) {
                            int tpo = 15;
                            if (!ListaPuertosN.contains(puerto)) {
                                ListaConectadosN.add(info);
                                ListaPuertosN.add(puerto);

                                t2[j] = new Tiempo(tpo);

                                ListaTiemposN.add(String.valueOf(t2[j].getTiempo()));

                                Thread h = new Thread(t2[j]);
                                h.start();
                                j++;
                            } else if (ListaPuertosN.contains(puerto)) {
                                int pos = ListaPuertosN.indexOf(puerto);

                                t2[pos].setTiempo(tpo);
                            }
                        }

                        if (!ListaTiemposN.isEmpty()) {
                            for (int l = 0; l < ListaTiemposN.size(); l++) {
                                int tiempo = t2[l].getTiempo();

                                if (tiempo == 0) {
                                    ListaConectadosN.remove(l);

                                    int tam = sn.ServidorRMI.getListaUbicaciones().size();
                                    int u = 0;

                                    for (int p = 0; p < tam; p++, u++) {
                                        if (Objects.equals(sn.ServidorRMI.getListaUbicaciones().get(u), ListaPuertosN.get(l))) {
                                            sn.ServidorRMI.ListaNombres.remove(u);
                                            sn.ServidorRMI.ListaUbicaciones.remove(u);
                                            sn.ServidorRMI.ListaMD5.remove(u);

                                            /*String NuevaCarpeta = "Carpetas/" + ListaPuertosN.get(l);
                                            File carpeta = new File(NuevaCarpeta);

                                            EliminarCarpeta(carpeta);*/
                                            u--;
                                        }
                                    }
                                    
                                    /*int nodo = ListaPuertosN.get(l);
                                    tam = sn.ServidorRMI.getListaConectadosN().size();
                                    
                                    for (int p = 0; p < tam; p++) {
                                        if(sn.ServidorRMI.getListaConectadosN().get(p) == nodo)
                                            sn.ServidorRMI.ListaNodos.remove(p);
                                    }*/
                                    
                                    sn.ServidorRMI.setNodoEliminado(ListaPuertosN.get(l));

                                    ListaPuertosN.remove(l);
                                    ListaTiemposN.remove(l);
                                    sn.ServidorRMI.NC('d');
                                    j--;
                                } else {
                                    ListaTiemposN.set(l, String.valueOf(tiempo));
                                }
                            }
                        }
                    }
                }//while
            }
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    public static List<String> getListaConectadosSN() {
        return ListaConectadosSN;
    }

    public static List<String> getListaTiemposSN() {
        return ListaTiemposSN;
    }

    public static List<String> getListaConectadosN() {
        return ListaConectadosN;
    }

    public static List<String> getListaTiemposN() {
        return ListaTiemposN;
    }

    private void EliminarCarpeta(File carpeta) {
        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File f : archivos) {
                EliminarCarpeta(f);
            }
        }
        carpeta.delete();
    }

    static class Tiempo implements Runnable {

        int t;

        public Tiempo(int t) {
            this.t = t;
        }

        void setTiempo(int t) {
            this.t = t;
        }

        int getTiempo() {
            return this.t;
        }

        @Override
        public void run() {
            try {
                while (t > 0) {
                    t--;

                    Thread.sleep(1000);
                }
            } catch (Exception e) {
            }
        }
    }
}
