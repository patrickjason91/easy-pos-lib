package io.patrickjasonlim.easyposlib.android.socketprinter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.patrickjasonlim.easyposlib.base.EscPosPrinter;
import io.patrickjasonlim.easyposlib.base.EscPosPrinterException;
import io.patrickjasonlim.easyposlib.base.PrintCommandSet;

public class NetworkPrinterManager {

    public static final int STATUS_CONNECTED = 0;
    public static final int STATUS_NOT_CONNECTED = 1;
    public static final int STATUS_NET_EXCEPTION = 2;

    public static final int STATUS_CONNECTION_ERROR = 4;

    static final int PORT_DEFAULT = 9100;
    private static final String TAG = NetworkPrinterManager.class.getSimpleName();

    private ThreadPoolExecutor executor;

    public NetworkPrinterManager() {
        init();
    }

    public void init() {
        // Determine the number of cores on the device
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        // Construct thread pool passing in configuration options
        // int minPoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit,
        // BlockingQueue<Runnable> workQueue
        executor = new ThreadPoolExecutor(
                NUMBER_OF_CORES*2,
                NUMBER_OF_CORES*2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
    }

    public void queuePrint(String address, PrintCommandSet commandSet, ConnectionListener connListener) {
        try {
            InetAddress netAddress = InetAddress.getByName(address);

            SocketPrintRunnable socketRunnable = new SocketPrintRunnable(netAddress, commandSet, connListener);
            executor.execute(socketRunnable);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address", e);
        }
    }

    public void close() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    public interface ConnectionListener {
        void onConnected(int status);
        void onConnectionChange(int status);
    }

    static class SocketPrintRunnable implements Runnable {

        private static final int TIMEOUT_SOCKET_DEFAULT = 10000; // Down to 10 seconds
        private Socket socket;
        private InetAddress mAddress;
        private SocketPrinterInterface socketPrinterInterface;
        private EscPosPrinter mPrinter;

        private PrintCommandSet mCommandSet;
        private ConnectionListener mConnListener;

        public SocketPrintRunnable(InetAddress address, PrintCommandSet commandSet, ConnectionListener connListener) {
            mAddress = address;
            mCommandSet = commandSet;
            mConnListener = connListener;
        }

        @Override
        public void run() {
            socket = new Socket();
            InetSocketAddress address = new InetSocketAddress(mAddress, PORT_DEFAULT);
            int status;
            // 1. Connect to the printer using socket
            try {
                socket.connect(address, TIMEOUT_SOCKET_DEFAULT);

                if (socket.isConnected()) {
                    status = STATUS_CONNECTED;

                } else {
                    status = STATUS_NOT_CONNECTED;
                }
            } catch (IOException e) {
                e.printStackTrace();
                status = STATUS_NET_EXCEPTION;
            }
            // 2. Notify client on connected status
            if (mConnListener != null) {
                mConnListener.onConnected(status);
            }
            // 3. Handle printing
            if (status == STATUS_CONNECTED) {
                try {
                    socketPrinterInterface = new SocketPrinterInterface(socket.getOutputStream());
                    mPrinter = new EscPosPrinter(socketPrinterInterface);

                    mPrinter.printSet(mCommandSet);
                } catch (IOException | EscPosPrinterException e) {
                    e.printStackTrace();
                    if (mConnListener != null) {
                        mConnListener.onConnectionChange(STATUS_CONNECTION_ERROR);
                    }
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
