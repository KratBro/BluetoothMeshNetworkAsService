package com.mobilecomputing.example.bluetoothmeshservice.service.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

/**
 * Created by Jan Urbansky on 19.12.2015.
 * <p/>
 * Übernommen aus dem RFCOMM-Server Projekt und unserem Projekt angepasst.
 * Ist die Serverseite der Verbindung.
 */
public final class ServerThread extends Thread {
    public static final String TAG = "fhflServerThread";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mServerSocket;
    private BluetoothComController mController;
    private String mServiceName;


    /**
     * @param btAdapter   BluetoothAdapter
     * @param controller  BluetoothComController
     * @param serviceName String
     */
    protected ServerThread(BluetoothAdapter btAdapter, BluetoothComController controller, String serviceName) {
        mBluetoothAdapter = btAdapter;
        mController = controller;
        mServiceName = serviceName;

        debugOut("ServerThread()");    // handler must be already initialized !!!

        if (mBluetoothAdapter == null) {
            debugOut("ServerThread(): Error: mBluetoothAdapter == null");

        } else {
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                debugOut("ServerThread(): create service-record");

                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mServiceName, BluetoothComController.MY_UUID);
            } catch (IOException e) {
                debugOut("ServerThread(): Error: IOException during get server-socket !!!");
                return;
            }
            mServerSocket = tmp;
        }

    }

    public void run() {
        BluetoothSocket socket = null;

        while (true) {
            try {
                debugOut("run(): listen");
                //blockiert den Thread -> wird vom ServerTimerThread durch aufrufen der cancel() Methode gebrochen.
                socket = mServerSocket.accept();
            } catch (IOException e) {
                debugOut("run(): Error: IOException during listen !!!");

                break;
            }

            debugOut("run(): connection accepted");

            // If a connection was accepted
            if (socket != null) {
                /*
                Sendet den BluetoothSocket an den BluetoothComController. Dieser liest den Socket aus und startet einen
                ServerThread.
                */
                mController.obtainMessage(BluetoothComController.SmMessage.AT_MANAGE_CONNECTED_SOCKET_AS_SERVER.ordinal(),
                        -1, -1, socket).sendToTarget();
                try {
                    mServerSocket.close();

                    debugOut("run(): accept server-socket closed");
                } catch (IOException e) {
                    debugOut("run(): Error: IOException during closing socket !!!");
                }

                break;
            }
        }
        debugOut("run(): thread terminates");
    }

    /**
     * Will cancel the listening socket, and cause the thread to finish
     */
    protected void cancel() {
        debugOut("cancel() ServerThread");
        try {
            mServerSocket.close();
        } catch (IOException e) {
            debugOut("cancel() ServerThread: IOException during closing socket !!!");
        }
    }

    private void debugOut(String str) {
        mController.obtainMessage(BluetoothComController.SmMessage.AT_DEBUG_SERVER.ordinal(),
                -1, -1, str).sendToTarget();
    }

}
