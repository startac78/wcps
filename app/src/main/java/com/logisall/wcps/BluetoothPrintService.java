package com.logisall.wcps;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

class BluetoothPrintService {
    // Debugging
    private static final String TAG = "BluetoothPrintService";
    private static final boolean D = true;

    // Unique UUID for this application
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private int mState;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    // Constants that indicate the current connection state
    static final int STATE_NONE = 0;       // we're doing nothing
    private static final int STATE_LISTEN = 1;     // now listening for incoming connections
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    static final int STATE_CONNECTED = 3;  // now connected to a remote device


    /**
     * Constructor. Prepares a new Bluetooth session.
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothPrintService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Set the current state of the connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if(D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the print service. Called by onResume() of the Activity
     */
    public synchronized void start() {
        if(D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure, boolean callback) {
        if (D) Log.d(TAG, "connect to: " + device);
        if (D) Log.d(TAG,"_____ connect 1 :");

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (D) Log.d(TAG,"_____ connect 2 :");

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (D) Log.d(TAG,"_____ connect 3 :");

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure, callback);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType, boolean callback) {
        if (D) Log.d(TAG, "connected, Socket Type:" + socketType);

        if (D) Log.d(TAG,"_____ connected 1 :");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (D) Log.d(TAG,"_____ connected 2 :");

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (D) Log.d(TAG,"_____ connected 3 :");
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType, callback);
        mConnectedThread.start();

        if (D) Log.d(TAG,"_____ connected 4 :");
        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();

        String address = "";
        address = device.getAddress();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        bundle.putString(MainActivity.DEVICE_MAC, address);
        bundle.putBoolean(MainActivity.BT_CALLBACK, callback);

        Log.d(TAG, "connected, device.getName() :" + device.getName());
        Log.d(TAG, "connected, device.getAddress() :" + device.getAddress());
        Log.d(TAG, "connected, device.getAddress() :" + device);

        msg.setData(bundle);
        mHandler.sendMessage(msg);

        if (D) Log.d(TAG,"_____ connected 5 :");
        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    synchronized void stop() {
        if(D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out  The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                if(D) Log.w(TAG, "BT session is not connected state");

                Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putInt(MainActivity.TOAST, R.string.not_connected);
                msg.setData(bundle);
                mHandler.sendMessage(msg);

                return;
            }
            r = mConnectedThread;
        }

        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed(boolean callback) {
//08-07 14:33:26.959: D/BluetoothPrintService(22462): _____ :java.io.IOException: Service discovery failed
        if (D) Log.d(TAG,"_____ connectionFailed 1 mState :"+mState);
        // When the application is destroyed, just return
        if (mState == STATE_NONE) return;

        if (D) Log.d(TAG,"_____ connectionFailed 2");
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        //bundle.putString(main.TOAST, "Bluetooth is disconnected");
        bundle.putString(MainActivity.TOAST, "블루투스 연결이 끊어졌습니다");
        bundle.putBoolean(MainActivity.BT_CALLBACK, callback);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        if (D) Log.d(TAG,"_____ connectionFailed 3");

        // Start the service over to restart listening mode
        BluetoothPrintService.this.start();

    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // When the application is destroyed, just return
        if (mState == STATE_NONE) return;

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putInt(MainActivity.TOAST, R.string.connect_lost);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothPrintService.this.start();
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;
        private boolean mcallback;

        public ConnectThread(BluetoothDevice device, boolean secure, boolean callback) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            mcallback = callback;
            if (D) Log.d(TAG, "_____ ConnectThread 1");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (D) Log.d(TAG, "_____ ConnectThread 2");
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                }
            } catch (IOException e) {
                if (D) Log.d(TAG, "_____ ConnectThread 3 :"+e.toString());
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "_____ ConnectThread 4");
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            if (D) Log.d(TAG, "_____ ConnectThread 5");
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                if (D) Log.d(TAG, "_____ ConnectThread 6");
                mmSocket.connect();
            } catch (IOException e) {
                if (D) Log.d(TAG, "_____ ConnectThread 7 :"+e.toString());
                // Close the socket
                try {
                    if (D) Log.d(TAG, "_____ ConnectThread 8 :");
                    mmSocket.close();
                } catch (IOException e2) {
                    if (D) Log.d(TAG, "_____ ConnectThread 9 :"+e2.toString());
                    Log.e(TAG, "unable to close() " + mSocketType + " socket during connection failure", e2);
                }
                if (D) Log.d(TAG, "_____ ConnectThread 10 :");
                connectionFailed(mcallback);
                if (D) Log.d(TAG, "_____ ConnectThread 11 :");
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothPrintService.this) {
                if (D) Log.d(TAG, "_____ ConnectThread 12 :");
                mConnectThread = null;
            }

            // Start the connected thread
            if (D) Log.d(TAG, "_____ ConnectThread 13 :");
            connected(mmSocket, mmDevice, mSocketType, mcallback);
            if (D) Log.d(TAG, "_____ ConnectThread 14 :");
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean mcallback;

        public ConnectedThread(BluetoothSocket socket, String socketType, boolean callback) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            mcallback = callback;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // buffer can be over-written by next input stream data, so it should be copied
                    byte[] rcvData = new byte[bytes];
                    rcvData = Arrays.copyOf(buffer, bytes);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, rcvData).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionFailed(mcallback);
                    //connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmInStream.close();
                mmOutStream.close();
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
