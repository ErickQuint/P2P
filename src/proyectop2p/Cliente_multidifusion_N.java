/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectop2p;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author erick
 */
public class Cliente_multidifusion_N implements Runnable {

    int pto, puerto;
    List<Integer> ListaPuertosSN;
    List<Integer> ListaPuertosN;
    List<String> ListaConectadosN;
    List<String> ListaTiemposN;
    Nodo nodo;
    Mensaje mensaje;
    boolean conexion;

    public Cliente_multidifusion_N(int puerto, Nodo nodo, Mensaje mensaje) {
        this.puerto = puerto;
        this.pto = 2000;
        ListaPuertosSN = new ArrayList<>();
        ListaPuertosN = new ArrayList<>();
        ListaConectadosN = new ArrayList<>();
        ListaTiemposN = new ArrayList<>();
        this.nodo = nodo;
        this.mensaje = mensaje;
        conexion = false;
    }

    @Override
    public void run() {
        escuchar();
    }

    private void escuchar() {
        try {
            String hhost = "228.1.1.10";
            SocketAddress remote = null;

            remote = new InetSocketAddress(hhost, 2000);

            NetworkInterface ni = NetworkInterface.getByName("lo");
            InetSocketAddress dir = new InetSocketAddress(pto);
            DatagramChannel s = DatagramChannel.open(StandardProtocolFamily.INET);
            s.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            s.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
            InetAddress group = InetAddress.getByName("228.1.1.10");
            s.join(group, ni);
            s.configureBlocking(false);
            s.socket().bind(dir);

            Selector sel = Selector.open();

            s.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            ByteBuffer b = ByteBuffer.allocate(11);
            byte[] bytes;

            int tiempo = 30;

            Tiempo t = new Tiempo(tiempo);

            Thread h = new Thread(t);
            h.start();

            while (true) {
                sel.select();
                Iterator<SelectionKey> it = sel.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey k = (SelectionKey) it.next();
                    it.remove();

                    if (k.isReadable()) {
                        DatagramChannel ch = (DatagramChannel) k.channel();
                        b.clear();

                        SocketAddress emisor = ch.receive(b);
                        InetSocketAddress d = (InetSocketAddress) emisor;

                        b.clear();
                        bytes = new byte[b.limit()];
                        b.get(bytes, 0, b.limit());
                        b.flip();

                        String[] datos = new String(bytes).split(" ");
                        int puerto = Integer.parseInt(datos[0]);
                        String opc = datos[1].trim();

                        if (opc.contains("S")) {
                            opc = "S";
                        }

                        if (t.getTiempo() > 0) {
                            System.out.println(t.getTiempo());
                            if (!ListaPuertosSN.contains(puerto) && opc.equals("S")) {
                                ListaPuertosSN.add(puerto);
                            }
                        } else if (t.getTiempo() == 0 && !conexion && !ListaPuertosSN.isEmpty()) {
                            int tam = ListaPuertosSN.size();

                            int n = (int) Math.floor(Math.random() * tam);

                            int sn = ListaPuertosSN.get(n);
                            System.out.println(sn);
                            nodo.setPto(sn);
                            nodo.Conectar();

                            while (!conexion) {
                                mensaje.mensaje += "Intendando conexion con: " + sn + "<br>";

                                if (nodo.DisponibilidadSN()) {
                                    nodo.CrearCarpeta(String.valueOf(this.puerto));
                                    nodo.setNC();
                                    conexion = true;
                                } else {
                                    ListaPuertosSN.remove(n);

                                    if (ListaPuertosSN.isEmpty()) {
                                        break;
                                    }

                                    sn = ListaPuertosSN.get(0);

                                    nodo.setPto(sn);
                                    nodo.Conectar();

                                    n = 0;
                                }
                            }

                            if (conexion) {
                                mensaje.mensaje += "<span style=\"color:green\"> Conectado con " + sn + "</span><br><br>";
                            } else {
                                mensaje.mensaje += "<span style=\"color:red\">No hay super nodos disponibles</span><br><br>";
                            }
                        }
                    }

                    if (k.isWritable() && conexion) {
                        nodo.ListarArchivos();

                        DatagramChannel ch = (DatagramChannel) k.channel();
                        b.clear();
                        String mensaje = puerto + " N" + nodo.getPto();
                        b.put(mensaje.getBytes());
                        b.flip();
                        ch.send(b, remote);
                    }

                    Thread.sleep(2000);
                }//while
            }
        } catch (Exception e) {
            e.printStackTrace();
        }//catch
    }

    public List<String> getListaTiemposN() {
        return ListaTiemposN;
    }

    public List<String> getListaConectadosN() {
        return ListaConectadosN;
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
