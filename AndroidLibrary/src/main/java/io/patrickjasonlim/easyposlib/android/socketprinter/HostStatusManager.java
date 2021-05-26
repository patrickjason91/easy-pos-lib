package io.patrickjasonlim.easyposlib.android.socketprinter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HostStatusManager {

    private static final String TAG = HostStatusManager.class.getSimpleName();
    private ThreadPoolExecutor executor;
    private OnHostStatusCheckListener mHostStatusListener;
    private List<NetworkPrinter> mPrinters;

    private Handler mainHandler;

    public HostStatusManager(List<NetworkPrinter> printers, OnHostStatusCheckListener hostStatusListener) {
        mHostStatusListener = hostStatusListener;
        mPrinters = printers;

        mainHandler = new Handler(Looper.getMainLooper());
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

    public void checkAllPrinterStatusAsync() {
        for (NetworkPrinter printer: mPrinters) {
            checkHostStatusAsync(printer);
        }
    }

    public void checkHostStatusAsync(NetworkPrinter printer) {

        StatusSocketRunnable socketRunnable = new StatusSocketRunnable(printer);
        executor.execute(socketRunnable);
    }

    public void close() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
        executor = null;
    }

    private void notifyHostStatus(final NetworkPrinter printer, final boolean status) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "notifyHostStatus() -> printer: " + printer.name + ", status: " + status);

                if (mHostStatusListener !=  null) {
                    mHostStatusListener.onStatusChanged(printer, status ? 1 : 0);
                }
            }
        });
    }

    public interface OnHostStatusCheckListener {
        void onStatusChanged(NetworkPrinter printer, int status);
    }

    class StatusSocketRunnable implements Runnable {

        private static final int SOCKET_TIMEOUT = 10000;
        private static final int PORT_PRINTER_DEFAULT = 9100;

        private NetworkPrinter mPrinter;
        private String mHost;

        StatusSocketRunnable(NetworkPrinter printer) {
            mPrinter = printer;
            mHost = printer.ipAddress;
        }

        @Override
        public void run() {
            boolean reachable = false;
            InetSocketAddress socketAddress = new InetSocketAddress(mHost, PORT_PRINTER_DEFAULT);
            Socket socket = new Socket();
            try {
                socket.connect(socketAddress, SOCKET_TIMEOUT);
                reachable = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            mPrinter.printerStatus = reachable ? NetworkPrinter.STATUS_ONLINE : NetworkPrinter.STATUS_OFFLINE;
            notifyHostStatus(mPrinter, reachable);
        }
    }
}
